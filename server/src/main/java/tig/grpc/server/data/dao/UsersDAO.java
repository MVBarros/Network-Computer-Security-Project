package tig.grpc.server.data.dao;

import tig.grpc.server.data.PostgreSQLJDBC;
import tig.grpc.server.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersDAO {

    public static void insertUser(String username, String password) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES(?,?,?,?)");
            stmt.setString(1, username);
            byte[] salt = PasswordUtils.generateRandomSalt();
            int iterations = PasswordUtils.iterations;
            byte[] hash = PasswordUtils.generateStrongPasswordHash(password, salt, iterations);
            stmt.setBytes(2, hash);
            stmt.setBytes(3, salt);
            stmt.setInt(3, iterations);
            int result = stmt.executeUpdate();
            if (result == 0) {
                //Should never happen
                throw new RuntimeException();
            }
        } catch (SQLException e) {
            //Username already exists (Will happen because of a PrimaryKeyViolation)
            throw new IllegalArgumentException("Username already in use");
        }
    }

    public static void authenticateUser(String username, String password) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT PasswordHash, PasswordSalt, Iterations FROM users WHERE username =(?)");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            //Get single result
            if (rs.next()) {
                byte[] realHash = rs.getBytes("PasswordHash");
                byte[] salt = rs.getBytes("PasswordSalt");
                int iterations = rs.getInt("Iterations");
                byte[] calculatedHash = PasswordUtils.generateStrongPasswordHash(password, salt, iterations);

                if (!PasswordUtils.validatePassword(realHash, calculatedHash)) {
                    throw new IllegalArgumentException("Invalid Password or Username");
                }
            } else {
                //Query was empty
                throw new IllegalArgumentException("No such Username");
            }

        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }
}

