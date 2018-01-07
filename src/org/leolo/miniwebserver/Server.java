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
		server.addServletMapping("hello.do", org.leolo.miniwebserver.sample.SampleServlet.class, 50);
		server.addServletMapping("*", "org.leolo.miniwebserver.sample.Count", Integer.MAX_VALUE);
		
	}
	
	/**
	 * Creates a default instance of the server.
	 */
	public Server(){
		servlets = new ServletRepository();
	}
	
	private ServletRepository servlets;
	private String staticContentPath = "./static";
	private String defaultPage = "index.html";
	private int serverPort = 8080;
	private boolean showError = true;
	private int maxRequestBodySize = 10485760; //10MiB
	/**
	 * Start the server with the current configuration.
	 * @throws IOException The Server cannot be started because of IO exceptions
	 */
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
	
	/**
	 * Maps a new Servlet to the server.
	 * 
	 * This operation can be performed after the server is started.
	 * @param pattern The URL pattern which will trigger the servlet
	 * @param servletClass The servlet Class object. Usually obtained by <code>MyServlet.class</code>
	 * @param processOrder The priority for this servlet to be executed. The smaller the <code>processOrder</code> is,
	 * the higher priority this entry is.
	 */
	public void addServletMapping(String pattern, Class<? extends HttpServlet> servletClass, int processOrder) {
		servlets.addServletMapping(pattern, servletClass, processOrder);
	}
	/**
	 * Maps a new Servlet to the server.
	 * 
	 * This operation can be performed after the server is started.
	 * @param pattern The URL pattern which will trigger the servlet
	 * @param servletClass The fully qualified name for the servlet
	 * @param processOrder The priority for this servlet to be executed. The smaller the <code>processOrder</code> is,
	 * the higher priority this entry is.
	 */
	public void addServletMapping(String pattern, String servletClass, int processOrder) throws ClassNotFoundException {
		servlets.addServletMapping(pattern, servletClass, processOrder);
	}
	/**
	 * Maps a new Servlet to the server with {@link ServletRepository#DEFAULT_PROCESS_ORDER default priority}.
	 * 
	 * This operation can be performed after the server is started.
	 * @param pattern The URL pattern which will trigger the servlet
	 * @param servletClass The servlet Class object. Usually obtained by <code>MyServlet.class</code>
	 */
	public void addServletMapping(String pattern, String servletClass) throws ClassNotFoundException {
		servlets.addServletMapping(pattern, servletClass);
	}
	/**
	 * Maps a new Servlet to the server with {@link ServletRepository#DEFAULT_PROCESS_ORDER default priority}.
	 * 
	 * This operation can be performed after the server is started.
	 * @param pattern The URL pattern which will trigger the servlet
	 * @param servletClass The fully qualified name for the servlet
	 */
	public void addServletMapping(String pattern, Class<? extends HttpServlet> servletClass){
		servlets.addServletMapping(pattern, servletClass);
	}
	
	/**
	 * Delete all servlet mapping with the given URL pattern
	 * @param pattern The URL pattern
	 * @return  number of mapping entry removed
	 */
	public int deleteServletMapping(String pattern) {
		return servlets.deleteServletMapping(pattern);
	}
	/**
	 * Delete all servlet mapping with the given class
	 * @param servlet The servlet Class object. Usually obtained by <code>MyServlet.class</code>
	 * @return  number of mapping entry removed
	 */
	public int deleteServletMapping(Class<? extends HttpServlet> servlet) {
		return servlets.deleteServletMapping(servlet);
	}
	/**
	 * Delete all servlet mapping with matching URL pattern and servlet class
	 * @param pattern The URL pattern
	 * @param servlet The servlet Class object. Usually obtained by <code>MyServlet.class</code>
	 * @return  number of mapping entry removed
	 */
	public int deleteServletMapping(String pattern, Class<? extends HttpServlet> servlet) {
		return servlets.deleteServletMapping(pattern, servlet);
	}
	/**
	 * Delete all servlet mapping with matching URL pattern and servlet class
	 * @param pattern The URL pattern, <code>null</code> if this URL pattern should be ignored
	 * @param servlet The fully qualified name for the servlet, <code>null</code> if class name should be ignored
	 * @return  number of mapping entry removed
	 */
	public int deleteServletMapping(String pattern, String servlet) throws ClassNotFoundException {
		return servlets.deleteServletMapping(pattern, servlet);
	}
	
	
	@Deprecated
	Class<? extends HttpServlet> getMappedServlet(String url) {
		logger.info("Looking for {}", url);
		return servlets.getMappedServlet(url);
	}
	
	Class<? extends HttpServlet> getMappedServlet(String url, ServletRepository.MappingSearchMode mode) {
		logger.info("Looking for {}", url);
		return servlets.getMappedServlet(url, mode);
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

	public int getMaxRequestBodySize() {
		return maxRequestBodySize;
	}

	public void setMaxRequestBodySize(int maxRequestBodySize) {
		this.maxRequestBodySize = maxRequestBodySize;
	}
}
