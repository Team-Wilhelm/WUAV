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
        //TODO set userRole
    }
}
