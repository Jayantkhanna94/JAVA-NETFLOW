package netflow.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized JDBC Database Connection Manager.
 * Provides static access to database connections using {@link DriverManager}.
 *
 * Connection details are set in the three constants below. Each one can also be
 * overridden with an environment variable (NETFLOW_DB_URL, NETFLOW_DB_USER,
 * NETFLOW_DB_PASSWORD), so the password does not have to be written in the code.
 */
public final class DBConnection {

    // ===== EDIT THESE THREE VALUES FOR YOUR MYSQL SETUP =====
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/netflow_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "your_mysql_password";
    // ========================================================

    private static final String URL = envOrDefault("NETFLOW_DB_URL", DEFAULT_URL);
    private static final String USER = envOrDefault("NETFLOW_DB_USER", DEFAULT_USER);
    private static final String PASSWORD = envOrDefault("NETFLOW_DB_PASSWORD", DEFAULT_PASSWORD);

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[WARN] MySQL JDBC Driver 'com.mysql.cj.jdbc.Driver' not found on classpath. "
                    + "Make sure the mysql-connector-j jar is in the lib/ folder.");
        }
    }

    private DBConnection() {
        // Prevent instantiation
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Establishes and returns a live connection to the NetFlow database.
     *
     * @return A valid {@link Connection} object.
     * @throws SQLException if a database access error occurs or credentials fail.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Diagnostic helper method to verify if the local MySQL database is reachable.
     *
     * @return true if connection succeeds; false otherwise.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }
}