package hr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

public class FileUtil {
	private static final Logger log = Logger.getLogger(FileUtil.class);
	
	public static final String readFile(String pathFile) {
		String result = "";
		byte[] data = readFileByteArray(pathFile);
		if (data != null) {
			try {
				result = new String(data, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error(e);
			}
		}
        
        return result;
	}
	
	public static final byte[] readFileByteArray(String pathFile) {
		byte[] data = null;
        InputStream is = null;
        try {
        	File file = new File(pathFile);
        	is = new FileInputStream(file);
        	data = new byte[(int)file.length()];
        	is.read(data);
        }
        catch(IOException e) {
        	log.debug(e);
        } finally {
        	try {
				is.close();
			} catch (IOException e) {
				log.error(e);
			}
        }
        
        return data;
	}
}
