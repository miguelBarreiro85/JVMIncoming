package httpserver;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest {
    private String method;
    private String target;
    private HashMap<String, String> headers = new HashMap<>();
    private HashMap<String, String> queryParams = new HashMap<>();
    private byte[] body;
    private int contentLength = 0;
    private String domain;
    private HashMap<String, String> routeParams = new HashMap<>();

    public HttpRequest(InputStream in) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int prev = -1, curr;

        while (true) {
            curr = in.read();
            if (prev == '\r' && curr == '\n') {
                break;
            }
            buffer.write(curr);
            prev = curr;
        }

        String tmp = buffer.toString("UTF-8");
        this.method = tmp.split(" ")[0].trim();

        String tmpTarget = tmp.split(" ")[1].trim();

        Pattern p = Pattern
                .compile("^(?:(?<protocol>https?)://(?<domain>[^/]+))?(?<path>/[^?]*)(?:\\\\?(?<query>.*))?");
        Matcher m = p.matcher(tmpTarget);
        if (m.find()) {
            this.domain = m.group("domain");
            this.target = m.group("path");
            String queryString = m.group("query");
            if (queryString != null) {
                String[] queryPA = queryString.split("&");
                for (String pa : queryPA) {
                    String[] qp = pa.split("=");
                    this.queryParams.put(qp[0], qp.length == 2 ? qp[1] : "");
                }
            }
        }

        if (tmpTarget.contains("?")) {
            tmpTarget = URLDecoder.decode(this.target, "UTF-8");
            String queryParamsStr = tmpTarget.split("\\?")[1].trim();
            String[] queryPA = queryParamsStr.split("&");
            for (String pa : queryPA) {
                String[] qp = pa.split("=");
                this.queryParams.put(qp[0], qp.length == 2 ? qp[1] : "");
            }
        }

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

        if (this.headers.containsKey("Content-Length")) {
            this.contentLength = Integer.parseInt(this.headers.get("Content-Length"));
        }
        this.body = new byte[this.contentLength];
        int bytesRead = 0;
        while (bytesRead < this.contentLength) {
            int res = in.read(this.body, bytesRead, this.contentLength - bytesRead);
            if (res == -1)
                break;
            bytesRead += res;
        }
    }

    public HashMap<String, String> getRouteParams(){
        return this.routeParams;
    }
    
    public void setRouteParams(HashMap<String, String> routeParams){
        this.routeParams = routeParams;
    }

    public String getMethod() {
        return method;
    }

    public String getTarget() {
        return target;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getDomain() {
        return domain;
    }
}
