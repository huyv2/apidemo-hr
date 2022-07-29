package hr.init;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.PropertyConfigurator;


public class Log4jServletInit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String initFile=this.getClass().getResource("log4j.properties").getPath();
		try {
			initFile=java.net.URLDecoder.decode(initFile,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.err.println("Log4jServletInit loading file :"+initFile);
		PropertyConfigurator.configure(initFile);
		try {
			URL url=config.getServletContext().getResource("/");
			String p=url.getProtocol()+"://"+url.getHost()+url.getPort()+url.getPath();
			System.err.println(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		System.err.println("Setting schedule get parameter ...");
		
		System.err.println("Log4jServletInit ...Done");
	}
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		super.doGet(request, response);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);;
	}
	
	public void initConsole(){
		String initFile=this.getClass().getResource("log4j.properties").getPath();
		try {
			initFile=java.net.URLDecoder.decode(initFile,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.err.println("Log4jServletInit loading file :"+initFile);
		PropertyConfigurator.configure(initFile);
	}
}
