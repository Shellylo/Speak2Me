package speaktome.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.SQLOutput;

public class ConnectToServer implements Runnable{
    private static final int SERVER_PORT = 3124;
    private static final String SERVER_IP = "109.67.52.245"; //10.0.2.2, 79.177.3.169

    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    private boolean isConnected = false;

    public ConnectToServer() {
        Thread createConnectionThread = new Thread(this);
        createConnectionThread.start();
        while (!this.isConnected) { //wait to connect

            // Prevent CPU overload
            try {
                Thread.sleep(50);
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /*
        [Thread] Connect to server (set up input stream and output stream)
                 * in order to use sockets, must be thread
        Input: None
        Output: None
     */
    @Override
    public void run() {
        try {
            //Connect to server
            this.clientSocket = new Socket(this.SERVER_IP, this.SERVER_PORT);
            //Get output stream
            OutputStream outToServer = this.clientSocket.getOutputStream();
            this.out = new DataOutputStream(outToServer);
            //Get input stream
            InputStream inFromServer = this.clientSocket.getInputStream();
            this.in = new DataInputStream(inFromServer);
            this.isConnected = true;
        }
        catch (Exception e) {
            try {
                this.clientSocket.close();
            }
            catch (Exception ex) {
                System.out.println(ex);
            }
            System.out.println(e);
        }
    }

    /*
        Function returns output stream
        Input: None
        Output: None
     */
    public DataOutputStream getOut() {
        return this.out;
    }

    /*
        Function returns input stream
        Input: None
        Output: None
     */
    public DataInputStream getIn() {
        return this.in;
    }

    public boolean closeSocket() {
        try {
            this.clientSocket.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return this.clientSocket.isClosed();
    }
}
