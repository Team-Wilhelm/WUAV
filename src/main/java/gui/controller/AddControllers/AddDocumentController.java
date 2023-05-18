package gui.controller.AddControllers;

import be.*;
import be.interfaces.Observable;
import be.interfaces.Observer;
import utils.enums.CustomerType;
import utils.enums.UserRole;
import bll.pdf.PdfGenerator;
import gui.controller.ViewControllers.DocumentController;
import gui.model.CustomerModel;
import gui.model.DocumentModel;
import gui.model.UserModel;
import gui.tasks.DeleteTask;
import gui.tasks.SaveTask;
import utils.enums.ResultState;
import gui.util.DialogueManager;
import io.github.palexdev.materialfx.controls.*;
import gui.nodes.*;
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
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;
import julia.nodes.MFXTextFieldWithAutofill;
import julia.nodes.TextAreaWithFloatingText;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.ResourceBundle;

public class AddDocumentController extends AddController<Document> implements Initializable, Observer<ImagePreview> {
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab jobInformationTab, customerInformationTab, picturesTab, pdfTab;
    @FXML
    private FlowPane flowPanePictures;
    @FXML
    private GridPane gridPanePdf, gridPaneCustomer, gridPaneJob;
    @FXML
    private MFXButton btnDelete, btnSave, btnUploadPictures, btnCreatePdf, btnNextJobTab, btnNextCustomerTab;
    @FXML
    private MFXFilterComboBox<User> comboTechnicians;
    @FXML
    private MFXTextField txtCity, txtCountry, txtEmail, txtHouseNumber, txtJobTitle, txtPhoneNumber, txtPostcode, txtStreetName;
    @FXML
    private MFXTextFieldWithAutofill txtName;
    @FXML
    private MFXToggleButton toggleCustomerType;
    @FXML
    private MFXDatePicker dateLastContract;
    @FXML
    private TextAreaWithFloatingText txtJobDescription, txtNotes;
    private DocumentPropertiesList propertiesList;

    private DocumentModel documentModel;
    private CustomerModel customerModel;
    private PdfGenerator pdfGenerator;
    private Document documentToEdit, currentDocument;
    private DocumentController documentController;
    private final ObservableList<ImageWrapper> pictures;
    private final ObservableList<ImagePreview> imagePreviews = FXCollections.observableArrayList();
    private DialogueManager dialogueManager;
    private ObservableList<User> allTechnicians;
    private final ThreadPool executorService;
    private boolean hasAccess = false;
    private ImagePreview lastFocused;

    // Document and customer information
    private UUID temporaryId;
    private String city, country, email, houseNumber, name, phoneNumber, postcode, streetName;
    private String jobDescription, jobTitle, notes;
    private CustomerType customerType;
    private Date lastContract;
    private List<User> technicians;
    private BooleanProperty isInputChanged, isEditing;
    private Customer customer;

    public AddDocumentController() {
        documentModel = DocumentModel.getInstance();
        customerModel = CustomerModel.getInstance();
        executorService = ThreadPool.getInstance();
        dialogueManager = DialogueManager.getInstance();
        pdfGenerator = new PdfGenerator();
        technicians = new ArrayList<>();

        temporaryId = UUID.randomUUID();
        pictures = FXCollections.observableArrayList();
        allTechnicians = FXCollections.observableArrayList();

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

        setTxtCustomerNameAutoComplete();
        assignListenersToTextFields();
        setUpComboBox();

        dateLastContract.setValue(LocalDate.now());

        Bindings.bindContent(flowPanePictures.getChildren(), imagePreviews);
        btnSave.disableProperty().bind(isInputChanged.not());
        btnDelete.disableProperty().bind(isEditing.not());

        addTooltips();
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
            if (isEditing.get())
                isInputChanged(documentToEdit);
        }
        refreshItems();
    }

    @FXML
    private void saveAction(ActionEvent actionEvent) {
        assignInputToVariables();

        Address address = new Address(streetName, houseNumber, postcode, city, country);
        if (customer == null) {
            customer = new Customer(name, email, phoneNumber, address, customerType, lastContract);
        } else {
            customer.setCustomerName(name);
            customer.setCustomerEmail(email);
            customer.setCustomerPhoneNumber(phoneNumber);
            customer.setCustomerAddress(address);
            address.setAddressID(customer.getCustomerAddress().getAddressID());
            customer.setCustomerType(customerType);
            customer.setLastContract(lastContract);
        }

        currentDocument = new Document(customer, jobDescription, notes, jobTitle, Date.valueOf(LocalDate.now()));
        currentDocument.setTechnicians(technicians);
        currentDocument.setDocumentImages(pictures);
        technicians.forEach(technician -> technician.addDocument(currentDocument));

        if (isEditing.get()) {
            currentDocument.setDocumentID(documentToEdit.getDocumentID());
        }

        SaveTask<Document> task = new SaveTask<>(currentDocument, isEditing.get(), documentModel);
        setUpSaveTask(task, documentController, gridPanePdf, this);
        executorService.execute(task);
        pdfTab.setDisable(false);
    }

    @FXML
    private void deleteAction(ActionEvent actionEvent) {
        CompletableFuture<ButtonType> result = dialogueManager.showConfirmation("Delete document", "Are you sure you want to delete this document?", gridPaneJob);
        result.thenAccept(r -> {
            if (r.equals(ButtonType.OK)) {
                Task<ResultState> deleteTask = new DeleteTask<>(documentToEdit.getDocumentID(), documentModel);
                setUpDeleteTask(deleteTask, documentController,gridPaneJob);
                executorService.execute(deleteTask);
                closeWindow(actionEvent);
            }
        });
    }

    @FXML
    private void nextAction(ActionEvent actionEvent) {
        tabPane.getSelectionModel().selectNext();
    }

    public void assignUserToDocument(User technician) {
        if (documentToEdit.getTechnicians().contains(UserModel.getLoggedInUser())) {
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
    }

    public void createPdfAction(ActionEvent actionEvent) {
        // Get the selected checkboxes
        List<DocumentPropertyCheckboxWrapper> checkboxWrappers = propertiesList.getCheckBoxes().stream()
                .filter(DocumentPropertyCheckboxWrapper::isSelected)
                .toList();
        pdfGenerator.generatePdf(documentToEdit, checkboxWrappers);
    }

    // region Listeners
    /**
     * Disables switching to the next tab if any of the required job text fields are empty.
     */
    private final ChangeListener<String> jobInputListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            boolean isNotFilled = isInputEmpty(txtJobTitle) || isInputEmpty(txtJobDescription.getTextArea());
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
            if (observable == txtName.textProperty()) {
                if (oldValue.length() > 0 && newValue.length() == 0) {
                    clearCustomerTextFields();
                    customer = null;
                }
            }
        }
    };

    private final ChangeListener<String> customerListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (newValue != null && !newValue.isEmpty()) {
                List<String> names = customerModel.getAll().values().stream().map(c -> addAddressToCustomerName(c))
                        .filter(customerName -> customerName.toLowerCase().contains(newValue.toLowerCase().trim()))
                        .toList();
                txtName.getEntries().clear();
                txtName.getEntries().addAll(names);
            }
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
            } else if (!newValue.equals(UserModel.getLoggedInUser())) {
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
                    CompletableFuture<ButtonType> result = dialogueManager.showConfirmation("Unsaved changes", "You have unsaved changes. Do you want to save them?", flowPanePictures);
                    result.thenAccept(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            saveAction(null);
                        }
                    });
                }
                setUpPdfListView();
            }
        }
    };
    // endregion

    // region Utilities, helpers and setters
    public void setIsEditing(Document document) {
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
        List<ImageWrapper> documentImages = new ArrayList<>(document.getDocumentImages());
        pictures.setAll(documentImages);

        // Technicians
        technicians.clear();
        allTechnicians.stream().filter(technician -> technician.getAssignedDocuments().containsKey(document.getDocumentID())).forEach(technician -> {
            technicians.add(technician);
        });
        documentToEdit.setTechnicians(technicians);

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
            imagePreview.addObserver(this);
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
                        file.deleteOnExit(); // Delete the file after closing the image viewer
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // Add delete functionality to the image preview
            imagePreview.setOnKeyPressed(e -> {
                if (e.getCode().equals(KeyCode.DELETE)) {
                    imagePreviews.remove(imagePreview);
                    pictures.remove(image);
                } if (e.isControlDown() && e.getCode().equals(KeyCode.E)) {
                    imagePreview.openDescriptionDialogue(hasAccess);
                }
            });

            // Add drag and drop functionality to the image preview
            imagePreview.setOnDragDetected(dragDetected);
            imagePreview.setOnDragOver(dragOver);
            imagePreview.setOnDragDropped(dragDropped);

            // Set delete button action
            imagePreview.setOnDelete(event -> {
                imagePreviews.remove(imagePreview);
                pictures.remove(image);
                refreshItems();
            });

            imagePreviews.add(imagePreview);
        });
    }

    // region Drag and drop
    /**
     * Event handler for when the user drops an image preview on the flow pane.
     */
    private EventHandler<MouseEvent> dragDetected = new EventHandler<>() {
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
                    if (isEditing.get())
                        isInputChanged(documentToEdit);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            event.setDropCompleted(success);
        }
    };
    // endregion

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
        } if (!(new HashSet<>(technicians).containsAll(document.getTechnicians())
                && new HashSet<>(document.getTechnicians()).containsAll(technicians))) {
            isInputChanged.setValue(true);
        }
    }

    private void populateComboBox() {
        allTechnicians.clear();
        allTechnicians.setAll(UserModel.getInstance().getAll().values().stream().filter(user ->
                user.getUserRole() == UserRole.TECHNICIAN).collect(Collectors.toList()));
        comboTechnicians.setItems(allTechnicians);
    }

    public void setUpPdfListView() {
        propertiesList = new DocumentPropertiesList(documentToEdit);
        gridPanePdf.getChildren().removeIf(node -> node instanceof DocumentPropertiesList);
        gridPanePdf.add(propertiesList, 0, 0);
    }

    private void clearCustomerTextFields() {
        txtName.clear();
        txtEmail.clear();
        txtPhoneNumber.clear();
        txtStreetName.clear();
        txtHouseNumber.clear();
        txtCity.clear();
        txtPostcode.clear();
        txtCountry.clear();
        dateLastContract.setValue(LocalDate.now());
    }

    private void addTooltips() {
        Tooltip flowPaneTooltip = new Tooltip("Drag and drop pictures to reorder them.");
        flowPaneTooltip.setShowDelay(Duration.millis(1000));
        flowPaneTooltip.setShowDuration(Duration.millis(2000));
        Tooltip.install(flowPanePictures, flowPaneTooltip);
    }

    private String addAddressToCustomerName(Customer customer) {
        return customer.getCustomerName()
                + " (" + customer.getCustomerAddress().getStreetName() + " "
                + customer.getCustomerAddress().getStreetNumber() + ")";
    }

    private void setTxtCustomerNameAutoComplete() {
        txtName.textProperty().addListener(customerInputListener);
        txtName.textProperty().addListener(customerListener);

        txtName.setSelectionCallback(selectedSuggestion -> {
            if (txtName.getText() != null) {
                String trimmedSuggestion = selectedSuggestion.substring(0, selectedSuggestion.lastIndexOf("(")).trim();
                customer = customerModel.getByName(trimmedSuggestion);

                if (customer != null && addAddressToCustomerName(customer).equals(selectedSuggestion)) {
                    txtName.setText(trimmedSuggestion);
                    txtName.positionCaret(txtName.getText().length());
                    txtEmail.setText(customer.getCustomerEmail());
                    txtPhoneNumber.setText(customer.getCustomerPhoneNumber());
                    txtStreetName.setText(customer.getCustomerAddress().getStreetName());
                    txtHouseNumber.setText(customer.getCustomerAddress().getStreetNumber());
                    txtPostcode.setText(customer.getCustomerAddress().getPostcode());
                    txtCity.setText(customer.getCustomerAddress().getTown());
                    txtCountry.setText(customer.getCustomerAddress().getCountry());
                    toggleCustomerType.setSelected(customer.getCustomerType() == CustomerType.PRIVATE);
                    dateLastContract.setValue(customer.getLastContract().toLocalDate());}
            }
        });
    }

    @Override
    public void update(Observable<ImagePreview> o, ImagePreview arg) {
        isInputChanged.setValue(true);
    }

    public void setVisibilityForUserRole() {
        UserRole loggedInUserRole = UserModel.getLoggedInUser().getUserRole();
        if (!isEditing.get() && loggedInUserRole == UserRole.TECHNICIAN){
            technicians.add(UserModel.getLoggedInUser());
        }

        if(loggedInUserRole == UserRole.ADMINISTRATOR
                || loggedInUserRole == UserRole.PROJECT_MANAGER
                || technicians.contains(UserModel.getLoggedInUser())){
            hasAccess = true;
        }
        gridPaneJob.getChildren().stream().filter(node -> node instanceof MFXTextField).forEach(node -> {
            ((MFXTextField) node).setEditable(hasAccess);
        });
        gridPaneCustomer.getChildren().stream().filter(node -> node instanceof MFXTextField).forEach(node -> {
            ((MFXTextField) node).setEditable(hasAccess);
        });
        txtJobDescription.setEditable(hasAccess);
        txtNotes.setEditable(hasAccess);
        btnDelete.setVisible(hasAccess);
        btnUploadPictures.setVisible(hasAccess);
        btnSave.setVisible(hasAccess);
        toggleCustomerType.disableProperty().setValue(!hasAccess);

        if (!hasAccess)
            imagePreviews.forEach(ImagePreview::makeContextMenuNotEditable);
    }
    // endregion

    //TODO add a confirmation dialog
            /*
            (Stage) btnAddDocument.getScene().getWindow().setOnCloseRequest(event -> {
                if (documentModel.isModified()) {
                    AlertManager.showConfirmationAlert("Are you sure you want to exit?", "You have unsaved changes. Are you sure you want to exit?", () -> {
                        documentModel.save();
                        Platform.exit();
                    });
                } else {
                    Platform.exit();
                }
            });
        });

             */
}
