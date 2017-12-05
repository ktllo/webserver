package org.leolo.miniwebserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerThread extends Thread {
	Logger logger = LoggerFactory.getLogger(ServerThread.class);
	
	private Socket socket;
	
	
	ServerThread(Socket socket){
		logger.info("Connection from "+socket.getInetAddress()+"/"+socket.getPort());
		this.setName("MWST-"+getId(socket));
		this.socket = socket;
	}
	
	
	
	public static int getThreadCount(){
		int threadCount = 0;
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for ( Thread t : threadSet){
            if ( t instanceof ServerThread){
                ++threadCount;
            }
        }
		return threadCount;
	}
	
	public void run(){
		logger.debug("Thread started");
		final String MESSAGE = "<html><body><p style=\"font-size:32pt;color:red\">Hello , Thread ID = "+this.getName()+"</p></body></html>";
        try {
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			out.println("HTTP/1.1 200 OK");
			out.println("Content-type: text/html");
			out.println("Content-length: "+MESSAGE.length());
			out.println();
			out.println(MESSAGE);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        logger.debug("Thread ended");
    }
	
	private static String getId(Socket s){
		int addr = s.getInetAddress().hashCode();
		int port = (s.getLocalPort()<<16)|s.getPort();
		int time = (int)(System.currentTimeMillis()/1000);
		int id = addr ^ port ^ time;
		String sid = Integer.toHexString(id);
		while(sid.length()<8){
			sid = "0" + sid;
		}
		return sid;
	}
	
}
