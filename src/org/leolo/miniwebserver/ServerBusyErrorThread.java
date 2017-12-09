package org.leolo.miniwebserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServerBusyErrorThread extends Thread {
	
	private static ServerBusyErrorThread  instance = null;
	Logger logger = LoggerFactory.getLogger(ServerBusyErrorThread.class);
	public static final int MAX_SIZE = 100;
	private int queueSize = 0;
	private Queue<Entry> queue;
	
	public synchronized static ServerBusyErrorThread getInstance(){
		if(instance == null)
			instance = new ServerBusyErrorThread();
		return instance;
	}
	
	private ServerBusyErrorThread(){
		logger.info("ServerBusyErrorThread is created");
		queue = new LinkedList<>();
		this.start();
	}
	
	class Entry{
		public Entry(Socket socket) {
			super();
			this.time = System.currentTimeMillis();
			this.socket = socket;
		}
		long time;
		Socket socket;
	}
	
	public void addEntry(Socket s){
		logger.info("New Entry");
		if(queueSize>MAX_SIZE){
			try {
				s.close();
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
		}
		synchronized(this){
			this.queue.add(new Entry(s));
			queueSize++;
			this.notify();
		}
		
	}
	
	public void run(){
		//Master Loop
		while(true){
			Socket s = null;
			try{
				if(queueSize==0){
					synchronized(this){
						try {
							logger.info("Waiting for new entry");
							wait();
						} catch (InterruptedException e) {
							logger.error(e.getMessage(),e);
						}
						logger.info("Has entry");
					}
				}
				synchronized(this){
					Entry e = queue.poll();
					if(e!=null){
						s = e.socket;
						logger.info("Wait time {} ms",System.currentTimeMillis() - e.time);
					}else{
						continue;
					}
				}
				try {
					PrintWriter out = new PrintWriter(s.getOutputStream());
					out.println("HTTP/1.1 503 Service Unavailable");
					out.println("Content-type: text/html");
					out.println("Connection: closed");
					out.println();
					out.println(ErrorPageRepository.getErrorPage(503));
					out.flush();
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}catch(Exception e){
				logger.error(e.getMessage(),e);
				try {
					s.close();
				} catch (IOException e1) {
					logger.error(e1.getMessage(),e1);
				}
			}
		}
	}
}
