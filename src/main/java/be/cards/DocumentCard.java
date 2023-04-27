package be.cards;

import be.Document;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DocumentCard extends VBox {
    private Document document;
    private Label customerName, dateOfCreation, jobTitle;

    public DocumentCard(Document document){
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

        //Create labels and populate nameAndDate box
        HBox dateBox = new HBox();
        dateBox.setAlignment(Pos.CENTER_LEFT);

        dateOfCreation = new Label("Created: " + document.getDateOfCreation().toString());
        dateOfCreation.maxWidthProperty().bind(dateBox.prefWidthProperty());


        HBox nameBox = new HBox();
        nameBox.setAlignment(Pos.CENTER_LEFT);

        customerName = new Label("Customer: " + document.getCustomer().getCustomerName());
        customerName.maxWidthProperty().bind(nameBox.prefWidthProperty());


        jobTitleBox.getChildren().add(jobTitle);
        dateBox.getChildren().add(dateOfCreation);
        nameBox.getChildren().add(customerName);

        //Populate document card
        VBox.setVgrow(jobTitleBox, Priority.ALWAYS);
        this.getChildren().addAll(jobTitleBox, dateBox, nameBox);
        this.getStyleClass().add("rounded");
    }

}
