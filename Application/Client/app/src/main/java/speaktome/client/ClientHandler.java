package speaktome.client;

public class ClientHandler {
    private static Client client = null;

    public static Client getClient() {
        if (ClientHandler.client == null) {
            ClientHandler.client = new Client();
        }
        return ClientHandler.client;
    }
}
