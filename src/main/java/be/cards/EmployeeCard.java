package be.cards;

import be.User;
import gui.util.CropImageToCircle;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class EmployeeCard extends VBox {
    private final int IMAGE_SIZE = 500;
    private User employee;

    // Child nodes
    private StackPane imageView;
    private Image userIcon;
    private Label nameLabel, usernameLabel;

    public EmployeeCard(User employee) {
        super();
        this.employee = employee;

        this.setPrefWidth(370);
        this.setPrefHeight(210);
        this.getStyleClass().add("coordinator-view");

        // Picture of the event coordinator
        userIcon = employee.getProfilePicture();
        imageView = CropImageToCircle.getRoundedImage(userIcon, IMAGE_SIZE/2);
        //imageView.setFitWidth(150);
        //imageView.setFitHeight(150);

        // Coordinator information
        nameLabel = new Label(employee.getFullName());
        nameLabel.getStyleClass().add("info-label");

        usernameLabel = new Label(employee.getUsername());
        usernameLabel.getStyleClass().add("info-label");

        // Store all the information in a VBox
        VBox information = new VBox(10);
        information.getStyleClass().add("coordinator-information");
        information.getChildren().addAll(
                new VBox(new Label("Name"), nameLabel),
                new VBox(new Label("Username"), usernameLabel),
                new VBox(new Label("Most recent event")));
        information.setAlignment(Pos.CENTER_LEFT);

        // Add all the elements to the VBox
        this.setPadding(new Insets(15,15,15,15));

        HBox hBox = new HBox(20, imageView, information);
        VBox filler = new VBox();
        VBox.setVgrow(filler, Priority.ALWAYS);
        this.getChildren().addAll(hBox, filler, new Label("ID: " + employee.getUserID()));

        // When vbox is clicked focus on it
        this.setOnMouseClicked(event -> {
            if (!this.isFocused())
                this.requestFocus();
        });

        // Use different backgrounds for focused and unfocused states
        this.backgroundProperty().bind(Bindings
                .when(this.focusedProperty())
                .then(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)))
                .otherwise(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY))));
    }

    public User getEmployee() {
        return employee;
    }
}
