package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * References:
 * <ul>
 * <li><a href="https://docs.oracle.com/javase/tutorial/jaxp/xslt/writingDom.html">Writing out DOM as an XML File</a></li>
 * </ul>
 */
public class XMLio {
	
	public final static String ENCODING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
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
	
	public static Document parse(Filename filename) {
		
		URI domain = null;
		File file;
		DocumentBuilderFactory factory;
		DocumentBuilder docBuilder;
		Document doc = null;
			
		try {
			
			domain = XMLio.class.getClassLoader().getResource(filename.fullname()).toURI();
			
		} catch (NullPointerException npe) {
			
			// File does not exist therefore create new file
			try {
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename.fullname())));
				String tag = "<" + filename.filename() + "></" + filename.filename() + ">";
				
				writer.write(ENCODING);
				writer.write(tag);
				writer.close();
				
				System.out.println(LocalDateTime.now() + " Created new file " + filename.fullname());
				
				domain = XMLio.class.getClassLoader().getResource(filename.fullname()).toURI();

			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
		} catch (URISyntaxException urise) {
			urise.printStackTrace();
		}
		
		try {

			file = new File(domain) ;
			
			factory = DocumentBuilderFactory.newInstance();
			docBuilder = factory.newDocumentBuilder();
			
			doc = docBuilder.parse(file);
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException saxe) {
			saxe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
			
		return doc;
	}

}