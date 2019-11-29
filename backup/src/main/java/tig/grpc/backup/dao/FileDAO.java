package tig.grpc.backup.dao;

import tig.utils.db.PostgreSQLJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    public static List<String> listFiles(String username) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("SELECT  filename, fileowner,t_created FROM files WHERE fileowner = (?)");
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
