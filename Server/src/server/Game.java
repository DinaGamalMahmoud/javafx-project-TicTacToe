package server;

import database.SavedGame;

public class Game {
    private final String player1;
    private final String player2;
    public String[][] currentGame;
    public int incMove;
    public boolean Horizontal = true, Vertical = true, DiagonalOne = true, DiagonalTwo = true;


    public Game(String player1, String player2) {
        this.incMove = 0;
        this.player1 = player1;
        this.player2 = player2;
        currentGame = new String[3][3];
        DiagonalOne = true;
    }

    public Game(SavedGame sg) {
        this.incMove = 0;
        for(int i = 0; i<9; i++){
            if(sg.player1.equals(sg.cell[i]) || sg.player2.equals(sg.cell[i]) )
                this.incMove++;
        }
        this.player1 = sg.player1;
        this.player2 = sg.player2;
        currentGame = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int v = Integer.parseInt(sg.cell[(i * 3) + j + 1]);
                if (v != 0) {
                    if (v == 1) {
                        currentGame[i][j] = sg.player1;
                    } else if (v == 2) {
                        currentGame[i][j] = sg.player2;
                    }
                }

            }
        }
        DiagonalOne = true;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }


    public boolean validateMove(String player, int x, int y) {
        if (currentGame[x][y] == null) {
            currentGame[x][y] = player;
            return true;
        } else {
            return false;
        }
    }


    public String checkForWin(String player, int x, int y) {
        for (int i = 0; i < 3; i++) {
            if (currentGame[x][i] != player) {
                Horizontal = false;
            }
            if (currentGame[i][y] != player) {
                Vertical = false;
            }
        }
        if (x == y || x == 2 - y) {
            for (int i = 0; i < 3; i++) {
                if (currentGame[i][i] != player) {
                    DiagonalOne = false;
                }
                if (currentGame[i][2 - i] != player) {
                    DiagonalTwo = false;
                }
            }
        } else {
            DiagonalOne = false;
            DiagonalTwo = false;
        }
        if (Horizontal || Vertical || DiagonalOne || DiagonalTwo) {
            return "win";
        } else if (incMove == 8) {
            return "draw";
        } else {
            Horizontal = Vertical = DiagonalOne = DiagonalTwo = true;
            incMove++;
            return "gameOn";
        }
    }
}
