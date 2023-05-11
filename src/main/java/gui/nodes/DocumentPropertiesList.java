package gui.nodes;

import be.Document;
import be.ImageWrapper;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;

public class DocumentPropertiesList extends GridPane {
    private Document document;
    private ColumnConstraints colLabel, colValue, colCheckbox;
    private MFXToggleButton toggleAll;
    private HashMap<String, CheckBox> checkBoxes;

    public DocumentPropertiesList(Document document) {
        super();
        this.document = document;

        setHgap(10);
        setVgap(10);
        setPadding(new Insets(10));

        checkBoxes = new HashMap<>();

        // Column constraints
        colLabel = new ColumnConstraints();
        colLabel.setHgrow(Priority.SOMETIMES);

        colValue = new ColumnConstraints();
        colValue.setHgrow(Priority.ALWAYS);

        colCheckbox = new ColumnConstraints();
        colCheckbox.setHgrow(Priority.SOMETIMES);
        colCheckbox.setHalignment(HPos.RIGHT);

        getColumnConstraints().addAll(colLabel, colValue, colCheckbox);

        // Toggle all checkbox
        toggleAll = new MFXToggleButton("Select all");
        toggleAll.setTextAlignment(TextAlignment.LEFT);
        toggleAll.setOnAction(event -> {
            for (CheckBox checkBox : checkBoxes.values()) {
                checkBox.setSelected(toggleAll.isSelected());
            }
        });
        add(toggleAll, 2, 0);

        displayProperties();
    }

    private void displayProperties() {
        //TODO throws exception when document is null
        int row = 1;

        Date date = document.getDateOfCreation() != null ? document.getDateOfCreation() : Date.valueOf(LocalDate.now());
        addProperty("Date of creation: ", date.toString(), row++);
        addProperty("Job title: ", document.getJobTitle(), row++);
        addProperty("Job description: ", document.getJobDescription(), row++);
        addProperty("Notes: ", document.getOptionalNotes(), row++);
        addProperty("Technicians: ", document.getTechnicianNames(), row++);

        for (ImageWrapper imageWrapper : document.getDocumentImages()) {
            addProperty("Image: " + imageWrapper.getName(), imageWrapper, row++);
        }
    }

    private void addProperty(String label, String value, int row) {
        add(new Label(label), 0, row);
        add(new Label(value), 1, row);
        MFXCheckbox checkBox = new MFXCheckbox();
        checkBoxes.put(label, checkBox);
        add(checkBox, 2, row);
    }

    private void addProperty(String label, ImageWrapper imageWrapper, int row) {
        ImageView imageView = new ImageView(imageWrapper.getImage());
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);

        add(new Label(label), 0, row);
        add(imageView, 1, row);
        MFXCheckbox checkBox = new MFXCheckbox();
        checkBoxes.put(label, checkBox);
        add(checkBox, 2, row);
    }

    public HashMap<String, CheckBox> getCheckBoxes() {
        return checkBoxes;
    }
}
