package gui.util.drawing;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * The toolbar that holds drawing tools and color picker.
 */
public class MyToolBar extends VBox {

    /**
     * Default selected color.
     */
    public static final Color DEFAULT_COLOR = Color.BLUE;

    /**
     * The canvas to draw on.
     */
    private final CanvasPane canvas;

    /**
     * Color picker.
     */
    private final ColorPicker colorPicker;

    /**
     * Group of radio buttons.
     */
    private final ToggleGroup group;

    /**
     * The current selected tool.
     */
    private Tool selectedTool;

    /**
     * Constructs a toolbar.
     * @param canvas the canvas to draw
     */
    public MyToolBar(CanvasPane canvas) {
        this.canvas = canvas;
        group = new ToggleGroup();
        colorPicker = new ColorPicker(DEFAULT_COLOR);
        setupItems();
        setupHandlers();
    }

    /**
     * Sets up ui controls.
     */
    private void setupItems() {
        final MFXRectangleToggleNode selectButton = new MFXRectangleToggleNode();
            var selectImage = new ImageView("file:src/main/resources/icons/select.png");
            selectImage.setFitHeight(40); selectImage.setFitWidth(40); selectButton.setGraphic(selectImage);

        selectButton.setToggleGroup(group);

        HBox handleBox = new HBox();
        handleBox.getChildren().addAll(selectButton);


        final MFXRectangleToggleNode circleButton = new MFXRectangleToggleNode();
            var circleImage = new ImageView("file:src/main/resources/icons/Circle.png");
            circleImage.setFitHeight(40); circleImage.setFitWidth(40); circleButton.setGraphic(circleImage);
        final MFXRectangleToggleNode ovalButton = new MFXRectangleToggleNode("Oval");
            var ovalImage = new ImageView("file:src/main/resources/icons/Elipse.png");
            ovalImage.setFitHeight(40); ovalImage.setFitWidth(40); ovalButton.setGraphic(ovalImage);
        final MFXRectangleToggleNode rectangleButton = new MFXRectangleToggleNode("Rectangle");
            var rectangleImage = new ImageView("file:src/main/resources/icons/Rect.png");
            rectangleImage.setFitHeight(40); rectangleImage.setFitWidth(40); rectangleButton.setGraphic(rectangleImage);
        final MFXRectangleToggleNode squareButton = new MFXRectangleToggleNode("Square");
            var squareImage = new ImageView("file:src/main/resources/icons/Square.png");
            squareImage.setFitHeight(40); squareImage.setFitWidth(40); squareButton.setGraphic(squareImage);

        circleButton.setToggleGroup(group);
        ovalButton.setToggleGroup(group);
        rectangleButton.setToggleGroup(group);
        squareButton.setToggleGroup(group);

        circleButton.setUserData(Tool.CIRCLE);
        ovalButton.setUserData(Tool.OVAL);
        rectangleButton.setUserData(Tool.RECTANGLE);
        squareButton.setUserData(Tool.SQUARE);
        selectButton.setUserData(Tool.SELECT);

        HBox controls = new HBox();
        controls.getChildren().addAll(circleButton, ovalButton, rectangleButton, squareButton);

        // Entities
        final MFXRectangleToggleNode btnCamera = new MFXRectangleToggleNode();
            var cameraImage = new ImageView("file:src/main/resources/icons/bi_projector-fill.png");
            cameraImage.setFitHeight(40); cameraImage.setFitWidth(40); btnCamera.setGraphic(cameraImage);
        final MFXRectangleToggleNode btnSpeaker = new MFXRectangleToggleNode();
            var speakerImage = new ImageView("file:src/main/resources/icons/bi_speaker-fill.png");
            speakerImage.setFitHeight(40); speakerImage.setFitWidth(40); btnSpeaker.setGraphic(speakerImage);
        final MFXRectangleToggleNode btnWifi = new MFXRectangleToggleNode();
            var wifiImage = new ImageView("file:src/main/resources/icons/bi_router-fill.png");
            wifiImage.setFitHeight(40); wifiImage.setFitWidth(40); btnWifi.setGraphic(wifiImage);
        final MFXRectangleToggleNode btnMicrophone = new MFXRectangleToggleNode();
            var microphoneImage = new ImageView("file:src/main/resources/icons/bi_router-fill.png");
            microphoneImage.setFitHeight(40); microphoneImage.setFitWidth(40); btnMicrophone.setGraphic(microphoneImage);

        btnCamera.setToggleGroup(group);
        btnSpeaker.setToggleGroup(group);
        btnWifi.setToggleGroup(group);
        btnMicrophone.setToggleGroup(group);

        btnCamera.setUserData(Tool.ICON);
        btnSpeaker.setUserData(Tool.ICON);
        btnWifi.setUserData(Tool.ICON);
        btnMicrophone.setUserData(Tool.ICON);

        HBox entityBox = new HBox();
        entityBox.getChildren().addAll(btnCamera, btnSpeaker, btnWifi, btnMicrophone);

        // Cables
        final MFXRectangleToggleNode btnHdmi = new MFXRectangleToggleNode();
            var hdmiImage = new ImageView("file:src/main/resources/icons/Hdmi.png");
            hdmiImage.setFitHeight(40); hdmiImage.setFitWidth(40); btnHdmi.setGraphic(hdmiImage);
        final MFXRectangleToggleNode btnElLive = new MFXRectangleToggleNode();
            var elLiveImage = new ImageView("file:src/main/resources/icons/ElWire.png");
            elLiveImage.setFitHeight(40); elLiveImage.setFitWidth(40); btnElLive.setGraphic(elLiveImage);
        final MFXRectangleToggleNode btnElNeutral = new MFXRectangleToggleNode();
            var elNeutralImage = new ImageView("file:src/main/resources/icons/NullWire.png");
            elNeutralImage.setFitHeight(40); elNeutralImage.setFitWidth(40); btnElNeutral.setGraphic(elNeutralImage);
        final MFXRectangleToggleNode btnElEarth = new MFXRectangleToggleNode();
            var elEarthImage = new ImageView("file:src/main/resources/icons/GroundWire.png");
            elEarthImage.setFitHeight(40); elEarthImage.setFitWidth(40); btnElEarth.setGraphic(elEarthImage);

        HBox cableBox = new HBox();
        cableBox.getChildren().addAll(btnHdmi, btnElLive, btnElNeutral, btnElEarth);

        btnHdmi.setToggleGroup(group);
        btnElLive.setToggleGroup(group);
        btnElNeutral.setToggleGroup(group);
        btnElEarth.setToggleGroup(group);

        btnHdmi.setUserData(Tool.CABLE);
        btnElLive.setUserData(Tool.CABLE);
        btnElNeutral.setUserData(Tool.CABLE);
        btnElEarth.setUserData(Tool.CABLE);

        // default selection
        selectedTool = Tool.CIRCLE;
        circleButton.setSelected(true);
        canvas.setEventHandler(createEventHandler());

        // Add save button
        MFXButton saveButton = new MFXButton("Save");
        saveButton.setOnAction(e -> {
            WritableImage wi = new WritableImage(1600, 900);
            Image snapshot = canvas.snapshot(null, wi);
            File output = new File("snapshot" + new Date().getTime() + ".png");
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", output);
            } catch (IOException eex) {
                throw new RuntimeException(eex);
            }
            System.out.println(snapshot);
        });
        MFXButton backButton = new MFXButton("Back");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinWidth(Region.USE_PREF_SIZE);
        getChildren().addAll(
                new Label("Toolbox"),
                handleBox,
                new Label("Shapes"),
                controls,
                new Label("Entities"),
                entityBox,
                new Label("Cables"),
                cableBox,
                new Region(),
                saveButton,
                backButton
                );
    }

    /**
     * Sets up handlers.
     */
    private void setupHandlers() {
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            selectedTool = (Tool) group.getSelectedToggle().getUserData();
            canvas.setEventHandler(createEventHandler());
        });

        colorPicker.setOnAction(event -> canvas.setEventHandler(createEventHandler()));
    }

    /**
     * Creates event handler depending on the selected tool and color.
     * @return a new event handler
     */
    private EventResponsible createEventHandler() {
        Color selectedColor = colorPicker.getValue();
        switch (selectedTool) {
            case OVAL -> {
                return new DrawingTool(canvas, Oval::new, selectedColor);
            }
            case CIRCLE -> {
                return new DrawingTool(canvas, Circle::new, selectedColor);
            }
            case SQUARE -> {
                return new DrawingTool(canvas, Square::new, selectedColor);
            }
            case RECTANGLE -> {
                return new DrawingTool(canvas, Rectangle::new, selectedColor);
            }
            case POLYGON -> {
                return new DrawingTool(canvas, Polygon::new, selectedColor);
            }
            case ICON -> {
                var current = (MFXRectangleToggleNode) group.getSelectedToggle();
                var iconPath = ((ImageView) current.getGraphic()).getImage().getUrl();
                System.out.println(iconPath);
                return new DrawingTool(canvas, () -> new Icon(iconPath), selectedColor);
            }
            case CABLE -> {
                return new DrawingTool(canvas, Line::new, selectedColor);
            }
            case SELECT -> {
                return new SelectionTool(canvas);
            }
            default -> {
                return null;
            }
        }
    }
}
