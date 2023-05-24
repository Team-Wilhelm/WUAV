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

    /**
     * Add a user to the database
     * @param user user to add
     * @return ResultState / NO_PERMISSION
     */
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

    /**
     * Update a user in the database
     * @param user user to update
     * @return ResultState / NO_PERMISSION
     */
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

    /**
     * Delete a user from the database
     * @param id id of the user to delete
     * @return ResultState / NO_PERMISSION
     */
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

    /**
     * Get all users from the database
     * @return Map<UUID, User>
     */
    @Override
    public Map<UUID, User> getAll() {
        return dao.getAll();
    }

    /**
     * Get a user by id from the database
     * @param id id of the user to get
     * @return User
     */
    @Override
    public User getById(UUID id) {
        return dao.getById(id);
    }

    /**
     * Log in a user
     * @param username username
     * @param password password
     * @return boolean
     */
    public boolean logIn(String username, byte[] password){
        return dao.logIn(username, password);
    }
}
