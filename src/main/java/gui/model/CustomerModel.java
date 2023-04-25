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
    public String add(Customer customer) {
        return customerManager.add(customer);
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
        return customerManager.getAll();
    }

    @Override
    public Object getById(UUID id) {
        return customerManager.getById(id);
    }
}
