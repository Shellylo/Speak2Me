package speaktome.client;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONObject;

public class StartScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        try {
            Client client = new Client();
            JSONObject json = new JSONObject("{\"code\": 100, \"phone\": \"0547768888\", \"password\":\"coolest\", \"name\":\"Nova\"}");
            client.sendAndRecieve(json);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
