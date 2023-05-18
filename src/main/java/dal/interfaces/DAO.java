package dal.interfaces;

import dal.DBConnection;
import utils.enums.ResultState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public abstract class DAO {
    public ResultState delete(UUID id, String sql) {
        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id.toString());
            ps.executeUpdate();
            return ResultState.SUCCESSFUL;
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultState.FAILED;
        } finally {
            DBConnection.getInstance().releaseConnection(connection);
        }
    }
}
