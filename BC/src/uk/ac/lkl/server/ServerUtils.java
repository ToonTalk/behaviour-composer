package uk.ac.lkl.server;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import uk.ac.lkl.server.ServerUtils;
import uk.ac.lkl.server.persistent.DataStore;
import uk.ac.lkl.server.persistent.MicroBehaviourData;
import uk.ac.lkl.server.persistent.MicroBehaviourNetLogoName;
import uk.ac.lkl.server.persistent.MicroBehaviourURLCopy;
import uk.ac.lkl.server.persistent.NetLogoNameSerialNumber;
import uk.ac.lkl.shared.CommonUtils;
import uk.ac.lkl.shared.NetLogoTokenizer;
import uk.ac.lkl.client.Modeller;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.google.apphosting.api.ApiProxy.ApiProxyException;
import com.google.apphosting.api.ApiProxy.OverQuotaException;
import com.google.apphosting.api.DeadlineExceededException;

public class ServerUtils {
    
    final static String codesForUUID = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-";
    
    // had better not occur "naturally" in NetLogo micro-behaviours
    private static final String WILL_BE_SUBSTITUTED_FOR_CONTENTS_OF_TEXTAREA = 
	" will-be-substituted-for-contents-of-text-area#";
    
    private static final String WILL_BE_SUBSTITUTED_FOR_MICRO_BEHAVIOURS = 
	" will-be-substituted-for-contents-of-macro-behaviour-named: ";
    
    private static ArrayList<String> notAttributes = new ArrayList<String>(
	    Arrays.asList("my-links", "my-in-links", "my-out-links", "my-location"));
    
    private static ThreadLocal<RemoteAPI> remoteAPIThreadLocal = new ThreadLocal<RemoteAPI>();

    public static String encodeUUID(UUID uuid) {
	StringBuilder encoding = new StringBuilder();
	long part1 = uuid.getLeastSignificantBits();
	long part2 = uuid.getMostSignificantBits();
	for (int i = 0; i < 11; i++) {
	    int index = (int) (0x3F & part1);
	    encoding.append(codesForUUID.charAt(index));
	    index = (int) (0x3F & part2);
	    encoding.append(codesForUUID.charAt(index));
	    part1 = (part1 >> 6);
	    part2 = (part2 >> 6);
	}
	return encoding.toString();
    }
    
    public static String generateGUIDString() {
	return encodeUUID(UUID.randomUUID());
    }
    
    public static String logException(Exception e, String message) {
	return logException(e, message, "", "");
    }
    
    public static String logException(Exception e, String message, String sessionGuid, String userGuid) {	    
	e.printStackTrace(System.err);
	String errorMessage = "Error " + message + " " + e.toString();
	ServerUtils.logError(errorMessage, sessionGuid, userGuid);
	return errorMessage;
    }
       
    /**
     * @param folderName
     * @param baseName
     * @param extension
     * @param runningLocalHost
     * @return a complete file name either on the server or local
     */
    @Deprecated
    public static String getFileName(String folderName, String baseName, String extension) {
	return getFullFolderName(folderName) + baseName + "." + extension;
    }
    
    /**
     * @param folderName
     * @param baseName
     * @param runningLocalHost
     * @return a complete file name either on the server or local
     */
    @Deprecated
    public static String getFileName(String folderName, String baseName) {
	return getFullFolderName(folderName) + baseName;
    }
    
    /**
     * @param folderName
     * @param runningLocalHost
     * @return a full folder path either on the server or local
     */
    @Deprecated
    public static String getFullFolderName(String folderName) {
	return CommonUtils.getHostBaseURL() + folderName + "/";
    }

    public static Timestamp createTimeStamp() {
	return new Timestamp(Calendar.getInstance().getTimeInMillis());
    }
    
    public static String getURL(String baseURL, String folderName, String baseName, String extension) {
	return CommonUtils.getHostBaseURL() + folderName + "/" + baseName + "." + extension;
    }
    
    public static String generateUniqueIdWithProcedureName(String url, String microBehaviourName) {
	String netLogoName = microBehaviourName == null ? generateGUIDString() : ServerUtils.netLogoNameWithoutSerialNumber(microBehaviourName);
	if (netLogoName == null) {
	    netLogoName = generateGUIDString();
	}
	return nextSerialNumber(netLogoName);
    }
    
    public static String nextSerialNumber(String netLogoName) {
	NetLogoNameSerialNumber serialNumber = DataStore.begin().find(NetLogoNameSerialNumber.class, netLogoName);
	if (serialNumber == null) {
	    serialNumber = new NetLogoNameSerialNumber(netLogoName, 1);
	} else {
	    serialNumber.incrementSerialNumber();
	}
	DataStore.begin().put(serialNumber);
	return netLogoName + "." + serialNumber.getSerialNumber();
    }
    
    public static String convertURLToNetLogoName(String url, String behaviourName) {
	if (url == null) {
	    // just in case
	    return null;
	}
	if (CommonUtils.getBaseURL(url) == null) {
	    return url; // already converted
	}
	String changesGuid = CommonUtils.changesGuid(url);
	String name;
	if (changesGuid != null) {
	    String netLogoNameFromGuid = CommonUtils.netLogoNameFromGuid(changesGuid, true);
	    if (netLogoNameFromGuid != null) {
		return netLogoNameFromGuid;
	    }
	    String originalURL;
	    MicroBehaviourURLCopy microBehaviourURLCopy = DataStore.begin().find(MicroBehaviourURLCopy.class, url);
	    if (microBehaviourURLCopy == null) {
		originalURL = url;
	    } else {
		originalURL = microBehaviourURLCopy.getOriginalURL();
	    }
	    MicroBehaviourNetLogoName storedName = DataStore.begin().find(MicroBehaviourNetLogoName.class, originalURL);
	    if (storedName != null) {
		return storedName.getName();
	    }
	    // this provides enough uniqueness
	    // name is made upper case to enable matching when removing micro-behaviours
	    // see equivalent-micro-behaviour?
	    name = generateNetLogoName(behaviourName);
	    DataStore.begin().put(new MicroBehaviourNetLogoName(originalURL, name));
	    return name;
	}
	// following rarely (never?) happens any more
	String fileName = CommonUtils.replaceAllNonLetterOrDigit(CommonUtils.getFileName(url), '-');
	String fileExtension = CommonUtils.replaceAllNonLetterOrDigit(CommonUtils.getFileExtension(url), '-');
	String attributes = CommonUtils.getURLAttributes(url);
	String baseURL = CommonUtils.replaceAllNonLetterOrDigit(CommonUtils.getBaseURL(url), '-');
	if (fileExtension != null) {
	    name = fileName + "." + fileExtension + "-from-" + baseURL;
	} else {
	    name = fileName + "-from-" + baseURL;
	}
	if (attributes != null) {
	    name += "-with-" + attributes;
	}
	if (!Character.isLetter(name.charAt(0))) {
	    // NetLogo names shouldn't start with a non-letter
	    name = "MB-" + name;
	}
	return name;
    }

    public static String generateNetLogoName(String behaviourName) {
	String nameWithoutSerialNumber = netLogoNameWithoutSerialNumber(behaviourName);
	// preface all generated names with '-' they appear at the top of the list when the 'procedures' tab is selected
	// but tokenizer needs updating for that to work
	if (nameWithoutSerialNumber.isEmpty()) {
	    nameWithoutSerialNumber = "NO-NAME-GIVEN";
	}
	return "-" + nameWithoutSerialNumber + "-" + MicroBehaviour.getNextSerialNumber(nameWithoutSerialNumber);
    }

    public static String netLogoNameWithoutSerialNumber(String behaviourName) {
	return CommonUtils.replaceAllNonLetterOrDigit(CommonUtils.removeHTMLMarkup(behaviourName.trim()).toUpperCase() , '-');
    }
    
    public static String urlFromNetLogoName(String netLogoName) {
	MicroBehaviourNetLogoName microBehaviourNetLogoName = 
		DataStore.begin().query(MicroBehaviourNetLogoName.class).filter("netNogoName", netLogoName).get();
	if (microBehaviourNetLogoName == null) {
	    return null;
	} else {
	    return microBehaviourNetLogoName.getUrl();
	}
    }
    
    public static int extraNameIndex(String fullBehaviourName) {
	int extraNameIndex = fullBehaviourName.indexOf("-page-of-");
	if (extraNameIndex < 0) {
	    extraNameIndex = fullBehaviourName.indexOf("-from-");
	} else {
	    // get to beginning of -in-...-page-of-...
	    extraNameIndex = fullBehaviourName.indexOf("-in-");
	}
	return extraNameIndex;
    }
    
    public static String fileToString(String fileName, boolean reportFileNotFound) {
	try {
	    BufferedReader in = new BufferedReader(new FileReader(fileName));
	    StringBuilder contents = new StringBuilder();
	    String line;
	    while ((line = in.readLine()) != null) {
		contents.append(line + "\r");
	    }
	    in.close();
	    return contents.toString();
	} catch (FileNotFoundException e) {
	    if (reportFileNotFound) {
		return logException(e, "File not found in fileToString. " + fileName);
	    }
	    return null;
	} catch (Exception e) {	    
	    return logException(e, "In fileToString ");
	}
    }
    
    public static String urlToString(String urlString, ClientState clientState, boolean reportURLNotFound) {
	// no longer supporting this kind of debugging
//	if (clientState.isRunningLocalHost() && urlString.startsWith("file://")) {
//	    return fileToString(urlString.substring(7), reportURLNotFound);
//	}
	try {
	    URL url = new URL(CommonUtils.ignoreReleaseVersionNumber(urlString));
	    URLConnection connection = url.openConnection();
	    // following fixes Error 403 from HTTP
	    connection.setRequestProperty("User-Agent", clientState.getAgentDescription());
	    connection.setConnectTimeout(ServerUtils.URL_FETCH_TIMEOUT);
	    connection.setReadTimeout(ServerUtils.URL_FETCH_TIMEOUT);
	    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    StringBuilder contents = new StringBuilder();
	    String line;
	    while ((line = in.readLine()) != null) {
		contents.append(line + "\r");
	    }
	    in.close();
	    return contents.toString();
	} catch (FileNotFoundException e) {
	    if (reportURLNotFound) {
		return logException(e, "URL not found in urlToString. " + urlString);
	    }
	    return null;
	} catch (Exception e) {	    
	    return logException(e, "In urlToString ");
	}
    }
    
    public static byte[] urlToBytes(String urlString) {
	// based upon code snippet in
	// http://stackoverflow.com/questions/637100/java-reading-a-pdf-file-from-url-into-byte-array-bytebuffer-in-an-applet
	try {
	    URLConnection connection = urlToConnection(urlString);
	    // Now that the InputStream is open, get the content length
	    int contentLength = connection.getContentLength();
	    // To avoid having to resize the array over and over and over as
	    // bytes are written to the array, provide an accurate estimate of
	    // the ultimate size of the byte array
	    InputStream in = connection.getInputStream();
	    ByteArrayOutputStream tmpOut;
	    if (contentLength != -1) {
		tmpOut = new ByteArrayOutputStream(contentLength);
	    } else {
		tmpOut = new ByteArrayOutputStream(16384); // Pick some appropriate size
	    }
	    copyBytes(in, tmpOut);
	    in.close();
	    tmpOut.close(); // No effect, but good to do anyway to keep the metaphor alive
	    return tmpOut.toByteArray();
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}	
    }
    
    public static String logError(String description) {
	return logError(description, null);
    }
    
    public static InputStream urlToInputStream(String urlString) {
	URLConnection connection = urlToConnection(urlString);
	if (connection == null) {
	    return null;
	} else {
	    try {
		return connection.getInputStream();
	    } catch (Exception e) {
		e.printStackTrace();
		return null;
	    }	
	}
    }
    
    public static URLConnection urlToConnection(String urlString) {
	try {
	    URL url = new URL(CommonUtils.ignoreReleaseVersionNumber(urlString));
	    URLConnection connection = url.openConnection();
	    connection.setConnectTimeout(ServerUtils.URL_FETCH_TIMEOUT);
	    connection.setReadTimeout(ServerUtils.URL_FETCH_TIMEOUT);
	    // Since you get a URLConnection, use it to get the InputStream
	    return connection;
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}	
    }
    
    public static String logError(String description, String sessionGuid) {
	return logError(description, sessionGuid, "");
    }
    
    public static String logError(String message, String sessionGuid, String userGuid) {
	message = CommonUtils.removeHTMLMarkup(message);
	Level level = message.startsWith("Warning") ? Level.WARNING : Level.SEVERE;
	if (sessionGuid != null && !sessionGuid.isEmpty()) {
	    message += " sessionGuid=" + sessionGuid;
	}
	if (userGuid != null && !userGuid.isEmpty()) {
	    message += " userGuid=" + userGuid;
	}
	Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).log(level, message);
	return message;
    }

    public static String fetchPageFromEncodedOrRelativeURL(String probableURL, ClientState clientState, boolean reportURLNotFound) {
	// decode in case using %20 and the like
	try {
	    String decodedModelGuid = URLDecoder.decode(probableURL, "UTF-8");
	    String fullURL = CommonUtils.completeURL(decodedModelGuid);
	    return urlToString(fullURL, clientState, reportURLNotFound);
	} catch (UnsupportedEncodingException e) {
	    ServerUtils.logException(e, "In fetchPageFromEncodedOrRelativeURL");
	    return null;
	}
    }
    
    public static String getClientInfo(HttpServletRequest httpServletRequest) {
	StringBuffer info = new StringBuffer();
	for (Enumeration<String> e = httpServletRequest.getHeaderNames(); e.hasMoreElements();) {
	    String headerName = e.nextElement();
	    // double colon to make it easier to process since typically contains URLs with single colons
	    info.append(headerName + ":: " + httpServletRequest.getHeader(headerName) + " ");
	 }
	return info.toString();
    }
    
    public static String getNameFromElement(Element element) {
	String name = getCDATAElementString("name", element);
	if (name == null) {
	    // could be older convention
	    String encodedName = element.getAttribute("name");
	    if (encodedName != null) {
		name = CommonUtils.decode(encodedName);
	    } else {
		Modeller.addToErrorLog("Expected macro-behaviour to have a name attribute: " + element.toString());
	    }
	}
	return name;
    }
    
    public static String getCDATAElementString(String tagName, Element element) {
	// could be more careful and check that there is a CDATA element as first child...
	// note this is using the XML Element not the DOM one
	// TODO: determine if this should only look for children rather than 
	// the entire tree the way getElementsByTagName does
	NodeList nameElements = element.getElementsByTagName(tagName);
	if (nameElements.getLength() > 0) {
	    Node nameElement = nameElements.item(0);
	    Node nameAsCDATAElement = nameElement.getFirstChild();
	    return nameAsCDATAElement.getNodeValue();		
	}
	// sometimes called just to check if there is such an element so no need to warn here
	return null;
    }

    public static String textAreaPlaceHolder(int i) {
	// adds 1000 so that 1 doesn't match 10 (1001 won't match 1010).
	return WILL_BE_SUBSTITUTED_FOR_CONTENTS_OF_TEXTAREA + (1000 + i);
    }
    
    public static String macroBehaviourPlaceHolder(String name) {
	return WILL_BE_SUBSTITUTED_FOR_MICRO_BEHAVIOURS + name;
    }
    
    public static Integer parseInt(String string, String context) {
	return parseInt(string, context, null);
    }
    
    public static Integer parseInt(String string, String context, ClientState clientState) {
	try {
	    return Integer.parseInt(string.trim());
	} catch (NumberFormatException e) {
	    e.printStackTrace();
	    String warning = "Could not interpret " + string + " as an integer. In " + context;
	    if (clientState != null) {
		clientState.warn(warning);
	    } else {
		logError(warning);
	    }
	    return 0;
	}
    }
    
    public static boolean isInteger(String s) {
	try {
	    Integer.parseInt(s.trim());
	    return true;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

    public static void persist(Object object) {
        PersistenceManager persistenceManager = JDO.getPersistenceManager();
        try {
            persistenceManager.makePersistent(object);
    	} catch (Exception exception) {
	    exception.printStackTrace();
        } finally {
            persistenceManager.close();
        }
    }
    
    public static <T extends Object> T getObjectById(Class<T> c, Object key) {
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	try {
	    return persistenceManager.getObjectById(c, key);
	} catch (JDOObjectNotFoundException e) {
	    return null;
	} finally {
	    persistenceManager.close();
	}
    }
    
    public static <T extends Object> List<T> getObjectsById(Class<T> c, List<?> keys) {
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	ArrayList<T> objects = new ArrayList<T>();
	try {
	    for (Object key : keys) {
		objects.add(persistenceManager.getObjectById(c, key));
	    }
	} catch (JDOObjectNotFoundException e) {
	    return null;
	} finally {
	    persistenceManager.close();
	}
	return objects;
    }

    public static String getDescription(String url) {
        PersistenceManager persistenceManager = JDO.getPersistenceManager();
        Query query = persistenceManager.newQuery(MicroBehaviourData.class);
        query.setFilter("url == urlParam");
        query.declareParameters("String urlParam");
        query.setUnique(true);
        MicroBehaviourData data = (MicroBehaviourData) query.execute(url);
        if (data != null) {
            return data.getBehaviourDescriptionHTML();
        } else {
            return null;
        }
    }

    public static byte[] getResourceBytes(Class<?> c, String name) throws IOException {
        InputStream input = c.getResourceAsStream(name);
        if (input == null) {
            return null;
        }
        return streamToBytes(input);
    }

    /**
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] streamToBytes(InputStream input) throws IOException {
	int available = input.available();
        ByteArrayOutputStream output = new ByteArrayOutputStream(available);
        copyBytes(input, output);
        input.close();
        return output.toByteArray();
    }
    
    public static boolean copyResourceBytes(Class<?> c, String name, OutputStream outputStream) throws IOException {
        InputStream input = c.getResourceAsStream(name);
        if (input == null) {
            return false;
        }
        copyBytes(input, outputStream);
        input.close();
        return true;
    }

    public static void copyBytes(InputStream input, OutputStream output)
	    throws IOException {
	byte[] buffer = new byte[512];
        while (true) {
            int len = input.read(buffer);
            if (len == -1) {
        	break;
            }
            output.write(buffer, 0, len);
        }
    }
    
    /**
     * @return Google App Engine cache
     */
    public static Cache getCache() {
	try {
	    return CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
	} catch (CacheException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public static InputStream getInputStreamFromResourceJar(String name, Class<?> c, String resourceArchiveFileName) {
        try {
            // could use JarFile instead but currently no advantage and is clumsier to 
            // create and update
            URL resource = c.getResource(resourceArchiveFileName); // v-n is a hack to work around GAE deploy update problem.
            File resourceFile = new File(resource.toURI());
            // we do close the input streams from the zip file -- believed to be sufficient to prevent 
            // resource leaks
	    @SuppressWarnings("resource")
	    ZipFile jarFile = new ZipFile(resourceFile);
            ZipEntry entry = jarFile.getEntry(name);
            if (entry != null) {
        	return jarFile.getInputStream(entry);
            } else {
        	return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getStringFromResourceJar(String name, Class<?> c, String resourceArchiveFileName) {
	InputStream inputStream = getInputStreamFromResourceJar(name, c, resourceArchiveFileName);
	try {
	    return ServerUtils.inputStreamToString(inputStream);
	} finally {
	    try {
		inputStream.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    public static String getResourceContents(Class<?> c, String name) throws IOException {
        InputStream inputStream = c.getResourceAsStream(name);
        try {
            return ServerUtils.inputStreamToString(inputStream);
	} finally {
	    try {
		inputStream.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String inputStreamToString(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            int available = inputStream.available();
            StringBuffer output = new StringBuffer(available);
            byte[] buffer = new byte[512];
            while (true) {
        	int len = inputStream.read(buffer);
        	if (len == -1) {
        	    break;
        	}
        	output.append(new String(buffer));
            }
            inputStream.close();
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final Text NULL_TEXT = new Text("");

    public static String reportDeadlineExceededError(DeadlineExceededException e) {
	e.printStackTrace();
        return "Error. Server timed out. " + e.getMessage() + 
               " Refresh and try again. Please report any persistent problems.";
    }
    
    public static void findAllKindsOfVariables(String code, VariableCollector variableCollector) {
	findVariables(code, variableCollector, true, true, true, true); 
    }
    
    public static void findBreedVariables(String code, VariableCollector variableCollector) {
	findVariables(code, variableCollector, true, false, false, false);
    }
    
    public static void findGlobalVariables(String code, VariableCollector variableCollector) {
	findVariables(code, variableCollector, false, true, false, false); 
    }
    
    public static void findPatchVariables(String code, VariableCollector variableCollector) {
	findVariables(code, variableCollector, false, false, true, false); 
    }
    
    public static void findLinkVariables(String code, VariableCollector variableCollector) {
	findVariables(code, variableCollector, false, false, false, true);
    }

    private static void findVariables(
	    String code, VariableCollector variableCollector, boolean breed, boolean global, boolean patch, boolean link) {
	if (code == null) {
	    return;
	}
	NetLogoTokenizer tokenizer = new NetLogoTokenizer(code);
	String token = tokenizer.nextToken();
	String previousToken = null;
	if (token == null) {
	    return;
	}
	if (global && token.equals("create-chooser")) {
	    // special case since for historical reasons it quotes the variable name
	    token = tokenizer.nextToken();
	    String variableName = token.replaceAll("\"", ""); // remove quotes
	    variableCollector.addExtraGlobalVariable(variableName, true, true);
	    return;
	}
	boolean defineParameter = token.equals("define-parameter");
	while (token != null) {
	    boolean writing = previousToken != null && previousToken.equals("set");
	    if (breed && token.startsWith("my-")) {
		if (!notAttributes.contains(token)) {
		    variableCollector.addBreedVariable(token, writing);
		}		
	    } else if (global && token.startsWith("the-")) {
		if (token.equals("the-other")) {
		    return;
		}
		variableCollector.addExtraGlobalVariable(token, true, writing || defineParameter);
	    } else if (patch && token.endsWith("-of-patch")) {
		variableCollector.addPatchOrLinkVariable(token, true, writing);
	    } else if (link && token.endsWith("-of-link")) {
		variableCollector.addPatchOrLinkVariable(token, false, writing);		
	    }
	    previousToken = token;
	    token = tokenizer.nextToken();
	}
    }
    
//  public static void findAllKindsOfVariables(String code, VariableCollector variableCollector) {
//  ServerUtils.findVariables(code, true, variableCollector); // breed variables
//  ServerUtils.findVariables(code, false, variableCollector); // globals too
//  ServerUtils.findPatchOrLinkVariables(code, true, variableCollector); // patch
//  ServerUtils.findPatchOrLinkVariables(code, false, variableCollector); // link
//}

//    public static void findVariables(String code, boolean breed, VariableCollector variableCollector) {
//        String searchPrefix;
//        String prefix;
//        // only consider prefixes preceded by white space or [ or (
//        if (breed) {
//            searchPrefix = "\\[my-|\\(my-|\\smy-";
//            prefix = "my-";
//        } else { // global variable
//            searchPrefix = "\\[the-|\\(the-|\\sthe-";
//            prefix = "the-";
//        }
//        ArrayList<String> lines = CommonUtils.splitIntoNonEmptyLinesWithoutNetLogoComments(code);
//        for (String line : lines) {
//            String parts[] = line.split(searchPrefix);
//            for (int i = 1; i < parts.length; i++) { 
//        	int index = 0;
//        	for (; index < parts[i].length(); index++) {
//        	    char c = parts[i].charAt(index);
//        	    if (ServerUtils.breakCharacter(c)) {
//        		break;
//        	    }
//        	}
//        	if (index > 0) {
//        	    String variableName = parts[i].substring(0, index);
//        	    boolean writing = parts[i-1].toLowerCase().endsWith("set");
//        	    String fullVariableName = prefix + variableName;
//        	    if (breed) {
//        		variableCollector.addBreedVariable(fullVariableName, writing);
//        	    } else {
//        		variableCollector.addExpectedGlobalVariable(fullVariableName, writing);
//        		if (i > 0 && parts[i-1].equals("set")) {
//        		    variableCollector.addExtraGlobalVariable(fullVariableName, true, writing);
//        		}
//        	    }
//        	}
//        	// else it is prefix followed by whitespace
//            }
//        }
//    }

//    public static void findPatchOrLinkVariables(String code, boolean patch, VariableCollector variableCollector) {
//        // patch variables now end with -of-patch
//        String postfix = patch ? "-of-patch" : "-of-link";
//        ArrayList<String> lines = CommonUtils.splitIntoNonEmptyLinesWithoutNetLogoComments(code);
//        for (int i = 0; i < lines.size(); i++) {
//            String line = lines.get(i);
//            String parts[] = line.split(postfix); 
//            if (parts.length == 1 && parts[0] != line) {
//        	// postfix ends the line
//        	int spaceIndex = line.lastIndexOf(' ');
//        	if (spaceIndex >= 0) {
//        	    String variableName = line.substring(spaceIndex + 1);
//        	    boolean writing = line.substring(spaceIndex).toLowerCase().endsWith("set");
//        	    variableCollector.addPatchOrLinkVariable(variableName, patch, writing);
//        	} else { // variable is only thing on the line
//        	    boolean writing = i > 0 && lines.get(i-1).toLowerCase().endsWith("set");
//        	    variableCollector.addPatchOrLinkVariable(line, patch, writing);
//        	}
//            } else {
//        	for (int j = 0; j < parts.length - 1; j++) { 
//        	    // skip last one since it didn't end with -of-patch
//        	    int index = parts[j].length() - 1;
//        	    for (; index >= 0; index--) {
//        		char c = parts[j].charAt(index);
//        		if (ServerUtils.breakCharacter(c)) {
//        		    break;
//        		}
//        	    }
//        	    if (index < parts[j].length() - 1) {
//        		String variableName = parts[j].substring(index + 1);
//        		boolean writing = j > 0 && parts[j-1].toLowerCase().endsWith("set");
//        		variableCollector.addPatchOrLinkVariable(variableName + postfix, patch, writing);
//        	    }
//        	}
//            }
//        }
//    }

    public static boolean breakCharacter(char c) {
        // NetLogo variables cannot contain white space and can contain punctuation except the following 
        return Character.isWhitespace(c) || c == ']' || c == '[' || c == ')' || c == '(' || c == ',' || c == '"' || c == '\n';
    }

    public static final String STATIC_RESOURCES_ZIP = "static_resources_v174.zip";

    // 60 seconds is the maximum GAE allows 
    // but odd things are happening (fast time outs) so used 10 seconds instead of the old limit of 30
    // experimenting with 30 seconds again 
    // TODO: determine if 60 seconds would be even better
    // according to https://developers.google.com/appengine/docs/java/urlfetch/overview
    //  When using the URLConnection interface, the service uses the connection timeout 
    // (setConnectTimeout()) plus the read timeout (setReadTimeout()) as the deadline.
    // so apparently the sum is 60 seconds
    public static final int URL_FETCH_TIMEOUT = 30000;

    public static boolean extractValue(String token, String nextToken, String body, int[] startEnd, ClientState clientState) {
        int tokenStart = body.indexOf(token, startEnd[0]);
        if (tokenStart < 0) {
            if (clientState != null) {
        	clientState.warn("Expected '" + token + "' in " + body);
            }
            return false;
        }
        startEnd[0] = tokenStart + token.length();
        char nextCharacter = body.charAt(startEnd[0]);
        if (nextCharacter == '\n') {
            startEnd[0]++;
        }
        startEnd[1] = body.indexOf(nextToken, startEnd[0]);
        if (startEnd[1] < 0) {
            startEnd[1] = body.length();
        }
        return true;
    }
    
    public static boolean runningLocalHost(ServletRequest servletRequest) {
	if (servletRequest == null) {
	    // not clear how this can be null but seen while debugging (in development environment)
	    return false;
	}
	String serverName = servletRequest.getServerName();
	// local machine or local network
	return serverName.equals("127.0.0.1") || serverName.startsWith("192.168.0") || serverName.startsWith("localhost");
    }
       
    /**
     * @param servletRequest
     * @return the RemoteAPI associated with the current thread
     */
    public static RemoteAPI getFreeRemoteAPI(ServletRequest servletRequest) {
	if (!runningLocalHost(servletRequest)) { // not running local host
	    return null;
	}
	try {
	    RemoteAPI remoteAPI = remoteAPIThreadLocal.get();
	    if (remoteAPI == null) {
		remoteAPI = new RemoteAPI();
		remoteAPIThreadLocal.set(remoteAPI);
	    }
	    if (remoteAPI.isRemoteAPIInUse()) {
		// already remote
		return null;
	    } else {
		return remoteAPI;
	    }
	} catch (IOException ioException) {
	    System.err.println("Exception while creating connection to remote API.");
	    ioException.printStackTrace();
	    return null;
	}
    }
    
    public static String hashSHA256(String serialisation) {
	try {
	    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
	    messageDigest.update(serialisation.getBytes());
	    byte[] digest = messageDigest.digest();
	    return new String(digest);
	} catch (NoSuchAlgorithmException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public static String createURLCopy(String url, String guid) {
	if (url == null) {
	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe("createURLCopy called with null URL.");
	    return null;
	}
        String newURL = CommonUtils.addAttributeOrHashAttributeToURL(url, "changes", guid);
        MicroBehaviourURLCopy microBehaviourURLCopy = DataStore.begin().find(MicroBehaviourURLCopy.class, url);
        String originalURL = microBehaviourURLCopy == null ? url : microBehaviourURLCopy.getOriginalURL();
        DataStore.begin().put(new MicroBehaviourURLCopy(newURL, originalURL));
        return newURL;
    }

    public static String documentToString(Document document) {
	// from http://stackoverflow.com/questions/2567416/document-to-string
	try {
	    StringWriter sw = new StringWriter();
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.transform(new DOMSource(document), new StreamResult(sw));
	    return sw.toString();
	} catch (Exception ex) {
	    throw new RuntimeException("Error converting to String", ex);
	}
    }
    
    public static String channelToken(String guid) {
	ChannelService channelService = ChannelServiceFactory.getChannelService();
	// lasts one day (minus one minute) -- maximum
	return channelService.createChannel(guid, 24*60-1);
    }
    
    /**
     * @param experiments
     * @param modelString
     * @return the modelString with the BehaviorSpace experiments inserted in the NetLogo location
     */
    public static String insertExperiments(String experiments, String modelString) {
	// from:
        // @#$#@#$#@
        // NetLogo 5.2.0
        // @#$#@#$#@
	// to
	// @#$#@#$#@
	// NetLogo <version number>
	// @#$#@#$#@
	// @#$#@#$#@
	// @#$#@#$#@
	// <experiments />
	// @#$#@#$#@
	// @#$#@#$#@
	int versionIndex = modelString.indexOf("@#$#@#$#@");
	if (versionIndex >= 0) {
	    // without the 5 in the following it ends up inserting in the wrong place
	    versionIndex = modelString.indexOf("NetLogo 5", versionIndex);
	}
	if (versionIndex >= 0) {
	    int insertionIndex = modelString.indexOf("\r", versionIndex);
	    if (insertionIndex < 0) {
		insertionIndex = modelString.indexOf("\n", versionIndex);
	    }
	    return modelString.substring(0, insertionIndex+1) 
		    +  "@#$#@#$#@\n@#$#@#$#@\n@#$#@#$#@\n" 
		    + experiments + "\n@#$#@#$#@\n" 
		    + modelString.substring(insertionIndex+1);
	} else {
	    // warn??
	    return modelString;
	}
    }
    
    public static String handleApiProxyException(ApiProxyException e, boolean sentQuotaExceededMessage) {
	if (e instanceof OverQuotaException) {
	    if (!sentQuotaExceededMessage) {
		XMPPService xmppService = XMPPServiceFactory.getXMPPService();
		JID fromJID = new JID("Modelling4All@m4a-gae.appspotchat.com");
		JID[] jids = new JID[2];
		jids[0] = new JID("toontalk@gmail.com");
		jids[1] = new JID("howard.noble@gmail.com");
		String messageBody = "Modelling4All GAE quota exceeded! " + e.getMessage() 
			             + ". To change the quota and more information visit https://appengine.google.com/billing/billing_status?&app_id=s~m4a-gae-hrd&version_id=552.367989689017953474";
		Message message = 
			new MessageBuilder().withRecipientJids(jids).withBody(messageBody).withFromJid(fromJID).build();   
		xmppService.sendMessage(message);
	    }
	    return "Error. Server quota exceeded. Sorry but your work isn't being saved and you can't load previous work. System administrators have been notified. Daily quotas are replenished daily at midnight US Pacific time. Please try again later.";
	} else {
	    return "Error. Server signaled an error. Sorry. Try again soon. Error description: " + e.getMessage();
	}
    }
    
}

//    public static void rememberMicroBehaviour(
//	    MicroBehaviour microBehaviour,
//	    String url, 
//	    String sessionGuid) {
//	Cache cache = getCache();
//	String urlAndSessionGuid[] = {url, sessionGuid};
//	cache.put(urlAndSessionGuid, microBehaviour);
//    }
//    
//    public static MicroBehaviour getMicroBehaviour(
//	    String url, 
//	    String sessionGuid) {
//	Cache cache = getCache();
//	String urlAndSessionGuid[] = {url, sessionGuid};
//	return (MicroBehaviour) cache.get(urlAndSessionGuid);
//    }
    
     
//    public static int getUserId(String sessionGuid) {
//	// returns the ID of the user who created this session
//	// not currently called -- note several users can contribute to a session
//	try {
//	    Connection database = DatabaseConnection.connectToBehaviourComposerDataBase();
//	    if (querySessionForUser  == null) {
//		String querySessionForUserSQL = "SELECT user_id FROM session WHERE read_write_guid = ?";
//		querySessionForUser = database.prepareStatement(querySessionForUserSQL);
//	    }
//	    querySessionForUser.setString(1, sessionGuid);
//	    if (querySessionForUser.execute()) {
//		ResultSet results = querySessionForUser.getResultSet();
//		if (results.next()) {
//		    return results.getInt(1);
//		}
//	    }
//	    return -1;
//	} catch (Exception e) {
//	    ServerUtils.logException(e, "In getUserId ", sessionGuid);
//	    return -1;
//	}
//    }

