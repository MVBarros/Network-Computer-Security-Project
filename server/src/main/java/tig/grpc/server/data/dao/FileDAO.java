package tig.grpc.server.data.dao;

import com.google.protobuf.ByteString;
import tig.grpc.server.data.PostgreSQLJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FileDAO {
    public static void fileUpload (String filename, ByteString fileContent) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO files VALUES (?,?,?)");

            String fileID = UUID.randomUUID().toString();
            stmt.setString(1, fileID);

            stmt.setString(2, filename);

            byte[] content = fileContent.toByteArray();
            stmt.setBytes(3, content);

            stmt.executeUpdate();

        } catch (SQLException e) {
            fileUpload(filename, fileContent);
        }
    }

    public static byte[] getFileContent (String fileId) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT filecontent FROM files " +
                                                            "WHERE filecontent = (?)");

            String fileID = UUID.randomUUID().toString();
            stmt.setString(1, fileID);


            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBytes("filecontent");
        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static void deleteFile(String username, String filename) {

        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            // verificar se user tem esse file
            PreparedStatement stmt = conn.prepareStatement("SELECT fileid FROM authorizations WHERE username = (?) and filename = (?)");
            stmt.setString(1, username);
            stmt.setString(2, filename);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // apagar o file
                PreparedStatement delete_stmt = conn.prepareStatement("DELETE FROM files WHERE fileid = (?)");
                delete_stmt.setString(1, rs.getString("fileid"));
                delete_stmt.executeUpdate();
            } else
                throw new IllegalArgumentException("No such file name.");

            /* Tambem posso fazer query toda junta
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM files UNION authorizations ON files.fileid = authorizations.fileid WHERE username = (?) and filename = (?)");
            mas assim nao faz verificacao segue
             */
        } catch (SQLException e) {
            // TODO rever
            throw new IllegalArgumentException("No such file name.");
        }

    }




}
