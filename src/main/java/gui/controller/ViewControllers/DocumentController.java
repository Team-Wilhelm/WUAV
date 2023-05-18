package gui.controller.ViewControllers;

import be.Document;
import javafx.scene.layout.GridPane;
import utils.enums.UserRole;
import gui.model.UserModel;
import gui.nodes.DocumentCard;
import gui.util.SceneManager;
import gui.controller.AddControllers.AddDocumentController;
import gui.model.DocumentModel;
import gui.tasks.TaskState;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.application.Platform;
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

    private ObservableList<DocumentCard> documentCards = FXCollections.observableArrayList();
    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private final DocumentModel documentModel = DocumentModel.getInstance();
    private boolean hasAccess = false;
    private DocumentCard lastFocusedCard;
    private AccessChecker checker = new AccessChecker();

    public DocumentController() {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setProgressVisibility(false);

        searchBar.textProperty().addListener((observable, oldValue, newValue) ->
                tblDocument.getItems().setAll(documentModel.searchDocuments(searchBar.getText().toLowerCase().trim())));

        documentList.addAll(documentModel.getAll().values());
        populateTableView();

        btnAddDocument.getStyleClass().addAll("addButton", "rounded");
        btnAddDocument.setText("");

        addTooltips();
        //TODO
        Platform.runLater(() -> {
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
        });
    }

    //region progress methods
    @Override
    public void setProgressVisibility(boolean visible) {
        progressSpinner.setVisible(visible);
        progressLabel.setVisible(visible);
    }

    @Override
    public void bindProgressToTask(Task<TaskState> task) {
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
    public void refreshLastFocusedCard() {
        if (lastFocusedCard != null) {
            //TODO: refresh last focused card
        }
    }

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
        try {
            controller = openWindow(SceneManager.ADD_DOCUMENT_SCENE, Modality.APPLICATION_MODAL).getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                try {
                    AddDocumentController controller = openWindow(SceneManager.ADD_DOCUMENT_SCENE, Modality.APPLICATION_MODAL).getController();
                    controller.setDocumentController(this);
                    controller.setIsEditing(tblDocument.getSelectionModel().getSelectedValue());;
                    controller.setVisibilityForUserRole();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        btnAddDocument.setVisible(hasAccess);
        //TODO make gridpane take all available space
    }

    public void addShortcuts() {
        btnAddDocument.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::addDocumentAction);
    }
}
