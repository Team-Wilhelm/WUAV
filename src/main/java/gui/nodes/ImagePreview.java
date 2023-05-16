package gui.nodes;

import be.ImageWrapper;
import be.interfaces.Observer;
import io.github.palexdev.materialfx.controls.MFXContextMenu;
import io.github.palexdev.materialfx.controls.MFXContextMenuItem;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Optional;

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
                    openAddDescriptionDialogue();
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
                openAddDescriptionDialogue();
            }
        });
    }

    public void openAddDescriptionDialogue() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add description");
        dialog.setHeaderText("Add a description to the image");
        dialog.setContentText("Description:");
        dialog.getEditor().setText(imageWrapper.getDescription());
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(imageWrapper::setDescription);
        notifyObservers(observable, this);
    }

    public void openSeeDescriptionDialogue() {
        Dialog<String> dialog = new Dialog<>();
        ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(type);
        dialog.setTitle("See description");
        dialog.setHeaderText("See the description of the image");
        String description = imageWrapper.getDescription() == null ? "No description" : imageWrapper.getDescription();
        dialog.setContentText("Description:\n" + description);
        dialog.showAndWait();
    }

    public void makeContextMenuNotEditable() {
        contextMenu.getItems().clear();
        contextMenu.minWidthProperty().unbind();
        seeDescriptionItem = MFXContextMenuItem.Builder.build()
                .setText("See description")
                .setAccelerator("Ctrl + E")
                .setOnAction(event -> {
                    openSeeDescriptionDialogue();
                })
                .get();
        contextMenu.getItems().addAll(seeDescriptionItem);
        contextMenu.minWidthProperty().bind(seeDescriptionItem.widthProperty().add(10));

        this.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode().equals(KeyCode.E)) {
                openSeeDescriptionDialogue();
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

    public void openDescriptionDialogue(boolean hasAccess) {
        if (hasAccess) {
            openAddDescriptionDialogue();
        } else {
            openSeeDescriptionDialogue();
        }
    }
}
