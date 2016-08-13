package util;

import java.io.File;
import java.net.URI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLio {
	
	public static void write(String filename, Document document, Node node) {

		try {
			
			URI domain = XMLio.class.getClassLoader().getResource(filename).toURI();
			DOMSource source = new DOMSource(node);
		    StreamResult result = new StreamResult(new File(domain));		
	    
		    TransformerFactory tFactory = TransformerFactory.newInstance();
		
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");
			
			if (document.getDoctype() != null) {
			    String systemValue = (new File (document.getDoctype().getSystemId())).getName();
			    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);    
			}
			
			transformer.transform(source, result);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Document parse(String filename) {
		
		Document doc = null;
		
		try {
			
			URI domain = XMLio.class.getClassLoader().getResource(filename).toURI(); 
			File file = new File(domain);
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			
			doc = docBuilder.parse(file);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}

}