package gui.controller.AddControllers;

import be.Address;
import be.Customer;
import be.Document;
import be.User;
import be.enums.CustomerType;
import be.enums.UserRole;
import bll.PdfGenerator;
import gui.controller.ViewControllers.DocumentController;
import gui.model.CustomerModel;
import gui.model.DocumentModel;
import gui.model.UserModel;
import gui.tasks.DeleteTask;
import gui.tasks.SaveTask;
import gui.tasks.TaskState;
import gui.util.AlertManager;
import io.github.palexdev.materialfx.controls.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import utils.BlobService;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class AddDocumentController extends AddController implements Initializable {
    @FXML
    private MFXButton btnCancel, btnDelete, btnSave, btnUploadPictures, btnCreatePdf;
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
    private PdfGenerator pdfGenerator;
    private boolean isEditing;
    private Document documentToEdit;
    private DocumentController documentController;
    private HashMap<Image, String> pictures;
    private AlertManager alertManager;
    private ObservableList<User> allTechnicians = FXCollections.observableArrayList();

    // Document and customer information
    private UUID temporaryId;
    private String city, country, email, houseNumber, jobTitle, name, phoneNumber, postcode, streetName;
    private String jobDescription, notes;
    private CustomerType customerType;
    private Date lastContract;
    private List<User> technicians;

    public AddDocumentController() {
        documentModel = DocumentModel.getInstance();
        customerModel = CustomerModel.getInstance();
        pdfGenerator = new PdfGenerator();
        pictures = new HashMap<>();
        alertManager = AlertManager.getInstance();
        technicians = new ArrayList<>();
        temporaryId = UUID.randomUUID();

        if (UserModel.getInstance().getLoggedInUser() != null
                && UserModel.getInstance().getLoggedInUser().getUserRole() == UserRole.TECHNICIAN)
            technicians.add(UserModel.getInstance().getLoggedInUser());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Disable the save button until the user has filled in the required fields
        // Disable the delete button until the user has selected a document to delete
        isEditing = false;
        btnSave.setDisable(true);
        btnDelete.setDisable(true);
        btnCreatePdf.setDisable(true);

        assignListenersToTextFields();
        setUpListView();
        setUpComboBox();
        dateLastContract.setValue(LocalDate.now());
    }

    @FXML
    private void uploadPicturesAction(ActionEvent actionEvent) throws Exception {
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
            String path = BlobService.getInstance().UploadFile(selectedFile.getAbsolutePath(), documentToEdit.getCustomer().getCustomerID());
            pictures.put(image, path);
            listViewPictures.getItems().add(image);
        }
    }

    @FXML
    private void cancelAction(ActionEvent actionEvent) {
        closeWindow(actionEvent);
    }

    @FXML
    private void saveAction(ActionEvent actionEvent) {
        assignInputToVariables();

        Address address = new Address(streetName, houseNumber, postcode, city, country);
        Customer customer = customerModel.getAll().values().stream()
                .filter(c -> c.getCustomerEmail().equals(email))
                .findFirst()
                .orElse(new Customer(name, email, phoneNumber, address, customerType, lastContract));
        Document document = new Document(customer, jobDescription, notes, jobTitle, Date.valueOf(LocalDate.now()));
        document.setTechnicians(technicians);
        document.setDocumentImages(pictures.values().stream().toList());

        if (isEditing) {
            document.setDocumentID(documentToEdit.getDocumentID());
            customer.setCustomerID(documentToEdit.getCustomer().getCustomerID());
            address.setAddressID(documentToEdit.getCustomer().getCustomerAddress().getAddressID());
        }

        Task<TaskState> task = new SaveTask<>(document, isEditing, documentModel);
        setUpSaveTask(task, documentController, txtCity.getScene().getWindow());
        executeTask(task);

        documentToEdit = document;
        btnCreatePdf.setDisable(false);
    }

    public void deleteAction(ActionEvent actionEvent) {
        Optional<ButtonType> result = alertManager.showConfirmation("Delete user", "Are you sure you want to delete this user?", txtName.getScene().getWindow());
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            Task<TaskState> deleteTask = new DeleteTask<>(documentToEdit.getDocumentID(), documentModel);
            setUpDeleteTask(deleteTask, documentController, txtName.getScene().getWindow());
            executeTask(deleteTask);
        }
        closeWindow(actionEvent);
    }

    public void assignUserToDocument(User technician) {
        if (technician.getAssignedDocuments().get(documentToEdit.getDocumentID()) == null) {
            technician.getAssignedDocuments().put(documentToEdit.getDocumentID(), documentToEdit);
            documentModel.assignUserToDocument(technician, documentToEdit, true);
        } else {
            technician.getAssignedDocuments().remove(documentToEdit.getDocumentID());
            documentModel.assignUserToDocument(technician, documentToEdit, false);
        }
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
        btnCreatePdf.setDisable(false);

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

        // Switch the listeners to editing mode
        comboTechnicians.getSelectionModel().selectedItemProperty().removeListener(technicianListenerNotEditing);
        comboTechnicians.getSelectionModel().selectedItemProperty().addListener(technicianListenerIsEditing);
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
                    Image image = listViewPictures.getSelectionModel().getSelectedValues().get(0);
                    try {
                        Desktop.getDesktop().open(new File(pictures.get(image)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    alertManager.showWarning("No image selected", "Please select an image to open", txtName.getScene().getWindow());
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

    /**
     * Assigns the user to the document in the database.
     */
    private final ChangeListener<User> technicianListenerIsEditing = (observable, oldValue, newValue) -> {
        if (newValue != null) {
            assignUserToDocument(newValue);
            comboTechnicians.getSelectionModel().clearSelection();
            populateComboBox();
        }
    };

    /**
     * Assigns the user to the document and adds them to the list of technicians,
     * saving them in a batch when saving the document.
     */
    private final ChangeListener<User> technicianListenerNotEditing = (observable, oldValue, newValue) -> {
        if (newValue != null) {
            if (!technicians.contains(newValue)) {
                newValue.getAssignedDocuments().put(temporaryId, documentToEdit);
                technicians.add(newValue);
            } else {
                technicians.remove(newValue);
                newValue.getAssignedDocuments().remove(temporaryId);
            }
            comboTechnicians.getSelectionModel().clearSelection();
            populateComboBox();
        }
    };

    private void setUpComboBox() {
        comboTechnicians.getSelectionModel().selectedItemProperty().addListener(technicianListenerNotEditing);

        comboTechnicians.setConverter(new StringConverter<>() {
            @Override
            public String toString(User object) {
                if (object != null) {
                    if (isEditing) {
                        return object.getAssignation(object.getAssignedDocuments().get(documentToEdit.getDocumentID()))
                                + " " + object.getFullName() + " (" + object.getUsername() + ")";
                    } else {
                        if (technicians.contains(object))
                            return "ASSIGNED:" + object.getFullName() + " (" + object.getUsername() + ")";
                    }  return object.getFullName() + " (" + object.getUsername() + ")";
                }
                return null;
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });

        populateComboBox();
    }

    public void setDocumentController(DocumentController documentController) {
        this.documentController = documentController;
    }

    public void createPdfAction(ActionEvent actionEvent) {
        if(isInputChanged(documentToEdit)){
            AlertManager.getInstance().showWarning("Unsaved changes", "Please save changes", txtCity.getScene().getWindow());
        }
        else pdfGenerator.generatePdf(documentToEdit);
    }

    private boolean isInputChanged(Document document){
        Address customerAddress = document.getCustomer().getCustomerAddress();
        return !txtCity.getText().trim().equals(customerAddress.getTown())
                || !txtCountry.getText().trim().equals(customerAddress.getCountry())
                || !txtEmail.getText().trim().equals(document.getCustomer().getCustomerEmail())
                || !txtHouseNumber.getText().trim().equals(customerAddress.getStreetNumber())
                || !txtJobTitle.getText().trim().equals(document.getJobTitle())
                || !txtName.getText().trim().equals(document.getCustomer().getCustomerName())
                || !txtPhoneNumber.getText().trim().equals(document.getCustomer().getCustomerPhoneNumber())
                || !txtPostcode.getText().trim().equals(customerAddress.getPostcode())
                || !txtStreetName.getText().trim().equals(customerAddress.getStreetName())
                || !txtJobDescription.getText().trim().equals(document.getJobDescription())
                || !txtNotes.getText().trim().equals(document.getOptionalNotes());
    }

    private void populateComboBox() {
        allTechnicians.clear();
        allTechnicians.setAll(UserModel.getInstance().getAll().values().stream().filter(user ->
                user.getUserRole() == UserRole.TECHNICIAN).collect(Collectors.toList()));
        comboTechnicians.setItems(allTechnicians);
    }
    // endregion
}
