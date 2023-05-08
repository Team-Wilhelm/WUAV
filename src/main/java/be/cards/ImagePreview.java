package be.cards;

import be.ImageWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ImagePreview extends VBox {
    private ImageWrapper imageWrapper;
    private Label fileName;
    private ImageView imageView;

    public ImagePreview(ImageWrapper imageWrapper) {
        super(10);
        this.imageWrapper = imageWrapper;
        this.setPrefWidth(150);
        this.setPrefHeight(100);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().addAll("document-view", "rounded");

        fileName = new Label(imageWrapper.getName());
        imageView = new ImageView(imageWrapper.getImage());
        imageView.setFitWidth(150);
        imageView.setFitHeight(100);

        this.getChildren().addAll(imageView, fileName);
    }

    public ImageWrapper getImageWrapper() {
        return imageWrapper;
    }
}
