package org.leolo.miniwebserver;


import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletRepository {
	static Logger logger = LoggerFactory.getLogger(ServletRepository.class);
	public static final int DEFAULT_PROCESS_ORDER = 0xffff;
	
	private TreeSet<Entry> repo;
	
	public ServletRepository(){
		repo = new TreeSet<>();
	}
	
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
		pattern = globToRegEx(pattern==null?"":pattern);
		Entry e = new Entry();
		e.serlvet = servletClass;
		e.urlPattern = Pattern.compile(pattern);
		e.processOrder = processOrder;
		repo.add(e);
		logger.info("Registered {} mapping to {}, current size is {}", pattern, servletClass.getName(), repo.size());
	}
	
	public void addServletMapping(String pattern, String servletClass, int processOrder)throws ClassNotFoundException{
		addServletMapping(pattern, (Class<? extends HttpServlet>) Class.forName(servletClass), processOrder);
	}
	
	public void addServletMapping(String pattern, String servletClass) throws ClassNotFoundException{
		addServletMapping(pattern, servletClass, DEFAULT_PROCESS_ORDER);
	}
	
	public void addServletMapping(String pattern, Class<? extends HttpServlet> servletClass){
		addServletMapping(pattern, servletClass, DEFAULT_PROCESS_ORDER);
	}
	
	//Base delete method 1: with matching pattern
	public int deleteServletMapping(String pattern){
		pattern = globToRegEx(pattern==null?"":pattern);
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
		pattern = globToRegEx(pattern==null?"":pattern);
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
		int removed = repo.size();
		repo.clear();
		return removed;
	}
	
	public Class<? extends HttpServlet> getMappedServlet(String url){
		for(Entry e:repo){
			logger.info("Checking pattern {}", e.urlPattern.pattern());
			if(e.urlPattern.matcher(url).matches()){
				return e.serlvet;
			}
		}
		return null;
	}
	
	private String globToRegEx(String line){
		line = line.trim();
	    int strLen = line.length();
	    StringBuilder sb = new StringBuilder(strLen);
	    boolean escaping = false;
	    int inCurlies = 0;
	    for (char currentChar : line.toCharArray()){
	        switch (currentChar){
	        case '*':
	            if (escaping)
	                sb.append("\\*");
	            else
	                sb.append(".*");
	            escaping = false;
	            break;
	        case '?':
	            if (escaping)
	                sb.append("\\?");
	            else
	                sb.append('.');
	            escaping = false;
	            break;
	        case '.':
	        case '(':
	        case ')':
	        case '+':
	        case '|':
	        case '^':
	        case '$':
	        case '@':
	        case '%':
	            sb.append('\\');
	            sb.append(currentChar);
	            escaping = false;
	            break;
	        case '\\':
	            if (escaping){
	                sb.append("\\\\");
	                escaping = false;
	            }
	            else
	                escaping = true;
	            break;
	        case '{':
	            if (escaping){
	                sb.append("\\{");
	            }
	            else{
	                sb.append('(');
	                inCurlies++;
	            }
	            escaping = false;
	            break;
	        case '}':
	            if (inCurlies > 0 && !escaping){
	                sb.append(')');
	                inCurlies--;
	            }
	            else if (escaping)
	                sb.append("\\}");
	            else
	                sb.append("}");
	            escaping = false;
	            break;
	        case ',':
	            if (inCurlies > 0 && !escaping){
	                sb.append('|');
	            }
	            else if (escaping)
	                sb.append("\\,");
	            else
	                sb.append(",");
	            break;
	        default:
	            escaping = false;
	            sb.append(currentChar);
	        }
	    }
	    return sb.toString();
	}
}

