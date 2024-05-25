import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class User {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String currentTopic;
    private double opinion;
    private double otherOpinion;
    private Map<String, Double> influenceMap = new HashMap<>();

    public User(String host, int port) throws IOException {
        // Connect to the server and setup input and output streams
        socket = new Socket(host, port);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
        opinion = Math.round(Math.random() * 100.0) / 100.0; // Initialize the opinion randomly
        System.out.println("Connected to the server at " + host + ":" + port);
    }

    public void listenForMessages() {
        // Listen for messages from the server
        try {
            String message;
            while ((message = input.readLine()) != null) {
                handleServerMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Lost connection to the server.");
            closeEverything();
        }
    }

    private void handleServerMessage(String message) {
        // Handle different types of messages from the server
        System.out.println("[Serveur] " + message); // Log for debugging
        if (message.startsWith("Topic:")) {
            currentTopic = message.substring(6); // Extract topic after "Topic:"
            System.out.println("New topic received: " + currentTopic);
        } else if (message.startsWith("Opinion:")) {
            String[] parts = message.split(":");
            otherOpinion = Double.parseDouble(parts[1]);
            System.out.println("opinion: " + opinion +", otherOpinion:"+ otherOpinion );
            // updateOpinion(otherOpinion);
        } else if (message.startsWith("Proof")) {
            // System.out.println(message); // Log the interaction proof
            updateOpinion(otherOpinion);
        }
    }

    private void updateOpinion(double otherOpinion) {
        // Calculate or retrieve the influence for the current topic
        Double influence = influenceMap.getOrDefault(currentTopic, new Random().nextDouble());
        influence = Math.round(influence * 100.0) / 100.0;
        influenceMap.put(currentTopic, influence);
        // Update opinion based on influence
        opinion = opinion + (otherOpinion - opinion) * influence;
        opinion = Math.round(opinion * 100.0) / 100.0; // Round to two decimal places
        System.out.println("Updated opinion: " + opinion + ", influence: " + influence);
    }

    private void closeEverything() {
        // Close all connections and streams
        try {
            if (input != null)
                input.close();
            if (output != null)
                output.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java User <host> <port>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            User client = new User(host, port);
            client.listenForMessages();
        } catch (IOException e) {
            System.out.println("Cannot connect to the server. Check if the server is running and the host/port are correct.");
            e.printStackTrace();
        }
    }
}
