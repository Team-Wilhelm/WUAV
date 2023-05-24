package gui.util;

import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CropImageToCircle {
    /**
     * Crop an image to a circle.
     * @param image The image to crop.
     * @return The cropped image.
     */
    @FXML
    public static Image getRoundedImage(Image image) {
        Circle clip = new Circle(image.getWidth() / 2, image.getHeight() / 2, image.getWidth() / 2);
        ImageView imageView = new ImageView(image);
        imageView.setClip(clip);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        return imageView.snapshot(parameters, null);
    }
}

