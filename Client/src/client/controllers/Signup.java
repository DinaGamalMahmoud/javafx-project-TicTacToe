
package client.controllers;

import client.Client;
import client.network.Session;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class Signup implements Initializable {
    
    @FXML private Label label=new Label();
    @FXML private Text massage = new Text();
    @FXML private TextField userName = new TextField();
    @FXML private TextField userPassword = new TextField();
    @FXML private TextField confirmPassword = new TextField();
    private ObservableList list = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {}
    
    @FXML private void signin(ActionEvent event) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Registration error!");
        if( userName.getText().equals("") || 
            userPassword.getText().equals("") ||
            confirmPassword.getText().equals("") ){
            alert.setContentText("complete all your information!");
            alert.showAndWait();
        }else if(!userPassword.getText().equals(confirmPassword.getText())){
            alert.setContentText("retype the password");
            alert.showAndWait();
        }else{
            if(Client.session == null){
                Client.session = new Session("192.168.1.98", 5555);
            }
            Client.session.openConnection();
            if(Client.session.connected){
                boolean regResult = Client.session.signup(userName.getText(), userPassword.getText());
                if(regResult){
                    Alert success = new Alert(AlertType.INFORMATION);
                    success.setTitle("Registration succeded!");
                    success.setContentText("Congratulations!");
                    success.showAndWait();
                    Client.primarystage.hide();
                    Client.primarystage.setScene(client.Client.signin);
                    Client.primarystage.show();
                }else{
                    alert.setContentText(" username already existed!");
                    alert.showAndWait();
                }
            }else{
                alert.setContentText("connection can't completed with server");
                alert.showAndWait();
            }
            Client.session.closeConnection();
        }
    }
   
    
}
