package org.leolo.miniwebserver.http;

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHeaders {
	
	Logger logger = LoggerFactory.getLogger(HttpHeaders.class);
	
	private List<Cookie> cookies;
	
	private List<HttpHeader> headers;
	
	{
		cookies = new Vector<>();
		headers = new Vector<>();
	}
	
	public void addHeader(String line) throws MalformedHeaderException{
		int breakpoint =  line.indexOf(":");
		if(breakpoint <= 0){
			throw new MalformedHeaderException("Bad header");
		}
		String name = line.substring(0, breakpoint).trim();
		String value = line.substring(breakpoint+1).trim();
		logger.debug("name={};value={};",name,value);
		if("Cookie".equals(name)){
			StringTokenizer st = new StringTokenizer(value,";");
			while(st.hasMoreTokens()){
				String cookie = st.nextToken();
				breakpoint = cookie.indexOf("=");
				if(breakpoint<=0)
					continue;
				String cName = cookie.substring(0, breakpoint);
				String cValue = cookie.substring(breakpoint+1);
				cookies.add(new Cookie(cName, cValue));
			}
		}else{
			headers.add(new HttpHeader(name, value));
		}
	}
}
