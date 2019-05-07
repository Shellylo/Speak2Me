package speaktome.client;

public class ClientHandler {
    private static Client client = null;

    /*
        Get static client (creates one if doesn't exist)
        Input: None
        Output: Client object
     */
    public static Client getClient() {
        if (ClientHandler.client == null) {
            ClientHandler.client = new Client();
        }
        return ClientHandler.client;
    }

    public static void deleteClient( ) {
        if (ClientHandler.client != null) {
            ClientHandler.client.closeSocket();
            ClientHandler.client = null;
        }
    }
}
