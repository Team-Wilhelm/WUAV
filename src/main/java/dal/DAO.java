package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public abstract class DAO {
    public String delete(UUID id, String sql) {
        String result = "deleted";
        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            result = e.getMessage();
        } finally {
            DBConnection.getInstance().releaseConnection(connection);
        }
        return result;
    }
}
