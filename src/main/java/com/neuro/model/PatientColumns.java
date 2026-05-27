/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.model;

public final class PatientColumns {

    private PatientColumns() {}

    public static final int PATIENT_ID = 0;

    public static final int PATIENT_NAME = 1;
    public static final int MOBILE = 2;
    public static final int AGE = 3;
    public static final int GENDER = 4;
    public static final int MARITAL_STATUS = 5;

    public static final int ADDRESS = 6;
    public static final int OCCUPATION = 7;
    public static final int BLOOD_GROUP = 8;
    public static final int HEIGHT = 9;
    public static final int WEIGHT = 10;

    public static final int SUFFERING_DURATION = 11;
    public static final int MAIN_DISEASE = 12;
    public static final int COMPLICATIONS = 13;
    public static final int SYMPTOMS = 14;
    public static final int PAIN_POINTS = 15;

    public static final int TONGUE = 16;
    public static final int STOOL = 17;
    public static final int URINE = 18;
    public static final int NAILS = 19;
    public static final int NAVEL = 20;

    public static final int NEUROTHERAPY_REQUIRED = 21;
    public static final int PREVIOUS_TREATMENT = 22;
    public static final int MEDICINES = 23;
    public static final int DETAILED_HISTORY = 24;
    public static final int EXAMINATION = 25;

    // 🔥 FIXED VITALS (separate fields, not single column)
    public static final int BP = 26;
    public static final int PULSE = 27;
    public static final int O2 = 28;
    public static final int TEMPERATURE = 29;

    public static final int USER_ID = 30;

    public static final int REPORTS = 31;
    public static final int MEDIA = 32;
    public static final int PATIENT_STORY = 33;
    public static final int REMARKS = 34;

    public static final int CREATED_AT = 35;
}
