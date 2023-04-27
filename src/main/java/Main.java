import gui.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(SceneManager.MENU_SCENE)));
        primaryStage.setTitle("WUAV");
        primaryStage.getIcons().add(new Image("/img/WUAV.png"));
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
        //Address a = new Address("Rainbow road", "77", "1234", "Night city", "Wonderland");
        //Customer c = new Customer("Justin Timbersea", "justin@hotmail.com", "12345678", a, CustomerType.PRIVATE, Date.valueOf(LocalDate.now()));
        //CustomerDAO customerDAO = new CustomerDAO();
        //customerDAO.add(c);
        //System.out.println(customerDAO.getById(UUID.fromString("AB99E9BB-7BC8-461D-AA78-ADA1F38ECBD9")).getCustomerName());
    }
}