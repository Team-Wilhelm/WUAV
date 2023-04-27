package gui.controller;

import be.cards.DocumentCard;
import gui.model.DocumentModel;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.ResourceBundle;

public class DocumentController implements Initializable {
    @FXML
    private FlowPane documentFlowPane;
    @FXML
    private MFXProgressSpinner progressSpinner;
    @FXML
    private Label progressLabel;
    @FXML
    private MFXButton btnAddDocument;
    private ObservableList<DocumentCard> documentCards = FXCollections.observableArrayList();
    private final DocumentModel documentModel = DocumentModel.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setProgressVisibility(false);

        Platform.runLater(()-> {
            documentCards.setAll(documentModel.getCreatedDocumentCards().values());
            Bindings.bindContent(documentFlowPane.getChildren(), documentCards);
        });

        btnAddDocument.getStyleClass().addAll("addButton", "rounded");

    }

    private void setProgressVisibility(boolean visible) {
        progressSpinner.setVisible(visible);
        progressLabel.setVisible(visible);
    }
}
