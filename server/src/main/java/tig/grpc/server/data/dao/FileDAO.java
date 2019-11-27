package tig.grpc.server.data.dao;

import tig.utils.db.PostgreSQLJDBC;

import tig.utils.encryption.EncryptedFile;
import tig.utils.encryption.EncryptionUtils;

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

    public static void fileUpload(String filename, byte[] fileContent, String owner) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO files VALUES (?,?,?,?,?,?)");

            SecretKey secretKey = EncryptionUtils.generateAESKey();
            EncryptedFile encryptedContent = EncryptionUtils.encryptFile(fileContent, secretKey);

            stmt.setString(1, filename);
            stmt.setString(2, owner);
            stmt.setString(3, LocalDateTime.now().toString());
            stmt.setBytes(4, encryptedContent.getContent());
            stmt.setBytes(5, secretKey.getEncoded());
            stmt.setBytes(6, encryptedContent.getIv());
            stmt.executeUpdate();

        } catch (SQLException e) {
            //Primary Key violation
            throw new IllegalArgumentException("Filename Provided already exists");
        }
    }

    public static void fileEdit(String filename, byte[] fileContent, String owner) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE files SET content=(?), encryption_key=(?), iv=(?) WHERE filename=(?) AND fileowner=(?)");

            SecretKey secretKey = EncryptionUtils.generateAESKey();
            EncryptedFile encryptedFile = EncryptionUtils.encryptFile(fileContent, secretKey);

            stmt.setBytes(1, encryptedFile.getContent());
            stmt.setBytes(2, secretKey.getEncoded());
            stmt.setBytes(3, encryptedFile.getIv());
            stmt.setString(4, filename);
            stmt.setString(5, owner);
            stmt.executeUpdate();

        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static byte[] getFileContent(String filename, String owner) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT content, encryption_key, iv FROM files " +
                    "WHERE filename = (?) AND fileowner = (?)");

            stmt.setString(1, filename);
            stmt.setString(2, owner);
            ResultSet rs = stmt.executeQuery();
            rs.next();

            SecretKeySpec key = EncryptionUtils.getAesKey(rs.getBytes("encryption_key"));

            return EncryptionUtils.decryptFile(new EncryptedFile(rs.getBytes("content"), rs.getBytes("iv")), key);

        } catch (SQLException e) {
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
