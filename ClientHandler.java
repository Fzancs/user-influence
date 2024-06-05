import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The {@code ClientHandler} class manages communication between the server and a single client.
 * It handles all input and output for one connection, processes incoming commands,
 * and sends appropriate responses back to the client.
 *
 * This class is also responsible for managing the lifecycle of the connection,
 * including opening and closing streams and the socket connection.
 *
 * @see Server
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;  // Assuming you have an appropriate Server class that manages clients.
    private BufferedReader in;
    private PrintWriter out;
    private int userId;  // Unique identifier for the user

    /**
     * Constructs a new client handler for managing interactions with a client connected to the specified socket.
     * This handler manages input and output streams for communications and initializes a unique user identifier.
     *
     * @param socket The socket associated with the connected client.
     * @param server The server instance this handler is part of, used to interact with other clients and manage global state.
     * @throws IOException If an error occurs setting up input and output streams.
     */
    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.userId = generateUserId();  // Generate a unique user ID when initializing the client handler
    }

    /**
     * The main run loop of the client handler that listens for messages from the connected client
     * and processes them based on predefined commands. It handles different types of client interactions
     * based on the command received.
     *
     * This method continues to process messages until an error occurs or the connection is terminated by the client.
     */


     /**
     * Continuously processes commands received from the connected client until the connection is closed.
I    * It handles different types of client interactions based on the command received.
     * <ul>
     *   <li><strong>Choice 1:</strong> Handles user and critical thinker interactions by receiving opinions, topics, evidence,
     *       selecting a random client, and sending this opinion to him.</li>
     *   <li><strong>Choice 2:</strong> Handles the creation of new topics by broadcasting them to all clients.</li>
     *   <li><strong>Choice 3:</strong> Manages influencer actions by broadcasting opinions to a random subset of clients.</li>
     *   <li><strong>Choice 4:</strong> Manages consensus finder actions by sharing opinions with another randomly selected client.</li>
     * </ul>
     * If any choice leads to an IOException, the connection is closed, and the client is removed from the server.
     * The method ends if an invalid choice is received or if the choice leads to an exception.
     * The loop also breaks if any other IOException occurs during the communication.
     */
    @Override
    public void run() {

        try {
            int choice = Integer.parseInt(in.readLine());
            // String topics = in.readLine();

            System.out.println("choice: " + choice);
            while (choice >= 1) {

                if (choice == 1) { // choice 1 gere User and CT
                    server.addClient(this);
                    try {
                        String opinion = in.readLine(); // receive opinion from user
                        List<ClientHandler> clients = new ArrayList<>(server.getClients());
                        // Remove this ClientHandler from the list to avoid selecting itself
                        clients.remove(this);
            
                        if (!clients.isEmpty()) {
                            Random rand = new Random();
                            int randomClientIndex = rand.nextInt(clients.size());
                            ClientHandler selectedClient = clients.get(randomClientIndex);
                            selectedClient.shareTopicOpinion(opinion, this.getClientInfo());
                        } else {
                            System.out.println("No other clients available to receive opinion 1.");
                        }
                    } catch (IOException e) {
                        System.out.println("Error reading input: " + e.getMessage());
                    closeConnections();                        
                    break;
                    }

                } else if (choice == 2) { // create topic
                    String topic = in.readLine();
                    System.out.println("topic: " + topic);
                    broadcastTopic(topic);
                } else if (choice == 3) { // for Influencers (send to everyone)
                    server.addClient(this);
                    try {
                        String opinion = in.readLine(); 
                        List<ClientHandler> clients = new ArrayList<>(server.getClients());
                        clients.remove(this);

                        //  each client selection is random and independent each time
                        if (!clients.isEmpty()) {
                            Set<Integer> pickedIndices = new HashSet<>();
                            int numberOfClients = clients.size();

                            int numberToSend = new Random().nextInt(numberOfClients / 2 + 1);  // Ensure at least one
                        
                            while (pickedIndices.size() < numberToSend + 1) {
                                int randomIndex = new Random().nextInt(numberOfClients);
                                ClientHandler client = clients.get(randomIndex);

                                if (!pickedIndices.contains(randomIndex)) {
                                    pickedIndices.add(randomIndex);
                                    client.shareTopicOpinionInflu(opinion, this.getClientInfo());
                                    System.out.println("Influencers sended opinion to "+ client.getClientInfo() );
                                }
                            }
                        } else {
                            System.out.println("No other clients available to receive opinion 3.");
                        }
                    } catch (IOException e) {
                        System.out.println("Error reading input: " + e.getMessage());
                    closeConnections();                        
                    break;
                    }

                } else if (choice == 4) { // for CF with a random user
                    // server.addClient(this);
                    try {
                        String opinion = in.readLine(); //receive a topic from CF
                        List<ClientHandler> clients = new ArrayList<>(server.getClients());
                        clients.remove(this);

                        
                        if (!clients.isEmpty() && (clients.size() >=2)) {
                            
                            Random rand = new Random();
                            int randomClientIndex1 = rand.nextInt(clients.size());
                            ClientHandler selectedClient = clients.get(randomClientIndex1);

                            int randomClientIndex2 = randomClientIndex1;
                            while (randomClientIndex2 == randomClientIndex1) {
                                randomClientIndex2 = rand.nextInt(clients.size());
                            }
                            ClientHandler selectedClient2 = clients.get(randomClientIndex2);
                            selectedClient.shareTopicOpinion(opinion, this.getClientInfo());
                            
                        } else {
                            System.out.println("No other clients available to receive opinion 4.");
                        }
                    } catch (IOException e) {
                        System.out.println("Error reading input: " + e.getMessage());
                    closeConnections();                        
                    break;
                    }

                } else {
                    System.out.println("Invalid choice: " + choice);
                    closeConnections();
                }         
            }

        } catch (IOException e) {
            System.out.println("Error handling connection: " + e.getMessage());
            server.removeClient(this);
        } finally {
            closeConnections();
        }
    }

    /**
     * Shares a topic opinion received from one client to another, including evidence as part of the message.
     * This method formats the message and sends it directly to another client's output stream.
     *
     * @param input The opinion data received from the client.
     * @param userID The unique identifier of the client receiving the opinion.
     */
    private void shareTopicOpinion(String input, int userID){
        String[] parts = input.split(":");
        String topicPart = parts[0];
        double opinionPart = Double.parseDouble(parts[1]);
        int evidencePart = Integer.parseInt(parts[2]);

        out.println(topicPart +":"+ opinionPart + ":" + userID + ":" + evidencePart); // send topic, opinion, evidence from userId to a random user
        System.out.println(this.getClientInfo() + " to " + userID + ", opinion:" + opinionPart + ", topic: " + topicPart + ", evidence: " + evidencePart);
    }

    /**
     * Shares a topic opinion from influencers to another client without including evidence.
     * This variant is used when the sender is an influencer and the protocol does not require evidence attachment.
     *
     * @param input The opinion data received from the client.
     * @param userID The unique identifier of the client receiving the opinion.
     */
    private void shareTopicOpinionInflu(String input, int userID){
        String[] parts = input.split(":");
        String topicPart = parts[0];
        double opinionPart = Double.parseDouble(parts[1]);

        out.println(topicPart +":"+ opinionPart + ":" + userID); // send topic, opinion, evidence from userId to a random user
        System.out.println(this.getClientInfo() + " to " + userID + ", opinion:" + opinionPart + ", topic: " + topicPart);
    }


    /**
     * Broadcasts a new topic to all clients connected to the server. This method is typically
     * invoked when a client proposes a new topic and the server needs to inform all other clients.
     *
     * @param topic The new topic to broadcast.
     */
    public void broadcastTopic(String topic) {
        List<ClientHandler> clients = server.getClients();
        for (ClientHandler client : clients) {
            System.out.println("Broadcasting topic '" + topic + "' to clients.");
            client.out.println("Topic:" + topic);
        }
    }

    /**
     * Closes all connections and resources associated with this client handler.
     * This includes input and output streams and the socket connection. This method
     * is usually called when the client disconnects or an error occurs.
     */
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

    /**
     * Retrieves the socket associated with this client handler.
     * This socket is the network connection to the client managed by this handler.
     *
     * @return The {@link Socket} used for communication with the connected client.
     */
    public Socket getSocket() {
        return this.socket;
    }

    /**
     * Generates a unique identifier for a client. This ID is used internally to manage client identity.
     * The ID is a randomly generated integer between 0 and 9999.
     *
     * @return An integer representing the unique ID of the client.
     */
    private int generateUserId() {
        return new Random().nextInt(10000);
    }

    /**
     * Retrieves the unique identifier for this client, which was generated at the time of client handler creation.
     * This identifier is used to uniquely distinguish and manage clients within the server.
     *
     * @return The user ID of the client as an integer.
     */
    public int getClientInfo() {
        return userId;
    }
}
