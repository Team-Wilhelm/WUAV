package gui.nodes;

import io.github.palexdev.materialfx.utils.AnimationUtils;
import javafx.animation.Animation;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import javax.swing.text.html.StyleSheet;


public class TextAreaWithFloatingText extends StackPane {
    // Child nodes
    private final TextArea textArea;
    private final Label floatingLabel;

    // Properties
    private final StringProperty floatingTextProperty = new SimpleStringProperty();
    private final BooleanProperty isFloatingProperty = new SimpleBooleanProperty(false);
    private int fontSize;

    //private static final StyleablePropertyFactory<TextAreaWithFloatingText> FACTORY = new StyleablePropertyFactory<>(TextAreaWithFloatingText.getClassCssMetaData());
    //private final StyleableProperty<Font> fontProperty;
    private Border unfocusedBorder, focusedBorder;
    private Animation floatingAnimation;

    public TextAreaWithFloatingText(String floatingText) {
        super();
        getStyleClass().setAll("text-area-with-floating-text");
        getStylesheets().setAll("/css/style.css");

        // StackPane
        setUpStackPane(floatingText);

        // Get font from style.css
        //fontProperty = new SimpleStyleableObjectProperty<>(FACTORY.createFontCssMetaData("-fx-font", s -> s.fontProperty, Font.getDefault(), false));

        // TextArea
        textArea = new TextArea();
        textArea.setWrapText(true);

        // Label
        floatingLabel = new Label();
        floatingLabel.textProperty().bind(floatingTextProperty);
        floatingLabel.getStyleClass().add("floating-label");

        this.getChildren().addAll(textArea, floatingLabel);
        StackPane.setAlignment(floatingLabel, Pos.TOP_LEFT);
        StackPane.setAlignment(textArea, Pos.CENTER_LEFT);

        this.setOnMouseClicked(e -> {
            if (!this.isFocused()) {
                textArea.requestFocus();
                textArea.positionCaret(textArea.getText().length());
            }
        });
        createBindings();

        isFloatingProperty.addListener((observable, oldValue, newValue) -> {
            resizeChildren();
        });
    }

    private void resizeChildren() {
        StackPane.setMargin(textArea, new Insets(floatingLabel.getHeight() + 5, 0, 0, 0));
    }

    private void setUpStackPane(String floatingText) {
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
        floatingTextProperty().set(floatingText);
        if (floatingText == null || floatingText.isEmpty()) {
            isFloatingProperty.set(true);
        }

        // Border
        unfocusedBorder = new Border(
                new BorderStroke(Color.LIGHTGRAY,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(0.5),
                        new BorderWidths(1.5)));
        focusedBorder = new Border(
                new BorderStroke
                (Color.valueOf("0e283f"),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(0.5),
                        new BorderWidths(1.5)));
    }

    private void createBindings() {
        // Border color
        this.borderProperty().bind(Bindings
                .when(textArea.focusedProperty())
                .then(focusedBorder)
                .otherwise(unfocusedBorder));

        // When the text area is focused or empty, the label should become larger
        isFloatingProperty.bind(Bindings.createBooleanBinding(() -> textArea.isFocused()
                        || !textArea.getText().isEmpty(),
                textArea.focusedProperty(),
                textArea.textProperty()));

        floatingLabel.fontProperty().bind(Bindings
                .when(isFloatingProperty)
                .then(Font.font(14))
                .otherwise(Font.font(16)));

        textArea.maxWidthProperty().bind(this.widthProperty().subtract(5));
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
    // endregion

}
