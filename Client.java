import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public static void main(String[] args) {
        String hostname = "localhost"; // Or "127.0.0.1"
        int port = 58008;

        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("Connected to the server.");

            // Set up streams to communicate with the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send a message to the server
            String message = "Hello from the Client!";
            out.println(message);
            System.out.println("Sent to server: " + message);

            // Read the response from the server
            String serverResponse = in.readLine();
            System.out.println("Received from server: " + serverResponse);

            // Resources are closed automatically by the try-with-resources statement

        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + hostname);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}