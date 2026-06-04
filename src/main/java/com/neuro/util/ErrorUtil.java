package com.neuro.util;
public final class ErrorUtil {

    private ErrorUtil() {}

    public static String getUserMessage(Exception e) {

        String msg = e == null ? "" : String.valueOf(e.getMessage());

        if (msg == null) {
            msg = "";
        }

        msg = msg.toLowerCase();

        // ======================
        // PATIENT VALIDATION
        // ======================

        if (msg.contains("patient name")) {
            return "Patient name is required.";
        }

        if (msg.contains("invalid mobile")) {
            return "Mobile number must contain exactly 10 digits.";
        }

        if (msg.contains("invalid age")) {
            return "Please enter a valid age.";
        }

        if (msg.contains("age must")) {
            return "Age must contain numbers only.";
        }

        if (msg.contains("invalid height")) {
            return "Please enter a valid height.";
        }

        if (msg.contains("invalid weight")) {
            return "Please enter a valid weight.";
        }

        if (msg.contains("invalid pulse")) {
            return "Please enter a valid pulse rate.";
        }

        if (msg.contains("invalid bp")) {
            return "Please enter a valid blood pressure value.";
        }

        if (msg.contains("invalid oxygen")) {
            return "Please enter a valid oxygen saturation value.";
        }

        if (msg.contains("invalid temperature")) {
            return "Please enter a valid temperature.";
        }

        if (msg.contains("gender")) {
            return "Please select gender and marital status.";
        }

        // ======================
        // LOGIN
        // ======================

        if (msg.contains("invalid credentials")) {
            return "Incorrect username or password.";
        }

        if (msg.contains("enter username")) {
            return "Please enter username and password.";
        }

        if (msg.contains("login")) {
            return "Unable to login. Please try again.";
        }

        // ======================
        // SIGNUP
        // ======================

        if (msg.contains("all fields")) {
            return "Please fill all required fields.";
        }

        if (msg.contains("passwords do not match")) {
            return "Passwords do not match.";
        }

        if (msg.contains("password must")) {
            return "Password must be at least 5 characters.";
        }

        if (msg.contains("username already")) {
            return "Username already exists.";
        }

        // ======================
        // DATABASE
        // ======================

        if (msg.contains("duplicate entry")) {

            if (msg.contains("mobile")) {
                return "A patient with this mobile number already exists.";
            }

            if (msg.contains("username")) {
                return "Username already exists.";
            }

            return "This record already exists.";
        }

        if (msg.contains("foreign key")) {
            return "Referenced data is invalid.";
        }

        if (msg.contains("cannot be null")) {
            return "Please fill all required fields.";
        }

        if (msg.contains("data too long")) {
            return "One of the entered values is too long.";
        }

        if (msg.contains("access denied")) {
            return "Database login failed.";
        }

        if (msg.contains("communications link failure")
                || msg.contains("connection refused")
                || msg.contains("unable to connect")) {

            return "Unable to connect to the database.";
        }

        if (msg.contains("doesn't exist")) {
            return "Database configuration is incomplete.";
        }

        // ======================
        // PDF / REPORTS
        // ======================

        if (msg.contains("pdf")) {
            return "Unable to generate PDF.";
        }

        if (msg.contains("report")) {
            return "Unable to process the report.";
        }

        // ======================
        // FILES
        // ======================

        if (msg.contains("file not found")) {
            return "Selected file could not be found.";
        }

        if (msg.contains("cannot open file")) {
            return "Unable to open the selected file.";
        }

        if (msg.contains("access is denied")) {
            return "Permission denied while accessing the file.";
        }

        if (msg.contains("invalid image")) {
            return "Please select a PNG or JPG image.";
        }

        if (msg.contains("image")) {
            return "Unable to load the image.";
        }

        // ======================
        // CLINIC SETTINGS
        // ======================

        if (msg.contains("clinic name")) {
            return "Clinic name is required.";
        }

        // ======================
        // LICENSE
        // ======================

        if (msg.contains("license expired")) {
            return "Your license has expired.";
        }

        if (msg.contains("invalid license")) {
            return "License key is invalid.";
        }

        if (msg.contains("license")) {
            return "License validation failed.";
        }

        // ======================
        // FALLBACK
        // ======================

        return "Operation failed. Please try again.";
    }
}