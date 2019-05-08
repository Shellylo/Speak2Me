package speaktome.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ReconnectScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconnect_screen);

        Thread waitForConnection = new Thread() {

            @Override
            public void run() {
                super.run();

                while (!ConnectionChangeReceiver.isNetworkActive()) { // Wait for connection

                    // Prevent CPU overload
                    try {
                        Thread.sleep(50);
                    }
                    catch (Exception e) {
                        System.out.println(e);
                    }
                }

                // Finish screen and return to StartScreen (login screen)
                Intent intent = new Intent(ReconnectScreen.this, StartScreen.class);
                finish();
                startActivity(intent);
            }
        };
        waitForConnection.start();

    }

}
