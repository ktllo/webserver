package org.leolo.miniwebserver;

public class ServerUtils {
	static String getStackTrace(Throwable t){
		final String NEW_LINE = "<br>\r\n";
		StringBuilder sb = new StringBuilder();
		sb.append(t.getClass().getName());
		if(t.getMessage()!=null){
			sb.append(" :").append(t.getMessage());
		}
		sb.append(NEW_LINE);
		for(StackTraceElement ste:t.getStackTrace()){
			sb.append(" at ").append(ste.getClassName()).append(".")
			.append(ste.getMethodName());
			if(ste.isNativeMethod()){
				sb.append(" (Native Method)").append(NEW_LINE);
			}else{
				sb.append(" (").append(ste.getFileName()).append(":")
				.append(ste.getLineNumber()).append(")").append(NEW_LINE);
			}
		}
		if(t.getCause()!=null){
			sb.append(" is caused by ").append(getStackTrace(t.getCause()));
		}
		return sb.toString();
	}
}
