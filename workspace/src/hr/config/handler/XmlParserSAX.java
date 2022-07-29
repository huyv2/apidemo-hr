package hr.config.handler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import hr.config.object.ServletMappingObject;
import hr.config.object.ServletObject;

public class XmlParserSAX {
	private static final Logger log = Logger.getLogger(XmlParserSAX.class);
	
	public static Map<String, String> getUriMappingObject(String configPathFile) {
		Map<String, String> resultMap = null;
		try {
			resultMap = new HashMap<String, String>();
			
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();
			
			ServletObjectHandler servletObjectHandler = new ServletObjectHandler();
			ServletMappingObjectHandler servletMappingObjectHandler = new ServletMappingObjectHandler();
			ServletDisplayNameHandler servletDisplayNameHandler = new ServletDisplayNameHandler();
			File configFile = new File(configPathFile);
			
			saxParser.parse(configFile, servletObjectHandler);
			saxParser.parse(configFile, servletMappingObjectHandler);
			saxParser.parse(configFile, servletDisplayNameHandler);
			
			List<ServletObject> servletObjectList = servletObjectHandler.getServletObjectList();
			List<ServletMappingObject> servletMappingObject = servletMappingObjectHandler.getServletMappingObjectList();
			String displayName = servletDisplayNameHandler.getWebAppDisplayName();
			
			StringBuilder sbBaseUri = null;
			
			for(int i = 0; i < servletObjectList.size(); i++) {
				for(int j = 0; j < servletMappingObject.size(); j++) {
					if (servletObjectList.get(i).getServletName().trim().equals(servletMappingObject.get(j).getServletName().trim())) {
						sbBaseUri = new StringBuilder("/").append(displayName);
						String functionName = servletObjectList.get(i).getServletClass().trim();
						int beginLocationEdemoName = functionName.indexOf("function.") + 9;
						int endLocationEdemoName = functionName.indexOf('.', beginLocationEdemoName);
						String eDemoName = functionName.substring(beginLocationEdemoName, endLocationEdemoName);
						String functionSimpleName = functionName.substring(functionName.lastIndexOf('.') + 1, functionName.length());
						String dtoSimpleName = functionSimpleName.substring(0, functionSimpleName.lastIndexOf("Function")) + "Dto";
						resultMap.put(eDemoName + dtoSimpleName, sbBaseUri.append(servletMappingObject.get(j).getUrlPattern().trim()).toString());
						break;
					}
				}
			}
		} catch(ParserConfigurationException | SAXException | IOException e) {
			log.error(e);
		}
		return resultMap;
	}
}
