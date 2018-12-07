package speaktome.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.ArrayList;

public class ConversationsScreen extends AppCompatActivity {

    private ArrayList<ItemDetails> chatsDetails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations_screen);

        initRecyclerDetails();
        initRecyclerView();

    }

    public void initRecyclerDetails() {
        this.chatsDetails.add(new ItemDetails((ImageView) findViewById(R.id.LayoutContactImage), "נתנאל", "0547379990", "מה קורה"));
        this.chatsDetails.add(new ItemDetails((ImageView) findViewById(R.id.LayoutContactImage), "אני", "0547767886", "אני בודקת איך זה נראה עם הודעה ארוכה"));
    }

    public void initRecyclerView(){
        RecyclerView chats = findViewById(R.id.ChatsList);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this.chatsDetails, this);
        chats.setAdapter(adapter);
        chats.setLayoutManager(new LinearLayoutManager(this));
    }
}
