/*
 * Copyright (c) 2026. All rights reserved.
 */
package com.neuro.mapper;

import com.neuro.entity.PatientEntity;
import com.neuro.model.Patient;

public final class PatientMapper {

    private PatientMapper() {
        // Utility class
    }

    public static PatientEntity toEntity(Patient patient) {
        if (patient == null) {
            return null;
        }

        PatientEntity entity = new PatientEntity();

        entity.setPatientId(patient.getPatientId());
        entity.setName(patient.getName());
        entity.setMobile(patient.getMobile());
        entity.setAge(patient.getAge());
        entity.setGender(patient.getGender());
        entity.setMaritalStatus(patient.getMaritalStatus());
        entity.setAddress(patient.getAddress());
        entity.setOccupation(patient.getOccupation());
        entity.setBloodGroup(patient.getBloodGroup());

        entity.setHeight(patient.getHeight());
        entity.setWeight(patient.getWeight());

        entity.setSufferingDuration(patient.getSufferingDuration());
        entity.setMainDisease(patient.getMainDisease());
        entity.setComplications(patient.getComplications());
        entity.setSymptoms(patient.getSymptoms());
        entity.setPainPoints(patient.getPainPoints());

        entity.setTongue(patient.getTongue());
        entity.setStool(patient.getStool());
        entity.setUrine(patient.getUrine());
        entity.setNails(patient.getNails());
        entity.setNavel(patient.getNavel());

        entity.setNeurotherapyRequired(patient.getNeurotherapyRequired());
        entity.setPreviousTreatment(patient.getPreviousTreatment());
        entity.setMedicines(patient.getMedicines());
        entity.setDetailedHistory(patient.getDetailedHistory());
        entity.setExamination(patient.getExamination());

        entity.setBp(patient.getBp());
        entity.setPulse(patient.getPulse());
        entity.setO2(patient.getO2());
        entity.setTemperature(patient.getTemperature());

        entity.setReports(patient.getReports());
        entity.setMedia(patient.getMedia());
        entity.setPatientStory(patient.getPatientStory());
        entity.setRemarks(patient.getRemarks());

        entity.setCreatedAt(patient.getCreatedAt());
        entity.setUserId(patient.getUserId());

        return entity;
    }

    public static Patient toModel(PatientEntity entity) {
        if (entity == null) {
            return null;
        }

        Patient patient = new Patient();

        patient.setPatientId(entity.getPatientId());
        patient.setName(entity.getName());
        patient.setMobile(entity.getMobile());
        patient.setAge(entity.getAge());
        patient.setGender(entity.getGender());
        patient.setMaritalStatus(entity.getMaritalStatus());
        patient.setAddress(entity.getAddress());
        patient.setOccupation(entity.getOccupation());
        patient.setBloodGroup(entity.getBloodGroup());

        patient.setHeight(entity.getHeight());
        patient.setWeight(entity.getWeight());

        patient.setSufferingDuration(entity.getSufferingDuration());
        patient.setMainDisease(entity.getMainDisease());
        patient.setComplications(entity.getComplications());
        patient.setSymptoms(entity.getSymptoms());
        patient.setPainPoints(entity.getPainPoints());

        patient.setTongue(entity.getTongue());
        patient.setStool(entity.getStool());
        patient.setUrine(entity.getUrine());
        patient.setNails(entity.getNails());
        patient.setNavel(entity.getNavel());

        patient.setNeurotherapyRequired(entity.getNeurotherapyRequired());
        patient.setPreviousTreatment(entity.getPreviousTreatment());
        patient.setMedicines(entity.getMedicines());
        patient.setDetailedHistory(entity.getDetailedHistory());
        patient.setExamination(entity.getExamination());

        patient.setBp(entity.getBp());
        patient.setPulse(entity.getPulse());
        patient.setO2(entity.getO2());
        patient.setTemperature(entity.getTemperature());

        patient.setReports(entity.getReports());
        patient.setMedia(entity.getMedia());
        patient.setPatientStory(entity.getPatientStory());
        patient.setRemarks(entity.getRemarks());

        patient.setCreatedAt(entity.getCreatedAt());
        patient.setUserId(entity.getUserId());

        return patient;
    }
}