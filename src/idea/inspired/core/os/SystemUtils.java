package idea.inspired.core.os;

import idea.inspired.core.util.Log;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;


/**
 * The class is used to provide extra functions for {@link System}. 
 * 
 * @author tsungjung411@yahoo.com.tw
 * @since 2013/02/16
 * @see {@link System}
 */
public class SystemUtils {
	
	private static final String TAG = SystemUtils.class.getSimpleName();
	
	/** 
	 * Defines the property name of line-separator on Windows/Linux 
	 * platform
	 * @see {@link System#getProperties()}
	 */
	private static final String LINE_SEPARATOR_PROPERTY
			= "line.separator";
	
	/** 
	 * Defines the property name of working-directory.
	 * @see {@link System#getProperties()}
	 */
	private static final String WORKING_DIRECTORY_PROPERTY = "user.dir";
	
	/**
	 * DO NOT directly access the value. Uses the {@link 
	 * #getLineSeparator()} instead.
	 * 
	 * @see {@link #getLineSeparator()}
	 */
	private static String sLineSeparator;
	
	/**
	 * DO NOT directly access the value. Uses the {@link 
	 * #getWorkingDirectory()} instead.
	 * 
	 * @see {@link #getLineSeparator()}
	 */
	private static String sWorkingDirectory;
	
	/**
	 * <P>Gets the {@link #LINE_SEPARATOR_PROPERTY} value from the 
	 * system properties.</P>
	 * 
	 * <P>In general,
	 * <UL>
	 *    <LI>Windows: \r\n</LI>
	 *    <LI>Linux: \n</LI>
	 * </UL>
	 * </P>
	 * @return
	 */
	public static String getLineSeparator() {
		String value = sLineSeparator;
		
		if (value == null) {
			value = System.getProperty(LINE_SEPARATOR_PROPERTY);
			
			if (value == null) {
				value = "\n"; // assign the default line-separator
			}
			sLineSeparator = value;
		}
		return sLineSeparator;
	}
	
	/**
	 * Gets the {@link #WORKING_DIRECTORY_PROPERTY} value from the system properties.
	 * @return
	 */
	public static String getWorkingDirectory() {
		String value = sWorkingDirectory;
		
		if (value == null) {
			value = System.getProperty(WORKING_DIRECTORY_PROPERTY);
			
			if (value == null) {
				try {
					value = new File(".").getCanonicalPath();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			sWorkingDirectory = value;
		}
		return sWorkingDirectory;
	}
	
	/**
	 * Gets the non-null sorted system properties.
	 * 
	 * @return
	 *    Returns the non-null sorted system properties
	 * @see 
	 *    {@link System#getProperties()}
	 */
	public static TreeMap<String, String> getSortedProperties() {
		Properties prop = System.getProperties();
		Iterator<Entry<Object, Object>> itr = prop.entrySet().iterator();
		Entry<Object, Object> entry;
		String key, value;
		
		// returns the tree map
		TreeMap<String, String> sortedProp = new TreeMap<String, String>();
		
		// puts all key-value pairs into the sorted collection 
		while (itr.hasNext()) {
			entry = itr.next();
			key = entry.getKey().toString();
			value = entry.getValue() != null ? entry.getValue().toString() : null;
			sortedProp.put(key, value);
		}
		
		// adds a new property, called "line.separator.readable", which 
		// represents the white spaces of "line.separator" with human-readable 
		// code
		value = sortedProp.get("line.separator");
		if (value != null) {
			String newValue = "";
			
			// char to code
			for (int i = 0; i < value.length(); i++) {
				switch (value.charAt(i)) {
				case '\r':
					newValue += "\\r";
					break;
				case '\n':
					newValue += "\\n";
					break;
				default:
					throw new RuntimeException("new character: " + 
							(int) value.charAt(i));
				}
			}
			sortedProp.put("line.separator.readable", newValue);
		}
		return sortedProp;
	}
	
	/**
	 * Dumps the sorted system properties.
	 */
	public static void dumpSortedProperties() {
		TreeMap<String, String> prop = getSortedProperties();
		Iterator<Entry<String, String>> itr = prop.entrySet().iterator();
		StringBuilder builder = new StringBuilder();
		String lineSeparator = getLineSeparator();

		while (itr.hasNext()) {
			builder.append(itr.next()).append(lineSeparator);
		}
		Log.v(TAG, builder.toString());
	}
	
	private static final String[] BROWSER_LIST = {
			"google-chrome", "firefox", "opera", "epiphany", "konqueror", 
			"conkeror", "midori", "kazehakase", "mozilla", "chromium-browser"};
	
	public static void openUrl(String url) {
		openUrl(URI.create(url));
	}
	
	/**
	 * Launches the default browser to display a URI.
	 * @param uri
	 */
	public static void openUrl(URI uri) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
				return;
			} catch (IOException e) {
				// If the current platform does not support the 
				// Desktop.Action.BROWSE action 
				e.printStackTrace();
			}
		}
		
		try {
			String osName = System.getProperty("os.name");
			
			if (osName.startsWith("Windows")) {
				Runtime.getRuntime().exec(
						"rundll32 url.dll, FileProtocolHandler " + uri);
				
			} else if (osName.startsWith("Mac OS")) {
				Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
						"openURL", new Class[] {String.class}).invoke(null,
								new Object[] {uri});
				
			} else { 
				// need to test
				// assume Unix or Linux
				String browser = null;
				for (String b : BROWSER_LIST) {
					System.err.println("b: " + b);
					if (browser == null && Runtime.getRuntime().exec(
							new String[]{"which", b}).getInputStream().read() != -1) {
						Runtime.getRuntime().exec(new String[] {browser = b, uri.toString()});
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
