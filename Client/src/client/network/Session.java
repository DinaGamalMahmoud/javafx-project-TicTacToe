
package client.network;

import assets.*;
import client.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class Session {

    public static HashMap<String, Player> allPlayers = new HashMap<String, Player>();
    public Player player;
    private String player1;
    private String player2;
    private Socket socket;
    private final int portNumber;
    private final String ipAddress;
    private ObjectInputStream readObj;
    private ObjectOutputStream writeObj;
    public boolean connected = false;
    private boolean loggedin = false;
    public boolean IAmX = false;
    public boolean myTurn;
    private Button[][] btns = {
        {ClientView.gameController.b1, ClientView.gameController.b2, ClientView.gameController.b3},
        {ClientView.gameController.b4, ClientView.gameController.b5, ClientView.gameController.b6},
        {ClientView.gameController.b7, ClientView.gameController.b8, ClientView.gameController.b9}};

    public Session(String ipAddress, int portNumber) {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
    }

    public void openConnection() {
        try {
            socket = new Socket(ipAddress, portNumber);
            writeObj = new ObjectOutputStream(socket.getOutputStream());
            readObj = new ObjectInputStream(socket.getInputStream());
            connected = true;
        } catch (IOException ex) {
            connected = false;
        }
    }

    public void closeConnection() {
        sendMessage(new Message(msgType.LOGOUT));
        connected = false;
        try {
            writeObj.close();
            readObj.close();
            socket.close();
        } catch (IOException ex) {
        }
    }

    public void endconnection() {
        closeConnection();
        Platform.runLater(() -> {
            ClientView.primarystage.setScene(ClientView.signin);
            ClientView.loginController.endconnection();
        });
    }

    private void startcommunication() {
        new Thread(() -> {
            while (connected) {
                try {
                    Message message = (Message) readObj.readObject();
                    messageHandler(message);
                } catch (IOException ex) {
                    connected = false;
                    break;
                } catch (ClassNotFoundException cnfex) {
                }
            }
            try {
                socket.close();
                readObj.close();
                writeObj.close();
            } catch (IOException ex) {
            }
        }).start();
    }

    private void messageHandler(Message message) {
        switch (message.getType()) {
            case INIT:
            case NOTIFY:
                update(message);
                break;
            case GAME_REQUEST:
                respond(message);
                break;
            case GAME_RESPONSE:
                handleresponse(message);
                break;
            case MOVE:
                handlemove(message);
                break;
            case GAME_OVER:
                gameover(message);
                break;
            case CHAT:
                chat(message);
                break;
            case TERM:
                endconnection();
                break;
            case PAUSE:
                pausegame();
                break;
           
        }
    }

    public boolean login(String username, String password) {
        Message message = new Message(msgType.LOGIN);
        message.setData("username", username);
        message.setData("password", password);
        if (connected) {
            sendMessage(message);
            while (connected) {
                try {
                    Message response = (Message) readObj.readObject();
                    if (response.getType() == msgType.LOGIN) {
                        if (response.getData("signal").equals(MsgSignal.SUCCESS)) {
                            loggedin = true;
                            player = new Player();
                            player.setUsername(response.getData("username"));
                            player.setScore(Integer.parseInt(response.getData("score")));
                            startcommunication();
                        }
                        break;
                    } else {
                        messageHandler(response);
                    }
                } catch (IOException ioex) {
                } catch (ClassNotFoundException cnfex) {
                }
            }
        }
        return loggedin;
    }

    public boolean signup(String username, String password) {
        boolean regResult = false;
        Message message = new Message(msgType.REGISTER);
        message.setData("username", username);
        message.setData("password", password);
        if (connected) {
            sendMessage(message);
            while (connected) {
                try {
                    Message response = (Message) readObj.readObject();
                    if (response.getType() == msgType.REGISTER) {
                        if (response.getData("signal").equals(MsgSignal.SUCCESS)) {
                            regResult = true;
                        }
                        break;
                    }
                } catch (IOException ioex) {
                } catch (ClassNotFoundException cnfex) {
                }
            }
        }
        return regResult;
    }

    private void sendMessage(Message message) {
        try {
            writeObj.writeObject(message);
        } catch (IOException ioex) {
        }
    }

    public void update(Message message) {
        if (!message.getData("username").equals(this.player.getUsername())) {
            if (message.getType() == msgType.INIT) {
                Player newPlayer = new Player();
                newPlayer.setUsername(message.getData("username"));
                newPlayer.setStatus(message.getData("status"));
                newPlayer.setScore(Integer.parseInt(message.getData("score")));
                allPlayers.put(message.getData("username"), newPlayer);
            } else if (message.getType() == msgType.NOTIFY) {
                switch (message.getData("key")) {
                    case "status":
                        allPlayers.get(message.getData("username")).setStatus(message.getData("value"));
                        break;
                    case "score":
                        allPlayers.get(message.getData("username")).setScore(Integer.parseInt(message.getData("value")));
                        break;
                }
            }
            Platform.runLater(ClientView.homeController::PlayersTable);
        } else {
            if (message.getType() == msgType.NOTIFY && message.getData("key").equals("score")) {
                player.setScore(Integer.parseInt(message.getData("value")));
                Platform.runLater(ClientView.homeController::player);
            }
        }
    }

   
    public void requestgame(String secondPlayerName) {
        Message message = new Message(msgType.GAME_REQUEST, "destination", secondPlayerName);
        sendMessage(message);
    }
     public void sendresponse(boolean response) {
        IAmX = false;
        Message outgoing = new Message(msgType.GAME_RESPONSE, "destination", player1);
        outgoing.setData("response", response ? "accept" : "deny");
        sendMessage(outgoing);
    }


    public void respond(Message incoming) {
        player1 = incoming.getData("source");
        Platform.runLater(() -> {
            ClientView.homeController.alert(player1);
        });
    }

   
    public void handleresponse(Message incoming) {
        if (incoming.getData("response").equals("accept")) {
            IAmX = true;
            myTurn = true;
            player2 = incoming.getData("source");
            Platform.runLater(() -> {
                ClientView.primarystage.setScene(client.ClientView.game);
                ClientView.gameController.resetScene();
                ClientView.gameController.img = new Image(Session.this.getClass().getResourceAsStream("/images/x.png"));
            });
        } else {
            //other player rejected request
        }
    }

    public void AI() {
        ClientView.gameController.resetScene();
        sendMessage(new Message(msgType.AIGAME));
        player1 = player.getUsername();
        player2 = "computer";
        IAmX = true;
        myTurn = true;
        ClientView.gameController.img = new Image(getClass().getResourceAsStream("/images/x.png"));
    }

    public void move(String x, String y) {
        myTurn = false;
        Message message = new Message(msgType.MOVE);
        message.setData("x", x);
        message.setData("y", y);
        message.setData("target", player2);
        sendMessage(message);
    }

    public void pause() {
        Message message = new Message(msgType.PAUSE);
        sendMessage(message);
    }

    private void handlemove(Message message) {
        myTurn = true;
        Platform.runLater(() -> {
            btns[Integer.parseInt(message.getData("x"))][Integer.parseInt(message.getData("y"))].setGraphic(new ImageView(IAmX ? "/images/o.png" : "/images/x.png"));
            if (Integer.parseInt(message.getData("x")) == 0) {
                if (Integer.parseInt(message.getData("y")) == 0) {
                    ClientView.gameController.flag1 = 1;
                } else if (Integer.parseInt(message.getData("y")) == 1) {
                    ClientView.gameController.flag2 = 1;
                } else {
                    ClientView.gameController.flag3 = 1;
                }
            } else if (Integer.parseInt(message.getData("x")) == 1) {
                if (Integer.parseInt(message.getData("y")) == 0) {
                    ClientView.gameController.flag4 = 1;
                } else if (Integer.parseInt(message.getData("y")) == 1) {
                    ClientView.gameController.flag5 = 1;
                } else {
                    ClientView.gameController.flag6 = 1;
                }
            } else {
                if (Integer.parseInt(message.getData("y")) == 0) {
                    ClientView.gameController.flag7 = 1;
                } else if (Integer.parseInt(message.getData("y")) == 1) {
                    ClientView.gameController.flag1 = 8;
                } else {
                    ClientView.gameController.flag9 = 1;
                }
            }
        });
    }

    private void gameover(Message message) {
        Platform.runLater(() -> {
            if (message.getData("line").equals("You lose !") || message.getData("line").equals("Draw !")) {
                btns[Integer.parseInt(message.getData("x"))][Integer.parseInt(message.getData("y"))].setGraphic(new ImageView(IAmX ? "/images/o.png" : "/images/x.png"));
            }
        });
        String msg = message.getData("line");
        Platform.runLater(() -> {
            if (player2 != null && player2.equals("computer")) {
                Alert alert = new Alert(AlertType.CONFIRMATION, msg, new ButtonType("Play again", ButtonData.OK_DONE), new ButtonType("cancel", ButtonData.NO));
                alert.setTitle("Game over");
                alert.showAndWait();
                if (alert.getResult().getButtonData() == ButtonData.OK_DONE) {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            btns[i][j].setGraphic(new ImageView("/images/empty.png"));
                        }
                    }
                    AI();
                } else {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            btns[i][j].setGraphic(new ImageView("/images/empty.png"));
                        }
                    }
                    ClientView.primarystage.hide();
                    ClientView.primarystage.setScene(client.ClientView.home);
                    ClientView.primarystage.show();
                }
            } else {
                Alert alert = new Alert(AlertType.INFORMATION, msg, new ButtonType("Ok", ButtonData.OK_DONE));
                alert.setTitle("Game over");
                alert.setHeaderText("Game over");
                alert.setContentText(msg);
                alert.showAndWait();
                if (alert.getResult().getButtonData() == ButtonData.OK_DONE) {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            btns[i][j].setGraphic(new ImageView("/images/empty.png"));
                        }
                    }
                    ClientView.primarystage.hide();
                    ClientView.primarystage.setScene(client.ClientView.home);
                    ClientView.primarystage.show();
                }
            }
        });
        myTurn = false;
    }

    public void pausegame() {
        String msg = "The other player has just paused the game. It will be saved for the next time.";
        Alert alert = new Alert(AlertType.INFORMATION, msg, new ButtonType("Ok", ButtonData.OK_DONE));
        alert.setTitle("Game Paused");
        alert.setHeaderText("Game Paused");
        alert.setContentText(msg);
        alert.showAndWait();
        if (alert.getResult().getButtonData() == ButtonData.OK_DONE) {
            ClientView.primarystage.hide();
            ClientView.primarystage.setScene(client.ClientView.home);
            ClientView.primarystage.show();
        }
    }


public String getname(){
        if(player2 == null)
            return player1;
        return player2;
    }

 public void chat(Message message) {
        Platform.runLater(() -> {
            String msg = "@" + message.getData("sender") + ": " + message.getData("text") + "\n";
            ClientView.gameController.para.appendText(msg);
        });
    }

    public void send(String text) {
        if (!text.equals("")) {
            Message message = new Message(msgType.CHAT);
            String receiver;
            if (player1 == null) {
                receiver = player2;
            } else {
                receiver = player1;
            }
            message.setData("sender", player.getUsername());
            message.setData("receiver", receiver);
            sendMessage(message);
        }
    }

}
