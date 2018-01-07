package org.leolo.miniwebserver;

import javax.servlet.http.HttpSession;

public abstract class SessionStorage {
	protected long createdTime;
	protected long accessedTime;
	protected String sessionId;
	
	public final HttpSession getSession(){
		accessedTime = System.currentTimeMillis();
		return _getSession();
	}
	
	protected abstract HttpSession _getSession();
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public long getAccessedTime() {
		return accessedTime;
	}
}
