package gui.controller;

import be.User;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.controls.MFXListView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;

public class AddDocumentController {
    @FXML
    private MFXButton btnCancel, btnDelete, btnSave, btnUploadPictures;
    @FXML
    private MFXFilterComboBox<User> comboTechnicians;
    @FXML
    private MFXListView<?> listViewPictures;
    @FXML
    private MFXTextField txtCity, txtCountry, txtEmail, txtHouseNumber, txtJobTitle, txtName, txtPhoneNumber, txtPostcode, txtStreetName;
    @FXML
    private TextArea txtJobDescription, txtNotes;

    public void uploadPicturesAction(ActionEvent actionEvent) {
    }

    public void cancelAction(ActionEvent actionEvent) {
        ((Node) actionEvent.getSource()).getScene().getWindow().hide();
    }

    public void saveAction(ActionEvent actionEvent) {
    }
}
