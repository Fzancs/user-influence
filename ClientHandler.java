import java.io.*;
import java.net.*;
import java.util.*;

class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;  // Assuming you have an appropriate Server class that manages clients.
    private BufferedReader in;
    private PrintWriter out;
    private int userId;  // Unique identifier for the user

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
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
            int choice = Integer.parseInt(in.readLine());
            // String topics = in.readLine();

            System.out.println("choice: " + choice);
            while (choice >= 1) {

                if (choice == 1) { // choice 1 gere User et CT
                    server.addClient(this);
                    try {
                        String opinion = in.readLine(); // recoit opinion de user
                        List<ClientHandler> clients = new ArrayList<>(server.getClients());
                        // Remove this ClientHandler from the list to avoid selecting itself
                        clients.remove(this);
            
                        if (!clients.isEmpty()) {
                            Random rand = new Random();
                            int randomClientIndex = rand.nextInt(clients.size());
                            ClientHandler selectedClient = clients.get(randomClientIndex);
                            selectedClient.shareTopicOpinion(opinion, this.getClientInfo());
                        } else {
                            System.out.println("No other clients available to receive opinion.");
                        }
                    } catch (IOException e) {
                        System.out.println("Error reading input: " + e.getMessage());
                    closeConnections();                        
                    break;
                    }

                } else if (choice == 2) { // creer topic
                    String topic = in.readLine();
                    System.out.println("topic: " + topic);
                    broadcastTopic(topic);
                } else if (choice == 3) { // gere Influencers (envoie a tout le monde)
                    server.addClient(this);
                    try {
                        String opinion = in.readLine(); 
                        List<ClientHandler> clients = new ArrayList<>(server.getClients());
                        clients.remove(this);

                        //  each client selection is random and independent each time
                        if (!clients.isEmpty()) {
                            Set<Integer> pickedIndices = new HashSet<>();
                            int numberOfClients = clients.size();
                            // System.out.println("number of clients: " + numberOfClients);

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
                            System.out.println("No other clients available to receive opinion.");
                        }
                    } catch (IOException e) {
                        System.out.println("Error reading input: " + e.getMessage());
                    closeConnections();                        
                    break;
                    }

                } else if (choice == 4) { // gere CF pour deux users
                    server.addClient(this);
                    try {
                        String opinion = in.readLine(); 
                        List<ClientHandler> clients = new ArrayList<>(server.getClients());
                        clients.remove(this);

                        if (!clients.isEmpty()) {
                            Random rand = new Random();
                            int randomClientIndex = rand.nextInt(clients.size());
                            ClientHandler selectedClient = clients.get(randomClientIndex);
                            selectedClient.shareTopicOpinion(opinion, this.getClientInfo());
                            
                        } else {
                            System.out.println("No other clients available to receive opinion.");
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

    private void shareTopicOpinion(String input, int userID){
        String[] parts = input.split(":");
        String topicPart = parts[0];
        double opinionPart = Double.parseDouble(parts[1]);
        int evidencePart = Integer.parseInt(parts[2]);

        out.println(topicPart +":"+ opinionPart + ":" + userID + ":" + evidencePart); // send topic, opinion, evidence from userId to a random user
        System.out.println(this.getClientInfo() + " to " + userID + ", opinion:" + opinionPart + ", topic: " + topicPart + ", evidence: " + evidencePart);
    }

    // sans evidence pour Influences
    private void shareTopicOpinionInflu(String input, int userID){
        String[] parts = input.split(":");
        String topicPart = parts[0];
        double opinionPart = Double.parseDouble(parts[1]);

        out.println(topicPart +":"+ opinionPart + ":" + userID); // send topic, opinion, evidence from userId to a random user
        System.out.println(this.getClientInfo() + " to " + userID + ", opinion:" + opinionPart + ", topic: " + topicPart);
    }



    public void broadcastTopic(String topic) {
        List<ClientHandler> clients = server.getClients();
        for (ClientHandler client : clients) {
            System.out.println("Broadcasting topic '" + topic + "' to clients.");
            client.out.println("Topic:" + topic);
        }
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

    public Socket getSocket() {
        return this.socket;
    }

}
