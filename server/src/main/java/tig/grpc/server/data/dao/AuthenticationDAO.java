package tig.grpc.server.data.dao;

import tig.grpc.server.data.PostgreSQLJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AuthenticationDAO {

    public static void authenticateFileAccess(String username, String filename, String owner, int type) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM files " +
                    "WHERE fileowner=(?) AND filename=(?)");
            stmt.setString(1, username);
            stmt.setString(2, filename);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                //Query was empty
                PreparedStatement stmt2 = conn.prepareStatement("SELECT * FROM authorizations" +
                        "WHERE fileowner=(?) AND filename=(?) AND user=(?) AND permission >= (?)");
                stmt2.setString(1, owner);
                stmt2.setString(2, filename);
                stmt2.setString(3, username);
                stmt2.setInt(4, type);
                rs = stmt2.executeQuery();
                if (!rs.next()) {
                    //Query was empty
                    throw new IllegalArgumentException("Cannot access given document");
                }


            }
        } catch (SQLException e) {
            //Should not happen
            throw new RuntimeException();
        }
    }

    public static void updateAccessControl(String filename, String owner, String target, int permission) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt;
            /*
               * permission = 0 is READ
               * permission = 1 is WRITE
               * permission = 2 is NONE
            */
            if (permission == 2) {
                stmt = conn.prepareStatement("DELETE FROM authorizations WHERE filename=(?) AND fileowner=(?) AND username=(?)");
                stmt.setString(1, filename);
                stmt.setString(2, owner);
                stmt.setString(3, target);
            } else {
                //TODO If this doesn't work DELETE before INSERT
                stmt = conn.prepareStatement("REPLACE INTO authorizations (filename, fileowner, username, permission) VALUES (?,?,?,?");
                stmt.setString(1, filename);
                stmt.setString(2, owner);
                stmt.setString(3, target);
                stmt.setInt(4, permission);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalArgumentException("No such file name owned");
        }

    }
}
