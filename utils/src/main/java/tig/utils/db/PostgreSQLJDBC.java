package tig.utils.db;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;

public class PostgreSQLJDBC {

    private static final Logger logger = Logger.getLogger(PostgreSQLJDBC.class);

    private static Integer port = null;
    private static String password = null;
    private static PostgreSQLJDBC instance = null;

    private Connection conn;


    public Connection getConn() {
        return conn;
    }


    public static void setPassword(String password) {
        PostgreSQLJDBC.password = password;
    }

    public static void setPort(int port) {
        PostgreSQLJDBC.port = port;
    }

    private PostgreSQLJDBC() {
        conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager
                    .getConnection("jdbc:postgresql://localhost:" + Integer.toString(port) + "/tigdb",
                            "postgres", password);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        logger.info("Opened database successfully");
    }

    public static PostgreSQLJDBC getInstance() {
        if (instance == null) {
            instance = new PostgreSQLJDBC();
        }
        return instance;
    }
}
