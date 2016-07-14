package obj;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import util.MerkleTree;

/**
 * <b>References</b>
 * <ul>
 * <li><a href="https://docs.oracle.com/javase/tutorial/jaxp/xslt/writingDom.html">Oracle Java Tutorials:
 * Writing Out a DOM as an XML File</a></li>
 * </ul>
 */
public class NetworkNode {
	
	public final static String ALGORITHM = "RSA";
	public final static String BLOCKCHAIN = "dat/blockchain.xml";
	public final static int REWARD = 50;
	public static String difficulty = "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
	
	private Logger logger = Logger.getLogger(NetworkNode.class.getName());
	private Wallet wallet;
	private ArrayList<Transaction> mempool;
	
	public NetworkNode() {
		wallet = new Wallet();
		mempool = new ArrayList<Transaction>();
//		updateMempool();
	}
	
	public Wallet wallet() {
		return wallet;
	}
	
	public void connect() {
		findPeers();
		updateBlockchain();
	}
	
	public void mine() throws NoSuchAlgorithmException, URISyntaxException, InvalidKeySpecException {
		Block newBlock = makeBlock();
		System.out.println("nonce: " + newBlock.header().nonce());
		extendBlockchain(BLOCKCHAIN, newBlock);
	}
	
	/*
	 * Private methods
	 */
	private void findPeers() {
		
	}
	
	private void updateBlockchain() {
		
	}
	
	private void updateMempool(Transaction newTransaction) {
		mempool.add(newTransaction);
	}
	
	private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		
		KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
		generator.initialize(512, new SecureRandom());
		
		return generator.generateKeyPair();	
	}
	
	private Block makeBlock() throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		ArrayList<Transaction> transactions = cloneMempool();
		
		Transaction mint = new Transaction();
		
		KeyPair newKeyPair = generateKeyPair();
		wallet.save(newKeyPair, REWARD);
		
		Output gold = new Output(newKeyPair.getPublic(), REWARD);
		
		mint.addOutput(gold);
		transactions.add(mint);
		
		MerkleTree tree = new MerkleTree();
		String root = tree.getRoot(transactions);	// TODO mint transaction should be either first or last
		
		BlockHeader newHeader = new BlockHeader("0013965daa8b69d8f56c3482f2d6894a56351e4a4c1ab38c135a189183e949a6", root);	// TODO
		String pow = newHeader.hash(difficulty);
		
		Block newBlock = new Block(newHeader, pow, transactions);
		mempool.clear();
		
		return newBlock;
		
	}
	
	private void extendBlockchain(String file, Block newBlock) throws URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException {
		
		URI domain = NetworkNode.class.getClassLoader().getResource(file).toURI();
		Document doc = parse(domain);
		doc.getDocumentElement().normalize();
		
		// Count number of existing blocks
		NodeList blocks = doc.getElementsByTagName("block");
		int height = blocks.getLength();
		height++;
		
		System.out.println("root:" + doc.getDocumentElement().getNodeName());	// TODO remove
		
		Node block = doc.createElement("block");	// TODO attribute block height/id
		block.appendChild(doc.createElement("header"));
		block.appendChild(doc.createElement("body"));
		
		// Block header
		Node child = block.getFirstChild();
		
		Node grandchild = doc.createElement("previousPoW");
		grandchild.setTextContent(newBlock.header().previousPoW());
		child.appendChild(grandchild);
		
		grandchild = doc.createElement("pow");
		grandchild.setTextContent(newBlock.pow());
		child.appendChild(grandchild);
		
		grandchild = doc.createElement("merkleRoot");
		grandchild.setTextContent(newBlock.header().merkleRoot());
		child.appendChild(grandchild);
		
		grandchild = doc.createElement("nonce");
		grandchild.setTextContent(Integer.toString(newBlock.header().nonce()));
		child.appendChild(grandchild);
		
		grandchild = doc.createElement("difficulty");
		grandchild.setTextContent(difficulty);
		child.appendChild(grandchild);
		
		// Block body
		child = block.getLastChild();
		
		ArrayList<Transaction> transactions = newBlock.transactions();
		
		for (int i = 0; i < transactions.size(); i++) {
			
			Transaction tx = transactions.get(i);
			ArrayList<Input> inputs = tx.inputs();
			
			grandchild = doc.createElement("transaction");
			child.appendChild(grandchild);
			
			for (int j = 0; j < inputs.size(); j++) {
				
				Output output = inputs.get(j).output();
				
				org.w3c.dom.Node greatgrandchild = doc.createElement("input");
				greatgrandchild.setTextContent(output.recipientAddress());
				grandchild.appendChild(greatgrandchild);
				
			}
			
			ArrayList<Output> outputs = tx.outputs();
			
			for (int k = 0; k < outputs.size(); k++) {
				
				Output output = outputs.get(k);
				
				Node node = doc.createElement("output");
				
				Node element = doc.createElement("address");
				element.setTextContent(output.recipientAddress());
				node.appendChild(element);
				
				element = doc.createElement("amount");
				element.setTextContent(Integer.toString(output.amount()));
				node.appendChild(element);
				
				grandchild.appendChild(node);
				
			}
			
		}
		
		doc.getDocumentElement().appendChild(block);
		write(domain, doc.getDocumentElement());
	}
	
	private Document parse(URI domain) {
		
		Document doc = null;
		
		try {
			
			File file = new File(domain);
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			
			doc = docBuilder.parse(file);
			
		} catch (SAXParseException spe) {
            
			// Error generated by the parser
            System.out.println("\n** Parsing error" + ", line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
            System.out.println("  " + spe.getMessage() );
  
            // Use the contained exception, if any
            Exception x = spe;
            if (spe.getException() != null) x = spe.getException();
            x.printStackTrace();
            
        } catch (SAXException sxe) {
           
        	// Error generated by this application (or a parser-initialization error)
            Exception x = sxe;
            if (sxe.getException() != null) x = sxe.getException();
            x.printStackTrace();
            
        } catch (ParserConfigurationException pce) {
            
        	// Parser with specified options cannot be built 
            pce.printStackTrace();
            
        } catch (IOException ioe) {
            
        	// I/O error
            ioe.printStackTrace();
            
        }
		
		return doc;
	}

	// TODO append NEW only!
	private void write(URI domain, Node node) {

	    DOMSource source = new DOMSource(node);
	    StreamResult result = new StreamResult(new File(domain));
	    
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");
			
			Document document = parse(domain);
			if (document.getDoctype() != null) {
			    String systemValue = (new File (document.getDoctype().getSystemId())).getName();
			    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);    
			}
			
			transformer.transform(source, result);
			
		} catch (TransformerConfigurationException tce) {
			tce.printStackTrace();
		} catch (TransformerException te) {
			te.printStackTrace();
		}
		
	}
	
	private ArrayList<Transaction> cloneMempool() {
		
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		for (int i = 0; i < mempool.size(); i++) transactions.add(mempool.get(i));
		
		return transactions;
	}
	
}