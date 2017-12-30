package org.leolo.miniwebserver.sample;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleServlet extends HttpServlet{
	Logger logger = LoggerFactory.getLogger(SampleServlet.class);
	private static final long serialVersionUID = -5760260488735565717L;
	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		doGet(request, response);
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		response.setContentType("text/html");
		
		try {
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE html><html><body>hello, world!</body></html>");
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e);
		}
		
	}

}
