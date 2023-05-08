package gui.controller.AddControllers;

import be.*;
import be.cards.ImagePreview;
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
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import utils.BlobService;
import utils.ThreadPool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.ResourceBundle;

public class AddDocumentController extends AddController implements Initializable {
    //TODO imageListView
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab jobInformationTab, customerInformationTab, picturesTab, pdfTab;
    @FXML
    private FlowPane flowPanePictures;
    @FXML
    private MFXButton btnCancel, btnDelete, btnSave, btnUploadPictures, btnCreatePdf, btnNextJobTab, btnNextCustomerTab;
    @FXML
    private MFXFilterComboBox<User> comboTechnicians;
    @FXML
    private MFXTextField txtCity, txtCountry, txtEmail, txtHouseNumber, txtJobTitle, txtName, txtPhoneNumber, txtPostcode, txtStreetName;
    @FXML
    private TextArea txtJobDescription, txtNotes;
    @FXML
    private MFXToggleButton toggleCustomerType;
    @FXML
    private MFXDatePicker dateLastContract;
    private MFXContextMenu contextMenu;

    private DocumentModel documentModel;
    private CustomerModel customerModel;
    private PdfGenerator pdfGenerator;
    private boolean isEditing;
    private Document documentToEdit;
    private DocumentController documentController;
    private final ObservableList<ImageWrapper> pictures;
    private final ObservableList<ImagePreview> imagePreviews = FXCollections.observableArrayList();
    private AlertManager alertManager;
    private ObservableList<User> allTechnicians;
    private final ThreadPool executorService;
    private ImagePreview lastFocused;

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
        pictures = FXCollections.observableArrayList();
        alertManager = AlertManager.getInstance();
        technicians = new ArrayList<>();
        temporaryId = UUID.randomUUID();
        executorService = ThreadPool.getInstance();
        allTechnicians = FXCollections.observableArrayList();

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
        setUpComboBox();
        refreshItems();
        dateLastContract.setValue(LocalDate.now());

        Bindings.bindContent(flowPanePictures.getChildren(), imagePreviews);

        // Set up the context menu for the list view
        contextMenu = new MFXContextMenu(flowPanePictures);
        MFXContextMenuItem deleteItem = MFXContextMenuItem.Builder.build()
                .setText("Delete")
                .setAccelerator("Ctrl + D")
                .setOnAction(event -> {
                    ImageWrapper image = lastFocused.getImageWrapper();
                    pictures.remove(image);
                })
                .setIcon(new MFXFontIcon("fas-delete-left", 16))
                .get();

        contextMenu.getItems().add(deleteItem);
        Platform.runLater(() -> flowPanePictures.getScene().setOnContextMenuRequested(event -> contextMenu.show(flowPanePictures, event.getScreenX(), event.getScreenY())));
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
            String path = BlobService.getInstance().UploadFile(selectedFile.getAbsolutePath(), documentToEdit.getCustomer().getCustomerID());
            ImageWrapper image = new ImageWrapper(path, selectedFile.getName());
            pictures.add(image);
        }
        refreshItems();
    }

    @FXML
    private void saveAction(ActionEvent actionEvent) {
        //TODO change the way we're dealing with customers
        assignInputToVariables();

        Address address = new Address(streetName, houseNumber, postcode, city, country);
        Customer customer = customerModel.getAll().values().stream()
                .filter(c -> c.getCustomerEmail().equals(email))
                .findFirst()
                .orElse(new Customer(name, email, phoneNumber, address, customerType, lastContract));
        Document document = new Document(customer, jobDescription, notes, jobTitle, Date.valueOf(LocalDate.now()));
        document.setTechnicians(technicians);
        document.setDocumentImages(pictures);

        if (isEditing) {
            document.setDocumentID(documentToEdit.getDocumentID());
            customer.setCustomerID(documentToEdit.getCustomer().getCustomerID());
            address.setAddressID(documentToEdit.getCustomer().getCustomerAddress().getAddressID());
        }

        Task<TaskState> task = new SaveTask<>(document, isEditing, documentModel);
        setUpSaveTask(task, documentController, txtCity.getScene().getWindow());
        executorService.execute(task);

        documentToEdit = document;
        btnCreatePdf.setDisable(false);
    }

    @FXML
    private void deleteAction(ActionEvent actionEvent) {
        Optional<ButtonType> result = alertManager.showConfirmation("Delete user", "Are you sure you want to delete this user?", txtName.getScene().getWindow());
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            Task<TaskState> deleteTask = new DeleteTask<>(documentToEdit.getDocumentID(), documentModel);
            setUpDeleteTask(deleteTask, documentController, txtName.getScene().getWindow());
            executorService.execute(deleteTask);
        }
        closeWindow(actionEvent);
    }

    @FXML
    private void nextAction(ActionEvent actionEvent) {
        tabPane.getSelectionModel().selectNext();
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

        // Pictures
        pictures.setAll(document.getDocumentImages());

        // Switch the listeners to editing mode
        comboTechnicians.getSelectionModel().selectedItemProperty().removeListener(technicianListenerNotEditing);
        comboTechnicians.getSelectionModel().selectedItemProperty().addListener(technicianListenerIsEditing);

        refreshItems();
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

    public void refreshItems() {
        imagePreviews.clear();
        pictures.forEach(image -> {
            ImagePreview imagePreview = image.getImagePreview();
            imagePreview.setOnMouseClicked(e -> {
                if (!imagePreview.isFocused()) {
                    imagePreview.requestFocus();
                    lastFocused = imagePreview;
                }

                if (e.getClickCount() == 2) {
                    try {
                        // Get the blob url, download picture and open the image in the default image viewer
                        String downloadPath = System.getProperty("user.home") + "/Downloads/" + image.getName();
                        Image imageToOpen = image.getImage();
                        File file = new File(downloadPath);
                        ImageIO.write(SwingFXUtils.fromFXImage(imageToOpen, null), "png", file);
                        Desktop.getDesktop().open(file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            imagePreviews.add(imagePreview);
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
        if (!txtCity.getText().trim().equals(customerAddress.getTown()))
            return false;
        if (!txtCountry.getText().trim().equals(customerAddress.getCountry()))
            return false;
        if (!txtEmail.getText().trim().equals(document.getCustomer().getCustomerEmail()))
            return false;
        if (!txtHouseNumber.getText().trim().equals(customerAddress.getStreetNumber()))
            return false;
        if (!txtJobTitle.getText().trim().equals(document.getJobTitle()))
            return false;
        if (!txtName.getText().trim().equals(document.getCustomer().getCustomerName()))
            return false;
        if (!txtPhoneNumber.getText().trim().equals(document.getCustomer().getCustomerPhoneNumber()))
            return false;
        if (!txtPostcode.getText().trim().equals(customerAddress.getPostcode()))
            return false;
        if (!txtStreetName.getText().trim().equals(customerAddress.getStreetName()))
            return false;
        if (!txtJobDescription.getText().trim().equals(document.getJobDescription()))
            return false;
        if (!txtNotes.getText().trim().equals(document.getOptionalNotes()))
            return false;
        if (!dateLastContract.getValue().equals(document.getCustomer().getLastContract()))
            return false;
        if (!toggleCustomerType.isSelected() == document.getCustomer().getCustomerType().equals(CustomerType.PRIVATE))
            return false;
        if (!comboTechnicians.getSelectionModel().getSelectedItem().equals(document.getTechnicians()))
            return false;
        if (!pictures.equals(document.getDocumentImages()))
            return false;
        return true;
    }

    private void populateComboBox() {
        allTechnicians.clear();
        allTechnicians.setAll(UserModel.getInstance().getAll().values().stream().filter(user ->
                user.getUserRole() == UserRole.TECHNICIAN).collect(Collectors.toList()));
        comboTechnicians.setItems(allTechnicians);
    }
    // endregion
}
