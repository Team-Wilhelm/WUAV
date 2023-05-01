package gui.util;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ImageCropper extends Application {
    //TODO figure this out

    private ImageView imageView;
    private Rectangle cropRectangle;
    private double cropStartX;
    private double cropStartY;
    private double moveStartX;
    private double moveStartY;

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();

        Button chooseImageButton = new Button("Choose Image");
        chooseImageButton.setOnAction(event -> chooseImage());

        root.setTop(chooseImageButton);

        imageView = new ImageView();
        StackPane imagePane = new StackPane(imageView);

        cropRectangle = new Rectangle(300, 200);
        cropRectangle.setFill(null);
        cropRectangle.setStrokeWidth(2);
        cropRectangle.setStroke(javafx.scene.paint.Color.RED);

        imagePane.getChildren().add(cropRectangle);

        root.setCenter(imagePane);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            imageView.setImage(image);

            cropRectangle.setOnMousePressed(this::startCrop);
            cropRectangle.setOnMouseDragged(this::move);
            cropRectangle.setOnMouseReleased(this::endCrop);
        }
    }

    private void startCrop(javafx.scene.input.MouseEvent event) {
        cropStartX = event.getX();
        cropStartY = event.getY();
    }

    private void resizeCrop(javafx.scene.input.MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        double minX = Math.min(cropStartX, x);
        double minY = Math.min(cropStartY, y);
        double maxX = Math.max(cropStartX, x);
        double maxY = Math.max(cropStartY, y);

        cropRectangle.setX(minX);
        cropRectangle.setY(minY);
        cropRectangle.setWidth(maxX - minX);
        cropRectangle.setHeight(maxY - minY);
    }

    private void endCrop(javafx.scene.input.MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        double minX = Math.min(cropStartX, x);
        double minY = Math.min(cropStartY, y);

        double cropWidth = cropRectangle.getWidth();
        double cropHeight = cropRectangle.getHeight();

        // Create a new image from the cropped area
        Image selectedImage = imageView.getImage();
        double scaleX = selectedImage.getWidth() / imageView.getFitWidth();
        double scaleY = selectedImage.getHeight() / imageView.getFitHeight();

        int croppedWidth = (int) Math.round(cropWidth * scaleX);
        int croppedHeight = (int) Math.round(cropHeight * scaleY);
        int croppedX = (int) Math.round(minX * scaleX);
        int croppedY = (int) Math.round(minY * scaleY);

        Image cropped = new WritableImage(selectedImage.getPixelReader(), croppedX, croppedY, croppedWidth, croppedHeight);
        ImageView croppedImageView = new ImageView(cropped);

        // Display the cropped image in a new window
        Stage croppedStage = new Stage();
        croppedStage.setTitle("Cropped Image");
        croppedStage.setScene(new Scene(new StackPane(croppedImageView), croppedWidth, croppedHeight));
        croppedStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void startMove(MouseEvent event) {
        moveStartX = event.getX();
        moveStartY = event.getY();
    }

    private void move(MouseEvent event) {
        double offsetX = event.getX() - moveStartX;
        double offsetY = event.getY() - moveStartY;
        double newX = cropRectangle.getX() + offsetX;
        double newY = cropRectangle.getY() + offsetY;

        // Keep the rectangle within the bounds of the image
        if (newX < 0) {
            newX = 0;
        }
        if (newY < 0) {
            newY = 0;
        }
        if (newX + cropRectangle.getWidth() > imageView.getBoundsInLocal().getWidth()) {
            newX = imageView.getBoundsInLocal().getWidth() - cropRectangle.getWidth();
        }
        if (newY + cropRectangle.getHeight() > imageView.getBoundsInLocal().getHeight()) {
            newY = imageView.getBoundsInLocal().getHeight() - cropRectangle.getHeight();
        }

        cropRectangle.setX(newX);
        cropRectangle.setY(newY);

        moveStartX = event.getX();
        moveStartY = event.getY();
    }
}