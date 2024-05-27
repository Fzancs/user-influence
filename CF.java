import java.io.*;

public class CF extends User{

    public CF(String host, int port) throws IOException {
        super(host, port); // Call the constructor of the User class
    }

    @Override
    public Double updateOpinion(Double selectedOpinion, Double otherOpinion, String whichUser) {
        selectedOpinion = (selectedOpinion + otherOpinion)/2;
        return selectedOpinion;
    }

    public static void main(String[] args) {

        String host = "localhost";
        int port = 1234;

        try {
            CF cf = new CF(host, port);
            System.out.println("Opinion: " + cf.topicOpinions.get(DEFAULT_TOPIC) + " on topic: " + DEFAULT_TOPIC) ;
            cf.output.println(4);
            cf.startSending();
            cf.listenForMessages();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

