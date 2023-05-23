import be.User;
import gui.util.DialogManager;
import gui.util.SceneManager;
import gui.model.UserModel;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import utils.ThreadPool;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        long start = System.currentTimeMillis();
        Parent root;
        if (!true)
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(SceneManager.LOGIN_SCENE)));
        else {
            User user = UserModel.getInstance().getUserByUsername("admin");
            UserModel.getInstance().setLoggedInUser(user);
            //TODO change back
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(SceneManager.MENU_SCENE)));
            primaryStage.setMaximized(true);
        }

        primaryStage.setTitle("WUAV Documentation Management System");
        primaryStage.getIcons().add(new Image("/img/icons/WUAV.png"));

        Scene scene = new Scene(root);
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
        System.out.println("Time to load: " + (System.currentTimeMillis() - start) + "ms");
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        ThreadPool.getInstance().shutdown();
    }
}