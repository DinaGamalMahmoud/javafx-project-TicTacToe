
package client;

import client.network.Session;
import client.controllers.*;
import client.controllers.Game;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ClientApp extends Application {

    public static Stage primarystage;
    public static Scene signin;
    public static Scene signup;
    public static Scene home;
    public static Scene game;
    public static Game gameController;
    public static Home homeController;
    public static Login loginController;
    public static Session session;

     public Scene game() {
        try {
            FXMLLoader gameLoader = new FXMLLoader();
            gameLoader.setLocation(getClass().getResource("/gui/game.fxml"));
            Parent gameParent = gameLoader.load();
            game = new Scene(gameParent, 900, 600);
        } catch (IOException ex) {
        }
        return game;

    }
     
    public void start(Stage stage) throws Exception {
        primarystage = stage;

        FXMLLoader signInLoader = new FXMLLoader();
        signInLoader.setLocation(getClass().getResource("/gui/login.fxml"));
        Parent signInParent = signInLoader.load();
        signin = new Scene(signInParent, 700, 500);
        loginController = (Login) signInLoader.getController();
        FXMLLoader signUpLoader = new FXMLLoader();
        signUpLoader.setLocation(getClass().getResource("/gui/signup.fxml"));
        Parent signUpParent = signUpLoader.load();
        signup = new Scene(signUpParent, 700, 500);
         FXMLLoader homeLoader = new FXMLLoader();
        homeLoader.setLocation(getClass().getResource("/gui/home.fxml"));
        Parent homeParent = homeLoader.load();
        home = new Scene(homeParent, 700, 500);
        homeController = (Home) homeLoader.getController();
        FXMLLoader gameLoader = new FXMLLoader();
        gameLoader.setLocation(getClass().getResource("/gui/game.fxml"));
        Parent gameParent = gameLoader.load();
        game = new Scene(gameParent, 900, 600);
        gameController = (Game) gameLoader.getController();

        stage.setTitle("Game");
        stage.setScene(signin);
        stage.show();
        stage.setMinWidth(800);
        stage.setMaxWidth(800);
        stage.setMinHeight(600);
        stage.setMaxHeight(600);
        primarystage.setOnCloseRequest((event) -> {
            if (session != null && session.connected) {
                session.closeConnection();
            }
        });
    }
    
         
   
    public static void main(String[] args) {
        launch(args);
    }

}
