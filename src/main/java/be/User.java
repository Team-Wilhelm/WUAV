package be;

import be.enums.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private UUID userID;
    private String fullName, username, password;
    private List<Document> assignedDocuments;
    private UserRole userRole;

    public User(){
        assignedDocuments = new ArrayList<>();
    }

    public User(String fullName, String username, String password, UserRole userRole) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.assignedDocuments = new ArrayList<>();
        this.userRole = userRole;
    }

    public User(UUID userID, String fullName, String username, String password, UserRole userRole) {
        this(fullName, username, password, userRole);
        this.userID = userID;
    }

    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Document> getAssignedDocuments() {
        return assignedDocuments;
    }

    public void setAssignedDocuments(List<Document> assignedDocuments) {
        this.assignedDocuments = assignedDocuments;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

}
