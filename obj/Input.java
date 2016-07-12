package obj;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;

import util.SHA256;

public class Input {
	
	private String reference;
	private Output output;
	private PrivateKey privateKey;
	
	public Input(String reference, Output output, PrivateKey privateKey) {
		this.reference = reference;
		this.output = output;
		this.privateKey = privateKey;
	}
	
	public String sign(ArrayList<Output> outputs) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < outputs.size(); i++) {
			str.append(outputs.get(i).toString());
			str.append("/");
		}
		
		byte[] bytes = str.toString().getBytes();
		
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey, new SecureRandom());
		signature.update(bytes);
		bytes = signature.sign();
		
		SHA256 sha256 = new SHA256();
		return sha256.bytesDecToHex(bytes);
	}
	
	public String reference() {
		return reference;
	}
	
	public Output output() {
		return output;
	}

}
