/*
 * Copyright (c) 2026. All rights reserved.
 */
package com.neuro.repo.impl;

import com.neuro.config.HibernateUtil;
import com.neuro.entity.PatientEntity;
import com.neuro.exceptions.DatabaseException;
import com.neuro.mapper.PatientMapper;
import com.neuro.model.Patient;
import com.neuro.model.PatientSummary;
import com.neuro.repo.Page;
import com.neuro.repo.PatientRepository;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.util.Optional;

public class HibernatePatientRepository implements PatientRepository {

    @Override
    public void savePatient(Patient patient) throws SQLException {

        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            PatientEntity entity = PatientMapper.toEntity(patient);

            session.persist(entity);

            transaction.commit();

            // Copy generated ID back to model
            patient.setPatientId(entity.getPatientId());

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new SQLException("Failed to save patient", e);
        }
    }

    @Override
    public Optional<Patient> findById(int patientId) throws DatabaseException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Page<PatientSummary> getPatientsSummaryPage(
            int userId,
            int page,
            int pageSize) throws DatabaseException {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Page<PatientSummary> searchPatientsSummaryByMobilePage(
            int userId,
            String mobile,
            int page,
            int pageSize) throws DatabaseException {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean deletePatient(int patientId, int userId)
            throws DatabaseException {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}