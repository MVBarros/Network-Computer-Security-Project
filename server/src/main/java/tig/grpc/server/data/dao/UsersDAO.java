package tig.grpc.server.data.dao;

import tig.grpc.server.data.PostgreSQLJDBC;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

public class UsersDAO {

    public static void insertUser(String username, String password) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES(?,?,?)");
            stmt.setString(1, username);
            String uuid = UUID.randomUUID().toString();

            //add salt to string to decrease the chance of the resulting password being in a SHA table
            password += uuid;

            byte[] b = password.getBytes(StandardCharsets.UTF_8);

            //FIXME Lookup Google Guava Library

            stmt.setBytes(2, MessageDigest.getInstance("SHA-256").digest(b));
            stmt.setString(3, uuid);

            stmt.executeUpdate();

        } catch (SQLException e) {
            //Username already exists
            throw new IllegalArgumentException("Username already in use");
        } catch (NoSuchAlgorithmException e) {
            //will never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static void authenticateUser(String username, String password) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT password, salt FROM users WHERE username =(?)");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            byte[] realSha = rs.getBytes("password");
            password += rs.getString("salt");

            byte[] requestSha = password.getBytes(StandardCharsets.UTF_8);
            requestSha = MessageDigest.getInstance("SHA-256").digest(requestSha);

            if (!Arrays.equals(requestSha, realSha)) {
                throw new IllegalArgumentException("Invalid Password");
            }

        } catch (SQLException e) {
            //Username does not exist
            throw new IllegalArgumentException("No such Username");
        } catch (NoSuchAlgorithmException e) {
            //will never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }




    public static void updateAccessControl(String username, String filename, Boolean auth) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("SELECT fileid FROM authorizations WHERE username = (?) and filename = (?)");
            stmt.setString(1, username);
            stmt.setString(2, filename);
            ResultSet rs = stmt.executeQuery();


            if (rs.next()) {
                // apagar o file
                PreparedStatement update_stmt = conn.prepareStatement("UPDATE authorizations SET public =(?) WHERE fileid =(?)");
                update_stmt.setBoolean(1, auth);
                update_stmt.setString(2, rs.getString("fileid"));
                update_stmt.executeUpdate();
            } else
                throw new IllegalArgumentException("No such file name.");


        } catch (SQLException e) {
            // TODO rever
            throw new IllegalArgumentException("No such file name.");
        }

    }

    public static void listFiles(String username) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        PreparedStatement stmt = null;
        try {
            // TODO acabar isto
            stmt = conn.prepareStatement("SELECT filename FROM authorizations WHERE username = (?) or public = 1");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();




        } catch (SQLException e) {
            // TODO rever
            throw new IllegalArgumentException("No such file name.");
        }

    }
}

