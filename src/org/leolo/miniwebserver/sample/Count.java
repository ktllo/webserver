package org.leolo.miniwebserver.sample;

import java.io.IOException;
import java.io.PrintWriter;

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
		response.setContentType("text/html");
		
		try {
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE html>");
			out.println("<html>");
			out.println("<head>");
			out.println("<title>Server Health Info</title>");
			out.println("</head>");
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
			out.println("</li>");
			out.println("</body>");
			out.println("</html>");
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e);
		}
		
	}
}
