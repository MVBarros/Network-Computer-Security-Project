package tig.grpc.backup.dao;

import tig.utils.db.PostgreSQLJDBC;
import tig.utils.encryption.EncryptedFile;
import tig.utils.encryption.EncryptionUtils;

import javax.crypto.SecretKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
