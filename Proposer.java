import java.io.*;
import java.net.*;

public class Proposer {
    private Socket socket;
    private PrintWriter output;

    public Proposer(String host, int port) throws IOException {
        // Connect to the server and setup input and output streams
        this.socket = new Socket(host, port);
        this.output = new PrintWriter(socket.getOutputStream(), true);
    
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Proposer --topic='nouveau topic'");
            System.exit(1);
        }
        String topic = args[0].split("=")[1];
        
        try {
            Proposer proposer = new Proposer("localhost", 1234);

            proposer.output.println(2);
            proposer.output.println(topic);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
