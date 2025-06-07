import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import app.Main;
import httpserver.HttpRequest;
import httpserver.HttpResponse;

public class Worker extends Thread {
    private final Socket socket;
    private HttpRequest r;
    public static final String CONTENT_lENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_ENCODING = "Content-Encoding";

    public Worker(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
         
            InputStream in = this.socket.getInputStream();
            this.r = new HttpRequest(in);

            System.out.println("Target: " + this.r.getTarget());
            HttpResponse res;
            if (this.r.getTarget().equals("/")) {
                res = this.handleHome();
            } else if (this.r.getTarget().matches("/echo/\\w+")) {
                res = this.handleEcho();
            } else if (this.r.getTarget().matches("/user-agent")) {
                res = this.handleUserAgent();
            } else if (this.r.getTarget().matches("/files/.*")) {
                if (this.r.getMethod().equals("GET")) {
                    res = this.handleGetFile();
                } else if (this.r.getMethod().equals("POST")) {
                    res = this.handlePostFile();
                } else {
                    res = this.handleNotFound();
                }

            } else {
                res =this.handleNotFound();
            }
            if(this.r.getHeaders().containsKey("Connection") && this.r.getHeaders().get("Connection").equals("close")){
                res.addHeader("Connection", "close");
            }

            this.socket.getOutputStream().write(res.getBytes());
            this.socket.getOutputStream().flush();
            if(this.r.getHeaders().containsKey("Connection") && this.r.getHeaders().get("Connection").equals("close")){
                this.socket.close();
            }else{
                new Worker(this.socket).start();
            }
            
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private HttpResponse handleNotFound() throws Exception {
        HttpResponse res = new HttpResponse(404);
        return res;
    }

    private HttpResponse handlePostFile() throws Exception {
        Path filePath = this.getFilePath();
        Files.write(filePath, this.r.getBody());
        HttpResponse res = new HttpResponse(201);
        return res;
    }

    private String getFileName() {
        return this.r.getTarget().substring("/files/".length());
    }

    private Path getFilePath() {
        return Paths.get(Main.fileDir, this.getFileName());
    }

    private HttpResponse handleGetFile() throws Exception {
        try {
            Path filePath = this.getFilePath();
            byte[] b = Files.readAllBytes(filePath);

            HashMap<String, String> h = new HashMap<>();
            h.put(Worker.CONTENT_TYPE, "application/octet-stream");
            h.put(Worker.CONTENT_lENGTH, Integer.toString(b.length));
            HttpResponse res = new HttpResponse(200, h, b);
            return res;
        } catch (NoSuchFileException e) {
            HttpResponse res = new HttpResponse(404);
            return res;
        }
    }

    private HttpResponse handleUserAgent() throws Exception {
        String ua = "";
        if (this.r.getHeaders().containsKey("User-Agent")) {
            ua = this.r.getHeaders().get("User-Agent");
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put(Worker.CONTENT_TYPE, "text/plain");
        HttpResponse res = new HttpResponse(200, headers, ua.getBytes("UTF-8"));
        return res;

    }

    private HttpResponse handleEcho() throws Exception {
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
            HttpResponse res = new HttpResponse(200, headers, byteStream.toByteArray());
            return res;
        } else {
            headers.put(Worker.CONTENT_TYPE, "text/plain");
            HttpResponse res = new HttpResponse(200, headers, echoMessage.getBytes("UTF-8"));
            return res;
        }
    }

    public HttpResponse handleHome() throws Exception{
        HttpResponse res = new HttpResponse(200);
        return res;
    }

}
