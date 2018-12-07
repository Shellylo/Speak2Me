package speaktome.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class StartScreen extends AppCompatActivity {
    private Client client;

    private Button signUpButton;
    private Button logInButton;
    private EditText phoneNumber;
    private EditText password;
    private TextView detailsMissingError;
    private TextView alreadyConnectedError;
    private TextView incorrectLogInError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        // Set client (server connection)
        this.client = ClientHandler.getClient();

        // Set Buttons
        this.signUpButton = (Button)findViewById(R.id.LogInSignUpButton);
        this.logInButton = (Button)findViewById(R.id.LogInLogInButton);
        this.phoneNumber = (EditText)findViewById(R.id.LogInPhoneBox);
        this.password = (EditText)findViewById(R.id.LogInPasswordBox);
        this.detailsMissingError = (TextView)findViewById(R.id.LogInDetailsMissingError);
        this.alreadyConnectedError = (TextView)findViewById(R.id.LogInAlreadyConnectedError);
        this.incorrectLogInError = (TextView)findViewById(R.id.LogInIncorrectLoginError);

        // Set listeners
        this.signUpListener();
        this.logInListener();
        /*try {
            JSONObject json = new JSONObject("{\"code\": 100, \"phone\": \"0547768888\", \"password\":\"coolest\", \"name\":\"Nova\"}");
            client.sendAndRecieve(json);
        }
        catch (Exception e) {
            System.out.println(e);
        }*/

    }

    public void signUpListener() {
        this.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartScreen.this.detailsMissingError.setVisibility(View.INVISIBLE);
                StartScreen.this.alreadyConnectedError.setVisibility(View.INVISIBLE);
                StartScreen.this.incorrectLogInError.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(StartScreen.this, SignupScreen.class);
                startActivity(intent);
            }
        });
    }

    public void logInListener() {
        this.logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject logInRequest = new JSONObject();
                    logInRequest.put("code", Codes.LOG_IN_CODE);
                    logInRequest.put("phone", StartScreen.this.phoneNumber.getText().toString());
                    logInRequest.put("password", StartScreen.this.password.getText().toString());
                    JSONObject signUpResponse = StartScreen.this.client.sendAndRecieve(logInRequest);
                    switch ((int)signUpResponse.get("code"))
                    {
                        case Codes.LOG_IN_CODE:
                            //Intent intent = new Intent(StartScreen.this, SCREEN.class);
                            //startActivity(intent);
                            //finish();
                            break;
                        case Codes.DETAILS_MISSING_ERROR_CODE:
                            StartScreen.this.alreadyConnectedError.setVisibility(View.INVISIBLE);
                            StartScreen.this.incorrectLogInError.setVisibility(View.INVISIBLE);
                            StartScreen.this.detailsMissingError.setVisibility(View.VISIBLE);
                            break;
                        case Codes.ALREADY_CONNECTED_ERROR_CODE:
                            StartScreen.this.detailsMissingError.setVisibility(View.INVISIBLE);
                            StartScreen.this.incorrectLogInError.setVisibility(View.INVISIBLE);
                            StartScreen.this.alreadyConnectedError.setVisibility(View.VISIBLE);
                            break;
                        case Codes.INCORRECT_LOGIN_ERROR_CODE:
                            StartScreen.this.detailsMissingError.setVisibility(View.INVISIBLE);
                            StartScreen.this.alreadyConnectedError.setVisibility(View.INVISIBLE);
                            StartScreen.this.incorrectLogInError.setVisibility(View.VISIBLE);
                            break;
                    }
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }
}
