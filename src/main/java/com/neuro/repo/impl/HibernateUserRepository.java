/*
 * Copyright (c) 2026. All rights reserved.
 */
package com.neuro.repo.impl;

import com.neuro.entity.UserEntity;
import com.neuro.exceptions.DatabaseException;
import com.neuro.repo.UserRepository;
import com.neuro.util.HibernateUtil;
import com.neuro.util.PasswordUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateUserRepository implements UserRepository {

    private static final Logger logger =
            LogManager.getLogger(HibernateUserRepository.class);

    @Override
    public boolean validateUser(String username, String password) {

        logger.info("Login validation requested for username={}", username);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            UserEntity user = session.createQuery(
                            "FROM UserEntity u WHERE u.username = :username",
                            UserEntity.class)
                    .setParameter("username", username.trim())
                    .uniqueResult();

            if (user == null) {
                logger.warn("Login failed user not found username={}", username);
                return false;
            }

            boolean valid =
                    PasswordUtil.verify(password, user.getPassword());

            if (valid) {
                logger.info("Login success username={}", username);
            } else {
                logger.warn("Invalid password username={}", username);
            }

            return valid;

        } catch (Exception e) {

            logger.error("Login validation failed for username={}", username, e);

            throw new DatabaseException(
                    "Login validation failed for username=" + username,
                    e);
        }
    }

    @Override
    public boolean hasAnyUser() {

        logger.info("hasAnyUser() called");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Long count = session.createQuery(
                            "SELECT COUNT(u) FROM UserEntity u",
                            Long.class)
                    .uniqueResult();

            boolean exists = count != null && count > 0;

            logger.info("Has any user check result={}", exists);

            return exists;

        } catch (Exception e) {

            logger.error("Failed checking whether any user exists", e);

            throw new DatabaseException(
                    "Failed checking whether any user exists",
                    e);
        }
    }

    @Override
    public boolean insertUser(String username, String password) {

        logger.info("Creating user username={}", username);

        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            tx = session.beginTransaction();

            UserEntity user = new UserEntity();

            user.setUsername(username.trim());
            user.setPassword(PasswordUtil.hash(password));

            session.persist(user);

            tx.commit();

            logger.info("User created username={}", username);

            return true;

        } catch (Exception e) {

            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            logger.error("User creation failed for username={}", username, e);

            throw new DatabaseException(
                    "User creation failed for username=" + username,
                    e);
        }
    }

    @Override
    public boolean userExists(String username) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Long count = session.createQuery(
                            "SELECT COUNT(u) FROM UserEntity u WHERE u.username = :username",
                            Long.class)
                    .setParameter("username", username.trim())
                    .uniqueResult();

            boolean exists = count != null && count > 0;

            logger.info(
                    "Username exists check username={} result={}",
                    username,
                    exists);

            return exists;

        } catch (Exception e) {

            logger.error(
                    "User existence check failed for username={}",
                    username,
                    e);

            throw new DatabaseException(
                    "User existence check failed for username=" + username,
                    e);
        }
    }

    @Override
    public int getUserId(String username) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            UserEntity user = session.createQuery(
                            "FROM UserEntity u WHERE u.username = :username",
                            UserEntity.class)
                    .setParameter("username", username.trim())
                    .uniqueResult();

            if (user != null) {

                logger.info(
                        "Resolved userId={} for username={}",
                        user.getUserId(),
                        username);

                return user.getUserId();
            }

            logger.warn("No userId found for username={}", username);

            return -1;

        } catch (Exception e) {

            logger.error(
                    "getUserId failed for username={}",
                    username,
                    e);

            throw new DatabaseException(
                    "getUserId failed for username=" + username,
                    e);
        }
    }
}