package org.leolo.miniwebserver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorPageRepository {
	Logger logger = LoggerFactory.getLogger(ErrorPageRepository.class);
	
	private static ErrorPageRepository instance;
	
	private Hashtable<Object,Object> replaceSet = null;
	private Hashtable<Integer, Entry> repo = null;
	
	public synchronized static ErrorPageRepository getInstance(){
		if(instance == null)
			instance = new ErrorPageRepository();
		return instance;
	}
	
	private ErrorPageRepository(){
		repo = new Hashtable<>();
		
	}
	
	public void loadErrorPage(int errorCode,String name, ErrorPageType type){
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
				sData = sData.replace(var, replaceSet.get(key).toString());
			}
			logger.info("Finished replacing variable, old size {}, new size {}", data.length(), sData.length());
		}else{
			logger.warn("No variable replacement set!");
		}
		//Step 3: Register the page
		repo.put(new Integer(errorCode), new Entry(type, sData));
	}
	
	public Hashtable<Object,Object> getReplaceSet() {
		return replaceSet;
	}

	public void setReplaceSet(Hashtable<Object,Object> replaceSet) {
		this.replaceSet = replaceSet;
	}
	
	private class Entry{
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
		DYMANIC;
	}
	
	public String getErrorPage(int errorCode){
		Entry e = repo.get(new Integer(errorCode));
		if(e == null){
			throw new RuntimeException("No such error page");
		}
		return e.data.toString();
	}
	
	public String getErrorPage(int errorCode, Map<Object, Object> rSet){
		Entry e = repo.get(new Integer(errorCode));
		if(e == null){
			throw new RuntimeException("No such error page");
		}
		if(e.type == ErrorPageType.STATIC){
			return e.data.toString();
		}
		String sData = e.data.toString();
		//Step 2: Replace the variables
		if(replaceSet !=null){
			for(Object key:replaceSet.keySet()){
				String var = "${"+key+"}";
				sData = sData.replace(var, replaceSet.get(key).toString());
			}
			logger.info("Finished replacing variable, old size {}, new size {}", e.data.length(), sData.length());
		}else{
			logger.warn("No variable replacement set!");
		}
		//Step 3: Register the page
		return sData;
	}
	
	
}
