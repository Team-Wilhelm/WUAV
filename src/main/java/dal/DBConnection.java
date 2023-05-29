package dal;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Singleton class for handling database connections.
 * Uses a connection pool to avoid creating new connections if not necessary.
 */
public class DBConnection {
    private final SQLServerDataSource ds = new SQLServerDataSource();
    private static DBConnection instance;
    private final Deque<Connection> connectionPool = new ArrayDeque<>();
    private final List<Connection> usedConnections = new ArrayList<>();

    private DBConnection() {
        ds.setServerName("10.176.111.34");
        ds.setDatabaseName("CSe22B_WUAV_Wilhelm");
        ds.setPortNumber(1433);
        ds.setUser("CSe2022B_e_16");
        ds.setPassword("CSe2022BE16#");
        ds.setTrustServerCertificate(true);
    }

    public static DBConnection getInstance() {
        if (instance == null)
            instance = new DBConnection();
        return instance;
    }

    /**
     * Returns a connection from the connection pool if available, otherwise creates a new connection.
     * @return a connection to the database
     * @throws SQLException
     */
    public synchronized Connection getConnection() throws SQLException {
        Connection connection;

        if (connectionPool.isEmpty()) {
            connection = ds.getConnection();
        } else {
            connection = connectionPool.poll();
            if (!connection.isValid(50)) {
                connection = ds.getConnection();
            }
        }
        usedConnections.add(connection);
        return connection;
    }

    /**
     * Releases a connection back to the connection pool.
     * @param connection the connection to release
     */
    public void releaseConnection(Connection connection){
        if (connection != null && usedConnections.contains(connection)){
            usedConnections.remove(connection);
            connectionPool.add(connection);
        }
    }
}
