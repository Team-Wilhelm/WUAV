package be.cards;

import be.Document;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DocumentCard extends VBox {
    private Document document;
    private Label customerName, dateOfCreation, jobTitle;

    private DocumentCard(Document document){
        super();
        this.document = document;

        this.setPrefWidth(200);
        this.setPrefHeight(200);
        this.getStyleClass().add("document-view");

        //Create label and populate jobTitleBox
        HBox jobTitleBox = new HBox();
        jobTitleBox.setAlignment(Pos.TOP_LEFT);

        jobTitle = new Label(document.getJobTitle());
        jobTitle.maxWidthProperty().bind(jobTitleBox.prefWidthProperty());
        jobTitle.setWrapText(true);
        jobTitle.getStyleClass().add("info-label");

        jobTitleBox.getChildren().add(jobTitle);

        //Create labels and populate nameAndDate box
        HBox nameAndDate = new HBox();
        nameAndDate.setAlignment(Pos.BOTTOM_LEFT);

        dateOfCreation = new Label("Created: " + document.getDateOfCreation().toString());
        dateOfCreation.maxWidthProperty().bind(nameAndDate.prefWidthProperty());
        dateOfCreation.getStyleClass().add("label");

        customerName = new Label("Customer: " + document.getCustomer().getCustomerName());
        customerName.maxWidthProperty().bind(nameAndDate.prefWidthProperty());
        customerName.getStyleClass().add("label");

        nameAndDate.getChildren().addAll(dateOfCreation, customerName);

        //Populate document card
        this.getChildren().addAll(jobTitleBox, nameAndDate);
    }

}
