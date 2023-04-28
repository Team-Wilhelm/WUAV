package gui.model;

import be.User;
import be.cards.UserCard;
import bll.IManager;
import bll.ManagerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserModel implements IModel<User> {
    private static UserModel instance;
    private IManager<User> userManager;
    private HashMap<UUID, User> allUsers;
    private HashMap<User, UserCard> loadedCards;
    private static User loggedInUser;

    private UserModel() {
        userManager = ManagerFactory.createManager(ManagerFactory.ManagerType.USER);
        loadedCards = new HashMap<>();
        setAllUsersFromManager();
        createUserCards();
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
    public CompletableFuture<String> update(User user) {
        String message = userManager.update(user);
        CompletableFuture<Map<UUID, User>> future = CompletableFuture.supplyAsync(() -> userManager.getAll());
        return future.thenApplyAsync(users -> {
            allUsers = (HashMap<UUID, User>) users;
            return message;
        });
    }

    @Override
    public CompletableFuture<String> delete(UUID id) {
        String message = userManager.delete(id);
        CompletableFuture<Map<UUID, User>> future = CompletableFuture.supplyAsync(() -> userManager.getAll());
        return future.thenApplyAsync(users -> {
            allUsers = (HashMap<UUID, User>) users;
            return message;
        });
    }

    /**
     * Get all users stored in the model
     * @return a map of all users
     */
    @Override
    public Map<UUID, User> getAll() {
        return allUsers;
    }

    @Override
    public User getById(UUID id) {
        return userManager.getById(id);
    }

    /**
     * Reloads all users from the database
     */
    public void setAllUsersFromManager() {
        this.allUsers = (HashMap<UUID, User>) userManager.getAll();;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        UserModel.loggedInUser = loggedInUser;
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

    public Map<User, UserCard> getLoadedCards() {
        return loadedCards;
    }

    /**
     * Creates a card for each user
     */
    public void createUserCards() {
        for (User user : allUsers.values()) {
            loadedCards.put(user, new UserCard(user));
        }
    }
}
