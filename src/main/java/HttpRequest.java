import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class HttpRequest {
    private String method;
    private String target;
    private HashMap<String, String> headers = new HashMap<>();
    private byte[] body;
    private int contentLength = 0;

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
        this.target = tmp.split(" ")[1].trim();

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

    public String getMethod() { return method; }
    public String getTarget() { return target; }
    public HashMap<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body; }
    public int getContentLength() { return contentLength; }
}
