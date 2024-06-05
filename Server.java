import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * The {@code Server} class encapsulates the functionality of a server that can handle multiple client connections simultaneously.
 * It manages client connections, creating a separate {@code ClientHandler} for each connected client.
 * This server uses a fixed thread pool to handle client requests concurrently, ensuring efficient usage of system resources.
 *
 * @see ClientHandler
 */
public class Server {
    private static final int PORT = 1234;
    private ServerSocket serverSocket;
    private ExecutorService pool = Executors.newFixedThreadPool(4);
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    /**
     * Constructs a new Server and initializes the server socket on the specified port.
     * 
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port: " + PORT);
    }

    /**
     * Starts the server to accept client connections continuously. 
     * For each connection, a new {@code ClientHandler} is created and executed in a separate thread.
     * 
     * @throws IOException if an I/O error occurs when waiting for a connection.
     */
    public void startServer() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(clientSocket, this);
            System.out.println("Client connected: " + clientHandler);
            pool.execute(clientHandler);
        }
    }

    /**
     * Removes a client from the server's list of currently connected clients.
     *
     * @param clientHandler The client handler to remove.
     */
    public synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Client removed: " + clientHandler.getClientInfo());
    }

    /**
     * Attempts to add a new client to the server's list of clients.
     * This method checks if the client is already connected based on socket address and port before adding.
     *
     * @param newClient The new client to add.
     * @return {@code true} if the client was added successfully, {@code false} if the client already exists.
     */
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

    /**
     * Provides a copy of the list of current clients.
     *
     * @return A new list containing all current clients.
     */
    public synchronized List<ClientHandler> getClients() {
        return new ArrayList<>(clients);
    }

    /**
     * The main method that creates a server and starts it.
     *
     * @param args Command line arguments (not used).
     * @throws IOException If an I/O error occurs.
     */
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startServer();
    }
}
