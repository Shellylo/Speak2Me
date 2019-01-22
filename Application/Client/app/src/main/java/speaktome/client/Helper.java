package speaktome.client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Helper {

    /*
        Function converts JSONArray to array of messages
        Input: the JSONArray
        Output: the array list of messages
     */
    public static ArrayList<Message> jsonArrayToList(JSONArray messages, String myPhone, boolean isInChat) {
        ArrayList<Message> ret = new ArrayList<Message>();
        JSONObject jsonMessage;
        boolean isMine;
        Message message;
        for(int i = 0; i < messages.length(); i++) {
            try {
                jsonMessage = new JSONObject(messages.get(i).toString()); //Get json object of a message
                isMine = myPhone.equals(jsonMessage.get("src_phone")); // Check if source is mine
                message = new Message(-1, isMine ? (String)jsonMessage.get("dst_phone") : (String)jsonMessage.get("src_phone"), isMine, (String)jsonMessage.get("content"), isInChat); // Create the message object
                ret.add(message);
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
        return ret;
    }

}
