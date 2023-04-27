package gui.controller;

import be.Document;
import be.User;
import gui.model.DocumentModel;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.controls.MFXListView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class AddDocumentController implements Initializable {
    @FXML
    private MFXButton btnCancel, btnDelete, btnSave, btnUploadPictures;
    @FXML
    private MFXFilterComboBox<User> comboTechnicians;
    @FXML
    private MFXListView<Image> listViewPictures;
    @FXML
    private MFXTextField txtCity, txtCountry, txtEmail, txtHouseNumber, txtJobTitle, txtName, txtPhoneNumber, txtPostcode, txtStreetName;
    @FXML
    private TextArea txtJobDescription, txtNotes;

    private DocumentModel documentModel;
    private boolean isEditing;
    private Document documentToEdit;
    private String city, country, email, houseNumber, jobTitle, name, phoneNumber, postcode, streetName;
    private HashMap<Image, String> pictures;

    public AddDocumentController() {
        documentModel = DocumentModel.getInstance();
        pictures = new HashMap<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isEditing = false;
        btnSave.setDisable(true);
        assignListenersToTextFields();
        setUpListView();
    }

    @FXML
    private void uploadPicturesAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose profile picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.jpeg"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            //TODO blobs
            Image image = new Image(selectedFile.toURI().toString());
            pictures.put(image, selectedFile.getAbsolutePath());
            listViewPictures.getItems().add(image);
        }
    }

    @FXML
    private void cancelAction(ActionEvent actionEvent) {
        ((Node) actionEvent.getSource()).getScene().getWindow().hide();
    }

    @FXML
    private void saveAction(ActionEvent actionEvent) {

    }

    // UTILITIES & HELPERS
    private void setDocumentToEdit(Document document) {
        isEditing = true;
        documentToEdit = document;
    }

    private void assignListenersToTextFields() {
        txtCity.textProperty().addListener(inputListener);
        txtCountry.textProperty().addListener(inputListener);
        txtEmail.textProperty().addListener(inputListener);
        txtHouseNumber.textProperty().addListener(inputListener);
        txtJobTitle.textProperty().addListener(inputListener);
        txtName.textProperty().addListener(inputListener);
        txtPhoneNumber.textProperty().addListener(inputListener);
        txtPostcode.textProperty().addListener(inputListener);
        txtStreetName.textProperty().addListener(inputListener);
        txtJobDescription.textProperty().addListener(inputListener);
        txtNotes.textProperty().addListener(inputListener);
    }

    private void checkInput() {

    }

    private final ChangeListener<String> inputListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (txtCity.getText().isEmpty() || txtCountry.getText().isEmpty() || txtEmail.getText().isEmpty() || txtHouseNumber.getText().isEmpty()
                    || txtJobTitle.getText().isEmpty() || txtName.getText().isEmpty() || txtPhoneNumber.getText().isEmpty()
                    || txtPostcode.getText().isEmpty() || txtStreetName.getText().isEmpty() || txtJobDescription.getText().isEmpty()) {
                btnSave.setDisable(true);
            } else {
                btnSave.setDisable(false);
            }
        }
    };

    private void setUpListView() {
        listViewPictures.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2) {
                if (!listViewPictures.getSelectionModel().getSelection().isEmpty()) {
                    Image ticket = listViewPictures.getSelectionModel().getSelectedValues().get(0);
                    try {
                        Desktop.getDesktop().open(new File(pictures.get(ticket)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    new Alert(Alert.AlertType.ERROR, "Please select a ticket!").showAndWait();
                }
            }
        });

        listViewPictures.setConverter(new StringConverter<Image>() {
            @Override
            public String toString(Image image) {
                String path = pictures.get(image);
                return path.substring(path.lastIndexOf("\\") + 1);
            }

            @Override
            public Image fromString(String string) {
                return null;
            }
        });
    }
}
