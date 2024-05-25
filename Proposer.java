import java.io.*;
import java.net.*;

public class Proposer {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Proposer --topic='nouveau topic'");
            System.exit(1);
        }

        String topic = args[0].split("=")[1];
        String serverAddress = "localhost";
        int serverPort = 1234;

        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("--topic=" + topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
