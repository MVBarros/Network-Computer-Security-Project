package tig.grpc.server.data.dao;

import tig.grpc.server.data.PostgreSQLJDBC;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UsersDAO {

    public static Boolean insertUser(String username, String password) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES(?,?)");
            stmt.setString(1, username);

            byte[] b = password.getBytes(StandardCharsets.UTF_8);
            stmt.setBytes(2,  MessageDigest.getInstance("SHA-1").digest(b));

            int i = stmt.executeUpdate();
            // TODO throw exception
            return i != 0;
        }
        catch(SQLException e){ System.out.println(e); return false;}
        catch (NoSuchAlgorithmException e ) {System.out.println(e); return false;}
    }
}
