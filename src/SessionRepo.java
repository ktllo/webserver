import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;
import org.leolo.miniwebserver.Session;
import org.leolo.miniwebserver.SessionRepository;

public class SessionRepo {

	@Test
	public void test() {
		SessionRepository.getInstance().clearAllSession();
		HashMap<String, String> map = new HashMap<>();
		for(int i=0;i<1000;i++){
			Session s = new Session();
			map.put(s.getId(), s.toString());
			if(!SessionRepository.getInstance().persists(s)){
				fail("Unable to persists");
			}
		}
		for(String key:map.keySet()){
			Session s = SessionRepository.getInstance().readFromDisk(key);
			if(s==null){
				fail("Unable to restore");
			}
			if(!s.toString().equals(map.get(key))){
				fail("Data changed,"+s.toString()+"/"+map.get(key));
			}
		}
	}

}
