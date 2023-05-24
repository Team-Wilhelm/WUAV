import gui.util.SceneManager;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.ThreadPool;

import java.io.IOException;
import java.util.Objects;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent splashRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("view/Splash.fxml")));
        Scene splashScene = new Scene(splashRoot);
        primaryStage.setScene(splashScene);
        primaryStage.show();

        PauseTransition splashDelay = new PauseTransition(Duration.seconds(1));
        splashDelay.setOnFinished(event -> {
            try {
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(SceneManager.LOGIN_SCENE)));
                primaryStage.setTitle("WUAV Documentation Management System");
                primaryStage.getIcons().add(new Image("/img/icons/WUAV.png"));

                Scene scene = new Scene(root);
                System.out.println("Loading application");
                primaryStage.setScene(scene);
                MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);

                primaryStage.centerOnScreen();
                primaryStage.show();

                // All tables always visible
                //primaryStage.setMinWidth(1310);

                primaryStage.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                    if (e.getClickCount() == 2 && primaryStage.isMaximized() && e.getScreenY() < 30) {
                        primaryStage.setMaximized(false);
                        primaryStage.setWidth(Screen.getPrimary().getBounds().getWidth() - 200);
                        primaryStage.setHeight(Screen.getPrimary().getBounds().getHeight() - 200);
                        primaryStage.centerOnScreen();
                    } else if (e.getClickCount() == 2 && !primaryStage.isMaximized())
                        primaryStage.setMaximized(true);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        splashDelay.play();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        ThreadPool.getInstance().shutdown();
    }
}