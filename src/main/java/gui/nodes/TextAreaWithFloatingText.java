package gui.nodes;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

public class TextAreaWithFloatingText extends VBox {
    private final Label floatingLabel;
    private final TextArea textArea;
    private Scale scale;
    private final StringProperty floatingTextProperty = new SimpleStringProperty();

    public TextAreaWithFloatingText(String floatingText) {
        super();

        scale = Transform.scale(0.85, 0.85, 0, 0);

        // TextArea
        textArea = new TextArea();
        floatingTextProperty().set(floatingText);

        // Label
        floatingLabel = new Label();
        floatingLabel.textProperty().bind(floatingTextProperty);
        floatingLabel.setLabelFor(textArea);
        floatingLabel.getStyleClass().add("floating-label");

        // Bind the label position
        floatingLabel.translateYProperty().bind(Bindings.createDoubleBinding(() -> {
            return textArea.getLayoutBounds().getMinY() - floatingLabel.getLayoutBounds().getHeight() - 5;
        }, textArea.layoutBoundsProperty(), floatingLabel.layoutBoundsProperty()));

        // Set alignment and spacing
        setAlignment(Pos.TOP_LEFT);
        setSpacing(5);

        this.getChildren().addAll(floatingLabel, textArea);
        setStyle("-fx-background-color: white;");
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

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        textArea.setText(text);
    }
}
