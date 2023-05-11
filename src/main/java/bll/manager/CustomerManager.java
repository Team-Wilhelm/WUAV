package bll.manager;

import be.Customer;
import bll.IManager;
import dal.dao.CustomerDAO;
import dal.DAOFactory;

import java.util.Map;
import java.util.UUID;

public class CustomerManager implements IManager<Customer> {
    private CustomerDAO dao;

    public CustomerManager() {
        dao = (CustomerDAO) DAOFactory.createDAO(DAOFactory.DAOType.CUSTOMER);
    }

    @Override
    public String add(Customer customer) {
        return dao.add(customer);
    }

    @Override
    public String update(Customer customer) {
        return dao.update(customer);
    }

    @Override
    public String delete(UUID id) {
        return dao.delete(id);
    }

    @Override
    public Map<UUID, Customer> getAll() {
        return dao.getAll();
    }

    @Override
    public Customer getById(UUID id) {
        return dao.getById(id);
    }
}
