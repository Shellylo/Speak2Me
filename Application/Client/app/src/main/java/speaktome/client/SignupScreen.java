package speaktome.client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class SignupScreen extends ErrorDisplayerScreen {
    private Button signUpButton;
    private EditText phoneNumber;
    private EditText password;
    private EditText displayName;

    private TextView detailsMissingError;
    private TextView phoneTakenError;
    private TextView incorrectDetailsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_screen);

        // Set widgets
        this.signUpButton = (Button)findViewById(R.id.SignUpSignUpButton);
        this.phoneNumber = (EditText)findViewById(R.id.SignUpPhoneBox);
        this.password = (EditText)findViewById(R.id.SignUpPasswordBox);
        this.displayName = (EditText)findViewById(R.id.SignUpNameBox);
        this.detailsMissingError = (TextView)findViewById(R.id.SignUpDetailsMissingError);
        this.phoneTakenError = (TextView)findViewById(R.id.SignUpPhoneTakenError);
        this.incorrectDetailsError = (TextView)findViewById(R.id.SignUpIncorrectDetailsError);
        this.phoneNumber.requestFocus();

        // Set listeners
        this.signUpListener();
    }

    /*
        Function listens to sign up button, and sends sign up request (or displays error message) when clicked
        Input: None
        Output: None
     */
    private void signUpListener()
    {
        this.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                // Get sign up details (phone, password and name) into json request
                JSONObject signUpRequest = new JSONObject();
                signUpRequest.put("code", Codes.SIGN_UP_CODE);
                signUpRequest.put("phone", SignupScreen.this.phoneNumber.getText().toString());
                signUpRequest.put("password", SignupScreen.this.password.getText().toString());
                signUpRequest.put("name", SignupScreen.this.displayName.getText().toString());

                // Send sign up request
                SignupScreen.this.client.send(signUpRequest);

                // Wait for response from server
                JSONObject signUpResponse = null;
                do {
                    signUpResponse = SignupScreen.this.client.getConversationFlow();
                } while (signUpResponse == null);

                switch ((int)signUpResponse.get("code"))
                {
                    case Codes.SIGN_UP_CODE: // User sign up completed, go back to log in screen
                        finish();
                        break;
                    case Codes.DETAILS_MISSING_ERROR_CODE: // Details missing in request message
                        SignupScreen.super.updateError(SignupScreen.this.detailsMissingError);
                        break;
                    case Codes.PHONE_EXISTS_ERROR_CODE: // Phone already exists, sign up not completed
                        SignupScreen.super.updateError(SignupScreen.this.phoneTakenError);
                        break;
                    case Codes.INCORRECT_SIGNUP_DETAILS_ERROR_CODE:
                        SignupScreen.super.updateError(SignupScreen.this.incorrectDetailsError);
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
