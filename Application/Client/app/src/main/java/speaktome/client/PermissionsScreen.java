package speaktome.client;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class PermissionsScreen extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_USE_MICROPHONE = 0; //***CHANGE VALUE***

    private boolean contatctPermission;
    private boolean michrophonePermission;

    private Switch contatctPermissionSwitch;
    private Switch michrophonePermissionSwitch;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_screen);

        this.contatctPermissionSwitch = (Switch)findViewById(R.id.PermissionsContactsSwitch);
        this.michrophonePermissionSwitch = (Switch)findViewById(R.id.PermissionsMichrophoneSwitch);
        this.continueButton = (Button)findViewById(R.id.PermissionsContinueButton);

        this.checkPermissions();
        if(this.contatctPermission && this.michrophonePermission) {
            this.moveToNextScreen();
        }
        else {
            this.continueButton.setEnabled(false);
        }

        if(this.contatctPermission) {
            this.contatctPermissionSwitch.setChecked(true);
            this.contatctPermissionSwitch.setClickable(false);
        }
        if(this.michrophonePermission) {
            this.michrophonePermissionSwitch.setChecked(true);
            this.michrophonePermissionSwitch.setClickable(false);
        }

        this.contatctPermissionListener();
        this.michrophonePermissionListener();
        this.continueListener();
    }

    /*
        Function moves to the next screen
        Input: None
        Output: None
     */
    private void moveToNextScreen() {
        Intent intent = new Intent(PermissionsScreen.this, ConversationsScreen.class);
        intent.putExtra("phone", this.getIntent().getStringExtra("phone"));
        finish();
        startActivity(intent);
    }

    /*
        Function checks and initializes the permissions
        Input: None
        Output: None
     */
    private void checkPermissions() {
        // Check the SDK version and whether the permission is already granted or not (contacts).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            this.contatctPermission = false;
        }
        // Android version is lesser than 6.0 or the permission is already granted (contacts).
        else {
            this.contatctPermission = true;
        }
        // Check the SDK version and whether the permission is already granted or not (microphone).
        if(true) { //***CHANGE THE STATEMENT***
            this.michrophonePermission = false;
        }
        // Android version is lesser than 6.0 or the permission is already granted (microphone).
        else {
            this.michrophonePermission = true;
        }
    }

    /*
        Function updates switches accordingly to the given permissions
        Input: None
        Output: None
     */
    private void updateSwitches() {
        if(this.contatctPermission) {
            this.contatctPermissionSwitch.setChecked(true);
            this.contatctPermissionSwitch.setClickable(false);
        }
        else {
            this.contatctPermissionSwitch.setChecked(false);
        }
        if(this.michrophonePermission) {
            this.michrophonePermissionSwitch.setChecked(true);
            this.michrophonePermissionSwitch.setClickable(false);
        }
        else {
            this.michrophonePermissionSwitch.setChecked(false);
        }
        if(this.contatctPermission && this.michrophonePermission) {
            this.continueButton.setAlpha(1);
            this.continueButton.setEnabled(true);
        }
    }

    /*
        Function listens to contact permission switch
        Input: None
        Output: None
     */
    private void contatctPermissionListener() {
        this.contatctPermissionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
                    PermissionsScreen.this.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            }
        });
    }

    /*
        Function listens to microphone permission switch
        Input: None
        Output: None
     */
    private void michrophonePermissionListener() {
        this.michrophonePermissionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
                    PermissionsScreen.this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_USE_MICROPHONE);
                }
            }
        });
    }

    /*
        Function listens to continue button
        Input: None
        Output: None
     */
    private void continueListener() {
        this.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNextScreen();
            }
        });
    }

    /*
        Function receives permission result and changes the permissions accordingly
        Input: requestCode, permissions and the results
        Output: None
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    this.contatctPermission = true;
                } else {
                    this.contatctPermission = false;
                    Toast.makeText(this, "Until you grant the permission, you cannot continue", Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSIONS_REQUEST_USE_MICROPHONE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    this.michrophonePermission = true;
                } else {
                    this.michrophonePermission = false;
                    Toast.makeText(this, "Until you grant the permission, you cannot continue", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        this.updateSwitches();
    }
}