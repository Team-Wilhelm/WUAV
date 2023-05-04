package dal;

import be.Document;
import be.ImageWrapper;
import be.User;
import dal.interfaces.DAO;
import dal.interfaces.IDAO;
import javafx.scene.image.Image;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class DocumentDAO extends DAO implements IDAO<Document> {
    private final DBConnection dbConnection;
    private final DocumentImageFactory imageFactory = DocumentImageFactory.getInstance();

    public DocumentDAO() {
        dbConnection = DBConnection.getInstance();
    }

    @Override
    public String add(Document document) {
        String result = "saved";

        // Check, if the customer is already in the database, if not, add them
        CustomerDAO customerDAO = new CustomerDAO();
        if (document.getCustomer().getCustomerID() == null) {
            customerDAO.add(document.getCustomer());
        } else {
            customerDAO.update(document.getCustomer());
        }

        Connection connection = null;
        try {
            connection = dbConnection.getConnection();

            // Insert the document into the database
            String sql = "INSERT INTO Document (JobTitle, JobDescription, Notes, CustomerId, DateOfCreation) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, document.getJobTitle());
            ps.setString(2, document.getJobDescription());
            ps.setString(3, document.getOptionalNotes());
            ps.setString(4, document.getCustomer().getCustomerID().toString());
            ps.setDate(5, document.getDateOfCreation());
            ps.executeUpdate();

            // Get the documentID from the database and set it as the document's ID
            sql = "SELECT DocumentID FROM Document WHERE JobTitle = ? AND JobDescription = ? AND Notes = ? AND CustomerID = ? AND DateOfCreation = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, document.getJobTitle());
            ps.setString(2, document.getJobDescription());
            ps.setString(3, document.getOptionalNotes());
            ps.setString(4, document.getCustomer().getCustomerID().toString());
            ps.setDate(5, document.getDateOfCreation());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                document.setDocumentID(UUID.fromString(rs.getString("DocumentID")));
            }

            // Link the document to the technicians
            sql = "INSERT INTO User_Document_Link (UserID, DocumentID) VALUES (?, ?)";
            ps = connection.prepareStatement(sql);
            for (User technician : document.getTechnicians()) {
                ps.setString(1, technician.getUserID().toString());
                ps.setString(2, document.getDocumentID().toString());
                ps.addBatch();
            }
            ps.executeBatch();


            //Save and link image filepaths to document
            sql = "INSERT INTO Document_Image_Link (DocumentID, Filepath, FileName) VALUES (?, ?, ?)";
            ps = connection.prepareStatement(sql);
            String documentID = document.getDocumentID().toString();
            for (ImageWrapper image: document.getDocumentImages()){
                ps.setString(1, documentID);
                ps.setString(2, image.getUrl());
                ps.setString(3, image.getName());
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return result;
    }

    @Override
    public String update(Document document) {
        String result = "updated";
        String sql = "UPDATE Document SET JobTitle = ?, JobDescription = ?, Notes = ?, CustomerID = ?, DateOfCreation = ? " +
                "WHERE DocumentID = ?";
        Connection connection = null;
        try {
            // Check, if the customer is already in the database and update them, if not, add them
            new CustomerDAO().addOrUpdateCustomer(document.getCustomer());
            connection = dbConnection.getConnection();

            // Update the document
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, document.getJobTitle());
            ps.setString(2, document.getJobDescription());
            ps.setString(3, document.getOptionalNotes());
            ps.setString(4, document.getCustomer().getCustomerID().toString());
            ps.setDate(5, document.getDateOfCreation());
            ps.setString(6, document.getDocumentID().toString());
            ps.executeUpdate();


            //Update document image link
            if (!document.getDocumentImages().isEmpty()) {
//                sql = "MERGE INTO Document_Image_Link AS d "
//                        + "USING (VALUES (?, ?)) AS s(DocumentID, filepath) "
//                        + "ON (d.DocumentID = s.DocumentID AND d.filepath = s.filepath) "
//                        + "WHEN NOT MATCHED THEN "
//                        + "INSERT (DocumentID, filepath) VALUES (s.DocumentID, s.filepath);";

                sql = "DELETE FROM Document_Image_Link WHERE DocumentID = ?;";
                ps = connection.prepareStatement(sql);
                ps.setString(1, document.getDocumentID().toString());
                ps.executeUpdate();

                sql = "INSERT INTO Document_Image_Link (DocumentID, Filepath, FileName) VALUES (?, ?, ?);";
                ps = connection.prepareStatement(sql);
                for (ImageWrapper image : document.getDocumentImages()) {
                    ps.setString(1, document.getDocumentID().toString());
                    ps.setString(2, image.getUrl());
                    ps.setString(3, image.getName());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
            return result;
        } finally {
            dbConnection.releaseConnection(connection);
        }
    }

    @Override
    public String delete(UUID id) {
        String result = "deleted";
        String sql = "UPDATE Document SET Deleted = 1 WHERE DocumentID = ?;" +
                "DELETE FROM Document_Image_Link WHERE DocumentID = ?";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id.toString());
            ps.setString(2, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            result = e.getMessage();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return result;
    }

    @Override
    public Map<UUID, Document> getAll() {
        long startTime = System.currentTimeMillis();
        String sql = "SELECT * FROM Document WHERE Deleted = 0";
        HashMap<UUID, Document> documents = new HashMap<>();
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Document document = createDocumentFromResultSet(rs);
                documents.put(document.getDocumentID(), document);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("DocumentDAO.getAll() took " + (endTime - startTime) + " milliseconds");
        return documents;
    }

    @Override
    public Document getById(UUID id) {
        String sql = "SELECT * FROM Document WHERE DocumentID = ? AND Deleted = 0";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return createDocumentFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return null;
    }

    private Document createDocumentFromResultSet(ResultSet rs) throws SQLException {
        Document document = new Document (
                UUID.fromString(rs.getString("DocumentID")),
                new CustomerDAO().getById(UUID.fromString(rs.getString("CustomerID"))),
                rs.getString("JobDescription"),
                rs.getString("Notes"),
                rs.getString("JobTitle"),
                rs.getDate("DateOfCreation")
            );
        document.setDocumentImages(assignImagesToDocument(document));
        return document;
    }

    public List<ImageWrapper> assignImagesToDocument(Document document){
        //TODO - make this faster
        long startTime = System.currentTimeMillis();
        String sql = "SELECT * FROM Document_Image_Link WHERE DocumentID =?;";
        Connection connection = null;
        List<ImageWrapper> images = new ArrayList<>();
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, document.getDocumentID().toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String filepath = rs.getString("Filepath");
                String filename = rs.getString("FileName");
                images.add(new ImageWrapper(filepath, filename, imageFactory.create(filepath)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken to get images for document " + document.getJobTitle() + ": " + (endTime - startTime));
        return images;
    }


    public void assignUserToDocument(User user, Document document, boolean isAssigning){
        String sql = "INSERT INTO User_Document_Link (UserID, DocumentID) VALUES (?, ?);";
        if (!isAssigning) {
            sql = "DELETE FROM User_Document_Link WHERE UserID = ? AND DocumentID =?;";
        }

        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getUserID().toString());
            statement.setString(2, document.getDocumentID().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
    }

    public HashMap<UUID, Document> getDocumentsByIDs(List<UUID> documentIDs) {
        HashMap<UUID, Document> documents = new HashMap<>();
        if (documentIDs.isEmpty() || documentIDs == null) {
            return documents;
        }

        // create comma-separated string of document IDs
        String sql = "SELECT * FROM Document WHERE DocumentID IN ("
                + String.join(",", Collections.nCopies(documentIDs.size(), "?"))
                + ")";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int i = 0; i < documentIDs.size(); i++) {
                statement.setString(i + 1, documentIDs.get(i).toString());
            }

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Document document = createDocumentFromResultSet(rs);
                documents.put(document.getDocumentID(), document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return documents;
    }
}

