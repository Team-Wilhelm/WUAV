package bll.manager;

import be.Customer;
import be.enums.UserRole;
import bll.IManager;
import dal.dao.CustomerDAO;
import dal.DAOFactory;
import utils.permissions.AccessChecker;
import utils.permissions.RequiresPermission;

import java.util.Map;
import java.util.UUID;

public class CustomerManager implements IManager<Customer> {
    private CustomerDAO dao;
    private AccessChecker checker = new AccessChecker();

    public CustomerManager() {
        dao = (CustomerDAO) DAOFactory.createDAO(DAOFactory.DAOType.CUSTOMER);
    }

    @Override
    @RequiresPermission({UserRole.ADMINISTRATOR, UserRole.PROJECT_MANAGER, UserRole.TECHNICIAN})
    public String add(Customer customer) {
        if (checker.hasAccess(this.getClass())) {
            return dao.add(customer);
        }
        else {
            return "No Permission";}
    }

    @Override
    @RequiresPermission({UserRole.ADMINISTRATOR, UserRole.PROJECT_MANAGER, UserRole.TECHNICIAN})
    public String update(Customer customer) {
        if (checker.hasAccess(this.getClass())) {
            return dao.update(customer);
        }
        else {
            return "No Permission";}
    }

    @Override
    @RequiresPermission({UserRole.ADMINISTRATOR, UserRole.PROJECT_MANAGER, UserRole.TECHNICIAN})
    public String delete(UUID id) {
        if (checker.hasAccess(this.getClass())) {
            return dao.delete(id);
        }
        else {
            return "No Permission";}
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
