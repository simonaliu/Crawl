package never.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BaseUtil {

	public static String encryptMD5(String strInput) {
		StringBuffer buf = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(strInput.getBytes());
			byte b[] = md.digest();
			buf = new StringBuffer(b.length * 2);
			for (int i = 0; i < b.length; i++) {
				if (((int) b[i] & 0xff) < 0x10) {
					buf.append("0");
				}
				buf.append(Long.toString((int) b[i] & 0xff, 16));
			}
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return buf.toString();
	}

}
