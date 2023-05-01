package dal;

import be.User;
import be.enums.UserRole;
import utils.BlobService;

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

            // Get the generated userID from the database and set it as the user's ID
            sql = "SELECT UserID FROM SystemUser WHERE Username = ? AND UserPassword = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setBytes(2, user.getPassword());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user.setUserID(UUID.fromString(rs.getString("UserID")));
                System.out.println("User ID: " + user.getUserID());
            }

            sql = "INSERT INTO SystemUser (ProfilePicture) VALUES (?) WHERE UserID = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, saveToBlobService(user));
            ps.setString(2, user.getUserID().toString());
            ps.executeUpdate();

            //TODO assignedDocuments
            //sql = "INSERT INTO UserDocument (UserID, DocumentID) VALUES (?, ?)";

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
        String sql = "UPDATE SystemUser SET FullName = ?, Username = ?, UserPassword = ?, " +
                "UserRole = ?, PhoneNumber = ?, ProfilePicture = ? " +
                "WHERE UserID = ?";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            fillPreparedStatement(ps, user);
            ps.setString(6, saveToBlobService(user));
            ps.setString(7, user.getUserID().toString());
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
                UserRole.fromString(resultSet.getString("UserRole")),
                resultSet.getString("ProfilePicture")
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

    private String saveToBlobService(User user) {
        String profilePicture = user.getProfilePicturePath();
        try {
            //TODO save to blob service
            String filePath = user.getProfilePicturePath().substring(0, user.getProfilePicturePath().lastIndexOf("\\") + 1);
            BlobService.getInstance().UploadFile(filePath, "profilePicture", user.getUserID());
        } catch (Exception e) {
            e.printStackTrace();
            profilePicture = user.getProfilePicturePath();
        }
        return profilePicture;
    }
}
