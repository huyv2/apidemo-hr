package hr.util;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.log4j.Logger;

public class HttpUtil {
	private static final Logger log = Logger.getLogger(HttpUtil.class);
	private String value;
	
	public HttpUtil (String value) {
		this.value = value;
	}
	
	public <T> T toObject(Class<T> tClass) {
		try {
			return JsonParserUtil.toObject(value, tClass);
		} catch (Exception e) {
			log.error(e);
		}
		return null;
	}
	
	public String toString() {
		return value;
	}
	
	public static HttpUtil of (BufferedReader reader) {
		StringBuilder sb = new StringBuilder();
		try {
			String line;
		    while ((line = reader.readLine()) != null) {
		        sb.append(line);
		    }
		} catch (IOException e) {
			log.error(e);
		}
		return new HttpUtil(sb.toString());
	}
}
