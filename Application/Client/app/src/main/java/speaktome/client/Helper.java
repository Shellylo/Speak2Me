package speaktome.client;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Helper {

    /*
        Function converts JSONArray to array of messages
        Input: the JSONArray
        Output: the array list of messages
     */
    public static ArrayList<Message> jsonArrayToList(JSONArray messages, String myPhone) {
        ArrayList<Message> ret = new ArrayList<Message>();
        JSONObject jsonMessage;
        boolean isMine;
        Message message;
        for(int i = 0; i < messages.length(); i++) {
            try {
                jsonMessage = new JSONObject(messages.get(i).toString()); //Get json object of a message
                isMine = myPhone.equals(jsonMessage.get("src_phone")); // Check if source is mine
                message = new Message(isMine ? (String)jsonMessage.get("dst_phone") : (String)jsonMessage.get("src_phone"), isMine, (String)jsonMessage.get("content")); // Create the message object
                ret.add(message);
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
        return ret;
    }

}
