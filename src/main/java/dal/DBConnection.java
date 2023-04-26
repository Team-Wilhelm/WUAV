package dal;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class DBConnection {
    private final SQLServerDataSource ds = new SQLServerDataSource();
    private static DBConnection instance;
    private Deque<Connection> connectionPool = new ArrayDeque<>();
    private List<Connection> usedConnections = new ArrayList<>();

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

    public Connection getConnection() throws SQLException {
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

    public void releaseConnection(Connection connection){
        if (usedConnections.contains(connection)){
            usedConnections.remove(connection);
            connectionPool.add(connection);
        }
    }
}
