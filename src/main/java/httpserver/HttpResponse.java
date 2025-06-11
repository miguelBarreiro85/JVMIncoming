package httpserver;
import java.util.HashMap;

public class HttpResponse {
    private int code;
    private HashMap<String,String> headers;
    private byte[] body; 

    public HttpResponse(int code) {
        this(code, new HashMap<>(), new byte[0]);
    }

    public HttpResponse(int code, HashMap<String,String> headers){
        this(code, headers, new byte[0]);
    }

    public HttpResponse(int code, HashMap<String,String> headers, byte[] body){
        this.code = code;
        this.headers = headers;
        this.body = body;
    }   

    public void addHeader(String key, String value){
        this.headers.put(key, value);
    }


    public byte[] getBody(){
        return this.body;
    }

    public byte[] getBytes() throws Exception{
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 " + Integer.toString(this.code) + " ");
        String m = "";
        switch (this.code) {
            case 200:
                m = "OK";
                break;
            case 201:
                m = "Created";
                break;
            case 404:
                m = "Not Found";
                break;
            case 500:
            default:
                m = "Error";
        }
        sb.append(m + "\r\n");
        this.headers.forEach((k,v) -> {
            sb.append(k+": " + v + "\r\n");
        });
        sb.append("\r\n");
        
        String r = sb.toString();
        byte[] headers = r.getBytes("UTF-8");
        byte[] res = new byte[headers.length + body.length];
        System.arraycopy(headers, 0, res, 0, headers.length);
        System.arraycopy(this.body, 0, res, headers.length, body.length);
        return res;
    }

    public void setBody(byte[] byteArray) {
        this.body = byteArray;
    }
}
