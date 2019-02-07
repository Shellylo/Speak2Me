package speaktome.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
    private void initRecyclerDetails() {
        System.out.println("PRINTING: CHECK");
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
