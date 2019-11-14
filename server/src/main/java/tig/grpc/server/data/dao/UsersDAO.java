package tig.grpc.server.data.dao;

import tig.utils.db.PostgreSQLJDBC;
import tig.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersDAO {

    public static void insertUser(String username) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES(?)");
            stmt.setString(1, username);
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

}

