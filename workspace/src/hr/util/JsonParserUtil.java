package hr.util;

import java.lang.reflect.Type;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class JsonParserUtil {
	private static final Logger log = Logger.getLogger(JsonParserUtil.class);
	
	public static String toJson(Object object) {
		String result = "";
		
		try {
			Gson gson = new Gson();
			result = gson.toJson(object);
		} catch(Exception e) {
			log.error(e);
		}
		
		return result;
	}
	
	public static <T> T toObject(String json, Class<T> tClass) {
		T result = null;
		
		try {
			Gson gson = new Gson();
			result = (T) gson.fromJson(json, tClass);
		} catch(Exception e) {
			log.error(e);
		}
			
		return result;
	}
	
	public static <T> T toObject(String json, Type type) {
		T result = null;
		
		try {
			Gson gson = new Gson();
			result = gson.fromJson(json, type);
		} catch(Exception e) {
			log.error(e);
		}
			
		return result;
	}
	
	public static <T> T parseFileToObject(String pathFile, Class<T> tClass) {
		String json = FileUtil.readFile(pathFile);
		T result = toObject(json, tClass);
		return result;
	}
	 
}
