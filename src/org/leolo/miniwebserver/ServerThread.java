package org.leolo.miniwebserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
	public static final int STREAM_COPY_BUFFER_SIZE = 4096;
	private static final String NEW_LINE = "\r\n";
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
					out.print("HTTP/1.1 400 Bad Request");out.print(NEW_LINE);
					out.print("Content-type: text/html");out.print(NEW_LINE);
					out.print("Connection: closed");out.print(NEW_LINE);
					out.print(NEW_LINE);
					out.print(ErrorPageRepository.getErrorPage(400));out.print(NEW_LINE);
					out.flush();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			socket.close();
		}catch(java.net.SocketException se){
			//Remote probably closed the connection. Ignore
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
		File targetFile = new File(server.getStaticContentPath()+path);
		logger.info("Looking for {}",targetFile.getAbsolutePath());
		if(targetFile.exists()){
			if(targetFile.getCanonicalPath().startsWith(new File(server.getStaticContentPath()).getCanonicalPath())){
				//Passed path check
				if(targetFile.isDirectory()){
					if(path.endsWith("/")){
						processStaticRequest(path+server.getDefaultPage());
					}else{
						processStaticRequest(path+"/"+server.getDefaultPage());
					}
				}else{
					logger.info(USAGE, "{} [{}] {} 200 {}", socket.getInetAddress(), logDateFormat.format(new Date()), path, targetFile.length());
					String extension = path.substring(path.lastIndexOf(".")+1);
					String mime = MimeTypeRepo.getMimeTypeByExtension(extension);
					logger.info("Ext {} is type {}", extension, mime);
					PrintWriter out = new PrintWriter(socket.getOutputStream());
					out.print("HTTP/1.1 200 OK");out.print(NEW_LINE);
					out.print("Content-type: "+mime);out.print(NEW_LINE);
					out.print("Connection: closed");out.print(NEW_LINE);
					out.print("Content-length: "+targetFile.length());out.print(NEW_LINE);
					out.print(NEW_LINE);
					out.flush();
					InputStream is = new FileInputStream(targetFile);
					logger.info("START");
					this.copyToOutputStream(is , socket.getOutputStream());
					logger.info("END");
					is.close();
				}
			}else{
				HashMap<Object, Object> errorMap = new HashMap<>();
				errorMap.put("pagename", path);
				String page = ErrorPageRepository.getErrorPage(403, errorMap);
				logger.info(USAGE, "{} [{}] {} 403 {}", socket.getInetAddress(), logDateFormat.format(new Date()), path, page.length());
				PrintWriter out = new PrintWriter(socket.getOutputStream());
				out.print("HTTP/1.1 403 Forbidden");out.print(NEW_LINE);
				out.print("Content-type: text/html");out.print(NEW_LINE);
				out.print("Connection: closed");out.print(NEW_LINE);
				out.print(NEW_LINE);
				out.print(page);out.print(NEW_LINE);
				out.flush();
			}
		}else{
			HashMap<Object, Object> errorMap = new HashMap<>();
			errorMap.put("pagename", path);
			String page = ErrorPageRepository.getErrorPage(404, errorMap);
			logger.info(USAGE, "{} [{}] {} 404 {}", socket.getInetAddress(), logDateFormat.format(new Date()), path, page.length());
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			out.print("HTTP/1.1 404 Not Found");out.print(NEW_LINE);
			out.print("Content-type: text/html");out.print(NEW_LINE);
			out.print("Connection: closed");out.print(NEW_LINE);
			out.print(NEW_LINE);
			out.print(page);out.print(NEW_LINE);
			out.flush();
		}
	}
	
	private void copyToOutputStream(InputStream in, OutputStream out) throws IOException{
		byte [] buffer = new byte [STREAM_COPY_BUFFER_SIZE];
		int offset= 0;
		int size = 0;
		while(true){
			size = in.read(buffer, 0, STREAM_COPY_BUFFER_SIZE);
			if(size<=0){
				break;
			}
			out.write(buffer, 0, size);
			offset += size;
			out.flush();
		}
	}
}
