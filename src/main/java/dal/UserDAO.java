package dal;

import be.User;
import be.enums.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserDAO extends DAO implements IDAO<User> {
    private DBConnection dbConnection;

    public UserDAO() {
        dbConnection = DBConnection.getInstance();
    }

    @Override
    public String add(User user) {
        String result = "saved";
        String sql = "INSERT INTO SystemUser (FullName, Username, UserPassword, UserRole, PhoneNumber) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            fillPreparedStatement(ps, user);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return result;
    }

    @Override
    public String update(User user) {
        String result = "updated";
        String sql = "UPDATE SystemUser SET FullName = ?, Username = ?, UserPassword = ?, UserRole = ?, PhoneNumber = ? " +
                "WHERE UserID = ?";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            fillPreparedStatement(ps, user);
            ps.setString(6, user.getUserID().toString());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return result;
    }

    @Override
    public String delete(UUID id) {
        String result = "deleted";
        String sql = "UPDATE SystemUser SET Deleted = 1 WHERE UserID = ?";
        delete(id, sql);
        return result;
    }


    @Override
    public Map<UUID, User> getAll() {
        HashMap<UUID, User> users = new HashMap<>();
        String sql = "SELECT * FROM SystemUser WHERE Deleted = 0";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                User user = getUserFromResultSet(resultSet);
                users.put(user.getUserID(), user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return users;
    }

    @Override
    public User getById(UUID id) {
        String sql = "SELECT * FROM SystemUser WHERE UserID = ?";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id.toString());
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return getUserFromResultSet(resultSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return null;
    }

    private User getUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User(
                UUID.fromString(resultSet.getString("UserID")),
                resultSet.getString("FullName"),
                resultSet.getString("Username"),
                resultSet.getBytes("UserPassword"),
                resultSet.getString("PhoneNumber"),
                UserRole.fromString(resultSet.getString("UserRole"))
        );
        return user;
    }

    private void fillPreparedStatement(PreparedStatement ps, User user) throws SQLException {
        ps.setString(1, user.getFullName());
        ps.setString(2, user.getUsername());
        ps.setBytes(3, user.getPassword());
        ps.setString(4, user.getUserRole().toString());
        ps.setString(5, user.getPhoneNumber());
    }
}
