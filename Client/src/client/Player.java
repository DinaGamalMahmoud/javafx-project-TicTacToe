
package client;


public class Player {
    private int score;
    private String status;
    private String username;
    private String password;


    public Player(){}
    public Player(String username, String password, int score) {

        this.username = username;
        this.password = password;
        this.score = score;
    }
   
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public String getUsername(){
        return username;
    }
    
    public void setScore(int score){
        this.score = score;
    }
    public int getScore(){
        return score;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public String getStatus(){
        return status;
    }
}
