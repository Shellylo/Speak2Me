package speaktome.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        TextView tv = new TextView(this);
        tv.setText("Test");
        LinearLayout chatLayout = (LinearLayout) findViewById(R.id.ChatLayout);
        chatLayout.addView(tv);
    }
}
