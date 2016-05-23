package idea.inspired.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This purpose of the header is mainly for Windows OS series.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Byte_order_mark">Byte order mark</a>
 * @see <a href="http://zh.wikipedia.org/wiki/位元組順序記號">位元組順序記號</a>
 * 
 * @author tsungjung411@yahoo.com.tw
 * @since 2014.05.27
 */
public enum FileHeader {
	UTF8(
			Charset.forName("UTF-8"), 
			new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF}),
			
	UTF16_BIG_ENDIAN(
			Charset.forName("UTF-16BE"), // 254 255
			new byte[] {(byte)0xFE, (byte)0xFF}),
			
	UTF16_LITTLE_ENDIAN(// default unicode
			Charset.forName("UTF-16LE"), // 255 254
			new byte[] {(byte)0xFF, (byte)0xFE}), 
			
	UTF32_BIG_ENDIAN(
			Charset.forName("UTF-32BE"), // 0 0 254 255
			new byte[] {(byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFF}),
			
	UTF32_LITTLE_ENDIAN(
			Charset.forName("UTF-32LE"), // 255 254 0 0
			new byte[] {(byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00}), 
			
	NO_HEADER(
			Charset.forName(System.getProperty("file.encoding")),
			null);
	
	
	
	/** 
	 * The name of the character set that is used to defines 
	 * methods for creating decoders and encoders.
	 */
	private Charset mCharset;
	
	/** Defines the byte sequence  */
	private byte [] mBytes;
	
	/** The private default constructor. */
	private FileHeader(Charset charset, byte [] bytes) {
		this.mCharset = charset;
		this.mBytes = bytes;
	}
	
	/**
	 * Returns the charset of the header.
	 * @return
	 */
	public Charset getCharset() {
		return this.mCharset;
	}
	
	/**
	 * Returns the length of the header.
	 * @return
	 */
	public int getHeaderLength() {
		return this.mBytes.length;
	}
	
	/**
	 * Gets the header info, including the name of the character 
	 * set and the header bytes.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static FileHeader getHeader(File file) throws IOException {
		final boolean DEBUG = false;
		FileHeader header;
		byte [] bytes = new byte [4];
		int length;
		
		// reads some number of bytes from the input stream
		FileInputStream fis = new FileInputStream(file);
		length = fis.read(bytes);
		fis.close();
		
		if (DEBUG) {
			FileManager.dumpHead(bytes, length, System.err);
		}
		
		if (compareBytes(bytes, length, UTF8)) {
			header = UTF8;
		} else if (compareBytes(bytes, length, UTF16_BIG_ENDIAN)) {
			header = UTF16_BIG_ENDIAN;
		} else if (compareBytes(bytes, length, UTF16_LITTLE_ENDIAN)) {
			header = UTF16_LITTLE_ENDIAN;
		} else if (compareBytes(bytes, length, UTF32_BIG_ENDIAN)) {
			header = UTF32_BIG_ENDIAN;
		} else if (compareBytes(bytes, length, UTF32_LITTLE_ENDIAN)) {
			header = UTF32_LITTLE_ENDIAN;
		} else {
			header = NO_HEADER;
		}
		return header;
	}

	private static boolean compareBytes(byte [] inputBytes, 
			int intputLength, FileHeader header) {
		byte [] bytes = header.mBytes;
		
		// the data is not enough
		if (intputLength < bytes.length) {
			return false;
		}
		
		// compares the bytes
		for (int i = 0; i < bytes.length; i++) {
			if (inputBytes[i] != bytes[i]) {
				return false; // one byte is not matched
			}
		}
		return true;
	}
}
