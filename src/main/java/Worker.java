import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Worker extends Thread {
    private Socket socket;
    private String method;
    private String target;
    private BufferedReader in;
    private HashMap<String, String> headers = new HashMap<>();
    private byte[] body;

    public Worker(Socket socket) {
        System.out.println("worker started");
        this.socket = socket;
        try {
            InputStream in = this.socket.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int prev = -1, curr;

            System.out.println("Start reading target" + in.toString());
            // write the method and target to the buffer
            while (true) {
                System.out.print("r");
                curr = in.read();
                System.out.print((char)curr);
                if (prev == '\r' && curr == '\n') {
                    break;
                }
                buffer.write(curr);
                prev = curr;
            }
            System.out.println("Finishi reading target");

            String tmp = buffer.toString("UTF-8");
            this.method = tmp.split(" ")[0].trim();
            this.target = tmp.split(" ")[1].trim();

            System.out.println("Target: " + this.target);
            System.out.println("Method: " + this.method);
            buffer.reset();
            // Read the headers
            while (true) {
                curr = in.read();
                buffer.write(curr);
                if (buffer.size() >= 4) {
                    byte[] last4 = buffer.toByteArray();
                    int len = last4.length;
                    if (last4[len - 1] == '\n' && last4[len - 2] == '\r' && last4[len - 3] == '\n'
                            && last4[len - 4] == '\r') {
                        break;
                    }
                }
            }

            // Read the Headers to the MAP
            String headers = buffer.toString("UTF-8");
            String[] headerLines = headers.split("\r\n");
            for (String header : headerLines) {
                if (header.contains(":")) {
                    String k = header.split(":")[0].trim();
                    String v = header.split(":")[1].trim();
                    this.headers.put(k, v);
                }
            }
            System.out.println("HEADERS: " + this.headers.toString());

            // READ the body
            int cl = 0;
            if (this.headers.containsKey("Content-Length")) {
                cl = Integer.parseInt(this.headers.get("Content-Length"));
            }
            this.body = new byte[cl];
            int bytesRead = 0;
            while (bytesRead < cl) {
                int res = in.read(this.body, bytesRead, cl - bytesRead);
                if (res == -1)
                    break;
                bytesRead += res;
            }
            System.out.println("BODY LENGTH: " + cl);
        } catch (Exception e) {
            System.out.println("REBENTOU: " + e.getMessage());
        }

    }

    public void run() {
        try {
            System.out.println("Target: " + this.target);

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
            this.socket.getOutputStream().flush();
            this.socket.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void handleNotFound() throws Exception {
        String httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
        this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
    }

    private void handlePostFile() throws Exception {
        Path filePath = this.getFilePath();

        Files.write(filePath, this.body);
        String httpResponse = "HTTP/1.1 201 Created\r\n\r\n";
        this.socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
    }

    private String getFileName() {
        return this.target.substring("/files/".length());
    }

    private Path getFilePath() {
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
            } catch (Exception e2) {
                System.out.println("handleGetFile writing to socket error");
            }

        } catch (Exception e) {
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
