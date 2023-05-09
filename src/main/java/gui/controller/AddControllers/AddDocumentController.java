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
import io.github.palexdev.materialfx.controls.cell.MFXCheckListCell;
import io.github.palexdev.materialfx.controls.cell.MFXListCell;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
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
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab jobInformationTab, customerInformationTab, picturesTab, pdfTab;
    @FXML
    private FlowPane flowPanePictures;
    @FXML
    private GridPane gridPanePdf;
    @FXML
    private MFXButton btnDelete, btnSave, btnUploadPictures, btnCreatePdf, btnNextJobTab, btnNextCustomerTab;
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
    @FXML
    private MFXCheckListView<Document> checkListViewDocuments;
    private MFXContextMenu contextMenu;

    private DocumentModel documentModel;
    private CustomerModel customerModel;
    private PdfGenerator pdfGenerator;
    private Document documentToEdit, currentDocument;
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
    private BooleanProperty isInputChanged, isEditing;

    public AddDocumentController() {
        documentModel = DocumentModel.getInstance();
        customerModel = CustomerModel.getInstance();
        executorService = ThreadPool.getInstance();
        alertManager = AlertManager.getInstance();
        pdfGenerator = new PdfGenerator();

        technicians = new ArrayList<>();
        temporaryId = UUID.randomUUID();
        pictures = FXCollections.observableArrayList();
        allTechnicians = FXCollections.observableArrayList();

        if (UserModel.getInstance().getLoggedInUser() != null
                && UserModel.getInstance().getLoggedInUser().getUserRole() == UserRole.TECHNICIAN)
            technicians.add(UserModel.getInstance().getLoggedInUser());
        isInputChanged = new SimpleBooleanProperty(true);
        isEditing = new SimpleBooleanProperty(false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnNextJobTab.setDisable(true);
        btnNextCustomerTab.setDisable(true);
        customerInformationTab.setDisable(true);
        picturesTab.setDisable(true);
        pdfTab.setDisable(true);
        tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);

        assignListenersToTextFields();
        setUpComboBox();
        setUpContextMenu();
        dateLastContract.setValue(LocalDate.now());

        Bindings.bindContent(flowPanePictures.getChildren(), imagePreviews);
        btnSave.disableProperty().bind(isInputChanged.not());
        btnDelete.disableProperty().bind(isEditing.not());
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
            UUID customerId = documentToEdit != null ? documentToEdit.getCustomer().getCustomerID() : UUID.randomUUID();
            String path = BlobService.getInstance().UploadFile(selectedFile.getAbsolutePath(), customerId);
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
        currentDocument = new Document(customer, jobDescription, notes, jobTitle, Date.valueOf(LocalDate.now()));
        currentDocument.setTechnicians(technicians);
        currentDocument.setDocumentImages(pictures);

        if (isEditing.get()) {
            currentDocument.setDocumentID(documentToEdit.getDocumentID());
            customer.setCustomerID(documentToEdit.getCustomer().getCustomerID());
            address.setAddressID(documentToEdit.getCustomer().getCustomerAddress().getAddressID());
        }

        Task<TaskState> task = new SaveTask<>(currentDocument, isEditing.get(), documentModel);
        setUpSaveTask(task, documentController, txtCity.getScene().getWindow());
        executorService.execute(task);

        setDocumentToEdit(currentDocument);
        pdfTab.setDisable(false);
        isInputChanged.set(false);
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
            technicians.add(technician);
            documentModel.assignUserToDocument(technician, documentToEdit, true);
        } else {
            technician.getAssignedDocuments().remove(documentToEdit.getDocumentID());
            technicians.remove(technician);
            documentModel.assignUserToDocument(technician, documentToEdit, false);
        }
    }


    // region Listeners
    /**
     * Disables switching to the next tab if any of the required job text fields are empty.
     */
    private final ChangeListener<String> jobInputListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            boolean isNotFilled = isInputEmpty(txtJobTitle) || isInputEmpty(txtJobDescription);
            btnNextJobTab.setDisable(isNotFilled);
            customerInformationTab.setDisable(isNotFilled);
        }
    };

    /**
     * Disables switching to the next tab if any of the required customer text fields are empty.
     */
    private final ChangeListener<String> customerInputListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            boolean isNotFilled = isInputEmpty(txtName) || isInputEmpty(txtEmail)
                    || isInputEmpty(txtPhoneNumber) || isInputEmpty(txtStreetName)
                    || isInputEmpty(txtHouseNumber) || isInputEmpty(txtPostcode)
                    || isInputEmpty(txtCity) || isInputEmpty(txtCountry);
            btnNextCustomerTab.setDisable(isNotFilled);
            picturesTab.setDisable(isNotFilled);
        }
    };

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


    /**
     * Listens for changes in the tab selection and prompts the user to save the document if they are editing it.
     */
    private final ChangeListener<Tab> tabChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
            if (newValue.equals(pdfTab) || newValue.equals(picturesTab)) {
                if (isEditing.get()) {
                    isInputChanged(documentToEdit);
                } else if (currentDocument != null && !isEditing.get()) {
                    isInputChanged(currentDocument);
                }

                if (isInputChanged.get() && newValue.equals(pdfTab)) {
                    Optional<ButtonType> result = alertManager.showConfirmation("Unsaved changes", "You have unsaved changes. Do you want to save them?", txtName.getScene().getWindow());
                    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                        saveAction(null);
                    }
                }
                setUpPdfListView();
            }
        }
    };
    // endregion

    // region Utilities, helpers and setters
    public void setDocumentToEdit(Document document) {
        isEditing.setValue(true);
        documentToEdit = document;
        pdfTab.setDisable(false);
        isInputChanged.set(false);

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

        setUpPdfListView();
        refreshItems();
    }

    protected void assignListenersToTextFields() {
        // Customer information
        txtName.textProperty().addListener(customerInputListener);
        txtEmail.textProperty().addListener(customerInputListener);
        txtPhoneNumber.textProperty().addListener(customerInputListener);

        // Customer address
        txtStreetName.textProperty().addListener(customerInputListener);
        txtHouseNumber.textProperty().addListener(customerInputListener);
        txtCity.textProperty().addListener(customerInputListener);
        txtPostcode.textProperty().addListener(customerInputListener);
        txtCountry.textProperty().addListener(customerInputListener);

        // Document information
        txtJobTitle.textProperty().addListener(jobInputListener);
        txtJobDescription.textProperty().addListener(jobInputListener);
        txtNotes.textProperty().addListener(jobInputListener);
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

        // Pictures
        pictures.clear();
        pictures.addAll(imagePreviews.stream().map(ImagePreview::getImageWrapper).toList());
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

            // Add drag and drop functionality to the image preview
            imagePreview.setOnDragDetected(dragDetected);
            imagePreview.setOnDragOver(dragOver);
            imagePreview.setOnDragDropped(dragDropped);

            imagePreviews.add(imagePreview);
        });

    }

    // Event handler for when the user drops an image preview on the flow pane
    private EventHandler<MouseEvent> dragDetected = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            ImagePreview imagePreview = (ImagePreview) event.getSource();
            Dragboard db = imagePreview.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent clipboard = new ClipboardContent();
            final int index = flowPanePictures.getChildrenUnmodifiable().indexOf(imagePreview);
            clipboard.putString(String.valueOf(index));
            db.setContent(clipboard);
            event.consume();
        }
    };

    private EventHandler<DragEvent> dragOver = new EventHandler<>() {
        @Override
        public void handle(DragEvent event) {
            ImagePreview imagePreview = (ImagePreview) event.getSource();
            boolean isAccepted = true;
            final Dragboard dragboard = event.getDragboard();

            if (dragboard.hasString()) {
                try {
                    final int newIndex = Integer.parseInt(dragboard.getString());
                    if (newIndex == flowPanePictures.getChildrenUnmodifiable().indexOf(imagePreview)) {
                        isAccepted = false;
                    }
                } catch (NumberFormatException e) {
                    isAccepted = false;
                }
            } else {
                isAccepted = false;
            }
            if (isAccepted) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        }
    };

    private EventHandler<DragEvent> dragDropped = new EventHandler<>() {
        @Override
        public void handle(DragEvent event) {
            boolean success = false;
            ImagePreview imagePreview = (ImagePreview) event.getSource();
            final Dragboard dragboard = event.getDragboard();

            if (dragboard.hasString()) {
                try {
                    final int newIndex = Integer.parseInt(dragboard.getString());
                    final int currentIndex = imagePreviews.indexOf(imagePreview);

                    final int laterIndex = Math.max(newIndex, currentIndex);
                    ImagePreview removedLater = imagePreviews.remove(laterIndex);
                    final int earlierIndex = Math.min(newIndex, currentIndex);
                    ImagePreview removedEarlier = imagePreviews.remove(earlierIndex);
                    imagePreviews.add(earlierIndex, removedLater);
                    imagePreviews.add(laterIndex, removedEarlier);

                    pictures.clear();
                    pictures.addAll(imagePreviews.stream().map(ImagePreview::getImageWrapper).toList());

                    success = true;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            event.setDropCompleted(success);
        }
    };


    private void setUpComboBox() {
        comboTechnicians.getSelectionModel().selectedItemProperty().addListener(technicianListenerNotEditing);
        comboTechnicians.setConverter(new StringConverter<>() {
            @Override
            public String toString(User object) {
                if (object != null) {
                    if (isEditing.get()) {
                        return object.getAssignation(object.getAssignedDocuments().get(documentToEdit.getDocumentID()))
                                + " " + object.getFullName() + " (" + object.getUsername() + ")";
                    } else {
                        if (technicians.contains(object))
                            return "ASSIGNED: " + object.getFullName() + " (" + object.getUsername() + ")";
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
        pdfGenerator.generatePdf(documentToEdit);
    }

    private void isInputChanged(Document document){
        // Check if job information has changed
        if (!txtJobTitle.getText().trim().equals(document.getJobTitle())) {
            isInputChanged.setValue(true);
            return;
        } if (!txtJobDescription.getText().trim().equals(document.getJobDescription())) {
            isInputChanged.setValue(true);
            return;
        } if (!txtNotes.getText().trim().equals(document.getOptionalNotes())) {
            isInputChanged.setValue(true);
            return;
        }

        // Check if customer information has changed
        if (!txtName.getText().trim().equals(document.getCustomer().getCustomerName())) {
            isInputChanged.setValue(true);
            return;
        } if (!txtPhoneNumber.getText().trim().equals(document.getCustomer().getCustomerPhoneNumber())) {
            isInputChanged.setValue(true);
            return;
        } if (!txtEmail.getText().trim().equals(document.getCustomer().getCustomerEmail())) {
            isInputChanged.setValue(true);
            return;
        } if (!toggleCustomerType.isSelected() == document.getCustomer().getCustomerType().equals(CustomerType.PRIVATE)) {
            isInputChanged.setValue(true);
            return;
        }

        // Address
        Address customerAddress = document.getCustomer().getCustomerAddress();
        Address address = new Address(txtStreetName.getText().trim(), txtHouseNumber.getText().trim(),
                txtPostcode.getText().trim(), txtCity.getText().trim(), txtCountry.getText().trim());
        if (!customerAddress.equals(address)) {
            isInputChanged.setValue(true);
            return;
        }

        // Other information
        if (!Date.valueOf(dateLastContract.getValue()).equals(document.getCustomer().getLastContract())) {
            isInputChanged.setValue(true);
            return;
        } if (!pictures.equals(document.getDocumentImages())) {
            isInputChanged.setValue(true);
            return;
        }

        // Check if technician assignation has changed
        if (!technicians.equals(document.getTechnicians())){
            isInputChanged.setValue(true);
        }
    }

    private void populateComboBox() {
        allTechnicians.clear();
        allTechnicians.setAll(UserModel.getInstance().getAll().values().stream().filter(user ->
                user.getUserRole() == UserRole.TECHNICIAN).collect(Collectors.toList()));
        comboTechnicians.setItems(allTechnicians);
    }

    private void setUpContextMenu() {
        //TODO deleting pictures
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
        Platform.runLater(() -> flowPanePictures.getScene().setOnContextMenuRequested(
                event -> contextMenu.show(flowPanePictures, event.getScreenX(), event.getScreenY())));
    }

    private void setUpPdfListView() {
        DocumentPropertiesList propertiesList = new DocumentPropertiesList(documentToEdit);

        propertiesList.prefWidthProperty().bind(gridPanePdf.widthProperty());
        propertiesList.prefHeightProperty().bind(gridPanePdf.heightProperty().subtract(btnCreatePdf.heightProperty()));
        propertiesList.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            // Handle the updated width of propertiesList here
            System.out.println("List: " + newWidth);
            System.out.println("Gridpane: " + gridPanePdf.getWidth());
        });

        gridPanePdf.getChildren().removeIf(node -> node instanceof DocumentPropertiesList);
        gridPanePdf.add(propertiesList, 0, 0);
    }
    // endregion
}
