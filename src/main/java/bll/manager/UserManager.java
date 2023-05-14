package bll.manager;

import be.User;
import bll.IManager;
import dal.DAOFactory;
import dal.dao.UserDAO;

import java.util.Map;
import java.util.UUID;

public class UserManager implements IManager<User> {
    private UserDAO dao;

    public UserManager() {
        dao = (UserDAO) DAOFactory.createDAO(DAOFactory.DAOType.USER);
    }

    @Override
    public String add(User user) {
        return dao.add(user);
    }

    @Override
    public String update(User user) {
        return dao.update(user);
    }

    @Override
    public String delete(UUID id) {
        return dao.delete(id);
    }

    @Override
    public Map<UUID, User> getAll() {
        return dao.getAll();
    }

    @Override
    public User getById(UUID id) {
        return dao.getById(id);
    }

    public boolean logIn(String username, byte[] password){
        return dao.logIn(username, password);
    }
}
