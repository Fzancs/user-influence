import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class User {
    protected Socket socket;
    protected BufferedReader input;
    protected PrintWriter output;
    protected static final String DEFAULT_TOPIC = "help";
    protected Map<String, Double> topicOpinions = new ConcurrentHashMap<>();
    protected Map<String, Double> influenceMap = new ConcurrentHashMap<>();
    protected String selectedTopic;
    protected double selectedOpinion;
    private Map<String, Integer> evidenceMap = new ConcurrentHashMap<>();
    protected boolean isCT;  // Indicates if the user is a Critical Thinker
    protected boolean isInfluencers;  // Indicates if the user is a Influencer

    public User(String host, int port) throws IOException {
        // Connect to the server and setup input and output streams
        this.socket = new Socket(host, port);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.topicOpinions.put(DEFAULT_TOPIC, Math.round(Math.random() * 100.0) / 100.0); // Initialize default topic with a default opinion value
        this.selectedOpinion = Math.round(Math.random() * 100.0) / 100.0; // Initialize the opinion randomly
        this.evidenceMap.put(DEFAULT_TOPIC, new Random().nextInt(7) + 1); // Initialize default topic with a default opinion value
        this.isCT = false;  // Indicates if the user is a Critical Thinker
        this.isInfluencers = false;  // Indicates if the user is a Influencer

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
                // System.out.println("frequency: " + frequency);
                sendRandomTopicOpinion();
            } catch (Exception e) {
                System.out.println("Error while sending: " + e.getMessage());
                executor.shutdown(); // Shut down the executor on error
                closeEverything();
            }
        }, 5, frequency, TimeUnit.SECONDS); // Start after 5seconds , repeat every "frequency" seconds
    }

    public synchronized void listenForMessages() {
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
                        if (parts.length < 3) {
                            System.out.println("Erreur : message incomplet reçu -> " + newOp);
                            return;
                        }

                        String topicPart = parts[0];
                        double opinionPart = Double.parseDouble(parts[1]);
                        String whichUser = parts[2];

                        int evidencePart = 0;
                        if (!isInfluencers() && parts.length > 3) { // Vérification avant d'accéder à parts[3]
                            evidencePart = Integer.parseInt(parts[3]);
                            System.out.println("Received evidence: " + evidencePart);
                        }

                        Double influence = 0.0;
                        influence = setInfluence(influence, whichUser);
                        System.out.println("Received opinion: " + opinionPart + ", topic: " + topicPart); 
                        
                        // Si ce n'est pas CT alors executer
                        if(!isCT() || evidencePart % 7 == 0) {
                            double otherOpinion = Double.parseDouble(String.valueOf(opinionPart));
                            // When new user are entering add them an opinion to topics
                            Double tempOpinion = topicOpinions.get(topicPart);
                            selectedOpinion = tempOpinion != null ? tempOpinion : Math.round(Math.random() * 100.0) / 100.0;
                            // selectedOpinion = selectedOpinion + (otherOpinion - selectedOpinion) * influenceMap.get(whichUser);
                            selectedOpinion = updateOpinion(selectedOpinion, otherOpinion, whichUser);
                            selectedOpinion = Math.round(selectedOpinion * 100.0) / 100.0; // Round to two decimal places
                            System.out.println("Updated Opinion:" + selectedOpinion + ", influence: " +influenceMap.get(whichUser) + " from " + whichUser); // receive selectedOpinion from random user
                            topicOpinions.put(topicPart, selectedOpinion);  //ajoute topic
                            // showAllOpinions(); 
                        }
                        else {
                            System.out.println("Opinion not updated, insufficient evidence: " + evidencePart);
                        }
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

    public Double setInfluence(Double influence, String whichUser) {
        influence = influenceMap.getOrDefault(whichUser, new Random().nextDouble());
        influence = Math.round(influence * 100.0) / 100.0;
        influenceMap.put(whichUser, influence); // getUser() + influence
        return influence;
    }

    public Double updateOpinion(Double selectedOpinion, Double otherOpinion, String whichUser) {
        selectedOpinion = selectedOpinion + (otherOpinion - selectedOpinion) * influenceMap.get(whichUser);
        return selectedOpinion;
    }

    public Double getOpinionForTopic(String topic) {
        return topicOpinions.get(topic);
    }

    public boolean isCT() {
        return isCT;
    }

    public boolean isInfluencers() {
        return isInfluencers;
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
            else if(choice == 2){ // Ajouter topic avec User comme Proposer
                if (args.length < 2) {
                    System.err.println("Usage: java User 2 --topic='nouveau topic'");
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
