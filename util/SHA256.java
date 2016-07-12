package util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <b> References </b>
 * <ul>
 * <li> <a href="https://gist.github.com/qpark99/7652032"> GitHub Gist qpark99/Digest.java </a> </li>
 * </ul>
 * Verified correct using <a href="http://www.xorbin.com/tools/sha256-hash-calculator"> XORBin SHA-256 hash calculator </a>
 */
public class SHA256 {
	
	private MessageDigest md;
	
	public SHA256() throws NoSuchAlgorithmException {
		md = MessageDigest.getInstance("SHA-256");
	}
	
	/**
	 * Hashes a given string using SHA-256 algorithm.  
	 * <p> First, the input string is decoded using UTF-8 to an array of bytes;   
	 * each byte is an integer in base 10.  Next, each byte is hashed and the 
	 * result is also in base 10.  Finally, the result is converted into base 16. </p>
	 * 
	 * @param string Input string
	 * @return String of 64 hex digits that represent the hash of s
	 */
	public String hashString(String string) {
		byte[] byteString = string.getBytes(Charset.forName("UTF-8"));
		return hashBytes(byteString);
	}
	
	/**
	 * Same as hashString but input type is an array of bytes.
	 * @param bytes Input array of bytes
	 * @return String of 64 hex digits that represent the hash of bytes
	 */
	public String hashBytes(byte[] bytes) {
		byte[] bytesDecimal = md.digest(bytes);
		return bytesDecToHex(bytesDecimal);	
	}
	
	/**
	 * Converts a byte array in base 10 to base 16.
	 * @param bytesDecimal Byte array in base 10
	 * @return Byte array in base 16
	 */
	public String bytesDecToHex(byte[] bytesDecimal) {
		
		StringBuffer str = new StringBuffer();
		
		for (int i = 0; i < bytesDecimal.length; i++) {
			
			String hex = Integer.toHexString(0xff & bytesDecimal[i]);
			if (hex.length() == 1) str.append('0');	// ensures that one byte always has two hex digits
			str.append(hex);
		}
		
		return str.toString();
	}

}
