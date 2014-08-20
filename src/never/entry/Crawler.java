package never.entry;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import never.utils.BaseUtil;
import never.utils.DbUtil;

public class Crawler {
	
	private static Crawler instance=new Crawler();
	
	public static Crawler instance(){
		return instance;
	}
	
	private Crawler(){
		
	}
	
	private Connection conn = null;
	private String lastMD5;
	public boolean onCrawling;
	
	public void startCrawler() {
		if(onCrawling){
			return;
		}
		onCrawling=true;
		if (initialCrawler()) {
			String tempStr = getInitialUrlAndClass();
			
			if(tempStr.equals("NOURL")){
				System.out.println("Hello");
				return;
			}
			String sArray[] = tempStr.split("::");
			String url = sArray[1];
			
			lastMD5=null;
			while (url != null) {
				//System.out.println(url);
				parseUrl(sArray);
				updateInitialUrl(url);
				String tempStr2 = getInitialUrlAndClass();
				if(tempStr2.equals("NOURL")){
					onCrawling=false;
					return;
				}
				sArray = tempStr2.split("::");
				url = sArray[1];
			}
			System.out.println("Crawler Finish Task!");
		}
		onCrawling=false;
	}

	public boolean initialCrawler() {             //开始爬取status为0的网站
		conn = DbUtil.instance().getConnection();
		return true;
	}

	public String getInitialUrlAndClass() {
		String[] rec=new String[13];
		try {
			PreparedStatement urlStmt = conn
					.prepareStatement("select * from seed where status = 0 limit 1");
			ResultSet urlRS = urlStmt.executeQuery();

			
			while (urlRS.next()) {
				rec[0] = urlRS.getString(2);
				rec[1] = urlRS.getString(3);
				rec[2] = urlRS.getString(4);
				rec[3] = urlRS.getString(6);
				rec[4] = urlRS.getString(7);
				rec[5] = urlRS.getString(8);
				rec[6] = urlRS.getString(9);
				rec[7] = urlRS.getString(10);
				rec[8] = urlRS.getString(11);
				rec[9] = urlRS.getString(12);
				rec[10] = urlRS.getString(13);
				rec[11] = urlRS.getString(14);
				rec[12] = urlRS.getString(15);
			}
			if(rec[1] == null && rec[2] == null){
				return "NOURL";
			}
			
			urlStmt.close();
			urlRS.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String result="";
		for(int i=0;i<rec.length;i++){
			result+=rec[i]+(i==rec.length-1?"":"::");
		}
		return result;
	}

	public void updateInitialUrl(String url) {
		try {
			PreparedStatement urlStmt = conn
					.prepareStatement("update seed set status = 1 where url = ?");
			urlStmt.setString(1, url);
			urlStmt.executeUpdate();
			urlStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * args:
	 * 0--网站标记
	 * 1--网站链接
	 * 2--网页名
	 * 3--列表标记
	 * 4--标题标记
	 * 5--来源标记
	 * 6--来源关键词
	 * 7--日期标记
	 * 8--正文标记
	 * 9--列表翻页后缀
	 * 10--第二页的页码
	 * 11--网页文件类型
	 * 12--爬取页面数
	 */
	public void parseUrl(String[] args) {
		
		try {
			boolean flag=true;
			int i=1;
			while(flag){
				Document doc = null;
				int pageIndex=Integer.parseInt(args[10]);
				if(i>Integer.parseInt(args[12])){
					break;
				}
				try{
					if (i == 1) {
						System.out.println(args[1]+(args[11]!=null&&!args[11].equals("null")?args[11]:""));
						doc = Jsoup.connect(args[1]+(args[11]!=null&&!args[11].equals("null")?args[11]:"")).get();
					} else {
						System.out.println(args[1] + args[9].replace("{1}", Integer.toString(i-2+pageIndex)) +(args[11]!=null&&!args[11].equals("null")?args[11]:""));
						doc = Jsoup.connect(args[1] + args[9].replace("{1}", Integer.toString(i-2+pageIndex)) +(args[11]!=null&&!args[11].equals("null")?args[11]:"")).get();
					}
				}
				catch (HttpStatusException e) {
					System.out.println(e.getMessage());
					i+=1;
					continue;
				}
				Elements aTags = null;
				if(args[3].contains("parent:")){
					String[] json=args[3].split(",");
					Elements list=doc.select(json[0].split(":")[1]);
					if(list.size()==0){
						i+=1;
						continue;
					}
					if(json.length>1){
						aTags=list.get(0).getElementsByAttributeValue(json[1].split(":")[0], json[1].split(":")[1]);
					}
					else{
						aTags=list.get(0).select("a");
					}
				}
				else{
					aTags=doc.select(args[3]);
				}
				try {
					for (int j = 0; j < aTags.size(); j++) {
						String detailUrl = aTags.get(j).absUrl("href");
						int a = extractInfor(detailUrl,args,0);
						if(a==0) return;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				i+=1;
				lastMD5=BaseUtil.encryptMD5(doc.text());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int extractInfor(String url,String[] args,int reconnect) throws SQLException {
		PreparedStatement urlStmt;
		try{
			urlStmt = conn.prepareStatement("select * from news where url_md5 = ?");
			urlStmt.setString(1, BaseUtil.encryptMD5(url));
			ResultSet rs=urlStmt.executeQuery();
			if(rs.next()){
				urlStmt.close();
				updateInitialUrl(args[2]);                   //遇到已经抓取的新闻则更新status跳转到下一个网站
				return 0;
			}
			urlStmt.close();
			urlStmt=null;
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		String titleString = "";
		String sourceString = "";
		String publishString = "";
		String bodyString = "";
		//System.out.println(url);

		try {
			Document doc = Jsoup.connect(url).get();

			Elements titleElements = doc.select(args[4]);
			//System.out.println(titleElements.size());
			for (int i = 0; i < titleElements.size(); i++) {
				if(titleElements.get(i).text().length()>0){
					titleString += titleElements.get(i).text();
				}
			}
			
			String[] bodyTags=args[8].split(";");
			for(int i=0;i<bodyTags.length;i++){
				Elements bodyElements = doc.select(bodyTags[i]);
				if(bodyElements.size()>0){
					bodyString = bodyElements.get(0).text();
				}
			}
			bodyString=bodyString.substring(0, Math.max(0, Math.min(bodyString.length()-1, 500)));
			//System.out.println(bodyString);
			if(bodyString.length()==0){
				return 1;
			}
			
			if(args[7]!=null&&!args[7].equals("null")){
				Elements dateElements = doc.select(args[7]);
				Pattern dateReg=Pattern.compile("\\d{2,4}-\\d{1,2}-\\d{1,2}\\s+\\d{0,2}\\:*\\d{0,2}\\:*\\d{0,2}");
				Pattern dateReg2=Pattern.compile("\\d{2,4}-\\d{1,2}-\\d{1,2}\\b");
				Pattern dateReg3 = Pattern.compile("\\d{2,4}年\\d{1,2}月\\d{1,2}日");
				//System.out.println(b12Elements.size());
				for (int i = 0; i < dateElements.size(); i++) {
					Matcher matcher=dateReg.matcher(dateElements.get(i).text());
					Matcher matcher2=dateReg2.matcher(dateElements.get(i).text());
					Matcher matcher3=dateReg3.matcher(dateElements.get(i).text());
					if(matcher.find()){
						publishString = matcher.group();
					}
					else if(matcher2.find()){
							publishString = matcher2.group();
						}
					
					else if(matcher3.find()){
						publishString = matcher3.group();
					}
				}
			}
			if(args[5]!=null&&!args[5].equals("null")){
				Elements fromElements = doc.select(args[5]);
				String str = fromElements.text();
				String htm = fromElements.html().toString();
				Pattern fromReg = Pattern.compile(args[6]);
               
				Matcher matcher = fromReg.matcher(str);
				Matcher matcher1 = fromReg.matcher(htm);
				if(matcher.find()){
					sourceString = matcher.group();
				}
				else if(matcher1.find()){
						sourceString = matcher1.group();
				}		
		  }
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("error url:"+url);
			if(reconnect==2){
				return 1;
			}
			else{
				extractInfor(url, args, reconnect+1);
				return 1;
			}
		} 

		try {
			
			urlStmt = conn
					.prepareStatement("insert into news(class,url,url_md5,title,source,publish_time,body,web) values (?,?,?,?,?,?,?,?)");
			urlStmt.setBytes(1, args[2].getBytes());
			urlStmt.setBytes(2, url.getBytes());
			urlStmt.setBytes(3, BaseUtil.encryptMD5(url).getBytes());
			urlStmt.setBytes(4, titleString.getBytes());
			urlStmt.setBytes(5, sourceString.getBytes());
			urlStmt.setString(6, publishString.length()>0?publishString:null);
			urlStmt.setBytes(7, bodyString.getBytes());
			urlStmt.setBytes(8, args[0].getBytes());
			urlStmt.executeUpdate();
			urlStmt.close();
			
			//System.out.println("****    " + (++count) + "   ******");
			
		} catch (SQLException e1) {
			e1.printStackTrace();
			System.out.println("error source:"+bodyString+","+url);
		} 
       return 1;
	}
}
