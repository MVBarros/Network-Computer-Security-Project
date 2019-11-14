package tig.grpc.backup.dao;

import tig.utils.db.PostgreSQLJDBC;
import tig.utils.encryption.EncryptedFile;
import tig.utils.encryption.EncryptionUtils;

import javax.crypto.spec.SecretKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FileDAO {

    public static byte[] getFileContent(String filename, String t_created) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT content FROM files " +
                    "WHERE filename = (?) and t_created = (?)");

            stmt.setString(1, filename);
            stmt.setString(2, t_created);
            ResultSet rs = stmt.executeQuery();
            rs.next();

            return rs.getBytes("content");

        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

}
