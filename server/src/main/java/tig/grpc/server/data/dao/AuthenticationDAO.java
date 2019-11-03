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

    public static void updateAccessControl(String username, String fileid, Boolean auth) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        PreparedStatement stmt = null;
        try {
                PreparedStatement update_stmt = conn.prepareStatement("UPDATE authorizations SET public =(?) WHERE fileid =(?)");
                update_stmt.setBoolean(1, auth);
                update_stmt.setString(2, fileid);
                update_stmt.executeUpdate();

        } catch (SQLException e) {
            // TODO rever
            throw new IllegalArgumentException("No such file name.");
        }

    }
}
