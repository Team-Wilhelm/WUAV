package gui.controller.AddControllers;

import be.Address;
import be.Customer;
import be.Document;
import be.User;
import be.enums.CustomerType;
import gui.controller.ViewControllers.DocumentController;
import gui.model.CustomerModel;
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
    private CustomerModel customerModel;
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
        customerModel = CustomerModel.getInstance();
        pictures = new HashMap<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isEditing = false;
        btnSave.setDisable(true);
        btnDelete.setDisable(true);
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
        Customer customer = customerModel.getAll().values().stream()
                .filter(c -> c.getCustomerEmail().equals(email))
                .findFirst()
                .orElse(new Customer(name, email, phoneNumber, address, customerType, lastContract));
        Document document = new Document(customer, jobDescription, notes, jobTitle, Date.valueOf(LocalDate.now()));

        if (isEditing) {
            document.setDocumentID(documentToEdit.getDocumentID());
            customer.setCustomerID(documentToEdit.getCustomer().getCustomerID());
            address.setAddressID(documentToEdit.getCustomer().getCustomerAddress().getAddressID());
        }

        Task<TaskState> task = new SaveTask<>(document, isEditing, documentModel);
        setUpSaveTask(task, documentController, txtCity.getScene().getWindow());
        executeTask(task);
    }

    /**
     * Disables the save button if any of the required text fields are empty.
     */
    private final ChangeListener<String> inputListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (isInputEmpty(txtCity) || isInputEmpty(txtCountry) || isInputEmpty(txtEmail) || isInputEmpty(txtHouseNumber)
                    || isInputEmpty(txtJobTitle) || isInputEmpty(txtName) || isInputEmpty(txtPhoneNumber)
                    || isInputEmpty(txtPostcode) || isInputEmpty(txtStreetName) || isInputEmpty(txtJobDescription)) {
                btnSave.setDisable(true);
            } else {
                btnSave.setDisable(false);
            }
        }
    };

    // region Utilities, helpers and setters
    public void setDocumentToEdit(Document document) {
        isEditing = true;
        documentToEdit = document;
        btnSave.setDisable(false);
        btnDelete.setDisable(false);

        // Customer information
        txtName.setText(document.getCustomer().getCustomerName());
        txtEmail.setText(document.getCustomer().getCustomerEmail());
        txtPhoneNumber.setText(document.getCustomer().getCustomerPhoneNumber());
        dateLastContract.setValue(document.getCustomer().getLastContract().toLocalDate());
        toggleCustomerType.setSelected(document.getCustomer().getCustomerType() == CustomerType.PRIVATE);

        // Customer address
        txtStreetName.setText(document.getCustomer().getCustomerAddress().getStreetName());
        txtHouseNumber.setText(document.getCustomer().getCustomerAddress().getStreetNumber());
        txtCity.setText(document.getCustomer().getCustomerAddress().getTown());
        txtPostcode.setText(document.getCustomer().getCustomerAddress().getPostcode());
        txtCountry.setText(document.getCustomer().getCustomerAddress().getCountry());

        // Document information
        txtJobTitle.setText(document.getJobTitle());
        txtJobDescription.setText(document.getJobDescription());
        txtNotes.setText(document.getOptionalNotes());
    }

    protected void assignListenersToTextFields() {
        // Customer information
        txtName.textProperty().addListener(inputListener);
        txtEmail.textProperty().addListener(inputListener);
        txtPhoneNumber.textProperty().addListener(inputListener);

        // Customer address
        txtStreetName.textProperty().addListener(inputListener);
        txtHouseNumber.textProperty().addListener(inputListener);
        txtCity.textProperty().addListener(inputListener);
        txtPostcode.textProperty().addListener(inputListener);
        txtCountry.textProperty().addListener(inputListener);

        // Document information
        txtJobTitle.textProperty().addListener(inputListener);
        txtJobDescription.textProperty().addListener(inputListener);
        txtNotes.textProperty().addListener(inputListener);
    }

    protected void assignInputToVariables() {
        // Customer information
        name = txtName.getText();
        email = txtEmail.getText();
        phoneNumber = txtPhoneNumber.getText();
        customerType = toggleCustomerType.isSelected() ? CustomerType.PRIVATE : CustomerType.BUSINESS;
        lastContract = dateLastContract.getValue() != null ? Date.valueOf(dateLastContract.getValue()) : Date.valueOf(LocalDate.now());

        // Customer address
        streetName = txtStreetName.getText();
        houseNumber = txtHouseNumber.getText();
        city = txtCity.getText();
        postcode = txtPostcode.getText();
        country = txtCountry.getText();

        // Document information
        jobTitle = txtJobTitle.getText();
        jobDescription = txtJobDescription.getText();
        notes = txtNotes.getText();
    }

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
