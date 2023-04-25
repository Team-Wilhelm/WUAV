package dao;

import be.Customer;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class CustomerDAO implements IDAO<Customer> {
    @Override
    public String add(Customer customer) {
        String result = "saved";
        return result;
    }

    @Override
    public String update(Customer customer) {
        String result = "updated";
        return result;
    }

    @Override
    public String delete(UUID id) {
        String result = "deleted";
        return result;
    }

    @Override
    public Map<UUID, Customer> getAll() {
        return null;
    }

    @Override
    public Object getById(UUID id) {
        return null;
    }
}
