import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    //
    try {
      ServerSocket serverSocket = new ServerSocket(4221);

      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);

      do {
        Socket clientSocket = serverSocket.accept(); // Wait for connection from client.

        System.out.println("Connection established with client: " + clientSocket.getInetAddress());
        BufferedReader in = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream()));

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String requestMessage = in.readLine();
        String requestTarget = requestMessage.split(" ")[1];
        String httpResponse;
        System.out.println("request target: " + requestTarget);
        if (requestTarget.matches("/echo/\\w+")) {
          String echoMessage = requestTarget.substring(6);
          httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: "
              + echoMessage.length() + "\r\n\r\n" + echoMessage;  
        } else {
          httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
        }
        System.out.println("The received message from the client: " + requestMessage);

        clientSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        clientSocket.close();
      } while (true); // Keep the server running indefinitely
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
