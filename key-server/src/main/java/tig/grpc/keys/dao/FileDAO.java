package tig.grpc.keys.dao;

import tig.utils.db.PostgreSQLJDBC;
import tig.utils.encryption.EncryptedFile;
import tig.utils.encryption.EncryptionUtils;
import tig.utils.encryption.FileKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    public static FileKey getFileEncryptionKey(String filename, String owner) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT encryption_key, iv FROM files " +
                    "WHERE filename = (?) AND fileowner = (?)");

            stmt.setString(1, filename);
            stmt.setString(2, owner);

            ResultSet rs = stmt.executeQuery();
            //there should be only one result
            rs.next();

            return new FileKey(rs.getBytes("encryption_key"), rs.getBytes("iv"));

        }catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }


    public static void deleteFile(String username, String filename) {

        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement delete_stmt = conn.prepareStatement("DELETE FROM files WHERE filename=(?) AND fileowner=(?)");
            delete_stmt.setString(1, filename);
            delete_stmt.setString(2, username);
            int result = delete_stmt.executeUpdate();
            if (result == 0) {
                throw new IllegalArgumentException("No such file name owned.");
            }
        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static void updateFileKey(FileKey key, String filename, String fileowner) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement key_stmt = conn.prepareStatement("UPDATE files SET encryption_key=(?), iv=(?) WHERE filename=(?) AND fileowner=(?)");
            key_stmt.setBytes(1, key.getKey());
            key_stmt.setBytes(2, key.getIv());
            key_stmt.setString(3, filename);
            key_stmt.setString(4, fileowner);
            key_stmt.executeUpdate();
        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }

    }

    public static void createFileKey(FileKey key, String filename, String fileowner) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement key_stmt = conn.prepareStatement("INSERT INTO files VALUES (?,?,?,?)");
            key_stmt.setString(1, filename);
            key_stmt.setString(2, fileowner);
            key_stmt.setBytes(3, key.getKey());
            key_stmt.setBytes(4, key.getIv());
            key_stmt.executeUpdate();
        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }

    }

    public static List<String> listFiles(String username) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("SELECT  filename, fileowner FROM files WHERE fileowner = (?)");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(String.format("File:%s\tOwner:%s\tPermission:R/W", rs.getString("filename"), rs.getString("fileowner")));
            }

            stmt = conn.prepareStatement("SELECT  filename, fileowner, permission FROM authorizations WHERE username = (?)");
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(String.format("File:%s\tOwner:%s\tPermission:%s", rs.getString("filename"),
                        rs.getString("fileowner"), rs.getInt("permission") == 1 ? "RW" : "R"));
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
