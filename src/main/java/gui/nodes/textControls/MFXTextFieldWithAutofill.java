package gui.nodes.textControls;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MFXTextFieldWithAutofill extends MFXTextField {
    private SortedSet<String> suggestions;
    private ContextMenu contextMenu;
    private Consumer<String> onSuggestionSelected;

    public MFXTextFieldWithAutofill() {
        super();
        suggestions = new TreeSet<>();
        contextMenu = new ContextMenu();
        setContextMenu(contextMenu);

        contextMenu.setMaxHeight(Double.MAX_VALUE);
        setListener();
    }

    private void setListener() {
        //Add "suggestions" by changing text
        textProperty().addListener((observable, oldValue, newValue) -> {
            String enteredText = getText();
            //always hide suggestion if nothing has been entered
            if (enteredText == null || enteredText.isEmpty()) {
                contextMenu.hide();
            } else {
                //filter all possible suggestions depends on "Text", case insensitive
                List<String> filteredEntries = suggestions.stream()
                        .filter(e -> e.toLowerCase().contains(enteredText.toLowerCase()))
                        .collect(Collectors.toList());
                //some suggestions are found
                if (!filteredEntries.isEmpty()) {
                    //build popup - list of "CustomMenuItem"
                    populatePopup(filteredEntries, enteredText);
                    if (!contextMenu.isShowing()) { //optional
                        contextMenu.show(MFXTextFieldWithAutofill.this, Side.BOTTOM, 0, 0); //position of popup
                    }
                    //no suggestions -> hide
                } else {
                    contextMenu.hide();
                }
            }
        });

        //Hide always by focus-in (optional) and out
        focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            contextMenu.hide();
        });
    }

    /**
     * Populate the entry set with the given search results. Display is limited to 10 entries, for performance.
     * @param searchResult The set of matching strings.
     */
    private void populatePopup(List<String> searchResult, String searchRequest) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        int maxEntries = 10;
        int count = Math.min(searchResult.size(), maxEntries);

        for (int i = 0; i < count; i++) {
            final String result = searchResult.get(i);
            //label with graphic (text flow) to highlight founded subtext in suggestions
            Label entryLabel = new Label(result);
            //entryLabel.setGraphic(Styles.buildTextFlow(result, searchRequest));
            entryLabel.setPrefHeight(25);
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            menuItems.add(item);

            //if any suggestion is select set it into text and close popup
            item.setOnAction(actionEvent -> {
                setText(result);
                positionCaret(result.length());
                contextMenu.hide();
                if (onSuggestionSelected != null)
                    onSuggestionSelected.accept(result);
            });
        }

        //"Refresh" context menu
        contextMenu.getItems().clear();
        contextMenu.getItems().addAll(menuItems);
    }


    /**
     * Get the existing set of autocomplete entries.
     *
     * @return The existing autocomplete entries.
     */
    public SortedSet<String> getEntries() { return suggestions; }

    /**
     * Set a callback to be executed when a suggestion is selected.
     *
     * @param callback The callback function to handle the selected suggestion.
     */
    public void setSelectionCallback(Consumer<String> callback) {
        this.onSuggestionSelected = callback;
    }
}

