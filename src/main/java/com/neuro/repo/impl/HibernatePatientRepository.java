package com.neuro.repo.impl;

import com.neuro.entity.PatientEntity;
import com.neuro.exceptions.DatabaseException;
import com.neuro.mapper.PatientMapper;
import com.neuro.model.Patient;
import com.neuro.model.PatientSummary;
import com.neuro.repo.Page;
import com.neuro.repo.PatientRepository;
import com.neuro.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.Objects;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
public class HibernatePatientRepository implements PatientRepository {

    private static final Logger logger =
            LogManager.getLogger(HibernatePatientRepository.class);
    // ================= SAVE =================
    @Override
    public void savePatient(Patient patient) throws SQLException {
        Objects.requireNonNull(patient, "patient");

        if (patient.getUserId() == null) {
            throw new IllegalArgumentException("patient.userId is required");
        }
        logger.info("Saving patient '{}' for userId={}", patient.getName(), patient.getUserId());
        Session session = null;
        Transaction transaction = null;

        try {

            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            PatientEntity entity = PatientMapper.toEntity(patient);

            session.persist(entity);

            transaction.commit();
            logger.info(
                    "Patient saved successfully patientId={} mobile={}",
                    entity.getPatientId(),
                    patient.getMobile());
            patient.setPatientId(entity.getPatientId());


        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error(
                    "Failed saving patient for userId={}",
                    patient.getUserId(),
                    e);

            e.printStackTrace();
            throw new SQLException("Failed to save patient", e);
            //throw new SQLException("Failed to save patient", e);

        } finally {

            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    // ================= FIND BY ID =================
    @Override
    public Optional<Patient> findById(int patientId) throws DatabaseException {
        logger.info(
                "Fetching patient by id={}",
                patientId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            PatientEntity entity = session.get(PatientEntity.class, patientId);
            if (entity == null) {
                logger.warn("No patient found for id={}", patientId);
                //return Optional.empty();
            }
            return entity == null
                    ? Optional.empty()
                    : Optional.of(PatientMapper.toModel(entity));

        } catch (Exception e) {
            logger.error(
                    "Failed fetching patient id={}",
                    patientId,
                    e);
            throw new DatabaseException("Failed to fetch patient by id", e);
        }
    }

    // ================= PAGINATED LIST =================
    @Override
    public Page<PatientSummary> getPatientsSummaryPage(
            int userId,
            int page,
            int pageSize) throws DatabaseException {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, pageSize);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

           // int offset = page * pageSize;
            //count query
            Long total = session.createQuery(
                            "SELECT COUNT(p) FROM PatientEntity p WHERE p.userId = :uid",
                            Long.class)
                    .setParameter("uid", userId)
                    .uniqueResult();
            int totalPages = total == 0
                    ? 1
                    : (int) Math.ceil(total / (double) safeSize);
            int clampedPage = Math.min(safePage, totalPages - 1);
            int offset = clampedPage * safeSize;
            // data query
            List<PatientEntity> list = session.createQuery(
                            "FROM PatientEntity p WHERE p.userId = :uid ORDER BY p.patientId DESC",
                            PatientEntity.class)
                    .setParameter("uid", userId)
                    .setFirstResult(offset)
                    .setMaxResults(safeSize)
                    .getResultList();



            List<PatientSummary> items = list.stream()
                    .map(p -> new PatientSummary(
                            p.getPatientId(),
                            p.getName(),
                            p.getMobile(),
                            p.getAge(),
                            p.getGender()
                    ))
                    .toList();
            logger.info(
                    "Loaded patient page userId={} page={} pageSize={} returned={} total={}",
                    userId,
                    page,
                    pageSize,
                    items.size(),
                    total);

            //return new Page<>(items, total.intValue(), page, pageSize);
            return new Page<>(
                    items,
                     total.intValue(),
                    clampedPage,
                    safeSize
            );

        } catch (Exception e) {
            logger.error(
                    "Failed loading patient page for userId={}",
                    userId,
                    e);
            throw new DatabaseException("Failed to load patient page", e);
        }
    }

    // ================= SEARCH BY MOBILE =================
    @Override
    public Page<PatientSummary> searchPatientsSummaryByMobilePage(
            int userId,
            String mobile,
            int page,
            int pageSize) throws DatabaseException {
        logger.info(
                "Searching patients userId={} mobile={} page={} pageSize={}",
                userId,
                mobile,
                page,
                pageSize);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            //int offset = page * pageSize;
            int safePage = Math.max(0, page);
            int safeSize = Math.max(1, pageSize);
            Long total = session.createQuery(
                            "SELECT COUNT(p) FROM PatientEntity p WHERE p.userId = :uid",
                            Long.class)
                    .setParameter("uid", userId)
                    .uniqueResult();
            int totalPages = total == 0
                    ? 1
                    : (int) Math.ceil(total / (double) safeSize);
            int clampedPage = Math.min(safePage, totalPages - 1);
            int offset = clampedPage * safeSize;
            List<PatientEntity> list = session.createQuery(
                            "FROM PatientEntity p " +
                                    "WHERE p.userId = :uid AND p.mobile LIKE :mob " +
                                    "ORDER BY p.patientId DESC",
                            PatientEntity.class)
                    .setParameter("uid", userId)
                    .setParameter("mob", "%" + mobile + "%")
                    .setFirstResult(offset)
                    .setMaxResults(safeSize)
                    .getResultList();



            List<PatientSummary> items = list.stream()
                    .map(p -> new PatientSummary(
                            p.getPatientId(),
                            p.getName(),
                            p.getMobile(),
                            p.getAge(),
                            p.getGender()
                    ))
                    .toList();
            logger.info(
                    "Search completed userId={} mobile={} returned={} total={}",
                    userId,
                    mobile,
                    items.size(),
                    total);
            return new Page<>(items, total.intValue(), clampedPage, safeSize);

        } catch (Exception e) {
            logger.error(
                    "Failed searching patients userId={} mobile={}",
                    userId,
                    mobile,
                    e);
            throw new DatabaseException("Failed to search patients", e);
        }
    }

    // ================= DELETE =================
    @Override
    public boolean deletePatient(int patientId, int userId) throws DatabaseException {
        logger.info(
                "Deleting patient id={} userId={}",
                patientId,
                userId);
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            tx = session.beginTransaction();

            PatientEntity entity = session.get(PatientEntity.class, patientId);
            if (entity == null || !userIdEquals(entity, userId)) {
                logger.warn(
                        "Delete failed. Patient id={} not found for userId={}",
                        patientId,
                        userId);
                return false;
            }
            if (entity == null || !userIdEquals(entity, userId)) {
                tx.rollback();
                return false;
            }

            session.remove(entity);

            tx.commit();
            logger.info(
                    "Patient deleted successfully patientId={} userId={}",
                    patientId,
                    userId);
            return true;

        } catch (Exception e) {
            logger.error(
                    "Failed deleting patient id={} userId={}",
                    patientId,
                    userId,
                    e);
            if (tx != null && tx.isActive() ) tx.rollback();

            throw new DatabaseException("Failed to delete patient", e);
        }
    }

    private boolean userIdEquals(PatientEntity entity, int userId) {
        return entity.getUserId() != null && entity.getUserId() == userId;
    }
    @Override
    public boolean mobileExists(String mobile) throws DatabaseException {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Long count = session.createQuery(
                            "select count(p) from PatientEntity p where p.mobile = :mobile",
                            Long.class)
                    .setParameter("mobile", mobile)
                    .uniqueResult();

            return count != null && count > 0;

        } catch (Exception e) {
            throw new DatabaseException("Failed checking mobile number", e);
        }
    }
}