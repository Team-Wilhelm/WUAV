package gui.controller.AddControllers;

import be.User;
import gui.model.IModel;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;

import java.net.URL;
import java.util.ResourceBundle;

public class AddUserController implements Initializable {
    private IModel UserModel;
    private User userToUpdate;
    private boolean isEditing;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isEditing = false;
    }

}
