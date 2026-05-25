package com.neuro.license;

import com.neuro.exceptions.LicenceException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Enumeration;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class LicenseManager {

    private LicenseManager() {}

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String SECRET_KEY = "your-very-secret-key";

    private static final Path LICENSE_FILE = Paths.get(
            System.getProperty("user.home"), ".neuro", "license.key"
    );

    private static final Path TRIAL_FILE = Paths.get(
            System.getProperty("user.home"), ".neuro", "trial.dat"
    );

    private static void log(String message) {
        try {

            Path logFile = Paths.get(
                    System.getProperty("user.home"),
                    ".neuro",
                    "license.log"
            );

            Files.createDirectories(logFile.getParent());

            Files.writeString(
                    logFile,
                    java.time.LocalDateTime.now()
                            + " : "
                            + message
                            + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= EXTERNAL DATE =================
    public static LocalDate getTrustedDate() {
        try {
            URL url = new URL("https://timeapi.io/api/Time/current/zone?timeZone=UTC");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

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
            System.out.println("❌ API ERROR: " + e.getMessage());
            System.out.println("⚠️ Falling back to local date");

            return LocalDate.now();
        }
    }
    // ================= MACHINE ID =================
    public static String getMachineIdentifier() {

        String os =
                System.getProperty("os.name").toLowerCase();

        try {

            // ================= MACOS =================
            if (os.contains("mac")) {
                Process process = Runtime.getRuntime().exec(
                        new String[]{
                                "system_profiler",
                                "SPHardwareDataType"
                        }
                );

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    System.out.println(line);
                    if (line.toLowerCase().contains("serial")) {
                        String uuid =
                                line.split(":")[1].trim();
                        log("Mac Serial Number: " + uuid);
                        return uuid;
                    }
                }
            }

            // ================= WINDOWS / FALLBACK =================
            Process process = Runtime.getRuntime().exec("reg query HKLM\\SOFTWARE\\Microsoft\\Cryptography /v MachineGuid");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                System.out.println(line);
                if (line.toLowerCase().contains("serial")) {
                    String uuid =
                            line.split(":")[1].trim();
                    log("Mac Serial Number: " + uuid);
                    return uuid;
                }
            }

        } catch (Exception e) {

            log("Machine ID error: "
                    + e.getMessage());
        }

        return "UNKNOWN_MACHINE";
    }

    public static void main(String[] args) {
        System.out.println(getMachineIdentifier());
    }

    // ================= VALIDATE LICENSE =================
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

            String expectedSignature = Base64.getEncoder()
                    .encodeToString(mac.doFinal(data.getBytes()));

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
            throw new LicenceException("Invalid License", e);
        }
    }

    // ================= TRIAL =================
    public static boolean isTrialValid() {
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
            return false;
        }
    }

    // ================= MAIN LICENSE CHECK =================
    public static boolean checkLicenseOrExit() {

        try {
            String key = loadLicense();

            System.out.println("🔐 Starting License Check...");
            System.out.println("LICENSE KEY FROM FILE: " + key);

            if (key == null) {

                if (isTrialValid()) {
                    JOptionPane.showMessageDialog(null,
                            "Trial Mode Active\nDays left: " +
                                    (7 - ChronoUnit.DAYS.between(
                                            LocalDate.parse(Files.readString(TRIAL_FILE).trim()),
                                            getTrustedDate()
                                    )));
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
                JOptionPane.showMessageDialog(null,
                        "License expires in " + info.daysLeft() + " days!");
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
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
    public static void saveLicense(String licenseKey) throws IOException {
        Files.createDirectories(LICENSE_FILE.getParent());
        Files.writeString(LICENSE_FILE, licenseKey, StandardCharsets.UTF_8);
    }

    public static String loadLicense() {
        try {
            if (!Files.exists(LICENSE_FILE)) return null;
            return Files.readString(LICENSE_FILE, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return null;
        }
    }
}