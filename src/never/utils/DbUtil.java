package never.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbUtil {

	private final String DRIVER = "com.mysql.jdbc.Driver";
	private final String URL = "jdbc:mysql://localhost/db_news";
	private final String USER = "root";
	private final String PWD = "";

	private DbUtil() {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static DbUtil instance=new DbUtil();
	
	public static DbUtil instance(){
		return instance;
	}

	public synchronized Connection getConnection() {
		Connection conn = null;
		try {
			conn = (Connection) DriverManager.getConnection(URL, USER, PWD);
			System.out.println("Connect DB Successfully！");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

}
