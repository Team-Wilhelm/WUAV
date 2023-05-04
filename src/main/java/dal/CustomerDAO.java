package dal;

import be.Address;
import be.Customer;
import be.enums.CustomerType;
import dal.interfaces.DAO;
import dal.interfaces.IDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomerDAO extends DAO implements IDAO<Customer> {
    private DBConnection dbConnection;
    public CustomerDAO() {
        dbConnection = DBConnection.getInstance();
    }

    @Override
    public String add(Customer customer) {
        String result = "saved";
        String sql = "DECLARE @AddressID INT;"+
                "INSERT INTO CustomerAddress (StreetName, StreetNumber, Postcode, Town, Country) " +
                "VALUES (?,?,?,?,?);" +
                "SET @AddressID = SCOPE_IDENTITY();" +
                "INSERT INTO Customer (CustomerName, CustomerEmail, CustomerPhoneNumber, AddressID, LastContract, CustomerType) " +
                "VALUES (?,?,?,@AddressID,?,?)";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);

            // Address information
            ps.setString(1, customer.getCustomerAddress().getStreetName());
            ps.setString(2, customer.getCustomerAddress().getStreetNumber());
            ps.setString(3, customer.getCustomerAddress().getPostcode());
            ps.setString(4, customer.getCustomerAddress().getTown());
            ps.setString(5, customer.getCustomerAddress().getCountry());

            // Customer information
            ps.setString(6, customer.getCustomerName());
            ps.setString(7, customer.getCustomerEmail());
            ps.setString(8, customer.getCustomerPhoneNumber());
            ps.setDate(9, customer.getLastContract());
            ps.setString(10, customer.getCustomerType().toString());

            ps.executeUpdate();

            // Get the generated customerID from the database and set it as the customer's ID
            sql = "SELECT CustomerID, AddressID FROM Customer WHERE CustomerName = ? AND CustomerEmail = ? AND CustomerPhoneNumber = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, customer.getCustomerName());
            ps.setString(2, customer.getCustomerEmail());
            ps.setString(3, customer.getCustomerPhoneNumber());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                customer.setCustomerID(UUID.fromString(rs.getString("CustomerID")));
                customer.getCustomerAddress().setAddressID(rs.getInt("AddressID"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return result;
    }

    @Override
    public String update(Customer customer) {
        String result = "updated";
        String sql = "UPDATE CustomerAddress " +
                "SET StreetName = ?, StreetNumber = ?, Postcode = ?, Town = ?, Country = ? " +
                "WHERE AddressID = ?;" +
                "UPDATE Customer " +
                "SET CustomerName = ?, CustomerEmail = ?, CustomerPhoneNumber = ?, LastContract = ? , CustomerType = ? " +
                "WHERE CustomerID = ?";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);

            // Address information
            ps.setString(1, customer.getCustomerAddress().getStreetName());
            ps.setString(2, customer.getCustomerAddress().getStreetNumber());
            ps.setString(3, customer.getCustomerAddress().getPostcode());
            ps.setString(4, customer.getCustomerAddress().getTown());
            ps.setString(5, customer.getCustomerAddress().getCountry());
            ps.setInt(6, customer.getCustomerAddress().getAddressID());

            // Customer information
            ps.setString(7, customer.getCustomerName());
            ps.setString(8, customer.getCustomerEmail());
            ps.setString(9, customer.getCustomerPhoneNumber());
            ps.setDate(10, customer.getLastContract());
            ps.setString(11, customer.getCustomerType().toString());
            ps.setString(12, customer.getCustomerID().toString());

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
        String sql = "UPDATE Customer SET Deleted = 1 WHERE CustomerID = ?";
        return delete(id, sql);
    }

    @Override
    public Map<UUID, Customer> getAll() {
        HashMap<UUID, Customer> customers = new HashMap<>();
        String sql = "SELECT * FROM Customer " +
                "INNER JOIN CustomerAddress ON Customer.AddressID = CustomerAddress.AddressID " +
                "WHERE Deleted = 0";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Customer customer = createCustomerFromResultSet(rs);
                customers.put(customer.getCustomerID(), customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return customers;
    }

    @Override
    public Customer getById(UUID id) {
        String sql = "SELECT * FROM Customer " +
                "INNER JOIN CustomerAddress ON Customer.AddressID = CustomerAddress.AddressID " +
                "WHERE CustomerID = ? AND Deleted = 0";
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return createCustomerFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnection.releaseConnection(connection);
        }
        return null;
    }

    private Customer createCustomerFromResultSet(ResultSet rs) throws SQLException {
        Address a = new Address(
                rs.getInt("AddressID"),
                rs.getString("StreetName"),
                rs.getString("StreetNumber"),
                rs.getString("Postcode"),
                rs.getString("Town"),
                rs.getString("Country")
        );
        return new Customer(
                UUID.fromString(rs.getString("CustomerID")),
                rs.getString("CustomerName"),
                rs.getString("CustomerEmail"),
                rs.getString("CustomerPhoneNumber"),
                a,
                CustomerType.valueOf(rs.getString("CustomerType")),
                rs.getDate("LastContract")
        );
    }

    public void addOrUpdateCustomer(Customer customer) {
        if (customer.getCustomerID() == null) {
            add(customer);
        } else {
            update(customer);
        }
    }
}
