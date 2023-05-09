package be.cards;

import be.ImageWrapper;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

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

        this.focusTraversableProperty().setValue(true);
        this.setOnMouseClicked(e -> {
            if (!this.isFocused())
                this.requestFocus();
        });

        this.backgroundProperty().bind(Bindings
                .when(this.focusedProperty())
                .then(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, null)))
                .otherwise(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, null))));
    }

    public ImageWrapper getImageWrapper() {
        return imageWrapper;
    }
}
