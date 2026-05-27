/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.license;

import com.neuro.exceptions.LicenceException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Owns all license-related operations: trusted-date retrieval, machine fingerprinting, key parsing
 * and validation, persistent trial tracking, and the top-level {@link #checkLicenseOrExit()} flow.
 *
 * <p>License keys are HMAC-SHA256 signed by {@link LicenseGenerator} and stored on disk at
 * {@code ~/.neuro/license.key}.
 */
public class LicenseManager {

    private static final Logger logger = LogManager.getLogger(LicenseManager.class);

    private LicenseManager() {}

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String SECRET_KEY = "your-very-secret-key";

    private static final Path LICENSE_FILE = Paths.get(System.getProperty("user.home"), ".neuro", "license.key");

    private static final Path TRIAL_FILE = Paths.get(System.getProperty("user.home"), ".neuro", "trial.dat");

    private static void log(String message) {
        try {

            Path logFile = Paths.get(System.getProperty("user.home"), ".neuro", "license.log");

            Files.createDirectories(logFile.getParent());

            Files.writeString(
                    logFile,
                    java.time.LocalDateTime.now() + " : " + message + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

        } catch (Exception e) {
            logger.error("Failed writing license log file", e);
        }
    }

    // ================= EXTERNAL DATE =================
    /**
     * Returns a date sourced from an external time API so that the user cannot bypass expiry by
     * winding the system clock back. Falls back to {@link LocalDate#now()} if the API is
     * unreachable.
     *
     * @return today's date according to the trusted source (or local fallback)
     */
    public static LocalDate getTrustedDate() {
        try {
            URL url = new URL("https://timeapi.io/api/Time/current/zone?timeZone=UTC");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            String json = response.toString();

            // 🔹 Extract date
            String date = json.split("\"date\":\"")[1].split("\"")[0];

            System.out.println("🌐 ONLINE DATE FETCHED");

            String date1 = json.split("\"date\":\"")[1].split("\"")[0];

            System.out.println("🌐 ONLINE DATE FETCHED: " + date1);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            return LocalDate.parse(date1, formatter);

        } catch (Exception e) {
            logger.warn("Trusted-date API failed, falling back to local date", e);
            return LocalDate.now();
        }
    }
    // ================= MACHINE ID =================
    /**
     * Reads a stable hardware identifier (Mac serial number on macOS, MachineGuid on Windows).
     * Used to bind a license key to a single machine.
     *
     * @return the machine identifier, or {@code "UNKNOWN_MACHINE"} if it cannot be determined
     */
    public static String getMachineIdentifier() {

        String os = System.getProperty("os.name").toLowerCase();

        try {

            // ================= MACOS =================
            if (os.contains("mac")) {
                Process process = Runtime.getRuntime().exec(new String[] {"system_profiler", "SPHardwareDataType"});

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    System.out.println(line);
                    if (line.toLowerCase().contains("serial")) {
                        String uuid = line.split(":")[1].trim();
                        log("Mac Serial Number: " + uuid);
                        return uuid;
                    }
                }
            }

            // ================= WINDOWS / FALLBACK =================
            Process process =
                    Runtime.getRuntime().exec("reg query HKLM\\SOFTWARE\\Microsoft\\Cryptography /v MachineGuid");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                System.out.println(line);
                if (line.toLowerCase().contains("serial")) {
                    String uuid = line.split(":")[1].trim();
                    log("Mac Serial Number: " + uuid);
                    return uuid;
                }
            }

        } catch (Exception e) {
            logger.error("Machine ID resolution failed", e);
            log("Machine ID error: " + e.getMessage());
        }

        return "UNKNOWN_MACHINE";
    }

    public static void main(String[] args) {
        System.out.println(getMachineIdentifier());
    }

    // ================= VALIDATE LICENSE =================
    /**
     * Decodes a license key and verifies its signature, machine binding, and expiry against the
     * trusted date.
     *
     * @param licenseKey Base64-encoded license string
     * @return parsed {@link LicenseInfo} if valid; {@code null} if structurally invalid, bound to a
     *     different machine, signature mismatched, or expired
     * @throws LicenceException if decoding or HMAC computation throws an unexpected error
     */
    public static LicenseInfo validateLicenseKey(String licenseKey) throws LicenceException {
        try {

            if (licenseKey == null || licenseKey.isBlank()) return null;

            String decoded = new String(Base64.getDecoder().decode(licenseKey));
            String[] parts = decoded.split("\\|");

            if (parts.length != 4) return null;

            String machineId = parts[0];
            String expiryDate = parts[1];
            String type = parts[2];
            String signature = parts[3];

            String currentMachineId = getMachineIdentifier();

            String data = machineId + "|" + expiryDate + "|" + type;

            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(SECRET_KEY.getBytes(), HMAC_ALGO));

            String expectedSignature = Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));

            System.out.println("----- LICENSE DEBUG -----");
            System.out.println("Decoded: " + decoded);
            System.out.println("Machine in license: " + machineId);
            System.out.println("Current machine: " + currentMachineId);
            System.out.println("Expiry: " + expiryDate);
            System.out.println("Type: " + type);
            System.out.println("-------------------------");

            if (!machineId.equals(currentMachineId)) {
                System.out.println("❌ Machine mismatch");
                return null;
            }

            if (!expectedSignature.equals(signature)) {
                System.out.println("❌ Signature mismatch");
                return null;
            }

            LocalDate expiry = LocalDate.parse(expiryDate);

            // 🔥 Use external date here
            if (expiry.isBefore(getTrustedDate())) {
                System.out.println("❌ License expired");
                return null;
            }

            return new LicenseInfo(type, expiry);

        } catch (Exception e) {
            logger.error("License key validation failed", e);
            throw new LicenceException("Invalid License", e);
        }
    }

    // ================= TRIAL =================
    private static boolean isTrialValid() {
        try {
            if (!Files.exists(TRIAL_FILE)) {
                Files.createDirectories(TRIAL_FILE.getParent());
                Files.writeString(TRIAL_FILE, getTrustedDate().toString());
                return true;
            }

            LocalDate start = LocalDate.parse(Files.readString(TRIAL_FILE).trim());
            long daysUsed = ChronoUnit.DAYS.between(start, getTrustedDate());

            long daysLeft = 7 - daysUsed;
            System.out.println("Trial days left: " + daysLeft);

            return daysUsed < 7;

        } catch (Exception e) {
            logger.warn("Trial validity check failed; treating as invalid", e);
            return false;
        }
    }

    // ================= MAIN LICENSE CHECK =================
    /**
     * Top-level entry point invoked at application start. Handles all branches: existing license,
     * expired license, trial period, or missing key (prompts for activation).
     *
     * @return {@code true} if the app may continue; {@code false} if the user must be blocked
     */
    public static boolean checkLicenseOrExit() {

        try {
            String key = loadLicense();

            System.out.println("🔐 Starting License Check...");
            System.out.println("LICENSE KEY FROM FILE: " + key);

            if (key == null) {

                if (isTrialValid()) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Trial Mode Active\nDays left: "
                                    + (7
                                            - ChronoUnit.DAYS.between(
                                                    LocalDate.parse(Files.readString(TRIAL_FILE)
                                                            .trim()),
                                                    getTrustedDate())));
                    return true;
                }

                showLicenseDialog();
                return false;
            }

            LicenseInfo info = validateLicenseKey(key);

            if (info == null) {
                showLicenseDialog();
                return false;
            }

            if (info.daysLeft() <= 5) {
                JOptionPane.showMessageDialog(null, "License expires in " + info.daysLeft() + " days!");
            }

            return true;

        } catch (Exception e) {
            logger.error("License check failed", e);
            JOptionPane.showMessageDialog(null, "License Error");
            return false;
        }
    }

    // ================= LICENSE UI =================
    private static void showLicenseDialog() {
        LicenseDialog dialog = new LicenseDialog();
        dialog.setVisible(true);

        if (!dialog.isSuccess()) {
            System.exit(0);
        }
    }

    // ================= SAVE / LOAD =================
    /**
     * Persists a license key on disk under {@code ~/.neuro/license.key}.
     *
     * @param licenseKey Base64-encoded license to store
     * @throws IOException if the file cannot be written
     */
    public static void saveLicense(String licenseKey) throws IOException {
        Files.createDirectories(LICENSE_FILE.getParent());
        Files.writeString(LICENSE_FILE, licenseKey, StandardCharsets.UTF_8);
    }

    private static String loadLicense() {
        try {
            if (!Files.exists(LICENSE_FILE)) return null;
            return Files.readString(LICENSE_FILE, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            logger.warn("Failed reading license file {}", LICENSE_FILE, e);
            return null;
        }
    }
}
