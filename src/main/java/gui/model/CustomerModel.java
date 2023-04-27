package gui.model;

import be.Customer;
import be.Document;
import bll.CustomerManager;
import bll.IManager;
import bll.ManagerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class CustomerModel implements IModel<Customer> {
    private static CustomerModel instance;
    private IManager<Customer> customerManager;
    private HashMap<UUID, Customer> allCustomers;

    private CustomerModel() {
        customerManager = ManagerFactory.createManager(ManagerFactory.ManagerType.CUSTOMER);
        allCustomers = new HashMap<>();
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
            allCustomers = (HashMap<UUID, Customer>) customers;
            return message;
        });
    }

    @Override
    public String update(Customer customer, CountDownLatch latch) {
        return customerManager.update(customer);
    }

    @Override
    public String delete(UUID id) {
        return customerManager.delete(id);
    }

    @Override
    public Map<UUID, Customer> getAll() {
        return customerManager.getAll();
    }

    @Override
    public Customer getById(UUID id) {
        return customerManager.getById(id);
    }
}
