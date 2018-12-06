package speaktome.client;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.util.LinkedList;
import java.util.Queue;

public class InputMessages implements Runnable {
    private static final int MAX_SIZE_LEN = 10;

    private JSONObject conversationFlow;
    private Queue<JSONObject> pushMessages;

    private DataInputStream in;

    public InputMessages(DataInputStream in) {
        this.in = in;
        this.conversationFlow = null;
        this.pushMessages = new LinkedList<JSONObject>();

        Thread createInputThread = new Thread(this);
        createInputThread.start();
    }

    @Override
    public void run() {
        try {
            byte[] input;
            while(true) {
                //
                input = new byte[MAX_SIZE_LEN];
                this.in.read(input);
                int size = Integer.valueOf(new String(input));

                //
                input = new byte[size];
                this.in.read(input);
                String strInput = new String(input);
                JSONObject json = new JSONObject(strInput);

                //
                int code = (int)json.get("code");
                if (Codes.PUSH_MESSAGE_CODE == code) {
                    this.pushMessages.add(json);
                } else {
                    this.conversationFlow = json;
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public boolean isMessageWaiting() {
        return this.conversationFlow != null;
    }

    public JSONObject getConversationFlow() {
        JSONObject ans = this.conversationFlow;
        this.conversationFlow = null;
        return ans;
    }

    public Queue<JSONObject> getPushMessages() {
        return this.pushMessages;
    }
}
