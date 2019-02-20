
package database;

import assets.Status;
import java.sql.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Players {
    public static DBConnection con = new DBConnection();
    public static HashMap<String, Player> allplayers() {
        HashMap<String, Player> hashmap = new HashMap<>();
        try {
            Connection conn = con.Connection();
            Statement stmt = conn.createStatement();
            String queryString = "select * from players";
            ResultSet rs = stmt.executeQuery(queryString);
            while (rs.next()) {
                Player p = new Player(rs.getString(1), rs.getInt(2), rs.getString(3));
                p.setStatus(Status.OFFLINE);
                hashmap.put(rs.getString("username"), p);
            }
            stmt.close();
            con.CloseConnection(conn);
        } catch (SQLException ex) {
        }
        return hashmap;
    }
    public static Player getplayer(String username) {
        Player player = new Player();
        try {
            Connection conn = con.Connection();
            Statement stmt = conn.createStatement();
            String queryString = "SELECT * FROM players WHERE username = '" + username + "'";
            ResultSet rs = stmt.executeQuery(queryString);
            while (rs.next()) {
                player.setUsername(rs.getString("username"));
                player.setScore(rs.getInt("score"));
            }
            stmt.close();
            con.CloseConnection(conn);
        } catch (SQLException ex) {
        }
        return player;
    }
    public static boolean isexitsed(String username) {
        try {
            Connection conn = con.Connection();
            Statement stmt = conn.createStatement();
            String queryString = "select * from players where username ='" + username + "'";
            ResultSet rs = stmt.executeQuery(queryString);
            if (rs.next()) {
                return true;
            }
            stmt.close();
            con.CloseConnection(conn);
        } catch (SQLException ex) {
            Logger.getLogger(Players.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    public static boolean signin(String username, String password) {
        boolean validAuth = false;
        if (isexitsed(username)) {
            try {
                Connection conn = con.Connection();
                Statement stmt = conn.createStatement();
                String queryString = "select * from players where username ='" + username + "' and password='" + password + "'";
                ResultSet rs = stmt.executeQuery(queryString);
                if (rs.next()) {
                    validAuth = true;
                }
                stmt.close();
                con.CloseConnection(conn);
            } catch (SQLException ex) {
                Logger.getLogger(Players.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return validAuth;
    }
    public static boolean updatewin(String username) {
        try {
            Connection conn = con.Connection();
            Statement stmt = conn.createStatement();
            String queryString = "UPDATE `players` SET `score`= score+10  WHERE username = '" + username + "' ";
            
            stmt.executeUpdate(queryString);
            stmt.close();
            con.CloseConnection(conn);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Players.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public static boolean updatedraw(String username) {
        try {
            Connection conn = con.Connection();
            Statement stmt = conn.createStatement();
            String queryString = "UPDATE `players` SET `score`= score+5  WHERE username = '" + username + "' ";
            
            stmt.executeUpdate(queryString);
            stmt.close();
            con.CloseConnection(conn);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Players.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public static synchronized boolean insertplayer(String username,String password) {
        try {
            Connection conn = con.Connection();
            Statement stmt = conn.createStatement();
            String queryString = "INSERT INTO `players` ( `username`, `score`, `password`) VALUES ('" + username + "', '" + 0 + "', '" + password + "')";
            stmt.executeUpdate(queryString);
            stmt.close();
            con.CloseConnection(conn);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Players.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    
    
    public static SavedGame checkpaused(String userName1, String userName2) {
        String sql = "SELECT * FROM SavedGame where Player1 = ? AND Player2 = ?";
        String sql2 = "Delete From SavedGame WHERE Player1 = ? AND Player2 = ?";
        SavedGame sg = new SavedGame();
        try {
            //con = this.connect();
            Connection conn = con.Connection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName1);
            pstmt.setString(2, userName2);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sg.player1 = rs.getString("Player1");
                sg.player2 = rs.getString("Player2");
                for (int i = 0; i < 9; i++) {
                    sg.cell[i] = rs.getString("cell" + (i + 1));
                }
                PreparedStatement pstmt2 = conn.prepareStatement(sql2);
                pstmt2.setString(1, userName1);
                pstmt2.setString(2, userName2);
                pstmt2.executeUpdate();

                System.out.println("Check true");
                return sg;
                //}
            }
            //} else {
            System.out.println("No SavedGame between these players");
            return null;
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("No SavedGame between these players");
        return null;
    }

    public static void pauseGame(SavedGame sg) {
        String sql = "INSERT INTO SavedGame VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try {
            //con = this.connect();
            Connection conn = con.Connection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sg.player1);
            pstmt.setString(2, sg.player2);
            for (int i = 0; i < 9; i++) {
                pstmt.setString((i + 3), sg.cell[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    
    
    
     public static void main(String[] args){
        Player p = new Player();
        p = Players.getplayer("dina");
   }
     
}
