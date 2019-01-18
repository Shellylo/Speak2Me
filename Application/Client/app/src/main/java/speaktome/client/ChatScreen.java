package speaktome.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class ChatScreen extends CommunicationScreen{
    private String dstPhone;
    private String dstName;

    private ScrollView scrollScreen;
    private LinearLayout chatLayout;

    private Button recordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        // Receive phones and destination name (received from previous screen)
        Intent intent = this.getIntent();
        this.dstPhone = intent.getStringExtra("dst_phone");
        this.dstName = intent.getStringExtra("dst_name");

        // Set widgets
        this.scrollScreen = (ScrollView) findViewById(R.id.ChatMessages);
        this.chatLayout = (LinearLayout) findViewById(R.id.ChatLayout);;
        this.recordButton = (Button) findViewById(R.id.ChatRecordButton);

        // Load messages and activate record button listener
        initMessages(); // Display messages history of current chat
        recordListener();

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
    protected void updateMessages(ArrayList<Message> messages)
    {
        super.updateMessages(messages);
        for (Message message : messages)
        {
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
}
