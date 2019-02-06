package speaktome.client;

import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;

import java.util.ArrayList;

public class CommunicationScreen extends GeneralScreen implements Runnable{
    protected String srcPhone;

    protected MySqliteDatabase sqlDB;

    protected boolean live;
    protected Thread getMessagesThread;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        this.srcPhone = intent.getStringExtra("src_phone");

        this.sqlDB = new MySqliteDatabase(this, this.srcPhone);
    }

    /*
        Changes the live variable to true when the activity is resumed (or started) and starts the thread
        Input: None
        Output: None
     */
    public void onResume() {
        super.onResume();
        this.live = true;
        this.getMessagesThread = new Thread(this);
        this.getMessagesThread.start();
    }

    /*
        Changes the live variable to false when the activity is paused (or finished) so the thread will be terminated
        Input: None
        Output: None
     */
    public void onPause() {
        super.onPause();
        this.live = false;
    }

    /*
        [Thread] Function checks for incoming messages, and handles each one
        Input: None
        Output: None
     */
    @Override
    public void run() {
        JSONObject response;
        ArrayList<Message> messages;
        while (this.live) {
            if((response = this.client.getConversationFlow()) != null) { //while -> if
                try {
                    if ((int)response.get("code") == Codes.RECEIVE_MESSAGES_CODE || (int)response.get("code") == Codes.SEND_TEXT_MESSAGE_CODE) {
                        messages = Helper.jsonArrayToList(response.getJSONArray("messages"), this.srcPhone, true);
                        this.updateMessages(messages);
                    }
                    else if((int)response.get("code") == Codes.SPEECH_TO_TEXT_CODE) {
                        messages = Helper.jsonArrayToList(response.getJSONArray("messages"), this.srcPhone, false);
                        this.updateMessages(messages);
                    }
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
            if((response = this.client.getPushedMessage()) != null) { //while -> if
                try {
                    if ((int)response.get("code") == Codes.PUSH_MESSAGE_CODE) {
                        messages = Helper.jsonArrayToList(response.getJSONArray("messages"), this.srcPhone, true);
                        this.updateMessages(messages);
                    }
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    protected void updateMessages(ArrayList<Message> messages)
    {
        for (int i = 0; i < messages.size(); i++)
        {
            messages.get(i).setId(this.sqlDB.insertMessage(messages.get(i)));
        }
    }
}
