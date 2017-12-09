package org.leolo.miniwebserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.leolo.miniwebserver.ErrorPageRepository.ErrorPageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
	public static void main(String [] args) throws InterruptedException, IOException{
		
		Logger logger = LoggerFactory.getLogger(Server.class);
		ServerSocket ss = new ServerSocket(8080);
		ServerBusyErrorThread.getInstance();
		Properties prop = new Properties();
		prop.load(new java.io.FileInputStream("web.properties"));
		ErrorPageRepository.getInstance().setReplaceSet(prop);
		ErrorPageRepository.getInstance().loadErrorPage(503,"503.html",ErrorPageType.STATIC);
		ErrorPageRepository.getInstance().loadErrorPage(404,"404.html",ErrorPageType.DYMANIC);
		logger.info("Server ready");
		while(true){
			Socket s = ss.accept();
			ServerBusyErrorThread.getInstance().addEntry(s);
		}
	}
}
