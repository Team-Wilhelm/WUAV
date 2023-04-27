package be;

import be.enums.CustomerType;

import java.sql.Date;
import java.util.UUID;

public class Customer {
    private UUID customerID;
    private String customerName, customerEmail, customerPhoneNumber;
    private Address customerAddress;
    private CustomerType customerType;
    private Date lastContract;

    public Customer(){
    }

    public Customer(String customerName, String customerEmail, String customerPhoneNumber, Address customerAddress, CustomerType customerType, Date lastContract) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhoneNumber = customerPhoneNumber;
        this.customerAddress = customerAddress;
        this.customerType = customerType;
        this.lastContract = lastContract;
    }

    public Customer(UUID customerID, String customerName, String customerEmail, String customerPhoneNumber, Address customerAddress, CustomerType customerType, Date lastContract) {
        this(customerEmail, customerName, customerPhoneNumber, customerAddress, customerType, lastContract);
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

    //Ensuring a customer is only compared by UUID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Customer customer = (Customer) o;

        return customerID.equals(customer.customerID);
    }

    @Override
    public int hashCode() {
        return customerID.hashCode();
    }
}
