package gui.model;

import be.User;
import bll.IManager;
import bll.ManagerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class UserModel implements IModel<User> {
    private static UserModel instance;
    private IManager<User> userManager;
    private HashMap<UUID, User> allUsers;
    private User loggedInUser;

    private UserModel() {
        userManager = ManagerFactory.createManager(ManagerFactory.ManagerType.USER);
        allUsers = new HashMap<>();
    }

    public static UserModel getInstance() {
        if (instance == null) {
            instance = new UserModel();
        }
        return instance;
    }

    @Override
    public CompletableFuture<String> add(User user) {
        String message = userManager.add(user);

        CompletableFuture<Map<UUID, User>> future = CompletableFuture.supplyAsync(() -> userManager.getAll());
        return future.thenApplyAsync(users -> {
            allUsers = (HashMap<UUID, User>) users;
            return message;
        });
    }

    @Override
    public String update(User user, CountDownLatch latch) {
        return userManager.update(user);
    }

    @Override
    public String delete(UUID id) {
        return userManager.delete(id);
    }

    @Override
    public Map<UUID, User> getAll() {
        return userManager.getAll();
    }

    @Override
    public User getById(UUID id) {
        return userManager.getById(id);
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public User getUserByUsername(String username){
        return allUsers.values().stream().filter(user -> user.getUsername().equals(username)).findFirst().orElse(null);
    }

    public List<User> searchUsers(String query) {
        List<User> filteredUsers = new ArrayList<>();
        allUsers.values().stream().filter(user ->
                user.getUsername().toLowerCase().contains(query.toLowerCase())
                        || user.getFullName().toLowerCase().contains(query.toLowerCase())
                        || user.getUserRole().toString().toLowerCase().contains(query.toLowerCase())
        ).forEach(filteredUsers::add);
        return filteredUsers;
    }
}
