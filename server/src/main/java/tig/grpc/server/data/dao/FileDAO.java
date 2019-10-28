package tig.grpc.server.data.dao;

import tig.grpc.server.data.PostgreSQLJDBC;
import tig.grpc.server.utils.EncryptionUtils;
import tig.grpc.server.utils.StringGenerator;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FileDAO {

    public static void fileUpload(String filename, byte[] fileContent, String owner) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO files VALUES (?,?,?,?,?)");

            SecretKey secretKey = EncryptionUtils.generateAESKey();
            byte[] encryptedContent = EncryptionUtils.encryptFile(fileContent, secretKey);

            stmt.setString(1, filename);
            stmt.setString(2, owner);
            stmt.setString(3, LocalDateTime.now().toString());
            stmt.setBytes(4, encryptedContent);
            stmt.setBytes(5, secretKey.getEncoded());

            stmt.executeUpdate();

        } catch (SQLException e) {
            //Primary Key violation
            throw new IllegalArgumentException("Filename Provided already exists");
        }
    }

    public static void fileEdit(String filename, byte[] fileContent, String owner) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE files SET filecontent=(?), encryption_key=(?) WHERE filename=(?) AND owner=(?)");

            SecretKey secretKey = EncryptionUtils.generateAESKey();
            byte[] encryptedContent = EncryptionUtils.encryptFile(fileContent, secretKey);

            stmt.setBytes(1, encryptedContent);
            stmt.setBytes(2, secretKey.getEncoded());
            stmt.setString(3, filename);
            stmt.setString(4, owner);
            stmt.executeUpdate();

        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static byte[] getFileContent(String filename, String owner) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT filecontent, encrytionKey FROM files " +
                    "WHERE filename = (?) AND owner = (?)");

            stmt.setString(1, filename);
            stmt.setString(2, owner);
            ResultSet rs = stmt.executeQuery();
            rs.next();

            SecretKeySpec key = EncryptionUtils.getAesKey(rs.getBytes("encryptionKey"));

            return EncryptionUtils.decryptFile(rs.getBytes("filecontent"), key);

        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static void deleteFile(String username, String filename) {

        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement delete_stmt = conn.prepareStatement("DELETE FROM files WHERE filename=(?) AND owner=(?)");
            delete_stmt.setString(1, filename);
            delete_stmt.setString(2, username);
            delete_stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalArgumentException("No such file name.");
        }

    }

    public static List<String> listFiles(String username) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        PreparedStatement stmt = null;
        try {
            HashSet<String> ownedFiles = new HashSet<String>();
            stmt = conn.prepareStatement("SELECT username,permission, filename, owner FROM files NATURAL JOIN authorizations WHERE owner = (?) or user = (?)");
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();

            List<String> result = new ArrayList<String>();
            while (rs.next()) {
                if (rs.getString("username").equals(rs.getString("owner")) &&
                            !ownedFiles.contains(rs.getString("filename"))) {
                    result.add(String.format("File:%s\tOwner:%s\tPermission:R/W", rs.getString("filename"), rs.getString("owner")));
                    ownedFiles.add(rs.getString("filename"));

                } else {
                    result.add(String.format("File:%s\tOwner:%s\tPermission:%s", rs.getString("filename"),
                            rs.getString("owner"), rs.getInt("permission") == 1 ? "RW" : "R"));
                }
            }

            if(result.size() == 0) {
                result.add("User has no files");
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }
}
