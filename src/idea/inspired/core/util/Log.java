package idea.inspired.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The class is used to pipe messages into some output streams.
 * 
 * @author tsungjung411@yahoo.com.tw
 * @since 2013/02/16
 * @see {@link System#out}
 * @see {@link System#err}
 */
public class Log {
	
	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
	
	/**
	 * Output a verbose log message.
	 * @param message
	 */
	public static void v(String message) {
		// Outputs the message to the standard output steam (normally 
		// mapped to the console screen).
		System.out.println(message);
	}
	
	/**
	 * Output a verbose log message with the specific tag.
	 * @param tag
	 * @param message
	 */
	public static void v(String tag, String message) {
		StringBuilder builder = new StringBuilder();
		
		if (message != null) {
			String [] lines = message.split("\r|\n");
			
			// presents in multi-line
			for (int i = 0; i < lines.length; i++) {
				builder.append(tag).append(": ").append(lines[i]);
			}
		}
		
		// Outputs the message to the standard output steam (normally 
		// mapped to the console screen).
		System.out.println(builder.toString());
	}
	
	/**
	 * Output a verbose date/time in standard format with the specific
	 * tag.
	 * @param tag
	 * @param date
	 */
	public static void v(String tag, Date date) {
		SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		// Outputs the message to the standard output steam (normally 
		// mapped to the console screen).
		System.out.println(tag + ": " + f.format(date));
	}
	
	/**
	 * Output an error log message.
	 * @param message
	 */
	public static void e(String message) {
		// Outputs the message to the error output steam.
		System.err.println(message);
	}
	
	/**
	 * Output an error log message with the specific tag.
	 * @param tag
	 * @param message
	 */
	public static void e(String tag, String message) {
		// Outputs the message to the error output steam.
		System.err.println(tag + ": " + message);
	}
}
