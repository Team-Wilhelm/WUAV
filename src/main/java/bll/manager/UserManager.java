package bll.manager;

import be.User;
import be.enums.UserRole;
import bll.IManager;
import dal.DAOFactory;
import dal.dao.UserDAO;
import gui.model.UserModel;
import utils.permissions.Checker;
import utils.permissions.RequiresPermission;

import java.util.Map;
import java.util.UUID;

public class UserManager implements IManager<User> {
    private UserDAO dao;
    private Checker checker;

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
    @RequiresPermission(UserRole.ADMINISTRATOR)
    public String delete(UUID id) {

        if (id.equals(UserModel.getInstance().getLoggedInUser().getUserID())
                || checker.hasAccess(this.getClass())) {
            return dao.delete(id);
        }
        else {
            throw new SecurityException("You do not have permission to delete this user.");}
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
