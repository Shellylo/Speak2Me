package speaktome.client;

public class Codes {
    //Not Connected Operations
    static final int SIGN_UP_CODE = 100;
    static final int LOG_IN_CODE = 101;

    //Connected Operations
    static final int RECEIVE_MESSAGES_CODE = 200;
    static final int SEND_TEXT_MESSAGE_CODE = 201;
    static final int PUSH_MESSAGE_CODE = 202;
    static final int SPEECH_TO_TEXT_CODE = 203;

    //Errors
    static final int GENERAL_ERROR_CODE = 0;
    static final int DETAILS_MISSING_ERROR_CODE = 1;
    static final int PHONE_EXISTS_ERROR_CODE = 2;
    static final int ALREADY_CONNECTED_ERROR_CODE = 3;
    static final int INCORRECT_LOGIN_ERROR_CODE = 4;
    static final int SOURCE_INVALID_ERROR_CODE = 5;
    static final int DESTINATION_UNREACHABLE_ERROR_CODE = 6;
    static final int INCORRECT_SIGNUP_DETAILS_ERROR_CODE = 7;
}
