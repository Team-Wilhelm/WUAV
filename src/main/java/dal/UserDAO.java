package dal;

import be.User;
import be.enums.UserRole;
import utils.BlobService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;


public class UserDAO extends DAO implements IDAO<User> {
    private final DBConnection dbConnection;

    public UserDAO() {
        dbConnection = DBConnection.getInstance();
    }

    @Override
    public String add(User user) {
        String result = "saved";
        String sql = "INSERT INTO SystemUser (FullName, Username, UserPassword, UserRole, PhoneNumber, Salt) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

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

            //TODO delete the file after it has been uploaded to the blob service
            /*
            // Save the profile picture to downloads folder
            File file = new File(user.getProfilePicturePath());
            File newFile = new File(System.getProperty("user.home") + "/Downloads/" +
                    user.getUserID() + "cropped.png");
            Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            user.setProfilePicturePath(newFile.getAbsolutePath());


            sql = "UPDATE SystemUser SET ProfilePicture = ? WHERE UserID = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, saveToBlobService(user));
            ps.setString(2, user.getUserID().toString());
            ps.executeUpdate();

             */
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
                "UserRole = ?, PhoneNumber = ?, Salt = ?, ProfilePicture = ? " +
                "WHERE UserID = ?";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            fillPreparedStatement(ps, user);
            String profilePicturePath = saveToBlobService(user);
            ps.setBytes(6, user.getPassword()[1]);
            ps.setString(7, profilePicturePath);
            ps.setString(8, user.getUserID().toString());
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
                List<UUID> documentIDs = new ArrayList<>();
                String documentIdsStr = resultSet.getString("DocumentIDs");
                if (documentIdsStr != null) {
                    String[] documentIdsArr = documentIdsStr.split(",");
                    for (String documentIdStr : documentIdsArr) {
                        documentIDs.add(UUID.fromString(documentIdStr));
                    }
                }
                User user = getUserFromResultSet(resultSet, documentIDs);
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
        String sql = "SELECT * FROM SystemUser LEFT JOIN User_Document_Link " +
                "ON SystemUser.UserID = User_Document_Link.UserID " +
                "WHERE UserID = ?";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id.toString());
            ResultSet resultSet = ps.executeQuery();

            // Get a list of document IDs assigned to the user
            List<UUID> documentIDs = new ArrayList<>();
            while (resultSet.next()) {
                documentIDs.add(UUID.fromString(resultSet.getString("DocumentID")));
            }
            return getUserFromResultSet(resultSet, documentIDs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return null;
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

    private User getUserFromResultSet(ResultSet resultSet, List<UUID> documentIDs) throws SQLException {
        User user = new User(
                UUID.fromString(resultSet.getString("UserID")),
                resultSet.getString("FullName"),
                resultSet.getString("Username"),
                new byte[][] {resultSet.getBytes("UserPassword"), resultSet.getBytes("Salt")},
                resultSet.getString("PhoneNumber"),
                UserRole.fromString(resultSet.getString("UserRole")),
                resultSet.getString("ProfilePicture")
        );
        user.setAssignedDocuments(new DocumentDAO().getDocumentsByIDs(documentIDs));
        return user;
    }

    private void fillPreparedStatement(PreparedStatement ps, User user) throws SQLException {
        ps.setString(1, user.getFullName());
        ps.setString(2, user.getUsername());
        ps.setBytes(3, user.getPassword()[0]);
        ps.setString(4, user.getUserRole().toString());
        ps.setString(5, user.getPhoneNumber());
        ps.setBytes(6, user.getPassword()[1]);
    }

    private String saveToBlobService(User user) throws ExecutionException, InterruptedException {
        // Try to upload the file to blob service
        Callable<String> uploadFile = () -> BlobService.getInstance().UploadFile(user.getProfilePicturePath(), user.getUserID());

        // Make sure the thread waits until blob service is done
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(uploadFile);

        String result = "/img/userIcon.png";
        try {
            result = future.get();
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread was interrupted while waiting for result: " + e.getMessage());
            throw e;
        } catch (ExecutionException e) {
            System.err.println("An error occurred while executing the task: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            throw e;
        } finally {
            executor.shutdown();
        }
    }
}
