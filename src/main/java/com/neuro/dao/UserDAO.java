package com.neuro.dao;

import com.neuro.db.DBConnection;
import com.neuro.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UserDAO {

    private static final Logger logger =
            LoggerFactory.getLogger(UserDAO.class);

    // ================= LOGIN VALIDATION =================
    public static boolean validateUser(
            String username,
            String password
    ) throws Exception {

        String sql =
                "SELECT password FROM users WHERE TRIM(username)=?";

        try(
                Connection conn=
                        DBConnection.getConnection();

                PreparedStatement stmt=
                        conn.prepareStatement(sql)
        ){

            logger.info(
                    "Login validation requested for username={}",
                    username
            );

            stmt.setString(
                    1,
                    username.trim()
            );

            try(
                    ResultSet rs=
                            stmt.executeQuery()
            ){

                if(rs.next()){

                    logger.debug(
                            "User found in database username={}",
                            username
                    );

                    String storedHash =
                            rs.getString("password");

                    boolean valid =
                            PasswordUtil.verify(
                                    password,
                                    storedHash
                            );

                    if(valid){
                        logger.info(
                                "Login success username={}",
                                username
                        );
                    }else{
                        logger.warn(
                                "Invalid password username={}",
                                username
                        );
                    }

                    return valid;
                }
            }

            logger.warn(
                    "Login failed user not found username={}",
                    username
            );

            return false;

        }catch(Exception e){

            logger.error(
                    "Login validation failed username={}",
                    username,
                    e
            );

            throw e;
        }
    }


    // ================= CHECK USERS EXIST =================
    public static boolean hasAnyUser(){

        String sql="SELECT COUNT(*) FROM users";

        try(
                Connection con=
                        DBConnection.getConnection();

                PreparedStatement ps=
                        con.prepareStatement(sql);

                ResultSet rs=
                        ps.executeQuery()
        ){

            boolean exists =
                    rs.next() &&
                            rs.getInt(1)>0;

            logger.info(
                    "Has any user check result={}",
                    exists
            );

            return exists;

        }catch(Exception e){

            logger.error(
                    "Failed checking existing users",
                    e
            );

            return false;
        }
    }


    // ================= CREATE USER =================
    public static boolean insertUser(
            String username,
            String password
    ) throws Exception {

        logger.info(
                "Creating user username={}",
                username
        );

        String hashedPassword =
                PasswordUtil.hash(password);

        String sql=
                "INSERT INTO users(username,password) VALUES (?,?)";

        try(
                Connection conn=
                        DBConnection.getConnection();

                PreparedStatement stmt=
                        conn.prepareStatement(sql)
        ){

            stmt.setString(
                    1,
                    username.trim()
            );

            stmt.setString(
                    2,
                    hashedPassword
            );

            int rows=
                    stmt.executeUpdate();

            logger.info(
                    "User created username={} rows={}",
                    username,
                    rows
            );

            return rows>0;

        }catch(SQLException e){

            logger.error(
                    "User creation failed username={}",
                    username,
                    e
            );

            throw e;
        }
    }


    // ================= USER EXISTS =================
    public static boolean userExists(
            String username
    ) throws Exception {

        String sql=
                "SELECT 1 FROM users WHERE TRIM(username)=?";

        try(
                Connection con=
                        DBConnection.getConnection();

                PreparedStatement ps=
                        con.prepareStatement(sql)
        ){

            ps.setString(
                    1,
                    username.trim()
            );

            try(
                    ResultSet rs=
                            ps.executeQuery()
            ){

                boolean exists =
                        rs.next();

                logger.info(
                        "Username exists check username={} result={}",
                        username,
                        exists
                );

                return exists;
            }

        }catch(Exception e){

            logger.error(
                    "User existence check failed username={}",
                    username,
                    e
            );

            throw e;
        }
    }



    // ================= ENCRYPT OLD PASSWORDS =================
    public static void encryptExistingPasswords()
            throws Exception {

        logger.info(
                "Starting legacy password encryption"
        );

        String select=
                "SELECT username,password FROM users";

        try(
                Connection conn=
                        DBConnection.getConnection();

                PreparedStatement ps=
                        conn.prepareStatement(select);

                ResultSet rs=
                        ps.executeQuery()
        ){

            while(rs.next()){

                String username=
                        rs.getString(
                                "username"
                        );

                String plainPassword=
                        rs.getString(
                                "password"
                        );

                if(
                        plainPassword!=null &&
                                plainPassword.length()>20
                ){

                    logger.debug(
                            "Skipping already hashed user={}",
                            username
                    );

                    continue;
                }

                String hashed=
                        PasswordUtil.hash(
                                plainPassword
                        );

                String update=
                        "UPDATE users SET password=? WHERE username=?";

                try(
                        PreparedStatement up=
                                conn.prepareStatement(update)
                ){

                    up.setString(
                            1,
                            hashed
                    );

                    up.setString(
                            2,
                            username
                    );

                    up.executeUpdate();

                    logger.info(
                            "Encrypted password for user={}",
                            username
                    );
                }
            }
        }

        logger.info(
                "Legacy password encryption completed"
        );
    }



    // ================= GET USER ID =================
    public static int getUserId(
            String username
    ) throws Exception {

        String sql=
                "SELECT user_id FROM users WHERE TRIM(username)=?";

        try(
                Connection conn=
                        DBConnection.getConnection();

                PreparedStatement stmt=
                        conn.prepareStatement(sql)
        ){

            stmt.setString(
                    1,
                    username.trim()
            );

            try(
                    ResultSet rs=
                            stmt.executeQuery()
            ){

                if(rs.next()){

                    int userId=
                            rs.getInt(
                                    "user_id"
                            );

                    logger.info(
                            "Resolved userId={} for username={}",
                            userId,
                            username
                    );

                    return userId;
                }
            }

            logger.warn(
                    "No userId found for username={}",
                    username
            );

            return -1;

        }catch(Exception e){

            logger.error(
                    "getUserId failed username={}",
                    username,
                    e
            );

            throw e;
        }
    }

}