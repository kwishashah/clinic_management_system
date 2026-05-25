package com.neuro.license;

import com.neuro.license.LicenseManager;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
public class LicenseGenerator {

    private static final String SECRET_KEY =
            "your-very-secret-key";

    public static String generateLicense(
            String machineId,
            String type,
            int days
    ) throws Exception {

        LocalDate expiryDate =
                LocalDate.now().plusDays(days);

        String data =
                machineId
                        + "|"
                        + expiryDate
                        + "|"
                        + type;

        Mac mac = Mac.getInstance("HmacSHA256");

        mac.init(
                new SecretKeySpec(
                        SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"
                )
        );

        String signature =
                Base64.getEncoder()
                        .encodeToString(
                                mac.doFinal(data.getBytes())
                        );

        String finalLicense =
                data + "|" + signature;

        return Base64.getEncoder()
                .encodeToString(finalLicense.getBytes());
    }

    public static void main(String[] args)
            throws Exception {

        String machineId =
                LicenseManager.getMachineIdentifier();

        System.out.println(
                "Machine ID: " + machineId
        );

        String license =
                generateLicense(
                        machineId,
                        "FULL",
                        365
                );

        System.out.println(
                "\nLICENSE KEY:\n" + license
        );
    }
}