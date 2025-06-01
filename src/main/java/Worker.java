import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
          this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        } else if (requestTarget.matches("/echo/\\w+")) {
          String echoMessage = requestTarget.substring(6);
          httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: "
              + echoMessage.length() + "\r\n\r\n" + echoMessage;  
          this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
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
          this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        } else if(requestTarget.matches("/files/.*")){
            String fileName = requestTarget.substring("/files/".length());
            try {
                Path filePath = Paths.get(Main.fileDir, fileName);
                byte[] b = Files.readAllBytes(filePath);
                httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\\r\\nContent-Length: " + b.length + "\r\n\r\n";
                this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
                this.socket.getOutputStream().write(b);
            }catch(NoSuchFileException e) {
                httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
                this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
            }
            
            
        } else {
          httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
          this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        }
        System.out.println("The received message from the client: " + requestMessage);
        this.socket.close();
    }catch(Exception e){
        System.out.println(e.toString());  
    } 
  }

}
