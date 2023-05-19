package gui.nodes.textControls;

import io.github.palexdev.materialfx.controls.MFXContextMenu;
import io.github.palexdev.materialfx.controls.MFXContextMenuItem;
import io.github.palexdev.materialfx.i18n.I18N;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;


public class TextAreaWithFloatingText extends StackPane {
    //TODO textArea no longer resizes
    // Child nodes
    private TextArea textArea;
    private Label floatingLabel;
    private MFXContextMenu contextMenu;

    // Properties
    private final StringProperty floatingTextProperty = new SimpleStringProperty();
    private final BooleanProperty isFloatingProperty = new SimpleBooleanProperty(false);
    private IntegerProperty fontSizeProperty = new SimpleIntegerProperty(16);
    private IntegerProperty maxTextLengthProperty = new SimpleIntegerProperty(Integer.MAX_VALUE);

    public TextAreaWithFloatingText() {
        this("");
    }

    public TextAreaWithFloatingText(String floatingText) {
        super();
        getStylesheets().add("/css/TextAreaWithFloatingText.css");
        getStyleClass().setAll("text-area-with-floating-text");

        // StackPane, textArea and floatingLabel
        setUpNodes(floatingText);

        this.setOnMouseClicked(e -> {
            if (!textArea.isFocused()) {
                textArea.requestFocus();
                textArea.deselect();
                textArea.positionCaret(textArea.getText().length());
                pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
                textArea.selectPositionCaret(textArea.getText().length());
            }
        });

       this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > maxTextLengthProperty.get()) {
                textArea.setText(oldValue);
            }
        });

        createBindings();
        defaultContextMenu();
    }

    private void resizeChildren() {
        StackPane.setMargin(textArea, new Insets(floatingLabel.getHeight() + 5, 0, 0, 0));
    }

    private void setUpNodes(String floatingText) {
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
        floatingTextProperty().set(floatingText);
        if (floatingText == null || floatingText.isEmpty()) {
            isFloatingProperty.set(true);
        }

        // TextArea
        textArea = new TextArea("");
        textArea.setWrapText(true);

        // Label
        floatingLabel = new Label();
        floatingLabel.textProperty().bind(floatingTextProperty);
        floatingLabel.getStyleClass().add("floating-label");

        this.getChildren().addAll(textArea, floatingLabel);
        StackPane.setAlignment(floatingLabel, Pos.TOP_LEFT);
        StackPane.setAlignment(textArea, Pos.CENTER_LEFT);
    }

    private void createBindings() {
        textArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
            this.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), newValue);
        });

        // When the text area is focused or empty, the label should become larger
        isFloatingProperty.bind(Bindings.createBooleanBinding(() -> textArea.isFocused() ||
                        !textArea.getText().isEmpty(),
                textArea.focusedProperty(),
                textArea.textProperty()));

        floatingLabel.fontProperty().bind(Bindings
                .when(isFloatingProperty)
                .then(Font.font(14))
                .otherwise(Font.font(16)));

        isFloatingProperty.addListener((observable, oldValue, newValue) -> {
            resizeChildren();
        });

        textArea.maxWidthProperty().bind(this.widthProperty().subtract(5));
    }

    public void defaultContextMenu() {
        MFXContextMenuItem copyItem = MFXContextMenuItem.Builder.build()
                .setIcon(new MFXFontIcon("fas-copy", 14))
                .setText(I18N.getOrDefault("textField.contextMenu.copy"))
                .setAccelerator("Ctrl + C")
                .setOnAction(event -> textArea.copy())
                .get();

        MFXContextMenuItem cutItem = MFXContextMenuItem.Builder.build()
                .setIcon(new MFXFontIcon("fas-scissors", 14))
                .setText(I18N.getOrDefault("textField.contextMenu.cut"))
                .setAccelerator("Ctrl + X")
                .setOnAction(event -> textArea.cut())
                .get();

        MFXContextMenuItem pasteItem = MFXContextMenuItem.Builder.build()
                .setIcon(new MFXFontIcon("fas-paste", 14))
                .setText(I18N.getOrDefault("textField.contextMenu.paste"))
                .setAccelerator("Ctrl + V")
                .setOnAction(event -> textArea.paste())
                .get();

        MFXContextMenuItem deleteItem = MFXContextMenuItem.Builder.build()
                .setIcon(new MFXFontIcon("fas-delete-left", 16))
                .setText(I18N.getOrDefault("textField.contextMenu.delete"))
                .setAccelerator("Ctrl + D")
                .setOnAction(event -> textArea.deleteText(textArea.getSelection()))
                .get();

        MFXContextMenuItem selectAllItem = MFXContextMenuItem.Builder.build()
                .setIcon(new MFXFontIcon("fas-check-double", 16))
                .setText(I18N.getOrDefault("textField.contextMenu.selectAll"))
                .setAccelerator("Ctrl + A")
                .setOnAction(event -> textArea.selectAll())
                .get();

        MFXContextMenuItem redoItem = MFXContextMenuItem.Builder.build()
                .setIcon(new MFXFontIcon("fas-arrow-rotate-right", 12))
                .setText(I18N.getOrDefault("textField.contextMenu.redo"))
                .setAccelerator("Ctrl + Y")
                .setOnAction(event -> textArea.redo())
                .get();
        redoItem.disableProperty().bind(textArea.redoableProperty().not());

        MFXContextMenuItem undoItem = MFXContextMenuItem.Builder.build()
                .setIcon(new MFXFontIcon("fas-arrow-rotate-left", 12))
                .setText(I18N.getOrDefault("textField.contextMenu.undo"))
                .setAccelerator("Ctrl + Z")
                .setOnAction(event -> textArea.undo())
                .get();
        undoItem.disableProperty().bind(textArea.undoableProperty().not());

        contextMenu = MFXContextMenu.Builder.build(textArea)
                .addItems(copyItem, cutItem, pasteItem, deleteItem, selectAllItem)
                .addLineSeparator()
                .addItems(redoItem, undoItem)
                .setPopupStyleableParent(textArea)
                .installAndGet();

        textArea.setOnContextMenuRequested(event -> {
            contextMenu.show(textArea, event.getScreenX(), event.getScreenY());
        });

        setUpShortcuts();
    }

    private void setUpShortcuts() {
        textArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
                contextMenu.hide();
            } else if (event.isControlDown()) {
                switch (event.getCode()) {
                    case C -> textArea.copy();
                    case X -> textArea.cut();
                    case V -> textArea.paste();
                    case D -> textArea.deleteText(textArea.getSelection());
                    case A -> textArea.selectAll();
                    case Y -> textArea.redo();
                    case Z -> textArea.undo();
                }
            }
        });

        textArea.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                contextMenu.hide();
            }
        });
    }

    // region Getters and Setters
    public String getFloatingText() {
        return floatingTextProperty.get();
    }

    public void setFloatingText(String floatingText) {
        this.floatingTextProperty.set(floatingText);
    }

    public StringProperty floatingTextProperty() {
        return floatingTextProperty;
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public void setWrapText(boolean wrapText) {
        textArea.setWrapText(wrapText);
    }

    public StringProperty textProperty() {
        return textArea.textProperty();
    }

    public void setEditable(boolean editable) {
        textArea.setEditable(editable);
    }

    public void setPromptText(String promptText) {
        textArea.setPromptText(promptText);
    }

    public String getPromptText() {
        return textArea.getPromptText();
    }

    public StringProperty promptTextProperty() {
        return textArea.promptTextProperty();
    }

    public IntegerProperty fontSizeProperty() {
        return fontSizeProperty;
    }

    public void setFontSize(int fontSize) {
        fontSizeProperty.set(fontSize);
    }

    public int getFontSize() {
        return fontSizeProperty.get();
    }

    public int maxTextLengthProperty() {
        return maxTextLengthProperty.get();
    }

    public void setMaxTextLength(int maxTextLength) {
        maxTextLengthProperty.set(maxTextLength);
    }

    public int getMaxTextLength() {
        return maxTextLengthProperty.get();
    }
    // endregion
}
