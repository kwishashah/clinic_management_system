package com.neuro.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "PatientHistory")
public class PatientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Integer patientId;

    @Column(name = "patient_name", nullable = false)
    private String name;

    @Column(name = "mobile_number",unique = true)
    private String mobile;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    private String gender;

    @Column(name = "marital_status")
    private String maritalStatus;

    @Column(name = "address",columnDefinition = "TEXT")
    private String address;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "height")
    private BigDecimal height;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "suffering_duration")
    private String sufferingDuration;

    @Column(name = "main_disease",columnDefinition = "TEXT")
    private String mainDisease;

    @Column(name = "complications",columnDefinition = "TEXT")
    private String complications;

    @Column(name = "symptoms",columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "pain_points",columnDefinition = "TEXT")
    private String painPoints;

    @Column(name = "tongue")
    private String tongue;

    @Column(name = "stool")
    private String stool;

    @Column(name = "urine")
    private String urine;

    @Column(name = "nails")
    private String nails;

    @Column(name = "navel")
    private String navel;

    @Column(name = "neurotherapy_required")
    private String neurotherapyRequired;

    @Column(name = "previous_treatment",columnDefinition = "TEXT")
    private String previousTreatment;

    @Column(name = "medicines",columnDefinition = "TEXT")
    private String medicines;

    @Column(name = "detailed_history",columnDefinition = "TEXT")
    private String detailedHistory;

    @Column(name = "examination",columnDefinition = "TEXT")
    private String examination;

    @Column(name = "bp")
    private String bp;

    @Column(name = "pulse")
    private String pulse;

    @Column(name = "o2")
    private String o2;

    @Column(name = "temperature")
    private String temperature;

    @Column(name = "reports",columnDefinition = "TEXT")
    private String reports;

    @Column(name = "media",columnDefinition = "TEXT")
    private String media;

    @Column(name = "patient_story",columnDefinition = "TEXT")
    private String patientStory;

    @Column(name = "remarks",columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "user_id")
    private Integer userId;

    // ---------------- Getters and Setters ----------------

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getSufferingDuration() {
        return sufferingDuration;
    }

    public void setSufferingDuration(String sufferingDuration) {
        this.sufferingDuration = sufferingDuration;
    }

    public String getMainDisease() {
        return mainDisease;
    }

    public void setMainDisease(String mainDisease) {
        this.mainDisease = mainDisease;
    }

    public String getComplications() {
        return complications;
    }

    public void setComplications(String complications) {
        this.complications = complications;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getPainPoints() {
        return painPoints;
    }

    public void setPainPoints(String painPoints) {
        this.painPoints = painPoints;
    }

    public String getTongue() {
        return tongue;
    }

    public void setTongue(String tongue) {
        this.tongue = tongue;
    }

    public String getStool() {
        return stool;
    }

    public void setStool(String stool) {
        this.stool = stool;
    }

    public String getUrine() {
        return urine;
    }

    public void setUrine(String urine) {
        this.urine = urine;
    }

    public String getNails() {
        return nails;
    }

    public void setNails(String nails) {
        this.nails = nails;
    }

    public String getNavel() {
        return navel;
    }

    public void setNavel(String navel) {
        this.navel = navel;
    }

    public String getNeurotherapyRequired() {
        return neurotherapyRequired;
    }

    public void setNeurotherapyRequired(String neurotherapyRequired) {
        this.neurotherapyRequired = neurotherapyRequired;
    }

    public String getPreviousTreatment() {
        return previousTreatment;
    }

    public void setPreviousTreatment(String previousTreatment) {
        this.previousTreatment = previousTreatment;
    }

    public String getMedicines() {
        return medicines;
    }

    public void setMedicines(String medicines) {
        this.medicines = medicines;
    }

    public String getDetailedHistory() {
        return detailedHistory;
    }

    public void setDetailedHistory(String detailedHistory) {
        this.detailedHistory = detailedHistory;
    }

    public String getExamination() {
        return examination;
    }

    public void setExamination(String examination) {
        this.examination = examination;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public String getPulse() {
        return pulse;
    }

    public void setPulse(String pulse) {
        this.pulse = pulse;
    }

    public String getO2() {
        return o2;
    }

    public void setO2(String o2) {
        this.o2 = o2;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getReports() {
        return reports;
    }

    public void setReports(String reports) {
        this.reports = reports;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getPatientStory() {
        return patientStory;
    }

    public void setPatientStory(String patientStory) {
        this.patientStory = patientStory;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}