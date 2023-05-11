package gui.nodes;

import be.ImageWrapper;
import be.enums.DocumentPropertyType;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class DocumentPropertyCheckboxWrapper {
    private DocumentPropertyType property;
    private MFXCheckbox checkbox;
    private BooleanProperty selected;
    private ImageWrapper image;

    public DocumentPropertyCheckboxWrapper(DocumentPropertyType property, MFXCheckbox checkbox) {
        super();
        this.property = property;
        this.checkbox = checkbox;
        this.selected = checkbox.selectedProperty();
    }

    public DocumentPropertyCheckboxWrapper(DocumentPropertyType property, MFXCheckbox checkbox, ImageWrapper image) {
        this(property, checkbox);
        this.image = image;
    }

    public DocumentPropertyType getProperty() {
        return property;
    }

    public MFXCheckbox getCheckbox() {
        return checkbox;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public ImageWrapper getImage() {
        return image;
    }
}
