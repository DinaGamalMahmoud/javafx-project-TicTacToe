
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DBConnection {
    private Connection connection;
    public Connection Connection(){
        try {
           
            Class.forName("com.mysql.jdbc.Driver");
            try {
                connection = DriverManager
                        .getConnection("jdbc:mysql://localhost/tictactoedb?"
                                + "user=dina&password=Dina123@#");
            } catch (SQLException ex) {
                Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return connection;
    }
    
    
 
    public void CloseConnection(Connection connection){
          try {
              connection.close();
          } catch (SQLException ex) {
          }
    
    }
    
   
    
    
}
