package speaktome.client;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class ReconnectScreen extends AppCompatActivity {

    public static boolean isCreated;
    private ConnectionChangeReceiver connReceiver = new ConnectionChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconnect_screen);

        ReconnectScreen.isCreated = true;

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
                ReconnectScreen.isCreated = false; // Screen is about to be destroyed
                Intent intent = new Intent(ReconnectScreen.this, StartScreen.class);
                finish();
                startActivity(intent);
            }
        };
        waitForConnection.start();

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(this.connReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(this.connReceiver);
    }

}
