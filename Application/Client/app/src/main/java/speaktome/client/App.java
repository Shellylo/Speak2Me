package speaktome.client;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_ID = "serviceChannel";

    private ConnectionChangeReceiver connReceiver = new ConnectionChangeReceiver();

    @Override
    public void onCreate() {
        super.onCreate();

        // Register receiver to listen to network/wifi connection
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connReceiver, filter);

        createServiceNotificationChannel();
    }

    private void createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
