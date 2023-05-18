package bll.manager;

import be.User;
import utils.enums.ResultState;
import utils.enums.UserRole;
import bll.IManager;
import dal.factories.DAOFactory;
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
    public ResultState add(User user) {
        if (checker.hasAccess(this.getClass())) {
            return dao.add(user);
        }
        else {
            return ResultState.NO_PERMISSION;
        }
    }

    @Override
    @RequiresPermission(UserRole.ADMINISTRATOR)
    public ResultState update(User user) {
        if (user.equals(UserModel.getLoggedInUser()) || checker.hasAccess(this.getClass())) {
            return dao.update(user);
        }
        else {
            return ResultState.NO_PERMISSION;
        }
    }

    @Override
    @RequiresPermission(UserRole.ADMINISTRATOR)
    public ResultState delete(UUID id) {
        if (checker.hasAccess(this.getClass())) {
            return dao.delete(id);
        }
        else {
            return ResultState.NO_PERMISSION;
        }
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
