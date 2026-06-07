/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.model;

/**
 * Lightweight projection of a patient row used by list / search views. Carries only the columns
 * needed to populate the dashboard table.
 */
public record PatientSummary(int patientId, String name, String mobile, Integer age, String gender) {}
