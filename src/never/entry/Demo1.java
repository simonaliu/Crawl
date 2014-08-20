package never.entry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;



public class Demo1{
public static void main(String[] args) {
    String urlStr = "http://paper.sciencenet.cn/paper/toppaper.aspx";
    String year = "2014";
    String month = "8";
    String day = "7";
    int page = 1; // 每次加1
    String result = ""; // 返回的内容
    
    Map<String, String> params = new HashMap<String, String>();
    params.put("__EVENTTARGET", "AspNetPager1");
    params.put("__EVENTARGUMENT", String.valueOf(page));
    params.put("ddlyear", year);
    params.put("ddlmonth", month);
    params.put("ddlday", day);
    params.put("AspNetPager1_input", String.valueOf(page - 1));
    
    URL url;
    HttpURLConnection httpConnection = null;
    try {
        String postStr = "";
        Iterator<Entry<String, String>> entryKeyIterator = params.entrySet().iterator();
        while (entryKeyIterator.hasNext()) {
            Entry<String, String> e = entryKeyIterator.next();
            String key = e.getKey();
            String value = URLEncoder.encode(e.getValue(), "UTF-8");
            postStr += key + "=" + value + "&";
        }
        url = new URL(urlStr);

        httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("POST");
        httpConnection.setDoOutput(true);
        httpConnection.setConnectTimeout(10000);
        httpConnection.setReadTimeout(15000);
        OutputStreamWriter out = new OutputStreamWriter(httpConnection.getOutputStream());
        out.write(postStr);
        out.close();
        int responseCode = httpConnection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream is = httpConnection.getInputStream();
            Scanner s = new Scanner(is, "UTF-8");
            s.useDelimiter("\\A");
            result = s.hasNext() ? s.next() : "";
            s.close();
        }
    } catch (MalformedURLException e) {
    } catch (IOException e) {
    } finally {
        if (httpConnection != null) {
            httpConnection.disconnect();
        }
    }
    System.out.println(result);
}
}