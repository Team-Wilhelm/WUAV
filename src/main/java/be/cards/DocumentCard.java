package be.cards;

import be.Document;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class DocumentCard extends VBox {
    private Document document;
    private Label customerName, dateOfCreation, jobTitle;

    public DocumentCard(Document document){
        super();
        this.document = document;

        this.setPrefWidth(350);
        this.setPrefHeight(200);
        this.getStyleClass().addAll("document-view", "rounded");

        //Create label and populate jobTitleBox
        HBox jobTitleBox = new HBox();
        jobTitleBox.setAlignment(Pos.TOP_LEFT);

        jobTitle = new Label(document.getJobTitle());
        jobTitle.maxWidthProperty().bind(jobTitleBox.prefWidthProperty());
        jobTitle.setWrapText(true);
        jobTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #000000;");

        jobTitleBox.getChildren().add(jobTitle);

        //Create labels and populate box respective boxes
        HBox dateBox = new HBox();
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.setSpacing(10);

        dateOfCreation = new Label(document.getDateOfCreation().toString());
        var imageDate = new ImageView(new Image("/img/material-symbols_calendar-month-outline-rounded.png"));
        imageDate.setFitWidth(20);
        imageDate.setFitHeight(20);

        dateBox.getChildren().addAll( imageDate, dateOfCreation);

        HBox nameBox = new HBox();
        nameBox.setAlignment(Pos.CENTER_LEFT);
        nameBox.setSpacing(10);

        customerName = new Label(document.getCustomer().getCustomerName());
        var imageCustomer = new ImageView(new Image("/img/mdi_clipboard-account-outline.png"));
        imageCustomer.setFitWidth(20);
        imageCustomer.setFitHeight(20);

        nameBox.getChildren().addAll(imageCustomer, customerName);

        VBox image = new VBox();
        image.getStyleClass().add("cardImage");
        image.setPrefWidth(150);
        image.setMaxWidth(150);
        image.setMinWidth(150);

        VBox textContent = new VBox();
        textContent.setPadding(new Insets(10));
        textContent.getChildren().addAll(jobTitleBox, dateBox, nameBox);
        textContent.setSpacing(10);

        HBox card = new HBox();
        card.getChildren().addAll(image, textContent);

        VBox.setVgrow(card, Priority.ALWAYS);

        this.getChildren().addAll(card);
        this.getStyleClass().add("rounded");

        this.setOnMouseClicked(e -> {
            if (!this.isFocused())
                this.requestFocus();
        });
    }

    public Document getDocument() {
        return document;
    }
}
