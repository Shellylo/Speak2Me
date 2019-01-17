package speaktome.client;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class GeneralScreen extends AppCompatActivity {
    protected Client client;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set client (server connection)
        this.client = ClientHandler.getClient();
    }
}
