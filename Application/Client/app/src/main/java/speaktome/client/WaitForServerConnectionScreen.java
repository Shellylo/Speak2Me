package speaktome.client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WaitForServerConnectionScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_server_connection_screen);

        Thread waitForServer = new Thread() {

            @Override
            public void run() {
                Client client = ClientHandler.getClient(); // When client is created, waiting to connect server

                // Finish screen and continue to StartScreen (login screen)
                Intent intent = new Intent(WaitForServerConnectionScreen.this, StartScreen.class);
                finish();
                startActivity(intent);
            }

        };
        waitForServer.start();

    }
}
