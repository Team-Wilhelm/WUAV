package gui.controller;

import be.Document;
import be.User;
import gui.model.IModel;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class AddDocumentController implements Initializable {

    private IModel DocumentModel;
    private Document documentToUpdate;
    private boolean isEditing;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isEditing = false;
    }
}
