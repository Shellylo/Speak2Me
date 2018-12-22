package speaktome.client;

import org.json.JSONObject;

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

    public JSONObject getPushedMessage() {
        return this.incomingMessages.getPushMessage();
    }
}
