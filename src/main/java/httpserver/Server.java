package httpserver;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    static HashMap<String, HandlerInfo> getList = new HashMap<>();
    static HashMap<String, HandlerInfo> postList = new HashMap<>();
    static HashMap<String, HandlerInfo> deleteList = new HashMap<>();
    static HashMap<String, HandlerInfo> putList = new HashMap<>();

    public static void get(String path, HandlerInfo hI) {
        Server.getList.put(path, hI);
    }

    public static void post(String path, HandlerInfo hI) {
        Server.postList.put(path, hI);
    }

    public static void put(String path, HandlerInfo hI) {
        Server.putList.put(path, hI);
    }

    public static void delete(String path, HandlerInfo hI) {
        Server.deleteList.put(path, hI);
    }

    public static void listen(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            do {
                Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
                System.out.println("Receive a connection: " + clientSocket.getInetAddress());
                Thread t = new Resolver(clientSocket);
                t.start();

            } while (true); // Keep the server running indefinitely
        } catch (Exception e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

}
