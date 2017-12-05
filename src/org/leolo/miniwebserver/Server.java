package org.leolo.miniwebserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String [] args) throws InterruptedException, IOException{
		ServerSocket ss = new ServerSocket(8080);
		while(true){
			Socket s = ss.accept();
			if(ServerThread.getThreadCount() < 100){
				new ServerThread(s).start();
			}else{
				s.close();
			}
		}
	}
}
