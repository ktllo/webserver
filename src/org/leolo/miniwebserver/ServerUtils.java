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
	
	static String getResponseCodeDescription(int responseCode){
		switch(responseCode){
		case 100  : return "Continue";
		case 101  : return "Switching Protocols";
		case 102  : return "Processing";
		case 103  : return "Early Hints";
		case 200  : return "OK";
		case 201  : return "Created";
		case 202  : return "Accepted";
		case 203  : return "Non-Authoritative Information";
		case 204  : return "No Content";
		case 205  : return "Reset Content";
		case 206  : return "Partial Content";
		case 207  : return "Multi-Status";
		case 208  : return "Already Reported";
		case 226  : return "IM Used";
		case 300  : return "Multiple Choices";
		case 301  : return "Moved Permanently";
		case 302  : return "Found";
		case 303  : return "See Other";
		case 304  : return "Not Modified";
		case 305  : return "Use Proxy";
		case 307  : return "Temporary Redirect";
		case 308  : return "Permanent Redirect";
		case 400  : return "Bad Request";
		case 401  : return "Unauthorized";
		case 402  : return "Payment Required";
		case 403  : return "Forbidden";
		case 404  : return "Not Found";
		case 405  : return "Method Not Allowed";
		case 406  : return "Not Acceptable";
		case 407  : return "Proxy Authentication Required";
		case 408  : return "Request Timeout";
		case 409  : return "Conflict";
		case 410  : return "Gone";
		case 411  : return "Length Required";
		case 412  : return "Precondition Failed";
		case 413  : return "Payload Too Large";
		case 414  : return "URI Too Long";
		case 415  : return "Unsupported Media Type";
		case 416  : return "Range Not Satisfiable";
		case 417  : return "Expectation Failed";
		case 421  : return "Misdirected Request";
		case 422  : return "Unprocessable Entity";
		case 423  : return "Locked";
		case 424  : return "Failed Dependency";
		case 425  : return "Unassigned";
		case 426  : return "Upgrade Required";
		case 427  : return "Unassigned";
		case 428  : return "Precondition Required";
		case 429  : return "Too Many Requests";
		case 430  : return "Unassigned";
		case 431  : return "Request Header Fields Too Large";
		case 451  : return "Unavailable For Legal Reasons";
		case 500  : return "Internal Server Error";
		case 501  : return "Not Implemented";
		case 502  : return "Bad Gateway";
		case 503  : return "Service Unavailable";
		case 504  : return "Gateway Timeout";
		case 505  : return "HTTP Version Not Supported";
		case 506  : return "Variant Also Negotiates";
		case 507  : return "Insufficient Storage";
		case 508  : return "Loop Detected";
		case 509  : return "Unassigned";
		case 510  : return "Not Extended";
		case 511  : return "Network Authentication Required";
		}
		return "";
	}
}
