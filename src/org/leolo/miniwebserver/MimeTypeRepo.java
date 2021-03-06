package org.leolo.miniwebserver;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MimeTypeRepo {
	
	Logger logger = LoggerFactory.getLogger(MimeTypeRepo.class);
	
	private static MimeTypeRepo instance = null;
	
	private Map<String, Entry> extMap = null;
	
	private Map<String, Entry> mimeMap = null;
	
	
	private MimeTypeRepo(){
		extMap = new TreeMap<>();
		mimeMap = new HashMap<>();
		init();
	}
	
	@SuppressWarnings("rawtypes")
	private void init(){
		// Step 1: Read the target file
		
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader("res/mime.json"));
			while(true){
				String line = br.readLine();
				if(line == null){
					break;
				}
				sb.append(line.trim());
			}
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		//Step 2: Parse the file
		JSONObject obj = new JSONObject(sb.toString());
		Map<String, Object> jsonMap = obj.toMap();
		for(String key:jsonMap.keySet()){
			Entry entry = new Entry(key);
			HashMap subMap = (HashMap)jsonMap.get(key);
			for(Object subKey:subMap.keySet()){
				if("source".equals(subKey)){
					entry.source = subMap.get(subKey).toString();
				}else if("compressible".equals(subKey)){
					entry.compresable = ((Boolean)subMap.get(subKey)).booleanValue();
				}else if("extensions".equals(subKey)){
					entry.addExtension((List)subMap.get(subKey));
				}
			}
			mimeMap.put(key, entry);
		}
		//Step 3: Create mapping for extension
		for(String key:mimeMap.keySet()){
			Entry e = mimeMap.get(key);
			if(e.extension != null){
				for(String ext:e.extension){
					if(!extMap.containsKey(ext.toLowerCase())){
						extMap.put(ext.toLowerCase(), e);
					}else{
						logger.warn("Dupulicated extension {}, for type {} and {}", ext, key, extMap.get(ext).typeName);
					}
				}
			}
		}
		//Step 4: Handle (some) major conflicts
		remap("wav","audio/wav");
		remap("bmp","image/bmp");
		remap("exe","application/octet-stream");
		remap("dll","application/octet-stream");
		remap("deb","application/octet-stream");
		remap("dmg","application/octet-stream");
		remap("iso","application/octet-stream");
		remap("msi","application/x-msdownload");
		remap("rtf","text/rtf");
		remap("3gpp","video/3gpp");
		remap("mp3","audio/mp3");
		remap("xml","text/xml");
		
	}
	
	public void remap(String ext, String mime){
		if(mimeMap.containsKey(mime)){
			mimeMap.get(mime).addExtension(ext);
			extMap.put(ext, mimeMap.get(mime));
		}else{
			Entry entry = new Entry(mime);
			entry.addExtension(ext);
			extMap.put(ext, entry);
			mimeMap.put(mime, entry);
		}
	}
	
	public static synchronized MimeTypeRepo getInstance(){
		if(instance == null){
			instance = new MimeTypeRepo();
		}
		return instance;
	}
	
	public static String getMimeTypeByExtension(String ext){
		Entry entry = instance.extMap.get(ext.toLowerCase());
		return entry == null ? "application/octet-stream" : entry.typeName;
	}
	
	static class Entry{
		String source;
		boolean compresable = false;
		Set<String> extension;
		String typeName;
		
		Entry(String typeName){
			this.typeName = typeName;
		}
		
		void addExtension(String extension){
			if(this.extension==null){
				this.extension = new java.util.HashSet<>();
			}
			this.extension.add(extension.toLowerCase());
		}
		
		@SuppressWarnings("rawtypes")
		void addExtension(List extension){
			for(Object o:extension){
				addExtension(o.toString());
			}
		}
	}
}
