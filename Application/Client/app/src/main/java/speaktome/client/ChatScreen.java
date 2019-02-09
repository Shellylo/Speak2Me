package speaktome.client;

import android.content.Intent;
import android.icu.text.AlphabeticIndex;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class ChatScreen extends CommunicationScreen{
    private String dstPhone;
    private String dstName;

    private ScrollView scrollScreen;
    private LinearLayout chatLayout;

    private TextView chatTitle;
    private EditText inputText;

    private Button activeButton;
    private Button recordButton;
    private Button sendButton;
    private Button timerButton;
    private ImageButton recordedMessagesButton;

    private RecordTask recordTask;

    private boolean stopRecord;

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
        this.activeButton = this.recordButton;
        this.sendButton = (Button) findViewById(R.id.ChatSendButton);
        this.sendButton.setClickable(false);
        this.timerButton = (Button) findViewById(R.id.ChatTimerButton);
        this.timerButton.setClickable(false);
        this.recordedMessagesButton = (ImageButton) findViewById(R.id.ChatRecordedMessagesButton);

        // Activate listeners
        recordListener();
        textListener();
        sendButtonListener();
        recordedMessagesListener();
        timerListener();
    }

    public void onResume()
    {
        super.onResume();

        // Reload screen with updated messages
        this.chatLayout.removeAllViews(); // clear previous messages (in order to update screen)
        initMessages(); // load messages
    }

    public void onPause()
    {
        super.onPause();
        if(this.recordTask != null) {
            this.recordTask.cancel(true);
        }
    }


    /*
        Display old messages saved in db
        Input: None
        Output: None
     */
    private void initMessages()
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
    private void addMessage(final String content, final Boolean isMine)
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

                forceScrollDown();
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
        Forces screen to scroll down
        Input: None
        Output: None
     */
    private void forceScrollDown() {
        this.scrollScreen.post(new Runnable() {
            @Override
            public void run() {
                ChatScreen.this.scrollScreen.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void sendRecord() {
        try {

            File file = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + RecordTask.AUDIO_PATH);
            int size = (int) file.length();
            byte[] bytes = new byte[size];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
            String content = Base64.encodeToString(bytes, Base64.DEFAULT);

            // Prepare audio message request
            JSONObject sendRecordReq = new JSONObject();
            sendRecordReq.put("code", Codes.SPEECH_TO_TEXT_CODE);
            sendRecordReq.put("src_phone", ChatScreen.this.srcPhone);
            sendRecordReq.put("dst_phone", ChatScreen.this.dstPhone);
            sendRecordReq.put("content", content);

            // Send message request
            ChatScreen.this.client.send(sendRecordReq);
        }
        catch (Exception e) {
            System.out.println(e);
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
                ChatScreen.this.recordTask = new RecordTask();
                ChatScreen.this.recordTask.execute();
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

    private void timerListener() {
        this.timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatScreen.this.stopRecord = true;
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
                if(ChatScreen.this.activeButton != ChatScreen.this.timerButton) {
                    if (s.length() != 0) { // Input was inserted, record button invisible
                        updateActiveButton(ChatScreen.this.sendButton);
                    } else { // Input box is empty, record button visible
                        updateActiveButton(ChatScreen.this.recordButton);
                    }
                }
                else if(s.length() != 0) {
                    ChatScreen.this.inputText.setText("");
                }
            }
        });

    }

    /*
        Function changes the active button
        Input: True if record is the active button, false otherwise
        Output: None
     */
    private void updateActiveButton(Button newButton)
    {
        updateButtonState(this.activeButton, false);
        this.activeButton = newButton;
        updateButtonState(this.activeButton, true);
    }

    /*
        Function updates deactivated button (removes from screen and disable clickable)
        Input: Button to update, does the button active
        Output: None
     */
    private void updateButtonState(Button button, boolean isActive)
    {
        button.setVisibility(isActive ? View.VISIBLE : View.INVISIBLE);
        button.setClickable(isActive);
    }

    /*
        Function listens recorded messages button button. When clicked, start recorded messages screen
        Input: None
        Output: None
     */
    private void recordedMessagesListener()
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                this.inputText.setText(data.getStringExtra("message"));
            }
        }
    }

    private class RecordTask extends AsyncTask<Void, Integer, Void> {

        private static final String AUDIO_PATH = "/messageRecord.m4a";
        private static final int MAX_RECORD_LENGTH_IN_MILLISECONDS = 20000;

        private MediaRecorder recorder;

        @Override
        protected void onPreExecute() {
            ChatScreen.this.stopRecord = false;
            updateActiveButton(ChatScreen.this.timerButton);
            ChatScreen.this.timerButton.setText("00");

            // Configure audio recorder settings
            this.recorder = new MediaRecorder();
            this.recorder.setAudioSource(MediaRecorder.AudioSource.MIC); // Audio source (microphone)
            this.recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // Audio format (m4a)
            this.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            this.recorder.setOutputFile(Environment.getExternalStorageDirectory() // Audio will be saved in this path
                                       .getAbsolutePath() + RecordTask.AUDIO_PATH);
        }

        /*
            Record message and update timer accordingly.
            Recording stops after 20 seconds /
            Input:
         */
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Start record
                this.recorder.prepare();
                this.recorder.start();

                long startTime = Calendar.getInstance().getTimeInMillis();
                long difference = 0;

                do {
                    difference = Calendar.getInstance().getTimeInMillis() - startTime;
                    if(difference/1000 != Integer.parseInt(ChatScreen.this.timerButton.getText().toString())) {
                        publishProgress((int)difference/1000);
                    }
                }
                while (!isCancelled() && !stopRecord && difference <= RecordTask.MAX_RECORD_LENGTH_IN_MILLISECONDS);

                this.recorder.stop();
                this.recorder.release();
            }
            catch (Exception e) {
                System.out.println(e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            ChatScreen.this.timerButton.setText(String.format("%02d", values[0]));
        }

        @Override
        protected void onPostExecute(Void result) {
            sendRecord();
            updateActiveButton(ChatScreen.this.recordButton);
        }

        @Override
        protected void onCancelled(Void result) {
            updateActiveButton(ChatScreen.this.recordButton);
        }

    }

}
