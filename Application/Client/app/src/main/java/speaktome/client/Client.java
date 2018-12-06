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

    public JSONObject sendAndRecieve(JSONObject request) {
        this.outgoingMessages.setConversationFlow(request); //Send request
        while(!this.incomingMessages.isMessageWaiting()) {} //Wait for response
        return this.incomingMessages.getConversationFlow(); //Get response
    }

    public Queue<JSONObject> getPushedMessages() {
        return this.incomingMessages.getPushMessages();
    }
}
