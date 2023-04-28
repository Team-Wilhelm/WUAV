package gui.util;

import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CropImageToCircle {
    @FXML
    public static StackPane getRoundedImage(Image image, int radius) {
        Circle clip = new Circle(Math.min(image.getWidth(), image.getHeight()) / 2);

        // Create imageview with the image and set its properties
        ImageView imageView = new ImageView(image);
        imageView.setClip(clip);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);

        // Create a StackPane to center the ImageView
        StackPane stackPane = new StackPane(imageView);
        stackPane.setPrefSize(200, 200);

        // Set the layoutX and layoutY properties of the ImageView to center it in the StackPane
        imageView.setLayoutX((200 - imageView.getBoundsInParent().getWidth()) / 2.0);
        imageView.setLayoutY((200 - imageView.getBoundsInParent().getHeight()) / 2.0);

        //SnapshotParameters parameters = new SnapshotParameters();
        //parameters.setFill(Color.TRANSPARENT);
        //return imageView.snapshot(parameters, null);
        return stackPane;
    }
}

