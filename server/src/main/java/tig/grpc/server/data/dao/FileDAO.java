package tig.grpc.server.data.dao;

import com.google.protobuf.ByteString;
import tig.grpc.server.data.PostgreSQLJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FileDAO {

    public static byte[] getFilename(String fileID) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT filename FROM files WHERE fileId =(?)");
            stmt.setString(1, fileID);


            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBytes("filename");

        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static void fileUpload(String filename, ByteString fileContent) {
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

    public static void fileEdit(String fileID, String filename, ByteString fileContent) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE INTO files VALUES (?,?,?)");

            stmt.setString(1, fileID);

            stmt.setString(2, filename);

            byte[] content = fileContent.toByteArray();
            stmt.setBytes(3, content);

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

    public static void deleteFile(String username, String fileid) {

        Connection conn = PostgreSQLJDBC.getInstance().getConn();
        try {
            PreparedStatement delete_stmt = conn.prepareStatement("DELETE FROM files WHERE fileid = (?)");
            delete_stmt.setString(1, fileid);
            delete_stmt.executeUpdate();

            /* Tambem posso fazer query toda junta
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM files UNION authorizations ON files.fileid = authorizations.fileid WHERE username = (?) and filename = (?)");
            mas assim nao faz verificacao segue
             */
        } catch (SQLException e) {
            // TODO rever
            throw new IllegalArgumentException("No such file name.");
        }

    }

    public static void listFiles(String username) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        PreparedStatement stmt = null;
        try {
            // TODO acabar isto
            stmt = conn.prepareStatement("SELECT filename FROM authorizations WHERE username = (?) or public = 1");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();




        } catch (SQLException e) {
            // TODO rever
            throw new IllegalArgumentException("No such file name.");
        }

    }


}
