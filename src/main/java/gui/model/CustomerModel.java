package gui.model;

import be.Customer;
import be.Document;
import bll.ManagerFactory;
import bll.manager.CustomerManager;
import utils.enums.BusinessEntityType;
import utils.enums.ResultState;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class CustomerModel implements IModel<Customer> {
    private static CustomerModel instance;
    private CustomerManager customerManager;
    private HashMap<UUID, Customer> allCustomers;

    private CustomerModel() {
        customerManager = (CustomerManager) ManagerFactory.createManager(BusinessEntityType.CUSTOMER);
        allCustomers = new HashMap<>();
    }

    public static CustomerModel getInstance() {
        if (instance == null) {
            instance = new CustomerModel();
        }
        return instance;
    }

    @Override
    public ResultState add(Customer customer) {
        ResultState resultState = customerManager.add(customer);
        if (resultState.equals(ResultState.SUCCESSFUL)) {
            allCustomers.put(customer.getCustomerID(), customer);
            customer.getContracts().values().forEach(document -> {
                DocumentModel.getInstance().getById(document.getDocumentID()).setCustomer(customer);
            });
        }
        return resultState;
    }

    @Override
    public ResultState update(Customer customer) {
        ResultState resultState = customerManager.update(customer);
        if (resultState.equals(ResultState.SUCCESSFUL)) {
            allCustomers.put(customer.getCustomerID(), customer);
            customer.getContracts().values().forEach(document -> {
                DocumentModel.getInstance().getById(document.getDocumentID()).setCustomer(customer);
            });
        }
        return resultState;
    }

    @Override
    public ResultState delete(UUID id) {
        ResultState resultState = customerManager.delete(id);
        if (resultState.equals(ResultState.SUCCESSFUL)) {
            allCustomers.remove(id);
            DocumentModel.getInstance().deleteDocumentsByCustomer(id);
        }
        return resultState;
    }

    @Override
    public Map<UUID, Customer> getAll() {
        return allCustomers;
    }

    @Override
    public Customer getById(UUID id) {
        return allCustomers.get(id);
    }

    public void deleteExpiredCustomers(){
        allCustomers.values().stream().filter(customer ->
                customer.getLastContract().before((Date.valueOf(LocalDate.now().minusMonths(48)))))
                .forEach(customer -> delete(customer.getCustomerID()));
    }

    public int getAlmostExpiredCustomers(){
        return customerManager.getAlmostExpiredCustomers(allCustomers);
    }

    public Customer getByName(String name) {
        return allCustomers.values().stream().filter(customer ->
                customer.getCustomerName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Customer getByEmail(String email) {
        return allCustomers.values().stream().filter(customer ->
                customer.getCustomerEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
    }

    public List<Customer> searchCustomers(String query) {
        List<Customer> filteredCustomers = new ArrayList<>();
        allCustomers.values().stream().filter(customer ->
                customer.getCustomerName().toLowerCase().contains(query) ||
                        customer.getCustomerEmail().toLowerCase().contains(query)).forEach(filteredCustomers::add);
        return filteredCustomers;
    }

    public void put(Customer customer) {
        Customer temp = allCustomers.get(customer.getCustomerID());
        if (temp != null) {
            customer.setContracts(temp.getContracts());
            allCustomers.replace(customer.getCustomerID(), customer);
        } else {
            allCustomers.put(customer.getCustomerID(), customer);
        }
    }

    public void addContract(UUID customerID, Document contract) {
        Customer customer = allCustomers.get(customerID);
        if (customer != null) {
            customer.addContract(contract);
        }
    }
}
