
package server.controllers;

import database.Player;
import server.*;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


public class Controlserver implements Initializable {

    @FXML
    private TableView<Player> tableView;
    @FXML
    private TableColumn login;
    @FXML
    private TableColumn score;
    @FXML
    private TableColumn status;
    private ObservableList<Player> playersList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        login.setCellValueFactory(
                new PropertyValueFactory<>("username")
        );
        score.setCellValueFactory(
                new PropertyValueFactory<>("score")
        );
        status.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );
    }

    @FXML
    protected void turnon(ActionEvent t) {
        if (!ServerView.server.running) {
            if (ServerView.server.startServer(5555)) {
                PlayersTable();

            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error starting the server");
                alert.setContentText("Cannot start the server, try using another port number");
                alert.showAndWait();
            }
        } else {
            ServerView.server.stopServer();

            playersList.clear();

        }
    }

    public void PlayersTable() {
       // if (!ServerApp.server.running) {
            playersList.clear();
    //    } else {
            ServerView.server.allPlayers.entrySet().forEach((player) -> {
                playersList.add(player.getValue());
                  tableView.setItems(playersList);
            });    
     //   }
    }
}
