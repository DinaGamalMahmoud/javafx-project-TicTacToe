
package client;

import client.network.Session;
import client.controllers.*;
import client.controllers.GameController;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ClientApp extends Application {

    public static Stage primaryStage;
    public static Scene signIn;
    public static Scene signUp;
    public static Scene home;
    public static Scene game;
    public static GameController gameController;
    public static HomeController homeController;
    public static LoginController loginController;
    public static Session session;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        //sign in
        FXMLLoader signInLoader = new FXMLLoader();
        signInLoader.setLocation(getClass().getResource("/gui/LoginView.fxml"));
        Parent signInParent = signInLoader.load();
        signIn = new Scene(signInParent, 700, 500);
        loginController = (LoginController) signInLoader.getController();
        //sign up
        FXMLLoader signUpLoader = new FXMLLoader();
        signUpLoader.setLocation(getClass().getResource("/gui/SignupView.fxml"));
        Parent signUpParent = signUpLoader.load();
        signUp = new Scene(signUpParent, 700, 500);
        //home
        FXMLLoader homeLoader = new FXMLLoader();
        homeLoader.setLocation(getClass().getResource("/gui/HomeView.fxml"));
        Parent homeParent = homeLoader.load();
        home = new Scene(homeParent, 700, 500);
        homeController = (HomeController) homeLoader.getController();
        //game
        FXMLLoader gameLoader = new FXMLLoader();
        gameLoader.setLocation(getClass().getResource("/gui/GameView.fxml"));
        Parent gameParent = gameLoader.load();
        game = new Scene(gameParent, 900, 600);
        gameController = (GameController) gameLoader.getController();

        stage.setTitle("Game");
        stage.setScene(signIn);
        stage.show();
        stage.setMinWidth(800);
        stage.setMaxWidth(800);
        stage.setMinHeight(600);
        stage.setMaxHeight(600);
        primaryStage.setOnCloseRequest((event) -> {
            if (session != null && session.connected) {
                session.closeConnection();
            }
        });
    }
    
            public Scene game(){
        try {
            FXMLLoader gameLoader = new FXMLLoader();
            gameLoader.setLocation(getClass().getResource("/gui/GameView.fxml"));
            Parent gameParent = gameLoader.load();
            game = new Scene(gameParent, 900, 600);
        } catch (IOException ex) {
        }
                    return game;

            }
   
    public static void main(String[] args) {
        launch(args);
    }

}
