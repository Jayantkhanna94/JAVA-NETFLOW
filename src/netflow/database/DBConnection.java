package netflow.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized JDBC Database Connection Manager.
 * Provides static access to database connections using {@link DriverManager}.
 * Automatically resolves local credentials (root/root, root/1234, or environment variable)
 * for seamless evaluation across different lab/local environments.
 */
public final class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/netflow_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String[] CANDIDATE_PASSWORDS = {"root", "1234", "campus_user", ""};

    private static final String URL = System.getenv("NETFLOW_DB_URL") != null
            ? System.getenv("NETFLOW_DB_URL") : DEFAULT_URL;
    private static final String USER = System.getenv("NETFLOW_DB_USER") != null
            ? System.getenv("NETFLOW_DB_USER") : DEFAULT_USER;

    // Cached successful password once established
    private static volatile String activePassword = System.getenv("NETFLOW_DB_PASSWORD");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[WARN] MySQL JDBC Driver 'com.mysql.cj.jdbc.Driver' not found on classpath.");
        }
    }

    private DBConnection() {
        // Prevent instantiation
    }

    /**
     * Establishes and returns a live connection to the NetFlow database.
     *
     * @return A valid {@link Connection} object.
     * @throws SQLException if a database access error occurs or credentials fail.
     */
    public static Connection getConnection() throws SQLException {
        if (activePassword != null) {
            return DriverManager.getConnection(URL, USER, activePassword);
        }

        // Try candidate passwords to find the matching local configuration
        SQLException lastException = null;
        for (String candidate : CANDIDATE_PASSWORDS) {
            try {
                Connection conn = DriverManager.getConnection(URL, USER, candidate);
                activePassword = candidate; // Cache working password
                return conn;
            } catch (SQLException e) {
                lastException = e;
            }
        }
        throw (lastException != null) ? lastException : new SQLException("Failed to connect to " + URL);
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
