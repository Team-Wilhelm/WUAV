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
    private final Deque<Connection> connectionPool = new ArrayDeque<>();
    private final List<Connection> usedConnections = new ArrayList<>();

    private DBConnection() {
        //ds.setServerName("wuaveasv.database.windows.net");
        //ds.setDatabaseName("school");
        //ds.setPortNumber(1433);
        //ds.setUser("mazur");
        //ds.setPassword("P@ssw0rd.+");
        //ds.setTrustServerCertificate(true);

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
        if (connection != null && usedConnections.contains(connection)){
            usedConnections.remove(connection);
            connectionPool.add(connection);
        }
    }
}
