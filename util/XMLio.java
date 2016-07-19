package util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XMLio {
	
	public static void write(String filename, Document document, Node node) throws ParserConfigurationException, SAXException, IOException, TransformerException, URISyntaxException {

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
	}
	
	public static Document parse(String filename) throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
			
		URI domain = XMLio.class.getClassLoader().getResource(filename).toURI(); 
		
		File file = new File(domain);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		
		Document doc = docBuilder.parse(file);
		
		return doc;
	}

}