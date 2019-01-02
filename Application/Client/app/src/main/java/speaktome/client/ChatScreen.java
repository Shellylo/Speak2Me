package speaktome.client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ChatScreen extends AppCompatActivity implements Runnable {
    private Client client;
    private MySqliteDatabase sqlDB;

    private String srcPhone;
    private String dstPhone;
    private String dstName;

    private ScrollView scrollScreen;
    private LinearLayout chatLayout;

    private Button recordButton;

    private Thread getMessagesThread;
    private boolean live;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        // Receive phones and destination name (received from previous screen)
        Intent intent = this.getIntent();
        this.srcPhone = intent.getStringExtra("src_phone");
        this.dstPhone = intent.getStringExtra("dst_phone");
        this.dstName = intent.getStringExtra("dst_name");

        // Set client (server connection) and sqlite database
        this.client = ClientHandler.getClient();
        this.sqlDB = new MySqliteDatabase(this, this.srcPhone);

        // Set widgets
        this.scrollScreen = (ScrollView) findViewById(R.id.ChatMessages);
        this.chatLayout = (LinearLayout) findViewById(R.id.ChatLayout);;
        this.recordButton = (Button) findViewById(R.id.ChatRecordButton);

        // Load messages and activate record button listener
        initMessages(); // Display messages history of current chat
        recordListener();

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
        Display old messages saved in db
        Input: None
        Output: None
     */
    public void initMessages()
    {
        ArrayList<Message> messages = this.sqlDB.getMessages(this.dstPhone);
        for (Message message : messages)
        {
            addMessage(message.getContent(), message.isMine());
        }
    }

    /*
        Push new messages at the bottom, if mine on the right side, otherwise on the left
        TODO: start display from the bottom of the layout
        Input: the message, is mine
        Output: None
     */
    public void addMessage(final String content, final Boolean isMine)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView messageText = new TextView(ChatScreen.this);
                messageText.setText(content);
                if (isMine)
                {
                    messageText.setGravity(Gravity.RIGHT);
                }
                else
                {
                    messageText.setGravity(Gravity.LEFT);
                }
                ChatScreen.this.chatLayout.addView(messageText);
            }
        });

    }

    /*
        Function updates text messages in current conversation (and adds each message to sql database)
        Input: Messages to insert
        Output: None
     */
    public void updateMessages(ArrayList<Message> messages)
    {
        for (Message message : messages)
        {
            this.sqlDB.insertMessage(message);
            if (message.getPhone().equals(this.dstPhone))
            {
                addMessage(message.getContent(), message.isMine());
            }
        }
    }

    /*
        Function listens to sign up button. When clicked, receives record and sends it to server
        Input: None
        Output: None
     */
    public void recordListener()
    {
        this.recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject sendRecordReq = new JSONObject();
                    sendRecordReq.put("code", Codes.SEND_VOICE_MESSAGE_CODE);
                    sendRecordReq.put("src_phone", ChatScreen.this.srcPhone);
                    sendRecordReq.put("dst_phone", ChatScreen.this.dstPhone);
                    sendRecordReq.put("content", "Very looooooooooooooooooooooooooooooooooooooong message wow wow coooooooool!! RANDOM NUMBER -- " + (int)(Math.random() * 50 + 1));

                    ChatScreen.this.client.send(sendRecordReq);
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
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
            while((response = this.client.getConversationFlow()) != null) {
                try {
                    if ((int)response.get("code") == Codes.RECEIVE_MESSAGES_CODE || (int)response.get("code") == Codes.SEND_VOICE_MESSAGE_CODE) {
                        messages = Helper.jsonArrayToList(response.getJSONArray("messages"), this.srcPhone);
                        this.updateMessages(messages);
                    }
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
            while((response = this.client.getPushedMessage()) != null) {
                try {
                    if ((int)response.get("code") == Codes.PUSH_MESSAGE_CODE) {
                        messages = Helper.jsonArrayToList(response.getJSONArray("messages"), this.srcPhone);
                        this.updateMessages(messages);
                    }
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }
}
