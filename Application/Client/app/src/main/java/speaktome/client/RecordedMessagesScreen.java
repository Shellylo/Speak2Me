package speaktome.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecordedMessagesScreen extends CommunicationScreen {
    private String dstPhone;

    private ScrollView scrollview;
    private LinearLayout messagesLayout;

    private ImageButton backButton;

    private View.OnClickListener textViewListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorded_messages_screen);

        Intent intent = this.getIntent();
        this.dstPhone = intent.getStringExtra("dst_phone");

        this.scrollview = (ScrollView) findViewById(R.id.RecordedMessagesScrollView);
        this.messagesLayout = (LinearLayout) findViewById(R.id.RecordedMessagesLayout);
        this.backButton = (ImageButton) findViewById(R.id.RecordedMessagesChatButton);

        this.textViewListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView message = (TextView)v;
                Intent data = new Intent();
                data.putExtra("message", message.getText().toString());
                setResult(RESULT_OK, data);
                finish();
            }
        };

        initMessages();
        backListener();
    }

    /*
        Display recorded messages saved in db
        Input: None
        Output: None
     */
    public void initMessages()
    {
        ArrayList<Message> messages = this.sqlDB.getMessages(this.dstPhone, false);
        for (Message message : messages)
        {
            addMessage(message.getContent());
        }
    }

    /*
        Push new messages at the bottom
        TODO: start display from the bottom of the layout
        Input: the message, is mine
        Output: None
     */
    public void addMessage(final String content)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView messageText = new TextView(RecordedMessagesScreen.this);
                messageText.setText(content);
                messageText.setClickable(true);

                messageText.setOnClickListener(RecordedMessagesScreen.this.textViewListener);
                RecordedMessagesScreen.this.messagesLayout.addView(messageText);
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
            if (message.getPhone().equals(this.dstPhone) && !message.isInChat())
            {
                addMessage(message.getContent());
            }
        }
    }

    public void backListener() {
        this.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
