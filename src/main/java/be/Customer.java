package be;

import utils.enums.CustomerType;

import java.sql.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Customer {
    private UUID customerID;
    private String customerName, customerEmail, customerPhoneNumber;
    private Address customerAddress;
    private CustomerType customerType;
    private Date lastContract;
    private HashMap<UUID, Document> contracts;

    public Customer(){}

    public Customer(String customerName, String customerEmail, String customerPhoneNumber, Address customerAddress, CustomerType customerType, Date lastContract) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhoneNumber = customerPhoneNumber;
        this.customerAddress = customerAddress;
        this.customerType = customerType;
        this.lastContract = lastContract;
        this.contracts = new HashMap<>();
    }

    public Customer(UUID customerID, String customerName, String customerEmail, String customerPhoneNumber, Address customerAddress, CustomerType customerType, Date lastContract) {
        this(customerName, customerEmail, customerPhoneNumber, customerAddress, customerType, lastContract);
        this.customerID = customerID;
    }

    public UUID getCustomerID() {
        return customerID;
    }

    public void setCustomerID(UUID customerID) {
        this.customerID = customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Address getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(Address customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    public Date getLastContract() {
        return lastContract;
    }

    public void setLastContract(Date lastContract) {
        this.lastContract = lastContract;
    }

    public void addContract(Document contract){
        this.contracts.put(contract.getDocumentID(), contract);
    }

    public void removeContract(Document contract){
        this.contracts.remove(contract.getDocumentID());
    }

    public HashMap<UUID, Document> getContracts() {
        return contracts;
    }

    public void setContracts(HashMap<UUID, Document> contracts) {
        this.contracts = contracts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerName, customer.customerName)
                && Objects.equals(customerEmail, customer.customerEmail)
                && Objects.equals(customerPhoneNumber, customer.customerPhoneNumber)
                && customerType == customer.customerType
                && customerAddress.equals(customer.customerAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerName, customerEmail, customerPhoneNumber, customerType);
    }
}
