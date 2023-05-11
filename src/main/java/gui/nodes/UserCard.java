package gui.nodes;

import be.User;
import be.interfaces.Observable;
import be.interfaces.Observer;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class UserCard extends VBox {
    private final User user;
    private Label nameLabel, positionLabel;
    private ImageView profileImage;

    public UserCard(User user) {
        super();
        this.user = user;

        // variables
        int width = 200;
        int height = 300;

        this.setPrefWidth(width);
        this.setPrefHeight(height);
        this.getStyleClass().addAll("user-card");

        // Profile image
        profileImage = new ImageView(new Image(user.getProfilePicturePath()));
        profileImage.setFitWidth(width);
        profileImage.setFitHeight(width);

        // Content
        HBox content = new HBox();

        // Text content
        VBox text = new VBox();
        text.setPadding(new Insets(15));
        Line line = new Line();
        line.setStrokeWidth(2);
        line.setStroke(Color.LIGHTGRAY);
        line.setStartX(0);
        line.setEndX(width / 3 * 2);

        content.getChildren().add(text);

        // User information
        nameLabel = new Label(user.getFullName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        positionLabel = new Label(user.getUserRole().toString());
        positionLabel.setPadding(new Insets(5,0,0,0));

        text.getChildren().addAll(nameLabel, line, positionLabel);

        // Add all the elements to the HBox
        this.getChildren().addAll(profileImage, content);

        // When vbox is clicked focus on it
        this.setOnMouseClicked(event -> {
            if (!this.isFocused())
                this.requestFocus();
        });
    }

    public User getUser() {
        return user;
    }

    //TODO look into observer pattern for this
    /*@Override
    public void update(Observable<User> o, User arg) {
        nameLabel.setText(user.getFullName());
        positionLabel.setText(user.getUserRole().toString());
        profileImage.setImage(user.getProfilePicture());
    }*/
}
