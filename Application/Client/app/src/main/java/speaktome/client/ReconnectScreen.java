package speaktome.client;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

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

    /*
        When back pressed, changing IP option appears (password required)
        Input: None
        Output: None
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_menu_compass)
                .setTitle("Change Destination IP Address")
                .setView(R.layout.layout_change_ip)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText ipEditText = ((AlertDialog)dialog).findViewById(R.id.NewIp);
                        String ipAddress = ipEditText.getText().toString();
                        EditText passwordEditText = ((AlertDialog)dialog).findViewById(R.id.Password);
                        String password = passwordEditText.getText().toString();
                        if (isValidIPAddress(ipAddress)) {
                            if (isPasswordCorrect(password)) {
                                ConnectToServer.setIP(ipAddress);
                                Toast.makeText(ReconnectScreen.this, "IP Changed", Toast.LENGTH_LONG).show();
                            }
                            else { // Incorrect password
                                Toast.makeText(ReconnectScreen.this, "Incorrect Password", Toast.LENGTH_LONG).show();
                            }
                        }
                        else { // Invalid IP address
                            Toast.makeText(ReconnectScreen.this, "Invalid IP Address", Toast.LENGTH_LONG).show();
                        }

                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /*
        Function checks if password is matching
        Input: Received password
        Output: True if password is correct, false otherwise
     */
    private boolean isPasswordCorrect(String password) {
        final String secretPassword = "3124supercoollongpassword";
        return password.equals(secretPassword);
    }

    /*
        Function checks if the IP address received is valid
        Valid IP address:
        * 4 numbers seperated by .
        * each number between 0-255
        Input: IP address
        Output: True if IP address is valid, false otherwise
     */
    private boolean isValidIPAddress(String IPAddress) {
        boolean valid = true;
        int num = 0;
        String[] numbers = IPAddress.split("\\.");
        if (numbers.length == 4) {
            for (int i = 0; i < 4 && valid; i++) {
                try {
                    num = Integer.parseInt(numbers[i]);
                    valid = num >= 0 && num <= 255;
                }
                catch (Exception e) { // Current element is not valid integer num
                    valid = false;
                }
            }
        }
        else { // There aren't 4 numbers divided by .
            valid = false;
        }
        return valid;
    }
}
