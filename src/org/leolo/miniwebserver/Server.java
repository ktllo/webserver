package org.leolo.miniwebserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import javax.servlet.http.HttpServlet;

import org.leolo.miniwebserver.ErrorPageRepository.ErrorPageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
	static Logger logger = LoggerFactory.getLogger(Server.class);
	
	public static void main(String [] args) throws InterruptedException, IOException{
		
		Properties prop = new Properties();
		prop.load(new java.io.FileInputStream("web.properties"));
		ErrorPageRepository.setReplaceSet(prop);
		ErrorPageRepository.loadErrorPage(503,"503.html",ErrorPageType.STATIC);
		ErrorPageRepository.loadErrorPage(400,"400.html",ErrorPageType.STATIC);
		ErrorPageRepository.loadErrorPage(404,"404.html",ErrorPageType.DYNAMIC);
		Server server = new Server();
		server.start();
	}
	
	public Server(){
		servlets = new ServletRepository();
	}
	
	private ServletRepository servlets;
	private String staticContentPath = "/static";
	
	
	public void start() throws IOException{
		ServerSocket ss = new ServerSocket(8080);
		ServerBusyErrorThread.getInstance();
		
		MimeTypeRepo.getInstance();
		logger.info("Server ready");
		while(true){
			Socket s = ss.accept();
			new ServerThread(this, s).start();
		}
	}

	public void addServletMapping(String pattern, Class<? extends HttpServlet> servletClass, int processOrder) {
		servlets.addServletMapping(pattern, servletClass, processOrder);
	}

	public void addServletMapping(String pattern, String servletClass, int processOrder) throws ClassNotFoundException {
		servlets.addServletMapping(pattern, servletClass, processOrder);
	}

	public void addServletMapping(String pattern, String servletClass) throws ClassNotFoundException {
		servlets.addServletMapping(pattern, servletClass);
	}

	public void addServletMapping(String pattern, Class<? extends HttpServlet> servletClass)
			throws ClassNotFoundException {
		servlets.addServletMapping(pattern, servletClass);
	}

	public int deleteServletMapping(String pattern) {
		return servlets.deleteServletMapping(pattern);
	}

	public int deleteServletMapping(Class<? extends HttpServlet> servlet) {
		return servlets.deleteServletMapping(servlet);
	}

	public int deleteServletMapping(String pattern, Class<? extends HttpServlet> servlet) {
		return servlets.deleteServletMapping(pattern, servlet);
	}

	public int deleteServletMapping(String pattern, String servlet) throws ClassNotFoundException {
		return servlets.deleteServletMapping(pattern, servlet);
	}

	Class<? extends HttpServlet> getMappedServlet(String url) {
		return servlets.getMappedServlet(url);
	}

	public String getStaticContentPath() {
		return staticContentPath;
	}

	public void setStaticContentPath(String staticContentPath) {
		this.staticContentPath = staticContentPath;
	}
}
