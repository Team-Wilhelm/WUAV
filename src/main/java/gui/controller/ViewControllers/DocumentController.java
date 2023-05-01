package gui.controller.ViewControllers;

import be.Document;
import be.cards.DocumentCard;
import gui.SceneManager;
import gui.controller.AddControllers.AddDocumentController;
import gui.model.DocumentModel;
import gui.tasks.TaskState;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Window;
import gui.util.AlertManager;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentController extends ViewController implements Initializable {
    @FXML
    private FlowPane documentFlowPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private MFXProgressSpinner progressSpinner;
    @FXML
    private Label progressLabel;
    @FXML
    private MFXButton btnAddDocument;

    private ObservableList<DocumentCard> documentCards = FXCollections.observableArrayList();
    private final DocumentModel documentModel = DocumentModel.getInstance();
    private DocumentCard lastFocusedCard;

    public DocumentController() {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Bindings.bindContent(documentFlowPane.getChildren(), documentCards);
        documentCards.setAll(documentModel.getCreatedDocumentCards().values());

        documentFlowPane.prefHeightProperty().bind(scrollPane.heightProperty());
        documentFlowPane.prefWidthProperty().bind(scrollPane.widthProperty());

        setProgressVisibility(false);
        refreshItems();
        btnAddDocument.getStyleClass().addAll("addButton", "rounded");
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
        documentCards.clear();

        HashMap<Document, DocumentCard> loadedCards = documentModel.getCreatedDocumentCards();
        for (Document document : (List<Document>) documentsToDisplay) {
            DocumentCard documentCard = loadedCards.get(document);
            if (documentCard == null) {
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
                    editDocument(scrollPane.getScene().getWindow());
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

    public void addDocumentAction(ActionEvent actionEvent) throws IOException {
        ((AddDocumentController) openWindow(SceneManager.ADD_DOCUMENT_SCENE, Modality.APPLICATION_MODAL).getController()).setDocumentController(this);
    }
}
