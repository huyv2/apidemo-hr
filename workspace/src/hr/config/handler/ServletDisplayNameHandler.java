package hr.config.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ServletDisplayNameHandler extends DefaultHandler {
	
	String webAppDisplayName = "";
	
	boolean bWebApp = false;
	boolean bWebAppDisplayName = false;
	
	public String getWebAppDisplayName() {
		return webAppDisplayName;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("web-app")) {
			bWebApp = true;
		} else if (bWebApp && qName.equals("display-name")) {
			bWebAppDisplayName = true;
		}
	}
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (bWebApp && bWebAppDisplayName && qName.equals("display-name")) {
			bWebAppDisplayName = bWebApp = false;
		}
	}
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (bWebApp && bWebAppDisplayName) {
			webAppDisplayName = new String(ch, start, length);
		}
	}
}
