package speaktome.client;

public class Message {
    private String phone;
    private boolean isMine;
    private String content;

    public Message(String phone, boolean isMine, String content) {
        this.phone = phone;
        this.isMine = isMine;
        this.content = content;
    }

    public String getPhone() {
        return this.phone;
    }

    public boolean isMine() {
        return this.isMine;
    }

    public String getContent() {
        return this.content;
    }
}
