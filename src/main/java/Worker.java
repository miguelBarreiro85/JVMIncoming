import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class Worker extends Thread{
    private Socket socket;

    public Worker(Socket socket){
        this.socket = socket;
    }

    public void run(){
        try {
        System.out.println("Connection established with client: " + this.socket.getInetAddress());
        BufferedReader in = new BufferedReader(
            new InputStreamReader(this.socket.getInputStream()));


        String requestMessage = in.readLine();
        String requestTarget = requestMessage.split(" ")[1];
        String httpResponse;

        System.out.println("request target: " + requestTarget);
        
        if (requestTarget.equals("/")) {
          httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
        } else if (requestTarget.matches("/echo/\\w+")) {
          String echoMessage = requestTarget.substring(6);
          httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: "
              + echoMessage.length() + "\r\n\r\n" + echoMessage;  
        } else if(requestTarget.matches("/user-agent")){
          String headerLine;
          String ua="";
          while((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            if(headerLine.startsWith("User-Agent:")){
              ua = headerLine.substring("User-Agent:".length()).trim();
              break;
            }
          }
        
          httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + ua.length() + "\r\n\r\n" + ua;
        }
        else {
          httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
        }
        System.out.println("The received message from the client: " + requestMessage);

        this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        this.socket.close();
    }catch(Exception e){
        System.out.println(e.toString());  
    } 
  }

}
