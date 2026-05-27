/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.license;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Offline utility used by support / packaging to mint signed license keys. The key is structured
 * as {@code machineId|expiryDate|type|hmac-sha256-signature}, then Base64-encoded for transport.
 *
 * <p>Not invoked by the running application; kept here so the same secret is used for generation
 * and validation in {@link LicenseManager}.
 */
public class LicenseGenerator {

    private static final String SECRET_KEY = "your-very-secret-key";

    /**
     * Builds and signs a license key for the given machine.
     *
     * @param machineId hardware identifier produced by {@link LicenseManager#getMachineIdentifier()}
     * @param type license type label (e.g. {@code FULL}, {@code TRIAL})
     * @param days number of days from today to expiry
     * @return a Base64-encoded license key suitable for distribution
     * @throws Exception if HMAC initialization fails
     */
    public static String generateLicense(String machineId, String type, int days) throws Exception {

        LocalDate expiryDate = LocalDate.now().plusDays(days);

        String data = machineId + "|" + expiryDate + "|" + type;

        Mac mac = Mac.getInstance("HmacSHA256");

        mac.init(new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

        String signature = Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));

        String finalLicense = data + "|" + signature;

        return Base64.getEncoder().encodeToString(finalLicense.getBytes());
    }

    public static void main(String[] args) throws Exception {

        String machineId = LicenseManager.getMachineIdentifier();

        System.out.println("Machine ID: " + machineId);

        String license = generateLicense(machineId, "FULL", 365);

        System.out.println("\nLICENSE KEY:\n" + license);
    }
}
