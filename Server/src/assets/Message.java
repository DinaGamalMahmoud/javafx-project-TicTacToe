
package assets;

import java.io.Serializable;
import java.util.HashMap;


public class Message implements Serializable{
    private msgType type;
    private HashMap<String,String> data;
    
    public Message(msgType type){
        this.type = type;
        data = new HashMap<String,String>();
    }
    public Message(msgType type, String key, String value){
        this.type = type;
        data = new HashMap<String,String>();
        data.put(key, value);
    }
    public void setType(msgType type){
        this.type = type;
    }
    public msgType getType(){ 
        return type; 
    }
    public void setData(String key, String value){
        data.put(key,value);
    }
    public String getData(String key){
        if(data.containsKey(key))
            return data.get(key);
        else
            return null;
    }
}
