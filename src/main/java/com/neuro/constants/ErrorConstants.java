package com.neuro.constants;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

public final class ErrorConstants {

    private ErrorConstants() {
    }
    public static final String ENTER_USERNAME_PASSWORD = "Enter username and password";

    public static final String INVALID_CREDENTIALS = "Invalid credentials";

    public static final String UNABLE_TO_LOGIN = "Unable to login";

    public static final String PATIENT_NAME_REQD = "Patient name required";

    public static final String INVALID_AGE_HEIGHT_WEIGHT = "Invalid age, height or weight";

    public static final String NO_REPORT_AVAILABLE = "No report available";

    public static final String CANNOT_OPEN_FILE = "Cannot open file";

    public static final String UNABLE_TO_LOAD_PATIENT_DETAILS = "Unable to load patient details";

    public static final String PDF_EXPORT_FAILED = "PDF export failed";

    public static final String INVALID_IMAGE = "Invalid image.Use PNG or JPG";

    public static final String UNABLE_TO_LOAD_LOGO = "Unable to load logo";

    public static final String CLINIC_NAME_REQD = "Clinic name is required";

    public static final String UNABLE_TO_SAVE_SETTINGS = "Unable to save settings";

    public static final String ALL_FIELDS_REQD = "All fields are required";

    public static final String PASSWORDS_DONT_MATCH = "Passwords do not match";

    public static final String PASSWORDS_LENGTH = "Password must be at least 5 characters";

    public static final String USERNAME_EXISTS = "Username already exists";

    public static final String UNABLE_TO_CREATE_ACCOUNT = "Unable to create account";

    public static final String UNABLE_TO_LOAD_SESSION = "Unable to load session";

    public static final String UNABLE_TO_SAVE_SESSION = "Unable to save session";

    public static final String ERROR_LOADING_PATIENTS = "Error loading patients";

    public static final String SEARCH_FAILED = "Search failed";

    public static final String SELECT_PATIENT = "Please select a patient";

    public static final String INVALID_MOBILE = "Invalid mobile number";

    public static final String INVALID_AGE = "Invalid age";

    public static final String AGE_NUMBER = "Age must be a number";

    public static final String SELECT_GENDER_MARITAL = "Select gender and marital status";

    public static final String UNABLE_TO_SAVE_PATIENT = "Unable to save patient details";

    public static final String LICENSE_GENERATION_FAILED = "License generation failed";

    public static final String LICENSE_ERROR = "License error";

    public static final String INVALID_LICENSE = "Invalid or expired license";

    public static final String ERROR_VALIDATING_LICENSE = "Error validating license";

    public static final String DUPLICATE_MOBILE = "A patient with this mobile number already exists. ";

}