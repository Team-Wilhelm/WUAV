package gui.controller.ViewControllers;

import be.Customer;
import be.Document;
import be.cards.DocumentCard;
import gui.SceneManager;
import gui.controller.AddControllers.AddDocumentController;
import gui.model.DocumentModel;
import gui.tasks.TaskState;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.enums.SortState;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Window;
import gui.util.AlertManager;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentController extends ViewController implements Initializable {
    @FXML
    private MFXTableView documentTableView;
    @FXML
    private MFXProgressSpinner progressSpinner;
    @FXML
    private Label progressLabel;
    @FXML
    private MFXButton btnAddDocument;
    @FXML
    private MFXTextField searchBar;

    private ObservableList<DocumentCard> documentCards = FXCollections.observableArrayList();
    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private final DocumentModel documentModel = DocumentModel.getInstance();
    private DocumentCard lastFocusedCard;

    public DocumentController() {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setProgressVisibility(false);

//        searchBar.textProperty().addListener((observable, oldValue, newValue) ->
//                refreshItems(documentModel.searchDocuments(searchBar.getText().toLowerCase().trim())));
        searchBar.textProperty().addListener((observable, oldValue, newValue) ->
                documentTableView.getItems().setAll(documentModel.searchDocuments(searchBar.getText().toLowerCase().trim())));

        documentList.addAll(documentModel.getAll().values());
        populateTableView();

        btnAddDocument.getStyleClass().addAll("addButton", "rounded");
        btnAddDocument.setText("");
    }

    //region progress methods
    @Override
    public void setProgressVisibility(boolean visible) {
        progressSpinner.setVisible(visible);
        progressLabel.setVisible(visible);
    }

    @Override
    public void bindProgressToTask(Task<TaskState> task) {
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
    public void refreshItems(List<?> documentsToDisplay) {
        //TODO refresh all cards with given customer
        // TODO fix this mess
        documentCards.clear();

        HashMap<Document, DocumentCard> loadedCards = documentModel.getCreatedDocumentCards();
        for (Document document : (List<Document>) documentsToDisplay) {
            DocumentCard documentCard = loadedCards.get(document);
            if (documentCard == null) {
                documentCard = new DocumentCard(document);
                documentModel.getCreatedDocumentCards().put(document, documentCard);
            }

            if (lastFocusedCard != null && documentCard.getDocument() == lastFocusedCard.getDocument()) {
                documentCard = new DocumentCard(document);
                documentModel.getCreatedDocumentCards().put(document, documentCard);
                loadedCards.put(document, documentCard);
            }

            final DocumentCard finalDocumentCard = documentCard;
            documentCard.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) lastFocusedCard = finalDocumentCard;
            });

            documentCard.setOnMouseClicked(e -> {
                if (!finalDocumentCard.isFocused())
                    finalDocumentCard.requestFocus();

                if (e.getClickCount() == 2) {
                    lastFocusedCard = finalDocumentCard;
                    editDocument(btnAddDocument.getScene().getWindow());
                }
            });
            documentCards.add(documentCard);
        }
    }

    @Override
    public void refreshItems() {
        refreshItems(List.copyOf(documentModel.getAll().values()));
    }

    private void editDocument(Window owner) {
        if (lastFocusedCard != null) {
            try {
                AddDocumentController controller = openWindow(SceneManager.ADD_DOCUMENT_SCENE, Modality.APPLICATION_MODAL).getController();
                controller.setDocumentController(this);
                controller.setDocumentToEdit(lastFocusedCard.getDocument());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            AlertManager.getInstance().showWarning("No document selected", "Please select a document to edit", owner);
        }
    }

    public void addDocumentAction() throws IOException {
        ((AddDocumentController) openWindow(SceneManager.ADD_DOCUMENT_SCENE, Modality.APPLICATION_MODAL).getController()).setDocumentController(this);
    }

    private void populateTableView(){
        Bindings.bindContentBidirectional(documentTableView.getItems(), documentList);

        MFXTableColumn<Document> jobTitle = new MFXTableColumn<>("Title", false);
        MFXTableColumn<Document> dateOfCreation = new MFXTableColumn<>("Date of Creation", false);
        MFXTableColumn<Document> customerName = new MFXTableColumn<>("Customer Name", false);
        MFXTableColumn<Document> customerEmail = new MFXTableColumn<>("Customer Email", false);

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

        documentTableView.getTableColumns().addAll(jobTitle, dateOfCreation, customerName, customerEmail);
        documentTableView.autosizeColumnsOnInitialization();
    }

    private void tableViewDoubleClickAction(MouseEvent mouseEvent) {
    }
}
