import java.io.*;
import java.util.*;

public class Influencers extends User{

    public Influencers(String host, int port) throws IOException {
        super(host, port); // Call the constructor of the User class
    }


    // remove evidence
    @Override
    public void sendRandomTopicOpinion() {
        List<String> topics = new ArrayList<>(topicOpinions.keySet());
        Random random = new Random();
        selectedTopic = topics.get(random.nextInt(topics.size()));
        selectedOpinion = topicOpinions.get(selectedTopic); 
        output.println(selectedTopic + ":" + selectedOpinion); // topic + selectedOpinion 
    }

    @Override
    // Influence entre 0 et 0.2 (Se fait très peu influencer)
    public Double setInfluence(Double influence, String whichUser) {
        influence = influenceMap.getOrDefault(whichUser, 0.2 * new Random().nextDouble());
        influence = Math.round(influence * 100.0) / 100.0;
        influenceMap.put(whichUser, influence); // getUser() + influence
        return influence;
    }
    
    @Override
    public boolean isInfluencers() {
        return true;
    }
    public static void main(String[] args) {

        String host = "localhost";
        int port = 1234;

        try {
            Influencers influencers = new Influencers(host, port);
            System.out.println("Opinion: " + influencers.topicOpinions.get(DEFAULT_TOPIC) + " on topic: " + DEFAULT_TOPIC) ;
            influencers.output.println(3);
            influencers.startSending();
            influencers.listenForMessages();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}



