package com.neuro.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "PatientSessions")
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Integer sessionId;



    @Column(name = "session_number")
    private Integer sessionNumber;

    @Column(name = "session_date")
    private LocalDate sessionDate;

    @Column(name = "treatment_given")
    private String treatment;

    @Column(name = "pain_before")
    private String painBefore;

    @Column(name = "pain_after")
    private String painAfter;

    @Column(name = "session_summary")
    private String summary;
    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "patient_id", nullable = false)

    private PatientEntity patient;

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public Integer getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(Integer sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getPainBefore() {
        return painBefore;
    }

    public void setPainBefore(String painBefore) {
        this.painBefore = painBefore;
    }

    public String getPainAfter() {
        return painAfter;
    }

    public void setPainAfter(String painAfter) {
        this.painAfter = painAfter;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}