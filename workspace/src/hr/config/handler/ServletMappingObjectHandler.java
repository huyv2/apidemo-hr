package hr.config.handler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import hr.config.object.ServletMappingObject;

public class ServletMappingObjectHandler extends DefaultHandler {
	
	private List<ServletMappingObject> servletMappingObjectList = null;
	ServletMappingObject servletMappingObject = null;
	private StringBuilder data = null;
	
	boolean bServletMapping = false;
	boolean bServletName = false;
	boolean bUrlPattern = false;
	
	public List<ServletMappingObject> getServletMappingObjectList() {
		return servletMappingObjectList;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("servlet-mapping")) {
			servletMappingObject = new ServletMappingObject();
			if (servletMappingObjectList == null) {
				servletMappingObjectList = new ArrayList<ServletMappingObject>();
			}
			bServletMapping = true;
		} else if (bServletMapping && qName.equals("servlet-name")) {
			data = new StringBuilder();
			bServletName = true;
		} else if (bServletMapping && qName.equals("url-pattern")) {
			data = new StringBuilder();
			bUrlPattern = true;
		}
	}
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (bServletName) {
			servletMappingObject.setServletName(data.toString());
			bServletName = false;
		} else if (bUrlPattern) {
			servletMappingObject.setUrlPattern(data.toString());
			bUrlPattern = false;
		}
		if (qName.equals("servlet-mapping")) {
			servletMappingObjectList.add(servletMappingObject);
			bServletMapping = false;
		}
	}
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (bServletName || bUrlPattern) {
			data.append(new String(ch, start, length));
		}
	}
}
