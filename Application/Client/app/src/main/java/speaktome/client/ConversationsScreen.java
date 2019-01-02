package speaktome.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ConversationsScreen extends AppCompatActivity implements Runnable{
    private Client client;
    private MySqliteDatabase sqlDB;

    private RecyclerView rv;

    private Button addChatButton;

    private String phone;

    private ArrayList<ItemDetails> chatsDetails = new ArrayList<>();
    private boolean live;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations_screen);

        Intent intent = this.getIntent();
        this.phone = intent.getStringExtra("phone");

        this.client = ClientHandler.getClient();
        this.sqlDB = new MySqliteDatabase(this, this.phone);

        this.addChatButton = (Button) findViewById(R.id.ChatsStartChatButton);

        addChatListener();

        this.initRecyclerDetails();
        this.initRecyclerView();

        this.requestNewMessages();
    }

    /*
        Changes the live variable to true when the activity is resumed (or started) and starts the thread
        Input: None
        Output: None
     */
    public void onResume() {
        super.onResume();
        this.live = true;
        this.thread = new Thread(this);
        this.thread.start();
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

    public void addChatListener()
    {
        this.addChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConversationsScreen.this, ContactsScreen.class);
                startActivity(intent);
            }
        });
    }

    /*
        Updates conversations
        Input: None
        Output: None
     */
    public void initRecyclerDetails() {
        this.chatsDetails.clear();
        this.chatsDetails.addAll(this.sqlDB.getTopMessages());
    }

    /*
        Initializes recyclerview object
        Input: None
        Output: None
     */
    public void initRecyclerView(){
        this.rv = findViewById(R.id.ChatsList);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this.chatsDetails, this, this.phone);
        this.rv.setAdapter(adapter);
        this.rv.setLayoutManager(new LinearLayoutManager(this));
    }

    /*
        The function requests new messages that have been sent to this user while he was offline
        Input: None
        Output: None
     */
    public void requestNewMessages() {
        try {
            JSONObject getMessagesRequest = new JSONObject();
            getMessagesRequest.put("code", Codes.RECEIVE_MESSAGES_CODE);
            getMessagesRequest.put("phone", this.phone);

            this.client.send(getMessagesRequest);
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    /*
        [Thread] The function handles the received messages
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
                        messages = Helper.jsonArrayToList(response.getJSONArray("messages"), this.phone);
                        this.updateChatsDetails(messages);
                    }
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
            while((response = this.client.getPushedMessage()) != null) {
                try {
                    if ((int)response.get("code") == Codes.PUSH_MESSAGE_CODE) {
                        messages = Helper.jsonArrayToList(response.getJSONArray("messages"), this.phone);
                        this.updateChatsDetails(messages);
                    }
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    /*
        Updates the sql database and the recycler view
        Input: New messages
        Output: None
     */
    private void updateChatsDetails(ArrayList<Message> messages) {
        for (Message message : messages) {
            sqlDB.insertMessage(message);
        }
        this.initRecyclerDetails();
        this.rv.getAdapter().notifyDataSetChanged();
    }
}
