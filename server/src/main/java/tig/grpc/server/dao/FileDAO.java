package tig.grpc.server.dao;

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

    public static void fileUpload(String fileId, byte[] fileContent, byte[] key, byte[] iv) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO files(fileId, content) VALUES (?,?)");

            SecretKey secretKey = EncryptionUtils.getAesKey(key);
            EncryptedFile encryptedContent = EncryptionUtils.encryptFile(fileContent, secretKey, iv);

            stmt.setString(1, fileId);
            stmt.setBytes(2, encryptedContent.getContent());
            stmt.executeUpdate();

        } catch (SQLException e) {
            //Primary Key violation
            throw new IllegalArgumentException("Filename Provided already exists");
        }
    }

    public static void fileEdit(String fileId, byte[] fileContent, byte[] key, byte[] iv) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE files SET content=(?) WHERE fileId=(?)");

            SecretKeySpec secretKey = EncryptionUtils.getAesKey(key);
            EncryptedFile encryptedFile = EncryptionUtils.encryptFile(fileContent, secretKey, iv);

            stmt.setBytes(1, encryptedFile.getContent());
            stmt.setString(2, fileId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }

    }

    public static byte[] getFileContent(String fileId, SecretKeySpec fileKey, byte[] iv ) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT content FROM files " +
                    "WHERE fileId = (?)");

            stmt.setString(1, fileId);
            ResultSet rs = stmt.executeQuery();
            rs.next();

            return EncryptionUtils.decryptFile(new EncryptedFile(rs.getBytes("content"), iv), fileKey);

        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static void deleteFile(String fileId) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement delete_stmt = conn.prepareStatement("DELETE FROM files WHERE fileId=(?)");
            delete_stmt.setString(1, fileId);
            int result = delete_stmt.executeUpdate();
            if (result == 0) {
                throw new IllegalArgumentException("No such file name owned.");
            }
        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }
}
