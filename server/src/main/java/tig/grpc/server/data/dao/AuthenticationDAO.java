package tig.grpc.server.data.dao;

import tig.grpc.server.data.PostgreSQLJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AuthenticationDAO {

    public static void authenticateFileAccess(String username, String fileId) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM authorizations " +
                    "WHERE username=(?) AND fileId=(?)");
            stmt.setString(1, username);
            stmt.setString(2, fileId);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                //Query was empty
                throw new IllegalArgumentException("Cannot access given document");
            }
        } catch (SQLException e) {
            //Should not happen
            throw new RuntimeException();
        }
    }
}
