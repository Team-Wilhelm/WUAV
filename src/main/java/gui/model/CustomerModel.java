package gui.model;

import be.Customer;
import bll.IManager;
import bll.ManagerFactory;
import bll.manager.CustomerManager;
import gui.util.DialogManager;
import javafx.scene.layout.Pane;
import utils.enums.ResultState;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class CustomerModel implements IModel<Customer> {
    private static CustomerModel instance;
    private CustomerManager customerManager;
    private HashMap<UUID, Customer> allCustomers;

    private CustomerModel() {
        customerManager = (CustomerManager) ManagerFactory.createManager(ManagerFactory.ManagerType.CUSTOMER);
        allCustomers = new HashMap<>();
        reloadCustomers();
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
        }
        return resultState;
    }

    @Override
    public ResultState update(Customer customer) {
        return customerManager.update(customer);
    }

    @Override
    public ResultState delete(UUID id) {
        return customerManager.delete(id);
    }

    @Override
    public Map<UUID, Customer> getAll() {
        return allCustomers;
    }

    @Override
    public Customer getById(UUID id) {
        return customerManager.getById(id);
    }

    public void reloadCustomers() {
        allCustomers.clear();
        allCustomers.putAll(customerManager.getAll());
    }

    public void deleteExpiredCustomers(){
        //TODO also delete documents or replace customer values
        allCustomers.values().stream().filter(customer ->
                customer.getLastContract().before((Date.valueOf(LocalDate.now().minusMonths(48)))))
                .forEach(customer -> delete(customer.getCustomerID()));
    }

    public void getAlmostExpiredCustomers(){
        customerManager.getAlmostExpiredCustomers(allCustomers);
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
}
