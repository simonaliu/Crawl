package never.entry;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import never.utils.BaseUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Demo {

	public static void main(String[] args) {
		
		try {
			Document doc = Jsoup.connect("http://www.nsfc.gov.cn/publish/portal0/tab87/info40158.htm").get();
			Document doc2 = Jsoup.connect("http://www.nsfc.gov.cn/publish/portal0/tab88/info44852.htm").get();
			String text1=BaseUtil.encryptMD5(doc.text());
			String text2=BaseUtil.encryptMD5(doc2.text());
			System.out.println(text1+","+text2+","+(text1.equals(text2)));
			Elements bodyElements = doc.select("div.line_xilan");
			Elements bodyElement = doc2.select("div.line_xilan");
			System.out.println("size:"+bodyElements.size());
			String str = bodyElements.text();
			//System.out.println(str);
			
			String htm = bodyElements.html().toString();
			
			//System.out.println(htm);
			
			Pattern source = Pattern.compile("(?<=\\b来源：)\\S+\\b");
			Matcher matcher = source.matcher(htm);	
			Matcher matcher1 = source.matcher(str);	
			boolean flags=false;
			if(matcher1.find()){
			    System.out.println(matcher1.group());
			   // flags=true;
			}
			else if(matcher.find()){
				System.out.println(matcher.group());
			}

			/*Pattern dateReg = Pattern.compile("\\d{2,4}年\\d{1,2}月\\d{1,2}日");
			Matcher matcher = dateReg.matcher(bodyElements.get(i).text());
			if(matcher.find()){
				System.out.println(matcher.group());
			}
			*/
			
			//Pattern dateReg=Pattern.compile("\\d{2,4}-\\d{1,2}-\\d{1,2}\\D+\\d{0,2}\\:*\\d{0,2}\\:*\\d{0,2}");
			//Pattern dateReg2=Pattern.compile("\\d{2,4}-\\d{1,2}-\\d{1,2}\\b");
			/*
				Matcher matcher = dateReg.matcher(bodyElements.get(i).text());
				Matcher matcher2 = dateReg.matcher(bodyElements.get(i).text());
				if(matcher.find()){
					System.out.println(matcher.group());
				}
				else if(matcher2.find()){
					System.out.println(matcher2.group());
				}
				
			}
			*/
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
