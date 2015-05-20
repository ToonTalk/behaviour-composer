/**
 * 
 */
package uk.ac.lkl.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.NotFoundException;

import uk.ac.lkl.server.persistent.DataStore;
import uk.ac.lkl.server.persistent.EditedPage;
import uk.ac.lkl.server.persistent.HTMLModelApplet;
import uk.ac.lkl.server.persistent.HTMLModelTemplate;
import uk.ac.lkl.server.persistent.ModelNetLogo;
import uk.ac.lkl.server.persistent.ModelXML;
import uk.ac.lkl.server.persistent.ModelDifferences;
import uk.ac.lkl.server.persistent.UsageStatistics;
import uk.ac.lkl.shared.CommonUtils;

/**
 * Serves static pages from the JDO store
 * 
 * 
 * @author Ken Kahn
 *
 */

public class StaticPageServlet extends HttpServlet {
    
    private static final long serialVersionUID = 3075405196542994862L;
    private static final String HTML_CONTENT_TYPE = "text/html; charset=utf-8";
    private static final String CSS_CONTENT_TYPE = "text/css; charset=utf-8";
    private static final String PLAIN_CONTENT_TYPE = "text/plain; charset=utf-8";
    private static final String XML_CONTENT_TYPE = "text/xml; charset=utf-8";
    private static final String JAR_CONTENT_TYPE = "application/java-archive";
    private static final String GZ_CONTENT_TYPE = "application/x-gzip";
    private static final String CLASS_CONTENT_TYPE = "application/java-class";
      
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, java.io.IOException {
	// following may try again (but only once due to use firstTime flag)
	respondToGet(request, response, true);
    }
    
    protected void respondToGet(HttpServletRequest request, HttpServletResponse response, boolean firstTime) 
	    throws IOException {
	String pathInfo = request.getPathInfo();
	if (pathInfo == null) {
	    // seen in logs due to "GET /p"
	    return;
	}
	String guid = CommonUtils.getFileName(pathInfo);
	String extension = CommonUtils.getFileExtension(pathInfo);
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	String name = pathInfo.substring(1);
	if (name.startsWith("GUI_Strings") || name.startsWith("Errors")) {
	    // WORKING AROUND A BUG IN NETLOGOLITE.JAR 5.0
	    // See https://github.com/NetLogo/NetLogo/issues/87
	    return;
	}
	// following needed (at least) for NLOGO files that contain non-ASCII text -- e.g. French characters
	response.setCharacterEncoding("UTF-8");
//	if (name.equals("url/url.jar")) {
////	    response.sendRedirect("'127.0.0.1:8888/netlogo/url/url.jar");
//	    // same origin policy prevents redirecting above (to a different port)
//	    ServletOutputStream outputStream = response.getOutputStream();
//	    try {
//		response.setContentType(JAR_CONTENT_TYPE);
//		// need to use resources since fetching a URL over 1MB isn't supported by 
//		// Google App Engine
//		ServerUtils.copyResourceBytes(getClass(), name, outputStream);
//	    } catch (Exception e) {
//		e.printStackTrace();
//	    } finally {
//		outputStream.close();
//		persistenceManager.close();
//	    }
//	    return;
//	}
	// moved NetlogoLite.jar to m/netlogo
//	if (extension.equals("jar")) {
////	    response.sendRedirect("'http://modelling4all.nsms.ox.ac.uk/User/applets.4.1.b2/" + name);
//	    // same origin policy prevents redirecting above (to a different port)
//	    ServletOutputStream outputStream = response.getOutputStream();
//	    try {
//		response.setContentType(JAR_CONTENT_TYPE);
//		// need to use resources since fetching a URL over 1MB isn't supported by 
//		// Google App Engine
//		ServerUtils.copyResourceBytes(getClass(), name, outputStream);
//	    } catch (Exception e) {
//		e.printStackTrace();
//	    } finally {
//		outputStream.close();
//		persistenceManager.close();
//	    }
//	    return;
//	}
	PrintWriter out = null;
	boolean regeneratable = false;
	try {
//	    if (name.startsWith("en/")) {
//		try {
//		    InputStream inputStream = 
//			ServerUtils.getInputStreamFromResourceJar(name.substring(3), getClass());
//		    out = response.getWriter();
//		    if (inputStream != null) {
//			byte[] bytes = ServerUtils.streamToBytes(inputStream);
//			if (bytes != null) {
//			    String contents = new String(bytes, Charset.forName("UTF-8"));
//			    if (extension.equals("html")) {
//				response.setContentType(HTML_CONTENT_TYPE);
//			    } else {
//				response.setContentType(PLAIN_CONTENT_TYPE);
//			    }
//			    out.print(contents);
//			}
//		    } else {
//			String message = "<p>File not found:  p/" + name + "</p>";
//			writeHTMLPage(message, "Unrecognised URL", response, out);
//		    }
//		} catch (Exception e) {
//		    e.printStackTrace();
//		}
	    // according to http://stackoverflow.com/questions/6520231/how-to-force-browser-to-download-file
	    // this needs be before writing the content of the response
	    setContentType(response, extension);
	    if (pathInfo.equals("/bc2netlogo.txt")) {
		String queryString = request.getQueryString();
		String userGuid = CommonUtils.getURLParameter("user", queryString);
		if ("new".equals(userGuid)) {
		    userGuid = null;
		}
		String sessionGuid = CommonUtils.getURLParameter("session", queryString);
		boolean epidemicGameMaker = CommonUtils.getURLParameter("EGM", queryString) != null;
		if ("new".equals(sessionGuid)) {
		    sessionGuid = null;
		}
		String modelGuid = CommonUtils.getURLParameter("frozen", queryString);
		if (userGuid == null || sessionGuid == null) {
		    HistoryServiceImpl historyServiceImpl = new HistoryServiceImpl();
		    String[] startEvent = 
			    historyServiceImpl.startEvent(userGuid, sessionGuid, null, null, modelGuid, null, false, "http://m.modelling4all.org/m/", false, true);
		    sessionGuid = startEvent[0];
		    userGuid = startEvent[2];
		}
		String channelTokenToNetLogo;
		String channelTokenFromNetLogo;
		String domain = CommonUtils.getURLParameter("domain", queryString);
		if (domain == null) {
		    domain = "http://m.modelling4all.org";
		}
		String newURL = domain + "/m/index.html?user=" + userGuid +
		                "&share=" + sessionGuid;
		if (epidemicGameMaker) {
		    newURL += "&EGM=1";
		}
		String reconnectingFromBC = CommonUtils.getURLParameter("reconnectingFromBC", queryString);
		String reconnectingFromNetLogo = CommonUtils.getURLParameter("reconnectingFromNetLogo", queryString);
		if (reconnectingFromBC == null) {
		    channelTokenToNetLogo = ServerUtils.channelToken(sessionGuid);
//		    System.out.println("channel token for " + sessionGuid + " is " + channelTokenToNetLogo); // debug this
		    newURL += "&bc2NetLogoChannelToken=" + channelTokenToNetLogo;
		}
		if (reconnectingFromNetLogo == null) {
		    channelTokenFromNetLogo = ServerUtils.channelToken(sessionGuid+userGuid);
//		    System.out.println("channel token for " + (sessionGuid+userGuid) + " is " + channelTokenFromNetLogo); // debug this
		    newURL += "&netLogo2BCChannelToken=" + channelTokenFromNetLogo;
		}
//		if (reconnectingFromBC != null || reconnectingFromNetLogo != null) {
//		    String key = sessionGuid+userGuid;
//		    String reconnectionCounter = reconnectingFromBC == null ? reconnectingFromNetLogo : reconnectingFromBC;
//		    BC2NetLogoChannels channels = DataStore.begin().find(BC2NetLogoChannels.class, key);
//		    if (channels == null || !reconnectionCounter.equals(channels.getReconnectionCounter())) {
//			// first time or out-of-date
//			channelTokenToNetLogo = ServerUtils.channelToken(sessionGuid);
//			channelTokenFromNetLogo = ServerUtils.channelToken(key);
//			if (channels == null) {
//			    channels = new BC2NetLogoChannels(key, channelTokenToNetLogo, channelTokenFromNetLogo, reconnectionCounter);	    
//			} else {
//			    channels.setChannelFromNetLogo(channelTokenFromNetLogo);
//			    channels.setChannelToNetLogo(channelTokenToNetLogo);
//			    channels.setReconnectionCounter(reconnectionCounter);
//			}
//			DataStore.begin().put(channels);
//		    } else {
//			channelTokenToNetLogo = channels.getChannelToNetLogo();
//			channelTokenFromNetLogo = channels.getChannelFromNetLogo();
//		    }
//		} else {
//		    channelTokenToNetLogo = ServerUtils.channelToken(sessionGuid);
//		    channelTokenFromNetLogo = ServerUtils.channelToken(sessionGuid+userGuid);
//		}
		// in the normal course of things if modelGuid != null then sessionGuid was just created above
//		if (modelGuid != null) {
//		    sessionGuid = "new"; // need new session if model is specified
//		}
		// following probably caused Issue 945
//		if (modelGuid != null) {
//		    newURL += "&frozen=" + modelGuid;
//		}
//		if (reconnectingFromBC != null && !reconnectingFromBC.equals("null")) {
//		    newURL += "&reconstructedChannelNumber=" + reconnectingFromBC;
//		}
		out = response.getWriter();
		out.print(newURL);
	    } else if (pathInfo.startsWith("/modelDifferences/")) {
		ModelDifferences modelDifferences = DataStore.begin().find(ModelDifferences.class, guid);
		if (modelDifferences != null) {
		    String procedureDifferences = modelDifferences.getProcedureDifferences();
		    procedureDifferences = addInformationToDifferences(procedureDifferences, request);
		    String differences = "<differences>" + procedureDifferences 
			                                 + modelDifferences.getDeclarationDifferences() 
			                                 + modelDifferences.getWidgetDifferences();
		    String infoTab = modelDifferences.getInfoTab();
		    if (infoTab != null && !infoTab.isEmpty()) {
			differences += "<infoTab>" + CommonUtils.createCDATASection(infoTab) + "</infoTab>";
		    }
		    differences += "</differences>";
		    out = response.getWriter();
		    out.print(differences);
		    // not needed anymore - remove from data store
		    DataStore.begin().delete(modelDifferences);
		}
	    } else if (pathInfo.startsWith("/modelStatistics/")) {
		UsageStatistics statistics = ModelXML.getStatistics(guid);
		String loadDescription;
		String runDescription;
		if (statistics.loadCount == 0) {
		    loadDescription = "is brand new.";
		} else if (statistics.loadCount == 1) {
		    loadDescription = "has been loaded once";
		} else {
		    loadDescription = "has been loaded " + statistics.loadCount + " times";
		}
		if (statistics.runCount == 0) {
		    runDescription = ".";
		} else if (statistics.runCount == 1) {
		    runDescription = " and run once.";
		} else {
		    runDescription = " and run " + statistics.runCount + " times.";
		}
		String message = "This model " + loadDescription + runDescription;
		out = response.getWriter();
		writeHTMLPage(message, "Usage statistics for " + guid, response, out);
	    } else if (extension.equals("xml")) {
		String contents;
		ModelXML modelXML;
		try { 
		    modelXML = ModelXML.getModelXML(guid, true);
		} catch (NotFoundException e) {
		    modelXML = null;
		}
		if (modelXML == null) {
		    contents = HistoryServiceImpl.getEvents(guid, request);
		} else {
		    contents = modelXML.getModelXML();
		}
		out = response.getWriter();
		if (contents == null) {
		    // if the wording of the following is changed then also change the error response in loadModelXML
		    String textMessage = "The server could not find a model or session whose id is " + guid + ". Sorry.";
		    String message = "<p>" + textMessage + "</p>";
		    writeHTMLPage(message, "Model or session not found", response, out);
		    ServerUtils.logError("Warning. " + textMessage);
		    extension = "html";
		} else {
		    out.print(contents);
		}
	    } else if (extension.equals("nlogo") || extension.equals("nlogo3d")) {
		regeneratable = true;
		ModelNetLogo modelNetLogo = persistenceManager.getObjectById(ModelNetLogo.class, guid);
		out = response.getWriter();
		out.print(modelNetLogo.getNetLogoFileContents());
	    } else if (guid.endsWith(".raw") && extension.equals("html")) {
		regeneratable = true;
		guid = guid.substring(0, guid.length()-4); // remove the final .raw
		HTMLModelApplet appletHTML = 
			persistenceManager.getObjectById(HTMLModelApplet.class, guid);
		out = response.getWriter();
		out.print(appletHTML.getAppletString());
	    } else if (guid.endsWith(".template") && extension.equals("html")) {
		regeneratable = true;
		guid = guid.substring(0, guid.length()-9); // remove the final .template
		HTMLModelTemplate templateHTML = 
			persistenceManager.getObjectById(HTMLModelTemplate.class, guid);
		out = response.getWriter();
		out.print(templateHTML.getTemplate());
	    } else if ((guid.endsWith(".edited") || pathInfo.endsWith(CommonUtils.EDITED_HTML)) && (extension.equals("html") || extension.equals("txt"))) {
		if (guid.endsWith(".edited")) {
		    guid = guid.substring(0, guid.length()-7); // remove the final .edited 
		}
		EditedPage editedPage = persistenceManager.getObjectById(EditedPage.class, guid);
		out = response.getWriter();
		String contents = editedPage.getContents();
		String newContents = contents; // unless updated below
		if (extension.equals("txt")) {
		    String contentsLowerCase = contents.toLowerCase();
		    if (!contents.contains("BehaviourComposer: ignore everything before this.")) {
			int bodyTokenStart = contentsLowerCase.indexOf("<body>");
			if (bodyTokenStart >= 0) {
			    int bodyTokenEnd = bodyTokenStart+"<body>".length();
			    int bodyCloseTokenStart = contentsLowerCase.indexOf("</body>", bodyTokenEnd);
			    if (bodyCloseTokenStart >= 0) {
				newContents = contents.substring(0, bodyTokenEnd) 
					+ "\n<p>BehaviourComposer: ignore everything before this.</p>\n"
					+ contents.substring(bodyTokenEnd, bodyCloseTokenStart ) 
					+ "\n<p>BehaviourComposer: ignore everything after this.</p>\n" 
					+ contents.substring(bodyCloseTokenStart);
			    }	          
			}
		    }
		    // remove blank lines
		    newContents = newContents.replace("\r\n", "\n").replaceAll("[\n]+", "\n");
		}
		out.print(newContents);
            // older applets expect NetLogo jars to be served by this servlet
	    // proxy to static GAE pages instead
	    } else if (pathInfo.equals("/netlogo/NetLogoLite.jar")) {
		InputStream inputStream = ServerUtils.urlToInputStream("http://modelling4all.org/netlogo/NetLogoLite.jar");
		ServerUtils.copyBytes(inputStream, response.getOutputStream());
	    } else if (pathInfo.equals("/netlogo/NetLogoLite.jar.pack.gz")) {
		InputStream inputStream = ServerUtils.urlToInputStream("http://modelling4all.org/netlogo/NetLogoLite.jar.pack.gz");
		ServerUtils.copyBytes(inputStream, response.getOutputStream());
	    } else if (guid.endsWith(".removeOldCachedApplets") && extension.equals("html")) {
		String maxAsString = guid.substring(0, guid.length()-".removeOldCachedApplets".length());
		int maxEntries = 1000;
		try {
		    maxEntries = Integer.parseInt(maxAsString);
		} catch (NumberFormatException e) {
		    // ignore
		}
		int numberRemoved = removeOldCachedApplets(maxEntries);
		out = response.getWriter();
		writeHTMLPage("<p>" + numberRemoved + " old cached applets removed.</p>", "Applet removal", response, out);
	    } else {
		if (!respondUsingResourceJar(response, pathInfo, name, out)) {
		    return;
		}
	    }
	} catch (JDOObjectNotFoundException e) {
	    if (firstTime && regeneratable && regenerateModel(guid)) {
		respondToGet(request, response, false);
	    } else {
		String resourceType = extension.equals("html") ? "page" : "model";
		String messageText = "The server could not find a " + resourceType + " whose id is " + guid + ". Sorry.";
		String message = "<p>" + messageText + "</p>";
		out = response.getWriter();
		writeHTMLPage(message, resourceType + " not found", response, out);
		if (!pathInfo.endsWith(".raw.html")) {
		    // raw.html is used only in the Run tab to display applets
		    // in this case the error is not seen so need to add to the system log
		    ServerUtils.logError("Warning. " + messageText);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    persistenceManager.close();
	    if (out != null) {
		out.close();
	    }
	}
    }

    private String addInformationToDifferences(String differences, HttpServletRequest request) {
	final String nameEquals = "procedureName='";
	String[] pieces = differences.split(nameEquals);
	if (pieces.length <= 1) {
	    return differences;
	}
	ResourcePageServiceImpl resourcePageServiceImpl = new ResourcePageServiceImpl();
	resourcePageServiceImpl.setRequest(request);
	ClientState clientState = new ClientState();
	String enhancedDifferences = pieces[0];
	for (int i = 1; i < pieces.length; i++) {
	    int nameEnd = pieces[i].indexOf('\'');
	    String name = pieces[i].substring(0, nameEnd);
	    String url = ServerUtils.urlFromNetLogoName(name);
	    if (url == null) {
		System.err.println("No URL found for " + name + ". If running localhost then remoteAPI could fix this.");
	    }
	    String extraInfo = "<url>" + CommonUtils.createCDATASection(url) + "</url>";
	    MicroBehaviour microBehaviour = resourcePageServiceImpl.getMicroBehaviourFromCacheOrDatabase(url, clientState);
	    if (microBehaviour != null) {
		String textAreas = "";
		ArrayList<String> textAreaElements = microBehaviour.getTextAreaElements();
		if (textAreaElements != null) {
		    for (int j = 0; j < textAreaElements.size(); j += 2) {
			String textAreaName = textAreaElements.get(j);
			String textAreaHTML = textAreaElements.get(j+1);
			textAreas += "<textArea name='" + textAreaName + "'>" + CommonUtils.createCDATASection(textAreaHTML) + "</textArea>";
		    }
		}
		if (!textAreas.isEmpty()) {
		    extraInfo += "<textAreas>" + textAreas + "</textAreas>";
		}
	    } else if (url == null) {
		System.err.println("Unable to find the URL for the NetLogo procedure: " + name + ". Can happen when micro-behaviour copy created by a different implementation of the Behaviour Composer.");
	    } else {
		System.err.println("Expected to find a micro-behaviour with the URL: " + url);
	    }
	    int endTag = pieces[i].indexOf("</procedureChanged>");
	    enhancedDifferences += nameEquals + pieces[i].substring(0, endTag) 
		                + extraInfo
		                + pieces[i].substring(endTag) ;
	}
	return enhancedDifferences;
    }

    private int removeOldCachedApplets(int maxEntries) {
	int count = 0;
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	try {
	    Query query = persistenceManager.newQuery(ModelNetLogo.class);
	    query.setOrdering("timeStamp asc");
	    @SuppressWarnings("unchecked")
	    List<ModelNetLogo> models = (List<ModelNetLogo>) query.execute();
	    // anything older than a month will be recomputed when needed
	    Date oldestKept = new Date(System.currentTimeMillis() - 31*24*60*60*1000L);
	    for (ModelNetLogo netLogo : models) {
		Date timeStamp = netLogo.getTimeStamp();
		if (timeStamp.before(oldestKept)) {
		    String uniqueID = netLogo.getUniqueID();
		    try {
			HTMLModelApplet modelApplet = persistenceManager.getObjectById(HTMLModelApplet.class, uniqueID);
			persistenceManager.deletePersistent(modelApplet);
			HTMLModelTemplate template = persistenceManager.getObjectById(HTMLModelTemplate.class, uniqueID);
			persistenceManager.deletePersistent(template);
			persistenceManager.deletePersistent(netLogo);
			System.out.println(uniqueID);
		    } catch (JDOObjectNotFoundException e) {
			// ignore
		    }
		    count++;
		    if (count >= maxEntries) {
			return count;
		    }
		}
	    }
	    // removing orphaned templates -- shouldn't be a new ones created after 30 December 2011
	    Query templateQuery = persistenceManager.newQuery(HTMLModelTemplate.class);
	    @SuppressWarnings("unchecked")
	    List<HTMLModelTemplate> templates = (List<HTMLModelTemplate>) templateQuery.execute();
	    for (HTMLModelTemplate template : templates) {
		String modelGuid = template.getModelGuid();
		try {
		    persistenceManager.getObjectById(ModelXML.class, modelGuid);
		} catch (JDOObjectNotFoundException e) {
		    try {
			persistenceManager.getObjectById(HTMLModelApplet.class, modelGuid);
		    } catch (JDOObjectNotFoundException e2) {
			// template has no model or NetLogo applet code so delete
			persistenceManager.deletePersistent(template);
			count++;
			if (count >= maxEntries) {
			    return count;
			}
		    }
		}
	    }
	} finally {
	    persistenceManager.close();
	}
	return count;
    }

    protected boolean regenerateModel(String modelGuid) {
	try {
	    ModelXML modelXML = ModelXML.getModelXML(modelGuid, true);
	    if (modelXML == null) {
		ServerUtils.logError("Warning, unable to find the model to regenerate the applet with the id " + modelGuid);
		return false;
	    }
	    String modelXMLString = modelXML.getModelXML();
	    String sessionGuid = modelXML.getSessionGuid();
	    ResourcePageServiceImpl resourcePageServiceImpl = new ResourcePageServiceImpl();
	    String userGuid = modelXML.getUserGuid();
	    if (userGuid == null) {
		userGuid = "regenerated model -- user guid not available";
	    }
	    String pageTemplate = modelXML.getPageTemplate();
	    if (pageTemplate == null) {
		pageTemplate = CommonUtils.DEFAULT_APPLET_TEMPLATE; //default
	    }
	    String hostBaseURL = "regenerate - no host base URL";
	    String[] result = 
		    resourcePageServiceImpl.reconstructModel(modelGuid, sessionGuid, userGuid, modelXMLString, pageTemplate, hostBaseURL);
	    String possibleError = result[0];
	    return !possibleError.startsWith("Error");
	} catch (NotFoundException e) {
	    ServerUtils.logError("Warning, unable to find the model to regenerate the applet with the id " + modelGuid);
	    return false;
	}
    }

    /**
     * @param response
     * @param extension
     */
    protected void setContentType(HttpServletResponse response, String extension) {
	if (extension.equalsIgnoreCase("html")) {
	    response.setContentType(HTML_CONTENT_TYPE);
	} else if (extension.equalsIgnoreCase("png") || 
		   extension.equalsIgnoreCase("jpg") || 
		   extension.equalsIgnoreCase("gif") ||
		   extension.equalsIgnoreCase("ico")) {
	    response.setContentType("image/" + extension.toLowerCase());
	} else if (extension.equalsIgnoreCase("css")) {
	    response.setContentType(CSS_CONTENT_TYPE);
	} else if (extension.equalsIgnoreCase("xml")) {
	    response.setContentType(XML_CONTENT_TYPE);
	} else if (extension.equalsIgnoreCase("class")) {
	    response.setContentType(CLASS_CONTENT_TYPE);
	} else if (extension.equalsIgnoreCase("jar")) {
	    response.setContentType(JAR_CONTENT_TYPE);
	} else if (extension.equalsIgnoreCase("gz")) {
	    response.setContentType(GZ_CONTENT_TYPE);
	} else if (extension.equalsIgnoreCase("nlogo") || extension.equalsIgnoreCase("nlogo3d")) {
	   response.setContentType("application/force-download");
	} else {
	    response.setContentType(PLAIN_CONTENT_TYPE);
	}
    }

    /**
     * @param response
     * @param pathInfo
     * @param name
     * @param out
     * @return true if resource found
     * @throws IOException
     */
    protected boolean respondUsingResourceJar(HttpServletResponse response, String pathInfo, String name, PrintWriter out) 
	    throws IOException {
	// following deals with problems like foo%20bar when 'foo bar' is in archive
	name = name.replace("%20", " ");
	InputStream input = ServerUtils.getInputStreamFromResourceJar(name, getClass(), getResourceArchiveFileName());
	if (input == null) {
	    input = getClass().getResourceAsStream(name);
	}
	if (input != null) {
	    ServerUtils.copyBytes(input, response.getOutputStream());
	    input.close();
	    return true;
	} else if (pathInfo.equals("/org.nlogo.lite.Applet")) {
	    // applet tries this if problem loading NetLogoLite.jar
	    System.out.println("Request to server for " + pathInfo + " ignored.");
	    out = response.getWriter();
	    String message = "Warning. Failed to load NetLogo applet JAR";
	    writeHTMLPage(message, "Error loading the NetLogo applet JAR. Try again.", response, out);
	    ServerUtils.logError(message);
	    return false;
	} else {
	    if (!pathInfo.endsWith("nls.pack.gz")) {
		// there is no compressed version of the nls file
		String message = "<p>Warning. Unrecognised url: " + pathInfo + "</p>";
		out = response.getWriter();
		writeHTMLPage(message, "URL could not be found on this server. ", response, out);
		ServerUtils.logError(message);
	    }
	    return false;
	}
    }
    
    protected String getResourceArchiveFileName() {
	return ServerUtils.STATIC_RESOURCES_ZIP;
    }
    
    public void writeHTMLPage(String message, String title, HttpServletResponse response, PrintWriter out) {
	response.setContentType(HTML_CONTENT_TYPE);
	out.println("<html>");
	out.println("<head><meta http-equiv='Content-Type' content='text/html;charset=utf-8' ><title>" + 
		    title + 
		    "</title></head>");
	out.println("<body>");
	out.println(message);
	out.println("</body></html>");
    }
    
//    @Override
//    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
//	
//    }

}
