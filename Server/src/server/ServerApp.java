
package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.controllers.Controlserver;
import server.network.Server;


public class ServerApp extends Application {
    public static Server server = new Server();
    public static Stage primaryStage ;
    public static Scene serverScene;
    public static Controlserver serverController;
    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader serverLoader = new FXMLLoader();
        serverLoader.setLocation(getClass().getResource("/views/gui.fxml"));
        Parent serverParent = serverLoader.load();
        serverScene = new Scene(serverParent);
        serverController = (Controlserver)serverLoader.getController();
        stage.setTitle("TicTacToe Server");
        stage.setScene(serverScene);
        stage.show();

        primaryStage.setOnCloseRequest((event) -> {
            if(server.running)
                server.stopServer();
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
    
}
