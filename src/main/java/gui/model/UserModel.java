package gui.model;

import be.User;
import gui.nodes.UserCard;
import bll.ManagerFactory;
import bll.manager.UserManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserModel implements IModel<User> {
    private static UserModel instance;
    private UserManager userManager;
    private HashMap<UUID, User> allUsers;
    private HashMap<User, UserCard> loadedCards;
    private static User loggedInUser;

    private UserModel() {
        userManager = (UserManager) ManagerFactory.createManager(ManagerFactory.ManagerType.USER);
        allUsers = new HashMap<>();
        loadedCards = new HashMap<>();
        setAllUsersFromManager();
        createUserCards();
    }

    public static UserModel getInstance() {
        if (instance == null) {
            instance = new UserModel();
        } return instance;
    }

    @Override
    public CompletableFuture<String> add(User user) {
        String message = userManager.add(user);
        CompletableFuture<Map<UUID, User>> future = CompletableFuture.supplyAsync(() -> userManager.getAll());
        return future.thenApplyAsync(users -> {
            allUsers.clear();
            allUsers.putAll(users);
            return message;
        });
    }

    @Override
    public String update(User user) {
        return userManager.update(user);
    }

    @Override
    public String delete(UUID id) {
        return userManager.delete(id);
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

    public User getByIDFFromModel(UUID id) {
        return allUsers.get(id);
    }

    /**
     * Reloads all users from the database
     */
    public void setAllUsersFromManager() {
        allUsers.clear();
        allUsers.putAll(userManager.getAll());
    }

    public static User getLoggedInUser() {
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
            UserCard userCard = new UserCard(user);
            loadedCards.put(user, userCard);
            //user.addObserver(userCard);
        }
    }

    public boolean logIn(String username, byte[] password){
        return userManager.logIn(username, password);
    }

    public UserCard addUserCard(User user){
        UserCard userCard = new UserCard(user);
        loadedCards.put(user, userCard);
        //user.addObserver(userCard);
        return userCard;
    }
}
