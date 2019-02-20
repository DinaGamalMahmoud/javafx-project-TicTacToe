
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
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


public class ServerController implements Initializable {

    @FXML
    private TableView<Player> tableView;
    @FXML
    private TableColumn loginColumn;
    @FXML
    private TableColumn scoreColumn;
    @FXML
    private TableColumn statusColumn;
    @FXML
    private ObservableList<Player> data;
    private ObservableList<Player> playersList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        loginColumn.setCellValueFactory(
                new PropertyValueFactory<>("username")
        );
        scoreColumn.setCellValueFactory(
                new PropertyValueFactory<>("score")
        );
        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );
        data = FXCollections.observableArrayList();
    }

    @FXML
    protected void handleToggleOnAction(ActionEvent t) {
        if (!ServerApp.server.running) {
            if (ServerApp.server.startServer(5555)) {
                PlayersTable();

            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error starting the server");
                alert.setContentText("Cannot start the server, try using another port number");
                alert.showAndWait();
            }
        } else {
            ServerApp.server.stopServer();

            playersList.clear();

        }
    }

    public void PlayersTable() {
       // if (!ServerApp.server.running) {
            playersList.clear();
    //    } else {
            ServerApp.server.allPlayers.entrySet().forEach((player) -> {
                playersList.add(player.getValue());
                  tableView.setItems(playersList);
            });    
     //   }
    }
}
