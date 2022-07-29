package hr.init;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.google.gson.reflect.TypeToken;

import hr.cache.Cache;
import hr.cache.CacheManager;
//import hr.cache.constant.TimeUnit;
import hr.config.Config;
import hr.config.handler.XmlParserSAX;
import hr.constant.CacheName;
import hr.dto.HeaderDto;
import hr.dto.request.RequestBaseDto;
import hr.factory.ExecutorFactory;
import hr.util.FileUtil;
import hr.util.JsonParserUtil;


public class ApplicationServletInit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public final Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		log.info("_______________________ eDemo is starting! _____________________________");
		
		String configPathFile = this.getServletContext().getRealPath("/WEB-INF/web.xml");
		log.info(String.format("configPathFile: %s", configPathFile));
		
		Config.loadConfig();
		loadLiteralClassCache(configPathFile);
		loadRules();
		loadErrorCode();
		//CacheManager.getInstance().createCache(CacheName.REQUEST_ID_CACHE_NAME, TimeUnit.MINUTE, 10);
		CacheManager.getInstance().init();
		
		ExecutorFactory.init();
		
		log.info("_______________________ eDemo started! ______________________________");
	}
	
	@SuppressWarnings("rawtypes")
	private void loadLiteralClassCache(String configPathFile) {
		try {
			String rootPackageName = "hr.dto.request";
			Map<String, String> configMap = XmlParserSAX.getUriMappingObject(configPathFile);
			List<Class> literalList = getClasses(rootPackageName, "Dto.class");
			if (literalList.size() > 0) {
				Cache cache = (Cache) CacheManager.getInstance().createCache(CacheName.LITERAL_CLASS_CACHE_NAME);
				for(Class literal : literalList) {
					String name = literal.getName();
					int beginLocationEdemoName = name.indexOf('.', rootPackageName.length()) + 1;
					int endLocationEdemoName = name.indexOf('.', beginLocationEdemoName);
					String eDemoName = name.substring(beginLocationEdemoName, endLocationEdemoName);
					cache.put(configMap.get(eDemoName + literal.getSimpleName()), literal);
				}
			}
		} catch(IOException | ClassNotFoundException e) {
			log.error(e);
		}
	}
	private void loadRules() {
		CacheManager.getInstance().createCache(CacheName.RULE_VALIDATION_CACHE_NAME);
		loadFieldHeaderRules();
		loadAllDataRules();
	}
	private void loadFieldHeaderRules() {
		try {
			HeaderDto headerRuleValueObject = new HeaderDto("", "", "");
			Class<?> classHeader = headerRuleValueObject.getClass();
			String headerName = classHeader.getSimpleName();
			Field[] fields = classHeader.getDeclaredFields();
			boolean isPut = false;
			for(Field field : fields) {
				field.setAccessible(true);
				String value = Config.getValue(new StringBuilder(headerName).append("_").append(field.getName()).toString());
				if (value != null && !value.isEmpty()) {
					field.set(headerRuleValueObject, value);
					isPut = true;
				}
			}
			if (isPut) {
				Cache cache = (Cache) CacheManager.getInstance().getCache(CacheName.RULE_VALIDATION_CACHE_NAME);
				cache.put(headerName, headerRuleValueObject);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.error(e);
		}
	}
	@SuppressWarnings("rawtypes")
	private void loadAllDataRules() {
		try {
			List<Class> literalList = getClasses("hr.dto.request", "Data.class");
			if (literalList.size() > 0) {
				Cache cache = (Cache) CacheManager.getInstance().getCache(CacheName.RULE_VALIDATION_CACHE_NAME);
				for(Class literal : literalList) {
					loadFieldDataRules(literal, cache);
				}
			}
		} catch(IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			log.error(e);
		}
	}
	private void loadFieldDataRules(Class<?> literal, Cache cache) throws InstantiationException, IllegalAccessException {
		Object dataRuleValueObject = literal.newInstance();
		Class<?> classData = dataRuleValueObject.getClass();
		String dataName = classData.getSimpleName();
		Field[] fields = classData.getDeclaredFields();
		boolean isPut = false;
		for(Field field : fields) {
			field.setAccessible(true);
			String value = Config.getValue(new StringBuilder(dataName).append("_").append(field.getName()).toString());
			if (value != null && !value.isEmpty()) {
				field.set(dataRuleValueObject, value);
				isPut = true;
			}
		}
		if (isPut) {
			cache.put(dataName, dataRuleValueObject);
		}
	}
    @SuppressWarnings("rawtypes")
	private List<Class> getClasses(String packageName, String endWith) throws IOException, ClassNotFoundException {
    	List<Class> classes = new ArrayList<Class>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = (URL) resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName, endWith));
        }
    	
        return classes;
    }
    @SuppressWarnings("rawtypes")
	private static List<Class> findClasses(File directory, String packageName, String endWith) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
        	String name = file.getName();
            if (file.isDirectory()) {
                assert !name.contains(".");
                //if (!name.equalsIgnoreCase("data")) {
                	classes.addAll(findClasses(file, new StringBuilder(packageName).append(".").append(name).toString(), endWith));
                //}
            } else if (file.getName().endsWith(endWith)) {
            	if (endWith.equals("Dto.class")) {
	            	name = name.substring(0, name.length() - 6);
	            	String className = new StringBuilder(packageName).append('.').append(name).toString();
	            	if (!RequestBaseDto.class.equals(Class.forName(className)) &&
	            			RequestBaseDto.class.isAssignableFrom(Class.forName(className))) {
	            		Class<? extends RequestBaseDto> literalElement = Class.forName(className).asSubclass(RequestBaseDto.class);
	            		classes.add(literalElement);
	            	}
            	} else if (endWith.equals("Data.class")) {
	            	name = name.substring(0, name.length() - 6);
	            	String className = new StringBuilder(packageName).append('.').append(name).toString();
            		Class<? extends Object> literalElement = Class.forName(className);
            		classes.add(literalElement);
            	}
            }
        }
        
        return classes;
    }
	
	private void loadErrorCode() {
		String codeJson = FileUtil.readFile(Config.class.getResource("errorcode.json").getFile());
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		Map<String, String> codeMap = JsonParserUtil.toObject(codeJson, type);
		Cache cache = (Cache) CacheManager.getInstance().createCache(CacheName.ERROR_CODE_CACHE_NAME);
		cache.putAllStr(codeMap);
	}
	
	@Override
	public void destroy() {
		log.info("_______________________ eDemo is stopping! _____________________________");
		
		ExecutorFactory.shutdown();
		CacheManager.getInstance().destroy();
		super.destroy();
		
		log.info("_______________________ eDemo stopped! ______________________________");
	}
}
