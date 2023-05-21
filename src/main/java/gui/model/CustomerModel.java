package gui.model;

import be.Customer;
import bll.IManager;
import bll.ManagerFactory;
import gui.util.DialogManager;
import javafx.scene.layout.Pane;
import utils.enums.ResultState;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class CustomerModel implements IModel<Customer> {
    private static CustomerModel instance;
    private IManager<Customer> customerManager;
    private HashMap<UUID, Customer> allCustomers;

    private CustomerModel() {
        customerManager = ManagerFactory.createManager(ManagerFactory.ManagerType.CUSTOMER);
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

    public void getAlmostExpiredCustomers(Pane pane){
        List<Customer> almostExpiredCustomers = new ArrayList<>();
        allCustomers.values().stream().filter(customer ->
                customer.getLastContract().before((Date.valueOf(LocalDate.now().minusMonths(47)))))
                .forEach(almostExpiredCustomers::add);

        StringBuilder sb = new StringBuilder();
        int expiredCustomers = almostExpiredCustomers.size();
        for (int i = 0; i < expiredCustomers; i++) {
            if (i != expiredCustomers - 1) {
                sb.append(almostExpiredCustomers.get(i).getCustomerName()).append(", ");
            } else sb.append(almostExpiredCustomers.get(i).getCustomerName());
        }
        if(almostExpiredCustomers.size() > 0) {
            DialogManager.getInstance().showInformation(
                    "Customer(s) will be deleted soon!",
                    "Update following customer(s) within one month if you wish to avoid deletion:\n" + sb, pane);
        }
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
