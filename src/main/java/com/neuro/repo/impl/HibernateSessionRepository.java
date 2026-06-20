package com.neuro.repo.impl;

import com.neuro.entity.SessionEntity;
import com.neuro.exceptions.DatabaseException;
import com.neuro.model.Session;
import com.neuro.repo.SessionRepository;
import com.neuro.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class HibernateSessionRepository implements SessionRepository {

    private static final Logger logger =
            LogManager.getLogger(HibernateSessionRepository.class);

    @Override
    public List<Session> getSessionsByPatient(int patientId) {

        logger.debug("Loading sessions for patientId={}", patientId);

        try (org.hibernate.Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            List<SessionEntity> entities = session.createQuery(
                            "FROM SessionEntity s WHERE s.patientId=:pid ORDER BY s.sessionNumber",
                            SessionEntity.class)
                    .setParameter("pid", patientId)
                    .getResultList();

            List<Session> data = new ArrayList<>();

            for (SessionEntity e : entities) {

                data.add(new Session(
                        e.getSessionId(),
                        e.getSessionNumber(),
                        e.getSessionDate(),
                        e.getTreatment(),
                        e.getPainBefore(),
                        e.getPainAfter(),
                        e.getSummary()
                ));
            }

            if (data.isEmpty()) {
                logger.warn("No sessions found for patientId={}", patientId);
            }

            logger.info("Loaded {} sessions for patientId={}",
                    data.size(), patientId);

            return data;

        } catch (Exception e) {

            logger.error("Failed loading sessions for patientId={}",
                    patientId, e);

            throw new DatabaseException(
                    "Failed loading sessions for patientId=" + patientId, e);
        }
    }

    @Override
    public int getNextSessionNumber(int patientId) {

        logger.debug("Fetching next session number for patientId={}",
                patientId);

        try (org.hibernate.Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            Integer max = session.createQuery(
                            "select max(s.sessionNumber) from SessionEntity s where s.patientId=:pid",
                            Integer.class)
                    .setParameter("pid", patientId)
                    .uniqueResult();

            int next = (max == null) ? 1 : max + 1;

            logger.info("Next session number={} for patientId={}",
                    next, patientId);

            return next;

        } catch (Exception e) {

            logger.error("Error getting next session number patientId={}",
                    patientId, e);

            throw new DatabaseException(
                    "Error getting next session number patientId=" + patientId,
                    e);
        }
    }

    @Override
    public void addSession(int patientId, Session model)
            throws DatabaseException {

        logger.info("Saving session patientId={} sessionNo={}",
                patientId, model.sessionNumber());

        Transaction tx = null;

        org.hibernate.Session session = null;

        try {

            session = HibernateUtil.getSessionFactory().openSession();

            tx = session.beginTransaction();

            SessionEntity entity = new SessionEntity();

            entity.setPatientId(patientId);
            entity.setSessionNumber(model.sessionNumber());
            entity.setSessionDate(model.sessionDate());
            entity.setTreatment(model.treatment());
            entity.setPainBefore(model.painBefore());
            entity.setPainAfter(model.painAfter());
            entity.setSummary(model.summary());

            session.persist(entity);

            tx.commit();

            logger.info("Session saved successfully patientId={}",
                    patientId);

        } catch (Exception e) {

            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            logger.error("Session save failed patientId={}",
                    patientId, e);

            throw new DatabaseException(
                    "Session save failed patientId=" + patientId, e);

        } finally {

            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void updateSession(int patientId, Session model)
            throws DatabaseException {

        logger.info("Updating sessionId={} patientId={}",
                model.sessionId(), patientId);

        Transaction tx = null;

        org.hibernate.Session session = null;

        try {

            session = HibernateUtil.getSessionFactory().openSession();

            tx = session.beginTransaction();

            SessionEntity entity =
                    session.get(SessionEntity.class, model.sessionId());

            if (entity == null) {
                throw new DatabaseException("Session not found");
            }

            entity.setSessionNumber(model.sessionNumber());
            entity.setSessionDate(model.sessionDate());
            entity.setTreatment(model.treatment());
            entity.setPainBefore(model.painBefore());
            entity.setPainAfter(model.painAfter());
            entity.setSummary(model.summary());

            session.merge(entity);

            tx.commit();

            logger.info("Session updated successfully sessionId={}",
                    model.sessionId());

        } catch (Exception e) {

            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            logger.error("Session update failed sessionId={}",
                    model.sessionId(), e);

            throw new DatabaseException(
                    "Session update failed sessionId=" + model.sessionId(),
                    e);

        } finally {

            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}