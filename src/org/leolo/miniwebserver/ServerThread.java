package org.leolo.miniwebserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

import org.leolo.miniwebserver.http.HttpHeaders;
import org.leolo.miniwebserver.http.MalformedHeaderException;
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
		try {
			String firstLine = null;
			HttpHeaders headers = new HttpHeaders();
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			try{
				while(true){
					String line = br.readLine();
					if(line==null ||  line.length() == 0) break;
					logger.debug(line);
					if(firstLine==null){
						firstLine = line;
					}else{
						headers.addHeader(line);
					}
				}
				logger.info("OK");
			}catch(MalformedHeaderException mhe){
				logger.warn(mhe.getMessage(), mhe);
				try {
					PrintWriter out = new PrintWriter(socket.getOutputStream());
					out.println("HTTP/1.1 400 Bad Request");
					out.println("Content-type: text/html");
					out.println("Connection: closed");
					out.println();
					out.println(ErrorPageRepository.getErrorPage(400));
					out.flush();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			socket.close();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
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
