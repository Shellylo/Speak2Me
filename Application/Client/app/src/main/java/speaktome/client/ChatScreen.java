package speaktome.client;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

    private TextView chatTitle;
    private EditText inputText;

    private Button recordButton;
    private Button sendButton;
    private ImageButton recordedMessagesButton;

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
        this.chatLayout = (LinearLayout) findViewById(R.id.ChatLayout);
        this.chatTitle = (TextView) findViewById(R.id.ChatNameTitle);
        this.chatTitle.setText(this.dstName); // Insert contact name into chat title
        this.inputText = (EditText) findViewById(R.id.ChatMessageInput);
        this.recordButton = (Button) findViewById(R.id.ChatRecordButton);
        this.sendButton = (Button) findViewById(R.id.ChatSendButton);
        this.recordedMessagesButton = (ImageButton) findViewById(R.id.ChatRecordedMessagesButton);
        this.sendButton.setClickable(false);

        initMessages(); // Display messages history of current chat

        // Activate listeners
        recordListener();
        textListener();
        sendButtonListener();
        recordedMessagesListener();
    }

    /*
        Display old messages saved in db
        Input: None
        Output: None
     */
    public void initMessages()
    {
        ArrayList<Message> messages = this.sqlDB.getMessages(this.dstPhone, true);
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
            if (message.getPhone().equals(this.dstPhone) && message.isInChat())
            {
                addMessage(message.getContent(), message.isMine());
            }
        }
    }

    /*
        Function listens record button. When clicked, receives record and sends it to server
        Input: None
        Output: None
     */
    private void recordListener()
    {
        this.recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject sendRecordReq = new JSONObject();
                    sendRecordReq.put("code", Codes.SPEECH_TO_TEXT_CODE);
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
        Function listens to send message button.
        When clicked, receives typed text in text box and sends it to server.
        Input: None
        Output: None
     */
    private void sendButtonListener()
    {
        this.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject sendRecordReq = new JSONObject();
                    sendRecordReq.put("code", Codes.SEND_TEXT_MESSAGE_CODE);
                    sendRecordReq.put("src_phone", ChatScreen.this.srcPhone);
                    sendRecordReq.put("dst_phone", ChatScreen.this.dstPhone);
                    sendRecordReq.put("content", ChatScreen.this.inputText.getText());

                    ChatScreen.this.client.send(sendRecordReq); // Send text message to server
                    ChatScreen.this.inputText.setText(""); // Clear typed text from text box in screen
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    /*
        Check if there is text typed, and change the button according to it (from record to send)
        Input: None
        Output: None
     */
    private void textListener()
    {
        this.inputText.addTextChangedListener(new TextWatcher() {

            // Override abstract functions
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            // Check if input has been inserted
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0) { // Input was inserted, record button invisible
                    changeButtonsState(false);
                }
                else { // Input box is empty, record button visible
                    changeButtonsState(true);
                }
            }
        });

    }

    private void changeButtonsState(boolean isRecordClickable)
    {
        this.recordButton.setClickable(isRecordClickable);
        this.sendButton.setClickable(!isRecordClickable);
        this.recordButton.setVisibility(isRecordClickable ? View.VISIBLE : View.INVISIBLE);
    }

    /*
        Function listens recorded messages button button. When clicked, start recorded messages screen
        Input: None
        Output: None
     */
    public void recordedMessagesListener()
    {
        this.recordedMessagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatScreen.this, RecordedMessagesScreen.class);
                intent.putExtra("src_phone", ChatScreen.this.srcPhone);
                intent.putExtra("dst_phone", ChatScreen.this.dstPhone);
                startActivityForResult(intent,0);
            }
        });
    }

    /*
        Function waits for result from recorded messages activity
        Input: request code, result code, and data
        Output: None
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                this.inputText.setText(data.getStringExtra("message"));
            }
        }
    }
}
