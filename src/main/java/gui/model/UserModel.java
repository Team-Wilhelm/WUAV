package gui.model;

import be.User;
import bll.IManager;
import bll.ManagerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class UserModel implements IModel<User> {
    private static UserModel instance;
    private IManager userManager;
    private HashMap<UUID, User> users;

    private UserModel() {
        userManager = ManagerFactory.createManager(ManagerFactory.ManagerType.USER);
        users = new HashMap<>();
    }

    public static UserModel getInstance() {
        if (instance == null) {
            instance = new UserModel();
        }
        return instance;
    }

    @Override
    public void add(User user) {

    }

    @Override
    public void update(User user) {

    }

    @Override
    public void delete(UUID id) {

    }

    @Override
    public HashMap<UUID, User> getAll() {
        return null;
    }

    @Override
    public Object getById(UUID id) {
        return null;
    }
}
