import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 1234;
    private ServerSocket serverSocket;
    private ExecutorService pool = Executors.newFixedThreadPool(4);
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port: " + PORT);
    }

    public void startServer() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(clientSocket, this);
            System.out.println("Client  " + clientHandler);
            pool.execute(clientHandler);
        }
    }

    public synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Client removed: " + clientHandler.getClientInfo());
    }

    public boolean addClient(ClientHandler newClient) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getSocket().getInetAddress().equals(newClient.getSocket().getInetAddress()) &&
                    client.getSocket().getPort() == newClient.getSocket().getPort()) {
                    return false; // Client already exists
                }
            }
            clients.add(newClient);
            return true;
        }
    }

    public synchronized List<ClientHandler> getClients() {
        return new ArrayList<>(clients);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startServer();
    }
}
