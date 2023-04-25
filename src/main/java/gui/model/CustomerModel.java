package gui.model;

import be.Customer;
import bll.CustomerManager;
import bll.IManager;
import bll.ManagerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class CustomerModel implements IModel<Customer> {
    private static CustomerModel instance;
    private IManager customerManager;

    private CustomerModel() {
        customerManager = ManagerFactory.createManager(ManagerFactory.ManagerType.CUSTOMER);
    }

    public static CustomerModel getInstance() {
        if (instance == null) {
            instance = new CustomerModel();
        }
        return instance;
    }

    @Override
    public void add(Customer customer) {
        customerManager.add(customer);
    }

    @Override
    public void update(Customer customer) {
        customerManager.update(customer);
    }

    @Override
    public void delete(UUID id) {
        customerManager.delete(id);
    }

    @Override
    public Map<UUID, Customer> getAll() {
        return customerManager.getAll();
    }

    @Override
    public Object getById(UUID id) {
        return customerManager.getById(id);
    }
}
