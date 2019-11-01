package tig.grpc.server.data.dao;

import com.google.protobuf.ByteString;
import tig.grpc.server.data.PostgreSQLJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
}
