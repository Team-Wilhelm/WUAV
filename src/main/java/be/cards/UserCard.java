package be.cards;

import be.User;
import gui.util.CropImageToCircle;
import gui.util.ImageViewPane;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.lang.reflect.Array;

public class UserCard extends HBox {
    private final int IMAGE_SIZE = 500;
    private User user;
    private int prefWidth = 370;
    private int prefHeight = 210;

    // Child nodes
    private StackPane imageView;
    private Image userIcon;
    private Label nameLabel, usernameLabel, positionLabel;

    public UserCard(User user) {
        super();
        this.user = user;

        this.setPrefWidth(prefWidth);
        this.setPrefHeight(prefHeight);
        this.getStyleClass().addAll("user-card", "rounded");

        VBox left = new VBox();
        left.setPrefWidth((double) prefWidth / 3);
        left.setMaxWidth((double) prefWidth / 3);

        // Picture of the event coordinator
        userIcon = user.getProfilePicture();
        ImageView imageView = new ImageView(userIcon);
        imageView.fitWidthProperty().bind(left.widthProperty());
        imageView.fitHeightProperty().bind(left.widthProperty());
        imageView.setPreserveRatio(true);

        Region[] regions = {new Region(), new Region()};
        for (Region region: regions) {
            region.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
            region.maxWidthProperty().bind(imageView.fitWidthProperty());
            region.maxHeightProperty().bind(left.heightProperty().subtract(imageView.fitHeightProperty().divide(2)));
            VBox.setVgrow(region, Priority.ALWAYS);
        }
        left.getChildren().addAll(regions[0], imageView, regions[1]);

        VBox right = new VBox();
        Line line = new Line();
        line.setStrokeWidth(2);
        line.setStroke(Color.LIGHTGRAY);

        // bind the end X and Y properties to the width and height of the VBox
        line.startXProperty().bind(right.layoutXProperty());
        line.startYProperty().bind(right.layoutYProperty());
        line.endXProperty().bind(right.widthProperty());
        line.endYProperty().bind(right.layoutYProperty());

        // User information
        nameLabel = new Label(user.getFullName());
        positionLabel = new Label(user.getUserRole().toString());
        right.getChildren().addAll(nameLabel, positionLabel, line);

        // Add all the elements to the HBox
        this.setPadding(new Insets(15,15,15,15));
        this.getChildren().addAll(left, right);

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

    public User getUser() {
        return user;
    }
}
