package speaktome.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class ConnectionChangeReceiver extends BroadcastReceiver {

    private static boolean isNetworkActive = true;

    /*
        Called when there is change in network connection.
        No connection available - trying to reconnect (and updates GUI accordingly)
        Reconnection completed - updates GUI back to login screen
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectionChangeReceiver.isNetworkActive = !(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false));
        if(!ConnectionChangeReceiver.isNetworkActive && !ReconnectScreen.isCreated) {

            ClientHandler.deleteClient();

            Intent activityIntent = new Intent(context, ReconnectScreen.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Closes all previous activities
            context.startActivity(activityIntent);
        }
    }

    /*
        Returns the value of the variable isNetworkActive
        Input: None
        Output: True if there is network connection, false otherwise
     */
    public static boolean isNetworkActive() {
        return ConnectionChangeReceiver.isNetworkActive;
    }
}