package org.leolo.miniwebserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServlet;

import org.leolo.miniwebserver.http.HttpHeaders;
import org.leolo.miniwebserver.http.MalformedHeaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ServerThread extends Thread {
	Logger logger = LoggerFactory.getLogger(ServerThread.class);
	private static final Marker USAGE = MarkerFactory.getMarker("USAGE");
	private Socket socket;
	private Server server;
	
	private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	ServerThread(Server server,Socket socket){
		logger.info("Connection from "+socket.getInetAddress()+"/"+socket.getPort());
		this.setName("MWST-"+getId(socket));
		this.socket = socket;
		this.server = server;
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
					if(firstLine==null){
						firstLine = line;
					}else{
						headers.addHeader(line);
					}
				}
				String method;
				String requestPath;
				try{
					StringTokenizer st = new StringTokenizer(firstLine);
					method = st.nextToken();
					requestPath = st.nextToken();
				}catch(RuntimeException re){
					throw new MalformedHeaderException(re.getClass().getName()+":"+re.getMessage(), re);
				}
				logger.info("Request method is {}, path is {}", method, requestPath);
				int delimPos = requestPath.indexOf("?");
				String queryString = "";
				if(delimPos > 0){
					queryString = requestPath.substring(delimPos+1);
					requestPath = requestPath.substring(0, delimPos);
				}
				requestPath = URLDecoder.decode(requestPath, "UTF-8");
				Class<? extends HttpServlet> servlet = server.getMappedServlet(requestPath);
				if(servlet != null){
					//Dynamic content
				}else{
					//Static content
					processStaticRequest(requestPath);
				}
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
			try {
				socket.close();
			} catch (IOException e1) {
				logger.error(e1.getMessage(),e1);
			}
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
	
	private void processStaticRequest(String path) throws IOException{
		logger.info("Looking for {}",path);
		File targetFile = new File(server.getStaticContentPath()+path);
		if(targetFile.exists()){
			
		}else{
			HashMap<Object, Object> errorMap = new HashMap<>();
			errorMap.put("pagename", path);
			String page = ErrorPageRepository.getErrorPage(404, errorMap);
			logger.info(USAGE, "{} [{}] {} 404 {}", socket.getInetAddress(), logDateFormat.format(new Date()), path, page.length());
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			out.println("HTTP/1.1 404 Not Found");
			out.println("Content-type: text/html");
			out.println("Connection: closed");
			out.println();
			out.println(page);
			out.flush();
		}
	}
	
}
