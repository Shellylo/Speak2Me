package speaktome.client;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class ConversationsScreen extends ContactsListScreen{
    private boolean created; //Temporary solution for duplicate items in recycler view

    private Button addChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations_screen);

        this.created = true;

        this.addChatButton = (Button) findViewById(R.id.ChatsStartChatButton);

        this.contactsDetails = new ArrayList<ContactChatDetails>();
        this.initRecyclerDetails();
        this.rv = findViewById(R.id.ChatsList);
        initRecyclerView();
        registerForContextMenu(this.rv); // Register the recycler view to context menu

        addChatListener();
        requestNewMessages();
    }

    public void onResume() {
        super.onResume();
        if(!created) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ConversationsScreen.this.initRecyclerDetails();
                    ConversationsScreen.this.rv.getAdapter().notifyDataSetChanged();
                }
            });
        }
        else {
            created = false;
        }
    }

    /*
        Initializes recyclerview object (override, in order to add costume adapter)
        Input: None
        Output: None
     */
    protected void initRecyclerView(){
        ConversationsScreenRecyclerViewAdapter adapter = new ConversationsScreenRecyclerViewAdapter(this.contactsDetails, this, this.srcPhone, this.sqlDB);
        this.rv.setAdapter(adapter);
        this.rv.setLayoutManager(new LinearLayoutManager(this));
    }

    private void addChatListener()
    {
        this.addChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConversationsScreen.this, ContactsScreen.class);
                intent.putExtra("src_phone", ConversationsScreen.this.srcPhone);
                startActivity(intent);
            }
        });
    }

    /*
        Updates conversations
        Input: None
        Output: None
     */
    protected void initRecyclerDetails() {
        this.contactsDetails.clear();
        ArrayList<ContactChatDetails> messages = this.sqlDB.getTopMessages();
        ArrayList<ContactChatDetails> contacts = super.getContacts();
        for (int i = 0; i < messages.size(); i++) {
            for(int k = 0; k < contacts.size(); k++) {
                if(messages.get(i).getContactPhone().equals(contacts.get(k).getContactPhone())) {
                    messages.get(i).setContactName(contacts.get(k).getContactName());
                    break;
                }
            }
        }
        this.contactsDetails.addAll(messages);
    }

    /*
        The function requests new messages that have been sent to this user while he was offline
        Input: None
        Output: None
     */
    private void requestNewMessages() {
        try {
            JSONObject getMessagesRequest = new JSONObject();
            getMessagesRequest.put("code", Codes.RECEIVE_MESSAGES_CODE);
            getMessagesRequest.put("phone", this.srcPhone);

            this.client.send(getMessagesRequest);
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    /*
        Updates the sql database and the recycler view
        Input: New messages
        Output: None
     */
    protected void updateMessages(ArrayList<Message> messages) {
        super.updateMessages(messages);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConversationsScreen.this.initRecyclerDetails();
                ConversationsScreen.this.rv.getAdapter().notifyDataSetChanged();
            }
        });
    }

}
