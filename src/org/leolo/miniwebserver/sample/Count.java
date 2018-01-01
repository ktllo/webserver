package org.leolo.miniwebserver.sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.Cookie;
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
			response.addCookie(new Cookie("SES","123"));
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
			}
			out.print("</ul>");
			Map<String, String[]> param = request.getParameterMap();
			if(param.size()>0){
				out.println("<h2>Parameters</h2>");
				out.println("<ul>");
				for(String key:param.keySet()){
					out.print("<li>");
					out.print(key);
					out.print("<ol>");
					for(String s:param.get(key)){
						out.print("<li>");
						out.print(s);
						out.print("</li>");
					}
					out.print("</ol>");
					out.print("</li>");
				}
				out.print("</ul>");
			}
			out.println("<h1>Cookies</h1>");
			for(Cookie c:request.getCookies()){
				out.print(c.toString());
				out.println("<br/>");
			}
			out.println("<h1>Sample Form</h1>");
			out.println("<form action=\"/upload.do?p=0\" method=\"post\" enctype=\"multipart/form-data\">");
			out.println("<input type=\"text\" name=\"description\" value=\"some text1\">");
			out.println("<input type=\"text\" name=\"description2\" value=\"some text2\">");
			
			out.println("<input type=\"file\" name=\"myFile\">");
			out.println("<button type=\"submit\">Submit</button>");
			out.println("</form>");
			out.println("<h1>Sample Form 2</h1>");
			out.println("<form action=\"/upload.do?p=1\" method=\"post\">");
			out.println("<input type=\"text\" name=\"description\" value=\"some text3\">");
			out.println("<input type=\"text\" name=\"description2\" value=\"some text4\">");
			
			out.println("<button type=\"submit\">Submit</button>");
			out.println("</form>");
			if(request.getContentLength()>0){
				out.println("<h1>POST data</h1><pre>");
				BufferedReader br= new BufferedReader(new InputStreamReader(request.getInputStream()));
				while(true){
					String line = br.readLine();
					if(line==null) break;
					out.println(line);
				}
				out.println("</pre>");
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
