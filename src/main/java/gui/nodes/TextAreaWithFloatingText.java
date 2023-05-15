package gui.nodes;

import io.github.palexdev.materialfx.beans.PositionBean;
import io.github.palexdev.materialfx.utils.PositionUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.w3c.dom.css.RGBColor;


public class TextAreaWithFloatingText extends StackPane {
    private final TextArea textArea;
    private final Label floatingLabel;
    private final StringProperty floatingTextProperty = new SimpleStringProperty();
    private final BooleanProperty isFloatingProperty = new SimpleBooleanProperty(false);

    public TextAreaWithFloatingText(String floatingText) {
        super();
        getStyleClass().add("text-area-with-floating-text");

        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
        floatingTextProperty().set(floatingText);
        if (floatingText == null || floatingText.isEmpty()) {
            isFloatingProperty.set(true);
        }

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

        textArea.prefWidthProperty().bind(this.widthProperty());
        textArea.maxHeightProperty().bind(this.heightProperty().subtract(floatingLabel.heightProperty()));

        this.setOnMouseClicked(e -> {
            if (!this.isFocused()) {
                textArea.requestFocus();
            }
        });

        this.borderProperty().bind(Bindings
                .when(textArea.focusedProperty())
                .then(new Border(new BorderStroke(Color.valueOf("0e283f"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))))
                .otherwise(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2)))));

        // When the text area is focused or empty, the label should float above the text area
        isFloatingProperty.bind(Bindings.createBooleanBinding(() -> textArea.isFocused()
                || !textArea.getText().isEmpty(),
                textArea.focusedProperty(),
                textArea.textProperty()));

        floatingLabel.fontProperty().bind(Bindings
                .when(isFloatingProperty)
                .then(Font.font(14))
                .otherwise(Font.font(16)));

        isFloatingProperty.addListener((observable, oldValue, newValue) -> {
            updateFloatingLabel();
        });
    }

    private void updateFloatingLabel() {
        if (isFloatingProperty.get()) {
            StackPane.setMargin(textArea, new Insets(floatingLabel.getHeight() + 4, 0, 0, 0));
        } else {
            StackPane.setMargin(textArea, Insets.EMPTY);
        }
    }

    private void setUpStackPane() {

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
