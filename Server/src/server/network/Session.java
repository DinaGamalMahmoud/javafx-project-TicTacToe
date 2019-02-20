
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
    private ObjectInputStream readobj;
    private ObjectOutputStream writeobj;
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
                Message message = (Message)readobj.readObject();
                msghandle(message);
            }catch(IOException ioex){
                closeConnection();
            }catch(ClassNotFoundException cnfex){
            }
        }
    }
    private boolean openConnection(){
        try{
            readobj = new ObjectInputStream(socket.getInputStream());
            writeobj = new ObjectOutputStream(socket.getOutputStream());
            return true;
        }catch(IOException ex){
            return false;
        }
    }
    private void closeConnection(){
        try{
            connected = false;
            writeobj.close();
            readobj.close();
            socket.close();
        }catch(IOException ex){
        }
    }
    private void startconnection(){
        for(Map.Entry<String, Player> player : allPlayers.entrySet()){
            Message message = new Message(msgType.INIT);
            message.setData("username", player.getValue().getUsername());
            message.setData("score", String.valueOf(player.getValue().getScore()));
            message.setData("status", player.getValue().getStatus());
            this.sendmsg(message);
        }
    }
    private void msghandle(Message message){
        switch(message.getType()){
            case LOGIN:
                login(message);
                break;
            case LOGOUT:
                logout();
                break;
            case REGISTER : 
                signup(message.getData("username"), message.getData("password"));
                break;
            case GAME_REQUEST :
                requestGame(message);
                break;
            case GAME_RESPONSE :
                respondGame(message);
                break;
            case AIGAME :
                AIgame();
                break;
            case MOVE:
                handleMove(message);
                break;
            case CHAT:
                chathandle(message);
                break;
            case PAUSE:
                saveGame(message);
                break;
        }
    }
    private void login(Message message){
        Message loginResult = new Message(msgType.LOGIN);
        boolean playerAuth = database.Players.signin(message.getData("username"), message.getData("password"));
        if(playerAuth){
            player = database.Players.getplayer(message.getData("username"));
            loginResult.setData("signal", MsgSignal.SUCCESS);
            loginResult.setData("username", player.getUsername());
            loginResult.setData("score", String.valueOf(player.getScore()));
            Server.allPlayers.get(player.getUsername()).setStatus(Status.ONLINE);
            this.connectedPlayers.put(player.getUsername(), this);
            sendmsg(loginResult);
            startconnection();
            notify("status", Server.allPlayers.get(player.getUsername()).getStatus());
        }else{
            loginResult.setData("signal", MsgSignal.FAILURE);
            sendmsg(loginResult);
            connected = false;
        }
    }
    private void logout(){
        connectedPlayers.remove(this);
        Server.allPlayers.get(player.getUsername()).setStatus(Status.OFFLINE);
        ServerApp.serverController.PlayersTable();
        notify("status", Server.allPlayers.get(player.getUsername()).getStatus());
        closeConnection();
    }
    private void signup(String username, String password){
        Message result = new Message(msgType.REGISTER);
        if(!database.Players.isexitsed(username)){
            if(database.Players.insertplayer(username,password)){
                result.setData("signal", MsgSignal.SUCCESS);
                Player newPlayer = new Player(username, 0, password);
                newPlayer.setStatus(Status.OFFLINE);
                showplayer(newPlayer);
                Server.allPlayers.put(username, newPlayer);
                ServerApp.serverController.PlayersTable();
            }
        }
        else{
           result.setData("signal", MsgSignal.FAILURE);}
        sendmsg(result);
        
    }
    public void sendmsg(Message message){
        try{
            this.writeobj.writeObject(message);
        }catch(IOException ioex){
            //error cannot send message to client
        }
    }
    public void chathandle(Message message){
        connectedPlayers.get(message.getData("sender")).sendmsg(message);
        if(!message.getData("sender").equals(message.getData("receiver"))){
            if(connectedPlayers.containsKey(message.getData("receiver")))
                connectedPlayers.get(message.getData("receiver")).sendmsg(message);    
        }else{
            Message autoReply = new Message(msgType.CHAT);
            autoReply.setData("sender", "sender");
            autoReply.setData("text", "iam computer");
            connectedPlayers.get(message.getData("receiver")).sendmsg(autoReply);
        }
            
    }
    
    public void notify(String key, String value){
        connectedPlayers.entrySet().forEach((session) -> {
            Message notification = new Message(msgType.NOTIFY);
            notification.setData("username", player.getUsername());
            notification.setData("key", key);
            notification.setData("value", value);
            session.getValue().sendmsg(notification);
        });
        ServerApp.serverController.PlayersTable();
    }
    
    private void showplayer(Player newPlayer){
        connectedPlayers.entrySet().forEach((session) -> {
            Message message = new Message(msgType.INIT);
            message.setData("username", newPlayer.getUsername());
            message.setData("status", newPlayer.getStatus());
            session.getValue().sendmsg(message);
        });
    }
    public void requestGame(Message incoming){
        Message outgoing=new Message(msgType.GAME_REQUEST,"source",player.getUsername());
        if(connectedPlayers.containsKey(incoming.getData("destination"))){
            connectedPlayers.get(incoming.getData("destination")).sendmsg(outgoing);
            
            ServerApp.server.allPlayers.get(player.getUsername()).setStatus(Status.PLAYING);
            Session.connectedPlayers.get(player.getUsername()).notify("status", Status.PLAYING);
            ServerApp.server.allPlayers.get(incoming.getData("destination")).setStatus(Status.PLAYING);
            Session.connectedPlayers.get(incoming.getData("destination")).notify("status", Status.PLAYING);
            ServerApp.serverController.PlayersTable();
        }
    }
    public void saveGame(Message incoming){
        Message outgoing=new Message(msgType.PAUSE,"source",player.getUsername());
        if(connectedPlayers.containsKey(incoming.getData("destination"))){
            connectedPlayers.get(incoming.getData("destination")).sendmsg(outgoing);
            
            ServerApp.server.allPlayers.get(player.getUsername()).setStatus(Status.ONLINE);
            Session.connectedPlayers.get(player.getUsername()).notify("status", Status.ONLINE);
            ServerApp.server.allPlayers.get(incoming.getData("destination")).setStatus(Status.ONLINE);
            Session.connectedPlayers.get(incoming.getData("destination")).notify("status", Status.ONLINE);
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
        if(incoming.getData("response").equals("accept")){
            if(database.Players.checkpaused(incoming.getData("destination"),player.getUsername()) == null && 
                    database.Players.checkpaused(player.getUsername(), incoming.getData("destination")) == null ){
                game=new Game(incoming.getData("destination"),player.getUsername()); // creating new game.
                connectedPlayers.get(incoming.getData("destination")).game=game;
            }else{
                SavedGame sg = database.Players.checkpaused(incoming.getData("destination"),player.getUsername());
                if(sg == null)
                    sg = database.Players.checkpaused(player.getUsername(), incoming.getData("destination"));
                game=new Game(sg);
                connectedPlayers.get(incoming.getData("destination")).game=game;
            }
        }else{
            ServerApp.server.allPlayers.get(player.getUsername()).setStatus(Status.ONLINE);
            Session.connectedPlayers.get("destination").notify("status", Status.ONLINE);
            ServerApp.server.allPlayers.get(player.getUsername()).setStatus(Status.ONLINE);
            Session.connectedPlayers.get("destination").notify("status", Status.ONLINE);
            ServerApp.serverController.PlayersTable();
        }
        Message outgoing=new Message(msgType.GAME_RESPONSE,"source",player.getUsername());
        outgoing.setData("response", incoming.getData("response"));
        if(connectedPlayers.containsKey(incoming.getData("destination"))){
            connectedPlayers.get(incoming.getData("destination")).sendmsg(outgoing);        
        }
    }
    private void AIgame(){
        aiGame = new AIGame(player.getUsername());
        ServerApp.server.allPlayers.get(player.getUsername()).setStatus(Status.PLAYING);
        Session.connectedPlayers.get(player.getUsername()).notify("status", Status.PLAYING);
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
                            connectedPlayers.get(game.getPlayer1()).sendmsg(message);
                        }else{
                            connectedPlayers.get(game.getPlayer2()).sendmsg(message);
                        }
                        break;
                    case "win" :
                        sendmsg(new Message(msgType.GAME_OVER,"line","You win !"));
                        Message lose=new Message(msgType.GAME_OVER,"line","You lose !");
                        String username=player.getUsername();
                        database.Players.updatewin(username);
                        ServerApp.server.allPlayers.get(this.player.getUsername()).setScore(ServerApp.server.allPlayers.get(this.player.getUsername()).getScore()+10);
                        ServerApp.serverController.PlayersTable();
                        lose.setData("x", message.getData("x"));
                        lose.setData("y", message.getData("y"));
                        connectedPlayers.get(game.incMove%2==1?game.getPlayer1():game.getPlayer2()).sendmsg(lose);
                        ServerApp.server.allPlayers.get(game.getPlayer1()).setStatus(Status.ONLINE);
                        Session.connectedPlayers.get(game.getPlayer1()).notify("status", Status.ONLINE);
                        ServerApp.server.allPlayers.get(game.getPlayer2()).setStatus(Status.ONLINE);
                        Session.connectedPlayers.get(game.getPlayer2()).notify("status", Status.ONLINE);
                        ServerApp.serverController.PlayersTable();
                        game=null;
                        break;
                    case "draw":
                        sendmsg(new Message(msgType.GAME_OVER,"line","Draw !"));
                        Message draw=new Message(msgType.GAME_OVER,"line","Draw !");
                        String username2=player.getUsername();
                        database.Players.updatedraw(username2);
                        ServerApp.server.allPlayers.get(this.player.getUsername()).setScore(ServerApp.server.allPlayers.get(this.player.getUsername()).getScore()+5);
                        ServerApp.serverController.PlayersTable();
                        draw.setData("x", message.getData("x"));
                        draw.setData("y", message.getData("y"));
                        connectedPlayers.get(game.incMove%2==1?game.getPlayer1():game.getPlayer2()).sendmsg(draw);
                        ServerApp.server.allPlayers.get(game.getPlayer1()).setStatus(Status.ONLINE);
                        Session.connectedPlayers.get(game.getPlayer1()).notify("status", Status.ONLINE);
                        ServerApp.server.allPlayers.get(game.getPlayer2()).setStatus(Status.ONLINE);
                        Session.connectedPlayers.get(game.getPlayer2()).notify("status", Status.ONLINE);
                        ServerApp.serverController.PlayersTable();
                        game=null;
                        break;
                }
            }
        

        }
    }
}
