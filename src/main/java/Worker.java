import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Worker extends Thread {
    private Socket socket;
    private String method;
    private String target;
    private BufferedReader in;
    private String requestMessage;

    public Worker(Socket socket) {
        this.socket = socket;
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.requestMessage = in.readLine();
            this.method = requestMessage.split(" ")[0];
            this.target = requestMessage.split(" ")[1];
        } catch (Exception e) {

        }

    }

    public void run() {
        try {
            System.out.println("Connection established with client: " + this.socket.getInetAddress());

            if (this.target.equals("/")) {
                this.handleHome();
            } else if (this.target.matches("/echo/\\w+")) {
                this.handleEcho();
            } else if (this.target.matches("/user-agent")) {
                this.handleUserAgent();
            } else if (this.target.matches("/files/.*")) {
                if (this.method.equals("GET")) {
                    this.handleGetFile();
                } else if (this.method.equals("POST")) {
                    this.handlePostFile();
                } else {
                    this.handleNotFound();
                }

            } else {
                this.handleNotFound();
            }
            System.out.println("The received message from the client: " + requestMessage);
            this.socket.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void handleNotFound() throws Exception{
        String httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
        this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
    }

    private void handlePostFile() throws Exception{
        Path filePath = this.getFilePath();
        String body = this.getBody();
        Files.write(filePath, body.getBytes("UTF-8"));
        String httpResponse = "HTTP/1.1 201 Created\r\n\r\n";
        this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
    }

    private String getBody() throws Exception {
        String line;
        String body = "";
        do  {
            line = this.in.readLine();
        }while(line != "\r\n");
        while ((line = this.in.readLine()) != null) {
            body += line;
        }
        return body;
    }

    private String getFileName(){
        return this.target.substring("/files/".length());
    }

    private Path getFilePath(){
        return Paths.get(Main.fileDir, this.getFileName());
    }

    private void handleGetFile() {
        String httpResponse;
        try {
            Path filePath = this.getFilePath();
            byte[] b = Files.readAllBytes(filePath);
            httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: "
                    + b.length + "\r\n\r\n";
            this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
            this.socket.getOutputStream().write(b);
        } catch (NoSuchFileException e) {
            httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
            try {
                this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
            }catch(Exception e2) {
                System.out.println("handleGetFile writing to socket error");
            }
            
        } catch(Exception e){
            System.out.println("handleGetFile Reading file or writing");
        }
    }

    private void handleUserAgent() {
        String headerLine, httpResponse;
        String ua = "";
        try {
            while ((headerLine = this.in.readLine()) != null && !headerLine.isEmpty()) {
                if (headerLine.startsWith("User-Agent:")) {
                    ua = headerLine.substring("User-Agent:".length()).trim();
                    break;
                }
            }
            httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + ua.length() + "\r\n\r\n"
                    + ua;
        } catch (Exception e) {
            System.out.println("Error while reading");
            httpResponse = "HTTP/1.1 500 OK\r\n\r\n"
                    + ua;
        }

        try {
            this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        } catch (Exception e) {
            System.out.println("Error writeing to socket");
        }
    }

    private void handleEcho() {
        String echoMessage = this.target.substring(6);
        String httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: "
                + echoMessage.length() + "\r\n\r\n" + echoMessage;
        try {
            this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        } catch (Exception e) {
            System.out.println("handleecho error writeing to sockey");
        }

    }

    public void handleHome() {
        String httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
        try {
            this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        } catch (Exception e) {

        }
    }

}
