package speaktome.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class SignupScreen extends AppCompatActivity {
    private Client client;

    private Button signUpButton;
    private EditText phoneNumber;
    private EditText password;
    private EditText displayName;
    private TextView detailsMissingError;
    private TextView phoneTakenError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_screen);

        // Set client (server connection)
        this.client = ClientHandler.getClient();

        // Set widgets
        this.signUpButton = (Button)findViewById(R.id.SignUpSignUpButton);
        this.phoneNumber = (EditText)findViewById(R.id.SignUpPhoneBox);
        this.password = (EditText)findViewById(R.id.SignUpPasswordBox);
        this.displayName = (EditText)findViewById(R.id.SignUpNameBox);
        this.detailsMissingError = (TextView)findViewById(R.id.SignUpDetailsMissingError);
        this.phoneTakenError = (TextView)findViewById(R.id.SignUpPhoneTakenError);

        // Set listeners
        this.signUpListener();
    }

    public void signUpListener()
    {
        this.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                JSONObject signUpRequest = new JSONObject();
                signUpRequest.put("code", Codes.SIGN_UP_CODE);
                signUpRequest.put("phone", SignupScreen.this.phoneNumber.getText().toString());
                signUpRequest.put("password", SignupScreen.this.password.getText().toString());
                signUpRequest.put("name", SignupScreen.this.displayName.getText().toString());

                SignupScreen.this.client.send(signUpRequest);
                JSONObject signUpResponse = SignupScreen.this.client.getConversationFlow();
                while (signUpResponse == null) {
                    signUpResponse = SignupScreen.this.client.getConversationFlow();
                }

                switch ((int)signUpResponse.get("code"))
                {
                    case Codes.SIGN_UP_CODE:
                        finish();
                        break;
                    case Codes.DETAILS_MISSING_ERROR_CODE:
                        SignupScreen.this.phoneTakenError.setVisibility(View.INVISIBLE);
                        SignupScreen.this.detailsMissingError.setVisibility(View.VISIBLE);
                        break;
                    case Codes.PHONE_EXISTS_ERROR_CODE:
                        SignupScreen.this.detailsMissingError.setVisibility(View.INVISIBLE);
                        SignupScreen.this.phoneTakenError.setVisibility(View.VISIBLE);
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
