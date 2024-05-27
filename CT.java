import java.io.*;
import java.util.Random;

public class CT extends User{

    public CT(String host, int port) throws IOException {
        super(host, port); // Call the constructor of the User class
    }

    @Override
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
                    else {
                        String[] parts = newOp.split(":");
                        String topicPart = parts[0];
                        double opinionPart = Double.parseDouble(parts[1]);
                        String whichUser = parts[2];
                        int evidencePart = Integer.parseInt(parts[3]);

                        Double influence = influenceMap.getOrDefault(whichUser, new Random().nextDouble());
                        influence = Math.round(influence * 100.0) / 100.0;
                        influenceMap.put(whichUser, influence); // getUser() + influence

                        System.out.println("Received opinion: " + opinionPart + ", topic: " + topicPart); 

                        if (evidencePart % 7 == 0) {
                            double otherOpinion = Double.parseDouble(String.valueOf(opinionPart));
                            // When new user are entering add them an opinion to topics
                            Double tempOpinion = topicOpinions.get(topicPart);
                            selectedOpinion = tempOpinion != null ? tempOpinion : Math.round(Math.random() * 100.0) / 100.0;
                            selectedOpinion = selectedOpinion + (otherOpinion - selectedOpinion) * influenceMap.get(whichUser);
                            selectedOpinion = Math.round(selectedOpinion * 100.0) / 100.0; // Round to two decimal places
                            System.out.println("Updated Opinion:" + selectedOpinion + ", influence: " +influenceMap.get(whichUser) + " from " + whichUser); // receive selectedOpinion from random user
                            topicOpinions.put(topicPart, selectedOpinion);  //ajoute topic
                        }
                        else {
                            System.out.println("Opinion not updated, insufficient evidence" + evidencePart);
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


    public static void main(String[] args) {

        String host = "localhost";
        int port = 1234;

        try {
            CT ct = new CT(host, port);

            ct.output.println(1);
            ct.startSending();
            ct.listenForMessages();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    
}



