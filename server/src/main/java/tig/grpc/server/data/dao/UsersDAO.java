package tig.grpc.server.data.dao;

import tig.grpc.server.data.PostgreSQLJDBC;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
}

