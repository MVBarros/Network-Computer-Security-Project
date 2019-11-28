package tig.utils.db;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;

public class PostgreSQLJDBC {

    private static final Logger logger = Logger.getLogger(PostgreSQLJDBC.class);

    private static PostgreSQLJDBC instance = null;
    private static String dbName = null;
    private Connection conn;

    public static void setDbName(String dbName) {
        PostgreSQLJDBC.dbName = dbName;

    }

    public Connection getConn() {
        return conn;
    }

    private PostgreSQLJDBC() {
        conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager
                    .getConnection("jdbc:sqlite:" + dbName + ".db");
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