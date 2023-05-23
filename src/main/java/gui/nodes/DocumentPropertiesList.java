package gui.nodes;

import be.Document;
import be.ImageWrapper;
import utils.enums.DocumentPropertyType;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocumentPropertiesList extends GridPane {
    private Document document;
    private ColumnConstraints colLabel, colValue, colCheckbox;
    private MFXToggleButton toggleAll;
    private List<DocumentPropertyCheckboxWrapper> checkBoxes;

    public DocumentPropertiesList() {
        this(new Document());
    }

    public DocumentPropertiesList(Document document) {
        super();
        this.document = document;

        setHgap(10);
        setVgap(10);
        setPadding(new Insets(10));

        checkBoxes = new ArrayList<>();

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
            for (DocumentPropertyCheckboxWrapper checkBox : checkBoxes) {
                checkBox.getCheckbox().setSelected(toggleAll.isSelected());
            }
        });
        add(toggleAll, 2, 0);

        displayProperties();
        toggleAll.fire();
    }

    private void displayProperties() {
        try {
            int row = 1;

            // Necessary properties
            Date date = document.getDateOfCreation() != null ? document.getDateOfCreation() : Date.valueOf(LocalDate.now());
            addProperty(DocumentPropertyType.DATE_OF_CREATION.toString(), date.toString(), row++);
            addProperty(DocumentPropertyType.JOB_TITLE.toString(), document.getJobTitle(), row++);

            int characters = Math.min(document.getJobDescription().length(), 50);
            String jobDescription = document.getJobDescription().length() > 50 ?
                    document.getJobDescription().substring(0, characters) + "..." : document.getJobDescription();
            addProperty(DocumentPropertyType.JOB_DESCRIPTION.toString(), jobDescription, row++);

            // Optional properties
            if (document.getOptionalNotes() != null && !document.getOptionalNotes().isEmpty())
                addProperty(DocumentPropertyType.NOTES.toString(), document.getOptionalNotes(), row++);

            for (ImageWrapper imageWrapper : document.getDocumentImages()) {
                addProperty(DocumentPropertyType.IMAGE.toString(), imageWrapper, row++);
            }

            String technicianNames = document.getTechnicianNames().substring(document.getTechnicianNames().indexOf(":") + 1); // (Technicians:) Name 1, Name 2, Name 3
            if (!technicianNames.isEmpty()) {
                addProperty(DocumentPropertyType.TECHNICIANS.toString(), document.getTechnicianNames(), row++);
            }
        } catch (Exception e) {
        }
    }

    private void addProperty(String label, String value, int row) {
        add(new Label(label), 0, row);
        add(new Label(value), 1, row);
        MFXCheckbox checkBox = new MFXCheckbox();
        checkBoxes.add(new DocumentPropertyCheckboxWrapper(DocumentPropertyType.fromString(label), checkBox));
        add(checkBox, 2, row);
    }

    private void addProperty(String label, ImageWrapper imageWrapper, int row) {
        ImageView imageView = new ImageView(imageWrapper.getImage());
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);

        add(new Label(label), 0, row);
        HBox hBox = new HBox(new HBox(new Label(imageWrapper.getName()), imageView));
        hBox.setAlignment(Pos.CENTER_LEFT);
        add(hBox, 1, row);
        MFXCheckbox checkBox = new MFXCheckbox();
        checkBoxes.add(new DocumentPropertyCheckboxWrapper(DocumentPropertyType.fromString(label), checkBox, imageWrapper));
        add(checkBox, 2, row);
    }

    public List<DocumentPropertyCheckboxWrapper> getCheckBoxes() {
        return checkBoxes;
    }
}
