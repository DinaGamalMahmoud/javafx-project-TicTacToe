
package client.controllers;



import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import assets.*;
import client.*;
import client.network.Session;
import java.net.URL;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
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
        primaryStage = Client.primarystage;
        allPlayersTable.getSelectionModel().selectedIndexProperty().addListener(new table());        
    }   
    private class table implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        }
    };
    @FXML protected void invite(ActionEvent event) {
        if(allPlayersTable.getSelectionModel().getSelectedItem()!= null){
            if(allPlayersTable.getSelectionModel().getSelectedItem().getStatus().equals(Status.ONLINE)){
                Client.session.requestgame(allPlayersTable.getSelectionModel().getSelectedItem().getUsername());
                Client.gameController.para.setText("");
            }else{
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Player not available");
                alert.setHeaderText("Player not available");
                alert.setContentText(allPlayersTable.getSelectionModel().getSelectedItem().getUsername()+" not available");
                alert.showAndWait();      
            }
        }
    };
    @FXML protected void logout(ActionEvent event) {
        Client.session.closeConnection();
        primaryStage.setScene(client.Client.signin);
    }
    @FXML protected void ai(ActionEvent event) {
        Client.session.AI();
        Client.gameController.para.setText("");
        primaryStage.setScene(client.Client.game);
        Client.gameController.resetScene();
    }
    @FXML public void player() {
        playerName.setText(Client.session.player.getUsername());
        playerScore.setText(Integer.toString(Client.session.player.getScore()));
        allPlayersTable.getSelectionModel().selectFirst();
    }
   
    public void PlayersTable(){
        playersData.clear(); 
        Session.allPlayers.entrySet().forEach((player) -> {
            playersData.add(player.getValue());
        });
        allPlayersTable.setItems(playersData);
    }
    public void alert(String playerName){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, playerName+" wants to play with you", ButtonType.NO, ButtonType.YES);
        if (alert.showAndWait().get() == ButtonType.YES) {
            Client.session.sendresponse(true);
            Client.gameController.resetScene();
            Client.primarystage.setScene(client.Client.game);
            System.out.println("play again");
            Client.gameController.img = new Image(getClass().getResourceAsStream("/images/o.png"));
        }else{
            Client.session.sendresponse(false);
        }
    }
}
