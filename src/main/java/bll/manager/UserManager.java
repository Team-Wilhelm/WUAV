package bll.manager;

import be.User;
import be.enums.UserRole;
import bll.IManager;
import dal.DAOFactory;
import dal.dao.UserDAO;
import gui.model.UserModel;
import utils.permissions.AccessChecker;
import utils.permissions.RequiresPermission;

import java.util.Map;
import java.util.UUID;

public class UserManager implements IManager<User> {
    private UserDAO dao;
    private AccessChecker checker = new AccessChecker();

    public UserManager() {
        dao = (UserDAO) DAOFactory.createDAO(DAOFactory.DAOType.USER);
    }

    @Override
    @RequiresPermission(UserRole.ADMINISTRATOR)
    public String add(User user) {
        if (checker.hasAccess(this.getClass())) {
            return dao.add(user);
        }
        else {
            return "No Permission";}
    }

    @Override
    @RequiresPermission(UserRole.ADMINISTRATOR)
    public String update(User user) {
        if (user.equals(UserModel.getLoggedInUser()) || checker.hasAccess(this.getClass())) {
            return dao.update(user);
        }
        else {
            return "No Permission";}
    }

    @Override
    @RequiresPermission(UserRole.ADMINISTRATOR)
    public String delete(UUID id) {
        if (checker.hasAccess(this.getClass())) {
            return dao.delete(id);
        }
        else {
           return "No Permission";}
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
