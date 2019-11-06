package tig.grpc.server.data.dao;

import com.google.protobuf.ByteString;
import tig.grpc.server.data.PostgreSQLJDBC;
import tig.grpc.server.utils.StringGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    public static String getFilename(String fileID) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT filename FROM files WHERE fileId =(?)");
            stmt.setString(1, fileID);


            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getString("filename");

        } catch (SQLException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static void fileUpload(String filename, byte[] fileContent, String username) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        String fileID = StringGenerator.randomStringNoMetacharacters(256);
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO files VALUES (?,?,?)");

            stmt.setString(1, fileID);

            stmt.setString(2, filename);

            stmt.setBytes(3, fileContent);

            stmt.executeUpdate();

        } catch (SQLException e) {
            fileUpload(filename, fileContent, username);
        }
        AuthenticationDAO.createAuth(username, fileID, false);
    }

    public static void fileEdit(String fileID, String filename, byte[] fileContent) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE INTO files VALUES (?,?,?)");

            stmt.setString(1, fileID);

            stmt.setString(2, filename);

            stmt.setBytes(3, fileContent);

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
            PreparedStatement delete_stmt = conn.prepareStatement("DELETE FROM files WHERE fileid = (?)");
            delete_stmt.setString(1, fileid);
            delete_stmt.executeUpdate();


        } catch (SQLException e) {
            // TODO rever
            throw new IllegalArgumentException("No such file name.");
        }

    }

    public static  List<String> listFiles(String username) {
        Connection conn = PostgreSQLJDBC.getInstance().getConn();

        PreparedStatement stmt = null;
        try {
            // TODO rever
            stmt = conn.prepareStatement("SELECT filename,fileid FROM authorizations NATURAL JOIN files WHERE username = (?) or public = TRUE");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            List<String> result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString("filename") + " " + rs.getString("fileid"));
            }
            return result;

        } catch (SQLException e) {
            throw new IllegalArgumentException("User has no files");
        }

    }


}
