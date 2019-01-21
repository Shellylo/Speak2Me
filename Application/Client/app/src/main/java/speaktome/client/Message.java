package speaktome.client;

public class Message {
    private String phone;
    private boolean isMine;
    private String content;
    private boolean isInChat;

    public Message(String phone, boolean isMine, String content, boolean isInChat) {
        this.phone = phone;
        this.isMine = isMine;
        this.content = content;
        this.isInChat = isInChat;

    }

    /*
        Function returns phone number of the message
        Input: None
        Output: Phone number
     */
    public String getPhone() {
        return this.phone;
    }

    /*
        Function returns whether the message is mine
        Input: None
        Output: true if message is mine, false otherwise
     */
    public boolean isMine() {
        return this.isMine;
    }

    /*
        Function returns message content
        Input: None
        Output: Message content
     */
    public String getContent() {
        return this.content;
    }

    /*
        Function returns if message in chat
        Input: None
        Output: if message is in chat
     */
    public boolean isInChat() {
        return isInChat;
    }
}
