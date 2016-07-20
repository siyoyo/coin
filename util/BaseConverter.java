package util;

public class BaseConverter {
	
	/**
	 * Converts a byte array in base 10 to base 16.
	 * @param bytesDecimal Byte array in base 10
	 * @return Byte array in base 16
	 */
	public static String bytesDecToHex(byte[] bytesInDecimal) {
		
		StringBuffer str = new StringBuffer();
		
		for (int i = 0; i < bytesInDecimal.length; i++) {
			
			String hex = Integer.toHexString(0xff & bytesInDecimal[i]);
			
			if (hex.length() == 1) str.append('0');	// ensures that one byte always has two hex digits
			str.append(hex);
		}
		
		return str.toString();
	}
	
	public static byte[] stringHexToDec(String stringInHex) {
		
		int size = 0;
		
		if (stringInHex.length() % 2 == 0) size = stringInHex.length() / 2;
		else size = 1 + (stringInHex.length() / 2); 

		byte[] result = new byte[size];
		
		String chars;
		int i = 0, j = 0;
		
		while (i < stringInHex.length()) {
			
			if (i + 2 > stringInHex.length()) chars = "0x" + stringInHex.substring(i, i + 1) + "0";
			else chars = "0x" + stringInHex.substring(i, i + 2);
			
			int a = Integer.decode(chars);
			
			if (a > 127) a = (byte) (-128 + (a - 128));
			result[j] = (byte) a;
			
			i = i + 2; 
			j++; 
		}
		
		return result;
	}

}