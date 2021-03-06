package org.leolo.miniwebserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServletResponse implements javax.servlet.http.HttpServletResponse {
	
	protected java.net.Socket socket;
	
	private OutputMode outputMode = OutputMode.UNBINDED;
	
	private ServletPrintWriter printWriter =  null;
	
	private static final Logger logger = LoggerFactory.getLogger(HttpServletResponse.class);
	
	private enum OutputMode{
		OUTPUT_STREAM,
		PRINT_WRITER,
		UNBINDED;
	}
	
	
	HttpServletResponse(java.net.Socket socket){
		this.socket = socket;
	}
	
	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}
	private String contentType = "text/html";
	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return contentType;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	@Override
	public PrintWriter getWriter() throws IOException {
		if(outputMode == OutputMode.PRINT_WRITER){
			return printWriter;
		}else if(outputMode == OutputMode.UNBINDED){
			synchronized(this){
				printWriter = new ServletPrintWriter(this);
				outputMode = OutputMode.PRINT_WRITER;
			}
			return printWriter;
		}else{
			throw new IllegalStateException("getOutputStream() already called");
		}
	}

	@Override
	public void setCharacterEncoding(String charset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentLength(int len) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentLengthLong(long len) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentType(String type) {
		contentType = type;
	}

	@Override
	public void setBufferSize(int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		if(outputMode == OutputMode.PRINT_WRITER){
			this.printWriter.flush();
			socket.getOutputStream().flush();
		}
	}

	@Override
	public void resetBuffer() {
		if(commit){
			throw new IllegalStateException("Response is committed");
		}
		if(outputMode == OutputMode.PRINT_WRITER){
			this.printWriter.close();
			outputMode = OutputMode.UNBINDED;
			try {
				this.getWriter();
			} catch (IOException e) {
			}
		}
	}

	private static final String NEW_LINE = "\r\n";
	protected synchronized void _commit(){
		
		if(!commit){
			logger.debug("Committing");
			try {
				PrintWriter out = new PrintWriter(socket.getOutputStream());
				out.print("HTTP/1.1 200 OK");out.print(NEW_LINE);
				out.print("Content-type: "+this.getContentType());out.print(NEW_LINE);
				out.print("Connection: closed");out.print(NEW_LINE);
				out.print(NEW_LINE);
				out.flush();
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
			commit = true;
			logger.debug("Committed");
		}
		
	}
	
	private boolean commit = false;
	
	@Override
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return commit;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLocale(Locale loc) {
		// TODO Auto-generated method stub

	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCookie(Cookie cookie) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsHeader(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String encodeURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeRedirectUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendError(int sc) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRedirect(String location) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDateHeader(String name, long date) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDateHeader(String name, long date) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeader(String name, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addHeader(String name, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIntHeader(String name, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addIntHeader(String name, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatus(int sc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatus(int sc, String sm) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

}
