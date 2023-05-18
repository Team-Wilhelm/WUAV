package gui.controller.ViewControllers;

import be.Document;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.GridPane;
import utils.enums.UserRole;
import gui.model.UserModel;
import gui.util.SceneManager;
import gui.controller.AddControllers.AddDocumentController;
import gui.model.DocumentModel;
import utils.enums.ResultState;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import gui.util.DialogueManager;
import javafx.stage.Window;
import utils.permissions.AccessChecker;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.*;

public class DocumentController extends ViewController<Document> implements Initializable {
    @FXML
    private MFXTableView<Document> tblDocument;
    @FXML
    private MFXProgressSpinner progressSpinner;
    @FXML
    private Label progressLabel;
    @FXML
    private MFXButton btnAddDocument;
    @FXML
    private MFXTextField searchBar;
    @FXML
    private GridPane gridPane;

    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private final DocumentModel documentModel = DocumentModel.getInstance();
    private boolean hasAccess = false;
    private BooleanBinding anyDocumentsLoadingImages;

    public DocumentController() {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setProgressVisibility(false);

        searchBar.textProperty().addListener((observable, oldValue, newValue) ->
                tblDocument.getItems().setAll(documentModel.searchDocuments(searchBar.getText().toLowerCase().trim())));

        refreshItems();
        populateTableView();
        addTooltips();

        btnAddDocument.getStyleClass().addAll("addButton", "rounded");
        btnAddDocument.setText("");

        progressLabel.visibleProperty().bind(progressSpinner.visibleProperty()); // show label when spinner is visible

        checkIfImagesAreLoaded();
    }

    //region progress methods
    @Override
    public void setProgressVisibility(boolean visible) {
        progressSpinner.setVisible(visible);
        progressLabel.setVisible(visible);
    }

    @Override
    public void bindProgressToTask(Task<ResultState> task) {
        progressSpinner.setProgress(0);
        progressSpinner.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());
    }

    @Override
    public void unbindProgress() {
        progressSpinner.progressProperty().unbind();
        progressSpinner.setProgress(100);
        String text = progressLabel.getText();
        progressLabel.textProperty().unbind();
        progressLabel.setText(text);
    }
    //endregion

    @Override
    public void refreshItems(List<Document> documentsToDisplay) {
        documentList.clear();
        documentList.addAll(documentsToDisplay);
    }

    @Override
    public void refreshItems() {
        refreshItems(List.copyOf(documentModel.getAll().values()));
    }

    @FXML
    private void addDocumentAction() {
        AddDocumentController controller;
        controller = openWindow(SceneManager.ADD_DOCUMENT_SCENE, Modality.APPLICATION_MODAL).getController();
        controller.setDocumentController(this);
        controller.setVisibilityForUserRole();
    }

    private Window getWindow() {
        return btnAddDocument.getScene().getWindow();
    }

    private void populateTableView(){
        documentList.sort(Comparator.comparing(Document::getDateOfCreation).reversed());
        Bindings.bindContentBidirectional(tblDocument.getItems(), documentList);

        MFXTableColumn<Document> jobTitle = new MFXTableColumn<>("Title", false, Comparator.comparing(Document::getJobTitle));
        MFXTableColumn<Document> dateOfCreation = new MFXTableColumn<>("Date of Creation", false, Comparator.comparing(Document::getDateOfCreation));
        MFXTableColumn<Document> customerName = new MFXTableColumn<>("Customer Name", false, Comparator.comparing(d -> d.getCustomer().getCustomerName()));
        MFXTableColumn<Document> customerEmail = new MFXTableColumn<>("Customer Email", false, Comparator.comparing(d -> d.getCustomer().getCustomerEmail()));
        MFXTableColumn<Document> myDocument = new MFXTableColumn<>("My Document", false, Comparator.comparing(d -> UserModel.getLoggedInUser().getAssignedDocuments().containsValue(d)));

        jobTitle.setRowCellFactory(document -> {
            MFXTableRowCell<Document, String> row = new MFXTableRowCell<>(Document::getJobTitle);
            row.setOnMouseClicked(this::tableViewDoubleClickAction);
            return row;
        });


        dateOfCreation.setRowCellFactory(document -> {
            MFXTableRowCell<Document, Date> row = new MFXTableRowCell<>(Document::getDateOfCreation);
            row.setOnMouseClicked(this::tableViewDoubleClickAction);
            return row;
        });

        customerName.setRowCellFactory(document -> {
            MFXTableRowCell<Document, String> row = new MFXTableRowCell<>(d -> d.getCustomer().getCustomerName());
            row.setOnMouseClicked(this::tableViewDoubleClickAction);
            return row;
        });

        customerEmail.setRowCellFactory(document -> {
            MFXTableRowCell<Document, String> row = new MFXTableRowCell<>(d -> d.getCustomer().getCustomerEmail());
            row.setOnMouseClicked(this::tableViewDoubleClickAction);
            return row;
        });

        myDocument.setRowCellFactory(document -> {
            MFXTableRowCell<Document, String> row = new MFXTableRowCell<>(d -> UserModel.getLoggedInUser().getAssignedDocuments().containsValue(d) ? "✔" : "");
            row.setOnMouseClicked(this::tableViewDoubleClickAction);
            return row;
        });

        tblDocument.getTableColumns().addAll(jobTitle, dateOfCreation, customerName, customerEmail, myDocument);
        tblDocument.autosizeColumnsOnInitialization();
        tblDocument.setFooterVisible(false);
    }

    @FXML
    private void tableViewDoubleClickAction(MouseEvent event) {
        if (event.getClickCount() == 2) {
            if (!tblDocument.getSelectionModel().getSelection().isEmpty()) {
                AddDocumentController controller = openWindow(SceneManager.ADD_DOCUMENT_SCENE, Modality.APPLICATION_MODAL).getController();
                controller.setDocumentController(this);
                controller.setIsEditing(tblDocument.getSelectionModel().getSelectedValue());
                controller.setVisibilityForUserRole();
            }
            else {
                DialogueManager.getInstance().showError("No document selected", "Please select a document", gridPane);
            }
        }
    }

    private void addTooltips() {
        btnAddDocument.setTooltip(new Tooltip("Press Ctrl+N to add a new document"));
    }

    public void setVisibilityForUserRole() {
        UserRole loggedInUserRole = UserModel.getLoggedInUser().getUserRole();
        if(loggedInUserRole == UserRole.ADMINISTRATOR || loggedInUserRole == UserRole.PROJECT_MANAGER || loggedInUserRole == UserRole.TECHNICIAN){
            hasAccess = true;
        }
        if(!(loggedInUserRole == UserRole.TECHNICIAN)){
            tblDocument.getTableColumns().remove(tblDocument.getTableColumns().size() - 1);
        }
        btnAddDocument.setVisible(hasAccess);
    }

    public void addShortcuts() {
        btnAddDocument.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::addDocumentAction);
    }

    /**
     * Checks if any of the documents are still loading images and shows progress through the progress spinner,
     * to let the user know if the images are still loading, since it's happening asynchronously.
     */
    private void checkIfImagesAreLoaded() {
        anyDocumentsLoadingImages = Bindings.createBooleanBinding(() ->
                documentList.stream().anyMatch(Document::isLoadingImages), documentList);
        progressSpinner.visibleProperty().bind(anyDocumentsLoadingImages);

        documentList.forEach(d -> d.isLoadingImagesProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                progressLabel.setText("Loading images...");
            } else {
                progressSpinner.setProgress(100);
                progressLabel.setText("Images loaded");
                progressSpinner.visibleProperty().unbind();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        progressSpinner.setVisible(false);
                    }
                }, 3000);
            }
        }));
    }
}
