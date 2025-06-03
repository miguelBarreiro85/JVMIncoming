import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

public class Worker extends Thread {
    private final Socket socket;
    private byte[] body;
    private HttpRequest r;
    public static final String CONTENT_lENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_ENCODING = "Content-Encoding";

    public Worker(Socket socket) {
        System.out.println("worker started");
        this.socket = socket;
    }

    public void run() {
        try {
         
            InputStream in = this.socket.getInputStream();
            this.r = new HttpRequest(in);

            System.out.println("Target: " + this.r.getTarget());

            if (this.r.getTarget().equals("/")) {
                this.handleHome();
            } else if (this.r.getTarget().matches("/echo/\\w+")) {
                this.handleEcho();
            } else if (this.r.getTarget().matches("/user-agent")) {
                this.handleUserAgent();
            } else if (this.r.getTarget().matches("/files/.*")) {
                if (this.r.getMethod().equals("GET")) {
                    this.handleGetFile();
                } else if (this.r.getMethod().equals("POST")) {
                    this.handlePostFile();
                } else {
                    this.handleNotFound();
                }

            } else {
                this.handleNotFound();
            }
            this.socket.getOutputStream().flush();
            if(this.r.getHeaders().containsKey("Connection") && this.r.getHeaders().get("Connection").equals("Close")){
                this.socket.close();
            }else{
                new Worker(this.socket).start();
            }
            
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void handleNotFound() throws Exception {
        HttpResponse res = new HttpResponse(404);
        res.writeTo(this.socket.getOutputStream());
    }

    private void handlePostFile() throws Exception {
        Path filePath = this.getFilePath();
        Files.write(filePath, this.body);
        HttpResponse res = new HttpResponse(201);
        res.writeTo(this.socket.getOutputStream());
    }

    private String getFileName() {
        return this.r.getTarget().substring("/files/".length());
    }

    private Path getFilePath() {
        return Paths.get(Main.fileDir, this.getFileName());
    }

    private void handleGetFile() throws Exception {
        try {
            Path filePath = this.getFilePath();
            byte[] b = Files.readAllBytes(filePath);

            HashMap<String, String> h = new HashMap<>();
            h.put(Worker.CONTENT_TYPE, "application/octet-stream");
            h.put(Worker.CONTENT_lENGTH, Integer.toString(b.length));
            HttpResponse res = new HttpResponse(200, h, b);
            res.writeTo(this.socket.getOutputStream());
        } catch (NoSuchFileException e) {
            HttpResponse res = new HttpResponse(404);
            res.writeTo(this.socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("handleGetFile Reading file or writing");
        }
    }

    private void handleUserAgent() throws Exception {
        String ua = "";
        if (this.r.getHeaders().containsKey("User-Agent:")) {
            ua = this.r.getHeaders().get("User-Agent");
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put(Worker.CONTENT_TYPE, "text/plain");
        headers.put(Worker.CONTENT_lENGTH, Integer.toString(ua.length()));
        HttpResponse res = new HttpResponse(200, headers, ua.getBytes("UTF-8"));
        res.writeTo(this.socket.getOutputStream());

    }

    private void handleEcho() throws Exception {
        String echoMessage = this.r.getTarget().substring(6);
        HashMap<String, String> headers = new HashMap<>();
        if (this.r.getHeaders().containsKey("Accept-Encoding")
                && this.r.getHeaders().get("Accept-Encoding").contains("gzip")) {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
                gzipStream.write(echoMessage.getBytes("UTF-8"));
            }
            headers.put(Worker.CONTENT_TYPE, "text/plain");
            headers.put(Worker.CONTENT_ENCODING, "gzip");
            headers.put(Worker.CONTENT_lENGTH, String.valueOf(byteStream.size()));
            HttpResponse res = new HttpResponse(200, headers, byteStream.toByteArray());
            res.writeTo(this.socket.getOutputStream());
        } else {
            headers.put(Worker.CONTENT_TYPE, "text/plain");
            headers.put(Worker.CONTENT_lENGTH, Integer.toString(echoMessage.length()));
            HttpResponse res = new HttpResponse(200, headers, echoMessage.getBytes("UTF-8"));
            res.writeTo(this.socket.getOutputStream());
        }
    }

    public void handleHome() {
        HttpResponse res = new HttpResponse(200);
        try {
            res.writeTo(this.socket.getOutputStream());
        } catch (Exception e) {

        }
    }

}
