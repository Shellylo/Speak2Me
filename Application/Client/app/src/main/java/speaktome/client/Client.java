package speaktome.client;

import android.icu.util.Output;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Queue;

public class Client {
    private ConnectToServer serverConnection;
    private InputMessages incomingMessages;
    private OutputMessages outgoingMessages;

    public Client() {
        this.serverConnection = new ConnectToServer();
        this.outgoingMessages = new OutputMessages(this.serverConnection.getOut());
        this.incomingMessages = new InputMessages(this.serverConnection.getIn());
    }

    public void send(JSONObject request) {
        this.outgoingMessages.addConversationFlow(request); //Send request
    }

    public JSONObject getConversationFlow() {
        return this.incomingMessages.getConversationFlow();
    }

    public JSONObject getPushedMessages() {
        return this.incomingMessages.getPushMessage();
    }
}
