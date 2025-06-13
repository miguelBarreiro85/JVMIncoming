package app.controllers;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import httpserver.Headers;
import httpserver.HttpRequest;
import httpserver.HttpResponse;
import httpserver.Server;

public class TestController {
    public static HttpResponse getHome(HttpRequest req) {
        return new HttpResponse(200);
    }

    public static HttpResponse getEcho(HttpRequest req) throws Exception {
        String echoMessage = "";
        if (req.getRouteParams().containsKey("word")) {
            echoMessage = req.getRouteParams().get("word");
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put(Headers.CONTENT_TYPE, "text/plain");
        HttpResponse res = new HttpResponse(200, headers, echoMessage.getBytes("UTF-8"));
        return res;
    
    }

    public static HttpResponse handleUserAgent(HttpRequest req) throws Exception {
        String ua = "";
        if (req.getHeaders().containsKey("User-Agent")) {
            ua = req.getHeaders().get("User-Agent");
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put(Headers.CONTENT_TYPE, "text/plain");
        HttpResponse res = new HttpResponse(200, headers, ua.getBytes("UTF-8"));
        return res;
    }

    public static HttpResponse handleGetFile(HttpRequest req) throws Exception {
        if (!req.getRouteParams().containsKey("filename")) {
            return new HttpResponse(404);
        }

        try {
            Path filePath = TestController.getFilePath(req.getRouteParams().get("filename"));
            byte[] b = Files.readAllBytes(filePath);
            HashMap<String, String> h = new HashMap<>();
            h.put(Headers.CONTENT_TYPE, "application/octet-stream");
            h.put(Headers.CONTENT_lENGTH, Integer.toString(b.length));
            HttpResponse res = new HttpResponse(200, h, b);
            return res;
        } catch (NoSuchFileException e) {
            return new HttpResponse(404);
        }

    }

    public static HttpResponse handlePostFile(HttpRequest req) throws Exception {
        if (!req.getRouteParams().containsKey("filename")) {
            return new HttpResponse(404);
        }
        Path filePath = TestController.getFilePath(req.getRouteParams().get("filename"));
        Files.write(filePath, req.getBody());
        HttpResponse res = new HttpResponse(201);
        return res;
    }

    private static Path getFilePath(String filename) {
        return Paths.get(Server.fileDir, filename);
    }
}
