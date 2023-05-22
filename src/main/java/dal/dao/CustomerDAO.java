package dal.dao;

import be.Address;
import be.Customer;
import utils.enums.CustomerType;
import dal.DBConnection;
import dal.interfaces.DAO;
import dal.interfaces.IDAO;
import utils.enums.ResultState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomerDAO extends DAO implements IDAO<Customer> {
    private final DBConnection dbConnection;
    public CustomerDAO() {
        dbConnection = DBConnection.getInstance();
    }

    @Override
    public ResultState add(Customer customer) {
        String sql = "SELECT AddressId from CustomerAddress WHERE StreetName = ? AND StreetNumber = ? AND Postcode = ? AND Town = ? AND Country = ?";

        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);

            // Address information
            fillAddressStatement(ps, customer.getCustomerAddress());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                customer.getCustomerAddress().setAddressID(rs.getInt("AddressID"));
            }
            else {
                sql = "INSERT INTO CustomerAddress (StreetName, StreetNumber, Postcode, Town, Country) " +
                        "VALUES (?,?,?,?,?);";

                // Execute the SQL statement to insert the new address and customer
                ps = connection.prepareStatement(sql);
                fillAddressStatement(ps, customer.getCustomerAddress());
                ps.executeUpdate();
            }

            sql= "INSERT INTO Customer (CustomerName, CustomerEmail, CustomerPhoneNumber, LastContract, CustomerType) " +
                    "VALUES (?,?,?,?,?) ";
            ps = connection.prepareStatement(sql);
            ps.setString(1, customer.getCustomerName());
            ps.setString(2, customer.getCustomerEmail());
            ps.setString(3, customer.getCustomerPhoneNumber());
            ps.setDate(4, customer.getLastContract());
            ps.setString(5, customer.getCustomerType().toString());
            ps.executeUpdate();


            // Get the generated addressID from the database and set it as the customer's address ID
            sql = "SELECT AddressId from CustomerAddress WHERE StreetName = ? AND StreetNumber = ? AND Postcode = ? AND Town = ? AND Country = ?";
            ps = connection.prepareStatement(sql);
            fillAddressStatement(ps, customer.getCustomerAddress());
            rs = ps.executeQuery();
            if (rs.next()) {
                customer.getCustomerAddress().setAddressID(rs.getInt("AddressID"));
            }


            // Get the generated customerID from the database and set it as the customer's ID
            sql = "SELECT CustomerID FROM Customer WHERE CustomerName = ? AND CustomerEmail = ? AND CustomerPhoneNumber = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, customer.getCustomerName());
            ps.setString(2, customer.getCustomerEmail());
            ps.setString(3, customer.getCustomerPhoneNumber());
            rs = ps.executeQuery();
            if (rs.next()) {
                customer.setCustomerID(UUID.fromString(rs.getString("CustomerID")));
            }

            sql = "INSERT INTO Customer_Address_Link (CustomerID, AddressId) VALUES (?,?)";
            ps = connection.prepareStatement(sql);
            ps.setString(1, customer.getCustomerID().toString());
            ps.setInt(2, customer.getCustomerAddress().getAddressID());
            ps.executeUpdate();

            return ResultState.SUCCESSFUL;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultState.FAILED;
        } finally {
            dbConnection.releaseConnection(connection);
        }
    }

    private void fillAddressStatement(PreparedStatement ps, Address customerAddress) {
        try {
            ps.setString(1, customerAddress.getStreetName());
            ps.setString(2, customerAddress.getStreetNumber());
            ps.setString(3, customerAddress.getPostcode());
            ps.setString(4, customerAddress.getTown());
            ps.setString(5, customerAddress.getCountry());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResultState update(Customer customer) {
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
            return ResultState.SUCCESSFUL;
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultState.FAILED;
        } finally {
            dbConnection.releaseConnection(connection);
        }
    }

    @Override
    public ResultState delete(UUID id) {
        String sql = "UPDATE Customer SET Deleted = 1 WHERE CustomerID = ?";
        return delete(id, sql);
    }

    @Override
    public Map<UUID, Customer> getAll() {
        HashMap<UUID, Customer> customers = new HashMap<>();
        String sql = "SELECT * FROM Customer " +
                "INNER JOIN Customer_Address_Link ON Customer.CustomerID = Customer_Address_Link.CustomerID " +
                "INNER JOIN CustomerAddress ON Customer_Address_Link.AddressID = CustomerAddress.AddressID " +
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
                "INNER JOIN Customer_Address_Link ON Customer.CustomerID = Customer_Address_Link.CustomerID " +
                "INNER JOIN CustomerAddress ON Customer_Address_Link.AddressID = CustomerAddress.AddressID " +
                "WHERE Deleted = 0 AND Customer.CustomerID = ?";
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
}
