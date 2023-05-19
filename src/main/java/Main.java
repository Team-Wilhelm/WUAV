import gui.util.SceneManager;
import gui.model.UserModel;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import utils.ThreadPool;

import java.util.Objects;


public class Main extends Application {
    //TODO check for customers who have been in the system for over 48 months and delete them
    //TODO custom window frame
    @Override
    public void start(Stage primaryStage) throws Exception {
        long start = System.currentTimeMillis();
        Parent root;
        if (true)
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(SceneManager.LOGIN_SCENE)));
        else {
            //TODO change back
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(SceneManager.MENU_SCENE)));
            primaryStage.setMaximized(true);
            UserModel.getInstance().setLoggedInUser(UserModel.getInstance().getUserByUsername("admin"));
        }

        primaryStage.setTitle("WUAV Documentation Management System");
        primaryStage.getIcons().add(new Image("/img/icons/WUAV.png"));

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);

        primaryStage.centerOnScreen();
        primaryStage.show();

        primaryStage.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getClickCount() == 2 && primaryStage.isMaximized() && e.getScreenY() < 30) {
                primaryStage.setMaximized(false);
                primaryStage.setWidth(Screen.getPrimary().getBounds().getWidth() - 200);
                primaryStage.setHeight(Screen.getPrimary().getBounds().getHeight() - 200);
                primaryStage.centerOnScreen();
            } else if (e.getClickCount() == 2 && !primaryStage.isMaximized())
                primaryStage.setMaximized(true);
        });

        //TODO set this to a proper close request
        primaryStage.setOnCloseRequest(e -> {
            primaryStage.close();
            stop();
            System.exit(0);
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