package org.leolo.miniwebserver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.net.InetSocketAddress;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServletRequest implements javax.servlet.http.HttpServletRequest {
	private Logger logger = LoggerFactory.getLogger(HttpServletRequest.class);
	private java.net.Socket socket;
	private ServletInputStream stream;
	private Reader reader;
	private enum InputMode{
		UNBINDED,
		STREAM,
		READER;
	}
	
	private InputMode inputMode = InputMode.UNBINDED;
	private int CONTENT_LENGTH;
	HttpServletRequest(java.net.Socket socket){
		this.socket = socket;
		try {
			CONTENT_LENGTH = socket.getInputStream().available();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			CONTENT_LENGTH = -1;
		}
	}
	
	@Override
	public Object getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getContentLength() {
		return CONTENT_LENGTH;
	}

	@Override
	public long getContentLengthLong() {
		return CONTENT_LENGTH;
	}

	@Override
	public String getContentType() {
		return getHeader("Content-type");
	}
	
	InputStream data;
	static final byte [] EMPTY_BYTE_ARRAY = new byte[0];
	@Override
	public ServletInputStream getInputStream() throws IOException {
		synchronized(this){
			if(inputMode == InputMode.UNBINDED){
				inputMode = InputMode.STREAM;
				if(data!=null){
					this.stream = new ServletInputStreamImpl(data);
				}else{
					this.stream = new ServletInputStreamImpl(new ByteArrayInputStream(EMPTY_BYTE_ARRAY,0,0));
				}
			}
			return stream;
		}
	}

	@Override
	public String getParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getParameterValues(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		// TODO Auto-generated method stub
		return null;
	}
	String protocol;
	
	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public String getScheme() {
		//This server only supports HTTP
		return "http";
	}

	@Override
	public String getServerName() {
		return socket.getLocalAddress().getHostName();
	}

	@Override
	public int getServerPort() {
		return socket.getLocalPort();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return ((InetSocketAddress)socket.getRemoteSocketAddress()).toString().replace("/", "");
	}

	@Override
	public String getRemoteHost() {
		return ((InetSocketAddress)socket.getRemoteSocketAddress()).getHostName();
	}

	@Override
	public void setAttribute(String name, Object o) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAttribute(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public Locale getLocale() {
		logger.debug("Accept-Language ={}", this.getHeader("Accept-Language"));
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return socket.getPort();
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return socket.getLocalPort();
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		throw new IllegalStateException("Unsupported Operation");
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		throw new IllegalStateException("Unsupported Operation");
	}

	@Override
	public boolean isAsyncStarted() {
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDateHeader(String name) {
		// TODO Auto-generated method stub
		return 0;
	}
	HttpHeaders headers;
	@Override
	public String getHeader(String name) {
		if(name==null){
			throw new NullPointerException("Null header name");
		}
		for(HttpHeader header:headers.headers){
			if(name.equalsIgnoreCase(header.getName())){
				return header.getValue();
			}
		}
		return null;
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		if(name==null){
			throw new NullPointerException("Null header name");
		}
		Vector<String> values = new Vector<>();
		for(HttpHeader header:headers.headers){
			if(name.equalsIgnoreCase(header.getName())){
				values.add(header.getValue());
			}
		}
		return values.elements();
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		Vector<String> names = new Vector<>();
		for(HttpHeader header:headers.headers){
			names.add(header.getName());
		}
		return names.elements();
	}

	@Override
	public int getIntHeader(String name) {
		if(name==null){
			throw new NullPointerException("Null header name");
		}
		for(HttpHeader header:headers.headers){
			if(name.equalsIgnoreCase(header.getName())){
				return Integer.parseInt(header.getValue());
			}
		}
		return -1;
	}
	protected String method;
	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession(boolean create) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	@Override
	public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

}
