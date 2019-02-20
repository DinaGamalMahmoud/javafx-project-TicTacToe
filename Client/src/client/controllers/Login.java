
package client.controllers;

import client.network.Session;
import client.Client;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class Login implements Initializable {
    
    @FXML private TextField password;
    @FXML private TextField username;
    private Stage primaryStage;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        primaryStage = Client.primarystage;
    }
    @FXML protected void login(ActionEvent event) {
        if(Client.session == null){
            Client.session = new Session("192.168.1.98", 5555);
        }
        Client.session.openConnection();
        if(Client.session.connected){
            if(Client.session.login(username.getText(), password.getText())){
                primaryStage.hide();
                primaryStage.setScene(client.Client.home);
                primaryStage.show();
                Client.homeController.PlayersTable();
                Client.homeController.player();
            }else{
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("TicTacToe");
                alert.setHeaderText("Login failure");
                alert.setContentText("Invalid username or password!");
                alert.showAndWait();
            }
        }else{
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("TicTacToe");
            alert.setHeaderText("Connection failure");
            alert.setContentText("Cannot establish connection with server!");
            alert.showAndWait();
        }
    }
    @FXML protected void signup(ActionEvent event) {
        primaryStage.hide();
        primaryStage.setScene(client.Client.signup);
        primaryStage.show();
    }
    public void endconnection(){
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Connection lost");
        alert.setHeaderText("Server disconnected!");
        alert.setContentText("Opps! you've lost the connection with server, try reconnecting later");
        alert.showAndWait();
    }
}
