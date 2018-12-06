package speaktome.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.json.JSONObject;

public class StartScreen extends AppCompatActivity {
    private Client client;

    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        // Set client (server connection)
        this.client = ClientHandler.getClient();

        // Set Buttons
        this.signUpButton = (Button)findViewById(R.id.LogInSignUpButton);

        // Set listeners
        this.signUpListener();
        /*try {
            JSONObject json = new JSONObject("{\"code\": 100, \"phone\": \"0547768888\", \"password\":\"coolest\", \"name\":\"Nova\"}");
            client.sendAndRecieve(json);
        }
        catch (Exception e) {
            System.out.println(e);
        }*/

    }

    public void signUpListener()
    {
        this.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartScreen.this, SignupScreen.class);
                startActivity(intent);
            }
        });
    }

}
