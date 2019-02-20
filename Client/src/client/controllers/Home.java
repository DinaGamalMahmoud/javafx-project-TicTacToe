
package client.controllers;


import assets.*;
import client.*;
import client.network.Session;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;



public class Home implements Initializable {


    
    @FXML private Label playerName,playerScore;
    @FXML private TableView<Player> allPlayersTable;
    @FXML private TableColumn colUsername;
    @FXML private TableColumn colScore;
    @FXML private TableColumn colStatus;
    private ObservableList<Player> playersData = FXCollections.observableArrayList();
    private Stage primaryStage;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colUsername.setCellValueFactory(
            new PropertyValueFactory<>("username")
        );
        colScore.setCellValueFactory(
            new PropertyValueFactory<>("score")
        );
        colStatus.setCellValueFactory(
            new PropertyValueFactory<>("status")
        );
        primaryStage = ClientApp.primaryStage;
        allPlayersTable.getSelectionModel().selectedIndexProperty().addListener(new RowSelectChangeListener());        
    }   
    private class RowSelectChangeListener implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            opponentInfo();
        }
    };
    @FXML protected void handleButton_invite_Action(ActionEvent event) {
        if(allPlayersTable.getSelectionModel().getSelectedItem()!= null){
            if(allPlayersTable.getSelectionModel().getSelectedItem().getStatus().equals(Status.ONLINE)){
                ClientApp.session.requestGame(allPlayersTable.getSelectionModel().getSelectedItem().getUsername());
                ClientApp.gameController.txt_area.setText("");
            }else{
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Player not available");
                alert.setHeaderText("Player not available");
                alert.setContentText(allPlayersTable.getSelectionModel().getSelectedItem().getUsername()+" not available");
                alert.showAndWait();      
            }
        }
    };
    @FXML protected void handleButton_logout_Action(ActionEvent event) {
        ClientApp.session.closeConnection();
        primaryStage.setScene(client.ClientApp.signIn);
    }
    @FXML protected void handleButton_arcade_Action(ActionEvent event) {
        ClientApp.session.playWithAI();
        ClientApp.gameController.txt_area.setText("");
        primaryStage.setScene(client.ClientApp.game);
        ClientApp.gameController.resetScene();
    }
    @FXML public void playerInfo() {
        playerName.setText(ClientApp.session.player.getUsername());
        playerScore.setText(Integer.toString(ClientApp.session.player.getScore()));
        allPlayersTable.getSelectionModel().selectFirst();
    }
    @FXML protected void opponentInfo() {
      
    }
    public void PlayersTable(){
        playersData.clear(); 
        Session.allPlayers.entrySet().forEach((player) -> {
            playersData.add(player.getValue());
        });
        allPlayersTable.setItems(playersData);
    }
    public void showAlert(String playerName){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, playerName+" wants to play with you", ButtonType.NO, ButtonType.YES);
        if (alert.showAndWait().get() == ButtonType.YES) {
            ClientApp.session.sendResponse(true);
            ClientApp.gameController.resetScene();
            ClientApp.primaryStage.setScene(client.ClientApp.game);
            System.out.println("play again");
            ClientApp.gameController.img = new Image(getClass().getResourceAsStream("/images/o.png"));
        }else{
            ClientApp.session.sendResponse(false);
        }
    }
}
