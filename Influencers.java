import java.io.*;
import java.util.*;

public class Influencers extends User{

    public Influencers(String host, int port) throws IOException {
        super(host, port); // Call the constructor of the User class
    }

    @Override
    // Influence entre 0.8 et 1
    public Double setInfluence(Double influence, String whichUser) {
        influence = influenceMap.getOrDefault(whichUser, 0.2 * new Random().nextDouble() + 0.8);
        influence = Math.round(influence * 100.0) / 100.0;
        influenceMap.put(whichUser, influence); // getUser() + influence
        return influence;
    }
    

    public static void main(String[] args) {

        String host = "localhost";
        int port = 1234;

        try {
            Influencers influencers = new Influencers(host, port);

            influencers.output.println(3);
            influencers.startSending();
            influencers.listenForMessages();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}



