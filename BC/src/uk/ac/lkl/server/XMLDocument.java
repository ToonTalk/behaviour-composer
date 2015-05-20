package uk.ac.lkl.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class XMLDocument {
    static private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder = null;
    private Document document = null;
    private Exception exception = null;
    
    public XMLDocument() {
	super();
	try {
	    builder = factory.newDocumentBuilder();
	    document = builder.newDocument();
	} catch (Exception e) {
//	    ServerUtils.logException(e, getServletContext().getRealPath("/"));
	    e.printStackTrace();
	    exception = e;
	}
    }
    
    public XMLDocument(String xml) {
	super();
	try {
	    builder = factory.newDocumentBuilder();
	    // TODO: make this general
	    xml = xml.replace("¬","&not;");
	    document = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
	} catch (IOException e) {
	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
		    "IOException in parsing XML in " + xml);
	    e.printStackTrace();
	    exception = e;
	} catch (SAXException e) {
	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
		    "SAXException in parsing XML." +  " in " + xml);
	    e.printStackTrace();
	} catch (ParserConfigurationException e) {
	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
		    "ParserConfigurationException in parsing XML");
	    e.printStackTrace();
	}
    }

    public Document getDocument() {
        return document;
    }

    public Exception getException() {
        return exception;
    }

}
