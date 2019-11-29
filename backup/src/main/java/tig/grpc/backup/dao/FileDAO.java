package tig.grpc.backup.dao;

import tig.utils.db.PostgreSQLJDBC;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    public static void uploadFile(String filename, String fileowner, String t_created, byte[] content) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO files(filename, fileowner, t_created, content) VALUES (?,?,?,?)");

            stmt.setString(1, filename);
            stmt.setString(2, fileowner);
            stmt.setString(3, t_created);
            stmt.setBytes(4, content);
            stmt.executeUpdate();

        } catch (SQLException e) {
            //Primary Key violation
            throw new IllegalArgumentException("Repeated backup");
        }
    }

    public static List<String> listFiles(String username) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("SELECT  filename, fileowner, t_created FROM files WHERE fileowner = (?)");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(String.format("File:%s\tOwner:%s\tDate Created:%s", rs.getString("filename"), rs.getString("fileowner"), rs.getString("t_created")));
            }
            if (result.size() == 0) {
                result.add("User has no files");
            }
            return result;
        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }
}
