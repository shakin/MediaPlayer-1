package org.rpi.config;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;

public class Config {
	
	
	public static String friendly_name = "Default Room";
	public static List<String> playlists = new ArrayList<String>();
	public static String debug = "None";
	public static String mplayer_path;
	public static boolean save_local_playlist = false;
	public static String version = "0.0.0.1";
	public static String logfile = "mediaplayer.log";
	public static int port = -99;
	public static int mplayer_cache = 500;
	public static int mplayer_cache_min = 70;
	public static String loglevel;
	public static String logconsole;
	
	
	public static String getProtocolInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("http-get:*:audio/x-flac:*,");
		sb.append("http-get:*:audio/wav:*,");
		sb.append("http-get:*:audio/wave:*,");
		sb.append("http-get:*:audio/x-wav:*,");
		sb.append("http-get:*:audio/mpeg:*,");
		sb.append("http-get:*:audio/x-mpeg:*,");
		sb.append("http-get:*:audio/mp1:*,");
		sb.append("http-get:*:audio/aiff:*,");
		sb.append("http-get:*:audio/x-aiff:*,");
		sb.append("http-get:*:audio/x-m4a:*,");
		sb.append("http-get:*:audio/x-ms-wma:*,");
		sb.append("rtsp-rtp-udp:*:audio/x-ms-wma:*,");
		sb.append("http-get:*:audio/x-scpls:*,");
		sb.append("http-get:*:audio/x-mpegurl:*,");
		sb.append("http-get:*:audio/x-ms-asf:*,");
		sb.append("http-get:*:audio/x-ms-wax:*,");
		sb.append("http-get:*:audio/x-ms-wvx:*,");
		sb.append("http-get:*:text/xml:*,");
		sb.append("http-get:*:audio/aac:*,");
		sb.append("http-get:*:audio/aacp:*,");
		sb.append("http-get:*:audio/mp4:*,");
		sb.append("http-get:*:audio/ogg:*,");
		sb.append("http-get:*:audio/x-ogg:*,");
		sb.append("http-get:*:application/ogg:*,");
		sb.append("http-get:*:video/mpeg:*,");
		sb.append("http-get:*:video/mp4:*,");
		sb.append("http-get:*:video/quicktime:*,");
		sb.append("http-get:*:video/webm:*,");
		sb.append("http-get:*:video/x-ms-wmv:*,");
		sb.append("http-get:*:video/x-ms-asf:*,");
		sb.append("http-get:*:video/x-msvideo:*,");
		sb.append("http-get:*:video/x-ms-wax:*,");
		sb.append("http-get:*:video/x-ms-wvx:*,");
		sb.append("http-get:*:video/x-m4v:*,");
		sb.append("http-get:*:video/x-matroska:*,");
		sb.append("http-get:*:application/octet-stream:*");
		return sb.toString();
	}
	
	public static Level getLogFileLevel()
	{
		return getLogLevel(loglevel);
	}
	
	public static Level getLogConsoleLevel()
	{
		return getLogLevel(logconsole);
	}
	
	private static Level getLogLevel(String s)
	{
		if(s==null)
			return Level.DEBUG;
		
		if(s.equalsIgnoreCase("DEBUG"))
		{
			return Level.DEBUG;
		}
		
		else if (s.equalsIgnoreCase("ALL"))
		{
			return Level.ALL;
		}
		else if (s.equalsIgnoreCase("ERROR"))
		{
			return Level.ERROR;
		}
		
		else if (s.equalsIgnoreCase("FATAL"))
		{
			return Level.FATAL;
		}
		
		else if (s.equalsIgnoreCase("INFO"))
		{
			return Level.INFO;
		}
		
		else if (s.equalsIgnoreCase("OFF"))
		{
			return Level.OFF;
		}
		
		else if (s.equalsIgnoreCase("TRACE"))
		{
			return Level.TRACE;
		}
		
		else if (s.equalsIgnoreCase("WARN"))
		{
			return Level.WARN;
		}
		return Level.DEBUG;
	}


	public static void setSaveLocalPlayList(String property) {
		if(property.equalsIgnoreCase("TRUE"))
			save_local_playlist = true;
	}
	
	public static int converStringToInt(String s)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch(Exception e)
		{
			
		}
		return -99;
	}
}