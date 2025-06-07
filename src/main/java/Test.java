import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String args[]){
        HashMap<String,String> qpHasMap= new HashMap<>();
        String url = "http://coisas.pt/cenas?price_id=&coupon=i0etgzgq&utm_source=interno&utm_medium=landing&utm_campaign=price_1QBHKaLIdwiNHqVn9jOrZB45&utm_term=&utm_content=";
        try {
            url = URLDecoder.decode(url, "UTF-8");    
        }catch(Exception e) {
            return;
        }
        
        String tmpTarget = url.split("\\?")[0].trim();
        String queryParamsStr = url.split("\\?")[1].trim();
        String[] qpA = queryParamsStr.split("&");

        String[] queryPA = queryParamsStr.split("&");
        for (String p : queryPA) {
            String[] qp = p.split("=");
            qpHasMap.put(qp[0],  qp.length == 2 ? qp[1] : "");
        }

        Pattern p = Pattern.compile("(https?):\\/\\/([^\\/]+)\\/");
        Matcher m = p.matcher(tmpTarget);
        
        while (m.find()) {
            System.out.println(m.group());
        }

        System.out.println("End " + qpHasMap.toString());
    }
    
}
