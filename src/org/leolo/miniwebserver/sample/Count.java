package org.leolo.miniwebserver.sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.leolo.miniwebserver.ServerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Count extends HttpServlet {
	Logger logger = LoggerFactory.getLogger(SampleServlet.class);
	private static final long serialVersionUID = -7239479257831205974L;
	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		doGet(request, response);
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		logger.info("Running");
		response.setContentType("text/html");
		logger.info("Content-length is {}", request.getContentLength());
		
		try {
			
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE html>");
			out.println("<html>");
			out.println("<head>");
			out.println("<title>Server Health Info</title>");
			out.println("</head>");logger.info("head OK");
			out.println("<body>");
			out.println("<h1>Server Health Info</h1>");
			out.println("<h2>Thread Count</h2>");
			out.println("<ul>");
			out.print("<li>Active Count:");
			out.print(Thread.activeCount());
			out.println("</li>");
			out.print("<li>Total Count:");
			out.print(Thread.getAllStackTraces().size());
			out.println("</li>");
			out.print("<li>Server Thread Count:");
			out.print(ServerThread.getThreadCount());
			out.println("</li></ul>");logger.info("Thread cnt OK");
			out.println("<h2>Request Info</h2><h3>Headers</h3><ul>");
			Enumeration<String> names = request.getHeaderNames();
			while(names.hasMoreElements()){
				String name = names.nextElement();
				out.print("<li>");
				out.print(name);
				out.println("<ol>");
				Enumeration<String> values = request.getHeaders(name);
				while(values.hasMoreElements()){
					out.print("<li>");
					out.print(values.nextElement());
					out.println("</li>");
				}
				out.println("</ol>");
				out.println("</li>");
			}logger.info("Hdr OK");out.flush();
			out.println("<h1>Sample Form</h1>");
			out.println("<form action=\"/upload.do\" method=\"post\" enctype=\"multipart/form-data\">");
			out.println("<input type=\"text\" name=\"description\" value=\"some text\">");
			out.println("<input type=\"file\" name=\"myFile\">");
			out.println("<button type=\"submit\">Submit</button>");
			out.println("</form>");
			if(request.getContentLength()>0){
				out.println("<h1>POST data</h1>");
				int _size = 0;
				while(true){
					byte  [] buf =new byte[4096];
					int read = request.getInputStream().read(buf);
				}
			}
			out.println("</body>");
			out.println("</html>");
			out.flush();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new RuntimeException(e.getMessage(), e);
		}
		
	}
}
