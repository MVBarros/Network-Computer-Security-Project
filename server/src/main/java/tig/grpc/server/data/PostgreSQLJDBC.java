package tig.grpc.server.data;

import java.sql.Connection;
import java.sql.DriverManager;

public class PostgreSQLJDBC {
    private static Integer port = null;
    private static String password = null;
    private static PostgreSQLJDBC instance = null;


    public static void setPassword(String password) {
        PostgreSQLJDBC.password = password;
    }

    public static void setPort(int port) {
        PostgreSQLJDBC.port = port;
    }

    private PostgreSQLJDBC() {
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:" + Integer.toString(port) + "/tigdb",
                            "postgres", password);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    public static PostgreSQLJDBC getInstance() {
        if (instance == null) {
            instance = new PostgreSQLJDBC();
        }
        return instance;

    }
}
