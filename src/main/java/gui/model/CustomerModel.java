package gui.model;

import be.Customer;
import bll.IManager;
import bll.ManagerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CustomerModel implements IModel<Customer> {
    private static CustomerModel instance;
    private IManager<Customer> customerManager;
    private HashMap<UUID, Customer> allCustomers;

    private CustomerModel() {
        customerManager = ManagerFactory.createManager(ManagerFactory.ManagerType.CUSTOMER);
        allCustomers = new HashMap<>();
        allCustomers.putAll(customerManager.getAll());
    }

    public static CustomerModel getInstance() {
        if (instance == null) {
            instance = new CustomerModel();
        }
        return instance;
    }

    @Override
    public CompletableFuture<String> add(Customer customer) {
        String message = customerManager.add(customer);

        CompletableFuture<Map<UUID, Customer>> future = CompletableFuture.supplyAsync(() -> customerManager.getAll());
        return future.thenApplyAsync(customers -> {
            allCustomers.clear();
            allCustomers.putAll(customers);
            return message;
        });
    }

    @Override
    public String update(Customer customer) {
        return customerManager.update(customer);
    }

    @Override
    public String delete(UUID id) {
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

    public Customer getByName(String name) {
        return allCustomers.values().stream().filter(customer ->
                customer.getCustomerName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Customer getByEmail(String email) {
        return allCustomers.values().stream().filter(customer ->
                customer.getCustomerEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
    }
}
