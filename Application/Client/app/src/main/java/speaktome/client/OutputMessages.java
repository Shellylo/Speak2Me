package speaktome.client;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.util.LinkedList;
import java.util.Queue;


public class OutputMessages implements Runnable{
    private Queue<JSONObject> conversationFlow;

    private DataOutputStream out;

    public OutputMessages(DataOutputStream out) {
        this.out = out;
        this.conversationFlow = new LinkedList<JSONObject>();

        Thread createOutputThread = new Thread(this);
        createOutputThread.start();
    }

    /*
        [Thread] Send waiting messages (if exist) to server
        Input: None
        Output: None
     */
    @Override
    public void run() {
        try {
            while (true) {
                while(!this.conversationFlow.isEmpty()) {
                    JSONObject msgToSend = this.conversationFlow.remove();
                    String strRequest = msgToSend.toString();
                    this.out.write(String.format("%010d", strRequest.length()).getBytes()); //Sends message size
                    this.out.write(strRequest.getBytes("UTF-8")); //Sends message
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    /*
        Add request to outgoing messages queue
        Input: request to send
        Output: None
     */
    public void addConversationFlow(JSONObject request) {
        this.conversationFlow.add(request);
    }
}
