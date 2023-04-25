package gui.model;

import be.User;
import bll.IManager;
import bll.ManagerFactory;
import dao.IDAO;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserModel implements IModel<User> {
    private static UserModel instance;
    private IManager<User> bll;
    private HashMap<UUID, User> users;
    private User loggedInUser;

    private UserModel() {
        bll = ManagerFactory.createManager(ManagerFactory.ManagerType.USER);
        users = new HashMap<>();
    }

    public static UserModel getInstance() {
        if (instance == null) {
            instance = new UserModel();
        }
        return instance;
    }

    @Override
    public String add(User user) {
        return bll.add(user);
    }

    @Override
    public String update(User user) {
        return bll.update(user);
    }

    @Override
    public String delete(UUID id) {
        return bll.delete(id);
    }

    @Override
    public Map<UUID, User> getAll() {
        return bll.getAll();
    }

    @Override
    public User getById(UUID id) {
        return bll.getById(id);
    }
}
