package org.leolo.miniwebserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ServletPrintWriter extends PrintWriter {

	private HttpServletResponse response;
	
	public ServletPrintWriter(HttpServletResponse response) throws IOException {
		super(response.socket.getOutputStream());
		this.response = response;
	}
	
	public synchronized void flush(){
		response._commit();
		super.flush();
	}
	private static final String NEW_LINE = "\r\n";
	@Override
	public void println() {
		super.print(NEW_LINE);
	}
}
