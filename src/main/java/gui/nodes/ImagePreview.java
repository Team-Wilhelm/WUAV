package gui.nodes;

import be.ImageWrapper;
import be.interfaces.Observer;
import gui.util.DialogManager;
import gui.util.ImageByteConverter;
import io.github.palexdev.materialfx.controls.MFXContextMenu;
import io.github.palexdev.materialfx.controls.MFXContextMenuItem;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.concurrent.CompletableFuture;

public class ImagePreview extends VBox {
    private Observer<ImagePreview> observer;
    private ImagePreviewObservable observable;
    private ImageWrapper imageWrapper;
    private Label fileName;
    private ImageView imageView;
    private Tooltip tooltip;
    private MFXContextMenu contextMenu;
    private EventHandler<ActionEvent> onDeleteAction, onAddDescriptionAction;
    private MFXContextMenuItem deleteItem, addDescriptionItem, seeDescriptionItem;

    public ImagePreview(ImageWrapper imageWrapper) {
        super(10);
        this.imageWrapper = imageWrapper;
        this.observable = new ImagePreviewObservable(this);

        this.setPrefWidth(150);
        this.setPrefHeight(100);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().addAll("document-view", "rounded");

        fileName = new Label(imageWrapper.getName());
        imageView = ImageByteConverter.getImageViewFromBytes(imageWrapper.getImageBytes());
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

        tooltip = new Tooltip("Right click for more options");
        Tooltip.install(this, tooltip);
        setUpContextMenu();
    }

    private void setUpContextMenu() {
        contextMenu = new MFXContextMenu(this);
        contextMenu.setHideOnEscape(true);
        contextMenu.setAutoHide(true);

        deleteItem = MFXContextMenuItem.Builder.build()
                .setText("Delete")
                .setAccelerator("Delete")
                .setIcon(new MFXFontIcon("fas-delete-left", 16))
                .get();
        addDescriptionItem = MFXContextMenuItem.Builder.build()
                .setText("Add description")
                .setAccelerator("Ctrl + E")
                .setOnAction(event -> {
                    openAddDescriptionDialog();
                })
                .get();

        contextMenu.getItems().addAll(deleteItem, addDescriptionItem);
        contextMenu.minWidthProperty().bind(addDescriptionItem.widthProperty().add(10));

        this.setOnMousePressed(e -> {
            if (e.isSecondaryButtonDown()) {
                contextMenu.show(this, e.getScreenX(), e.getScreenY());
            }
        });

        this.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode().equals(KeyCode.E)) {
                openAddDescriptionDialog();
            }
        });
    }

    public void openAddDescriptionDialog() {
        CompletableFuture<String> result = DialogManager.getInstance().showTextInputDialog("Add description",
                "Add a description to the image", imageWrapper.getDescription(), this, true);
        // Get the result of the dialogue and set the description of the image
        result.thenAccept(s -> {
            if (s != null) {
                imageWrapper.setDescription(s);
                notifyObservers(observable, this);
            }
        });
    }

    public void openSeeDescriptionDialog() {
        String description = imageWrapper.getDescription() == null ? "No description available" : imageWrapper.getDescription();
        DialogManager.getInstance().showTextInputDialog("See description",
                "Add a description to the image", description, this, false);
    }

    public void makeContextMenuNotEditable() {
        contextMenu.getItems().clear();
        contextMenu.minWidthProperty().unbind();
        seeDescriptionItem = MFXContextMenuItem.Builder.build()
                .setText("See description")
                .setAccelerator("Ctrl + E")
                .setOnAction(event -> {
                    openSeeDescriptionDialog();
                })
                .get();
        contextMenu.getItems().addAll(seeDescriptionItem);
        contextMenu.minWidthProperty().bind(seeDescriptionItem.widthProperty().add(10));

        this.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode().equals(KeyCode.E)) {
                openSeeDescriptionDialog();
            }
        });
    }

    public void setOnDelete(EventHandler<ActionEvent> onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
        deleteItem.setOnAction(onDeleteAction);
    }

    public void setOnAddDescription(EventHandler<ActionEvent> onAddDescriptionAction) {
        this.onAddDescriptionAction = onAddDescriptionAction;
        addDescriptionItem.setOnAction(onAddDescriptionAction);
    }

    public ImageWrapper getImageWrapper() {
        return imageWrapper;
    }

    // Observer pattern implementation
    public void addObserver(Observer<ImagePreview> o) {
        observer = o;
    }

    public void removeObserver(Observer<ImagePreview> o) {
        observer = null;
    }

    protected void notifyObservers(ImagePreviewObservable observable, ImagePreview arg) {
        observer.update(observable, arg);
    }

    public void openDescriptionDialog(boolean hasAccess) {
        if (hasAccess) {
            openAddDescriptionDialog();
        } else {
            openSeeDescriptionDialog();
        }
    }
}
