package gui.controller.ViewControllers;

import be.Document;
import be.cards.DocumentCard;
import gui.SceneManager;
import gui.controller.AddControllers.AddDocumentController;
import gui.model.DocumentModel;
import gui.tasks.TaskState;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import gui.util.AlertManager;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.*;

public class DocumentController extends ViewController implements Initializable {
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

    private ObservableList<DocumentCard> documentCards = FXCollections.observableArrayList();
    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private final DocumentModel documentModel = DocumentModel.getInstance();
    private DocumentCard lastFocusedCard;

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
    public void refreshItems(List<?> documentsToDisplay) {
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
                    //editDocument(btnAddDocument.getScene().getWindow());
                }
            });
            documentCards.add(documentCard);
        }
    }

    @Override
    public void refreshItems() {
        refreshItems(List.copyOf(documentModel.getAll().values()));
    }
//
//    private void editDocument(Window owner) {
//        if (lastFocusedCard != null) {
//            try {
//                AddDocumentController controller = openWindow(SceneManager.ADD_DOCUMENT_SCENE, Modality.APPLICATION_MODAL).getController();
//                controller.setDocumentController(this);
//                controller.setDocumentToEdit(lastFocusedCard.getDocument());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            AlertManager.getInstance().showWarning("No document selected", "Please select a document to edit", owner);
//        }
//    }

    public void addDocumentAction() throws IOException {
        ((AddDocumentController) openWindow(SceneManager.ADD_DOCUMENT_SCENE, Modality.APPLICATION_MODAL).getController()).setDocumentController(this);
    }

    private void populateTableView(){
        documentList.sort(Comparator.comparing(Document::getDateOfCreation).reversed());
        Bindings.bindContentBidirectional(tblDocument.getItems(), documentList);

        MFXTableColumn<Document> jobTitle = new MFXTableColumn<>("Title", false, Comparator.comparing(Document::getJobTitle));
        MFXTableColumn<Document> dateOfCreation = new MFXTableColumn<>("Date of Creation", false, Comparator.comparing(Document::getDateOfCreation));
        MFXTableColumn<Document> customerName = new MFXTableColumn<>("Customer Name", false, Comparator.comparing(d -> d.getCustomer().getCustomerName()));
        MFXTableColumn<Document> customerEmail = new MFXTableColumn<>("Customer Email", false, Comparator.comparing(d -> d.getCustomer().getCustomerEmail()));

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

        tblDocument.getTableColumns().addAll(jobTitle, dateOfCreation, customerName, customerEmail);
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
                    controller.setIsEditing(tblDocument.getSelectionModel().getSelectedValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                AlertManager.getInstance().showError("No document selected", "Please select a document", btnAddDocument.getScene().getWindow());
            }
        }
    }
}
