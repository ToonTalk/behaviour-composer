package uk.ac.lkl.server;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.apphosting.api.DeadlineExceededException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.NotFoundException;

import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.client.rpc.ResourcePageService;
import uk.ac.lkl.server.persistent.BehaviourCode;
import uk.ac.lkl.server.persistent.DataStore;
import uk.ac.lkl.server.persistent.EGMSerialNumber;
import uk.ac.lkl.server.persistent.EditedPage;
import uk.ac.lkl.server.persistent.HTMLModelApplet;
import uk.ac.lkl.server.persistent.HTMLModelTemplate;
import uk.ac.lkl.server.persistent.MicroBehaviourCopyListsOfMicroBehaviours;
import uk.ac.lkl.server.persistent.MicroBehaviourCopyMicroBehaviours;
import uk.ac.lkl.server.persistent.MicroBehaviourData;
import uk.ac.lkl.server.persistent.MicroBehaviourNetLogoName;
import uk.ac.lkl.server.persistent.ModelNetLogo;
import uk.ac.lkl.server.persistent.ModelXML;
import uk.ac.lkl.server.persistent.URLContents;
import uk.ac.lkl.shared.CommonUtils;
import uk.ac.lkl.shared.DeltaPageResult;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.html.dom.HTMLDocumentImpl;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;
import java.io.StringReader;

import org.cyberneko.html.parsers.DOMFragmentParser;

@SuppressWarnings("serial")
public class ResourcePageServiceImpl extends RemoteServiceServlet implements ResourcePageService {

    private static final String CACHED = "CACHED:";
    public static final String RESOURCE_SERVICE_LOGGER_NAME = "Resource Service";
    // TODO: eliminate this and use sessionToMicroBehaviourURLTable
    private ConcurrentHashMap<String, MicroBehaviour> microBehaviourURLTable = 
            new ConcurrentHashMap<String, MicroBehaviour>();
    private ConcurrentHashMap<String, Long> microBehaviourURLUpdateTime = 
            new ConcurrentHashMap<String, Long>();
    private ConcurrentHashMap<String, String> behaviourNameURLTable = new ConcurrentHashMap<String, String>();
    // following used in microBehaviourURLTable to indicate micro-behaviour is being created
    // used to prevent infinite recursion
    private MicroBehaviour dummyMicroBehaviour = new MicroBehaviour();
    //    protected  ConcurrentHashMap<String, ConcurrentHashMap<String, MicroBehaviour>> sessionToMicroBehaviourURLTable = 
    //	new ConcurrentHashMap<String, ConcurrentHashMap<String, MicroBehaviour>>();
    //    private ConcurrentHashMap<String, Long> sessionToMicroBehaviourURLTableLastAccessTime = 
    //	new ConcurrentHashMap<String, Long>();

    //    private ConcurrentHashMap<String, HeadlessWorkspace> netLogoWorkspaces = 
    //	new ConcurrentHashMap<String, HeadlessWorkspace>();
    //    
    //    private ConcurrentHashMap<String, Long> netLogoWorkspacesLastAccess = 
    //	new ConcurrentHashMap<String, Long>();
    //    
    //    private ConcurrentHashMap<String, String> netLogoTracesRemaining = 
    //	new ConcurrentHashMap<String, String>();

    // if a NetLogo model is calling this and has set this
    // TODO: see if this might be concurrently accessed by different users
    private NetLogoModel netLogoModel = null;

    private Logger logger = Logger.getLogger(RESOURCE_SERVICE_LOGGER_NAME);

    //    final private int unaccessedWorkspaceLifetime = 300000; // 5 minutes in milliseconds
    final private int sessionCacheLifetime = 6000000; // 100 minutes in milliseconds
    private ServletRequest request;
    private String infoTabContents = null;
    private String hostBaseURL;

    final public static String BEGIN_PAGE_SECTION = "BehaviourComposer: ignore everything before this.";
    final public static String END_PAGE_SECTION = "BehaviourComposer: ignore everything after this.";
    final public static String REDIRECT_TOKEN = "BehaviourComposer: Redirect";
    final public static String BEGIN_MB_TOKEN = "Begin micro-behaviour";
    final public static String US_ENGLISH_BEGIN_MB_TOKEN = "Begin micro-behavior";
    final public static String BEGIN_NETLOGO_CODE_TOKEN = "Begin NetLogo code"; // : at end optional
    final public static String END_MB_TOKEN = "End NetLogo code";
    final public static List<String> sectionMarkerTokens =
            Arrays.asList(BEGIN_PAGE_SECTION, END_PAGE_SECTION, BEGIN_MB_TOKEN, US_ENGLISH_BEGIN_MB_TOKEN, BEGIN_NETLOGO_CODE_TOKEN, END_MB_TOKEN); 
    final public static String LIST_OF_MICRO_BEHAVIOURS = "list-of-micro-behaviours";
    final public static String LIST_OF_MICRO_BEHAVIOURS_US_ENGLISH = "list-of-micro-behaviors";

    public ResourcePageServiceImpl() {
        super();
    }

    /**
     * Fetches the contents of the urlString and transforms it
     * 
     * @return String[7] containing 
     *  0 fully transformed contents, 
     *  1 absolute URL contents, 
     *  2 counter at the end, 
     *  3 idPrefix, 
     *  4 new URL if saved edits
     *  5 warnings
     *  6 micro-behaviour description/name (in HTML) -- if no changes GUID
     *  7 read-only flag (only for locally hosted pages)
     */
    @Override
    public String[] fetchAndTransformPage(
            String allURLs,
            String sessionGuid, 
            String userGuid, 
            int idPrefix,
            String hostBaseURL,
            boolean cachingEnabled,
            boolean internetAccess) {
        this.hostBaseURL = hostBaseURL;
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), cachingEnabled, internetAccess);
        CommonUtils.setHostBaseURL(hostBaseURL);
        //	String[] split = allURLs.split(";", 2); // might have alternative older URLs
        //	allURLs = split[0];
        String tabUrl = CommonUtils.extractTabAttribute(allURLs);
        String urlString;
        if (tabUrl == null) {
            urlString = allURLs;
        } else {
            // if ...?tab=<url> then use the inner url
            urlString = tabUrl;
        }
        int semicolonIndex = urlString.indexOf(';');
        if (semicolonIndex > 0) {
            urlString = urlString.substring(0, semicolonIndex);
        }
        try {
            urlString = URLDecoder.decode(urlString, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            String message = "Failed to decode the url: " + urlString;
            if (!allURLs.equals(urlString)) {
                message += " original URL: " + allURLs;
            }
            logger.severe(message);
            e1.printStackTrace();
        }
        removeOldEntriesFromMicroBehaviourURLTable(System.currentTimeMillis()-sessionCacheLifetime);
        if (urlString.startsWith("http://http://")) {
            // users do this -- sloppy copy and paste
            urlString = urlString.substring(7); 
        }
        urlString = CommonUtils.cannonicaliseURL(urlString);
        String baseURL = CommonUtils.getBaseURL(urlString);
        String answer[] = new String[8];
        // why recompute the following?
        clientState = new ClientState(sessionGuid, getThreadLocalRequest(), cachingEnabled, internetAccess);
        try {
            Document document = fetchPageDocument(urlString, clientState, answer);
            if (document == null) {
                answer[5] = "Error. Unable to read the URL: " + urlString + ". You may be able to read it if you try again.";
                ServerUtils.logError("Warning reading the URL: " + urlString + " original URL: " + allURLs, sessionGuid, userGuid);
            } else {
                // about to update cache so don't use it while processing this
                // typical use case: URL itself has been updated and re-opening it
                removeMicroBehaviourCache(urlString);
                transformDocument(document, allURLs, baseURL, clientState, idPrefix, answer);
            }
        } catch (DeadlineExceededException e) {
            answer[5] = ServerUtils.reportDeadlineExceededError(e);
        } catch (Exception e) {    
            answer[5] = 
                    ServerUtils.logException(
                            e, 
                            "fetching the contents of " + urlString + " original URL: " + allURLs, 
                            sessionGuid, 
                            userGuid);
        }
        String warningsToSendBackToClient = clientState.getAndRecordWarningsToSendBackToClient();
        if (answer[5] == null) {
            answer[5] = warningsToSendBackToClient;
        } else {
            answer[5] += " " + warningsToSendBackToClient;
        }
        // make sure the cache has this version since the page may have been updated
        //	MicroBehaviour microBehaviour = getMicroBehaviourFromDatabase(urlString);
        //	if (microBehaviour != null) {
        //	    microBehaviour.createtOrUpdateMicroBehaviourData();
        //	    rememberMicroBehaviour(microBehaviour, urlString);
        //	}
        return answer;
    }

    @Override
    public String[] transformPage(
            String currentHTML, String oldURL, boolean replaceOld, String sessionGuid, String hostBaseURL, int idPrefix, boolean cachingEnabled, boolean internetAccess) {
        // editing an already loaded URL
        this.hostBaseURL = hostBaseURL;
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), cachingEnabled, internetAccess);
        CommonUtils.setHostBaseURL(hostBaseURL);
        return transformPage(currentHTML, oldURL, replaceOld, hostBaseURL, clientState, idPrefix);
    }

    protected String[] transformPage(String currentHTML, String oldURL, boolean replaceOld, String hostBaseURL, ClientState clientState, int idPrefix) {   
        this.hostBaseURL = hostBaseURL;
        String answer[] = new String[7];
        // Contents, originalContents, counter at the end, idPrefix, new URL, and warnings
        if (!CommonUtils.isErrorResponse(currentHTML)) {
            if (oldURL == null) {
                replaceOld = false;
            }
            try {
                String repairedHTML = CommonUtils.removeBRfromPREs(currentHTML);
                Document document = getDocument(repairedHTML);
                if (document != null) {
                    String guid = null;
                    if (replaceOld) {
                        answer[4] = oldURL;
                    } else {
                        guid = ServerUtils.generateGUIDString();
                        String newURL = CommonUtils.getStaticPagePath() + guid + CommonUtils.EDITED_HTML;
                        answer[4] = newURL;
                    }
                    transformDocument(document, answer[4], CommonUtils.getBaseURL(oldURL), clientState, idPrefix, answer);
                    answer[5] = clientState.getAndRecordWarningsToSendBackToClient();
                    if (!CommonUtils.isErrorResponse(answer[5])) {
                        if (replaceOld) {
                            oldURL = CommonUtils.removeBookmark(oldURL);
                            if (oldURL.endsWith(CommonUtils.EDITED_HTML)) {
                                int lastSlashIndex = oldURL.lastIndexOf("/");
                                guid = oldURL.substring(lastSlashIndex+1, oldURL.length()-CommonUtils.EDITED_HTML.length()); // remove the final .edited.html
                                EditedPage editedPage = ServerUtils.getObjectById(EditedPage.class, guid);
                                if (editedPage != null) {
                                    editedPage.setContents(currentHTML);
                                    ServerUtils.persist(editedPage);
                                } else {
                                    answer[0] = "Could not find edited page with guid: " + guid;
                                }
                            } else {
                                answer[0] = "Can only replace pages whose URL ends with " + CommonUtils.EDITED_HTML + ". URL is " + oldURL;
                            }
                        } else { // if (clientState.isCachingEnabled()) {
                            EditedPage editedPage = 
                                    new EditedPage(guid, oldURL, repairedHTML, clientState.getSessionGuid());
                            ServerUtils.persist(editedPage);
                        }
                    }
                } else {
                    answer[0] = "Error could not parse the HTML document:\n" + currentHTML;
                }
                return answer;
            } catch (Exception e) {
                answer[0] = ServerUtils.logException(e, "transformPage writing edited version of " + oldURL);
                answer[5] = clientState.getAndRecordWarningsToSendBackToClient();
                return answer;
            }
        } else {
            answer[0] = currentHTML;
            answer[2] = "0";
            answer[5] = clientState.getAndRecordWarningsToSendBackToClient();
        }
        return answer;
    }

    protected void transformDocument(Document document, String urlString, String baseURL, ClientState clientState, int idPrefix, String[] answer) {
        // idCounters[0] and idCounters[1] for element ids
        // idCounters[2] for text area indices
        int idCounters[] = {1, idPrefix, 0};
        try {
            transformNodes(document, document, urlString, baseURL, clientState, idCounters, null, 0);
            answer[1] = nodeToHTMLWithExceptions(document); // absolute version of original
            transformNodes(document, document, urlString, baseURL, clientState, idCounters, null, 1);
            answer[0] = nodeToHTMLWithExceptions(document);
            ArrayList<ArrayList<String>> listsOfMicroBehaviours = null;
            if (CommonUtils.hasChangesGuid(urlString)) {
                // if not a copy then just use the list statically specified on the source page
                listsOfMicroBehaviours = new ArrayList<ArrayList<String>>();
            }
            if (findMicroBehaviourAndAddAttributes(document, urlString, baseURL, null, clientState, idCounters, listsOfMicroBehaviours, answer)) {
                // been further transformed
                answer[0] = nodeToHTMLWithExceptions(document);
            }
            answer[0] = substituteTextAreasForNames(answer[0], urlString, clientState);
            answer[0] = substituteMacroBehaviourElements(answer[0], urlString, clientState, idCounters, listsOfMicroBehaviours);
            // otherwise leave the transformed one alone since searching for micro-behaviours may have
            // altered the document
            answer[2] = Integer.toString(idCounters[0]-1);
            answer[3] = Integer.toString(idCounters[1]);
        } catch (DeadlineExceededException e) {
            answer[0] = ServerUtils.reportDeadlineExceededError(e);
        } catch (Exception e) {
            e.printStackTrace();
            answer[0] = ServerUtils.logException(e, "Exception occured in processing " + urlString + " in transformDocument. Perhaps it uses some exotic characters that can't be encoded. ");
        }
    }

    private String substituteMacroBehaviourElements(String pageContents, 
            String urlString, 
            ClientState clientState, 
            int[] idCounters, 
            ArrayList<ArrayList<String>> listsOfMicroBehaviours) {
        MicroBehaviour microBehaviour = getMicroBehaviourFromCacheOrDatabase(urlString, clientState);		    
        if (microBehaviour == null) {
            return pageContents;
        }
        final ArrayList<MacroBehaviour> macroBehaviours = microBehaviour.getMacroBehaviours();
        if (macroBehaviours == null || macroBehaviours.isEmpty()) {
            return pageContents;
        }
        StringBuffer newPageContents = new StringBuffer(pageContents);
        for (MacroBehaviour macroBehaviour : macroBehaviours) {
            String name = macroBehaviour.getObjectName();
            int tokenStart = newPageContents.indexOf(LIST_OF_MICRO_BEHAVIOURS);
            if (tokenStart < 0) {
                tokenStart = newPageContents.indexOf(LIST_OF_MICRO_BEHAVIOURS_US_ENGLISH);
            }
            if (tokenStart < 0) {
                clientState.warn("Processing list of micro-behaviours named " + name + 
                        " but can no longer find the token " + LIST_OF_MICRO_BEHAVIOURS + 
                        " in " + newPageContents);
                continue;
            }
            int listStart = newPageContents.indexOf("[", tokenStart);
            int macroBehaviourEnd = newPageContents.indexOf("]", tokenStart);
            String initialMicroBehaviours;
            if (listsOfMicroBehaviours == null) {
                initialMicroBehaviours = 
                        removeQuotedUrlsAndEncode(newPageContents.substring(listStart+1, macroBehaviourEnd), 
                                clientState,
                                urlString);
            } else {
                initialMicroBehaviours = stringOfMicroBehaviourUrls(name, listsOfMicroBehaviours);
            }
            String macroBehaviourHTMLElement = 
                    "<span macroBehaviour='" + CommonUtils.encode(name) + 
                    "' url='" + urlString +
                    "' id='" + ResourcePageServiceImpl.generateID(idCounters);
            if (initialMicroBehaviours != null && !initialMicroBehaviours.trim().isEmpty()) {
                macroBehaviourHTMLElement += "' initialMicroBehaviours='" + initialMicroBehaviours;
            }
            macroBehaviourHTMLElement +=  "'>" + name + "</span>";
            newPageContents.replace(tokenStart, macroBehaviourEnd+1, macroBehaviourHTMLElement);
        }
        return newPageContents.toString();
    }

    public static String removeQuotedUrlsAndEncode(String stringOfUrls, ClientState clientState, String sourceUrlString) {
        // stringOfUrls should be a space separated list of urls optionally quoted
        // removes quotes and url encodes input
        // first strip away any HTML
        stringOfUrls = CommonUtils.getInnerText(stringOfUrls);
        StringBuffer result = new StringBuffer();
        int quoteStart = stringOfUrls.indexOf('"');
        if (quoteStart >= 0) {
            int quoteEnd = stringOfUrls.indexOf('"', quoteStart+1);
            if (quoteEnd < 0) {
                clientState.warn("Did not find the expected matching quotes in " + stringOfUrls + " in " + sourceUrlString);
                return stringOfUrls;
            }
            if (quoteStart > 0) {
                result.append(removeQuotedUrlsAndEncode(stringOfUrls.substring(0, quoteStart), clientState, sourceUrlString));
                result.append(" ");
            }
            try {
                result.append(URLEncoder.encode(stringOfUrls.substring(quoteStart+1, quoteEnd), "UTF-8"));
                result.append(" ");
            } catch (UnsupportedEncodingException e) {
                clientState.logException(e, " Can't encode URL in removeQuotedUrlsAndEncode");
                return stringOfUrls;
            }
            result.append(removeQuotedUrlsAndEncode(stringOfUrls.substring(quoteEnd+1), clientState, sourceUrlString));
        } else {
            String[] urls = stringOfUrls.split("(\\s)+");
            for (String url : urls) {
                try {
                    result.append(URLEncoder.encode(url, "UTF-8"));
                    result.append(" ");
                } catch (UnsupportedEncodingException e) {
                    clientState.logException(e, " Can't encode URL in removeQuotedUrlsAndEncode");
                    return stringOfUrls;
                }
            }
        }	
        return result.toString();
    }

    protected String substituteTextAreasForNames(String pageContents, String urlString, ClientState clientState) {
        MicroBehaviour microBehaviour = getMicroBehaviourFromCacheOrDatabase(urlString, clientState);
        if (microBehaviour == null) {
            return pageContents;
        }
        ArrayList<String> textAreaElements = microBehaviour.getTextAreaElements();
        if (textAreaElements == null || textAreaElements.isEmpty()) {
            return pageContents;
        }
        boolean namesUpdated = false;
        StringBuffer newPageContents = new StringBuffer(pageContents);
        for (int i = 0; i < textAreaElements.size(); i += 2) {
            String name = textAreaElements.get(i);
            int declarationStart = newPageContents.indexOf(CommonUtils.SUBSTITUTE_TEXT_AREA_FOR);
            if (declarationStart < 0) {
                if (namesUpdated) {
                    return newPageContents.toString();
                } else {
                    clientState.warn("In processing '" + name + "' could not find " + CommonUtils.SUBSTITUTE_TEXT_AREA_FOR + " in " + newPageContents);
                    return newPageContents.toString();
                }
            }
            int declarationEnd = newPageContents.indexOf("\n", declarationStart);
            if (declarationEnd < 0) {
                declarationEnd = newPageContents.indexOf("\r", declarationStart);
                if (declarationEnd < 0) {
                    clientState.warn("Expected a new line after the declaration beginning with " + CommonUtils.SUBSTITUTE_TEXT_AREA_FOR);
                    return newPageContents.toString();
                }
            }
            newPageContents.replace(declarationStart, declarationEnd+1, "");
            int nameStart = newPageContents.indexOf(name, declarationStart);
            if (nameStart < 0) {
                // hack to deal with built-in MBs having the names of their coordinates
                // changed from lower-left-* to upper-left-*
                // and          upper-right-* to lower-right-*
                if (name.equals("lower-left-corner-x")) {
                    nameStart = newPageContents.indexOf("upper-left-corner-x", declarationStart);
                } else if (name.equals("lower-left-corner-y")) {
                    nameStart = newPageContents.indexOf("upper-left-corner-y", declarationStart);
                } else if (name.equals("upper-right-corner-x")) {
                    nameStart = newPageContents.indexOf("lower-right-corner-x", declarationStart);
                } else if (name.equals("upper-right-corner-y")) {
                    nameStart = newPageContents.indexOf("lower-right-corner-y", declarationStart);
                }
                namesUpdated = true;
            }
            if (nameStart < 0) {
                // if the same name is used twice then the second time it will have been substituted already
                // and name will be Will.
                clientState.warn("Could not find " + name + " in code area."); 
            } else {
                int nameEnd = nameStart + name.length();
                newPageContents.replace(nameStart, nameEnd, textAreaElements.get(i+1));
            }
        }
        return newPageContents.toString();
    }

    protected String fetchAndCreateMicroBehaviours(String urlString, String baseURL, ClientState clientState) {
        return fetchAndCreateMicroBehaviours(urlString, baseURL, null, clientState);
    }

    protected String fetchAndCreateMicroBehaviours(String urlString, String baseURL, MacroBehaviour containingMacroBehaviour, ClientState clientState) {
        // returns null or an error string
        // TODO: determine if error string still makes sense
        // since can give error to clientState directly
        if (urlString.isEmpty()) {
            return null;
        }
        try {
            // if relative make it absolute
            urlString = CommonUtils.joinPaths(baseURL, urlString);
            int newIdCounters[] = new int[3];
            // these won't be used or kept
            // for element ids
            newIdCounters[0] = 0;
            newIdCounters[1] = 0;
            // used to index text areas
            newIdCounters[2] = 0;
            if (CommonUtils.isAbsoluteURL(urlString)) {
                baseURL = CommonUtils.getBaseURL(urlString);
            }
            Document document = fetchPageDocument(urlString, clientState, null);
            if (document == null) {
                return "Error reading the file: " + urlString;
            }
            return createMicroBehaviours(document, urlString, baseURL, containingMacroBehaviour, clientState, newIdCounters);
            //	} catch (FileNotFoundException e) {
            //	    return "Error the file was not found: " + urlString;
        } catch (Exception e) {    
            return ServerUtils.logException(e, "fetching the contents of " + urlString);
        }
    }

    protected String createMicroBehaviours(
            Document document, 
            String urlString, 
            String baseURL, 
            MacroBehaviour containingMacroBehaviour, 
            ClientState clientState, 
            int idCounters[]) {
        try {
            transformNodes(document, document, urlString, baseURL, clientState, idCounters, null, -1);
            findMicroBehaviourAndAddAttributes(document, urlString, baseURL, containingMacroBehaviour, clientState, idCounters, null, null);
            return null;
        } catch (Exception e) {
            return ServerUtils.logException(e, "In createMicroBehaviours ");
        }
    }

    protected Element transformNodes(Document document, Node node, String urlString, String baseURL, ClientState clientState,
            int idCounters[], Element behaviourNameElement, int phase) {
        Node child = node.getFirstChild();
        while (child != null) {
            // recur first to explore in depth-first fashion
            // so elements that contain micro-behaviours are processed and parents don't need to be
            behaviourNameElement = transformNodes(document, child, urlString, baseURL, clientState, idCounters, behaviourNameElement, phase);
            // compute the following first in case this gets replaced
            Node nextChild = child.getNextSibling();
            if (child instanceof Element) {
                Element newBehaviourNameElement = transformElement((Element) child, document, urlString, baseURL, clientState,
                        idCounters, behaviourNameElement, phase);
                if (behaviourNameElement == null && newBehaviourNameElement != null) {
                    behaviourNameElement = newBehaviourNameElement;
                }       	
            }  // otherwise can be <?xml version="1.0" encoding="UTF-8"?> for example
            child = nextChild;
        }
        return behaviourNameElement;
    }

    protected boolean findMicroBehaviourAndAddAttributes(Node node, 
            String urlString, 
            String baseURL,
            MacroBehaviour containingMacroBehaviour,
            ClientState clientState, 
            int idCounters[], 
            ArrayList<ArrayList<String>> listsOfMicroBehaviours, 
            String[] answer) {
        // returns true if found and added attributes to a micro-behaviour
        Node child = node.getFirstChild();
        while (child != null) {
            if (findMicroBehaviourAndAddAttributes(child, urlString, baseURL, containingMacroBehaviour, clientState, idCounters, listsOfMicroBehaviours, answer)) {
                return true;        	
            }
            if (child instanceof Element) {
                if (addToAttributesIfMicroBehaviour((Element) child,  urlString, baseURL, containingMacroBehaviour, clientState, idCounters, listsOfMicroBehaviours, answer)) {
                    if (listsOfMicroBehaviours != null && listsOfMicroBehaviours.isEmpty()) {
                        // seems there is some redundancy in storing the micro-behaviours of macro-behaviours of micro-behaviours
                        // so check if available here before assuming there are no micro-behaviours on this page
                        // TODO: rationalise all this
                        MicroBehaviour microBehaviour = getMicroBehaviourFromCacheOrDatabase(urlString, clientState);
                        if (microBehaviour != null) {
                            ArrayList<MacroBehaviour> macroBehaviours = microBehaviour.getMacroBehaviours();
                            for (MacroBehaviour macroBehaviour : macroBehaviours) {
                                ArrayList<String> listOfMicroBehaviours = new ArrayList<String>();
                                listOfMicroBehaviours.add(macroBehaviour.getObjectName());
                                ArrayList<MicroBehaviour> microBehaviours = macroBehaviour.getMicroBehaviours();
                                for (MicroBehaviour innerMicroBehaviour : microBehaviours) {
                                    listOfMicroBehaviours.add(innerMicroBehaviour.getBehaviourURL());
                                }
                                listsOfMicroBehaviours.add(listOfMicroBehaviours);
                            }
                        }
                    }
                    return true;
                }
            }         
            child = child.getNextSibling();
        }
        return false;
    }

    protected boolean addToAttributesIfMicroBehaviour(Element element, 
            String allURLs, 
            String baseURL,
            MacroBehaviour containingMacroBehaviour,
            ClientState clientState, 
            int idCounters[], 
            ArrayList<ArrayList<String>> listsOfMicroBehaviours,
            String[] answer) {
        // commenting out the following fixes the problem of opening something that was opened as required
        // but must break circular reference handling
        //	MicroBehaviour alreadyProcessedMicroBehaviour = microBehaviourURLTable.get(urlString);
        //	if (alreadyProcessedMicroBehaviour != null && alreadyProcessedMicroBehaviour != MicroBehaviour.getDummyMicroBehaviour()) {
        //	    setElementBehaviourAttributes(element, urlString, idCounters, null);
        //	    return true;
        //	}
        String text = element.getTextContent();
        int codeTokenStart = -1;
        int tokenStart = -1;
        int tokenEnd;
        tokenStart = text.indexOf(BEGIN_MB_TOKEN);
        if (tokenStart < 0) {
            tokenStart = text.indexOf(US_ENGLISH_BEGIN_MB_TOKEN);
            tokenEnd = tokenStart + US_ENGLISH_BEGIN_MB_TOKEN.length();
        } else {
            tokenEnd = tokenStart + BEGIN_MB_TOKEN.length();
        }
        if (tokenStart < 0) {
            return false;
        }
        String[] urls = allURLs.split(";");
        for (int i = 0; i < urls.length; i++) {
            String url = urls[i];
            String withoutMinusSign = CommonUtils.withoutMinusSign(allURLs);
            boolean active = (allURLs == withoutMinusSign);
            allURLs = withoutMinusSign;
            // might be searching for "Begin micro-behaviour" -- no need to warn
            // and text is typically only part of the page
            boolean okToWarn = (text.indexOf(END_MB_TOKEN, tokenStart) >= 0);
            ArrayList<MicroBehaviour> requiredMicroBehaviours = new ArrayList<MicroBehaviour>();
            StringBuilder behaviourNameHTML = new StringBuilder();
            StringBuilder behaviourNameText = new StringBuilder(); // TODO: determine if obsolete
            codeTokenStart = text.indexOf(BEGIN_NETLOGO_CODE_TOKEN);
            Node currentNode = element;
            boolean removeCurrentNode = false;
            StringBuilder code = new StringBuilder();
            int codeEnd = text.indexOf(END_MB_TOKEN);
            if (codeTokenStart > tokenEnd) {
                // "plain text" micro-behaviour
                String behaviourName = text.substring(tokenEnd,codeTokenStart).replace("\\s", " ");
                behaviourNameText.append(behaviourName);
                behaviourNameHTML.append(behaviourName); // ok?
                if (codeEnd > 0) {
                    int codeStart = codeTokenStart+BEGIN_NETLOGO_CODE_TOKEN.length();
                    if (text.charAt(codeStart) == ':') { // optional trailing :
                        codeStart++;
                    }
                    code.append(text.substring(codeStart, codeEnd));
                }
            } else {
                while (codeTokenStart < 0) {
                    // look for rest in next siblings and then uncles
                    Node nextNode = currentNode.getNextSibling();
                    if (nextNode == null) {
                        Node parent = currentNode.getParentNode();
                        if (parent == null) {
                            if (okToWarn) {
                                clientState.warn("Found token for starting a micro-behaviour but didn't find " + BEGIN_NETLOGO_CODE_TOKEN);
                            }
                            return false;
                        } else {
                            // Parent is already finished so try my "uncle"
                            nextNode = parent.getNextSibling();
                        }
                    }
                    if (removeCurrentNode) {
                        currentNode.getParentNode().removeChild(currentNode);
                        removeCurrentNode = false;
                    }
                    if (nextNode == null) {
                        break;
                    }
                    currentNode = nextNode;
                    text = currentNode.getTextContent();
                    codeTokenStart = text.indexOf(BEGIN_NETLOGO_CODE_TOKEN);
                    if (currentNode != element) {
                        // not the Element that is accumulating attributes
                        removeCurrentNode = true;
                    }
                    if (codeTokenStart < 0) {
                        behaviourNameHTML.append(nodeToHTML(currentNode, clientState));
                        behaviourNameText.append(text.replace("\\s", " "));
                    } else {
                        behaviourNameText.append(text.substring(0,codeTokenStart).replace("\\s", " "));
                        break;
                    }
                }
            }
            if (answer != null) {
                answer[6] = behaviourNameHTML.toString();
            }
            // now collect up the code 
            while (codeEnd < 0) {
                // look for rest in next siblings and uncles
                Node nextNode = currentNode.getNextSibling();
                if (nextNode == null) {
                    Node parent = currentNode.getParentNode();
                    if (parent == null) {
                        if (okToWarn) {
                            clientState.warn("Found token for starting a micro-behaviour and the code but didn't find " + END_MB_TOKEN);
                        }
                        return false;
                    } else {
                        // Parent is already finished so try my "uncle"
                        nextNode = parent.getNextSibling();
                    }
                }
                if (removeCurrentNode) {
                    currentNode.getParentNode().removeChild(currentNode);
                    removeCurrentNode = false;
                }
                if (nextNode == null) {
                    if (okToWarn) {
                        clientState.warn("Found token for starting a micro-behaviour and the code but didn't find " + END_MB_TOKEN);
                    }
                    return false;
                }
                currentNode = nextNode;
                text = currentNode.getTextContent().trim();
                if (!text.equals("")) {
                    if (currentNode instanceof Element) {
                        codeEnd = code.indexOf(END_MB_TOKEN);
                        if (codeEnd > 0) {
                            code.replace(codeEnd, code.length(), "");
                        }
                        int textEnd = text.indexOf(END_MB_TOKEN);
                        if (codeEnd >= 0 || textEnd >= 0) {
                            if (currentNode != element) {
                                // not the Element that is accumulating attributes
                                currentNode.getParentNode().removeChild(currentNode);
                            }
                            if (textEnd >= 0) {
                                break;
                            }
                        }
                        Element nextElement = (Element) currentNode;
                        String linkURL = nextElement.getAttribute("hyperlink");
                        if (linkURL.isEmpty()) {
                            String transformedCode = 
                                    transformElementsInCode(nextElement, requiredMicroBehaviours, baseURL, clientState, idCounters);
                            code.append(transformedCode);
                            if (!transformedCode.endsWith("\n")) {
                                code.append("\n");
                            }
                        } else {
                            MicroBehaviour microBehaviour = getMicroBehaviourFromCacheOrDatabase(linkURL, clientState);	    
                            if (microBehaviour == null) { // is new
                                String error = fetchAndCreateMicroBehaviours(linkURL, baseURL, clientState);
                                if (error != null) {
                                    clientState.warn(error);
                                } else {
                                    microBehaviour = getMicroBehaviourFromCache(linkURL);
                                }
                                generateReferenceToBehaviour(microBehaviour, linkURL, code, requiredMicroBehaviours, clientState);
                            } else {
                                addConvertedURLToNetLogoCode(microBehaviour, linkURL, code);
                            }
                        }
                        if (codeEnd >= 0) {
                            break;
                        }
                    }
                }
            }
            String behaviourNameHTMLString = behaviourNameHTML.toString().trim();
            String changesGuid = CommonUtils.changesGuid(allURLs);
            HashMap<Integer, String> textAreaValues = null;
            if (changesGuid != null) {
                textAreaValues = fetchTextAreaValues(url, clientState);
                if (textAreaValues == null || textAreaValues.isEmpty()) {
                    // not a problem in general since model XML also includes the changes
                    //		clientState.warn("Warning. Could not find the changes for this URL: " + urlString + 
                    //			         ". Using the original values instead. ");
                } else {
                    String newBehaviourName = textAreaValues.get(-1);
                    if (newBehaviourName == null) {
                        if (i == urls.length-1) {
                            clientState.warn("Warning. Could not find the behaviour name of the changes GUID: " + changesGuid + 
                                    " while processing " + allURLs +
                                    ". Using the original name instead. ");
                            // update it so don't get warning every time it is loaded
                            MicroBehaviourData microBehaviourData = MicroBehaviourData.getMicroBehaviourData(url);
                            textAreaValues.put(-1, behaviourNameHTMLString);
                            microBehaviourData.setTextAreaValues(textAreaValues);
                            ServerUtils.persist(microBehaviourData);			    
                        } else {
                            // try next URL
                            continue;
                        }
                    } else {
                        behaviourNameHTMLString = newBehaviourName;
                    }
                }
                if (listsOfMicroBehaviours != null) {
                    String error = fetchListsOfMicroBehaviours(changesGuid, listsOfMicroBehaviours, clientState);
                    if (error != null) {
                        textAreaValues.put(-2, error);
                    }
                }
            }
            // warn if behaviourNameHTML is just white space (i.e. the empty string after trimming)?
            if (behaviourNameHTMLString.isEmpty()) {
                clientState.warn("No name found. Could not find text between " + BEGIN_MB_TOKEN +
                        " and " + BEGIN_NETLOGO_CODE_TOKEN);
                return false;
            }
            String codeString = code.toString();
            ArrayList<String> textAreaElements = new ArrayList<String>();
            codeString = substituteTextAreas(
                    codeString, 
                    textAreaElements, 
                    clientState, 
                    idCounters);
            ArrayList<MacroBehaviour> macroBehaviours = new ArrayList<MacroBehaviour>();
            codeString = substituteMacroBehaviours(codeString, macroBehaviours, baseURL, clientState, idCounters, listsOfMicroBehaviours, allURLs);
            if (BehaviourCode.update(url, codeString, textAreaElements)) {
                behaviourCodeChanged(url, codeString, textAreaElements);
            }
            MicroBehaviour behaviour = getMicroBehaviourFromCache(allURLs);
            if (behaviour == null || behaviour == getDummyMicroBehaviour()) {
                behaviour = 
                        new MicroBehaviour(
                                behaviourNameHTMLString, 
                                codeString,
                                allURLs, 
                                this, 
                                netLogoModel, // can be null so need to pass clientState as well
                                clientState,
                                textAreaElements,
                                fetchEnhancements(allURLs, clientState),
                                macroBehaviours,
                                false);
            } else {
                if (behaviour.getMacroBehaviours() == null) {
                    behaviour.setMacroBehaviours(macroBehaviours);
                }
                ArrayList<String> oldTextAreaElements = behaviour.getTextAreaElements();
                if (oldTextAreaElements == null) {
                    behaviour.setTextAreaElements(textAreaElements);
                } else {
                    // merge giving priority to the newly discovered text areas
                    int size = textAreaElements.size();
                    for (int j = 0; j < size; j += 2) {
                        updateTextAreaElements(oldTextAreaElements, textAreaElements.get(j), textAreaElements.get(j+1));
                        if (textAreaValues != null && textAreaValues.get(j) == null) {
                            textAreaValues.put(j, CommonUtils.removeHTMLMarkup(textAreaElements.get(j+1)));
                        }
                    }
                }
            }
            if (containingMacroBehaviour != null) {
                containingMacroBehaviour.add(behaviour, active);
            }
            behaviour.setReferencedMicroBehaviours(requiredMicroBehaviours);
            setElementBehaviourAttributes(
                    element, allURLs, clientState, idCounters, behaviourNameHTMLString);
            behaviour.createMicroBehaviourDataIfNeeded();
            behaviour.updateMicroBehaviourDataIfNeeded(textAreaValues);
            rememberMicroBehaviour(behaviour, allURLs);
            return true;
        }
        return false;
    }

    private void removeOldEntriesFromMicroBehaviourURLTable(long earliestValidTime) {
        //	if (CommonUtils.useJDO) {
        //	    Set<Entry<String, Long>> entrySet = sessionToMicroBehaviourURLTableLastAccessTime.entrySet();
        //	    for (Entry<String, Long> entry : entrySet) {
        //		if (entry.getValue() < earliestValidTime) {
        //		    String sessionGuid = entry.getKey();
        //		    sessionToMicroBehaviourURLTable.remove(sessionGuid);
        //		    sessionToMicroBehaviourURLTableLastAccessTime.remove(sessionGuid);
        //		}
        //	    }
        //	    return;
        //	}
        Set<Entry<String, Long>> entrySet = microBehaviourURLUpdateTime.entrySet();
        for (Entry<String, Long> entry : entrySet) {
            if (entry.getValue() < earliestValidTime) {
                String url = entry.getKey();
                microBehaviourURLTable.remove(url);
                microBehaviourURLUpdateTime.remove(url);
                //		Logger.getLogger(RESOURCE_SERVICE_LOGGER_NAME).warning("Removed the expired micro-behaviour " + url+ ". Size of cache is " + microBehaviourURLTable.size());
            }
        }
    }

    private void updateTextAreaElements(ArrayList<String> textAreaElements,
            String name, 
            String element) {
        int size = textAreaElements.size();
        for (int i = 0; i < size; i += 2) {
            if (name.equals(textAreaElements.get(i))) {
                textAreaElements.set(i+1, element);
                return;
            }
        }
        textAreaElements.add(name);
        textAreaElements.add(element);
    }

    private String substituteTextAreas(String behaviourCode,
            ArrayList<String> textAreaElements, 
            ClientState clientState, 
            int idCounters[]) {
        return substituteTextAreas(behaviourCode, behaviourCode, textAreaElements, clientState, idCounters);
    }

    private String substituteTextAreas(String behaviourCode,
            String originalBehaviourCode, 
            ArrayList<String> textAreaElements,
            ClientState clientState, 
            int idCounters[]) {
        // searches for occurrences of 
        // substitute-text-area-for name value...
        // the length of value (including spaces on either side) determines the number of columns of the text area
        int substituteTextAreaTokenStart = behaviourCode.indexOf(CommonUtils.SUBSTITUTE_TEXT_AREA_FOR);
        if (substituteTextAreaTokenStart < 0) {
            return behaviourCode; // none left
        }
        int substituteTextAreaTokenEnd = substituteTextAreaTokenStart + CommonUtils.SUBSTITUTE_TEXT_AREA_FOR.length();
        int[] nextTokenStartEnd = nextToken(behaviourCode, substituteTextAreaTokenEnd, clientState);
        if (nextTokenStartEnd == null) {
            return behaviourCode;
        }
        String name = behaviourCode.substring(nextTokenStartEnd[0], nextTokenStartEnd[1]);
        Integer columns = null;
        int rows = 1;
        if (name.equalsIgnoreCase("columns:")) {
            nextTokenStartEnd = nextToken(behaviourCode, nextTokenStartEnd[1]+1, clientState);
            if (nextTokenStartEnd == null) {
                return behaviourCode;
            }
            try {
                String columnsString = behaviourCode.substring(nextTokenStartEnd[0], nextTokenStartEnd[1]);
                columns = Integer.parseInt(columnsString);
            } catch (NumberFormatException e) {
                clientState.warn("Expected columns: to be followed by a number in " + behaviourCode);
                return behaviourCode;
            }
            nextTokenStartEnd = nextToken(behaviourCode, nextTokenStartEnd[1]+1, clientState);
            if (nextTokenStartEnd == null) {
                return behaviourCode;
            }
            name = behaviourCode.substring(nextTokenStartEnd[0], nextTokenStartEnd[1]);
        }
        if (name.equalsIgnoreCase("rows:")) {
            nextTokenStartEnd = nextToken(behaviourCode, nextTokenStartEnd[1]+1, clientState);
            if (nextTokenStartEnd == null) {
                return behaviourCode;
            }
            try {
                String rowsString = behaviourCode.substring(nextTokenStartEnd[0], nextTokenStartEnd[1]);
                rows = Integer.parseInt(rowsString);
            } catch (NumberFormatException e) {
                clientState.warn("Expected rows: to be followed by a number in " + behaviourCode);
                return behaviourCode;
            }
            nextTokenStartEnd = nextToken(behaviourCode, nextTokenStartEnd[1]+1, clientState);
            if (nextTokenStartEnd == null) {
                return behaviourCode;
            }
            name = behaviourCode.substring(nextTokenStartEnd[0], nextTokenStartEnd[1]);
        }
        int valueStart = nextTokenStartEnd[1]+1;
        int valueEnd = behaviourCode.indexOf('\n', valueStart); 
        if (valueEnd < 0) {
            valueEnd = behaviourCode.indexOf('\r', valueStart); // just in case
            if (valueEnd < 0) {
                clientState.warn("Expected a new line after the declaration beginning with " + CommonUtils.SUBSTITUTE_TEXT_AREA_FOR + " in " + originalBehaviourCode);
                return behaviourCode; 
            }
        }
        String value = behaviourCode.substring(valueStart, valueEnd);
        int textAreaCounter = idCounters[2]++;
        if (columns != null) {
            value = value.trim();
        }
        // multi-line values use <br> to break the line
        value = value.replace("<br>", "\r").replace("\\n", "\r");
        String textArea = "<textarea cols='" + (columns==null?1000:columns) + 
                "' rows='" + rows + "' name='" + name + 
                "' title='Click here to edit " + name + ".'" + 
                " id='" + ResourcePageServiceImpl.generateID(idCounters) + 
                "' index='" + textAreaCounter  + "'>" + 
                // spaces control the number of columns but not the value itself
                value + 
                "</textarea>";
        textAreaElements.add(name);
        textAreaElements.add(textArea);
        String nameReplacement = ServerUtils.textAreaPlaceHolder(textAreaCounter);
        StringBuffer codeReplacement = 
                new StringBuffer(behaviourCode.substring(0, substituteTextAreaTokenStart) + behaviourCode.substring(valueEnd));
        int nameReferenceStart = codeReplacement.indexOf(name);
        if (nameReferenceStart < 0) {
            clientState.warn("Could not find " + name + " in " + codeReplacement); 
        } else {
            int nameLength = name.length();
            int nameReplacementLength = nameReplacement.length();
            int nameReferenceEnd = nameReferenceStart + nameLength;
            codeReplacement.replace(nameReferenceStart, nameReferenceEnd, nameReplacement);
            // replace all
            while ((nameReferenceStart = codeReplacement.indexOf(name, nameReferenceStart+nameReplacementLength)) >= 0) {
                codeReplacement.replace(nameReferenceStart, nameReferenceStart + nameLength, nameReplacement);
            }
        }
        return substituteTextAreas(codeReplacement.toString(), originalBehaviourCode, textAreaElements, clientState, idCounters);
    }

    private int[] nextToken(String string, int start, ClientState clientState) {
        // returns the start and end indices for the next token
        int[] result = new int[2];
        result[0] = CommonUtils.firstNonSpace(string, start);
        result[1] = string.indexOf(' ', result[0]);
        if (result[1] < 0) {
            clientState.warn("Expected a name followed by a space after " + CommonUtils.SUBSTITUTE_TEXT_AREA_FOR + " in " + string);
            return null;
        }
        return result;
    }

    private String substituteMacroBehaviours(String behaviourCode,
            ArrayList<MacroBehaviour> macroBehaviours,
            String baseURL, 
            ClientState clientState, 
            int idCounters[],
            ArrayList<ArrayList<String>> listsOfMicroBehaviours,
            String sourceUrlString) {
        return substituteMacroBehaviours(behaviourCode, behaviourCode, macroBehaviours, baseURL, clientState, idCounters, listsOfMicroBehaviours, sourceUrlString);
    }

    private String substituteMacroBehaviours(String behaviourCode,
            String originalBehaviourCode,
            ArrayList<MacroBehaviour> macroBehaviours,
            String baseURL,
            ClientState clientState, 
            int idCounters[],
            ArrayList<ArrayList<String>> listsOfMicroBehaviours,
            String sourceUrlString) {
        int macroBehaviourStart = behaviourCode.indexOf(LIST_OF_MICRO_BEHAVIOURS);
        int macroBehaviourEnd = macroBehaviourStart + LIST_OF_MICRO_BEHAVIOURS.length();
        if (macroBehaviourStart < 0) {
            macroBehaviourStart = behaviourCode.indexOf(LIST_OF_MICRO_BEHAVIOURS_US_ENGLISH);
            macroBehaviourEnd = macroBehaviourStart + LIST_OF_MICRO_BEHAVIOURS_US_ENGLISH.length();
        }
        if (macroBehaviourStart < 0) {
            return behaviourCode; // none left
        }	
        int nameStart = behaviourCode.indexOf('"', macroBehaviourEnd);
        if (nameStart < 0) {
            clientState.warn("Expected a quoted name after " + LIST_OF_MICRO_BEHAVIOURS + " in " + originalBehaviourCode);
            return behaviourCode;
        }
        int nameEnd = behaviourCode.indexOf('"', nameStart+1);
        if (nameEnd < 0) {
            clientState.warn("Expected a quoted name after " + LIST_OF_MICRO_BEHAVIOURS + " in " + originalBehaviourCode);
            return behaviourCode;
        }
        String name = behaviourCode.substring(nameStart+1, nameEnd);
        int microBehavioursStart = behaviourCode.indexOf('[', macroBehaviourEnd);
        if (microBehavioursStart < 0) {
            clientState.warn("Expected a square bracketed list of micro-behaviours after " + LIST_OF_MICRO_BEHAVIOURS + " \"" + name + "\"  in " + originalBehaviourCode);
            return behaviourCode;
        }
        microBehavioursStart++; // skip over the "
        int microBehavioursEnd = behaviourCode.indexOf(']', microBehavioursStart);
        if (microBehavioursEnd < 0) {
            clientState.warn("Expected a square bracketed list of micro-behaviours after " + LIST_OF_MICRO_BEHAVIOURS + " \"" + name + "\"  in " + originalBehaviourCode);
            return behaviourCode;
        }
        MacroBehaviour macroBehaviour = new MacroBehaviour(name, netLogoModel, this);
        String encodedURLs = "";
        String changesGuid = CommonUtils.changesGuid(sourceUrlString);
        if (listsOfMicroBehaviours == null && changesGuid == null) {
            // i.e. this is not a copy so use the default micro-behaviours
            // specified on the web page
            String microBehavioursList = 
                    behaviourCode.substring(microBehavioursStart, microBehavioursEnd);
            encodedURLs = removeQuotedUrlsAndEncode(microBehavioursList, clientState, sourceUrlString);
        } else {
            if (listsOfMicroBehaviours == null) {
                listsOfMicroBehaviours = new ArrayList<ArrayList<String>>();
                String error = fetchListsOfMicroBehaviours(changesGuid, listsOfMicroBehaviours, clientState);
                if (error != null) {
                    clientState.warn(error);
                }
            }
            // use what the database found
            for (ArrayList<String> listOfMicroBehaviours  : listsOfMicroBehaviours) {
                if (listOfMicroBehaviours.get(0).equals(name)) {
                    for (int i = 1; i < listOfMicroBehaviours.size(); i++) {
                        encodedURLs += listOfMicroBehaviours.get(i) + " ";
                    }
                }
            }
        }
        fetchAndCreateEachMicroBehaviour(
                encodedURLs, baseURL, macroBehaviour, clientState, idCounters, sourceUrlString);
        macroBehaviours.add(macroBehaviour);
        String newBehaviourCode = behaviourCode.substring(0, macroBehaviourStart) + 
                ServerUtils.macroBehaviourPlaceHolder(macroBehaviour.getObjectName()) +
                behaviourCode.substring(microBehavioursEnd+1);
        return substituteMacroBehaviours(newBehaviourCode, 
                originalBehaviourCode, macroBehaviours, baseURL, clientState, idCounters, listsOfMicroBehaviours, sourceUrlString);
    }

    public void fetchAndCreateEachMicroBehaviour(String encodedURLs, 
            String baseURL, 
            MacroBehaviour containingMacroBehaviour, 
            ClientState clientState,
            int idCounters[],
            String sourceUrlString) {
        String[] urls = encodedURLs.split("(\\s)+"); // white space
        for (String urlString : urls) {
            if (!urlString.isEmpty()) {
                try {
                    String withoutMinusSign = CommonUtils.withoutMinusSign(urlString);
                    boolean active = (urlString == withoutMinusSign);
                    urlString = withoutMinusSign;
                    String decodedUrlString = URLDecoder.decode(urlString, "UTF-8");
                    if (decodedUrlString.startsWith(CommonUtils.TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN_TRIMMED)) {
                        String prototypeName = 
                                decodedUrlString.substring(CommonUtils.TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN_LENGTH);
                        MacroBehaviourAsMicroBehaviour macroBehaviourAsMicroBehaviour = 
                                new MacroBehaviourAsMicroBehaviour(prototypeName, null);
                        containingMacroBehaviour.add(macroBehaviourAsMicroBehaviour, active);
                    } else {
                        urlString = CommonUtils.joinPaths(baseURL, decodedUrlString);
                        // using the table to stop recursion
                        MicroBehaviour microBehaviour = getMicroBehaviourFromCache(urlString);
                        if (microBehaviour != getDummyMicroBehaviour()) {
                            // if not recursively already fetching this micro-behaviour
                            // record that we are recursively working on this
                            if (microBehaviour == null) { // is new
                                microBehaviour = getDummyMicroBehaviour();
                                rememberMicroBehaviour(microBehaviour, urlString);
                                microBehaviour = 
                                        getMicroBehaviour(
                                                urlString, baseURL, clientState, idCounters, containingMacroBehaviour, true);	
                                if (microBehaviour == null) {
                                    removeMicroBehaviourCache(urlString);
                                    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe("Could not fetch the micro-behaviour: " + urlString);
                                    return;
                                } else {
                                    rememberMicroBehaviour(microBehaviour);
                                }
                            }
                            containingMacroBehaviour.add(microBehaviour, active);
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String stringOfMicroBehaviourUrls(String name,
            ArrayList<ArrayList<String>> listsOfMicroBehaviours) {
        // returns a space separated string of urls that for 'name'
        for (ArrayList<String> listOfMicroBehaviours : listsOfMicroBehaviours) {
            if (listOfMicroBehaviours.get(0).equals(name)) {
                StringBuffer urls = new StringBuffer();
                int size = listOfMicroBehaviours.size();
                for (int i = 1; i < size; i++) {
                    urls.append(listOfMicroBehaviours.get(i));
                    urls.append(" ");
                }
                return urls.toString();
            }
        }
        return null; // warn?
    }

    protected void setElementBehaviourAttributes(Element element, String urlString, ClientState clientState, int[] idCounters, String behaviourName) {
        String cachedBehaviourName = behaviourNameURLTable.get(urlString);
        if (cachedBehaviourName != null) {
            String oldBehaviourName = CommonUtils.decode(cachedBehaviourName);
            if (!oldBehaviourName.equals(behaviourName)) {
                // cache is out of date
                behaviourNameURLTable.remove(urlString);
                setElementBehaviourAttributes(element, urlString, clientState, idCounters, behaviourName);
                return;
            }
        }
        if (cachedBehaviourName != null) {
            element.setAttribute("behaviourname", cachedBehaviourName);
        } else if (behaviourName == null) {
            clientState.warn("Internal error within setElementBehaviourAttributes.");
            return;
        } else {
            String nameEncoding = fetchUpdatedNameHTML(urlString, clientState);
            if (nameEncoding == null) {
                nameEncoding = CommonUtils.encode(behaviourName);
            }
            element.setAttribute("behaviourname", nameEncoding);
            behaviourNameURLTable.put(urlString, nameEncoding);
        }
        element.setAttribute("SourceURL", urlString);
        element.setAttribute("MicroBehaviour", "1"); // still used???
        element.setAttribute("id", generateID(idCounters));
    }

    public String fetchUpdatedNameHTML(String urlString, ClientState clientState) {
        String changesGuid = CommonUtils.changesGuid(urlString);
        if (changesGuid != null) {
            String nameHTML = fetchUpdateFromCopy(urlString, -1, clientState);
            if (nameHTML == null) {
                return null;
            } else {
                return CommonUtils.encode(nameHTML);
            }
        }
        return null;
    }

    protected Element transformElement(
            Element element, 
            Document document, 
            String urlString, 
            String baseURL, 
            ClientState clientState,  
            int idCounters[], 
            Element behaviourNameElement, 
            int phase) {
        Element newBehaviourNameElement = null;
        if (phase == 0) {
            expandURLs("src", element, urlString, baseURL);
            expandURLs("action", element, urlString, baseURL);
            expandURLs("@import", element, urlString, baseURL);
            // following makes absolute version but further processing is in next phase
            expandURLs("href", element, urlString, baseURL); 
            renumberIDs(element, idCounters);
        } else if (phase == 1) {
            replaceHREFs(element, document, urlString, baseURL, idCounters);
            // TODO: ensure that the following is only done when running MoPiX
            addAttributesToMoPiXElements(element, "equationMathML", idCounters);
            addAttributesToMoPiXElements(element, "modelMathML", idCounters);
            newBehaviourNameElement = addAttributesToMicroBehaviours(element);
            addToAttributesIfPRE(element, behaviourNameElement, urlString, baseURL, clientState, idCounters, phase);
            addAttributesifFORM(element, document, urlString, idCounters);
            addAttributesifTEXTAREA(element, document, urlString, idCounters);
            insertElementBeforeAndAfterNetLogoCode(element, document);
        } else if (phase == -1) {
            // just processing referenced micro-behaviours
            replaceHREFs(element, document, urlString, baseURL, idCounters); 
            newBehaviourNameElement = addAttributesToMicroBehaviours(element);
            addToAttributesIfPRE(element, behaviourNameElement, urlString, baseURL, clientState, idCounters, phase);
        }
        if (newBehaviourNameElement != null) {
            return newBehaviourNameElement;
        } else {
            return null;
        }
    }

    protected Element addAttributesToMicroBehaviours(Element element) {
        // backwards compatible with BehaviourComposer 1.0
        // returns true if next element is expected to be a PRE element with NetLogo code
        String id = element.getAttribute("id");
        if (id.equalsIgnoreCase("behaviourname")) {
            return element;
        }
        return null;
    }

    /**
     * 
     * Adds two elements before and after element if it is a PRE element
     * @param element
     * @param document 
     * 
     */
    protected void insertElementBeforeAndAfterNetLogoCode(Element element, Document document) {
        String textContent = element.getTextContent();
        if (textContent.startsWith(BEGIN_NETLOGO_CODE_TOKEN)) {
            Element beforeElement = document.createElement("PRE");
            beforeElement.setAttribute("id", CommonUtils.BEFORE_CODE_ELEMENT);
            Node parentNode = element.getParentNode();
            parentNode.insertBefore(beforeElement, element.getNextSibling());
        } else if (textContent.startsWith(END_MB_TOKEN)) {
            Element afterElement = document.createElement("PRE");
            afterElement.setAttribute("id", CommonUtils.AFTER_CODE_ELEMENT);
            Node parentNode = element.getParentNode();
            parentNode.insertBefore(afterElement, element);  
        }
    }

    // TODO: determine if there are any old micro-behaviours left that require this
    protected void addToAttributesIfPRE(
            Element element, 
            Element behaviourNameElement, 
            String urlString, 
            String baseURL, 
            ClientState clientState, 
            int idCounters[], 
            int phase) {
        if (!element.getTagName().equalsIgnoreCase("PRE")) {
            return;
        }
        String checkBoxCode = nodeToHTML(element, clientState).replaceAll("&nbsp;", " ");
        int checkBoxStart = checkBoxCode.indexOf("Begin Replay Session Events Check Box:");
        if (checkBoxStart >= 0) {
            int checkBoxEnd = checkBoxCode.indexOf("End Replay Session Events Check Box", checkBoxStart);
            if (checkBoxEnd < 0) {
                clientState.warn("Found the beginning of a session events check box but not the end in: " + checkBoxCode);
                return;
            }
            String tokens[] = {"ID: ",
                    "Label: ",
                    "Session ID: ",
                    "Do message: ",
                    "Undo message: ",
                    "Title: ",
            "\n"};
            int startEnd[] = {0, 0};
            if (!ServerUtils.extractValue(tokens[0], tokens[1], checkBoxCode, startEnd, clientState)) {
                return;
            }
            String id = checkBoxCode.substring(startEnd[0], startEnd[1]).trim();
            if (!ServerUtils.extractValue(tokens[1], tokens[2], checkBoxCode, startEnd, clientState)) {
                return;
            }
            String label = checkBoxCode.substring(startEnd[0], startEnd[1]).trim();
            if (!ServerUtils.extractValue(tokens[2], tokens[3], checkBoxCode, startEnd, clientState)) {
                return;
            }
            String guid = checkBoxCode.substring(startEnd[0], startEnd[1]).trim();
            if (!ServerUtils.extractValue(tokens[3], tokens[4], checkBoxCode, startEnd, clientState)) {
                return;
            }
            String doMessage = checkBoxCode.substring(startEnd[0], startEnd[1]).trim();
            if (!ServerUtils.extractValue(tokens[4], tokens[5], checkBoxCode, startEnd, clientState)) {
                return;
            }
            String undoMessage = checkBoxCode.substring(startEnd[0], startEnd[1]).trim();
            if (!ServerUtils.extractValue(tokens[5], tokens[6], checkBoxCode, startEnd, clientState)) {
                return;
            }
            String title = checkBoxCode.substring(startEnd[0], startEnd[1]).trim();
            element.setAttribute("SessionEventsCheckBoxID", id);
            element.setAttribute("SessionEventsCheckBoxLabel", label);
            element.setAttribute("SessionEventsCheckBoxSessionID", guid);
            element.setAttribute("SessionEventsCheckBoxDoMessage", doMessage);
            element.setAttribute("SessionEventsCheckBoxUndoMessage", undoMessage);
            element.setAttribute("SessionEventsCheckBoxTitle", title);
            element.setAttribute("id", generateID(idCounters));
            return;
        }
        // the following is for old style micro-behaviours
        if (behaviourNameElement == null) {
            return;
        }
        try {
            String behaviourNameHTML = nodeToHTML(behaviourNameElement, clientState);
            ArrayList<MicroBehaviour> requiredMicroBehaviours = new ArrayList<MicroBehaviour>();
            String code = transformElementsInCode(element, requiredMicroBehaviours, baseURL, clientState, idCounters);
            if (code == null) {
                return; // reached this recursively
            }
            MicroBehaviour behaviour = 
                    new MicroBehaviour(behaviourNameHTML, code, urlString, this, netLogoModel, null, null, null, true);
            behaviour.setReferencedMicroBehaviours(requiredMicroBehaviours);
            rememberMicroBehaviour(behaviour, urlString);
            behaviourNameElement.setAttribute("SourceURL", urlString);
            behaviourNameElement.setAttribute("behaviourname", CommonUtils.encode(behaviourNameHTML));
            behaviourNameElement.setAttribute("MicroBehaviour", "1");
            behaviourNameElement.setAttribute("id", generateID(idCounters));
            behaviourNameElement = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String transformElementsInCode(
            Element element, 
            ArrayList<MicroBehaviour> requiredMicroBehaviours, 
            String baseURL, 
            ClientState clientState, 
            int idCounters[]) {
        NodeList nodes = element.getChildNodes();
        int count = nodes.getLength();
        if (count > 0) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < count; i++) {
                Node node = nodes.item(i);
                String nodeName = node.getNodeName();
                if (node instanceof Element) {
                    Element childElement = (Element) node; 
                    String tagName = childElement.getTagName();
                    if (tagName.equalsIgnoreCase("br")) {
                        // adding the new line (\n) fixes issue 140
                        result.append("\n");
                    } else if (tagName.equalsIgnoreCase("TEXTAREA")) {
                        String indexString = childElement.getAttribute("index");
                        if (indexString.isEmpty()) {
                            result.append(ServerUtils.textAreaPlaceHolder(idCounters[2]++));
                        } else {
                            try {
                                int index = Integer.parseInt(indexString);
                                result.append(ServerUtils.textAreaPlaceHolder(index));
                            } catch (NumberFormatException e) {
                                Logger.getLogger(RESOURCE_SERVICE_LOGGER_NAME).warning(
                                        indexString + " is not an integer (in transformElementsInCode)");
                                result.append(ServerUtils.textAreaPlaceHolder(idCounters[2]++));
                            }    
                        }
                        result.append("\n");
                    } else { // at this point is a SPAN rather than an A element
                        String url = childElement.getAttribute("hyperlink");
                        if (url.equals("")) {
                            String transformedCode = transformElementsInCode(childElement, requiredMicroBehaviours, baseURL, 
                                    clientState, idCounters);
                            result.append(transformedCode);
                        } else if (currentLineIsIncompleteStringOrComment(result.toString())) {
                            String text = node.getTextContent();
                            result.append(text);
                        } else if (url.startsWith("javascript:")) {
                            ServerUtils.logError("URL not supported: " + url + " found inside: " + baseURL);
                        } else {
                            MicroBehaviour microBehaviour = getMicroBehaviourFromCache(url);
                            if (microBehaviour != getDummyMicroBehaviour()) {
                                // not recursively working on this already
                                if ( microBehaviour == null) {
                                    microBehaviour = getMicroBehaviourFromCacheOrDatabase(url, clientState);
                                }
                                if (microBehaviour == null) { // is new
                                    // this doesn't get removed even if microBehaviourLifeTime passes
                                    microBehaviour = getDummyMicroBehaviour();
                                    rememberMicroBehaviour(microBehaviour, url);
                                    String error = fetchAndCreateMicroBehaviours(url, baseURL, clientState);
                                    if (error != null) {
                                        clientState.warn(error);
                                        removeMicroBehaviourCache(url);
                                    } else {
                                        microBehaviour = getMicroBehaviourFromCacheOrDatabase(url, clientState);		    
                                    }
                                } 
                                if (microBehaviour != getDummyMicroBehaviour()) {
                                    generateReferenceToBehaviour(microBehaviour, url, result, requiredMicroBehaviours, clientState);
                                }
                            } else {
                                addConvertedURLToNetLogoCode(microBehaviour, url, result);
                            }
                        }
                        if (nodeName.equalsIgnoreCase("pre") || nodeName.equalsIgnoreCase("p") || nodeName.equalsIgnoreCase("div")) { 
                            // these cause a new line (others??)
                            result.append("\n");
                        }
                    }
                } else {
                    String text = node.getTextContent();
                    if (!text.isEmpty()) {
                        result.append(text);
                        if (!currentLineIsIncompleteStringOrComment(text)) {
                            // don't introduce a new line within a string or comment
                            result.append("\n");
                        }
                        String[] substitutions = text.split(CommonUtils.SUBSTITUTE_TEXT_AREA_FOR);
                        if (substitutions.length > 1) {
                            for (String substitution : substitutions) {
                                int splitCount = 3;
                                if (substitution.contains(" columns: ")) {
                                    splitCount += 2;
                                }
                                if (substitution.contains(" rows: ")) {
                                    splitCount += 2;
                                }
                                String[] nameAndValue = substitution.split("(\\s)+", splitCount);
                                if (nameAndValue.length > 2) {
                                    int valueIndex = 0;
                                    if (nameAndValue[0].isEmpty()) {
                                        valueIndex++;
                                    }
                                    if (nameAndValue[valueIndex].equals("columns:")) {
                                        valueIndex += 2; // skip number of columns as well
                                    }
                                    if (nameAndValue.length > valueIndex+2) {
                                        if (nameAndValue[valueIndex].equals("rows:")) {
                                            valueIndex += 2; // skip number of rows as well
                                        }
                                    }
                                    valueIndex++; // skip the name as well
                                    String defaultValue = nameAndValue[valueIndex];
                                    int indexOfEndOfLine = defaultValue.indexOf('\n');
                                    if (indexOfEndOfLine > 0) {
                                        // remove extra info (e.g. the name)
                                        defaultValue = defaultValue.substring(0, indexOfEndOfLine);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return result.toString();
        } else {
            return element.getTextContent();
        }
    }

    protected void generateReferenceToBehaviour(MicroBehaviour microBehaviour, String url, StringBuilder result,
            ArrayList<MicroBehaviour> requiredMicroBehaviours, ClientState clientState) {
        if (microBehaviour != null) {
            if (!microBehaviour.isRawNetLogoCode()) {
                addConvertedURLToNetLogoCode(microBehaviour, url, result);
            }
            requiredMicroBehaviours.add(microBehaviour);
        } else {
            clientState.warn("Unable to load micro behaviour from: " + url);
        }
    }

    protected void addConvertedURLToNetLogoCode(MicroBehaviour microBehaviour, String url, StringBuilder result) {
        // no longer need to quote these
        //	result.append(" \"");
        result.append(microBehaviour.getNetLogoName());
        result.append(' ');
        //	result.append("\" ");
        //	// add a comment with the user friendly name (add semi-colons to each new line)
        //	String behaviourDescription = microBehaviour.getBehaviourDescription();
        //	if (behaviourDescription != null) {
        //	    result.append("; " + behaviourDescription.replaceAll("\n", "\n;") + "\n");
        //	}
    }

    protected boolean currentLineIsIncompleteStringOrComment(String code) {
        // returns true if last line contains an odd number of quotes
        // currently doesn't bother to see if the quote itself is back quoted (is that how NetLogo does things?)
        String lines[] = code.split("\n");
        String lastLine;
        if (lines.length > 0) {
            lastLine = lines[lines.length-1];
        } else {
            lastLine = code;
        }
        if (lastLine.indexOf(';') >= 0) {
            return true;
        }
        String parts[] = lastLine.split("\"");
        return parts.length%2 == 0;
    }

    protected void addAttributesToMoPiXElements(Element element, String attribute, int idCounters[]) {
        String attributeValue = element.getAttribute(attribute);
        if (attributeValue.length() > 0) {
            element.setAttribute("id", generateID(idCounters));
            // originally also added mopix='1' but I don't think that is used
        }
    }

    protected void expandURLs(String attribute, Element element, String urlString, String baseURL) {
        String attributeName = element.getAttribute(attribute);
        if (attributeName.length() > 0) {
            String expandedURL = expandURL(attributeName, urlString, baseURL);
            if (expandedURL != urlString) {
                // actually expanded it
                element.setAttribute(attribute, expandedURL);
            }
        }
    }

    protected String expandURL(String urlPath, String urlString, String baseURL) {
        if (urlPath.charAt(0) == '#') {
            // don't expand links to anchors
            return urlPath;
        }
        String tabURL = CommonUtils.getURLParameter("tab", urlPath);
        if (tabURL != null) {
            try {
                urlPath = URLDecoder.decode(tabURL, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.severe("Unable to decode the tab URL: " + tabURL);
                e.printStackTrace();
            }
        }
        if (urlPath.indexOf("//") < 0) { 
            if (urlPath.charAt(0) == '/') {
                return CommonUtils.joinPaths(CommonUtils.extractServer(urlString), urlPath);
            } else {
                // relative path so make it full path
                return CommonUtils.joinPaths(baseURL, urlPath);
            }
        }
        return urlPath;
    }

    protected void renumberIDs(Element element, int idCounters[]) {
        String id = element.getAttribute("id");
        if (id.length() > 0 && id.startsWith(CommonUtils.MODELLER_ID_PREFIX)) {
            element.setAttribute("id", generateID(idCounters));
        }
    }

    public static String generateID(int[] idCounters) {
        return CommonUtils.MODELLER_ID_PREFIX + idCounters[1] + "_" + idCounters[0]++;
    }

    protected void replaceHREFs(Element element, Document document, String urlString, String baseURL, int idCounters[]) {
        String href = element.getAttribute("href");
        boolean anchorTag = element.getTagName().equals("A");
        if (href.length() > 0) {
            char firstCharacter = href.charAt(0);
            if (anchorTag) {
                String target = element.getAttribute("target");
                boolean hrefIsServlet = href.contains(CommonUtils.M4A_MODEL_URL_PARAMETER + "=1") ||
                        // in case some of these old tags are out there
                        href.contains(CommonUtils.OLD_M4A_MODEL_URL_PARAMETER + "=1");
                if (hrefIsServlet || (!target.equalsIgnoreCase("_blank") && !href.startsWith("mailto:"))) {
                    // not a new window target that we can leave alone
                    List<String> linksToAreas = Arrays.asList(
                            "#_Resources_", "#_BehaviourComposer_", "#_History_", "#_Models_", "#_Settings_", "#_Help_", "#_Search_");
                    if (firstCharacter != '#' || linksToAreas.contains(href)) {
                        // if local anchor link leave it alone
                        // need to change it into something the browser won't also respond to
                        Element spanElement = copyAsNewElement("SPAN", element, document);
                        spanElement.setAttribute("id", generateID(idCounters));
                        boolean isAHyperlink = true; // unless discovered to be otherwise
                        if (hrefIsServlet) {
                            int argumentsPosition = href.indexOf('?');
                            if (argumentsPosition > 0) {
                                String urlAttributes = href.substring(argumentsPosition);
                                String modelGuid = CommonUtils.urlAttributeWithoutURLDecoding(urlAttributes, "frozen");
                                if (modelGuid == null) { // try again with old name
                                    modelGuid = CommonUtils.urlAttributeWithoutURLDecoding(urlAttributes, "model");
                                }
                                if (modelGuid != null) {
                                    isAHyperlink = false;
                                    spanElement.setAttribute("modelRef", modelGuid);
                                } else {
                                    String sessionID = CommonUtils.urlAttributeWithoutURLDecoding(urlAttributes, "share");
                                    if (sessionID == null) { // try again with older name
                                        sessionID = CommonUtils.urlAttributeWithoutURLDecoding(urlAttributes, "session");
                                    }
                                    if (sessionID != null) {
                                        isAHyperlink = false;
                                        spanElement.setAttribute("sessionRef", sessionID);
                                    } else {
                                        sessionID = CommonUtils.urlAttributeWithoutURLDecoding(urlAttributes, "copy");
                                        if (sessionID != null) {
                                            isAHyperlink = false;
                                            spanElement.setAttribute("sessionRefReadOnly", sessionID);
                                        }
                                    }
                                }
                            }
                        }
                        if (isAHyperlink) {
                            href = expandURL(href, urlString, baseURL);
                            spanElement.setAttribute("hyperlink", href);
                        }
                        element.getParentNode().replaceChild(spanElement, element);
                    }
                }
            } else if (firstCharacter == '/' && href.charAt(1) != '/') {
                element.setAttribute("href", href + CommonUtils.extractServer(urlString));
            }
        } else if (anchorTag) {
            // no href so presumably is a local anchor target
            String name = element.getAttribute("name");
            if (name != null && !name.isEmpty()) {
                element.setAttribute("id", name + "-M4ATargetID");
            }	    
        }
    }

    protected void addAttributesifFORM(Element element, Document document, String urlString, int idCounters[]) {
        if (element.getTagName().equals("FORM")) {
            // need to change the element into something the browser won't also respond to
            String action = element.getAttribute("action");
            String method = element.getAttribute("method");
            Element spanElement = copyAsNewElement("SPAN", element, document);
            spanElement.setAttribute("id", generateID(idCounters));
            if (action.equals("")) {
                return;
            }
            spanElement.setAttribute("formaction", action);
            spanElement.setAttribute("actionurl", CommonUtils.joinPaths(CommonUtils.extractServer(urlString), action));    
            if (method.length() > 0) {
                spanElement.setAttribute("method", method);
            }
            element.getParentNode().replaceChild(spanElement, element);
        }
    }

    protected void addAttributesifTEXTAREA(Element element, Document document, String urlString, int idCounters[]) {
        if (element.getTagName().equalsIgnoreCase("TEXTAREA")) {
            element.setAttribute("id", generateID(idCounters));
            String indexString = Integer.toString(idCounters[2]++);
            element.setAttribute("index", indexString);
        }
    }

    protected Element copyAsNewElement(String tagName, Element element, Document document) {
        Element newElement = document.createElement(tagName);
        NodeList nodes = element.getChildNodes();
        Node previousNode = null;
        int nodeCount = nodes.getLength();
        for (int i = nodeCount-1; i >= 0 ; i--) {
            // count down since this removes the children from element
            Node node = nodes.item(i);
            newElement.insertBefore(node, previousNode);
            previousNode = node;
        }
        //	// attributes too
        //	NamedNodeMap attributes = element.getAttributes();
        //	int attributeCount = attributes.getLength();
        //	for (int i = 0; i < attributeCount; i++) {
        //	    Node attribute = attributes.item(i);
        //	    String nodeValue = attribute.getNodeValue();
        //	    String textContent = attribute.getTextContent();
        //	    newElement.setAttribute(nodeValue, textContent);
        //	}
        return newElement;
    }

    protected String fetchPage(String urlString, ClientState clientState) {
        try {
            Node node = fetchPageDocument(urlString, clientState, null);
            if (node != null) {
                return nodeToHTML(node, clientState);
            } else {
                return "Error reading the file: " + urlString;
            }
            //	} catch (FileNotFoundException e) {
            //	    return "Error the file was not found: " + urlString;
        } catch (Exception e) {    
            return ServerUtils.logException(e, "fetching the contents of " + urlString);
        }
    }

    public static String nodeToHTMLWithExceptions(Node node) throws Exception {
        // see comment 7 in http://code.google.com/p/appengine-mapreduce/issues/detail?id=46
        System.setProperty(
                "javax.xml.transform.TransformerFactory", 
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    public static String nodeToHTML(Node node, ClientState clientState) {
        // caller should know that there won't be any exceptions
        try {
            return nodeToHTMLWithExceptions(node);
        } catch (Exception e) {
            clientState.warn("Returning no HTML due to an error transforming a node to HTML:");
            e.printStackTrace();
            return "";
        }
    }

    protected Document fetchPageDocument(String urlString, ClientState clientState, String[] answer)  {
        return fetchPageDocument(urlString, clientState, true, answer);
    }

    protected Document fetchPageDocument(String urlString, ClientState clientState, boolean firstTime, String[] answer)  {
        String htmlString = fetchPageString(urlString, clientState, firstTime, answer);
        if (htmlString == null) {
            return null;
        }
        try {
            return getDocument(htmlString);
        } catch (Exception e) {
            clientState.logException(e, "In fetchPageDocument of \"" + urlString + "\"");
            return null;
        }
    }

    @Override
    public String cacheURLs(String urls[], String sessionGuid, String userGuid) {
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), userGuid, true, true);
        for (String url : urls) {
            fetchPageString(url, clientState, true, null);
        }
        // no errors to report
        return null;
    }

    @Override
    public String[] fetchURLContents(String urlString) {
        // returns String[2] where [0] is the contents of the url
        // and [1] is any warnings or error messages to be sent to the client
        String result[] = new String[2];
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            // set timeout to GAE maximum (30 seconds)
            // while some URLs are in the URLFileCache
            // there are also the URLs used in the 'copy' URL parameter
            connection.setConnectTimeout(30000); 
            connection.setReadTimeout(30000);
            // following fixes Error 403 from HTTP
            String userAgent = getThreadLocalRequest().getHeader("user-agent");
            connection.setRequestProperty("User-Agent", userAgent);
            InputStream inputStream = connection.getInputStream();
            result[0] = inputStreamToString(inputStream);
            if (result[0].contains("404 Not Found")) {
                // should be caught below and cache used
                throw new Exception("404 Not found returned from " + urlString);		
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Error while trying to fetch " + urlString + ". " + e.getMessage();
            result[1] = message;
        }
        return result;
    }

    protected String inputStreamToString(InputStream inputStream)
            throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder contents = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            contents.append(line + "\r");
        }
        in.close();
        return contents.toString();
    }

    protected String fetchPageString(String urlString, ClientState clientState, boolean firstTime, String[] answer)  {
        if (urlString.startsWith(CommonUtils.TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN)) {
            // shouldn't really be called to fetch a prototype acting like a list of micro-behaviours
            return null;
        }
        urlString = urlString.trim(); // ignore initial or trailing spaces
        if (urlString.isEmpty()) {
            logger.warning("fetchPageString called with an empty string for a URL");
            return null;
        }
        if (urlString.charAt(0) == '-') { // inactivated MB URL
            urlString = urlString.substring(1);
        }
        InputStreamReader reader = null;
        String start = CommonUtils.getStaticPagePath();
        boolean shouldBeInDataStore = CommonUtils.removeBookmark(urlString).endsWith(CommonUtils.EDITED_HTML);
        boolean shouldBeInResourceArchive = urlString.startsWith(start);
        int fileNameStart = 0; 
        if (shouldBeInResourceArchive) {
            fileNameStart = start.length();
        } else {
            int index = urlString.indexOf("m4a-gae.appspot.com/p/");
            shouldBeInResourceArchive = index >= 0;
            if (shouldBeInResourceArchive) {
                fileNameStart = index+"m4a-gae.appspot.com/p/".length();
            }
        }
        if (shouldBeInDataStore) {
            int extensionIndex = urlString.indexOf(CommonUtils.EDITED_HTML);
            if (extensionIndex >= 0) {
                // e.g. http://127.0.0.1:8888/p/IXgHKuB7QVleNW8lFoqj6b.edited.html
                int lastSlashIndex = urlString.lastIndexOf('/');
                final String pageGuid = urlString.substring(lastSlashIndex+1, extensionIndex);
                EditedPage editedPage = null;
                try {
                    editedPage = getEditedPage(pageGuid);
                } catch (JDOObjectNotFoundException e) {
                    RemoteAPI freeRemoteAPI = ServerUtils.getFreeRemoteAPI(getThreadLocalRequest());
                    if (freeRemoteAPI != null) {
                        RunOnProductionGAECallback<EditedPage> callback = new RunOnProductionGAECallback<EditedPage>() {

                            @Override
                            public EditedPage execute() {
                                return getEditedPage(pageGuid);
                            }

                        };
                        editedPage = freeRemoteAPI.runOnProductionGAE(callback);
                        if (editedPage != null && clientState.isCachingEnabled()) {
                            EditedPage localEditedPage = new EditedPage(CACHED + editedPage.getUrl(), 
                                    editedPage.getOldURL(), 
                                    editedPage.getContents(), 
                                    editedPage.getSessionGuid(), 
                                    editedPage.getTimeStamp());
                            ServerUtils.persist(localEditedPage);
                        }
                    } else {
                        editedPage = getEditedPage(CACHED + pageGuid);
                    }
                }
                if (editedPage != null) {
                    if (answer != null) {
                        answer[7] = Boolean.toString(editedPage.isReadOnly());
                    }
                    return editedPage.getContents();
                }
            }
            shouldBeInResourceArchive = false;
        } else if (!shouldBeInResourceArchive) {
            if (urlString.startsWith("http://m4a-gae.appspot.com/p/") && start.equals("http://m.modelling4all.org/p/")) {
                // m.modelling4all.org redirects to http://m4a-gae.appspot.com
                shouldBeInResourceArchive = true;
                start = "http://m4a-gae.appspot.com/p/";
            } else if (start.indexOf("m4a-gae.appspot.com/p") > 0 && urlString.indexOf("m4a-gae.appspot.com/p") > 0) {
                // support testing of non-default releases --- nnn.m4a-gae.appspot.com
                String newUrlString = CommonUtils.ignoreReleaseVersionNumber(urlString);
                logger.warning("Substituting " + newUrlString + " for " +  urlString);
                urlString = newUrlString;
                shouldBeInResourceArchive = true;
                start = "http://m.modelling4all.org/p/";
            }
        }
        if (shouldBeInResourceArchive) {
            String fileName = CommonUtils.removeBookmark(urlString.substring(fileNameStart));
            InputStream inputStream = 
                    ServerUtils.getInputStreamFromResourceJar(fileName, getClass(), getResourceArchiveFileName());
            if (inputStream == null) {
                // work around to MoPiX release where sometimes an extra p/ was generated 
                // perhaps no longer needed
                if (fileName.startsWith("p/")) {
                    fileName = fileName.substring(2);
                    inputStream = 
                            ServerUtils.getInputStreamFromResourceJar(fileName, getClass(), getResourceArchiveFileName());
                }
            }
            if (inputStream != null) {
                reader = new InputStreamReader(inputStream);
            } else {
                logger.severe("Could not find file in jar file: " + fileName + " start: " + start + " urlString: " + urlString);
            }
        }
        if (reader == null) {
            if (clientState.isInternetAccess()) {
                // urlString should be a full (not relative) path
                try {
                    //		    urlString = URLEncoder.encode(urlString, "UTF-8");
                    // above didn't work -- should really only encode the part after the domain
                    urlString = CommonUtils.removeBookmark(urlString.replace(" ", "%20"));
                    URL url = new URL(CommonUtils.ignoreReleaseVersionNumber(urlString));
                    URLConnection connection = url.openConnection();
                    // following fixes Error 403 from HTTP
                    connection.setRequestProperty("User-Agent", clientState.getAgentDescription());
                    connection.setConnectTimeout(ServerUtils.URL_FETCH_TIMEOUT);
                    connection.setReadTimeout(ServerUtils.URL_FETCH_TIMEOUT);
                    reader = new InputStreamReader(connection.getInputStream(), "UTF-8");
                } catch (Exception e) {
                    if (firstTime && !urlString.endsWith("null")) {
                        // no point wasting time on such URLs (seen in logs)
                        if (urlString.startsWith("https:")) {
                            // perhaps the problem is that access is not authorised as https but the same content is available without SSL
                            urlString =  urlString.replace("https:", "http:");
                        }
                        // try once more
                        String message = "Failed to fetch " + urlString + ". Trying again at " + System.currentTimeMillis();
                        Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).log(Level.WARNING, message);
                        String result = fetchPageString(urlString, clientState, false, null);
                        if (result == null) {
                            message = "Failed again to fetch " + urlString + ". At " + System.currentTimeMillis();
                        } else {
                            message = "Second attempt succeeded to fetch " + urlString + " at " +  + System.currentTimeMillis();
                        }
                        Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).log(Level.WARNING, message);
                        return result;
                    }
                    if (ServerUtils.runningLocalHost(getThreadLocalRequest())) {
                        clientState.setInternetAccess(false);
                    }
                    clientState.logException(
                            e, 
                            "In fetchPageDocument of \"" + urlString + "\"" +
                                    (shouldBeInResourceArchive ? " Also checked in the resource archive." : " Did not check resource archive since did not start with: " + start));
                    return null;
                }
            }
        }
        if (reader != null) {
            BufferedReader in = new BufferedReader(reader);
            // commented out since need to ignore stuff before and after special tokens
            //	Document result = getDocument(new InputSource(in));
            String line;
            String originalLine; // for error messages
            StringBuffer html = new StringBuffer();
            boolean sectionStarted = false;
            // if true (e.g. an error page returned) then return the body to the client
            boolean returnRawBody = false;
            boolean bodyEncountered = false;
            try {
                while ((line = in.readLine()) != null) {
                    originalLine = line;
                    line = CommonUtils.replaceNonBreakingSpaces(line);
                    if (line.contains(REDIRECT_TOKEN)) {
                        line = CommonUtils.removeHTMLMarkup(line.trim());
                        while (line != null) {
                            int urlStartIndex = line.indexOf("http");
                            if (urlStartIndex >= 0) {
                                int urlEndIndex = line.indexOf("//s", urlStartIndex);
                                if (urlEndIndex < 0) {
                                    urlEndIndex = line.length();
                                }
                                line = line.substring(urlStartIndex, urlEndIndex);
                                break;
                            }
                            line = in.readLine();
                            if (line != null) {
                                line = CommonUtils.removeHTMLMarkup(line.trim()); 
                            }    
                        }
                        if (!CommonUtils.isAbsoluteURL(line)) {
                            logger.severe("Did not find a URL after this: " + REDIRECT_TOKEN);
                        } else {
                            String fetchPageString = fetchPageString(line, clientState, firstTime, null);
                            if (fetchPageString == null) {
                                logger.warning("Did not find any contents of the redirection: " + originalLine);
                            }
                            return fetchPageString;
                        }
                    }
                    if (line.contains("<TITLE>Error 5") || 
                            line.contains("<title>Error 5") ||
                            line.contains("<title>Page not found")) {
                        returnRawBody = true;
                        // ignore all the rest
                        html = new StringBuffer();
                        continue;
                    }
                    if (bodyEncountered) {
                        html.append(line);
                        if (line.contains("</body>") || line.contains("</BODY>")) {
                            break;
                        } else {
                            continue;
                        }
                    }
                    if (returnRawBody) {
                        if (line.contains("<body ") || line.contains("<BODY>")) {
                            bodyEncountered = true;
                            html.append(line);
                        }
                        continue;
                    }
                    if (sectionStarted) {
                        int endIndex = line.indexOf(END_PAGE_SECTION);
                        if (endIndex < 0) {
                            if (html.length() == 0) {
                                // following deals with the fact that the BEGIN_PAGE_SECTION
                                // could be inside an HTML element, e.g. <pre>...</pre>
                                line = CommonUtils.stripAwayInitialClosingHTMLElements(line);
                            }
                            if (!line.isEmpty()) {
                                html.append(line);
                                html.append("\r");
                            }
                        } else if (endIndex == 0) {
                            break; // ignore the rest			
                        } else {
                            html.append(CommonUtils.removeFinalOpeningTags(line.substring(0, endIndex)));
                            break; // ignore the rest
                        }
                    } else {
                        int startIndex = line.indexOf(BEGIN_PAGE_SECTION);
                        if (startIndex < 0) {
                            html.append(line);
                            html.append("\r");
                        } else {
                            sectionStarted = true;
                            String remainderOfLine = line.substring(startIndex+BEGIN_PAGE_SECTION.length());
                            int endIndex = remainderOfLine.indexOf(END_PAGE_SECTION);
                            if (endIndex >= 0) {
                                remainderOfLine = CommonUtils.removeFinalOpeningTags(remainderOfLine.substring(0, endIndex));
                            }
                            html = new StringBuffer(remainderOfLine); // throw away the stuff above
                            html.append("\r");
                            //		            html.append(line.substring(startIndex + BEGIN_PAGE_SECTION.length()));
                        }
                    }
                }
                in.close(); // not clear if needed
                String htmlString = html.toString();
                if (clientState.isCachingEnabled()) {
                    // cache with url as key (or hash of url in case too long?)
                    URLContents urlContents = new URLContents(urlString, htmlString);
                    ServerUtils.persist(urlContents);
                }
                return htmlString;
            } catch (IOException e) {
                clientState.logException(e, "In fetchPageDocument of \"" + urlString + "\"");
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // see if cached
            URLContents urlContents = ServerUtils.getObjectById(URLContents.class, CommonUtils.removeBookmark(urlString));
            if (urlContents != null) {
                return urlContents.getContents();
            }   
        }
        return null;
    }

    protected EditedPage getEditedPage(String pageGuid) {
        return ServerUtils.getObjectById(EditedPage.class, pageGuid);
    }

    protected String getResourceArchiveFileName() {
        return ServerUtils.STATIC_RESOURCES_ZIP;
    }

    public static Document getDocument(InputSource inputSource) throws SAXException, IOException {
        DOMFragmentParser parser = new DOMFragmentParser();
        // not sure the following 2 lines accomplish anything
        parser.setFeature("http://cyberneko.org/html/features/insert-namespaces", true);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        HTMLDocument document = new HTMLDocumentImpl();
        DocumentFragment fragment = document.createDocumentFragment();
        parser.parse(inputSource, fragment);
        NodeList nodes = fragment.getChildNodes();
        //	ArrayList<Node> reversedNodes = new ArrayList<Node>();
        Element htmlElement = document.getDocumentElement();
        int length = nodes.getLength();
        if (length == 1) {
            htmlElement.appendChild(nodes.item(0));
        } else {
            // have to go thru the nodes backwards because transferring them
            // removes them from their parent (fragment)
            // and need to assemble them in the right order
            Node previousNode = null;
            for (int i = length-1; i >= 0; i--) {
                Node node = nodes.item(i);
                if (previousNode == null) {
                    htmlElement.appendChild(node);
                } else {
                    htmlElement.insertBefore(node, previousNode);
                }
                previousNode = node;
            }
        }
        return document;
    }

    public static Document getDocument(String html) throws SAXException, IOException {
        StringReader stringReader = new StringReader(html);
        return getDocument(new InputSource(stringReader));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
        //	// called by submitting a form in the client
        //	String newURL = null;
        //	String query = request.getQueryString();
        //	if (query == null) {
        //	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).warning(
        //		    "Received a null query from a form. Ignoring it.");
        //	    return;
        //	}
        //	String modelName = request.getParameter("trace");
        //	if (modelName != null) {
        //	    String netLogoCommand = request.getParameter("commands");
        //	    String trace = null;
        //	    if (netLogoCommand != null && netLogoCommand.equals("start")) {
        //		// if restarting so throw away remaining trace
        //		netLogoTracesRemaining.remove(modelName); // in case
        //	    } else {
        //		trace = netLogoTracesRemaining.get(modelName);
        //	    }
        //	    int traceLength;
        //	    if (trace != null) {
        //		traceLength = trace.length();
        //		if (traceLength < 2048) { // last piece
        //		    trace = "|" + trace; // mark it as last piece
        //		    traceLength++;
        //		}
        //	    } else {
        ////		String serverName = request.getServerName();
        ////		boolean runningLocal = serverName.equals("localhost");
        ////		String fileName = ServerUtils.getFileName(CommonUtils.staticPageFolder(), modelName, "nlogo"); 
        ////		trace = nextVisualTrace(fileName, netLogoCommand);
        ////		traceLength = trace.length();
        //		Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe("Trace (for Second Life) no longer fully implemented.");
        //	    }
        //	    // Second Life only supports HTTP responses with a maximum length of 2048 
        ////	    if (traceLength > 2048) {
        ////		// need to break it up
        ////		netLogoTracesRemaining.put(modelName, trace.substring(2047));
        ////		trace = "+" + trace.substring(0, 2047);
        ////		traceLength = 2048;
        ////	    } else {
        ////		netLogoTracesRemaining.remove(modelName);
        ////	    }
        ////	    response.setContentType("text/html;charset=UTF-8");
        ////	    response.setContentLength(traceLength);
        //	    PrintWriter writer = response.getWriter();
        //	    writer.write(trace);
        //	    writer.close();
        //	    return;
        //	}
        //	StringBuilder queryBuffer = new StringBuilder(query);
        //	// following is a simpler way to do this and target but
        //	// doesn't remove attribute from query
        ////	String newURL = CommonUtils.urlAttribute(query, "actionURL");
        //	int actionStart = query.indexOf("actionURL=");
        //	if (actionStart >= 0) {
        //	    int actionEnd = query.indexOf("&",actionStart);
        //	    if (actionEnd < 0) {
        //		actionEnd = query.length();
        //	    }
        //	    newURL = query.substring(actionStart + "actionURL=".length(), actionEnd);
        //	    newURL = URLDecoder.decode(newURL, "UTF-8");
        //	    queryBuffer.replace(actionStart, actionEnd, "");
        //	} else {
        //	    int targetStart = query.indexOf("target=");
        //	    if (targetStart >= 0) {
        //		int targetEnd = query.indexOf("&",targetStart);
        //		if (targetEnd < 0) {
        //		    targetEnd = query.length();
        //		}
        //		String target = query.substring(targetStart + "target=".length(), targetEnd);
        //		queryBuffer.replace(targetStart, targetEnd, "");
        //		int baseURLStart = query.indexOf("baseURL=");
        //		if (baseURLStart >= 0) {
        //		    int baseURLEnd = query.indexOf("&",baseURLStart);
        //		    if (baseURLEnd < 0) {
        //			baseURLEnd = query.length();
        //		    }
        //		    String baseURL = query.substring(baseURLStart + "baseURL=".length(), baseURLEnd);
        //		    queryBuffer.replace(baseURLStart, baseURLEnd, "");
        //		    baseURL = URLDecoder.decode(baseURL, "UTF-8");
        //		    newURL = CommonUtils.joinPaths(baseURL, target);
        //		}
        //	    }
        //	}
        //	if (newURL != null) {
        //	    int ampersandLocation = 0;
        //	    while ((ampersandLocation = queryBuffer.indexOf("&&", ampersandLocation)) >= 0) {
        //		queryBuffer.deleteCharAt(ampersandLocation);
        //	    }
        //	    if (queryBuffer.charAt(0) == '&') {
        //		// delete the ampersand since not need after question mark
        //		queryBuffer.deleteCharAt(0);
        //	    }
        //	    java.io.PrintWriter writer = response.getWriter();
        //	    writer.print(newURL + "?" + queryBuffer.toString());
        //	    writer.close();
        //	} else {
        //	    response.setContentType("text/html;charset=UTF-8");
        //	    String html = "<html><body>Unable to respond to " + request.getServletPath()  + "/" + request.getQueryString() + "</body></html>";
        //	    response.setContentLength(html.length());
        //	    PrintWriter writer = response.getWriter();
        //	    writer.write(html);
        //	    writer.close();
        //	}
    }

    protected MicroBehaviour getMicroBehaviour(String urlString, 
            String baseURL, 
            ClientState clientState, 
            int idCounters[], 
            MacroBehaviour containingMacroBehaviour,
            boolean useCache) {
        // TODO: remove useCache since sharing is now session-specific so no danger
        boolean inactivated = urlString.charAt(0) == '-';
        if (inactivated) {
            urlString = urlString.substring(1);
        }
        String[] split = urlString.split("\\d+m4a-gae.appspot.com", 2);
        if (split.length == 2) {
            urlString = "http://m4a-gae.appspot.com" + split[1];
        }
        MicroBehaviour microBehaviour = getMicroBehaviourFromCache(urlString);
        if (microBehaviour == null || microBehaviour == getDummyMicroBehaviour()) {
            microBehaviour = getMicroBehaviourFromDatabase(urlString, clientState);
            if (microBehaviour != null) {
                return microBehaviour;
            }
            String error = 
                    fetchAndCreateMicroBehaviours(urlString, baseURL, containingMacroBehaviour, clientState);
            if (error != null) {
                clientState.warn(error);
                return null;		
            }
            microBehaviour = getMicroBehaviourFromCacheOrDatabase(urlString, clientState);
        }
        if (microBehaviour == null) {
            clientState.warn("In getMicroBehaviour. Unable to load micro behaviour from url: " + urlString);
        }
        return microBehaviour;
    }

    public MicroBehaviour getMicroBehaviourFromCacheOrDatabase(String urlString, ClientState clientState) {
        if (urlString == null) {
            return null;
        }
        if (urlString.charAt(0) == '-') {
            // is an inactivated micro-behaviour
            urlString = urlString.substring(1);
        }
        MicroBehaviour microBehaviour = microBehaviourURLTable.get(urlString);
        if (microBehaviour == null) {
            microBehaviour = getMicroBehaviourFromDatabase(urlString, clientState);
            if (microBehaviour != null) {
                rememberMicroBehaviour(microBehaviour, urlString);
            }
        }
        if (microBehaviour == null && urlString.indexOf("m4a-gae.appspot.com") >= 0) {
            // work despite GAE version number
            return getMicroBehaviourFromCacheOrDatabase(CommonUtils.ignoreReleaseVersionNumber(urlString), clientState);

        }
        return microBehaviour;
    }

    public MicroBehaviour getMicroBehaviourFromDatabase(final String urlString, final ClientState clientState) {
        MicroBehaviourData microBehaviourData = MicroBehaviourData.getMicroBehaviourData(urlString);
        if (microBehaviourData != null) {
            return microBehaviourData.getMicroBehaviour(this, getThreadLocalRequest());
        }
        // following commented out until this GAE issue is resolved:
        // http://code.google.com/p/googleappengine/issues/detail?id=6349
        if (urlString.endsWith("MB.4/doc.html") || 
                urlString.endsWith("libraries/basic-library")) {
            return null;
        }
        if (urlString.startsWith("http://127.0.0.1") || urlString.startsWith("http://localhost")) {
            return null;
        }
        if (!clientState.isInternetAccess()) {
            return null;
        }
        RemoteAPI freeRemoteAPI = ServerUtils.getFreeRemoteAPI(getRequest());
        if (freeRemoteAPI == null) {
            // already remote or exception encountered
            return null;
        }
        RunOnProductionGAECallback<MicroBehaviour> callback = new RunOnProductionGAECallback<MicroBehaviour>() {

            @Override
            public MicroBehaviour execute() {
                return getMicroBehaviourFromDatabase(urlString, clientState);
            }

        };
        MicroBehaviour microBehaviour = freeRemoteAPI.runOnProductionGAE(callback);
        if (microBehaviour != null && clientState.isCachingEnabled()) { 
            microBehaviourData = microBehaviour.getMicroBehaviourData().copy();
            ServerUtils.persist(microBehaviourData);
        }
        return microBehaviour;
    }

    private MicroBehaviour getMicroBehaviourFromCache(String urlString) {
        return microBehaviourURLTable.get(urlString);
    }

    /* @return String[6] containing 
     * the unique ID, -- the GUID or a string beginning with "Error"
     * the applet width, 
     * the applet height,
     * list of URL renamings
     * the number of dimension, 
     * any warnings to communicate back
     */

    @Override
    public String[] runModel(
            String sessionGuid,
            // session guid before any 'reload without history' events
            String originalSessionGuid, 
            String userGuid, 
            String modelXML, 
            String pageTemplate,
            String hostBaseURL,
            String bc2NetLogoChannelToken,
            String bc2NetLogoOriginalSessionGuid,
            boolean cachingEnabled,
            boolean internetAccess,
            boolean useAuxiliaryFile,
            boolean forWebVersion) {
        this.hostBaseURL = hostBaseURL;
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), userGuid, cachingEnabled, internetAccess);
        CommonUtils.setHostBaseURL(hostBaseURL);
        String modelGuid = ServerUtils.generateGUIDString();
        String[] result;
        try {
            result = runModel(modelGuid, sessionGuid, originalSessionGuid, modelXML, pageTemplate, false, useAuxiliaryFile, forWebVersion, clientState);
            if (bc2NetLogoChannelToken != null) {
                ChannelService channelService = ChannelServiceFactory.getChannelService();
                String modelURL = "http://m.modelling4all.org/p/" + modelGuid + ".nlogo";
                //		System.out.println("Sending model " + modelGuid + " to " + bc2NetLogoOriginalSessionGuid); // debug this
                channelService.sendMessage(new ChannelMessage(bc2NetLogoOriginalSessionGuid, modelURL + ";" + sessionGuid));
            }
        } catch (DeadlineExceededException e) {
            result = new String[6];
            result[0] = ServerUtils.reportDeadlineExceededError(e);
            Logger.getLogger(RESOURCE_SERVICE_LOGGER_NAME).warning(result[0] + " Model " + modelGuid);
        } finally {
            ModelXML.persistModelXML(modelXML, sessionGuid, modelGuid, userGuid, pageTemplate);
        }
        return result;
    }

    public String[] reconstructModel(
            String modelGuid,
            String sessionGuid, 
            String userGuid, 
            String modelXML, 
            String pageTemplate,
            String hostBaseURL) {
        this.hostBaseURL = hostBaseURL;
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), userGuid, false, true);
        CommonUtils.setHostBaseURL(hostBaseURL); 
        return runModel(modelGuid, sessionGuid, null, modelXML, pageTemplate, true, false, false, clientState);
    }

    public String[] runModel(
            String modelGuid,
            String sessionGuid,
            String originalSessionGuid,
            String modelXML, 
            String pageTemplate,
            boolean reconstructingApplet,
            boolean useAuxiliaryFile,
            boolean forWebVersion,
            ClientState clientState) {
        long startTime = System.currentTimeMillis();
        XMLDocument xmlDocument = new XMLDocument(modelXML);
        NetLogoModel netLogoModel = new NetLogoModel(this, useAuxiliaryFile, clientState);
        Document document = xmlDocument.getDocument();
        String userGuid = clientState.getUserGuid();
        if (document != null) {
            long startTimeGeneration = System.currentTimeMillis();
            // remove any entries that are too old
            removeOldEntriesFromMicroBehaviourURLTable(startTimeGeneration - sessionCacheLifetime);
            // TODO: figure out if the following is still needed since
            // the netLogoModel was just created
            netLogoModel.resetMacroBehaviours();
            Element documentElement = document.getDocumentElement();
            if (documentElement == null) {
                logger.warning("Model xml:\n" + modelXML);
                return constructErrorAnswer("Error unable to load the model on server.");		
            }
            NodeList childNodes = documentElement.getChildNodes();
            if (constructModel(childNodes, netLogoModel)) {
                long constructTime = System.currentTimeMillis();	
                logger.info("Constructing the model " + modelGuid + " took " + 
                        (constructTime-startTimeGeneration)*.0001 + " seconds. Session GUID=" + sessionGuid + "; userGuid=" + userGuid);
                String modelInfo[] = netLogoModel.generateNLogoFile(modelGuid, infoTabContents, forWebVersion, clientState);
                logger.info("Generating the NLogo file took " + (System.currentTimeMillis()-constructTime)*.0001 + " seconds.");
                if (CommonUtils.isErrorResponse(modelInfo[0])) {
                    logger.warning("Model " + modelGuid + " failed to compile.");
                    logger.warning("Model GUID: " + modelGuid + " ;Model XML: " + modelXML);
                    if (!reconstructingApplet) {
                        ModelXML.persistModelXML(modelXML, sessionGuid, modelGuid, userGuid, pageTemplate);
                    }
                    return modelInfo;
                }
                if (!clientState.getWarningsToSendBackToClient().isEmpty()) {
                    logger.warning("Model GUID: " + modelGuid + " ;Model XML: " + modelXML);
                }
                try {
                    String template = null;
                    if (CommonUtils.isAbsoluteURL(pageTemplate)) {
                        template = fetchPageString(pageTemplate, clientState, true, null);
                    } else {
                        template = ServerUtils.getStringFromResourceJar(pageTemplate, getClass(), getResourceArchiveFileName());
                    }
                    if (template == null) {
                        modelInfo[0] = "Error. Unable to open " + pageTemplate;
                        return modelInfo;
                    }
                    String appletString =
                            "<applet\ncode='org.nlogo.lite.Applet'\nalign='baseline'\nwidth='" +
                                    (netLogoModel.getAppletWidth()+10) + 
                                    "' height='" +
                                    (netLogoModel.getAppletHeight()+10) + 
                                    "'\narchive='../netlogo/NetLogoLite.jar'>\n<param name='DefaultModel' value='" + modelGuid +
                                    ".nlogo'>\n" +
                                    "<param name='java_arguments' value='-Djnlp.packEnabled=true -Xmx1024m'>\n" +
                                    CommonUtils.NOAPPLET + "\n</applet>\n" + 
                                    // following works around a FireFox problem where the applet didn't display
                                    CommonUtils.FIREFOX_RESIZING_APPLET_JAVASCRIPT + "\n";
                    //		               CommonUtils.UNLOAD_APPLET_JAVASCRIPT + "\n";
                    template = template.replace("***replace this with the applet***", appletString);
                    if (template.indexOf("***replace this with the serial number***") >= 0) {
                        long serialNumber = generateSerialNumber(modelGuid);
                        template = template.replace("***replace this with the serial number***", Long.toString(serialNumber));
                    }
                    String modelURL = CommonUtils.getHostBaseURL() + "?frozen=" + modelGuid;
                    template = template.replace("***model_url_here***", modelURL);
                    ServerUtils.persist(new HTMLModelTemplate(modelGuid, template));
                    // make one for iFrame that is just the applet and nothing else
                    ServerUtils.persist(new HTMLModelApplet(modelGuid, appletString));
                } catch (Exception e) {
                    modelInfo[0] = "Error in generating NetLogo applet " + e.toString() + " " + modelInfo[0];
                    logger.warning("Model " + modelGuid);
                    if (!reconstructingApplet) {
                        ModelXML.persistModelXML(modelXML, sessionGuid, modelGuid, userGuid, pageTemplate);
                    }
                    return modelInfo;
                }
                ArrayList<String> microBehaviourRenamings = netLogoModel.getMicroBehaviourRenamings();
                if (!microBehaviourRenamings.isEmpty()) {
                    for (int i = 0; i < microBehaviourRenamings.size(); i += 2) {
                        modelXML = modelXML.replaceAll(microBehaviourRenamings.get(i), 
                                microBehaviourRenamings.get(i+1));
                    }
                    modelXML = modelXML.replace(" dirty='true'", "");
                }
                if (!reconstructingApplet) {
                    ModelXML.persistModelXML(modelXML, sessionGuid, modelGuid, userGuid, pageTemplate);
                    //		    if (originalSessionGuid != null) {
                    //			// can be null if recomputing model XML for example
                    //			DataStore.begin().put(new ModelToOriginalSessionGuid(modelGuid, originalSessionGuid));
                    //		    }
                }
                logger.info("Running the model took " + (System.currentTimeMillis()-startTime)*.0001 + " seconds. Guid is " + modelGuid);
                return modelInfo;
            } else {
                logger.warning("Model " + modelGuid + " could not be reconstructed.");
                if (!reconstructingApplet) {
                    ModelXML.persistModelXML(modelXML, sessionGuid, modelGuid, userGuid, pageTemplate);
                }
                return constructErrorAnswer("Error unable to re-construct the model on server.");
            }
        } else {
            String guid = ServerUtils.generateGUIDString();
            logger.warning("Model " + guid + " run model error.");
            if (!reconstructingApplet) {
                ModelXML.persistModelXML(modelXML, sessionGuid, modelGuid, userGuid, pageTemplate);
            }
            String message = "Error in runModel. ";
            if (xmlDocument != null && xmlDocument.getException() != null) {
                message += xmlDocument.getException().toString();
            }
            return constructErrorAnswer(message);
        }
    }

    private long generateSerialNumber(String guid) {
        EGMSerialNumber egmSerialNumber = new EGMSerialNumber(guid);
        ServerUtils.persist(egmSerialNumber);
        return egmSerialNumber.getSerialNumber();
    }

    public static String fetchGuidFromSerialNumber(int serialNumber) {
        EGMSerialNumber egmSerialNumber = ServerUtils.getObjectById(EGMSerialNumber.class, serialNumber);
        if (egmSerialNumber != null) {
            return egmSerialNumber.getGuid();
        } else {
            return null;
        }
    }

    @Override 
    public String fetchModel(String modelGuid, String sessionGuid, String hostPageBaseURL, boolean cachingEnabled, boolean internetAccess) {
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), cachingEnabled, internetAccess);
        CommonUtils.setHostBaseURL(hostPageBaseURL);
        String modelGuidError = CommonUtils.validateGuid(modelGuid, "the id of a stored model");
        if (modelGuidError != null) {
            String message = "Error. Ignoring the attempt to fetch a model with an invalid id. " + modelGuidError;
            ServerUtils.logError(message);
            return message;
        }
        // also validate the sessionGuid?
        return fetchModel(modelGuid, clientState);
    }

    protected String fetchModel(final String modelGuid, final ClientState clientState) {
        try {
            ModelXML modelXML = ModelXML.getModelXML(modelGuid, true);
            if (modelXML == null) {
                String modelXMLString = fetchOldModel(modelGuid, clientState);
                if (modelXMLString == null) {
                    return ServerUtils.logError("Unable to find XML for model: " + modelGuid);
                } else {
                    //TODO: store the old model on this server -- really? -- why not let the user click to run, download, or share?
                    return modelXMLString;
                }
            } else {
                return modelXML.getModelXML();
            }   
        } catch (NotFoundException e) {
            if (!clientState.isInternetAccess()) {
                return null;
            }
            RemoteAPI freeRemoteAPI = ServerUtils.getFreeRemoteAPI(getThreadLocalRequest());
            if (freeRemoteAPI == null) {
                return null;
            } else {
                RunOnProductionGAECallback<String> callback = new RunOnProductionGAECallback<String>() {

                    @Override
                    public String execute() {
                        return fetchModel(modelGuid, clientState);
                    }

                };
                return freeRemoteAPI.runOnProductionGAE(callback);
            }
        } 
    }

    private String fetchOldModel(String modelGuid, ClientState clientState) {
        return ServerUtils.urlToString("http://m.modelling4all.org/p/" + modelGuid + ".xml", clientState, false);
        // using NSMS server now but switch once it is shut down
        //	String appletFolders[] = {"applets.4.1.b2/", "applets.4.1/", "applets.2/", "applets/"};
        //	for (String appletFolder : appletFolders) {
        //	    String url = "http://modelling4all.nsms.ox.ac.uk/User/" + appletFolder + modelGuid + ".xml";
        //	    String contents = ServerUtils.urlToString(url, clientState, false);
        //	    if (contents != null && contents.startsWith("<?xml")) {
        //		return contents.replace(
        //			"http://modelling4all.nsms.ox.ac.uk/Resources/Composer/",
        //			CommonUtils.getStaticPagePath());
        //	    }
        //	}
        //	return null;
    }

    protected static String[] constructErrorAnswer(String message) {
        String answer[] = new String[3];
        answer[0] = message;
        return answer;
    }

    private boolean constructModel(NodeList nodes, NetLogoModel netLogoModel) {
        int count = nodes.getLength();
        for (int i = 0; i < count; i++) {    
            Node node = nodes.item(i);
            if (!(node instanceof Element)) {
                // any need to warn?
                continue;
            }
            Element element = (Element) node;
            String tag = element.getNodeName();
            if (tag == null) {
                return false;
            } else if (tag.equals("macrobehaviour")) {
                String name = ServerUtils.getNameFromElement(element);
                MacroBehaviour macroBehaviour = new MacroBehaviour(name, netLogoModel, this);
                // baseURL doesn't matter since using this to generated NetLogo code
                // not to transform a resource page
                boolean ok = macroBehaviour.processXMLNode(element, netLogoModel);
                if (ok) {
                    String activeString = element.getAttribute("active");
                    if ("false".equals(activeString)) {
                        macroBehaviour.setActive(false);
                    }
                    String addToModelString = element.getAttribute("addToModel");
                    if ("false".equals(addToModelString)) {
                        macroBehaviour.setAddToModel(false);
                    }
                    netLogoModel.addMacroBehaviour(macroBehaviour);
                } else {
                    return false;
                }
            } else if (tag.equals("infoTab")) {
                infoTabContents = element.getFirstChild().getNodeValue();
            } else if (tag.equals("model")) {
                return constructModel(element.getChildNodes(), netLogoModel);
            } // more??
        }
        return true;
    }

    public String deleteModel(String modelGuid, String sessionGuid) {
        PersistenceManager persistenceManager = JDO.getPersistenceManager();
        try {
            ModelXML modelXML = ModelXML.getModelXML(modelGuid, false, persistenceManager);
            if (modelXML == null) {
                return "Warning. Could not find the model " + modelGuid + " to remove it from the datastore.";
            }
            if (modelXML.getRunCount() > 1) {
                // perhaps is shared via ModelCopy -- isn't transitory if run multiple times
                return null;
            }
            if (!modelXML.getSessionGuid().equals(sessionGuid)) {
                Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).log(
                        Level.WARNING,
                        "The temporary model " + modelGuid + 
                        " cannot be deleted since it doesn't belong to the session " + sessionGuid);
            }
            try {
                // the try clause is just in case marked transient (but I think that was due to a bug fixed on 101013
                persistenceManager.deletePersistent(modelXML);
            } catch (Exception e) {
                System.err.println("Error in deleteModel of ModelXML.");
                e.printStackTrace();
            }
            deleteApplet(modelGuid, persistenceManager);    
        } catch (Exception e) {
            System.err.println("Error in deleteModel");
            e.printStackTrace();
        } finally {
            persistenceManager.close();
        }
        return null;
    }

    /**
     * @param modelGuid
     * @param persistenceManager
     */
    public static void deleteApplet(String modelGuid, PersistenceManager persistenceManager) {
        try {
            HTMLModelApplet appletHTML = 
                    persistenceManager.getObjectById(HTMLModelApplet.class, modelGuid);
            persistenceManager.deletePersistent(appletHTML);
        } catch (JDOObjectNotFoundException e) {
            // ignore
        }
        try {
            ModelNetLogo modelNetLogo = 
                    persistenceManager.getObjectById(ModelNetLogo.class, modelGuid);
            persistenceManager.deletePersistent(modelNetLogo);
        } catch (JDOObjectNotFoundException e) {
            // ignore
        }
        try {
            HTMLModelTemplate template = 
                    persistenceManager.getObjectById(HTMLModelTemplate.class, modelGuid);
            persistenceManager.deletePersistent(template);
        } catch (JDOObjectNotFoundException e) {
            // ignore
        }
    }

    //    public String checkNetLogoFile(String netLogoFileName) {
    //	String result = openNetLogoFile(netLogoFileName);
    //	disposeOfNetLogoWorkspace(netLogoFileName);
    //	return result;
    //    }
    //    
    //    public String openNetLogoFile(String netLogoFileName) {
    //	String errors = null;
    //	HeadlessWorkspace workspace = netLogoWorkspaces.get(netLogoFileName);
    //	if (workspace == null) {
    //	    long start = System.currentTimeMillis();
    //	    workspace = HeadlessWorkspace.newInstance();
    //	    long workspaceCreated = System.currentTimeMillis(); 
    //	    try {
    //		workspace.open(netLogoFileName);
    //		netLogoWorkspaces.put(netLogoFileName, workspace);
    //	    } catch (CompilerException e) {
    //		String contents = ServerUtils.fileToString(e.fileName(), true);
    //		int startPosition = e.startPos();
    //		int endPosition = e.endPos();
    //		int beforeStart = Math.max(0, contents.lastIndexOf("\rto", startPosition));
    //		int afterEnd = contents.indexOf("\rto", endPosition);
    //		if (afterEnd < endPosition) {
    //		    afterEnd = contents.length();
    //		}
    //		String before = contents.substring(beforeStart, startPosition).replace("\r", "<br>");
    //		String after = contents.substring(endPosition, afterEnd).replace("\r", "<br>");
    //		errors = e.getLocalizedMessage() + 
    //		         " in<br>" + before +
    //		         CommonUtils.stronglyHighlight(contents.substring(startPosition, endPosition)) +
    //		         after;
    //		ServerUtils.logError(errors);
    //	    } catch (Exception e) {
    //		errors = ServerUtils.logException(e, "In checkNetLogoFile ");
    //	    }
    //	    long end = System.currentTimeMillis();
    //	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).warning(
    //		    new Date().toString() + " Loading NLOGO headless took " + (workspaceCreated-start) + "ms. Total took " + (end-start) + "ms.");
    //	}
    //        return errors;
    //    }
    //
    //    private void disposeOfNetLogoWorkspace(String netLogoFileName) {
    //	HeadlessWorkspace workspace = netLogoWorkspaces.get(netLogoFileName);
    //	if (workspace == null) {
    //	    return;
    //	}
    //	try {
    //	    workspace.dispose();
    //	    netLogoWorkspaces.remove(netLogoFileName);
    //	    netLogoWorkspacesLastAccess.remove(netLogoFileName);
    //	} catch (InterruptedException e) {
    //	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe("Error disposing of NetLogo workspace for error checking.");
    //	    e.printStackTrace();
    //	}
    //    }
    //    
    //    private void disposeOfOldNetLogoWorkspaces(long now) {
    //	Set<Entry<String, Long>> entrySet = netLogoWorkspacesLastAccess.entrySet();
    //	for (Entry<String, Long> entry : entrySet) {
    //	    if (entry.getValue() + unaccessedWorkspaceLifetime < now) {
    //		entrySet.remove(entry);
    //		String netLogoModleName = entry.getKey();
    //		disposeOfNetLogoWorkspace(netLogoModleName);
    //	    } 
    //	}    
    //    }
    //    
    //    public String nextVisualTrace(String netLogoFileName, String netLogoCommand) {
    //	HeadlessWorkspace workspace = netLogoWorkspaces.get(netLogoFileName);
    //	if (workspace == null) {
    //	    String errors = openNetLogoFile(netLogoFileName);
    //	    if (errors != null) { // some errors
    //		disposeOfNetLogoWorkspace(netLogoFileName);
    //		return "Error. " + errors;
    //	    }
    //	    workspace = netLogoWorkspaces.get(netLogoFileName);
    //	    if (workspace == null) {
    //		return "Error opening " + netLogoFileName + " in NetLogo on server.";
    //	    }
    //	    try {
    //		workspace.command("start"); // not strictly necessary since LSL script does this too
    //	    } catch (CompilerException e) {
    //		return ServerUtils.logException(e, "In nextVisualTrace ");
    //	    } catch (LogoException e) {
    //		return ServerUtils.logException(e, "In nextVisualTrace ");
    //	    }
    //	}
    //	long now = System.currentTimeMillis();
    //	netLogoWorkspacesLastAccess.put(netLogoFileName, now);
    //	disposeOfOldNetLogoWorkspaces(now);
    //	if (netLogoCommand != null) {
    //	    try {
    //		workspace.command(netLogoCommand);
    //	    } catch (CompilerException e) {
    //		String message = "In nextVisualTrace while running " + netLogoCommand;
    //		return ServerUtils.logException(e, message);
    //	    } catch (LogoException e) {
    //		String message = "In nextVisualTrace while running " + netLogoCommand;
    //		return ServerUtils.logException(e, message);
    //	    }
    //	}
    //	try {
    //	    return (String) workspace.report("next-visual-state-of-all-individuals");
    //	} catch (LogoException e) {
    //	    return ServerUtils.logException(e, "In nextVisualTrace ");
    //	} catch (CompilerException e) {
    //	    return ServerUtils.logException(e, "In nextVisualTrace ");
    //	}
    //    }
    //    
    //    public String visualStateTrace(String netLogoFileName) {
    //	HeadlessWorkspace workspace = HeadlessWorkspace.newInstance();
    //	String result = null;
    //        try {
    //	    workspace.open(netLogoFileName);
    //	    Boolean stop = false;
    //	    while (!stop) {
    //		workspace.command("go");
    //		String state = (String) workspace.evaluateReporter("visual-state-of-all-individuals");
    //		System.out.println(state);
    //	    }
    //        } catch (CompilerException e) {
    //            String contents = ServerUtils.fileToString(netLogoFileName, true);
    //            int startPosition = e.startPos();
    //	    int endPosition = e.endPos();
    //	    int beforeStart = Math.max(0, contents.lastIndexOf("\rto", startPosition));
    //	    int afterEnd = contents.indexOf("\rto", endPosition);
    //	    if (afterEnd < endPosition) {
    //		afterEnd = contents.length();
    //	    }
    //	    String before = contents.substring(beforeStart, startPosition).replace("\r", "<br>");
    //	    String after = contents.substring(endPosition, afterEnd).replace("\r", "<br>");
    //	    result = e.getLocalizedMessage() + 
    //                    " in<br>" + before +
    //                    CommonUtils.stronglyHighlight(contents.substring(startPosition, endPosition)) +
    //                    after;
    //	    ServerUtils.logError(result);
    //        } catch (Exception e) {
    //            result = ServerUtils.logException(e, "In visualStateTrace ");
    //        }
    //         try {
    //	    workspace.dispose();
    //	} catch (InterruptedException e) {
    //	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
    //		    "Error disposing of NetLogo workspace for tracing visual state.");
    //	    e.printStackTrace();
    //	}
    //        return result;
    //    }

    @Override
    public DeltaPageResult createDeltaPage(String nameHTML, 
            String oldURL,
            String userGuid,
            String sessionGuid,
            HashMap<Integer, String> textAreaValues,
            List<MicroBehaviourEnhancement> enhancements,
            ArrayList<ArrayList<String>> listsOfMicroBehaviours,
            boolean listsOfMicroBehavioursNeedNewURLs,
            String hostPageBaseURL, 
            boolean cachingEnabled, 
            boolean internetAccess) {
        if (oldURL == null) {
            String errorMessage = "Internal error. Sorry. Null URL passed to createDeltaPage.";
            logger.severe(errorMessage + " sessionGuid=" + sessionGuid + " nameHTML:" + nameHTML);
            return new DeltaPageResult(null, null, errorMessage);
        }
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), cachingEnabled, internetAccess);
        CommonUtils.setHostBaseURL(hostPageBaseURL);
        //	    ArrayList<Integer> indices = new ArrayList<Integer>();
        //	    ArrayList<Text> values = new ArrayList<Text>();
        //	    if (textAreaValues != null) {
        //		Set<Entry<Integer, String>> entrySet = textAreaValues.entrySet();
        //		for (Entry<Integer, String> entry : entrySet) {
        //		    indices.add(entry.getKey());
        //		    values.add(new Text(entry.getValue()));
        //		}
        //	    }
        //	    indices.add(-1); // for the name
        //	    values.add(new Text(nameHTML));
        //	    PersistenceManager persistenceManager = JDO.getPersistenceManager();
        //	    try {
        //		persistenceManager.makePersistent(new MicroBehaviourCopyUpdate(guid, indices, values));
        //		addListsOfMicroBehavioursToCopy(guid, listsOfMicroBehaviours);
        //	    } finally {
        //		persistenceManager.close();
        //	    }
        MicroBehaviour microBehaviour = getMicroBehaviourFromCacheOrDatabase(oldURL, clientState);
        if (microBehaviour == null || microBehaviour == getDummyMicroBehaviour()) {
            // load URL from web and try again
            try {
                // fetch and transform the page for the side effect
                // of registering the url and the presumed micro-behaviour on the page
                String[] pageParse = fetchAndTransformPage(oldURL, sessionGuid, "", 0, hostPageBaseURL, cachingEnabled, internetAccess);
                if (pageParse[5] != null && !pageParse[5].isEmpty()) {
                    logger.severe("fetchAndTransformPage reported the following parse error: " + pageParse[5]);
                }
                microBehaviour = getMicroBehaviourFromCacheOrDatabase(oldURL, clientState);
                if (microBehaviour == null) {
                    int tableSize = microBehaviourURLTable.size();
                    logger.severe("Created document for " + oldURL + " but did not find the micro-behaviour on the page. Static page path is " + 
                            CommonUtils.getStaticPagePath() + ". Size of cache is " + tableSize);
                    if (tableSize > 0) {
                        String message = "microBehaviourURLTable contains\n";
                        Set<Entry<String, MicroBehaviour>> entrySet = microBehaviourURLTable.entrySet();
                        for (Entry<String, MicroBehaviour> entry : entrySet) {
                            message += entry.getKey() + "\n";
                        }
                        logger.severe(message);
                    }		
                }
            } catch (Exception e) {
                Logger.getLogger(RESOURCE_SERVICE_LOGGER_NAME).severe("Exception fetching URL to make delta page.");
                e.printStackTrace();
            }	
        }
        if (microBehaviour == null || microBehaviour == getDummyMicroBehaviour()) {
            String errorMessage = "Could not find a micro-behaviour for " + oldURL + " when creating a copy.";
            logger.severe(errorMessage);
            String oldStyleURL = CommonUtils.addAttributeOrHashAttributeToURL(oldURL, "changes", ServerUtils.generateGUIDString());
            return new DeltaPageResult(oldStyleURL, null, errorMessage);
        }
        if (textAreaValues == null) {
            // never initialised so use value from data store
            textAreaValues = fetchTextAreaValues(oldURL, clientState);
        }
        if (enhancements == null) {
            // never initialised so use value from data store
            enhancements = fetchEnhancements(oldURL, clientState);
        }
        String microBehaviourName = CommonUtils.getName(textAreaValues.get(-1));
        if (microBehaviourName == null) {
            microBehaviourName = microBehaviour.getName();
        }
        String guid = ServerUtils.generateUniqueIdWithProcedureName(oldURL, microBehaviourName);
        String newURL = CommonUtils.addAttributeOrHashAttributeToURL(oldURL, "changes", guid);
        String netLogoNameFromGuid = CommonUtils.netLogoNameFromGuid(guid, false);
        if (netLogoNameFromGuid != null) {
            DataStore.begin().put(new MicroBehaviourNetLogoName(newURL, netLogoNameFromGuid));
        }
        MicroBehaviour copy = microBehaviour.copy(newURL, textAreaValues, enhancements);
        ArrayList<ArrayList<String>> listsOfMicroBehavioursCopy;
        if (listsOfMicroBehavioursNeedNewURLs) {
            listsOfMicroBehavioursCopy = copyListsOfMicroBehaviours(listsOfMicroBehaviours);
        } else {
            listsOfMicroBehavioursCopy = listsOfMicroBehaviours;
        }
        addListsOfMicroBehavioursToCopy(guid, listsOfMicroBehavioursCopy);
        copy.createMicroBehaviourDataIfNeeded();
        rememberMicroBehaviour(copy, newURL);
        // following didn't work since if two MBs have the same values they end up being
        // treated as one (e.g. a change to one changes all)
        //	String originalURL = copy.getOriginalURL();
        //	if (originalURL != null) {
        //	    result[0] = originalURL;
        //	} else {
        //	    result[0] = newURL;
        //	    rememberMicroBehaviour(copy, newURL);
        //	}
        // this renaming only needed when oldURL is a resource page
        // client deals with this properly now
        //	ServerReplaceURLEvent serverReplaceURLEvent = new ServerReplaceURLEvent(oldURL, newURL, sessionGuid, userGuid);
        //	ServerUtils.persist(serverReplaceURLEvent);

        //	MicroBehaviourURLCopy microBehaviourURLCopy = DataStore.begin().find(MicroBehaviourURLCopy.class, newURL);
        //	String originalURL;
        //	if (microBehaviourURLCopy != null) {
        //	    originalURL = microBehaviourURLCopy.getOriginalURL();
        //	} else {
        //	    originalURL = oldURL;
        //	}
        return new DeltaPageResult(newURL, listsOfMicroBehavioursCopy, null);
    }

    private ArrayList<ArrayList<String>> copyListsOfMicroBehaviours(ArrayList<ArrayList<String>> listsOfMicroBehaviours) {
        if (listsOfMicroBehaviours == null) {
            return null;
        }
        ArrayList<ArrayList<String>> copyOfListsOfMicroBehaviours = new ArrayList<ArrayList<String>>();
        for (List<String> listOfMicroBehaviours : listsOfMicroBehaviours) {
            ArrayList<String> copyOfListOfMicroBehaviours = new ArrayList<String>();
            boolean first = true;
            for (String nameOrURL : listOfMicroBehaviours) {
                if (first) {
                    // first one is just the name of the macro-behaviour
                    copyOfListOfMicroBehaviours.add(nameOrURL);
                    first = false;
                } else if (nameOrURL != null) {
                    String guid = ServerUtils.nextSerialNumber(CommonUtils.changesGuid(nameOrURL));
                    String newURL = ServerUtils.createURLCopy(nameOrURL, guid);
                    copyOfListOfMicroBehaviours.add(newURL);
                } else {
                    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
                            "nameOrURL is null in copyListsOfMicroBehaviours. Not copied.");
                }
            }
            copyOfListsOfMicroBehaviours.add(copyOfListOfMicroBehaviours);
        }
        return copyOfListsOfMicroBehaviours;
    }

    public String addListsOfMicroBehavioursToCopy(String guid, ArrayList<ArrayList<String>> listsOfMicroBehaviours) {
        if (listsOfMicroBehaviours == null || listsOfMicroBehaviours.isEmpty()) {
            return null;
        }
        ServerUtils.persist(new MicroBehaviourCopyListsOfMicroBehaviours(guid, listsOfMicroBehaviours));	
        //	for (ArrayList<String> nameAndUrls : listsOfMicroBehaviours) {
        //	    String error = addListOfMicroBehavioursToCopy(guid, nameAndUrls);
        //	    if (error != null) {
        //		return error;
        //	    }
        //	}
        return null;
    }

    //    private String addListOfMicroBehavioursToCopy(String guid, ArrayList<String> nameAndUrls) {
    //	// JDO uses this with database null
    //	// TODO: rationalise once JDO is stable
    //	StringBuffer urls = new StringBuffer();
    //	int size = nameAndUrls.size();
    //	try {
    //	    for (int i = 1; i < size; i++) {
    //		urls.append(URLEncoder.encode(nameAndUrls.get(i), "UTF-8"));
    //		urls.append(" "); // separator -- ok since urls encoded above so no space in it
    //	    }
    //	} catch (Exception e) {    
    //	    return ServerUtils.logException(e, "In addListOfMicroBehavioursToCopy ");
    //	}
    //	return addListOfMicroBehavioursToCopy(guid, nameAndUrls.get(0), urls.toString());
    //    }

    //    private static String addListOfMicroBehavioursToCopy(
    //	    String guid,
    //	    String name,
    //	    String urls) {
    //	ServerUtils.persist(new MicroBehaviourCopyMicroBehaviours(guid, name, urls));   
    //	return null;
    //    }

    @Override
    public HashMap<Integer, String> fetchTextAreaValues(String url, String sessionGuid, String hostBaseURL, boolean cachingEnabled, boolean internetAccess) {
        this.hostBaseURL = hostBaseURL;
        // the above is a better way since it is local to the instance of this class
        // TODO: remove the following and its dependencies
        CommonUtils.setHostBaseURL(hostBaseURL);
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), cachingEnabled, internetAccess);
        return fetchTextAreaValues(url, clientState);
    }

    public HashMap<Integer, String> fetchTextAreaValues(String url, ClientState clientState) {
        MicroBehaviour microBehaviour = getMicroBehaviourFromCacheOrDatabase(url, clientState);
        if (microBehaviour == null) {
            // TODO: get from other server if importing a model from another 
            // Behaviour Composer server
            return null;
        } else {
            return microBehaviour.getTextAreaValues();
        }
    }

    @Override
    public List<MicroBehaviourEnhancement> fetchEnhancements(String url, String sessionGuid, String hostBaseURL, boolean cachingEnabled, boolean internetAccess) {
        this.hostBaseURL = hostBaseURL;
        CommonUtils.setHostBaseURL(hostBaseURL);
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), cachingEnabled, internetAccess);
        return fetchEnhancements(url, clientState);
    }

    public List<MicroBehaviourEnhancement> fetchEnhancements(String url, ClientState clientState) {
        MicroBehaviour microBehaviour = getMicroBehaviourFromCacheOrDatabase(url, clientState);
        if (microBehaviour == null) {
            return null;
        }
        return microBehaviour.getEnhancements();
    }

    public String fetchListsOfMicroBehaviours(final String guid,
            final ArrayList<ArrayList<String>> listsOfMicroBehaviours, 
            final ClientState clientState) {
        // adds to listsOfMicroBehaviours
        // returns error message (a String) or null if all went well
        PersistenceManager persistenceManager = JDO.getPersistenceManager();
        MicroBehaviourCopyListsOfMicroBehaviours microBehaviourCopyListsOfMicroBehaviours;
        try {
            microBehaviourCopyListsOfMicroBehaviours = 
                    persistenceManager.getObjectById(MicroBehaviourCopyListsOfMicroBehaviours.class, guid);
            ArrayList<ArrayList<String>> newListsOfMicroBehaviours = 
                    microBehaviourCopyListsOfMicroBehaviours.getListsOfMicroBehaviours();
            if (!newListsOfMicroBehaviours.isEmpty()) {
                for (ArrayList<String> listOfMicroBehaviours : newListsOfMicroBehaviours) {
                    listsOfMicroBehaviours.add(listOfMicroBehaviours);
                }
            } else {
                Query query = persistenceManager.newQuery(MicroBehaviourCopyMicroBehaviours.class);
                try {
                    query.setFilter("guid == guidParam");
                    query.declareParameters("String guidParam");
                    @SuppressWarnings("unchecked")
                    List<MicroBehaviourCopyMicroBehaviours> copies = 
                    (List<MicroBehaviourCopyMicroBehaviours>) query.execute(guid);
                    for (MicroBehaviourCopyMicroBehaviours copy : copies) {
                        ArrayList<String> listOfMicroBehaviours = new ArrayList<String>();
                        listsOfMicroBehaviours.add(listOfMicroBehaviours);
                        String name = copy.getName();
                        listOfMicroBehaviours.add(name);
                        String urlStrings[] = copy.getUrls();
                        for (String url : urlStrings) {
                            // don't use URLDecoder.decode since spaces will make things ambiguous 
                            listOfMicroBehaviours.add(url);
                        }
                    }

                } finally {
                    query.closeAll();
                }
            }
        } catch (JDOObjectNotFoundException e) {
            if (!clientState.isInternetAccess()) {
                return null;
            }
            RemoteAPI freeRemoteAPI = ServerUtils.getFreeRemoteAPI(getThreadLocalRequest());
            if (freeRemoteAPI == null) {
                return null;
            } else {
                RunOnProductionGAECallback<String> callback = new RunOnProductionGAECallback<String>() {

                    @Override
                    public String execute() {
                        return fetchListsOfMicroBehaviours(guid, listsOfMicroBehaviours, clientState);
                    }

                };
                String listsOfMicroBehaviour = freeRemoteAPI.runOnProductionGAE(callback);
                if (clientState.isCachingEnabled()) {
                    // TODO:
                }
                return listsOfMicroBehaviour;
            }
        } finally {
            persistenceManager.close();
        }
        return null;
    }

    public String fetchUpdateFromCopy(String url, int index, ClientState clientState) {
        MicroBehaviour microBehaviour = getMicroBehaviourFromCacheOrDatabase(url, clientState);
        if (microBehaviour == null) {
            return null;
        } else {
            HashMap<Integer, String> textAreaValues = microBehaviour.getTextAreaValues();
            if (textAreaValues == null) {
                return null;
            }
            return textAreaValues.get(index);
        }
    }

    //    public static String fetchUpdateFromCopy(String changesGuid, int index) {
    //	PersistenceManager persistenceManager = JDO.getPersistenceManager();
    //	Query query = persistenceManager.newQuery(MicroBehaviourCopyUpdate.class);
    //	query.setFilter("guid == guidParam && textAreaIndex == textAreaIndexParam");
    //	query.declareParameters("String guidParam, int textAreaIndexParam");
    //	query.setUnique(true);
    //	try {
    //	    MicroBehaviourCopyUpdate update = 
    //		(MicroBehaviourCopyUpdate) query.execute(changesGuid, index);
    //	    if (update != null) {
    //		return update.getValue();
    //	    }
    //	} finally {
    //	    query.closeAll();
    //	    persistenceManager.close();
    //	}
    //	return null;
    //    }

    public void rememberMicroBehaviour(MicroBehaviour microBehaviour) {
        rememberMicroBehaviour(microBehaviour, microBehaviour.getBehaviourURL());
    }

    public void rememberMicroBehaviour(MicroBehaviour microBehaviour, String url) {
        if (url != null) {
            microBehaviourURLTable.put(url, microBehaviour);
            microBehaviourURLUpdateTime.put(url, System.currentTimeMillis());
        }
    }

    //    private ConcurrentHashMap<String, MicroBehaviour> getMicroBehaviourURLTable(String sessionGuid) {
    //	ConcurrentHashMap<String, MicroBehaviour> behaviourURLTable = 
    //	    sessionToMicroBehaviourURLTable.get(sessionGuid);
    //	if (behaviourURLTable == null) {
    //	    behaviourURLTable = new ConcurrentHashMap<String, MicroBehaviour>();
    //	    sessionToMicroBehaviourURLTable.put(sessionGuid, behaviourURLTable);
    //	}
    //	sessionToMicroBehaviourURLTableLastAccessTime.put(sessionGuid, System.currentTimeMillis());
    //	return behaviourURLTable;
    //    }

    public void removeMicroBehaviourCache(String url) {
        microBehaviourURLTable.remove(url);
        microBehaviourURLUpdateTime.remove(url);
    }

    public NetLogoModel getNetLogoModel() {
        return netLogoModel;
    }

    public void setNetLogoModel(NetLogoModel netLogoModel) {
        this.netLogoModel = netLogoModel;
    }

    /* (non-Javadoc)
     * @see uk.ac.lkl.client.rpc.ResourcePageService#logMessage(java.lang.String, java.lang.String)
     */
    public String logMessage(String levelName, String message) {
        // I tried making this void but that caused everything to hang when deployed
        Level level = Level.parse(levelName);
        if (level == Level.INFO) {
            // Info warnings are ignored with the default settings
            // could change the settings but then lots of distracting messages are in the logs
            level = Level.WARNING;
            message = "INFO: " + message;
        }
        Logger.getLogger("Client").log(level, message);
        return null;
    }

    @Deprecated
    // not certain but I think this is obsolete TODO: confirm it is obsolete and remove
    public String addTextAreaValueToCopy(int index, String value, String guid, String sessionGuid) {
        //	ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest());
        // returns null or an error message
        //	    PersistenceManager persistenceManager = JDO.getPersistenceManager();
        //	    Query query = persistenceManager.newQuery(MicroBehaviourCopyUpdate.class);
        //	    query.setFilter("guid == guidParam && textAreaIndex == textAreaIndexParam");
        //	    query.declareParameters("String guidParam, int textAreaIndexParam");
        //	    query.setUnique(true);
        //	    try {
        //		MicroBehaviourCopyUpdate update = 
        //		    (MicroBehaviourCopyUpdate) query.execute(guid, index);
        //		if (update == null) {
        //		    persistenceManager.makePersistent(new MicroBehaviourCopyUpdate(guid, index, value));
        //		} else {
        //		    update.setValue(value);
        //		}
        //	    } finally {
        //		query.closeAll();
        //		persistenceManager.close();
        //	    }
        // TODO:
        Logger.getLogger(RESOURCE_SERVICE_LOGGER_NAME).severe("addTextAreaValueToCopy not yet re-implemented");
        return null;
    }

    @Override
    public String[] getAttributesOfMicroBehaviours(String[] microBehaviourURLs, String sessionGuid, boolean cachingEnabled, boolean internetAccess) {
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), cachingEnabled, internetAccess);
        AttributesCollector attributesCollector = new AttributesCollector();
        for (String url : microBehaviourURLs) {
            if (!url.startsWith(CommonUtils.TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN)) {
                MicroBehaviour microBehaviour = getMicroBehaviourFromCacheOrDatabase(url, clientState);
                if (microBehaviour != null) {
                    try {
                        String behaviourCode = microBehaviour.getBehaviourCode();
                        attributesCollector.setMicroBehaviour(microBehaviour);
                        ServerUtils.findAllKindsOfVariables(behaviourCode, attributesCollector);
                    } catch (NetLogoException e) {
                        e.printStackTrace();
                    }
                } else {
                    ServerUtils.logError("Expected to find a micro-behaviour for the URL: " + url);
                }
            }
        }
        ArrayList<FoundAttribute> foundAttributes = attributesCollector.getFoundAttributes();
        Comparator<FoundAttribute> comparator = new Comparator<FoundAttribute>() {

            @Override
            public int compare(FoundAttribute o1, FoundAttribute o2) {
                return o1.getAttributeName().compareTo(o2.getAttributeName());
            }

        };
        Collections.sort(foundAttributes, comparator);
        String result[] = new String[foundAttributes.size()*3];
        int index = 0;
        for (FoundAttribute foundAttribute : foundAttributes) {
            result[index++] = foundAttribute.getAttributeName();
            result[index++] = foundAttribute.encodeReadingMicroBehavioursAsString();
            result[index++] = foundAttribute.encodeWritingMicroBehavioursAsString();
        }
        return result;
    }

    @Override
    public String[] getNetLogoCode(String xml, 
            String sessionGuid, 
            String userGuid, 
            String hostBaseURL,
            boolean cachingEnabled,
            boolean internetAccess,
            boolean useAuxiliaryFile) {
        this.hostBaseURL = hostBaseURL;
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), userGuid, cachingEnabled, internetAccess);
        CommonUtils.setHostBaseURL(hostBaseURL); 
        XMLDocument xmlDocument = new XMLDocument(xml);
        Document document = xmlDocument.getDocument();
        Element microBehaviourElement = document.getDocumentElement();
        NetLogoModel netLogoModel = new NetLogoModel(this, useAuxiliaryFile, clientState);
        netLogoModel.setOnlyForFetchingCode(true);
        MicroBehaviour microBehaviour = MacroBehaviour.processMicroBehaviourXML(microBehaviourElement, netLogoModel, null);
        String[] result = new String[2];
        if (microBehaviour != null) {
            try {
                result[0] = microBehaviour.getTransformedBehaviourCode();
            } catch (NetLogoException e) {
                e.printStackTrace();
                result[1] = e.toString();
            }
        } else {
            result[1] = netLogoModel.getClientState().getAndRecordWarningsToSendBackToClient();
        }
        return result;
    }

    public MicroBehaviour getDummyMicroBehaviour() {
        return dummyMicroBehaviour;
    }

    public ServletRequest getRequest() {
        if (request == null) {
            return getThreadLocalRequest();
        }
        return request;
    }

    public void setRequest(ServletRequest request) {
        this.request = request;
    }

    @Override 
    public String getNetLogo2BCChannelToken(String userGuid, String sessionGuid) {
        return ServerUtils.channelToken(sessionGuid+userGuid);
    }

    @Override
    public DeltaPageResult copyMicroBehaviourCustomisations(String oldURL, String replacementURL, 
            ArrayList<ArrayList<String>> listsOfMicroBehaviours,
            String userGuid, String sessionGuid, 
            String hostPageBaseURL, boolean cachingEnabled, boolean internetAccess) {
        // gets the customisations of the oldURL and apply them to the replacementURL
        ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), userGuid, cachingEnabled, internetAccess);
        MicroBehaviour oldMicroBehaviour = getMicroBehaviourFromCacheOrDatabase(oldURL, clientState);
        if (oldMicroBehaviour == null) {
            return new DeltaPageResult("Unable to find the micro-behaviour of " + oldURL);
        } else {
            MicroBehaviour replacementMicroBehaviour = getMicroBehaviourFromCacheOrDatabase(replacementURL, clientState);
            String nameHTML;
            if (replacementMicroBehaviour == null) {
                nameHTML = oldMicroBehaviour.getName();
            } else {
                nameHTML = replacementMicroBehaviour.getName();
            }
            return createDeltaPage(nameHTML, 
                    replacementURL,
                    userGuid,
                    sessionGuid,
                    oldMicroBehaviour.getTextAreaValues(),
                    oldMicroBehaviour.getEnhancements(),
                    listsOfMicroBehaviours,
                    true,
                    hostPageBaseURL, 
                    cachingEnabled, 
                    internetAccess);
        }
    }

    public void behaviourCodeChanged(String behaviourURL, String behaviourCode, ArrayList<String> textAreaElements) {
        String url = CommonUtils.removeBookmark(behaviourURL);
        ArrayList<String> removals = new ArrayList<String>();
        Set<Entry<String, MicroBehaviour>> entrySet = microBehaviourURLTable.entrySet();
        for (Entry<String, MicroBehaviour> entry : entrySet) {
            MicroBehaviour microBehaviour = entry.getValue();
            String microBehaviourURL = entry.getKey();
            if (microBehaviourURL.startsWith(url) && 
                    (!behaviourCode.equals(microBehaviour.getBehaviourCodeUnprocessed()) ||
                            !textAreaElements.equals(microBehaviour.getTextAreaElements()))) {
                microBehaviour.setBehaviourCode(behaviourCode);
                microBehaviour.setTextAreaElements(textAreaElements);
                removals.add(microBehaviourURL);
            }
        }
        for (String urlToRemove : removals) {
            microBehaviourURLTable.remove(urlToRemove);
        }
    }

    public String getHostBaseURL() {
        return hostBaseURL;
    }

    @Override
    public String makeReadOnly(String urlString, String sessionGuid, String hostBaseURL, boolean cachingEnabled, boolean internetAccess) {
        // extra arguments could be used for better error handling
        // e.g. http://127.0.0.1:8888/p/IXgHKuB7QVleNW8lFoqj6b.edited.html
        int extensionIndex = urlString.indexOf(CommonUtils.EDITED_HTML);
        if (extensionIndex < 0) {
            return "URL is not an edited page: " + urlString;
        }
        int lastSlashIndex = urlString.lastIndexOf('/');
        final String pageGuid = urlString.substring(lastSlashIndex+1, extensionIndex);
        EditedPage editedPage = null;
        try {
            editedPage = getEditedPage(pageGuid);
        } catch (Exception e) {
            return "Unable to find the page with id: " + pageGuid;
        }
        editedPage.setReadOnly(true);
        ServerUtils.persist(editedPage);
        return null;
    }

    //    @Override
    //    public int[] getModelStatistics(String modelGuid) {
    //	return ModelXML.getStatistics(modelGuid);
    //    }

    //    @Override
    //    protected void onBeforeRequestDeserialized(java.lang.String serializedRequest) {
    //	System.out.println(serializedRequest);
    //    }

    //    @Override
    //    protected void processPost(HttpServletRequest request,
    //	    HttpServletResponse response) throws Throwable {
    //	Enumeration attributeNames = request.getAttributeNames();
    //	while (attributeNames.hasMoreElements()) {
    //	System.out.println(attributeNames.nextElement());
    //	}
    //	
    //    }

    //    @Override
    //    public void processPost(javax.servlet.http.HttpServletRequest request,
    //            javax.servlet.http.HttpServletResponse response)
    //     throws java.lang.Throwable
    //}

    //  @Override
    //  protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    //	
    //  }

}


