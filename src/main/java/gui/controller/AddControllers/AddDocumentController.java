package gui.controller.AddControllers;

import be.Address;
import be.Customer;
import be.Document;
import be.User;
import be.enums.CustomerType;
import gui.controller.ViewControllers.DocumentController;
import gui.model.DocumentModel;
import gui.model.IModel;
import gui.tasks.SaveTask;
import gui.tasks.TaskState;
import io.github.palexdev.materialfx.controls.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
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
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.ResourceBundle;

public class AddDocumentController extends AddController implements Initializable {
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
    @FXML
    private MFXToggleButton toggleCustomerType;
    @FXML
    private MFXDatePicker dateLastContract;

    private DocumentModel documentModel;
    private boolean isEditing;
    private Document documentToEdit;
    private DocumentController documentController;
    private HashMap<Image, String> pictures;

    // Document and customer information
    private String city, country, email, houseNumber, jobTitle, name, phoneNumber, postcode, streetName;
    private String jobDescription, notes;
    private CustomerType customerType;
    private Date lastContract;

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
        dateLastContract.setValue(LocalDate.now());
    }

    @FXML
    private void uploadPicturesAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.jpeg"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        fileChooser.setTitle("Choose a picture");

        // Try to set the initial directory to the user's pictures folder
        try {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Pictures"));
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        closeWindow(actionEvent);
    }

    @FXML
    private void saveAction(ActionEvent actionEvent) {
        closeWindow(actionEvent);
        assignInputToVariables();

        Address address = new Address(streetName, houseNumber, postcode, city, country);
        Customer customer = new Customer(name, email, phoneNumber, address, customerType, lastContract);
        Document document = new Document(customer, jobTitle, jobDescription, notes);
        if (isEditing) document.setDocumentID(documentToEdit.getDocumentID());

        Task<TaskState> task = new SaveTask<>(document, isEditing, documentModel);
        setUpSaveTask(task, documentController, actionEvent);
        executeTask(task);
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

    private void assignInputToVariables() {
        city = txtCity.getText();
        country = txtCountry.getText();
        email = txtEmail.getText();
        houseNumber = txtHouseNumber.getText();
        jobTitle = txtJobTitle.getText();
        name = txtName.getText();
        phoneNumber = txtPhoneNumber.getText();
        postcode = txtPostcode.getText();
        streetName = txtStreetName.getText();
        customerType = toggleCustomerType.isSelected() ? CustomerType.PRIVATE : CustomerType.BUSINESS;
        jobDescription = txtJobDescription.getText();
        notes = txtNotes.getText();
        lastContract = dateLastContract.getValue() != null ? Date.valueOf(dateLastContract.getValue()) : Date.valueOf(LocalDate.now());
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

    public void setDocumentController(DocumentController documentController) {
        this.documentController = documentController;
    }
}
