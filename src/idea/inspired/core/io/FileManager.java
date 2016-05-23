package idea.inspired.core.io;

import idea.inspired.core.os.SystemUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;


/**
 * The class is used to provide some IO operations.
 * 
 * @author tsungjung411@yahoo.com.tw
 * @since 2012.12.30
 * @version 1.0
 */
public class FileManager {
	
	/** 
	 * The character set is used to represent hexadecimal (base 16) 
	 */
	private static final char [] HEXI_DECIMAL
			= "0123456789ABCDEF".toCharArray();
	
	/**
	 * Dumps the header part with the specified length.
	 * 
	 * @param content - the string to dump
	 * @param length - the length of the header part, 
	 *    i.e. [start=0, end=length)
	 * @param stream - could be {@link System#out}, or {@link System#err}
	 */
	public static void dumpHead(String content, int length, 
			PrintStream stream) {
		if (content == null) {
			stream.append("dumpHead: content is null");
			return;
		} else if (length < 0) {
			throw new IllegalArgumentException(
					"The length should be a natural number");
		}
		
		if (content.length() < length) {
			length = content.length();
		}
		
		char ch;
		char b11, b12, b21, b22;
		StringBuilder buffer;
		
		for (int i = 0; i < length; i++) {
			ch = content.charAt(i);
			b11 = HEXI_DECIMAL[(ch & 0xF000) >> 12];
			b12 = HEXI_DECIMAL[(ch & 0x0F00) >> 8];
			b21 = HEXI_DECIMAL[(ch & 0x00F0) >> 4];
			b22 = HEXI_DECIMAL[ch & 0x000F];
			
			buffer = new StringBuilder(20);
			buffer
			.append("[").append(i).append("] ").append(ch)
			.append(" (").append((int) ch)
			.append(" = 0x").append(b11).append(b12).append(b21).append(b22)
			.append(")");
			
			stream.println(buffer.toString());
		}
		
		// flushes the stream
		stream.flush();
	}
	
	/**
	 * Dumps the header part with the specified length.
	 * 
	 * @param bytes - the bytes to dump
	 * @param length - the length of the header part, 
	 *    i.e. [start=0, end=length)
	 * @param stream - could be {@link System#out}, or {@link System#err}
	 */
	public static void dumpHead(byte [] bytes, int length, 
			PrintStream stream) {
		
		// checks the arguments
		if (bytes == null) {
			stream.append("dumpHead(): content is null");
			return;
		} else if (length < 0) {
			throw new IllegalArgumentException(
					"The length should be a natural number");
		}
		
		if (bytes.length < length) {
			length = bytes.length;
		}
		
		final String format = "bytes[%d] '%s' (%d = 0x%s%s)";
		int b11, b12, value;
		
		for (int i = 0; i < length; i++) {
			b11 = (bytes[i] & 0xF0) >> 4;
			b12 = bytes[i] & 0x0F;
			value = (b11 << 4) + b12;
			
			stream.println(String.format(format, 
					i, (char) bytes[i], 
					value, HEXI_DECIMAL[b11], HEXI_DECIMAL[b12]));
		}
		
		// flushes the stream
		stream.flush();
	}
	
	/**
	 * If the source is a file, it will copy the file to the target 
	 * path. If the source is a directory, it will copy the children 
	 * of the source directory to the target directory, and 
	 * recursively copy them.
	 * @param src
	 * @param dest
	 * @return
	 */
	public static boolean copy(File src, File dest) {
		
		// Checks the parameters:
		// Case 1: null pointer
		if (src == null || dest == null) {
			return false;
		}
		
		// Case 2: The source file/directory does not exist. 
		if (!src.exists()) {
			return false;
		}
		
		try {
			return _copy(src, dest);
		} catch (IOException e) {
			e.printStackTrace();

			StringBuffer error = new StringBuffer();
			
			error.append("\n[src]:\n   ");
			error.append(src);
			error.append("\n[src.getAbsolutePath()]:\n   ");
			error.append(src.getAbsolutePath());
			try {
				error.append("\n[src.getCanonicalPath())]:\n   "); 
				error.append(src.getCanonicalPath());
			} catch (IOException srcErr) {
			};
			
			error.append("\n");
			
			error.append("\n[dest]:\n   ");
			error.append(dest);
			error.append("\n[dest.getAbsolutePath()]: \n   ");
			error.append(dest.getAbsolutePath());
			try {
				error.append("\n[dest.getCanonicalPath())]: \n   ");
				error.append(dest.getCanonicalPath());
			} catch (IOException srcErr) {
			};

			error.append("\n");
			System.err.println(error);
			return false;
		}
	}
	
	private static boolean _copy(File src, File dest) 
			throws IOException {
		
		if (src.isDirectory()) {
			String [] files = src.list();
			
			// Checks whether the destination directory exists or not. 
			if (!dest.exists()) {
				
				// Creates the directory named by this abstract 
				// pathname, including any necessary but nonexistent 
				// parent directories. For example,
				//    copy from "tmp/*" to "tmp1/tmp2/tmp3/*'
				if (!dest.mkdirs()) {
					return false;
				}
			}
			
			// Recursively does it
			for (int i = 0; i < files.length; i++) {
				_copy(new File(src, files[i]),
						new File(dest, files[i]));
			}
		} else {
			//---------------------------------------------
			// copies the specific file to the target file
			//---------------------------------------------
			BufferedInputStream bis;
			BufferedOutputStream bos;
			byte [] buffer = new byte [1024];
			int length;
			
			// if the target file exists and it can be written, then
			// changes the mode to be writable
			if (dest.exists() && !dest.canWrite()) {
				if ( ! dest.setWritable(true)) { // for the owner
					
					// If the user does not have permission to change
					// the access permissions of the file, the 
					// operation will fail.
					Exception e = new IllegalAccessException(
							"Failed to set the write permission." +
							"\n - file: " + dest);
					e.printStackTrace(System.err);
					return false;
				}
			}
			
			bis = new BufferedInputStream(new FileInputStream(src));
			bos = new BufferedOutputStream(new FileOutputStream(dest));
			
			//starts to copy bits from the source to the target
			while ((length = bis.read(buffer)) != -1) {
				bos.write(buffer, 0, length);
			}
			
			//closes the input & output stream
			bis.close();
			bos.close();
		}
		return true;
	}
	
	/**
	 * Reads the first line of the content.
	 * @param content
	 * @return
	 * <UL>
	 *    <LI>the first line of the content</LI>
	 *    <LI><CODE>null</CODE> if the argument <CODE><B>content</B>
	 *        </CODE>is <CODE>null</CODE></LI>
	 * </UL>
	 */
	public static String readSingleLine(StringBuilder content) {
		if (content == null) {
			return null;
		}
		return readSingleLine(content.toString());
	}
	
	/**
	 * Reads the first line of the content.
	 * @param content
	 * @return
	 * <UL>
	 *    <LI>the first line of the content</LI>
	 *    <LI><CODE>null</CODE> if the argument <CODE><B>content</B>
	 *        </CODE>is <CODE>null</CODE></LI>
	 * </UL>
	 */
	public static String readSingleLine(String content) {
		if (content == null) {
			return null;
		}
		
		// Finds out the ending position of the first line of the 
		// content.
		int lineEndAt = content.indexOf(SystemUtils.getLineSeparator());
		
		if (lineEndAt >= 0) {
			return content.substring(0, lineEndAt);
		} else {
			return content;
		}
	}
	
	/**
	 * Reads the content from the specified file.
	 * 
	 * @param path - the path to be used for the file. 
	 * @return
	 * <UL>
	 *    <LI>the content in {@link StringBuilder}</LI>
	 *    <LI><CODE>null</CODE> if the argument <CODE><B>path</B></CODE>
	 *        is <CODE>null</CODE></LI>
	 *    <LI><CODE>null</CODE> if the file does not exist</LI>
	 *    <LI><CODE>null</CODE> if there is an {@link IOException}</LI>
	 * </UL>
	 * @see {@link #read(File)}
	 * @see {@link #read(File, String)}
	 */
	public static StringBuilder read(String path) {
		if (path == null) {
			return null;
		}
		return read(new File(path));
	}
	
	/**
	 * Reads the content from the specified file.
	 * 
	 * @param file - a File to be opened for reading characters from.
	 * @return
	 * <UL>
	 *    <LI>the content in {@link StringBuilder}</LI>
	 *    <LI><CODE>null</CODE> if the argument <CODE><B>file</B></CODE>
	 *        is <CODE>null</CODE></LI>
	 *    <LI><CODE>null</CODE> if the file does not exist</LI>
	 *    <LI><CODE>null</CODE> if there is an {@link IOException}</LI>
	 * </UL>
	 * @see {@link #read(String)} 
	 * @see {@link #read(File, String)}
	 */
	public static StringBuilder read(final File file) {
		StringBuilder content = new StringBuilder();
		
		// Does the file not exist?
		if (file == null || file.exists() == false) {
			return null;
		}
		
		// reads the file content
		try {
			// checks the header info to see if there are any header 
			// bytes 
			FileHeader header = FileHeader.getHeader(file);
			
			// this purpose is mainly for Windows OS series
			if (header == FileHeader.NO_HEADER) {
				content = readViaFileReader(file);
			} else {
				content = readViaFileInputStream(file, header);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return content;
	}
	
	/**
	 * Reads the file content via {@link FileReader}.
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static StringBuilder readViaFileReader(File file) 
			throws FileNotFoundException, IOException {
		final int BUFFER_LENGTH = 1024;
		StringBuilder content = new StringBuilder();
		
		// creates a buffering character-input stream
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		// creates a buffering character array
		char [] buffer = new char [BUFFER_LENGTH];
		int length;
		
		// reads characters into the array
		while ( (length = br.read(buffer)) != -1 ) {
			content.append(buffer, 0, length);
		}
		br.close();
		return content;
	}
	
	private static StringBuilder readViaFileInputStream(File file, 
			FileHeader header) throws FileNotFoundException, IOException {
		final int BUFFER_LENGTH = 1024;
		StringBuilder content = new StringBuilder();
		
		// creates a buffering byte-input & output streams
		BufferedInputStream bis = 
				new BufferedInputStream(new FileInputStream(file));
		ByteArrayOutputStream baos = 
				new ByteArrayOutputStream(BUFFER_LENGTH);
		
		// creates a buffering character array
		byte [] buffer = new byte [BUFFER_LENGTH];
		int length;
		
		// skips the head info
		bis.skip(header.getHeaderLength());
		
		// reads characters into the array
		while ((length = bis.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		content.append(new String(
				baos.toByteArray(), header.getCharset()));
		return content;
	}
	
	/**
	 * <P>Reads the content from the specified file with the specific 
	 * converter (It could be UTF-8, UTF-16, ISO-8859-1, etc.).</P>
	 * 
	 * <P>Please note that if there is the header info, there will be
	 * an extra character in the beginning of the content.
	 * <PRE>
	 *    [0] ? (65279 = 0xFEFF) for UTF-16 little endian
	 *    [0] ? (65533 = 0xFFFD) for UTF-16 big endian
	 * </PRE> 
	 * </P>
	 * 
	 * @param file - a File to be opened for reading characters from.
	 * @param charsetName - identifies the character converter to use
	 *    The name of a supported {@link java.nio.charset.Charset 
	 *    <code>charset</code>}
	 * @return
	 * <UL>
	 *    <LI>the content in {@link StringBuilder}</LI>
	 *    <LI><CODE>null</CODE> if the argument <CODE><B>file</B></CODE>
	 *        is <CODE>null</CODE></LI>
	 *    <LI><CODE>null</CODE> if the file does not exist</LI>
	 *    <LI><CODE>null</CODE> if there is an {@link IOException}</LI>
	 * </UL>
	 * @see {@link #read(String)}
	 * @see  {@link #read(File))}
	 * @See {@link Charset}
	 */
	public static StringBuilder read(File file, String charsetName) {
		final int BUFFER_LENGTH = 1024;
		StringBuilder sb = new StringBuilder();
		BufferedReader br;
		char [] buffer = new char [BUFFER_LENGTH];
		int length;
		
		// Does the file not exist?
		if (file == null || file.exists() == false) {
			return null;
		}
		
		// reads the file content
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), charsetName));
			while ( (length = br.read(buffer)) != -1 ) {
				sb.append(buffer, 0, length);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return sb;
	} 

	/**
	 * Writes the content to the specific file.
	 * @param file
	 * @param content
	 * @return
	 */
	public static boolean write(String file, String content) {
		return write(new File(file), content);
	}
	
	/**
	 * Writes the content to the specific file.
	 * @param file
	 * @param content
	 * @return 
	 *    true if writing to the specified file successfully; false 
	 *  otherwise 
	 */
	public static boolean write(File file, String content) {
		BufferedWriter bw;
		
		if (checkWrite(file) == false) {
			return false;
		}
		
		try {
			bw = new BufferedWriter(new FileWriter(file));
			if (content == null) {
				content = "";
			}
			bw.write(content);
			bw.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Writes the content to the specific file. 
	 * Like "abc".getBytes("UTF-8");
	 * 
	 * @param file
	 * @param content
	 * @return
	 */
	public static boolean write(String file, byte [] content) {
		return write(new File(file), content);
	}
	
	/**
	 * Writes the content to the specific file.
	 * Like "abc".getBytes("UTF-8");
	 * 
	 * @param file
	 * @param content
	 * @return 
	 *    true if writing to the specified file successfully; false 
	 *  otherwise 
	 */
	public static boolean write(File file, byte [] content) {
		BufferedOutputStream bos;
		
		if (checkWrite(file) == false) {
			return false;
		}
		
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file));
			if (content != null) {
				bos.write(content);
			}
			bos.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static boolean checkWrite(File file) {
		if (file == null) {
			String e = "The file can not be null.";
			throw new IllegalArgumentException(e);
		}
		
		// Permission denied?
		if (file.exists()) {
			if ( ! file.canWrite()) {
				
				// become to writable?
				if ( ! file.setWritable(true)) { // for the owner
					
					// If the user does not have permission to change
					// the access permissions of the file, the 
					// operation will fail.
					Exception e = new IllegalAccessException(
							"Failed to set the write permission." +
							"\n - file: " + file);
					e.printStackTrace(System.err);
					return false;
				}
			}
		} else {
			// the specified file does not exist, then:
			
			// Creates the parent directories first if they do not 
			// exist, or it will crash, 
			// like
			//     java.io.FileNotFoundException: a\b\c\d.txt 
			//     (The system cannot find the path specified.)
			if (!ensureParentDirectory(file)) {
				System.err.println(
						"Failed to create the file directory \"" + 
						file.getParent() + "\"!");
				return false;
			}
		}
		
		// It seems to not need to create the file.
		//try {
		//	// creates the specified file
		//	if (!file.createNewFile()) {
		//		System.err.println("Failed to create the file " + 
		//				file + "!");
		//		return false;
		//	}
		//} catch (IOException e) {
		//	e.printStackTrace();
		//	return false;
		//}
		return true;
	}
	
	/**
	 * To ensure that the parent directory of the specific file exists.
	 * if the file does not exist, it will create its parent first. If 
	 * its parent already exists, nothing to do and returns true.
	 * 
	 * @param file
	 * @return
	 *    true if it exists; false otherwise
	 */
	public static boolean ensureParentDirectory(File file) {
		if (file == null) {
			return false;
		}
		
		File parent = file.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs()) {
			return false;
		}  else {
			//(1) "parent is null" means there is no parent
			//(2) parent exists
			//(3) makes the parent directory successfully
			return true;
		}
	}
	
	/**
	 * Gets the file-system root (the volume path) of the file. If the 
	 * given <CODE>file</CODE> is <CODE>null</CODE>, the file will be 
	 * pointed to the current working directory.
	 * 
	 * @param file
	 * @return
	 *    Returns the non-null root file.
	 */
	public static File getRoot(File file) {
		if (file == null) {
			file = new File(".");
			try {
				file = file.getCanonicalFile();
			} catch (IOException e) {
				e.printStackTrace(); // never happens
			}
		}
		
		// recursively get the parent until it returns null
		File previous;
		do {
			previous = file;
			file = file.getParentFile();
		} while (file != null);
		return previous;
	}
	
	/**
	 * Gets an available file. If the given file does not exist, 
	 * trying to get its parent. (If needed, recursively find an 
	 * available parent).
	 * 
	 * @param file - the current file
	 * @return
	 *   Returns an available file. Or <code>null</code> if there is 
	 * no file available in the volume.
	 */
	public static File getAvailableSuperOrItself(File file) {
		if (file == null) {
			file = new File(".");
			try {
				file = file.getCanonicalFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return file;
		}
		
		// The file doesn't exist. Trying to get the parent.
		while (file != null && !file.exists()) {
			file = file.getParentFile();
		}
		return file;
	}
	
	/**
	 * Gets the extension of the file name.
	 * @param file
	 * @return
	 *  <UL>
	 *     <LI>the extension of the file name</LI>
	 *     <LI><CODE>null</CODE> if the file name is without the 
	 *         extension</LI>
	 *     <LI><CODE>null</CODE> if the file is <CODE>null</CODE></LI>
	 *  </UL>
	 */
	public static String getExtension(File file) {
		if (file == null) {
			return null;
		}
		
		String name = file.getName();
		int splitAt = name.indexOf('.');
		
		// CASE 1: 0 < splitAt
		//   There is at least 1 period in the name.
		// CASE 2: splitAt < name.length() - 1
		//   Except the case that the period is at the end of the name.
		//   Where the extension will be empty.
		if (0 < splitAt && splitAt < name.length() - 1) {
			return name.substring(splitAt + 1).toLowerCase();
		}
		return null;
	}
}
