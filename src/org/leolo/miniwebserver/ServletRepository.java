package org.leolo.miniwebserver;


import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;

public class ServletRepository {
	
	public static final int DEFAULT_PROCESS_ORDER = 0xffff;
	
	private TreeSet<Entry> repo;
	
	private class Entry implements Comparable<Entry>{
		Class<? extends HttpServlet> serlvet;
		Pattern urlPattern;
		int processOrder;
		@Override
		public int compareTo(Entry o) {
			if(processOrder<o.processOrder) return -1;
			if(processOrder>o.processOrder) return 1;
			if(serlvet.hashCode()<o.serlvet.hashCode()) return -1;
			if(serlvet.hashCode()>o.serlvet.hashCode()) return 1;
			return 0;
		}
	}
	
	public void addServletMapping(String pattern, Class<? extends HttpServlet> servletClass, int processOrder){
		Entry e = new Entry();
		e.serlvet = servletClass;
		e.urlPattern = Pattern.compile(pattern);
		e.processOrder = processOrder;
		repo.add(e);
	}
	
	public void addServletMapping(String pattern, String servletClass, int processOrder)throws ClassNotFoundException{
		addServletMapping(pattern, (Class<? extends HttpServlet>) Class.forName(servletClass), DEFAULT_PROCESS_ORDER);
	}
	public void addServletMapping(String pattern, String servletClass) throws ClassNotFoundException{
		addServletMapping(pattern, servletClass, DEFAULT_PROCESS_ORDER);
	}
	public void addServletMapping(String pattern, Class<? extends HttpServlet> servletClass) throws ClassNotFoundException{
		addServletMapping(pattern, servletClass, DEFAULT_PROCESS_ORDER);
	}
	
}
