import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;  // Assuming you have an appropriate Server class that manages clients.
    private BufferedReader in;
    private PrintWriter out;
    private int userId;  // Unique identifier for the user
    private String currentTopic;
    private double opinion;
    private ScheduledExecutorService interactionExecutor;
    private boolean interactionsStarted = false;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.opinion = new Random().nextDouble();
        this.userId = generateUserId();  // Generate a unique user ID when initializing the client handler
    }

    private int generateUserId() {
        // Generate a unique ID for each client using a random number.
        return new Random().nextInt(10000);
    }

    public int getClientInfo() {
        // Return user ID as client info.
        return userId;
    }

    public void run() {
        try {
            String input = in.readLine();
            if (input != null) {
                if (input.startsWith("--topic=")) {
                    String topic = input.substring("--topic=".length());
                    broadcastTopic(topic);
                    closeConnections();
                } else {
                    server.addClient(this);
                    while ((input = in.readLine()) != null) {
                        processClientMessage(input);
                    }
                    server.removeClient(this);
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling connection: " + e.getMessage());
            server.removeClient(this);
        } finally {
            closeConnections();
        }
    }

    private void processClientMessage(String message) {
        // Additional message processing can be implemented here.
    }

    private void closeConnections() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.removeClient(this);
    }

    public void setTopic(String topic) {
        this.currentTopic = topic;
        System.out.println("New topic: '" + currentTopic + "'");
        out.println("Topic:" + currentTopic);
        resetOpinion();
    }

    private void resetOpinion() {
        this.opinion = new Random().nextDouble();
        System.out.println("New opinion: " + opinion);
        out.println("Opinion:" + opinion + ":" + userId);  // Sending the user ID with the opinion
    }

    public void broadcastTopic(String topic) {
        List<ClientHandler> clients = server.getClients();
        for (ClientHandler client : clients) {
            client.setTopic(topic);
            System.out.println("Broadcasting topic '" + topic + "' to clients.");
        }
        if (!interactionsStarted) {
            initiateInteractions();
            interactionsStarted = true;
        }
    }

    private void initiateInteractions() {
        interactionExecutor = Executors.newScheduledThreadPool(1);
        interactionExecutor.scheduleAtFixedRate(this::performInteractions, 5, 5, TimeUnit.SECONDS);
    }

    public void performInteractions() {
        List<ClientHandler> clients = server.getClients();
        if (clients.size() > 1) {
            Random rand = new Random();
            for (int i = 0; i < clients.size(); i++) {
                int index = rand.nextInt(clients.size());
                ClientHandler client = clients.get(i);
                ClientHandler otherClient = clients.get(index);
                while (otherClient == client) {
                    index = rand.nextInt(clients.size());
                    otherClient = clients.get(index);
                }
                System.out.println("Initiating interaction between " + client.getClientInfo() + " and " + otherClient.getClientInfo());
                client.sendProofAndOpinion(otherClient);
            }
        }
    }

    private void sendProofAndOpinion(ClientHandler other) {
        out.println("Proof from " + getClientInfo() + " to " + other.getClientInfo() + " on topic: " + currentTopic);
    }
}
