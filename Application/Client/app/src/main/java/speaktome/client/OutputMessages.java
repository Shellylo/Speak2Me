package speaktome.client;

import org.json.JSONObject;

import java.io.DataOutputStream;


public class OutputMessages implements Runnable{
    private JSONObject conversationFlow;

    private DataOutputStream out;

    public OutputMessages(DataOutputStream out) {
        this.out = out;
        this.conversationFlow = null;

        Thread createOutputThread = new Thread(this);
        createOutputThread.start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                if(this.conversationFlow != null) {
                    String strRequest = conversationFlow.toString();
                    this.out.write(Integer.toString(strRequest.length()).getBytes()); //Sends message size
                    this.out.write(strRequest.getBytes()); //Sends message
                    this.conversationFlow = null;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public void setConversationFlow(JSONObject request) {
        this.conversationFlow = request;
    }
}
