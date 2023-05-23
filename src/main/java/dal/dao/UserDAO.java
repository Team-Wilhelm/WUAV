package dal.dao;

import be.User;
import utils.enums.ResultState;
import utils.enums.UserRole;
import dal.DBConnection;
import dal.interfaces.DAO;
import dal.interfaces.IDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class UserDAO extends DAO implements IDAO<User> {
    private final DBConnection dbConnection;
    private DocumentDAO documentDAO;

    public UserDAO() {
        dbConnection = DBConnection.getInstance();
        documentDAO = new DocumentDAO();
    }

    @Override
    public ResultState add(User user) {
        String sql = "INSERT INTO SystemUser (FullName, Username, UserPassword, UserRole, PhoneNumber, Salt, ProfilePicture) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection connection = null;
        try {
            // Insert the user into the database
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            fillPreparedStatement(ps, user);
            ps.executeUpdate();

            // Get the generated userID from the database and set it as the user's ID
            sql = "SELECT UserID FROM SystemUser WHERE Username = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user.setUserID(UUID.fromString(rs.getString("UserID")));
            }
            return ResultState.SUCCESSFUL;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultState.FAILED;
        } finally {
            dbConnection.releaseConnection(connection);
        }
    }

    @Override
    public ResultState update(User user) {
        String sql = "UPDATE SystemUser SET FullName = ?, Username = ?, UserPassword = ?, " +
                "UserRole = ?, PhoneNumber = ?, Salt = ?, ProfilePicture = ? " +
                "WHERE UserID = ?";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            fillPreparedStatement(ps, user);
            ps.setString(8, user.getUserID().toString());
            ps.executeUpdate();
            return ResultState.SUCCESSFUL;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultState.FAILED;
        } finally {
            dbConnection.releaseConnection(connection);
        }
    }

    @Override
    public ResultState delete(UUID id) {
        String sql = "UPDATE SystemUser SET Deleted = 1 WHERE UserID = ?";
        return delete(id, sql);
    }

    @Override
    public Map<UUID, User> getAll() {
        long startTime = System.currentTimeMillis();
        HashMap<UUID, User> users = new HashMap<>();
        // STUFF() is used to concatenate the document IDs into a single string
        // FOR XML PATH('') is used to remove the XML tags from the string
        // CONVERT(VARCHAR(36) is used to convert the UUID to a string
        String sql = "SELECT SystemUser.*, " +
                "STUFF((" +
                    "SELECT ',' + CONVERT(VARCHAR(36), User_Document_Link.DocumentID, 1) " +
                    "FROM User_Document_Link " +
                    "WHERE SystemUser.UserID = User_Document_Link.UserID " +
                    "FOR XML PATH('')), 1, 1, '') AS DocumentIDs " +
                "FROM SystemUser " +
                "WHERE SystemUser.Deleted = 0";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                User user = getUser(resultSet, false);
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
        String sql = "SELECT SystemUser.*, " +
                "STUFF((" +
                "SELECT ',' + CONVERT(VARCHAR(36), User_Document_Link.DocumentID, 1) " +
                "FROM User_Document_Link " +
                "WHERE SystemUser.UserID = User_Document_Link.UserID " +
                "FOR XML PATH('')), 1, 1, '') AS DocumentIDs " +
                "FROM SystemUser " +
                "WHERE SystemUser.Deleted = 0 " +
                "AND SystemUser.UserID = ?";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id.toString());
            ResultSet resultSet = ps.executeQuery();

            // Get a list of document IDs assigned to the user
            if (resultSet.next()) {
                return getUser(resultSet, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return null;
    }

    private User getUser(ResultSet resultSet, boolean assignDocuments) throws SQLException {
        List<UUID> documentIDs = new ArrayList<>();
        String documentIdsStr = resultSet.getString("DocumentIDs");
        if (documentIdsStr != null) {
            String[] documentIdsArr = documentIdsStr.split(",");
            for (String documentIdStr : documentIdsArr) {
                documentIDs.add(UUID.fromString(documentIdStr));
            }
        }
        User user = getUserFromResultSet(resultSet, documentIDs, assignDocuments);
        user.setAssignedDocuments(documentDAO.getDocumentsByIDs(documentIDs));
        return user;
    }

    public boolean logIn(String username, byte[] password) {
        String sql = "SELECT UserPassword, Salt FROM [SystemUser] WHERE Username = ?";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                byte[] dbPassword = resultSet.getBytes("UserPassword");
                return Arrays.equals(password, dbPassword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return false;
    }

    private User getUserFromResultSet(ResultSet resultSet, List<UUID> documentIDs, boolean assignDocuments) throws SQLException {
        User user = new User(
                UUID.fromString(resultSet.getString("UserID")),
                resultSet.getString("FullName"),
                resultSet.getString("Username"),
                new byte[][] {resultSet.getBytes("UserPassword"), resultSet.getBytes("Salt")},
                resultSet.getString("PhoneNumber"),
                UserRole.fromString(resultSet.getString("UserRole")),
                resultSet.getString("ProfilePicture")
        );
        if (assignDocuments) {
            user.setAssignedDocuments(documentDAO.getDocumentsByIDs(documentIDs));
        }
        return user;
    }

    private void fillPreparedStatement(PreparedStatement ps, User user) throws SQLException {
        ps.setString(1, user.getFullName());
        ps.setString(2, user.getUsername());
        ps.setBytes(3, user.getPassword());
        ps.setString(4, user.getUserRole().toString());
        ps.setString(5, user.getPhoneNumber());
        ps.setBytes(6, user.getSalt());
        ps.setString(7, user.getProfilePicturePath());
    }
}
