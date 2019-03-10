package speaktome.client;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.util.LinkedList;
import java.util.Queue;

public class InputMessages implements Runnable {
    private static final int MAX_SIZE_LEN = 10;

    private Queue<JSONObject> conversationFlow;
    private Queue<JSONObject> pushMessages;

    private DataInputStream in;

    public InputMessages(DataInputStream in) {
        this.in = in;
        this.conversationFlow = new LinkedList<JSONObject>();
        this.pushMessages = new LinkedList<JSONObject>();

        Thread createInputThread = new Thread(this);
        createInputThread.start();
    }

    /*
        Function receives responses from server and inserts it to matching queue
        Input: None
        Output: None
     */
    @Override
    public void run() {
        try {
            byte[] input;
            while(true) {
                // Receive response size
                input = new byte[MAX_SIZE_LEN];
                this.in.read(input);
                int size = Integer.valueOf(new String(input));

                // Receive response and convert into JSONObject
                input = new byte[size];
                this.in.read(input);
               // input = Security.decrypt(input);
                String strInput = new String(input);
                JSONObject json = new JSONObject(strInput);

                // Add response to matching queue - incoming messages / server regular response
                int code = (int)json.get("code");
                if (Codes.PUSH_MESSAGE_CODE == code) {
                    this.pushMessages.add(json);
                } else {
                    this.conversationFlow.add(json);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    /*
        Get one message from conversation flow
        Input: None
        Output: message if exists, null otherwise
     */
    public JSONObject getConversationFlow() {
        JSONObject ret = null;
        if(!this.conversationFlow.isEmpty()) {
            ret = this.conversationFlow.remove();
        }
        return ret;
    }

    /*
        Get one message from push messages
        Input: None
        Output: message if exists, null otherwise
     */
    public JSONObject getPushMessage() {
        JSONObject ret = null;
        if(!this.pushMessages.isEmpty()) {
            ret = this.pushMessages.remove();
        }
        return ret;
    }
}
