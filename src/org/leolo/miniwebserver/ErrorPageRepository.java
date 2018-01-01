package org.leolo.miniwebserver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorPageRepository {
	private static final Logger logger = LoggerFactory.getLogger(ErrorPageRepository.class);
	
	private static ErrorPageRepository instance;
	
	private static Hashtable<Object,Object> replaceSet = null;
	private static Hashtable<Integer, Entry> repo = null;
	
	@Deprecated
	public synchronized static ErrorPageRepository getInstance(){
		if(instance == null)
			instance = new ErrorPageRepository();
		return instance;
	}
	
	static{
		repo = new Hashtable<>();
	}
	
	public static void loadErrorPage(int errorCode,String name, ErrorPageType type){
		StringBuilder data = new StringBuilder();
		//Step 1: Read the target file
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("error/"+name)));
			while(true){
				String line = br.readLine();
				if(line == null){
					break;
				}
				data.append(line).append("\r\n");//Always use Windows new line
			}
			br.close();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new RuntimeException(e.getMessage(),e);
		}
		logger.info("error/{} is loaded",name);
		String sData = data.toString();
		//Step 2: Replace the variables
		if(replaceSet !=null){
			for(Object key:replaceSet.keySet()){
				String var = "${"+key+"}";
				if(replaceSet.get(key)==null){
					logger.warn("{} has null empty", key);
					continue;
				}
				logger.debug("{} -> {}",var,replaceSet.get(key).toString());
				sData = sData.replace(var, replaceSet.get(key).toString());
			}
			logger.info("Finished replacing variable, old size {}, new size {}", data.length(), sData.length());
		}else{
			logger.warn("No variable replacement set!");
		}
		//Step 3: Register the page
		repo.put(new Integer(errorCode), new Entry(type, sData));
	}
	
	public static Hashtable<Object,Object> getReplaceSet() {
		return replaceSet;
	}

	public static void setReplaceSet(Hashtable<Object,Object> replaceSet) {
		ErrorPageRepository.replaceSet = replaceSet;
	}
	
	private static class Entry{
		public Entry(ErrorPageType type, CharSequence data) {
			super();
			this.type = type;
			this.data = data;
		}
		ErrorPageType type;
		CharSequence data;
	}
	
	public enum ErrorPageType{
		STATIC,
		DYNAMIC;
	}
	
	public static String getErrorPage(int errorCode){
		Entry e = repo.get(new Integer(errorCode));
		if(e == null){
			return getDefaultErrorPage(errorCode, null);
		}
		return e.data.toString();
	}
	
	public static String getErrorPage(int errorCode, Map<Object, Object> rSet){
		Entry e = repo.get(new Integer(errorCode));
		if(e == null){
			return getDefaultErrorPage(errorCode, rSet);
		}
		if(e.type == ErrorPageType.STATIC){
			return e.data.toString();
		}
		String sData = e.data.toString();
		//Step 2: Replace the variables
		if(rSet !=null){
			for(Object key:rSet.keySet()){
				String var = "${"+key+"}";
				if(rSet.get(key)==null){
					logger.warn("{} has null empty", key);
					continue;
				}
				logger.debug("{} -> {}",var,rSet.get(key).toString());
				sData = sData.replace(var, rSet.get(key).toString());
			}
			logger.info("Finished replacing variable, old size {}, new size {}", e.data.length(), sData.length());
		}else{
			logger.warn("No variable replacement set!");
		}
		//Step 3: Register the page
		return sData;
	}
	
	private static String getDefaultErrorPage(int errorCode, Map<Object, Object> rSet){
		Entry e = repo.get(new Integer(-1));
		if(e == null){
			throw new RuntimeException("No such error page");
		}
		if(e.type == ErrorPageType.STATIC){
			return e.data.toString();
		}
		String sData = e.data.toString();
		//Step 2: Replace the variables
		if(rSet == null){
			rSet = new HashMap<>();
		}
		rSet.put("_ECode", Integer.toString(errorCode));
		rSet.put("_EMsg", ServerUtils.getResponseCodeDescription(errorCode));
		for(Object key:rSet.keySet()){
			String var = "${"+key+"}";
			if(rSet.get(key)==null){
				logger.warn("{} has null empty", key);
				continue;
			}
			logger.debug("{} -> {}",var,rSet.get(key).toString());
			sData = sData.replace(var, rSet.get(key).toString());
		}
		logger.info("Finished replacing variable, old size {}, new size {}", e.data.length(), sData.length());
	
		//Step 3: Register the page
		return sData;
	}
}
