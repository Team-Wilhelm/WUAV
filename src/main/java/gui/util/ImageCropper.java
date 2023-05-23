package gui.util;

import be.User;
import gui.controller.AddControllers.AddUserController;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;

public class ImageCropper {
    private int CROP_WIDTH = 400;
    private int CROP_HEIGHT = 400;
    private int imageWidth, imageHeight;
    private final AddUserController controller;
    private User user;

    // JavaFX components
    private final ImageView imageView;
    private final GridPane gridPane;
    private ColumnConstraints column1, column2;
    private final Group group;
    private final MFXButton cropButton, resetButton, confirmButton;
    private final Stage stage;
    private Rectangle cropRectangle;
    private Image image, croppedImage;
    private String imagePath;

    public ImageCropper(AddUserController controller) {
        this.controller = controller;

        gridPane = new GridPane();
        setUpGridPane();

        setUpCropRectangle();
        imageView = new ImageView();

        // Add the rectangle to a Group along with the image view
        group = new Group();
        group.getChildren().addAll(imageView, cropRectangle);

        // Allow the rectangle to be dragged around the image
        setDragHandlers(cropRectangle, imageView);

        cropButton = new MFXButton("Crop");
        cropButton.setOnAction(event -> finishCrop());

        resetButton = new MFXButton("Reset");
        resetButton.setOnAction(event -> resetCrop());

        confirmButton = new MFXButton("Confirm");
        confirmButton.setOnAction(event -> confirmCrop());

        GridPane.setHalignment(group, HPos.CENTER);
        gridPane.add(group, 0, 0, 2, 1);
        gridPane.add(cropButton, 1, 1);

        stage = new Stage();
    }

    private void confirmCrop() {
        String home = System.getProperty("user.home");
        String path = home + "/Downloads/";
        path = user != null ? (path + user.getUserID() + "_cropped.png") : path + imagePath.substring(
                imagePath.lastIndexOf("\\") + 1, imagePath.lastIndexOf("."))
                + "_cropped"  + ".png";
        File file = new File(path);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(croppedImage, null), "png", file);
            controller.setProfilePicture(croppedImage, file.getAbsolutePath());
            stage.close();
        } catch (Exception e) {
            DialogManager.getInstance().showError("Error", "An error occurred while saving the image.", gridPane);
        }
    }

    private void resetCrop() {
        imageView.setImage(image);
        imageView.setFitWidth(imageWidth);
        imageView.setFitHeight(imageHeight);

        setGridPaneChildren(false);
        stage.setWidth(imageWidth + 100);
        stage.setHeight(imageHeight + cropButton.getHeight() + 70);
        stage.centerOnScreen();
    }

    private void finishCrop() {
        // Calculate the scale factor between the actual image and the displayed image
        double scaleX = image.getWidth() / imageView.getFitWidth();
        double scaleY = image.getHeight() / imageView.getFitHeight();

        // Calculate the crop rectangle's position and size in the original image
        double x = (cropRectangle.getLayoutX() - imageView.getLayoutX()) * scaleX;
        double y = (cropRectangle.getLayoutY() - imageView.getLayoutY()) * scaleY;
        double width = cropRectangle.getWidth() * scaleX;
        double height = cropRectangle.getHeight() * scaleY;

        // Crop the image
        croppedImage = new WritableImage(image.getPixelReader(), (int) x, (int) y, (int) width, (int) height);
        imageView.setFitWidth(CROP_WIDTH);
        imageView.setFitHeight(CROP_HEIGHT);
        imageView.setImage(croppedImage);

        stage.setWidth(CROP_WIDTH + 100);
        stage.setHeight(CROP_HEIGHT + cropButton.getHeight() + 75);
        setGridPaneChildren(true);
        stage.centerOnScreen();
    }

    public void chooseImage(User user) {
        chooseImage();
        this.user = user;
    }

    public void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose profile picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.jpeg"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        // Try to set the initial directory to the user's pictures folder
        try {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Pictures"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Get absolute path to the selected file without the file name
            imagePath  = selectedFile.getAbsolutePath();

            // Calculate the image's display size based on the screen size
            calculateImageDisplaySize(selectedFile);

            Scene scene = new Scene(gridPane, imageWidth + 100, imageHeight + cropButton.getHeight() + 75);
            stage.setScene(scene);
            stage.show();
        }
    }

    private void setUpCropRectangle() {
        cropRectangle = new Rectangle(CROP_WIDTH, CROP_HEIGHT);
        cropRectangle.setFill(null);
        cropRectangle.setStrokeWidth(5);
        cropRectangle.setStroke(Color.GRAY);
        cropRectangle.setStrokeLineCap(StrokeLineCap.ROUND);
        cropRectangle.setStrokeLineJoin(StrokeLineJoin.ROUND);
    }

    private void setDragHandlers(Rectangle rect, ImageView imageView) {
        final double[] offsetX = new double[1];
        final double[] offsetY = new double[1];
        rect.setOnMousePressed(event -> {
            offsetX[0] = event.getSceneX() - rect.getLayoutX();
            offsetY[0] = event.getSceneY() - rect.getLayoutY();
        });
        rect.setOnMouseDragged(event -> {
            double x = event.getSceneX() - offsetX[0];
            double y = event.getSceneY() - offsetY[0];
            double maxX = imageView.getFitWidth() - rect.getWidth();
            double maxY = imageView.getFitHeight() - rect.getHeight();
            x = Math.min(Math.max(0, x), maxX);
            y = Math.min(Math.max(0, y), maxY);
            rect.setLayoutX(x);
            rect.setLayoutY(y);
        });
    }

    private void setGridPaneChildren(boolean isCropped) {
        if (isCropped) {
            group.getChildren().remove(cropRectangle);
            gridPane.getChildren().remove(cropButton);
            gridPane.add(resetButton, 0, 1);
            gridPane.add(confirmButton, 1, 1);
        } else {
            group.getChildren().add(cropRectangle);
            gridPane.getChildren().remove(resetButton);
            gridPane.getChildren().remove(confirmButton);
            gridPane.add(cropButton, 1, 1);
            cropButton.setAlignment(Pos.BOTTOM_RIGHT);
        }
    }

    private void setUpGridPane() {
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25));

        // Create the column constraints
        column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        column1.setHalignment(HPos.LEFT);

        column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        column2.setHalignment(HPos.RIGHT);

        // Add the column constraints to the gridpane
        gridPane.getColumnConstraints().addAll(column1, column2);
    }

    private void calculateImageDisplaySize(File selectedFile) {
        // Get the image's dimensions and scale it down if it's too big
        Image tempImage = new Image(selectedFile.toURI().toString());
        double imageRatio = tempImage.getWidth() / tempImage.getHeight();

        // If the image is too small, set the crop size to the image's dimensions
        if (tempImage.getWidth() < CROP_WIDTH || tempImage.getHeight() < CROP_HEIGHT) {
            CROP_WIDTH = Math.min((int) tempImage.getWidth(), (int) tempImage.getHeight());
            CROP_HEIGHT = CROP_WIDTH;
        }

        double screenHeight = Screen.getPrimary().getBounds().getHeight();

        // If the image is too big, scale it down
        imageHeight = (int) Math.min(screenHeight * 0.75, tempImage.getHeight());
        imageWidth = (int) (imageHeight * imageRatio);
        image = new Image(selectedFile.toURI().toString(), tempImage.getWidth(), tempImage.getHeight(), true, true);
        imageView.setImage(image);
        imageView.setFitWidth(imageWidth);
        imageView.setFitHeight(imageHeight);
    }
}