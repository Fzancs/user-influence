import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

public class User {
    protected Socket socket;
    protected BufferedReader input;
    protected PrintWriter output;
    private static final String DEFAULT_TOPIC = "help";
    protected Map<String, Double> topicOpinions = new ConcurrentHashMap<>();
    protected Map<String, Double> influenceMap = new ConcurrentHashMap<>();
    private String selectedTopic;
    protected double selectedOpinion;
    private Map<String, Integer> evidenceMap = new ConcurrentHashMap<>();

    public User(String host, int port) throws IOException {
        // Connect to the server and setup input and output streams
        this.socket = new Socket(host, port);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.topicOpinions.put(DEFAULT_TOPIC, Math.round(Math.random() * 100.0) / 100.0); // Initialize default topic with a default opinion value
        this.selectedOpinion = Math.round(Math.random() * 100.0) / 100.0; // Initialize the opinion randomly
        this.evidenceMap.put(DEFAULT_TOPIC, new Random().nextInt(7) + 1); // Initialize default topic with a default opinion value

        System.out.println("Connected to the server at " + host + ":" + port);

    }

    public void closeEverything() {
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


    public void sendRandomTopicOpinion() {
        List<String> topics = new ArrayList<>(topicOpinions.keySet());
        Random random = new Random();
        selectedTopic = topics.get(random.nextInt(topics.size()));
        selectedOpinion = topicOpinions.get(selectedTopic);

        Integer tempEvidence = evidenceMap.get(selectedTopic);
        int evidence;
        if (tempEvidence != null) {
            evidence = tempEvidence;
        } else {
            evidence = new Random().nextInt(7) + 1;
            if (!evidenceMap.containsKey(selectedTopic)) {
                evidenceMap.put(selectedTopic, evidence);
            }
        }    

        output.println(selectedTopic + ":" + selectedOpinion + ":" + evidence); // topic + selectedOpinion + evidence
        // System.out.println("UserA Sending opinion: " + selectedOpinion + " on topic: " + selectedTopic);
    }


    public void startSending() {
        Random random = new Random();
        int frequency = 1 + random.nextInt(10); // Generate random frequency between 1 and 10 seconds
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {

            try {
                // output.println(1);
                // System.out.println("frequency: " + frequency);
                sendRandomTopicOpinion();
            } catch (Exception e) {
                System.out.println("Error while sending: " + e.getMessage());
                executor.shutdown(); // Shut down the executor on error
                closeEverything();
            }
        }, 5, frequency, TimeUnit.SECONDS); // Start immediately, repeat every 5 seconds
    }

    public void listenForMessages() {
        new Thread(() -> {
            try {
                String newOp;
                
                while ((newOp = input.readLine()) != null) {

                    if (newOp.startsWith("Topic:")) {
                        String[] parts = newOp.split(":");
                        String topicPart = parts[1];
                        System.out.println("Received new topic: " + topicPart);
                        topicOpinions.put(topicPart, Math.round(Math.random() * 100.0) / 100.0);  //ajoute topic
                    }
                    else{
                        String[] parts = newOp.split(":");
                        String topicPart = parts[0];
                        double opinionPart = Double.parseDouble(parts[1]);
                        String whichUser = parts[2];
                        // int evidencePart = Integer.parseInt(parts[3]);

                        Double influence = influenceMap.getOrDefault(whichUser, new Random().nextDouble());
                        influence = Math.round(influence * 100.0) / 100.0;
                        influenceMap.put(whichUser, influence); // getUser() + influence

                        System.out.println("Received opinion: " + opinionPart + ", topic: " + topicPart); 

                        double otherOpinion = Double.parseDouble(String.valueOf(opinionPart));
                        // When new user are entering add them an opinion to topics
                        Double tempOpinion = topicOpinions.get(topicPart);
                        selectedOpinion = tempOpinion != null ? tempOpinion : Math.round(Math.random() * 100.0) / 100.0;

                        selectedOpinion = selectedOpinion + (otherOpinion - selectedOpinion) * influenceMap.get(whichUser);
                        selectedOpinion = Math.round(selectedOpinion * 100.0) / 100.0; // Round to two decimal places
                        System.out.println("Updated Opinion:" + selectedOpinion + ", influence: " +influenceMap.get(whichUser) + " from " + whichUser); // receive selectedOpinion from random user
                        topicOpinions.put(topicPart, selectedOpinion);  //ajoute topic
                        // showAllOpinions(); 
                    }
                }
            } catch (IOException e) {
                System.out.println("Error while receiving: " + e.getMessage());
                closeEverything();
                System.exit(0); // Forcer la fermeture du client
            }
        }).start();
    }

    // show every topic and opinion on current user
    public void showAllOpinions() {
        for (Map.Entry<String, Double> entry : topicOpinions.entrySet()) {
            System.out.println("Topic: " + entry.getKey() + ", Opinion: " + entry.getValue());
        }
    }

    public static void main(String[] args) {

        try {
            if (args.length < 1) {
                System.out.println("Missing argument.");
                System.out.println("Usage: java Client <choice> [<arguments>]");
                System.exit(1);
            }

            int choice = Integer.parseInt(args[0]);
            String host = "localhost";
            int port = 1234;

            User client = new User(host, port);

            if(choice == 1){
                if (args.length < 1) {
                    System.out.println("Usage: java User Choice ");
                    return;
                }
                System.out.println("Opinion: " +client.topicOpinions.get(DEFAULT_TOPIC) + " on topic: " + DEFAULT_TOPIC) ;
                client.output.println(choice);
                client.startSending();
                client.listenForMessages();
        
            }
            else if(choice == 2){ // Ajouter topic avec user ou Proposer

                if (args.length < 2) {
                    System.err.println("Usage: java Proposer --topic='nouveau topic'");
                    System.exit(1);
                }

                String topic = args[1].split("=")[1];
                System.out.println(topic);
                client.output.println(2);
                client.output.println(topic);
            }
            else{
                System.out.println("Invalid choice.");
            
            }
        } catch (IOException e) {
            System.out.println("Cannot connect to the server. Check if the server is running and the host/port are correct.");
            e.printStackTrace();
        }
    }
}
