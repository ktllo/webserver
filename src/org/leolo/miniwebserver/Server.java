package org.leolo.miniwebserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

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
		
		ErrorPageRepository.loadErrorPage(404,"404.html",ErrorPageType.DYMANIC);
		
		Server server = new Server();
		server.start();
	}
	
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
}
