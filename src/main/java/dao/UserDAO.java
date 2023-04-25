package dao;

import be.Customer;
import be.User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class UserDAO implements IDAO<User> {
    @Override
    public String add(User user) {
        String result = "saved";
        return result;
    }

    @Override
    public String update(User user) {
        String result = "updated";
        return result;
    }

    @Override
    public String delete(UUID id) {
        String result = "deleted";
        return result;
    }


    @Override
    public Map<UUID, User> getAll() {
        return null;
    }

    @Override
    public User getById(UUID id) {
        return null;
    }
}
