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
	
	public static void main(String [] args) throws InterruptedException, IOException, ClassNotFoundException{
		
		Properties prop = new Properties();
		prop.load(new java.io.FileInputStream("web.properties"));
		ErrorPageRepository.setReplaceSet(prop);
		ErrorPageRepository.loadErrorPage(503,"503.html",ErrorPageType.STATIC);
		ErrorPageRepository.loadErrorPage(400,"400.html",ErrorPageType.STATIC);
		ErrorPageRepository.loadErrorPage(404,"404.html",ErrorPageType.DYNAMIC);
		ErrorPageRepository.loadErrorPage(403,"403.html",ErrorPageType.DYNAMIC);
		ErrorPageRepository.loadErrorPage(8000,"500_stacktrace.html",ErrorPageType.DYNAMIC);
		
		Server server = new Server();
		server.start();
		server.addServletMapping("hello.do", org.leolo.miniwebserver.sample.SampleServlet.class);
		server.addServletMapping("*.do", "org.leolo.miniwebserver.sample.Count", Integer.MAX_VALUE);
		
	}
	
	public Server(){
		servlets = new ServletRepository();
	}
	
	private ServletRepository servlets;
	private String staticContentPath = "./static";
	private String defaultPage = "index.html";
	private int serverPort = 8080;
	private boolean showError = true;
	
	public void start() throws IOException{
		ServerSocket ss = new ServerSocket(serverPort);
		ServerBusyErrorThread.getInstance();
		
		MimeTypeRepo.getInstance();
		logger.info("Server ready");
		new Thread(new Runnable(){

			@Override
			public void run() {
				while(true){
					Socket s;
					try {
						s = ss.accept();
						new ServerThread(Server.this, s).start();
					} catch (IOException e) {
						logger.error(e.getMessage(),e);
					}
				}
			}
			
		}).start();
		
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

	public void addServletMapping(String pattern, Class<? extends HttpServlet> servletClass){
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
		logger.info("Looking for {}", url);
		return servlets.getMappedServlet(url);
	}

	public String getStaticContentPath() {
		return staticContentPath;
	}

	public void setStaticContentPath(String staticContentPath) {
		this.staticContentPath = staticContentPath;
	}

	public String getDefaultPage() {
		return defaultPage;
	}

	public void setDefaultPage(String defaultPage) {
		this.defaultPage = defaultPage;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public boolean isShowError() {
		return showError;
	}

	public void setShowError(boolean showError) {
		this.showError = showError;
	}
}
