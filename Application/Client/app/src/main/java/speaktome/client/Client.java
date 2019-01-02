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

    /*
        Sends request to server (using outgoing messages queue)
        Input: request to send
        Output: None
     */
    public void send(JSONObject request) {
        this.outgoingMessages.addConversationFlow(request); //Send request
    }

    /*
        Function returns first message from conversation flow queue
        Input: None
        Output: JSONObject message if exist, null otherwise
     */
    public JSONObject getConversationFlow() {
        return this.incomingMessages.getConversationFlow();
    }

    /*
        Function returns first message from pushed messages queue
        Input: None
        Output: JSONObject message if exist, null otherwise
     */
    public JSONObject getPushedMessage() {
        return this.incomingMessages.getPushMessage();
    }
}
