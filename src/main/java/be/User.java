package be;

import be.enums.UserRole;
import javafx.scene.image.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class User {
    private UUID userID;
    private String fullName, username, phoneNumber, profilePicturePath;
    private byte[] password;
    private List<Document> assignedDocuments;
    private UserRole userRole;
    private Image profilePicture;

    public User(){
        assignedDocuments = new ArrayList<>();
    }

    public User(String fullName, String username, byte[] password, String phoneNumber, UserRole userRole, String profilePicturePath) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.userRole = userRole;
        this.assignedDocuments = new ArrayList<>();
        this.profilePicturePath = profilePicturePath;

        if (profilePicturePath != null) {
            File file = new File(profilePicturePath);
            this.profilePicture = new Image(file.toURI().toString());
        } else {
            this.profilePicturePath = "/img/userIcon.png";
            this.profilePicture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/userIcon.png")));
        }
    }

    public User(UUID userID, String fullName, String username, byte[] password, String phoneNumber, UserRole userRole, String profilePicturePath) {
        this(fullName, username, password, phoneNumber, userRole, profilePicturePath);
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

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
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

    public Image getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Image profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void addDocument(Document document) {
        assignedDocuments.add(document);
    }

    public void removeDocument(Document document) {
        assignedDocuments.remove(document);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
}
