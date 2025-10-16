import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        int port = 58008;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            // The server will accept one client, process it, and then shut down.
            // To handle multiple clients, you would typically use a loop.
            System.out.println("Waiting for a client to connect...");
            Socket clientSocket = serverSocket.accept(); // Blocks until a client connects
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

            // Set up streams to communicate with the client
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Read message from the client
            String clientMessage = in.readLine();
            System.out.println("Received from client: " + clientMessage);

            // Send a response back to the client
            out.println("Server received your message: '" + clientMessage + "'");
            System.out.println("Response sent to client.");

            // Close resources
            in.close();
            out.close();
            clientSocket.close();
            System.out.println("Connection with client closed.");

        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}