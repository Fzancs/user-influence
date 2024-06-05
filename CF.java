import java.io.*;
import java.net.*;
import java.util.*;

public class CF extends User {


    public CF(String host, int port) throws IOException {
        super(host, port); // Call the constructor of the User class
    }

    public void updateOpinions() {
        try {
            // Simulate asking the server for two random user opinions
            output.println(4); // send choice to ClientHandler

            // Assume server sends back two opinions formatted as "user1:opinion1 user2:opinion2"
            String response = input.readLine();
            if (response != null && !response.isEmpty()) {
                String[] parts = response.split(" ");
                double opinion1 = Double.parseDouble(parts[0].split(":")[1]);
                double opinion2 = Double.parseDouble(parts[1].split(":")[1]);

                // Update opinions by averaging
                double updatedOpinion = updateOpinion(opinion1, opinion2, "");

                // Send updated opinion back to the server
                output.println("update_opinion " + updatedOpinion);
            }
        } catch (IOException e) {
            System.out.println("Failed to update opinions: " + e.getMessage());
        }
    }

    
    @Override
    public void sendRandomTopicOpinion() {
        List<String> topics = new ArrayList<>(topicOpinions.keySet());
        Random random = new Random();
        selectedTopic = topics.get(random.nextInt(topics.size()));

        output.println(selectedTopic); // topic + selectedOpinion + evidence
    }



    @Override
    public Double updateOpinion(Double selectedOpinion, Double otherOpinion, String whichUser) {
        return (selectedOpinion + otherOpinion) / 2;
    }


    public static void main(String[] args) {
        String host = "localhost";
        int port = 1234;

        try {
            CF cf = new CF(host, port);
            cf.startSending();
        } catch (IOException e) {
            System.out.println("Error starting CF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
