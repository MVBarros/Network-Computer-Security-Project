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
                    "WHERE owner=(?) AND filename=(?)");
            stmt.setString(1, username);
            stmt.setString(2, filename);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                //Query was empty
                PreparedStatement stmt2 = conn.prepareStatement("SELECT * FROM authorizations" +
                        "WHERE owner=(?) AND filename=(?) AND user=(?) AND permission >= (?)");
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

    /*public static void createAuth(String filename, String owner, String username, int permission) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        //FIXME se if auth already exists on intermediary version
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO authorizations VALUES (?,?,?,?)");
            stmt.setString(1, filename);
            stmt.setString(2, owner);
            stmt.setString(3, username);
            stmt.setInt(4, permission);
            stmt.executeUpdate();
        } catch (SQLException e) {
            //Auth already exists
            throw new IllegalArgumentException("Impossible to create auth.");
        }
    }*/

    public static void updateAccessControl(String filename, String owner, String target, int permission) {
        //so o owner pode executar esta funcao
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt;
            // permission = 0 é READ
            // permission = 1 é WRITE
            // permission = 2 é NONE
            if (permission == 2) {
                stmt = conn.prepareStatement("DELETE FROM authorizations WHERE filename=(?) AND owner=(?) AND username=(?)");
                stmt.setString(1, filename);
                stmt.setString(2, owner);
                stmt.setString(3, target);
            }

            else {
                // nao verificamos se user tem este file porque se nao tiver da SQL violation (neste caso foreign key violation)
                stmt = conn.prepareStatement("REPLACE INTO authorizations (filename, owner, username, permission) VALUES (?,?,?,?");
                stmt.setString(1, filename);
                stmt.setString(2, owner);
                stmt.setString(3, target);
                stmt.setInt(4, permission);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            // TODO rever
            throw new IllegalArgumentException("No such file name.");
        }

    }
}
