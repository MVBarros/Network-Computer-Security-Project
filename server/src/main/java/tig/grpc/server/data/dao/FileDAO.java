package tig.grpc.server.data.dao;

import tig.grpc.server.data.PostgreSQLJDBC;
import tig.grpc.server.utils.EncryptionUtils;
import tig.grpc.server.utils.StringGenerator;

import javax.crypto.SecretKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FileDAO {

    public static void fileUpload(String filename, byte[] fileContent, String username) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        String fileID = StringGenerator.randomStringNoMetacharacters(256);
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO files VALUES (?,?,?,?,?)");

            SecretKey secretKey = EncryptionUtils.generateAESKey();
            byte[] encryptedContent = EncryptionUtils.encryptFile(fileContent, secretKey);

            stmt.setString(1, filename);
            stmt.setString(2, username);
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
            PreparedStatement stmt = conn.prepareStatement("UPDATE INTO files VALUES (?,?,?,?,?)");

            SecretKey secretKey = EncryptionUtils.generateAESKey();
            byte[] encryptedContent = EncryptionUtils.encryptFile(fileContent, secretKey);

            stmt.setString(1, filename);
            stmt.setString(2, owner);
            stmt.setString(3, LocalDateTime.now().toString());
            stmt.setBytes(4, encryptedContent);
            stmt.setBytes(5, secretKey.getEncoded());
            stmt.executeUpdate();

        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static byte[] getFileContent(String fileId) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT filecontent FROM files " +
                    "WHERE fileId = (?)");

            stmt.setString(1, fileId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBytes("filecontent");
        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static void deleteFile(String username, String filename, String owner) {

        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            // So o owner pode apagar um ficheiro
            if (!username.equals(owner))
                throw new IllegalArgumentException("Only the owner of the file can delete the file.");

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
            // TODO rever
            stmt = conn.prepareStatement("SELECT username,permission, filename, owner FROM files NATURAL JOIN authorizations WHERE owner = (?) or user = (?)");
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();

            List<String> result = new ArrayList<String>();
            while (rs.next()) {
                if (rs.getString("username") == rs.getString("owner")) {
                    if (!ownedFiles.contains(rs.getString("filename"))) {
                        result.add(String.format("File: %s Owner: %s Permission: R/W", rs.getString("filename"), rs.getString("owner")));
                        ownedFiles.add(rs.getString("filename"));
                    }
                } else {
                    result.add(String.format("File: %s Owner: %s Permission: %s", rs.getString("filename"),
                            rs.getString("owner"), rs.getInt("permission") == 1 ? "RW" : "R"));

                }
            }
            return result;

        } catch (SQLException e) {
            throw new IllegalArgumentException("User has no files");
        }
    }
}
