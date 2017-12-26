package org.leolo.miniwebserver;


import java.util.HashSet;
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
		addServletMapping(pattern, (Class<? extends HttpServlet>) Class.forName(servletClass), processOrder);
	}
	
	public void addServletMapping(String pattern, String servletClass) throws ClassNotFoundException{
		addServletMapping(pattern, servletClass, DEFAULT_PROCESS_ORDER);
	}
	
	public void addServletMapping(String pattern, Class<? extends HttpServlet> servletClass) throws ClassNotFoundException{
		addServletMapping(pattern, servletClass, DEFAULT_PROCESS_ORDER);
	}
	
	//Base delete method 1: with matching pattern
	public int deleteServletMapping(String pattern){
		HashSet<Entry> pendingDelete = new HashSet<>();
		for(Entry e:repo){
			if(e.urlPattern.pattern().equals(pattern)){
				pendingDelete.add(e);
			}
		}
		repo.removeAll(pendingDelete);
		return pendingDelete.size();
	}
	
	//Base delete method 2: with matching servlet
	public int deleteServletMapping(Class<? extends HttpServlet> servlet){
		HashSet<Entry> pendingDelete = new HashSet<>();
		for(Entry e:repo){
			if(e.serlvet.getName().equals(servlet.getName())){
				pendingDelete.add(e);
			}
		}
		repo.removeAll(pendingDelete);
		return pendingDelete.size();
	}
	
	//Base delete method 3: with matching servlet AND pattern
	public int deleteServletMapping(String pattern, Class<? extends HttpServlet> servlet){
		HashSet<Entry> pendingDelete = new HashSet<>();
		for(Entry e:repo){
			if(e.urlPattern.pattern().equals(pattern) && e.serlvet.getName().equals(servlet.getName())){
				pendingDelete.add(e);
			}
		}
		repo.removeAll(pendingDelete);
		return pendingDelete.size();
	}
			
	//Extended delete method
	public int deleteServletMapping(String pattern, String servlet) throws ClassNotFoundException{
		Class<? extends HttpServlet> servletClass = servlet == null ? null : (Class<? extends HttpServlet>) Class.forName(servlet);
		if(pattern == null && servletClass != null){
			return deleteServletMapping(servletClass);
		}else if(pattern != null && servletClass != null){
			return deleteServletMapping(pattern, servletClass);
		}else if(pattern != null && servletClass == null){
			return deleteServletMapping(pattern);
		}
		return 0;
	}
	
	public Class<? extends HttpServlet> getMappedServlet(String url){
		for(Entry e:repo){
			if(e.urlPattern.matcher(url).matches()){
				return e.serlvet;
			}
		}
		return null;
	}
}
