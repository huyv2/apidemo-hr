package hr.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config {
	private static final Logger log = Logger.getLogger(Config.class);
	
	private static Properties props = null;
	
	public static void loadConfig() {
		if (props == null) {
			InputStream isConfig = null;
			try {
				String pathFile = java.net.URLDecoder.decode(Config.class.getResource("application.properties").getPath(), "UTF-8");
				props = new Properties();
				isConfig = new FileInputStream(pathFile);
				props.loadFromXML(isConfig);
				log.debug("Loading config file successfully!!!");
			} catch(Exception e) {
				log.error("Loading config file failed!!!", e);
			} finally {
				try {
					isConfig.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		}
	}
	
	public static String getValue(String key) {
		String result = "";
		if (props != null) {
			result = props.getProperty(key);
		}
		return result;
	}
}
