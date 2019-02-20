
package server.network;

import database.Player;
import assets.*;
import assets.msgType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import database.SavedGame;
import server.AIGame;
import server.Game;
import server.ServerApp;
import static server.network.Server.allPlayers;


public class Session extends Thread{
    public static HashMap<String,Session> connectedPlayers = new HashMap<String,Session>();
    private Player player;
    private boolean connected = false;
    private Socket socket;
    private ObjectInputStream downLink;
    private ObjectOutputStream upLink;
    private Game game;
    private AIGame aiGame;
    private static int moveNum = 0;
    
    public Session(Socket socket){
        this.socket = socket;
        connected = openConnection();
        start();
    }
    public void run(){
        while(connected){
            try{
                Message message = (Message)downLink.readObject();
                messageHandler(message);
            }catch(IOException ioex){
                closeConnection();
            }catch(ClassNotFoundException cnfex){
                //error invalid message sent by client
            }
        }
    }
    private boolean openConnection(){
        try{
            downLink = new ObjectInputStream(socket.getInputStream());
            upLink = new ObjectOutputStream(socket.getOutputStream());
            return true;
        }catch(IOException ex){
            //error server cannot connect to client
            return false;
        }
    }
    private void closeConnection(){
        try{
            connected = false;
            upLink.close();
            downLink.close();
            socket.close();
        }catch(IOException ioex){
            //error connection already closed
        }
    }
    private void messageHandler(Message message){
        switch(message.getType()){
            case LOGIN:
                playerLogin(message);
                break;
            case LOGOUT:
                playerLogout();
                break;
            case REGISTER : 
                playerSignup(message.getData("username"), message.getData("password"));
                break;
            case GAME_REQUEST :
                requestGame(message);
                break;
            case GAME_RESPONSE :
                respondGame(message);
                break;
            case AIGAME :
                AIrequestGame();
                break;
            case MOVE:
                handleMove(message);
                break;
            case CHAT:
                chatHandler(message);
                break;
            case PAUSE:
                saveGame(message);
                break;
            default:
                //client sent unknown message type
                break;
        }
    }
    private void playerLogin(Message message){
        Message loginResult = new Message(msgType.LOGIN);
        boolean playerAuth = database.Players.playerAuth(message.getData("username"), message.getData("password"));
        if(playerAuth){
            player = database.Players.getPlayerInfo(message.getData("username"));
            loginResult.setData("signal", MsgSignal.SUCCESS);
            loginResult.setData("username", player.getUsername());
            loginResult.setData("score", String.valueOf(player.getScore()));
            Server.allPlayers.get(player.getUsername()).setStatus(Status.ONLINE);
            this.connectedPlayers.put(player.getUsername(), this);
            sendMessage(loginResult);
            initConnection();
            pushNotification("status", Server.allPlayers.get(player.getUsername()).getStatus());
        }else{
            loginResult.setData("signal", MsgSignal.FAILURE);
            sendMessage(loginResult);
            connected = false;
        }
    }
    private void playerLogout(){
        connectedPlayers.remove(this);
        Server.allPlayers.get(player.getUsername()).setStatus(Status.OFFLINE);
        ServerApp.serverController.PlayersTable();
        pushNotification("status", Server.allPlayers.get(player.getUsername()).getStatus());
        closeConnection();
    }
    public void sendMessage(Message message){
        try{
            this.upLink.writeObject(message);
        }catch(IOException ioex){
            //error cannot send message to client
        }
    }
    public void chatHandler(Message message){
        connectedPlayers.get(message.getData("sender")).sendMessage(message);
        if(!message.getData("sender").equals(message.getData("receiver"))){
            if(connectedPlayers.containsKey(message.getData("receiver")))
                connectedPlayers.get(message.getData("receiver")).sendMessage(message);    
        }else{
            Message autoReply = new Message(msgType.CHAT);
            autoReply.setData("sender", "sender");
            autoReply.setData("text", "iam computer");
            connectedPlayers.get(message.getData("receiver")).sendMessage(autoReply);
        }
            
    }
    private void playerSignup(String username, String password){
        Message result = new Message(msgType.REGISTER);
        if(!database.Players.playerExisted(username)){
            if(database.Players.insertPlayer(username,password)){
                result.setData("signal", MsgSignal.SUCCESS);
                Player newPlayer = new Player(username, 0, password);
                newPlayer.setStatus(Status.OFFLINE);
                broadcastNewPlayer(newPlayer);
                Server.allPlayers.put(username, newPlayer);
                ServerApp.serverController.PlayersTable();
            }
        }
        else{
           result.setData("signal", MsgSignal.FAILURE);}
        sendMessage(result);
        
    }
    public void pushNotification(String key, String value){
        connectedPlayers.entrySet().forEach((session) -> {
            Message notification = new Message(msgType.NOTIFY);
            notification.setData("username", player.getUsername());
            notification.setData("key", key);
            notification.setData("value", value);
            session.getValue().sendMessage(notification);
        });
        ServerApp.serverController.PlayersTable();
    }
    private void initConnection(){
        for(Map.Entry<String, Player> player : allPlayers.entrySet()){
            Message message = new Message(msgType.INIT);
            message.setData("username", player.getValue().getUsername());
            message.setData("score", String.valueOf(player.getValue().getScore()));
            message.setData("status", player.getValue().getStatus());
            this.sendMessage(message);
        }
    }
    private void broadcastNewPlayer(Player newPlayer){
        connectedPlayers.entrySet().forEach((session) -> {
            Message message = new Message(msgType.INIT);
            message.setData("username", newPlayer.getUsername());
            message.setData("score", String.valueOf(newPlayer.getScore()));
            message.setData("status", newPlayer.getStatus());
            session.getValue().sendMessage(message);
        });
    }
    public void requestGame(Message incoming){
        //handle request from client 1 and forward it to client2
        Message outgoing=new Message(msgType.GAME_REQUEST,"source",player.getUsername());
        if(connectedPlayers.containsKey(incoming.getData("destination"))){
            connectedPlayers.get(incoming.getData("destination")).sendMessage(outgoing);
            
            ServerApp.server.allPlayers.get(player.getUsername()).setStatus(Status.PLAYING);
            Session.connectedPlayers.get(player.getUsername()).pushNotification("status", Status.PLAYING);
            ServerApp.server.allPlayers.get(incoming.getData("destination")).setStatus(Status.PLAYING);
            Session.connectedPlayers.get(incoming.getData("destination")).pushNotification("status", Status.PLAYING);
            ServerApp.serverController.PlayersTable();
        }
    }
    public void saveGame(Message incoming){
        Message outgoing=new Message(msgType.PAUSE,"source",player.getUsername());
        if(connectedPlayers.containsKey(incoming.getData("destination"))){
            connectedPlayers.get(incoming.getData("destination")).sendMessage(outgoing);
            
            ServerApp.server.allPlayers.get(player.getUsername()).setStatus(Status.ONLINE);
            Session.connectedPlayers.get(player.getUsername()).pushNotification("status", Status.ONLINE);
            ServerApp.server.allPlayers.get(incoming.getData("destination")).setStatus(Status.ONLINE);
            Session.connectedPlayers.get(incoming.getData("destination")).pushNotification("status", Status.ONLINE);
            ServerApp.serverController.PlayersTable();
            SavedGame sg = new SavedGame();
            sg.player1 = player.getUsername();
            sg.player2 = incoming.getData("destination");
            int k = 0;
            for(int i = 0; i<3; i++){
                for(int j = 0; j<3; j++){
                    sg.cell[k] = game.currentGame[i][j];
                    k++;
                }  
            }
            database.Players.pauseGame(sg);
        }    
    }
    public void respondGame(Message incoming){
        //handle response from client 2 and forward it to client1
        if(incoming.getData("response").equals("accept")){
            if(database.Players.checkPaused(incoming.getData("destination"),player.getUsername()) == null && 
                    database.Players.checkPaused(player.getUsername(), incoming.getData("destination")) == null ){
                game=new Game(incoming.getData("destination"),player.getUsername()); // creating new game.
                connectedPlayers.get(incoming.getData("destination")).game=game;
            }else{
                SavedGame sg = database.Players.checkPaused(incoming.getData("destination"),player.getUsername());
                if(sg == null)
                    sg = database.Players.checkPaused(player.getUsername(), incoming.getData("destination"));
                game=new Game(sg);
                connectedPlayers.get(incoming.getData("destination")).game=game;
            }
        }else{
            ServerApp.server.allPlayers.get(player.getUsername()).setStatus(Status.ONLINE);
            Session.connectedPlayers.get("destination").pushNotification("status", Status.ONLINE);
            ServerApp.server.allPlayers.get(player.getUsername()).setStatus(Status.ONLINE);
            Session.connectedPlayers.get("destination").pushNotification("status", Status.ONLINE);
            ServerApp.serverController.PlayersTable();
        }
        Message outgoing=new Message(msgType.GAME_RESPONSE,"source",player.getUsername());
        outgoing.setData("response", incoming.getData("response"));
        if(connectedPlayers.containsKey(incoming.getData("destination"))){
            connectedPlayers.get(incoming.getData("destination")).sendMessage(outgoing);        
        }
    }
    private void AIrequestGame(){
        aiGame = new AIGame(player.getUsername());
        ServerApp.server.allPlayers.get(player.getUsername()).setStatus(Status.PLAYING);
        Session.connectedPlayers.get(player.getUsername()).pushNotification("status", Status.PLAYING);
        ServerApp.serverController.PlayersTable();
    }
    private void handleMove(Message message) {
         if(message.getData("target")!=null&&message.getData("target").equals("computer")){
             aiGame.takeMove(Integer.parseInt(message.getData("x")), Integer.parseInt(message.getData("y")));
         }else{
            if(game.validateMove(player.getUsername(), Integer.parseInt(message.getData("x")), Integer.parseInt(message.getData("y")))){
                switch (game.checkForWin(player.getUsername(), Integer.parseInt(message.getData("x")), Integer.parseInt(message.getData("y")))){
                    case "gameOn":
                        if(game.incMove%2==0){
                            connectedPlayers.get(game.getPlayer1()).sendMessage(message);
                        }else{
                            connectedPlayers.get(game.getPlayer2()).sendMessage(message);
                        }
                        break;
                    case "win" :
                        sendMessage(new Message(msgType.GAME_OVER,"line","You win !"));
                        Message lose=new Message(msgType.GAME_OVER,"line","You lose !");
                        String username=player.getUsername();
                        database.Players.updateScoreWin(username);
                        ServerApp.server.allPlayers.get(this.player.getUsername()).setScore(ServerApp.server.allPlayers.get(this.player.getUsername()).getScore()+10);
                        ServerApp.serverController.PlayersTable();
                        lose.setData("x", message.getData("x"));
                        lose.setData("y", message.getData("y"));
                        connectedPlayers.get(game.incMove%2==1?game.getPlayer1():game.getPlayer2()).sendMessage(lose);
                        ServerApp.server.allPlayers.get(game.getPlayer1()).setStatus(Status.ONLINE);
                        Session.connectedPlayers.get(game.getPlayer1()).pushNotification("status", Status.ONLINE);
                        ServerApp.server.allPlayers.get(game.getPlayer2()).setStatus(Status.ONLINE);
                        Session.connectedPlayers.get(game.getPlayer2()).pushNotification("status", Status.ONLINE);
                        ServerApp.serverController.PlayersTable();
                        game=null;
                        break;
                    case "draw":
                        sendMessage(new Message(msgType.GAME_OVER,"line","Draw !"));
                        Message draw=new Message(msgType.GAME_OVER,"line","Draw !");
                        String username2=player.getUsername();
                        database.Players.updateScoreDraw(username2);
                        ServerApp.server.allPlayers.get(this.player.getUsername()).setScore(ServerApp.server.allPlayers.get(this.player.getUsername()).getScore()+5);
                        ServerApp.serverController.PlayersTable();
                        draw.setData("x", message.getData("x"));
                        draw.setData("y", message.getData("y"));
                        connectedPlayers.get(game.incMove%2==1?game.getPlayer1():game.getPlayer2()).sendMessage(draw);
                        ServerApp.server.allPlayers.get(game.getPlayer1()).setStatus(Status.ONLINE);
                        Session.connectedPlayers.get(game.getPlayer1()).pushNotification("status", Status.ONLINE);
                        ServerApp.server.allPlayers.get(game.getPlayer2()).setStatus(Status.ONLINE);
                        Session.connectedPlayers.get(game.getPlayer2()).pushNotification("status", Status.ONLINE);
                        ServerApp.serverController.PlayersTable();
                        game=null;
                        break;
                }
            }
        

        }
    }
}
