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


public class TextAreaWithFloatingText extends StackPane {
    private final TextArea textArea;
    private final Label floatingLabel;
    private final StringProperty floatingTextProperty = new SimpleStringProperty();

    public TextAreaWithFloatingText(String floatingText) {
        super();
        getStyleClass().add("text-area-with-floating-text");

        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        // TextArea
        textArea = new TextArea();
        floatingTextProperty().set(floatingText);

        // Label
        floatingLabel = new Label();
        floatingLabel.textProperty().bind(floatingTextProperty);
        floatingLabel.getStyleClass().add("floating-label");

        // Bind the text area's height to the stack pane's height minus the label's height
        textArea.prefHeightProperty().bind(this.heightProperty().subtract(floatingLabel.heightProperty()));

        Bindings.createBooleanBinding(() -> !textArea.getText().isEmpty(), textArea.textProperty()).addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                floatingLabel.setFont(Font.font(12));
                //StackPane.setMargin(textArea, new Insets(floatingLabel.getHeight() + 5, 0, 0, 0));
            } else {
                floatingLabel.setFont(Font.font(14));
                //StackPane.setMargin(textArea, new Insets(floatingLabel.getHeight() + 5, 0, 0, 0));
            }
        });

        Bindings.createBooleanBinding(textArea::isFocused, textArea.focusedProperty()).addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                floatingLabel.setFont(Font.font(12));
            } else if (textArea.getText().isEmpty()) {
                floatingLabel.setFont(Font.font(14));
            }
        });

        this.getChildren().addAll(textArea, floatingLabel);
        StackPane.setAlignment(floatingLabel, Pos.TOP_LEFT);
        StackPane.setAlignment(textArea, Pos.CENTER_LEFT);
        //StackPane.setMargin(floatingLabel, new Insets(5, 0, 0, 5));
        //StackPane.setMargin(textArea, new Insets(floatingLabel.getHeight() + 5, 0, 0, 0));

        textArea.prefWidthProperty().bind(this.widthProperty());
        textArea.prefHeightProperty().bind(this.heightProperty());
        //addListener();

        this.setOnMouseClicked(e -> {
            if (!this.isFocused()) {
                textArea.requestFocus();
            }
        });
    }

    private void addListener() {
        textArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { // If text area is focused, make label float
                floatingLabel.setFont(Font.font(12));
            } else if (textArea.getText().isEmpty()) { // If text area is not focused and empty, make label sink
                floatingLabel.setFont(Font.font(14));
            }
        });

        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!textArea.getText().isEmpty()) {
                floatingLabel.setFont(Font.font(12));  // Set smaller font size
            }
        });

        // Update the label's size based on its content
        double labelWidth = computeLabelWidth();
        double labelHeight = computeLabelHeight();
        floatingLabel.resize(labelWidth, labelHeight);
    }

    private double computeLabelWidth() {
        double textWidth = textArea.getWidth();
        double labelWidth = textArea.isFocused() ? floatingLabel.prefWidth(-1) : 0;
        return Math.max(textWidth, labelWidth);
    }

    private double computeLabelHeight() {
        double textHeight = textArea.getHeight();
        double labelHeight = textArea.isFocused() ? floatingLabel.prefHeight(-1) : 0;
        return Math.max(textHeight, labelHeight);
    }

    public String getFloatingText() {
        return floatingTextProperty.get();
    }

    public void setFloatingText(String floatingText) {
        this.floatingTextProperty.set(floatingText);
    }

    public StringProperty floatingTextProperty() {
        return floatingTextProperty;
    }
}
