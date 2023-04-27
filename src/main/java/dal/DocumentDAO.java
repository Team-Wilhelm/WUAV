package dal;

import be.Document;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DocumentDAO extends DAO implements IDAO<Document> {
    private DBConnection dbConnection;

    public DocumentDAO() {
        dbConnection = DBConnection.getInstance();
    }

    @Override
    public String add(Document document) {
        String result = "saved";
        String sql = "SELECT CustomerID FROM Customer WHERE CustomerID = ?";

        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, document.getCustomer().getCustomerID().toString());
            ResultSet rs = ps.executeQuery();

            // Check, if the customer is already in the database, if not, add them
            UUID customerID;
            if (rs.next()) {
                customerID = UUID.fromString(rs.getString("CustomerID"));
            } else {
                CustomerDAO customerDAO = new CustomerDAO();
                customerDAO.add(document.getCustomer());
                customerID = document.getCustomer().getCustomerID();
            }

            // Insert the document into the database
            sql = "INSERT INTO Document (JobTitle, JobDescription, Notes, CustomerId) VALUES (?, ?, ?, ?)";
            ps = connection.prepareStatement(sql);
            ps.setString(1, document.getJobTitle());
            ps.setString(2, document.getJobDescription());
            ps.setString(3, document.getOptionalNotes());
            ps.setString(4, customerID.toString());
            ps.executeUpdate();

            // Get the documentID from the database and set it as the document's ID
            sql = "SELECT DocumentID FROM Document WHERE JobTitle = ? AND JobDescription = ? AND Notes = ? AND CustomerID = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, document.getJobTitle());
            ps.setString(2, document.getJobDescription());
            ps.setString(3, document.getOptionalNotes());
            ps.setString(4, customerID.toString());
            rs = ps.executeQuery();
            if (rs.next()) {
                document.setDocumentID(UUID.fromString(rs.getString("DocumentID")));
            }
        } catch (SQLException e) {
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
        String sql = "UPDATE Document SET JobTitle = ?, JobDescription = ?, Notes = ?, CustomerID = ? " +
                "WHERE DocumentID = ?";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, document.getJobTitle());
            ps.setString(2, document.getJobDescription());
            ps.setString(3, document.getOptionalNotes());
            ps.setString(4, document.getCustomer().getCustomerID().toString());
            ps.setString(5, document.getDocumentID().toString());
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
    public String delete(UUID id) {
        String sql = "UPDATE Document SET Deleted = 1 WHERE DocumentID = ?";
        return delete(id, sql);
    }


    @Override
    public Map<UUID, Document> getAll() {
        HashMap<UUID, Document> documents = new HashMap<>();
        String sql = "SELECT * FROM Document WHERE Deleted = 0";
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
        return null;
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
        return new Document(
                UUID.fromString(rs.getString("DocumentID")),
                new CustomerDAO().getById(UUID.fromString(rs.getString("CustomerID"))),
                rs.getString("JobDescription"),
                rs.getString("Notes"),
                rs.getString("JobTitle")
            );
    }
}
