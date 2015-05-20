package uk.ac.lkl.server;

//import java.io.ByteArrayInputStream;
//import javax.xml.rpc.encoding.XMLType;
//import javax.xml.rpc.ParameterMode;
//
//import javax.xml.namespace.QName;
//import javax.xml.parsers.DocumentBuilderFactory;

//import org.apache.axis.Message;
//import org.apache.axis.client.Call;
//import org.apache.axis.client.Service;
import org.apache.axis.encoding.Base64;
//import org.apache.axis.message.SOAPHeaderElement;
//import org.apache.axis.message.SOAPEnvelope;
//import org.w3c.dom.Document;

import java.net.Socket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.io.*;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import uk.ac.lkl.client.rpc.MathDiLSService;
import uk.ac.lkl.client.Modeller;

@SuppressWarnings("serial")
public class MathDiLSServiceImpl extends RemoteServiceServlet implements MathDiLSService {
    static String functionSuspended;
    static String fileTitleSaved;
    static String fileDescriptionSaved;
    static String fileTypeSaved; // i.e. model, object, or equation
    static String contentsSaved;
    static String actionBase = "http://remath.cti.gr/MathDiLS/";
    static String nameSpaceUri = "http://remath.cti.gr/MathDiLS";
    static String endpointSOAP = "http://remath.cti.gr/MathDiLS/MathDiLS.asmx?WSDL";
    static final public String post1 = "POST /MathDiLS/MathDiLS.asmx?WSDL HTTP/1.0\r\nContent-Type: text/xml; charset=utf-8\r\nAccept: application/soap+xml, application/dime, multipart/related, text/*\r\nHost: remath.cti.gr\r\nCache-Control: no-cache\r\nPragma: no-cache\r\nSOAPAction: \"http://remath.cti.gr/MathDiLS/";
    static final public String post2 = "\"\r\nContent-Length: ";
    static final public String post3 = "\r\n\r\n";
    static final public String DDAFileReadEnvelope1 = "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><soapenv:Header><AuthHeader xmlns='http://remath.cti.gr/MathDiLS' soapenv:actor='' soapenv:mustUnderstand='0'><Username>KenKahn</Username><password>ahclem</password></AuthHeader></soapenv:Header><soapenv:Body><DDAFileRead xmlns='http://remath.cti.gr/MathDiLS'><DDAFileId xsi:type='xsd:int'>";
    static final public String DDAFileReadEnvelope2 = "</DDAFileId></DDAFileRead></soapenv:Body></soapenv:Envelope>";
    static final public String ListDDAFilesEnvelope = "<?xml version='1.0' encoding='utf-8'?><soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'><soap:Header><AuthHeader xmlns='http://remath.cti.gr/MathDiLS'><Username>KenKahn</Username><password>ahclem</password></AuthHeader></soap:Header><soap:Body><listDDAFiles xmlns='http://remath.cti.gr/MathDiLS'><DDACategoryId>37</DDACategoryId><return_content>false</return_content></listDDAFiles></soap:Body></soap:Envelope>";
    static final public String insertDDAFile2Part1 = "<?xml version='1.0' encoding='utf-8'?><soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'><soap:Header><AuthHeader xmlns='http://remath.cti.gr/MathDiLS'><Username>KenKahn</Username><password>ahclem</password></AuthHeader></soap:Header><soap:Body><insertDDAFile2 xmlns='http://remath.cti.gr/MathDiLS'><title>";
    static final public String insertDDAFile2Part2 = "</title><desc>";
    static final public String insertDDAFile2Part3 = "</desc><level>0</level><file_s>";
    static final public String insertDDAFile2Part4 = "</file_s><filename>MoPiX_Equations.xml</filename><MIMEType>.xml</MIMEType><DDAID>6</DDAID><catIds><int>37</int></catIds><language>English</language></insertDDAFile2></soap:Body></soap:Envelope>";
    static int responseStringIndex = 0;
    public static String currentResponseString = null;

    
    public String DDAFileRead(int fileID) {
	return extractMathML(postAndReadResponse("DDAFileRead",
		             DDAFileReadEnvelope1 + fileID + DDAFileReadEnvelope2));
    }

    public String[] listDDAFiles() {
	return extractFileDescriptions(postAndReadResponse("listDDAFiles", ListDDAFilesEnvelope));
    }

    public String insertDDAFile2(String title, String description, String content) {
	String UUEncodedcontent = new String(Base64.encode(content.getBytes()));
	String envelope = insertDDAFile2Part1 + title + insertDDAFile2Part2
		          + description + insertDDAFile2Part3 + UUEncodedcontent
		          + insertDDAFile2Part4;
	return extractResponse(postAndReadResponse("insertDDAFile2", envelope));
    }

    public String postAndReadResponse(String operationName, String envelope) {
	try {
	    Socket socket = new Socket("remath.cti.gr", 80);
	    OutputStream out = socket.getOutputStream();
	    String post = post1 + operationName + post2 + envelope.length() + post3;
	    out.write(post.getBytes());
	    out.write(envelope.getBytes());
	    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    // StringBuilder response = new StringBuilder();
	    char response[] = null;
	    String userInput;
	    String contentLengthHeader = "Content-Length: ";
	    while ((userInput = in.readLine()) != null) {
		if (userInput.startsWith(contentLengthHeader, 0)) {
		    int contentLength = 
			Integer.parseInt(userInput.substring(contentLengthHeader.length()));
		    in.skip(2); // new lines between header and envelope
		    response = new char[contentLength];
		    int charactersRead = in.read(response);
		    if (charactersRead < contentLength) {
			StringBuilder buffer = new StringBuilder();
			buffer.append(response, 0, charactersRead);
			while ((userInput = in.readLine()) != null) {
			    buffer.append(userInput);
			}
			out.close();
			in.close();
			socket.close();
			return buffer.toString();
		    }
		    // System.out.println(new String(response));
		    break;
		} else if (userInput.equals("HTTP/1.1 400 Bad Request")) {
		    return userInput;
		}
		// response.append(userInput);
	    }
	    out.close();
	    in.close();
	    socket.close();
	    return new String(response);
	} catch (UnknownHostException e) {
	    // caller needs to deal with this...
	    return "Unable to connect to ReMath MathDiLS server. Check network connections and try again.";
	} catch (SocketException e) {
	    return "Able to connect to ReMath MathDiLS server followed by connection errors. Check network connections and try again.";
	} catch (Exception e) {  
	    return ServerUtils.logException(e, "in connecting to MathDiLS");
	}
    }

    public String extractResponse(String responseString) {
	String errorId = extractFirstTagValue("_errorId", responseString);
	if (errorId == null || !errorId.equals("200")) {
	    String errorMessage = extractFirstTagValue("_errorMessage", responseString);
	    if (errorMessage == null)
		return "Unknown error occured.";
	    return errorMessage;
	}
	return "Model saved.";
    }

    public String extractMathML(String responseString) {
	// use extractResponse here too
	int startContent = responseString.indexOf("<cnt>");
	if (startContent >= 0) {
	    String restOfResponseString = 
		responseString.substring(startContent + 5); // plus length of <cnt>
	    int endContent = restOfResponseString.indexOf("</cnt>");
	    if (endContent >= 0) {
		String content = restOfResponseString.substring(0, endContent);
		String decoded = new String(Base64.decode(content));
		// System.out.println(decoded);
		return decoded;
	    }
	}
	Modeller.setStatusLine("Unable to extract contents from " + responseString);
	return "";
    }

    public String[] extractFileDescriptions(String responseString) {
	// returns an array of strings alternating between descriptions and id numbers (as strings)
	int count = 0;
	int index = responseString.indexOf("<ID>");
	while (index >= 0) {
	    count++;
	    index = responseString.indexOf("<ID>", index + 5);
	}
	if (count == 0) {
	    // warn?
	    return new String[] { responseString };
	}
	String result[] = new String[count * 2];
	int i = 0;
	currentResponseString = responseString;
	responseStringIndex = 0;
	String idString;
	while ((idString = extractNextTagValue("ID")) != null) {
	    result[i++] = idString;
	    result[i++] = extractNextTagValue("desc");
	}
	return result;
    }

    public String extractFirstTagValue(String tagName, String xmlString) {
	currentResponseString = xmlString;
	responseStringIndex = 0;
	return extractNextTagValue(tagName);
    }

    public String extractNextTagValue(String tagName) {
	if (currentResponseString == null)
	    return null;
	responseStringIndex = 
	    currentResponseString.indexOf("<" + tagName + ">", responseStringIndex);
	if (responseStringIndex >= 0) {
	    int tagNameLength = tagName.length();
	    int start = responseStringIndex + 2 + tagNameLength; 
	    // + 2 for <>
	    responseStringIndex = 
		currentResponseString.indexOf("</" + tagName + ">", start);
	    if (responseStringIndex >= 0) {
		String value = 
		    currentResponseString.substring(start, responseStringIndex);
		responseStringIndex += 3 + tagNameLength; // 3 for </>
		return value;
	    }
	}
	return null;
    }

}
