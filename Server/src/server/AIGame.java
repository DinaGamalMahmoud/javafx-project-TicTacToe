package server;

import assets.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import assets.msgType;
import server.network.Session;

class Point {
    int x, y;
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}

class PointAndScore {
    int score;
    Point point;
    PointAndScore(int score, Point point) {
        this.score = score;
        this.point = point;
    }
}

public class AIGame {
    Scanner scan = new Scanner(System.in);
    int[][] board = new int[3][3];
    List<Point> availablePoints;
    String player = null;
    Point compmove;
    
    public AIGame() {}
    public AIGame(String name) {
        player=name;
    }
    public boolean gameover() {
        return (hasXWon() || hasOWon() || getAvailableStates().isEmpty());
    }
    public boolean hasXWon() {
        if ((board[0][0] == board[1][1] && board[0][0] == board[2][2] && board[0][0] == 1) || (board[0][2] == board[1][1] && board[0][2] == board[2][0] && board[0][2] == 1)) {
            return true;
        }
        for (int i = 0; i < 3; ++i) {
            if (((board[i][0] == board[i][1] && board[i][0] == board[i][2] && board[i][0] == 1)
                    || (board[0][i] == board[1][i] && board[0][i] == board[2][i] && board[0][i] == 1))) {
                return true;
            }
        }
        return false;
    }
    public boolean hasOWon() {
        if ((board[0][0] == board[1][1] && board[0][0] == board[2][2] && board[0][0] == 2) || (board[0][2] == board[1][1] && board[0][2] == board[2][0] && board[0][2] == 2)) {
            return true;
        }
        for (int i = 0; i < 3; ++i) {
            if ((board[i][0] == board[i][1] && board[i][0] == board[i][2] && board[i][0] == 2)
                    || (board[0][i] == board[1][i] && board[0][i] == board[2][i] && board[0][i] == 2)) {
                return true;
            }
        }

        return false;
    }
    public List<Point> getAvailableStates() {
        availablePoints = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                if (board[i][j] == 0) {
                    availablePoints.add(new Point(i, j));
                }
            }
        }
        return availablePoints;
    }
    public void placeAMove(Point point, int player) {
        board[point.x][point.y] = player;  
    }
    public void takeHumanInput() {
        System.out.println("Your move: ");
        int x = scan.nextInt();
        int y = scan.nextInt();
        Point point = new Point(x, y);
        placeAMove(point, 2);
    }
    public String takeMove(int x, int y) {
        String stats = null;
        Point point = new Point(x, y);
        this.placeAMove(point, 2);
        this.minimax(0, 1); 
        placeAMove(this.compmove, 1);
        int x1 = this.compmove.x;
        int y1 = this.compmove.y;
        Message message=new Message(msgType.MOVE);
        message.setData("x", Integer.toString(x1));
        message.setData("y", Integer.toString(y1));
        if (this.hasXWon()) {
            message.setType(msgType.GAME_OVER);
            stats = "You lose !";
            message.setData("line", stats);
            Session.connectedPlayers.get(player).sendmsg(message); 
            
            ServerView.server.allPlayers.get(player).setStatus(Status.ONLINE);
            Session.connectedPlayers.get(player).notify("status", Status.ONLINE);
            ServerView.serverController.PlayersTable();
        } else if (this.hasOWon()) {
            message.setType(msgType.GAME_OVER);
            stats = "You win !";
            message.setData("line", stats);
            Session.connectedPlayers.get(player).sendmsg(message); 
        } else if (this.gameover() && !this.hasXWon()) {
            message.setType(msgType.GAME_OVER);
            stats = "Draw !";
            message.setData("line", stats);
            Session.connectedPlayers.get(player).sendmsg(message); 
          
            database.Players.updatedraw(player);
            int score = ServerView.server.allPlayers.get(player).getScore();
            ServerView.server.allPlayers.get(player).setScore(score+5);
            Session.connectedPlayers.get(player).notify("score", String.valueOf(score+5));
            
            ServerView.server.allPlayers.get(player).setStatus(Status.ONLINE);
            Session.connectedPlayers.get(player).notify("status", Status.ONLINE);
            ServerView.serverController.PlayersTable();
        } else {
            stats = "gameon";
            Session.connectedPlayers.get(player).sendmsg(message); 
        }
        return stats;
    }
    public int minimax(int depth, int turn) {
        if (hasXWon()) {
            return +1;
        }
        if (hasOWon()) {
            return -1;
        }

        List<Point> pointsAvailable = getAvailableStates();
        if (pointsAvailable.isEmpty()) {
            return 0;  
        }
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;

        for (int i = 0; i < pointsAvailable.size(); ++i) {
            Point point = pointsAvailable.get(i);
            if (turn == 1) {
                placeAMove(point, 1);
                int currentScore = minimax(depth + 1, 2);
                max = Math.max(currentScore, max);

                if (depth == 0) {
                    System.out.println("Score for position " + (i + 1) + " = " + currentScore);
                }
                if (currentScore >= 0) {
                    if (depth == 0) {
                        compmove = point;
                    }
                }
                if (currentScore == 1) {
                    board[point.x][point.y] = 0;
                    break;
                }
                if (i == pointsAvailable.size() - 1 && max < 0) {
                    if (depth == 0) {
                        compmove = point;
                    }
                }
            } else if (turn == 2) {
                placeAMove(point, 2);
                int currentScore = minimax(depth + 1, 1);
                min = Math.min(currentScore, min);
                if (min == -1) {
                    board[point.x][point.y] = 0;
                    break;
                }
            }
            board[point.x][point.y] = 0; 
        }
        return turn == 1 ? max : min;
    }
}