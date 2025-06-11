package httpserver;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

public class Resolver extends Thread {
    private Socket socket;

    public Resolver(Socket s) {
        this.socket = s;
    }

    public void run() {
        try {
            HttpRequest req = new HttpRequest(this.socket.getInputStream());
            HashMap<String, HandlerInfo> l;
            switch (req.getMethod()) {
                case "GET":
                    l = Server.getList;
                    break;
                case "POST":
                    l = Server.postList;
                    break;
                case "PUT":
                    l = Server.putList;
                    break;
                case "DELETE":
                    l = Server.deleteList;
                    break;
                default:
                    throw new Exception("Method unknown");
            }

            HttpResponse res = new HttpResponse(404);
            for (String path : l.keySet()) {
                String[] routeParts = path.split("/");
                String[] targetParts = req.getTarget().split("/");

                boolean match = routeParts.length == targetParts.length;
                HashMap<String, String> routeParams = new HashMap<>();

                if (match) {
                    for (int i = 0; i < routeParts.length; i++) {
                        if (routeParts[i].startsWith("{") && routeParts[i].endsWith("}")) {
                            // It's a variable, capture it
                            String paramName = routeParts[i].substring(1, routeParts[i].length() - 1);
                            routeParams.put(paramName, targetParts[i]);
                        } else if (!routeParts[i].equals(targetParts[i])) {
                            match = false;
                            break;
                        }
                    }
                }

                if (match) {
                    HandlerInfo h = l.get(path);
                    Method m = h.controllerClass.getMethod(h.methodName, HttpRequest.class);
                    req.setRouteParams(routeParams);
                    res = (HttpResponse) m.invoke(h.controllerClass, req);
                    
                    if (req.getHeaders().containsKey(Headers.ACCEP_ENCODING)
                            && req.getHeaders().get(Headers.ACCEP_ENCODING).contains("gzip")) {
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
                            gzipStream.write(res.getBody());
                        }
                        
                        res.addHeader(Headers.CONTENT_ENCODING, "gzip");
                        res.setBody(byteStream.toByteArray());
                    }
                    break;
                }
            }

            res.addHeader(Headers.CONTENT_lENGTH, Integer.toString(res.getBody().length));

            if (req.getHeaders().containsKey(Headers.CONNECTION)
                    && req.getHeaders().get(Headers.CONNECTION).equals("close")) {
                res.addHeader(Headers.CONNECTION, "close");
                this.socket.getOutputStream().write(res.getBytes());
                this.socket.getOutputStream().flush();
                this.socket.close();
                return;
            } else {
                this.socket.getOutputStream().write(res.getBytes());
                this.socket.getOutputStream().flush();
                new Resolver(this.socket).start();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }
}
