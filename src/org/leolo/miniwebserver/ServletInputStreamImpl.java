package org.leolo.miniwebserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javax.servlet.ReadListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletInputStreamImpl extends javax.servlet.ServletInputStream {
	
	private InputStream stream;
	private static Logger logger = LoggerFactory.getLogger(ServletInputStreamImpl.class);
	
	ServletInputStreamImpl(Socket socket){
		try {
			stream = socket.getInputStream();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}
	
	@Override
	public boolean isFinished() {
		try {
			return stream.available()>0;
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			return false;
		}
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setReadListener(ReadListener readListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public int read() throws IOException {
		return stream.read();
	}

}
