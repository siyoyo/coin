package test;

import static org.junit.Assert.*;

import java.security.interfaces.RSAPublicKey;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import obj.TransactionReference;
import util.BlockExplorer;

/**
 * <b>References</b>
 * <ul>
 * <li><a href="https://github.com/junit-team/junit4/wiki/Assertions">JUnit Assertions</a></li>
 * <li><a href="http://junit.org/junit4/javadoc/4.12/org/junit/Before.html">Annotation Type Before</a></li>
 * </ul>
 */
public class BlockExplorerTest {

	private String file = "dat/blockchain.xml";
	private BlockExplorer explorer;
	private TransactionReference reference;
	
	@Before
	public void initialise() {
		explorer = new BlockExplorer(file);
		reference = new TransactionReference("00beca72451ca0eb365c622f8a35997eb56773c9b05ad91a9ccf24ecec33e384", "1", "1");
	}
	
	@Test
	public void testGetLastBlockHeader() {
		assertEquals("00beca72451ca0eb365c622f8a35997eb56773c9b05ad91a9ccf24ecec33e384", explorer.getLastPoW());
	}
	
	@Test
	public void testGetBlockByHeight() {
		
		Node node = explorer.getBlockNodeByHeight("1");
		
		node = node.getFirstChild().getNextSibling();	// header node
		node = node.getFirstChild().getNextSibling();	// previousPoW node
		node = node.getNextSibling().getNextSibling();	// pow node
		node = node.getNextSibling().getNextSibling();	// merkleRoot node
		
		assertEquals("1e1c6a005317233a83915b07dd20e501163810ea6fe46962b08fbb6127ad6f92", node.getTextContent());
		
	}
	
	@Test
	public void testGetBlock() {
			
		Node node = explorer.getBlockNodeByHash("000337a5cfd5c7db77a49eeaf39c544c72c828ff79a35828b3c58ed6a8d09465");
		
		node = node.getFirstChild().getNextSibling();	// header node
		node = node.getFirstChild().getNextSibling();	// previousPoW node
		node = node.getNextSibling().getNextSibling();	// pow node
		node = node.getNextSibling().getNextSibling();	// merkleRoot node
		
		assertEquals("1e1c6a005317233a83915b07dd20e501163810ea6fe46962b08fbb6127ad6f92", node.getTextContent());
		
	}
	
	@Test
	public void testRecipientAddress() {
		
		RSAPublicKey publicKey = explorer.publicKey(reference);
		String address = publicKey.getModulus().toString(16);
		
		assertEquals("9ec35a14972974a70bcf7e5dd602f90b9047dcd63ed6b3815b1531fda053f8ede56efe308d1cb71590c1599353038190174f1d3e0c94cc8e8634c9f24df36953", address);
	}
	
	@Test
	public void testTransactionAmount() {
		assertEquals("50", explorer.transactionAmount(reference));
	}

}