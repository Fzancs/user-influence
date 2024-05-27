import java.io.*;

public class CT extends User{

    public CT(String host, int port) throws IOException {
        super(host, port); // Call the constructor of the User class
    }

    @Override
    public boolean isCT() {
        return true;  // Always true for instances of CriticalThinker
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



