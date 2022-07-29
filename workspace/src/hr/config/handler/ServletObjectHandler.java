package hr.config.handler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import hr.config.object.ServletObject;

public class ServletObjectHandler extends DefaultHandler {
	
	private List<ServletObject> servletObjectList = null;
	private ServletObject servletObject = null;
	private StringBuilder data = null;
	
	boolean bServlet = false;
	boolean bServletName = false;
	boolean bServletClass = false;
	boolean bLoadOnStartup = false;
	
	public List<ServletObject> getServletObjectList() {
		return servletObjectList;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("servlet")) {
			servletObject = new ServletObject();
			if (servletObjectList == null) {
				servletObjectList = new ArrayList<ServletObject>();
			}
			bServlet = true;
		} else if (bServlet && qName.equals("servlet-name")) {
			data = new StringBuilder();
			bServletName = true;
		} else if (bServlet && qName.equals("servlet-class")) {
			data = new StringBuilder();
			bServletClass = true;
		} else if (bServlet && qName.equals("load-on-startup")) {
			bLoadOnStartup = true;
		}
	}
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (bServletName) {
			servletObject.setServletName(data.toString());
			bServletName = false;
		} else if (bServletClass) {
			servletObject.setServletClass(data.toString());
			bServletClass = false;
		}
		if (qName.equals("servlet")) {
			if (!bLoadOnStartup) {
				servletObjectList.add(servletObject);
			}
			bLoadOnStartup = false;
			bServlet = false;
		}
	}
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (bServletName || bServletClass) {
			data.append(new String(ch, start, length));
		}
	}
}
