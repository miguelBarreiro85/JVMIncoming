import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

  public static String fileDir;
  
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");

    Main.fileDir = args.length > 0 ? args[1] : null;
    // Uncomment this block to pass the first stage
    //
    try {
      ServerSocket serverSocket = new ServerSocket(4221);

      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);

      do {
        Socket clientSocket = serverSocket.accept(); // Wait for connection from client.

        Thread t = new Worker(clientSocket);
        t.start();
        
        
      } while (true); // Keep the server running indefinitely
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
