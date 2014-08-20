package never.entry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import never.utils.DbUtil;

public class Timer extends Thread {
	public boolean flag=true;
	private Connection conn = null;
	private int hour,minute;
	
	public Timer() {
		flag=true;
	}
	

	
	@Override
	public void run() {
		while(flag){
			PreparedStatement urlStmt;
			if(conn==null){
				conn = DbUtil.instance().getConnection();
			}
			try {
				urlStmt = conn
						.prepareStatement("select infos from localvalue where valueName = ?");
				urlStmt.setString(1, "dailyTime");
				ResultSet urlRS = urlStmt.executeQuery();
				
				while (urlRS.next()) {
					String time=urlRS.getString(1);
					hour=Integer.parseInt(time.split(":")[0]);
					minute=Integer.parseInt(time.split(":")[1]);
					break;
				}
				urlStmt.close();
				urlStmt=null;
				GregorianCalendar gc=new GregorianCalendar();
				gc.setTimeInMillis(System.currentTimeMillis());
				if(gc.get(Calendar.HOUR_OF_DAY)==hour&&gc.get(Calendar.MINUTE)==minute){
					urlStmt = conn
							.prepareStatement("update seed set status = 0 where status = 1");
					urlStmt.executeUpdate();
					urlStmt.close();
					Crawler.instance().startCrawler();
					try {
						sleep(600000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else{
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
}
