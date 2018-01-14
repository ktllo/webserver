package org.leolo.miniwebserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionRepository {
	
	private Random random;
	
	public static final int MAX_CACHE_SIZE = 100;
	
	private static SessionRepository instance = null;
	
	/**
	 * the length of the session identifier in bytes
	 */
	public static final int IDENTIFIER_LENGTH = 15;
	
	private Logger logger = LoggerFactory.getLogger(SessionRepository.class);
	private transient  SecretKey secretKey;
	private final byte [] IV;
	private SessionRepository(){
		random = new Random();
		cache = new HashMap<>();
		KeyGenerator keyGen = null;
		try {
			keyGen = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(),e);
		}
		keyGen.init(128,new SecureRandom( ) );
		secretKey = keyGen.generateKey();
		IV = new byte[128 / 8]; 
		SecureRandom prng = new SecureRandom();
		prng.nextBytes(IV);
	}
	
	public static SessionRepository getInstance(){
		if(instance == null){
			instance = new SessionRepository();
		}
		return instance;
	}
	
	public synchronized String getNextSessionId(){
		byte [] data = new byte [IDENTIFIER_LENGTH];
		random.nextBytes(data);
		org.apache.commons.codec.binary.Base32 b32 = new org.apache.commons.codec.binary.Base32();
		return b32.encodeAsString(data);
	}
	
	HashMap<String, Session> cache;
	
	public void clearAllSession(){
		synchronized(this){
			//Step 1: Clear the cache
			cache.clear();
			//Step 2: Clear the long term storage
			File root = new File("tmp");
			for(File f:root.listFiles()){
				f.delete();
			}
		}
	}
	
	public void save(Session s){
		cache.put(s.getId(), s);
		if(cache.size()>MAX_CACHE_SIZE){
			_write(cache.size() - MAX_CACHE_SIZE);
		}
	}
	
	//See which sessions should be written to disk
	private synchronized void _write(int count){
		List<Session> list = getCacheList();
		Collections.sort(list, new Comparator<Session>(){
			@Override
			public int compare(Session o1, Session o2) {
				if(o1.lastAccessedTime < o2.lastAccessedTime) return -1;
				if(o1.lastAccessedTime > o2.lastAccessedTime) return 1;
				return 0;
			}
		});
		ArrayList<Session> pending = new ArrayList<>();
		for(int i=0;i<count;i++){
			pending.add(list.get(i));
			if(persists(list.get(i))){
				cache.remove(list.get(i).getId());
			}
		}
	}
	public boolean persists(Session s)  {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(s);
			Cipher cipher = Cipher.getInstance("AES"); //: Same as ES/ECB/PKCS5Padding
			cipher.init(Cipher.ENCRYPT_MODE, secretKey); 
			byte[] cipherText = cipher.doFinal(baos.toByteArray());
			FileOutputStream fos = new FileOutputStream("tmp/"+s.getId());
			fos.write(cipherText);
			fos.close();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			return false;
		} catch (GeneralSecurityException e) {
			logger.error(e.getMessage(),e);
			return false;
		}finally{
			if(oos!=null){
				try {
					oos.close();
					baos.close();
				} catch (IOException e) {
					logger.error(e.getMessage(),e);
				}
			}
		}
		
		return true;
	}
	
	public Session readFromDisk(String sessionId){
		//Stage 1: See is target file exists
		File f = new File("tmp/"+sessionId);
		if(!f.exists()){
			return null;
		}
		Session s = null;
		try{
			if(f.length() > 1048576)
				return null;
			FileInputStream fis = new FileInputStream(f);
			byte[] data = new byte[(int) f.length()];
			fis.read(data);
			fis.close();
			Cipher cipher = Cipher.getInstance("AES"); 
			cipher.init(Cipher.DECRYPT_MODE, secretKey);  
			byte[] decryptedData = cipher.doFinal(data);
			ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object obj = ois.readObject();
			if(obj instanceof Session){
				s = (Session) obj;
			}else{
				logger.error("Unexpected class {}",obj.getClass().getName());
			}
		}catch (IOException e) {
			logger.error(e.getMessage(),e);
		} catch (GeneralSecurityException e) {
			logger.error(e.getMessage(),e);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(),e);
		} 
		return s;
	}
	
	private List<Session> getCacheList(){
		ArrayList<Session> list = new ArrayList<>(cache.size());
		for(Session s:cache.values()){
			list.add(s);
		}
		return list;
	}
	
	
}
