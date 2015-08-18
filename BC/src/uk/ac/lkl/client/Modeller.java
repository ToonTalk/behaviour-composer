package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import uk.ac.lkl.client.rpc.CreateResourcePageService;
import uk.ac.lkl.client.rpc.CreateHistoryService;
import uk.ac.lkl.client.rpc.ResourcePageServiceAsync;
import uk.ac.lkl.client.rpc.HistoryServiceAsync;
import uk.ac.lkl.client.event.ReconstructEventsContinutation;
import uk.ac.lkl.client.event.RemoveMacroBehaviourEvent;
import uk.ac.lkl.client.event.StartEvent;
import uk.ac.lkl.client.event.BrowseToPageEvent;
import uk.ac.lkl.client.event.ModellerEvent;
import uk.ac.lkl.client.event.CompoundEvent;
import uk.ac.lkl.client.BrowsePanel;
import uk.ac.lkl.client.composer.CustomisationPopupPanel;
import uk.ac.lkl.client.composer.MacroBehaviourAsMicroBehaviourView;
import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.shared.CommonUtils;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelFactory;
import com.google.gwt.appengine.channel.client.ChannelFactory.ChannelCreatedCallback;
import com.google.gwt.appengine.channel.client.SocketError;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

/**
 * This contains the entry point code that is common between 
 * the BehaviourComposer and MoPiX
 * 
 * 
 * @author Ken Kahn
 *
 */
abstract public class Modeller implements EntryPoint {
    public static Modeller INSTANCE; // set by subclasses
    public static final String NON_BREAKING_SPACE = "&nbsp;";
    public static String applicationTitle = ""; // overridden by subclasses
    // TODO: split up constants into common, mopix-only, and bc-only
    public static ModellerConstants constants = (ModellerConstants) GWT.create(ModellerConstants.class);
    protected static String defaultExplorerPage = null;
    protected static String defaultManualPage = null;
    protected static String resourceFolderName = "";
    public static String defaultURLBase = null; // but URL attribute can reset this
    public static boolean debuggingEnabled = false;
    public static String debugMessages = "";
    public static boolean debugMessagesHTML = false;
    private final static AlertsLine alertsLine = new AlertsLine();
    private static Anchor lastCommand = null;
    public static Button debugButton = new ModellerButton("An internal error occurred. Click to show debugging information.");
    public static TopLevelPanel wholePanel = new TopLevelPanel();
    // this switches from tab panel, history, settings, etc.
    private static SimplePanel mainContentsPanel = new SimplePanel();
    public static ModellerTabPanel mainTabPanel = new ModellerTabPanel();
    public static Anchor resourcesLink = new CommandAnchor(constants.resources());
    public static HorizontalPanel splitScreenCheckBoxAndSessionHelp = new HorizontalPanel();
    public static ModellerSplitLayoutPanel splitPanel = null;
    public static Widget splitPanelOtherWidget = null;
    final public static HistoryPanelContents historyPanel = new HistoryPanelContents();
    final public static Anchor modelHistoryLink = new CommandAnchor(constants.modelHistory());
    public static BrowsePanel manualPanel = null;
    public static final Anchor helpLink = new CommandAnchor(constants.help());
    public static BrowsePanel searchPanel = null;
    public static Anchor searchLink = new CommandAnchor(constants.search());
    public static Widget searchResultsPanel = null;
    public static ErrorsPanel errorsPanel = new ErrorsPanel();
    public static VerticalPanel optionsPanel = new VerticalPanel();
    public static final Anchor settingsLink = createSettingsLink();
    public static Anchor languageLink = null;
    public static CheckBox collaborateButton = null;
    public static HTML collaborationURLHTML = null;
    public static CheckBox enableEditorButton = null;
    public static CheckBox enableLocalResourceEditingButton = null;
    public static int windowWidthMargins = 20;
    public static int windowHeightMargins = 190;
    public static ResourcePageServiceAsync resourcePageService = null;
    public static HistoryServiceAsync historyService = null;
    public static boolean addingHistoryToken = false; 
    public static ArrayList<BrowseToPageEvent> browseToPageEvents = new ArrayList<BrowseToPageEvent>();
    public static String sessionGuid = null;
    public static String userGuid = null;
    // used to link an old session id to a new one
    public static String sessionGuidToBeReplaced = null;
    public static int userID = -1;
    public static String initialReadOnlySessionID = null;
    public static String readOnlySessionID = null;
    public static boolean cookiesSaved = false;
    public static boolean cookieFound = false;
    public static boolean cookieGivenInURL = false;
    public static String originalURL = null;
    public static String locale = null;
    public static String modelID = null;
    public static String moduleBaseURL = null;
    private static AsyncCallbackNetworkFailureCapable<String[]> recordFirstEventCallback = null;
    private static long timeOfLastUpdate = 0;
    private static boolean keepCheckingForNewEvents = false;
    // now that TextAreas and macro-behaviours on pages do it better than page editing 
    // turn off editing by default
    public static boolean pageEditingEnabled = false; 
    private static BrowsePanel protectedBrowsePanel = null;
    private static boolean loadingInProgress = true;
    
    // used with GWT ensureDebugId
    public static int debugIDCounter = 0;
    
    public static final int IGNORE_NO_EVENTS = 0;
    // following used when implementing session check boxes
    public static final int IGNORE_START_AND_LOAD_EVENTS = 1;
    // following used only to add events to the history
    public static final int IGNORE_START_AND_LOAD_EVENTS_AND_DONT_RECONSTRUCT = 2;
    
    private static HashMap<String, BrowsePanel> urlPanelMap = 
	new HashMap<String, BrowsePanel>();
     
    private static ReconstructEventsContinutation dummyContinuation =
	new ReconstructEventsContinutation() {

	    @Override
	    public void reconstructSubsequentEvents(ModellerEvent event) {
		// do nothing
	    }
	
    };
    
//    public static boolean useLocalHost;  
    public static boolean cachingEnabled;
    public static boolean internetAccess;
    public static boolean useAuxiliaryFile = false;
    public static boolean forWebVersion = false; // current compatibility with NetLogo Web
    public static boolean forWebVersionRequested = false; // user requested compatibility with NetLogo Web
    private static boolean alertsLineClear = true;
    // null indicates not yet initialised
    protected Boolean advancedMode = null;
    protected boolean needToReload;
    protected String explorerPage;
    private String givenName;
    protected String channelClientId;
    private String contextId;
    private ScrollPanel otherScrollPanel;
    private HorizontalPanel iconAndCommandBarAndAlertsLine = new HorizontalPanel();
    private ScrollPanel restScrollPanelAfterSplit;
    public ArrayList<MacroBehaviourView> allPrototypes = new ArrayList<MacroBehaviourView>();
    public int prototypeCounter = 0;
    private boolean translateEnabled;
    protected boolean warnIfUnknownSessionId = true;
    private String behaviourCursorStyle;
    private String bc2NetLogoChannelToken;
    private String bc2NetLogoOriginalSessionGuid;
    private String nonBehaviourCursorStyle;
    protected String originalModelDescription;
//    private String reconstructedChannelNumber = "0";
    protected HorizontalPanel composerButtonPanel = new HorizontalPanel();
    private boolean ignoreChannelErrors = false;
    
    public Images getImages() {
	return (Images) GWT.create(Images.class);
    }
    
    public static void addHTMLToDebugMessages(String message) {
	if (debuggingEnabled) {
	    if (!debugButton.isAttached()) {
		wholePanel.add(debugButton);
	    }
	    debugMessages += message + "<br>";
	    debugMessagesHTML = true;
	    System.out.println(message);
	}
    }

    public static void addToDebugMessages(String message) {
	if (debuggingEnabled) {
	    if (!debugButton.isAttached()) {
		errorsPanel.add(debugButton);
	    }
	    debugMessages += message + "\r\n";
	    System.out.println(message);
	}
    }
    
    protected void addModel(String data, boolean removeOld) {
	// subclasses do the work
    }

    public static void browseToFromHistory(final String urlString) {
	for (BrowseToPageEvent event : browseToPageEvents) {
	    if (urlString.equals(event.getUrl())) {
		BrowsePanel panel = event.getPanel();
		panel.setCurrentURLBase(event.getBaseUrl());
		panel.browseTo(urlString, false, true, null);
		return;
	    }
	}
	Modeller.addToErrorLog("Couldn't find a BrowseToPageEvent " + urlString);
    }

    public static ResourcePageServiceAsync getResourcePageService() {
	if (resourcePageService == null) {
	    resourcePageService = CreateResourcePageService.createResourcePageService();
	}
	return resourcePageService;
    }
    
    public static HistoryServiceAsync getHistoryService() {
	if (historyService == null) {
	    historyService = CreateHistoryService.createHistoryService();
	}
	return historyService;
    }
    
    public AsyncCallbackNetworkFailureCapable<String[]> getRecordFirstEventCallback() {
	if (recordFirstEventCallback == null) {
	    recordFirstEventCallback = createRecordStartEventCallback();
	}
	return recordFirstEventCallback;
    }

    protected AsyncCallbackNetworkFailureCapable<String[]> createRecordStartEventCallback() {
	final AsyncCallbackNetworkFailureCapable<String[]> callback = new AsyncCallbackNetworkFailureCapable<String[]>() {
	    
	    @Override
	    public void onFailure(Throwable caught) {
		NetworkFailure.instance().networkFailure(caught, Modeller.constants.recordingHistory(), getTimer());
	    }

	    @Override
	    public void onSuccess(String[] result) {
		super.onSuccess(result);
		if (result != null) {
		    String warnings = result[4];
		    if (warnings != null && !warnings.isEmpty() && !warnings.equals("null")) {
			addToErrorLog(warnings);
//			if (result[0] == null || CommonUtils.isErrorResponse(warnings)) {
//			    return;
//			}
		    }
		    String sessionID = result[0];
		    if (sessionID != null && CommonUtils.isErrorResponse(sessionID)) {
			addToErrorLog(sessionID);
//			return;
		    }
		    boolean newSession = (sessionGuid == null);
		    if (result[1] != null) {
			readOnlySessionID = result[1];
		    }
		    if (sessionID != null) {
			sessionGuid = sessionID;
		    }
		    boolean newUserGuid = (userGuid == null);
		    userGuid = result[2];
		    try {
			userID = Integer.parseInt(result[3]);
		    } catch (Exception e) {
			reportException(e, "Problem interpreting userID: " + result[3]);
		    }
		    if (sessionGuid != null && Utils.getLocationParameter("cookiesEnabled") != null) {
			Date now = new Date();
			long expiration = now.getTime() + 365*24*60*60*1000; 
			// a year in milliseconds
			Date expires = new Date(expiration);
			Cookies.setCookie("UserIdentity", sessionGuid, expires); // pre-database convention
			Cookies.setCookie("UserIdentityGuid", userGuid, expires); // introduced with database
		    }
		    if (newSession || newUserGuid) {
			reloadWithSessionID(computeNewURL(true));
		    }    
		}
	    }};
	    return callback;   
    }
    
    public static void fetchModel(String modelURL, AsyncCallback<String> callback) {
	getResourcePageService().fetchModel(URL.encode(modelURL), 
		                            Modeller.sessionGuid, 
		                            GWT.getHostPageBaseURL(),
		                            Modeller.cachingEnabled,
		                            Modeller.internetAccess,
		                            callback);
    }
    
    public MicroBehaviourView getMicroBehaviourView(String urlStrings) {
	String[] allURLs = urlStrings.split(";");
	for (String urlString : allURLs) {
	    for (MacroBehaviourView macroBehaviour : allPrototypes) {
		MicroBehaviourView microBehaviourWithURL = 
			macroBehaviour.getMicroBehaviourWithURL(urlString, false, true);
		if (microBehaviourWithURL != null) {
		    return microBehaviourWithURL;
		}
	    }
	}
	BrowsePanel openBrowsePanel = getOpenBrowsePanel(urlStrings);
	if (openBrowsePanel != null) {
	    return openBrowsePanel.getMicroBehaviour();
	}
	return null;
    }
    
    public void walkMicroBehaviourViews(MicroBehaviourComand command) {
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    if (!macroBehaviour.walkMicroBehaviourViews(command)) {
		return;
	    }
	}
    }
    
    public MicroBehaviourView getMicroBehaviourViewWithOriginalURL(String urlString) {
   	for (MacroBehaviourView macroBehaviour : allPrototypes) {
   	    MicroBehaviourView microBehaviourWithURL = 
   		macroBehaviour.getMicroBehaviourWithOriginalURL(urlString, true);
   	    if (microBehaviourWithURL != null) {
   		return microBehaviourWithURL;
   	    }
   	}
   	return null;
    }
    
    public MicroBehaviourView getMicroBehaviourViewOfWidget(String type, String name, com.google.gwt.xml.client.Element element) {
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    if (macroBehaviour.isActive()) {
		MicroBehaviourView microBehaviourOfWidget = 
			macroBehaviour.getMicroBehaviourViewOfWidget(type, name, element);
		if (microBehaviourOfWidget != null) {
		    return microBehaviourOfWidget;
		}
	    }
   	}
   	return null;
    }
    
    public MicroBehaviourView getMicroBehaviourViewOfDeclaration(String newDeclaration, String oldDeclaration) {
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    if (macroBehaviour.isActive()) {
		MicroBehaviourView microBehaviourOfWidget = 
			macroBehaviour.getMicroBehaviourViewOfDeclaration(newDeclaration, oldDeclaration);
		if (microBehaviourOfWidget != null) {
		    return microBehaviourOfWidget;
		}
	    }
   	}
   	return null;
    }
    
    public ArrayList<MicroBehaviourView> getAllMicroBehaviours() {
	ArrayList<MicroBehaviourView> microBehaviours = new ArrayList<MicroBehaviourView>();
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    macroBehaviour.addMicroBehavioursRecursively(microBehaviours);
	}
	return microBehaviours;
    }
    
    public static void executeOnMicroBehaviourPage(
	    final String url,     
	    final BrowsePanelCommand command,
	    final boolean removeAfterProcessing,
	    final boolean copyOnUpdate) {
	BrowsePanel panel = mainTabPanel.getBrowsePanelWithURL(url);
	if (panel == null) {
	    panel = urlPanelMap.get(url);
	}
	if (panel == null || panel.getMicroBehaviour() == null) {
	    BrowsePanelCommand fullCommand = 
		new BrowsePanelCommand() {

		    @Override
		    public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
			if (panel != null) {
			    urlPanelMap.put(url, panel);
			}
			command.execute(panel, answer, panelIsNew);
		    }
		
	    };
//	    Log.info("executeOnMicroBehaviourPage url: " + url); // for debugging
	    executeOnNewMicroBehaviourPage(url, fullCommand, removeAfterProcessing, copyOnUpdate);
	} else {
	    if (panel.isTemporary() && !removeAfterProcessing) {
		panel.setTemporary(false);
	    }
	    command.execute(panel, null, false);
	}	
    }
    
    public static void removeFromUrlPanelMap(String url) {
	urlPanelMap.remove(url);
    }

    public static void executeOnNewMicroBehaviourPage(
	    final String url,
	    final BrowsePanelCommand command,
	    final boolean removeAfterProcessing,
	    boolean copyOnUpdate) {
	if (url.startsWith(CommonUtils.TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN)) {
	    command.execute(null, null, false);
	    return; // not a normal micro-behaviour -- just a prototype acting like a list
	}
	// temporarily load the page to get its micro behaviour view
	BrowsePanelCommand fullCommand = new BrowsePanelCommand() {
	    public void execute(final BrowsePanel panel, final String answer[], final boolean panelIsNew) {
		if (command != null) {
		    command.execute(panel, answer, panelIsNew);
		}
		if (panel.isTemporary() || (removeAfterProcessing && panel.getMicroBehaviour() != null)) {
		    mainTabPanel.remove(panel);
		}			    	
	    }
	};
	browseToNewTab(
		CommonUtils.getFileName(url), 
		null,
		null,
		null, 
		url, 
		null, 
		null, 
		fullCommand, 
		false, 
		removeAfterProcessing, 
		removeAfterProcessing, 
		copyOnUpdate);
    }
    
    public void updateTextArea(
	    final String url, 
	    final String newContents, 
	    final int indexInCode, 
	    final boolean initialising,
	    final boolean copyOnUpdate,
	    final Command doAfterCommand) {
	BrowsePanelCommand command = new BrowsePanelCommand() {

	    public void execute(BrowsePanel panel, String answer[], boolean PanelIsNew) {
		panel.updateTextArea(indexInCode, newContents, initialising);
		updateTextAreaOfAllMicroBehaviourViews(url, newContents, indexInCode);
		if (doAfterCommand != null) {
		    doAfterCommand.execute();
		}
//		tabPanel.remove(panel);
	    }

	};
//	Log.info("updateTextArea url: " + url); // for debugging
//	System.out.println("updating to " + newContents);
	executeOnMicroBehaviourPage(url, command, true, copyOnUpdate);	    
    }
    
    public static void enhanceMicroBehaviour(
	    final MicroBehaviourEnhancement enhancement,
	    final String url, 
	    final Command doAfterCommand) {
	BrowsePanelCommand command = new BrowsePanelCommand() {

	    public void execute(BrowsePanel panel, String answer[], boolean panelIsNew) {
		MicroBehaviourView microBehaviour = panel.getMicroBehaviour();
		if (microBehaviour == null) {
		    Modeller.addToErrorLog("Excepted to find a micro-behaviour associated with " + url);  
		} else {
		    microBehaviour.addEnhancement(enhancement);
		    microBehaviour.enhanceCode(enhancement, panel, false, microBehaviour.nextTextAreaIndex()-1);
		}
		// need anything like the following???
//		updateTextAreaOfAllMicroBehaviourViews(url, newContents, indexInCode);
		if (doAfterCommand != null) {
		    doAfterCommand.execute();
		}
	    }

	};
//	Log.info("enhanceMicroBehaviour url: " + url); // for debugging
	executeOnMicroBehaviourPage(url, command, true, true);	    
    }
    
    public static void removeMicroBehaviourEnhancement(final String url, final Command doAfterCommand) {
	BrowsePanelCommand command = new BrowsePanelCommand() {

	    public void execute(BrowsePanel panel, String answer[], boolean panelIsNew) {
		MicroBehaviourView microBehaviour = panel.getMicroBehaviour();
		if (microBehaviour == null) {
		    Modeller.addToErrorLog("Excepted to find a micro-behaviour associated with " + url);  
		} else {
		    microBehaviour.removeLastEnhancement();
		    if (panel.isAttached()) {// need to refresh the browser panel
			panel.refresh();
		    }
		}
		if (doAfterCommand != null) {
		    doAfterCommand.execute();
		}
	    }

	};
//	Log.info("removeMicroBehaviourEnhancement url: " + url); // for debugging
	executeOnMicroBehaviourPage(url, command, true, true);	    
    }
    
    public void updateTextAreaOfAllMicroBehaviourViews(String url, String newContents, int indexInCode) {
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    macroBehaviour.updateTextAreaOfAllMicroBehaviourViews(url, newContents, indexInCode);
	}
    }

    public static void setStatusLineHTMLError(String s) {
	setAlertsLine(CommonUtils.emphasiseError(s));
	Modeller.addToErrorLog(s);
    }
    
    public static String setAlertsLine(String html) {
	if (html == null) {
	    return html;
	}
	// TODO: determine if this returning the string is obsolete
	if (BehaviourComposer.epidemicGameMakerMode()) {
	    // Epidemic Game Maker has bolder bigger status messages
	    if (html.contains("size='4'")) {
		// make sure all of it is size 4 but don't change color, etc. (might be a warning)
		alertsLine.setHTML(CommonUtils.fontSize(html, 4));
	    } else {
		alertsLine.setHTML(CommonUtils.emphasise(html));
	    }
	} else {
	    clearAlertsLine();
	    alertsLine.setHTML(html);
	}
	return html;
    }
    
    public static String setAlertsLineAndHighlight(String html) {
	return setAlertsLine(CommonUtils.stronglyHighlight(html));
    }
    
    // addAlert below does this better
//    public static void clearAlertsLine(String html) {
//	// conditional commented out since this
//	// leaves loading messages up for other tabs
//	if (alertsLine.getHTML() == html) {
//	    // == test since don't want two processes that put up the same message
//	    // to interfere
//	    clearAlertsLine();
//	}
//    }
    
    public static void clearAlertsLine() {
	alertsLine.clearAlertsLine();
    }
    
    public static void removeAlerts() {
	alertsLine.removeAlerts();
    }
    
    public static boolean isAlertsLineClear() {
	return alertsLineClear;
    }
    
    public static boolean isAlertsLineEmpty() {
	return alertsLine.isActiveAlertsEmpty();
    }
    
    public static String addAlert(String alert) {
	alertsLine.addAlert(alert);
	return alert;
    }
    
    public static void removeAlert(String alert) {
	alertsLine.removeAlert(alert);
    }
    
    public static void addToErrorLog(String s) {
	instance().addToErrorLogInternal(s);
    }
    
    public static void addWdigetToErrorLog(Widget widget) {
	instance().addWdigetToErrorLogInternal(widget);
    }
    
    public void addWdigetToErrorLogInternal(Widget widget) {
	errorsPanel.add(widget);
    }
    
    public void addToErrorLogInternal(String message) {
	if (message == null || message.isEmpty()) {
	    return;
	}
	if (message.startsWith("Warning")) {
	    Utils.logServerMessage(Level.WARNING, message);
	    Modeller.setAlertsLine(message);
	    return;
	}
	Utils.logServerMessage(CommonUtils.errorIsDueToUser(message) ? Level.WARNING : Level.SEVERE, message);
	if (isAdvancedModeTurnedOff()) { // explicitly turned off -- don't do this if not yet initialised
	    DecoratedPopupPanel decoratedPopupPanel = new DecoratedPopupPanel(true, true);
	    HTML errorMessage = new HTML(CommonUtils.emphasiseError(message));
	    decoratedPopupPanel.setWidget(errorMessage);
	    errorMessage.setSize("640px", "480px");
	    decoratedPopupPanel.setAnimationEnabled(true);
	    decoratedPopupPanel.center();
	    decoratedPopupPanel.show(); 
	    return;
	}
	setAlertsLine(CommonUtils.emphasiseError(constants.encounteredAnError()));
	if (!mainContentsPanel.isAttached()) {
	    // problem reported by the server but haven't fully initialised
	    Utils.popupMessage(message);
//	    RootLayoutPanel.get().add(html);
//	    return;
	}
	HTML html = new HTML(message);
	errorsPanel.insert(html, 0);
	if (!errorsPanel.isAttached()) {
	    ModellerTabPanel mainTabPanel = instance().getMainTabPanel();
	    ClosableTab tabNameWidget = 
		new ClosableTab(constants.errors(), errorsPanel, mainTabPanel);
	    mainTabPanel.add(errorsPanel, tabNameWidget);
	    // following the probable cause of Issue 666
//	    if (!mainTabPanel.isAttached()) {
//		RootLayoutPanel.get().add(mainTabPanel);
//	    }
	    errorsPanel.setSpacing(6); // to separate messages
	    // very rarely useful and too confusing
//	    if (!earlierVersionPanelAdded) {
//		FlowPanel earlierVersionPanel = new FlowPanel();
//		InlineHTML earlierVersionHTML = new InlineHTML(constants.ifThisWorkedInEarlierVersions());
//		earlierVersionPanel.add(earlierVersionHTML);
//		earlierVersionPanel.add(new InlineHTML("&nbsp;"));
//		earlierVersionPanel.add(createSettingsLink());
//		errorsPanel.add(earlierVersionPanel);
//		earlierVersionPanelAdded = true;
//	    }
	}
	switchToErrorsArea();
    }

    public abstract ModellerTabPanel getMainTabPanel();

    public static CheckBox createReloadWithoutHistory() {
	CheckBox option = new CheckBox(constants.reloadWithoutHistory());
	option.setTitle(constants.thisWillReloadYourCurrentSessionQuicklyButTheHistoryPanelWillBeReset());
	option.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		ModelRunner modelRunner = new ModelRunner() {

		    @Override
		    public void runModelNow(String modelXML, 
			                    boolean run,
			                    boolean share,
			                    RunDownloadSharePanel initiatingPanel,
			                    String alert) {
			String modelGuid = BehaviourComposer.getLastModelGuid();
			if (modelGuid == null) {
			    AsyncCallbackNetworkFailureCapable<String[]> callback = new AsyncCallbackNetworkFailureCapable<String[]>() {
				@Override
				public void onSuccess(String[] result) { 
				    super.onSuccess(result);
				    String guidOrError = result[0];
				    if (CommonUtils.isErrorResponse(guidOrError)) {
					Modeller.setStatusLineHTMLError(constants.failedToRunModelToRemoveHistory() + " " + guidOrError);
					return;				    
				    }
				    reloadWithModelGuid(guidOrError);
				}

			    };			
			    BehaviourComposer.instance().runModelNow(modelXML, 
				    run,
				    share, 
				    initiatingPanel,
				    alert,
				    callback);
			} else {
			    BehaviourComposer.setLastModelGuid(null);
			    reloadWithModelGuid(modelGuid);
			}
		    }
		    
		};
		BehaviourComposer.instance().runModel(false, false, null, modelRunner);
	    }
		
	});
	return option;
    }
    
    private static void reloadWithModelGuid(String modelGuid) {
	String url= Window.Location.getHref();
	url = CommonUtils.removeBookmark(url);
	url = CommonUtils.addAttributeToURL(url, "share", "new");
	url = CommonUtils.addAttributeToURL(url, "frozen", modelGuid);
	url = CommonUtils.addAttributeToURL(url, "sessionGuidToBeReplaced", sessionGuid);
	Window.Location.replace(url);
    }
    
    public static CheckBox createOldVersionOption() {
	CheckBox option = new CheckBox(constants.reloadWithPreviousVersionOfTheModelling4AllSoftware());
	option.setTitle(constants.thisWillReloadYourCurrentSessionInThePreviousReleaseOfTheSoftware());
	option.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		String queryString = Window.Location.getQueryString();
		// while in principle one can go back multiple versions they are not always kept
		// maybe by keeping a list of here it will be clearer which versions to keep
		// 403, 414, 423, 448, 492, 497, 518, 536, 545, 560, 568, 593, 600, 623, 636, 642, 647, 666, 719
		Window.Location.replace("http://761.m4a-gae.appspot.com/m/" + queryString);
	    }
		
	});
	return option;
    }
    
    public static CheckBox webNetLogoVersionOption() {
	final CheckBox option = new CheckBox(constants.generateWebNetLogoCompatibleCode());
	option.setTitle(constants.clickIfYouWantToRunYourModelInTheWebVersionOfNetLogo());
	option.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		Modeller.forWebVersion = option.getValue();
		Modeller.forWebVersionRequested = Modeller.forWebVersion;
	    }
		
	});
	option.setValue(Modeller.forWebVersion);
	return option;
    }
//    
       
    public void splitPanelFromRest(Widget panel, boolean split, boolean horizontally) {
	if (split && splitPanel != null) {
	    // already split -- presumably with a different panel so unsplit that one first
	    unsplitPanel(true);
	}
	if (split) {
	    splitPanel = new ModellerSplitLayoutPanel();
	    if (panel instanceof BrowsePanel) {
		// don't add the '+' tab
		((BrowsePanel) panel).setTemporary(true);
	    }
	    mainTabPanel.remove(panel);
	    restScrollPanelAfterSplit = new ScrollPanel(mainContentsPanel);
	    restScrollPanelAfterSplit.setStylePrimaryName("modeller-split-panel-scroll-panel");
	    otherScrollPanel = new ScrollPanel(panel);
	    otherScrollPanel.setStylePrimaryName("modeller-split-panel-scroll-panel");
	    int halfClientWidth = Window.getClientWidth()/2;
	    if (horizontally) {
		splitPanel.addEast(restScrollPanelAfterSplit, halfClientWidth-10);
		splitPanel.add(otherScrollPanel);	
	    } else {
		splitPanel.addNorth(restScrollPanelAfterSplit, getMainTabPanelHeight()/2);
		splitPanel.add(otherScrollPanel);
	    }
	    otherScrollPanel.scrollToTop();
	    splitPanelOtherWidget = panel;
	    wholePanel.insert(splitPanel, 1);
	    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
		@Override
		public void execute() {
		    splitterMoved();
		}
	    });
//	    RootPanel.get().add(splitPanel);
//	    if (horizontally) {
//		restScrollPanelAfterSplit.setPixelSize(halfClientWidth-20, clientHeight);
//		wholePanel.resize();
//		// subtract some for room for the scroll bar
//		otherScrollPanel.setWidth(halfClientWidth-35 + "px");
//		panel.setWidth(halfClientWidth-60 + "px");
//	    } else {
////		restScrollPanelAfterSplit.setHeight(clientHeight + "px");
//		panel.setPixelSize(wholePanel.getOffsetWidth()-10, wholePanel.getOffsetHeight()/2);	
//	    }
	    // good idea to go to resource area
	    // either one tab has been removed
	    // or Composer Area has been removed
	    // so may as well then go to Resources Area
	    switchToResourcesPanel();
	    mainTabPanel.selectTab(0);
	} else {
	    unsplitPanel(true);
	}
	// following prevents unneeded horizontal scroll bars from appearing
	// there is probably a cleaner way to computing all this
	resize();
//	recomputeBrowsePanelHeights();
    }

    public void unsplitPanel(boolean removeTicks) {
	if (splitPanel == null) {
	    return;
	}
	if (splitPanelOtherWidget instanceof BrowsePanel) {
	    BrowsePanel browsePanel = (BrowsePanel) splitPanelOtherWidget;
	    if (removeTicks) {
		browsePanel.getSplitScreenCheckBox().removeTicks();
	    }
	    browsePanel.setTemporary(false);
	    // could restore the old position...
	    ClosableTab tabWidget = browsePanel.getTabWidget();
	    mainTabPanel.add(browsePanel, tabWidget);
	    tabWidget.setVisible(true);
	}
	splitPanel.removeFromParent();
	mainContentsPanel.setWidget(mainTabPanel);
	splitPanel = null;
	wholePanel.insert(mainContentsPanel, 1);
	resize();
    }
    
    protected void resize() {
	if (splitPanel == null) {
	    mainTabPanel.resize();
	    Widget mainContents = mainContentsPanel.getWidget();
	    if (mainContents instanceof ScrollPanel) {
		((ScrollPanel) mainContents).setPixelSize(Modeller.instance().getMainTabPanelWidth(), 
	                                                  Modeller.instance().getMainTabPanelHeight()-4);
	    } 
	}
	CustomisationPopupPanel.arrangePopupPanels();
//	if (splitPanel != null) {
//	    splitPanel.resize();
//	    int mainTabPanelWidth = getMainTabPanelWidth();
//	    int mainTabPanelHeight = getMainTabPanelHeight();
//	    if (isSplitHorizontally()) {
////		mainTabPanel.setPixelSize(halfClientWidth-20, getMainTabPanelHeight());
//		// subtract some for room for the scroll bar
//		otherScrollPanel.setPixelSize(mainTabPanelWidth-8, mainTabPanelHeight);
////		splitPanelOtherWidget.setPixelSize(mainTabPanelWidth-32, mainTabPanelHeight);
//		restScrollPanelAfterSplit.setPixelSize(mainTabPanelWidth-8, mainTabPanelHeight);
////		mainContentsPanel.setPixelSize(mainTabPanelWidth-32, mainTabPanelHeight);
//		// defer this since hasn't yet been moved to its place in the split panel
//		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//		    @Override
//		    public void execute() {
//			restScrollPanelAfterSplitLeft = restScrollPanelAfterSplit.getAbsoluteLeft();
//		    }
//		});	
//	    } else {
//		otherScrollPanel.setPixelSize(mainTabPanelWidth, mainTabPanelHeight-10);
//		restScrollPanelAfterSplit.setPixelSize(mainTabPanelWidth, getMainTabPanelHeight(false)/2);
//		// defer this since hasn't yet been moved to its place in the split panel
//		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//		    @Override
//		    public void execute() {
//			otherScrollPanelTop = otherScrollPanel.getAbsoluteTop();
//		    }
//		});		
//	    }
//	} else {
//	    BehaviourComposer.resourcesTabPanel.resize();
//	    BehaviourComposer.composerPanel.resize();
//	}
//	alertsLine.resize();
//	BehaviourComposer.commandBarPanel.resize();
    }
    
    public void splitterMoved() {
	if (isSplitHorizontally()) {
	    int offsetWidth = splitPanel.getOffsetWidth();
	    String width = offsetWidth-(mainTabPanel.getAbsoluteLeft()+splitPanel.getSplitterSize()) + "px";
//	    mainTabPanel.remove(BehaviourComposer.composerPanel);
//	    mainTabPanel.insert(BehaviourComposer.composerPanel, 0);
	    mainTabPanel.setWidth(width);
	} else {
	    int height = splitPanelOtherWidget.getAbsoluteTop()-(mainTabPanel.getAbsoluteTop()+splitPanel.getSplitterSize());
	    mainTabPanel.setPixelSize(Window.getClientWidth()-10, height);
	}
//	if (isSplitHorizontally()) {
//	    int restScrollPanelAfterSplitLeftNow = restScrollPanelAfterSplit.getAbsoluteLeft();
//	    int deltaX = restScrollPanelAfterSplitLeftNow-restScrollPanelAfterSplitLeft;
////	    System.out.println(deltaX + " at " + System.currentTimeMillis());
//	    restScrollPanelAfterSplitLeft = restScrollPanelAfterSplitLeftNow;
//	    int oldWidth = restScrollPanelAfterSplit.getOffsetWidth();
//	    int newWidth = oldWidth-deltaX;
//	    String restWidth = Math.max(1, newWidth) + "px";
//	    Utils.setWidthAndSoleContents(restScrollPanelAfterSplit, restWidth);
//	    String otherWidth = Math.max(1, otherScrollPanel.getOffsetWidth()+deltaX) + "px";
//	    Utils.setWidthAndSoleContents(otherScrollPanel, otherWidth);
//	} else {
//	    int otherScrollPanelTopNow = otherScrollPanel.getAbsoluteTop();
//	    int deltaY = otherScrollPanelTopNow-otherScrollPanelTop;
////	    System.out.println(deltaY + " at " + System.currentTimeMillis());
//	    otherScrollPanelTop = otherScrollPanelTopNow;
//	    int oldHeight = otherScrollPanel.getOffsetHeight();
//	    int newHeight = oldHeight-deltaY;
//	    Utils.setHeightAndSoleContents(otherScrollPanel, Math.max(1, newHeight) + "px");
//	    Utils.setHeightAndSoleContents(restScrollPanelAfterSplit, Math.max(1, restScrollPanelAfterSplit.getOffsetHeight()+deltaY) + "px");
//	}
    }
    
    public static boolean partOfSplitPanel(Widget widget) {
	if (splitPanel == null) {
	    return false;
	}
	if (splitPanelOtherWidget == widget) {
	    return true;
	}
	if (splitPanelOtherWidget instanceof ScrollPanel) {
	    ScrollPanel scrollPanel = (ScrollPanel) splitPanelOtherWidget;
	    return scrollPanel.getWidget() == widget;
	}
	Widget ancestor = widget.getParent();
	while (ancestor != null) {
	    if (ancestor == splitPanelOtherWidget) {
		return true;
	    }
	    ancestor = ancestor.getParent();
	}
	// top panel is now "the rest" so no point searching it
	return false;
    }
       
//    public static Widget getSplitPanelOtherWidget() {
//	if (splitPanel != null) {
//	    if (splitPanelOtherWidget instanceof ScrollPanel) {
//		ScrollPanel scrollPanel = (ScrollPanel) splitPanelOtherWidget;
//		return scrollPanel.getWidget();
//	    } else if (splitPanelOtherWidget instanceof BrowsePanel) {
//		return splitPanelOtherWidget;
//	    }
//	}
//	return null;
//    }

    public boolean isSplitHorizontally() {
	if (instance() == null) {
	    // during initialization
	    return false;
	}
	return splitPanel != null && 
	       restScrollPanelAfterSplit != null && restScrollPanelAfterSplit.getParent() == splitPanel &&
	       splitPanel.getWidgetDirection(restScrollPanelAfterSplit) == DockLayoutPanel.Direction.EAST;
    }
    
    public boolean isSplitVertically() {
	if (instance() == null) {
	    // during initialization
	    return false;
	}
	return splitPanel != null && 
	       restScrollPanelAfterSplit != null && restScrollPanelAfterSplit.getParent() == splitPanel &&
	       splitPanel.getWidgetDirection(restScrollPanelAfterSplit) == DockLayoutPanel.Direction.NORTH;
    }
    
//    public static boolean inSplitPanelMode() {
//	return splitPanel != null;
//    }

    public void switchToConstructionArea() {	
	// overridden by subclasses 
    }
    
    public void switchToErrorsArea() {	
	// overridden by subclasses 
    }
    
    public static void switchToResourcesPanel() {
	switchTo(BehaviourComposer.resourcesTabPanel, resourcesLink);
    }
      
    /**
     * Note, we defer all application initialization code to {@link #onModuleLoad2()} so that the
     * UncaughtExceptionHandler can catch any unexpected exceptions.
     */
    @Override
    public void onModuleLoad() {
      /*
       * Install an UncaughtExceptionHandler which will produce <code>FATAL</code> log messages
       */
      Log.setUncaughtExceptionHandler();
      // use deferred command to catch initialization exceptions in onModuleLoad2
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
	  @Override
	  public void execute() {
	      onModuleLoad2();
	  }
      });
    }

    private void onModuleLoad2() {
	INSTANCE = this;
	waitCursor();
	String debugParameter = Window.Location.getParameter("debug");
	if (debugParameter != null && debugParameter.equals("1")) {
	    debuggingEnabled = true;
	}
	String warnIfUnknownSessionIdString = Utils.getLocationParameter("warnIfUnknownSessionId");
	warnIfUnknownSessionId = warnIfUnknownSessionIdString == null || !warnIfUnknownSessionIdString.equals("0");
	userGuid = Cookies.getCookie("UserIdentityGuid");
	sessionGuid = Cookies.getCookie("UserIdentity");
	CommonUtils.setHostBaseURL(GWT.getHostPageBaseURL());
	// TODO: should really be a parameter whose value is the local resource file path
	// rather than just a boolean
//	useLocalHost = Utils.getLocationParameter("localhost") != null;
	String internetAccessString = Utils.getLocationParameter("internetAccess");
	internetAccess = internetAccessString == null || !internetAccessString.equals("0");
	String cachingEnabledString = Utils.getLocationParameter("cachingEnabled");
	cachingEnabled = cachingEnabledString != null && cachingEnabledString.equals("1");
	String useAuxiliaryFileString = Utils.getLocationParameter("useAuxiliaryFile");
	useAuxiliaryFile = useAuxiliaryFileString != null && useAuxiliaryFileString.equals("1");
	String forWebVersionString = Utils.getLocationParameter("forWebVersion");
	if (forWebVersionString == null) {
	    // default is web version if mobile device
	    // see http://www.useragentstring.com/pages/Mobile%20Browserlist/
	    String agent = Window.Navigator.getUserAgent();
	    forWebVersion = agent.contains("Android") || agent.contains("BlackBerry") || 
		            agent.contains("Windows Phone") || agent.contains("Nokia") || agent.contains("Opera Mini") ||
		            agent.contains("SonyEricsson") || agent.contains("iPhone") ||
		            BehaviourComposer.epidemicGameMakerMode();
	} else {
	    forWebVersion = forWebVersionString.equals("1");
	    forWebVersionRequested = forWebVersion;
	}
	if (userGuid == null) {
	    // first recorded event will create one and save it
	    // test cookies
//	    Cookies.setCookie("UserIdentityTest", "test");
	    cookiesSaved = false; // "test".equals(Cookies.getCookie("UserIdentityTest"));
	} else {
	    cookiesSaved = true;
	    cookieFound = true;
	    Utils.logServerMessage(Level.INFO, "Session: " + sessionGuid + "; User: " + userGuid);
	}
	final Element googleTranslateElement = DOM.getElementById("google_translate_element");
	translateEnabled = googleTranslateElement != null;
	sessionGuidToBeReplaced = Utils.getLocationParameter("sessionGuidToBeReplaced");
	originalURL = Window.Location.getHref();
	if (originalURL != null) {
	    // url could be null if wrong file name is entered
	    // instead of say index.html
	    int argumentsPosition = originalURL.indexOf('?');
	    if (argumentsPosition > 0) {
		String newSessionGuid = Utils.getLocationParameter("share");
		if (newSessionGuid == null || newSessionGuid.isEmpty()) { // try again with old name
		    newSessionGuid = Utils.getLocationParameter("session");
		}
		initialReadOnlySessionID = Utils.getLocationParameter("copy");
		BehaviourComposer.initialModelID = Utils.getLocationParameter("frozen");
		if (BehaviourComposer.initialModelID == null) {
//		    try again with old name
		    BehaviourComposer.initialModelID = Utils.getLocationParameter("model");
		}
		if ((BehaviourComposer.initialModelID == null || BehaviourComposer.initialModelID.isEmpty())
			&& BehaviourComposer.epidemicGameMakerMode()) {
		    // change in policy about restarting EGM -- always get initial model
//			&& (newSessionGuid == null || newSessionGuid.equals("new"))) {
		    if (Modeller.forWebVersion) {
			BehaviourComposer.initialModelID = "p-H7keNjmSMdYNkwB8A75g";
		    } else {
			BehaviourComposer.initialModelID = "keJ0rKKMf19IqBb6IwYy4_";
		    }
		}
		String newUserGuid = Utils.getLocationParameter("user");
		if (newUserGuid != null) {
		    if (newUserGuid.equals("new")) {
			userGuid = null;
		    } else {
//			Cookies.setCookie("UserIdentityGuid", newUserGuid);
			userGuid = newUserGuid;
			cookieFound = true;
			cookieGivenInURL = true;
		    }
		}
		if (newSessionGuid != null) {	    
		    if (newSessionGuid.equals("new")) {
			startNewSession();
			return;
		    } else {
			sessionGuid = newSessionGuid;
			if (Utils.getLocationParameter("cookiesEnabled") != null) {
			    Cookies.setCookie("UserIdentity", newSessionGuid);
			}
		    }
		}
		pageEditingEnabled = Utils.urlAttributeNotZero("editor", pageEditingEnabled);
		givenName = Utils.getLocationParameter("givenName");
		contextId = Utils.getLocationParameter("contextId");
		if (initialReadOnlySessionID != null || BehaviourComposer.initialModelID != null) {
		    if (sessionGuid == null) {
			// start a new session initialised with a copy of the referenced session
			if (newSessionGuid != null) {
			    sessionGuid = newSessionGuid;
			}
			startNewSession();
			return;
		    } else {
			modelID = initialReadOnlySessionID != null ? initialReadOnlySessionID : BehaviourComposer.initialModelID;
		    }
		}
//		if (sessionGuid != null && initialModelID == null && Utils.getLocationParameter("model") == null) {
//		    // session ID not an attribute but known from cookie
//		    // but don't reload with cookie if loading a model URL
//		    reloadWithSessionID();
//		    return; // probably never called
//		} else 
		if (userGuid == null || sessionGuid == null) {
		    // not session attribute or cookie
		    // start a new session -- on success will reload the URL with the session ID
		    startNewSession();
		    return;
		}
	    }
	    moduleBaseURL = CommonUtils.getBaseURL(originalURL);
	    if (translateEnabled && moduleBaseURL != null) {
		moduleBaseURL += "/translate.html";
	    } else if (!moduleBaseURL.endsWith("/")) {
		moduleBaseURL += "/"; // index.html is implicit
	    }
	}
	bc2NetLogoChannelToken = Utils.getLocationParameter("bc2NetLogoChannelToken");
	if (bc2NetLogoChannelToken != null) {
	    bc2NetLogoOriginalSessionGuid = Utils.getLocationParameter("bc2NetLogoOriginalSessionGuid");
	}
//	String urlParameter = Utils.getLocationParameter("reconstructedChannelNumber");
//	if (urlParameter != null) {
//	    reconstructedChannelNumber = urlParameter;
//	}
	String netLogo2BCChannelToken = Utils.getLocationParameter("netLogo2BCChannelToken");
	if (netLogo2BCChannelToken != null) {
	    if (netLogo2BCChannelToken.equals("new")) {
		reloadWithNetLogo2BCChannelToken();
		return;
	    }
	    listenToGAEChannel(netLogo2BCChannelToken);
	}
	locale = Utils.getLocationParameter("locale");
	if (locale == null) {
	    locale = "en";
	}
	defaultURLBase = Utils.getLocationParameter("baseURL");
	final String cssURL = Utils.getLocationParameter("css");
	if (cssURL != null) {
	    AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {

		@Override
		public void onFailure(Throwable caught) {
		    Utils.popupMessage("Failed to load CSS file: " + cssURL, true);
		    onModuleLoad3(googleTranslateElement);
		}

		@Override
		public void onSuccess(String[] result) {
		    if (result[1] != null) {
			Utils.popupMessage(result[1], true);
		    }
		    StyleInjector.inject(result[0]);
		    onModuleLoad3(googleTranslateElement);
		}
		
	    };
	    getResourcePageService().fetchURLContents(cssURL, callback);
	} else {
	    onModuleLoad3(googleTranslateElement);
	}
    }
    
    private void onModuleLoad3(Element googleTranslateElement) {
	configure();
	RootLayoutPanel.get().add(wholePanel);
//	BehaviourComposer.commandBarPanel.setWidth(Utils.getAvailableWidth() + "px");
	Window.addResizeHandler(new ResizeHandler() {
	    @Override
	    public void onResize(ResizeEvent event) {
		resize();		
	    }
	});
	debugButton.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		if (debugMessagesHTML) {
		    Label label = new HTML(debugMessages);
		    errorsPanel.add(label);
		} else {
		    Label label = new Label(debugMessages);
		    errorsPanel.add(label);
		}
	    };
	});
	if (translateEnabled) {
	    wholePanel.getElement().insertFirst(googleTranslateElement);
	} 
	languageLink = createLanguageLink();
//	JavaScript.exposeAPItoJavaScript(); 
	// to define JavaScript interface to this application
//	wholePanel.add(statusLine);
	if (modelID == null) {
	    modelID = Utils.getLocationParameter("frozen");
	}
	if (modelID == null || modelID.isEmpty()) { // try again with old name
	    modelID = Utils.getLocationParameter("model");
	}
	ReconstructEventsContinutation continuation = afterReconstructingHistory();
	if (needToReload) {
	    reloadWithSessionID(computeNewURL(true));
	} else {
	    if (sessionGuid != null && modelID == null) {
		addAlert(constants.loadingPleaseWait());
		loadAndReconstructHistory(sessionGuid, true, false, Modeller.IGNORE_NO_EVENTS, true, continuation);
	    } else if (initialReadOnlySessionID != null) {
		loadAndReconstructHistory(initialReadOnlySessionID, true, true, Modeller.IGNORE_NO_EVENTS, true, continuation);
	    } else {
		createInitialContents(false, false);
	    }
	}
	ClosingHandler windowClosingHandler = new ClosingHandler() {

	    @Override
	    public void onWindowClosing(ClosingEvent event) {
		if (NetworkFailure.instance().isOutstandingNetworkFailures()) {
		    event.setMessage(constants.networkMessagesRemain());
		}		
	    }
	    
	};
	Window.addWindowClosingHandler(windowClosingHandler);
    }
    
    protected String computeNewURL(boolean defaultNeedToReload) {
	// subclasses may do more
	return null;	
    }

    protected ReconstructEventsContinutation afterReconstructingHistory() {
	// subclasses may do more
	return null;
    }

    protected void configure() {
	// overridden by subclasses that have specific configuration needs
    }

    protected void startNewSession() {
	cookieFound = false;
	sessionGuid = null;
	needToReload = true;
    }

    // TODO: move this to BehaviourComposer class
    public void loadModel(String modelID, 
	                  boolean unload, 
	                  boolean okToRemoveEmptyModel, 
	                  ArrayList<Boolean> macroBehavioursSelected,
	                  Command continuation,
	                  boolean switchToConstructionArea,
	                  boolean reconstructingHistory) {
	BehaviourComposer.enableRunShareTabs(false);
	fetchInitialModel(modelID, unload, okToRemoveEmptyModel, macroBehavioursSelected, continuation, switchToConstructionArea, reconstructingHistory);
    }

    public void fetchInitialModel(final String modelID, 
	                          final boolean unload,
	                          final boolean okToRemoveEmptyModel, 
	                          final ArrayList<Boolean> macroBehavioursSelected,
	                          final Command continuation,
	                          final boolean switchToConstructionArea,
	                          final boolean reconstructingHistory) {
	final AsyncCallbackNetworkFailureCapable<String> callback = new AsyncCallbackNetworkFailureCapable<String>() {
	    
	    @Override
	    public void onFailure(Throwable caught) {
		BehaviourComposer.enableRunShareTabs(true);
		restoreCursor();
		NetworkFailure.instance().networkFailure(caught, Modeller.constants.loadingModel(), getTimer());
//		Modeller.reportException(caught, "In fetching a model from the server.");
//		if (continuation != null) {
//		    continuation.execute(); // I guess this is reasonable
//		}
	    }

	    @Override
	    public void onSuccess(String result) {
		super.onSuccess(result);
		restoreCursor();
		// tried to postpone the following but Swarming model never restored the tabs
		try {
		    if (result != null) {
			if (CommonUtils.isErrorResponse(result)) {
			    addToErrorLog(result);
			    return;
			}
			if (switchToConstructionArea) {
			    switchToConstructionArea();
			}
			loadModelXML(result, modelID, unload, okToRemoveEmptyModel, macroBehavioursSelected, reconstructingHistory);
		    } else {
			Modeller.addToErrorLog("Could not find a model with the ID: " + modelID);
		    }
		    if (continuation != null) {
			continuation.execute();
		    }
		} finally {
		    BehaviourComposer.enableRunShareTabs(true);
		}
	    }
	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		fetchModel(modelID, callback);
	    }
	    
	});
    }
    
    public void loadModelXML(
	    String model, String modelID, boolean unload, boolean replaceOldModel, ArrayList<Boolean> macroBehavioursSelected, boolean reconstructingHistory) {
	// subclasses do the work
    }
    
    public void removeEmptyModel() {
	if (allPrototypes.size() == 1 && allPrototypes.get(0).isEmpty()) {
	    removeMacroBehaviour(allPrototypes.get(0));   
	}
    }
    
    public CompoundEvent removeAllMacroBehaviours() {
	int count = allPrototypes.size();
	if (count == 0) return null;
	ArrayList<ModellerEvent> events = new ArrayList<ModellerEvent>(count);
	CompoundEvent compoundEvent = new CompoundEvent(events);
	compoundEvent.setAlternativeHTML(Modeller.constants.removedAllPrototypes());
	for (int i = count-1; i >= 0; i--) {
	    // iterative from high to low since this also removes elements from
	    // the list
	    MacroBehaviourView macroBehaviour = allPrototypes.get(i);
	    if (removeMacroBehaviour(macroBehaviour)) {
		events.add(new RemoveMacroBehaviourEvent(macroBehaviour));
	    }
	}
	return compoundEvent;
    }
    
//    public static void addSessionURLHelp() {
//	if (cookie == null) return;
//	if (sessionURLHelpHTML != null) {
//	    sessionURLHelpHTML.removeFromParent();
//	}
//	String help;
//	if (cookiesSaved) {
//	    help = constants.youCanRestoreThisSessionOnAnotherBrowserOrAfterStartingANewSessionByUsingThisURL();
//	} else {
//	    help = constants.youCanRestoreThisSessionNextTimeByUsingThisURL();
//	}
//	String sessionURL = CommonUtils.getServletBaseURL() + "?session=" + cookie;
//	reloadWithSessionID();
//	sessionURLHelpHTML = new HTML(help + HTML_SPACE + 
//		"<a target='_blank' " +
//		"title='" + constants.saveThisLinkToContinueThisSessionAnotherTime() + "' " +
//		"href='" + sessionURL + "'>" + sessionURL + "</a>");
//	if (sessionURLHelpHTML != null) {
//	    splitScreenCheckBoxAndSessionHelp.setSpacing(8);
//	    splitScreenCheckBoxAndSessionHelp.add(sessionURLHelpHTML);
//	}
//	if (!splitScreenCheckBoxAndSessionHelp.isAttached()) {
//	    wholePanel.add(splitScreenCheckBoxAndSessionHelp);
//	}
//    }

    protected void createInitialContents(boolean restoringHistory, boolean failedToFetchHistory) {
	BrowserHistory.enableHistory();
	if (!restoringHistory && !failedToFetchHistory) {
	    // TODO: restore history to MoPiX
	    // if failedToFetchHistory then don't create a new session since presumably
	    // the old one is still there just a network problem
	    new StartEvent(readOnlySessionID, initialReadOnlySessionID, BehaviourComposer.initialModelID).addToHistory();
	    if (modelID != null) {
		Command continuation = null;
		if (BehaviourComposer.epidemicGameMakerMode()) {
		    continuation = new Command() {

			@Override
			public void execute() {
			    BehaviourComposer.instance().hideEpidemicGameDetails();
			    BehaviourComposer.instance().setInterfaceEnabled(true);
			    Modeller.setAlertsLine(Modeller.constants.tryMakingYourGame() + "<br>" + Modeller.constants.clickFollowingToImproveGame());
			}
			
		    };
		}
		loadModel(modelID, false, true, null, continuation, false, false);
	    }
	}
	if (sessionGuid == null) {
	    return;
	}
//	com.google.gwt.user.client.Element loadingMessage = DOM.getElementById("initialloadingmessage");
//	if (loadingMessage != null) {
//	    com.google.gwt.user.client.Element parentElement = loadingMessage.getParentElement();
//	    DOM.removeChild(parentElement, loadingMessage);
////	    loadingMessage.setInnerHTML("");
//	}
	Element loadingMessage = DOM.getElementById("initialloadingmessage");
	if (loadingMessage != null) {
	    Element parentElement = loadingMessage.getParentElement();
	    parentElement.removeChild(loadingMessage);
//	    loadingMessage.setInnerHTML("");
	}
//	wholePanel.setSpacing(2);
	// if commandBar is made full width then since it is a table each element is spread out
	// using commandBarPanel works around this
	// TODO: move this BehaviourComposer.java
	BehaviourComposer.commandBarPanel.setWidget(BehaviourComposer.commandBar);
	BehaviourComposer.commandBarPanel.setStylePrimaryName("modeller-command-bar-panel");
	VerticalPanel commandBarAndAlertsLine = new VerticalPanel();
	commandBarAndAlertsLine.setSpacing(6);
	commandBarAndAlertsLine.add(BehaviourComposer.commandBarPanel);
	Image modelling4allIcon = new Image(BehaviourComposer.resources().modelling4AllIcon());
	modelling4allIcon.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		Window.open("/community/index.html", "_blank", "");
	    }
	});
	modelling4allIcon.addStyleName("modelling-m4a-icon");
	modelling4allIcon.setTitle(constants.openM4AWebSite());
	iconAndCommandBarAndAlertsLine.setSpacing(6);
	iconAndCommandBarAndAlertsLine.add(modelling4allIcon);
	iconAndCommandBarAndAlertsLine.add(commandBarAndAlertsLine);
	wholePanel.insert(iconAndCommandBarAndAlertsLine, 0);
//	BehaviourComposer.commandBarPanel.setWidth(Utils.getAvailableWidth() + "px");
	alertsLine.addStyleName("modeller-alerts");
	commandBarAndAlertsLine.add(alertsLine);
//	wholePanel.insert(alertsLine, 1);
	mainContentsPanel.setWidget(mainTabPanel);
	wholePanel.insert(mainContentsPanel, 1);
	explorerPage = Utils.getLocationParameter("explore");
	if (explorerPage == null) {
	    explorerPage = Utils.getLocationParameter("resources"); 
	    // either name is OK
	}
	if (explorerPage == null) {
	    String localValue = Utils.getLocationParameter("localhost");
	    if (localValue != null && !localValue.equals("0")) {
		explorerPage = "m/Code.html";
	    }
	}
	if (explorerPage == null) {
	    explorerPage = defaultExplorerPage;
	}
	HorizontalPanel editorControlPanel = new HorizontalPanel();
	editorControlPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
//	editorControlPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	historyPanel.setTitle(constants.theHistoryOfYourActionsWithTheMostRecentFirst());
//	historyPanel = new ScrollPanel(historyPanelContents);
//	tabPanel.add(historyPanel, constants.history());
	String searchPage = Utils.getLocationParameter("search");
	if (searchPage == null) {
	    searchPage = ""; // http://www.google.com";;
//	    if (locale != null && !locale.startsWith("en") && locale.length() >= 2) {
//		searchPage += "/intl/" + locale.substring(0,2) + "/";	
//	    }
	}
	if (!searchPage.isEmpty()) {
	    String defaultResourceURL = defaultResourceURL(searchPage);
	    searchPanel = new BrowsePanel();
//	    tabPanel.add(searchPanel, constants.search()); // just before help panel
	    searchPanel.browseTo(defaultResourceURL);
	}
	String manualPage = Utils.getLocationParameter("manual");
	if (manualPage == null) {
	    manualPage = defaultManualPage;
	}
	if (!manualPage.isEmpty()) {
	    String defaultResourceURL = defaultResourceURL(manualPage);
	    manualPanel = new BrowsePanel();
	    manualPanel.setOkToAddTaggingButtons(false);
	    manualPanel.addStyleName("modeller-wait-cursor");
	    manualPanel.browseTo(defaultResourceURL);
	}
	createCommandBar();
	String tabURL = CommonUtils.extractTabAttribute(originalURL);
	if (tabURL != null && !tabURL.isEmpty()) {
	    String decodedURL = URL.decodeQueryString(tabURL);
	    final BrowsePanel newTabPanel = new BrowsePanel(); // browseToNewTab(decodedURL, null);
	    Command doAfterLoading = new Command() {

		@Override
		public void execute() {
		    ClosableTab tabWidget = null;
		    MicroBehaviourView microBehaviour = newTabPanel.getMicroBehaviour();
		    if (microBehaviour == null) {
			tabWidget = 
			    new ClosableTab(
				    Utils.goodTabNameFromURL(newTabPanel.getCurrentURL()), 
				    newTabPanel, 
				    Modeller.mainTabPanel);
			newTabPanel.setTabWidget(tabWidget);
		    }
		    switchToResourcesPanel();	    
		    tabWidget = newTabPanel.getTabWidget();
		    setAdvancedMode(!BehaviourComposer.epidemicGameMakerMode());
		    mainTabPanel.switchTo(newTabPanel);
		    if (tabWidget != null) {
			tabWidget.setVisible(true);
		    }
		}
		
	    };
	    newTabPanel.loadNewURL(decodedURL, doAfterLoading);    
	} else if (manualPanel != null && explorerPage == defaultExplorerPage) {
	    switchTo(manualPanel, helpLink);
	} else {
	    setAdvancedMode(true);
	    instance().switchToConstructionArea();
	}
	if (!BehaviourComposer.epidemicGameMakerMode()) {
	    fetchPreviousSessions();
	}
    }

    public void fetchPreviousSessions() {
	final AsyncCallbackNetworkFailureCapable<ArrayList<String>> callback = 
		new AsyncCallbackNetworkFailureCapable<ArrayList<String>>() {
	    
	    @Override
	    public void onSuccess(ArrayList<String> sessions) {
		NetworkFailure.instance().networkOK(getTimer());
		if (BehaviourComposer.modelsPanel == null) {
		    BehaviourComposer.modelsPanel = new ModelsPanel(sessions, userGuid, sessionGuid, moduleBaseURL);
		    // can't be otherwise -- this is the only code to construct a ModelsPanel
//		} else {
//		    BehaviourComposer.modelsPanel.populateGrid();
		}
		// use first (i.e. current) session info tab contents
		String infoTab = BehaviourComposer.infoPanel.getContents();
		if (infoTab == null || infoTab.isEmpty()) {
		    infoTab = BehaviourComposer.modelsPanel.getInfoTab();
		}
		if (infoTab == null || infoTab.isEmpty()) {
		    BehaviourComposer.infoPanel.initialise();
		} else {
		    BehaviourComposer.infoPanel.setContents(infoTab);
		}
		BehaviourComposer.modelsPanel.setInfoTab(infoTab);
		BehaviourComposer.modelsPanel.setDescription(originalModelDescription);
		Modeller.instance().switchToTabIfIndicatedInURL();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
		    @Override
		    public void execute() {
			resize();
		    }
		});
	    }		    
	    
	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		getHistoryService().fetchAllSessionsOfUser(userGuid, warnIfUnknownSessionId, internetAccess, callback);
	    }
	    
	});
    }
  
    protected void createCommandBar() {
	// subclasses can do this
    }

    public static void becomeHelpLink(Anchor anchor) {
	anchor.setTitle(constants.helpLinkTitle());
	anchor.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		switchTo(manualPanel, helpLink);
	    }
	    
	});
    }

    public static void becomeSearchLink(Anchor anchor) {
	anchor.setTitle(constants.searchLinkTitle());
	anchor.addClickHandler(new ClickHandler() {

	@Override
	public void onClick(ClickEvent event) {
	    switchTo(searchPanel, searchLink);
	}

	});
	BehaviourComposer.commandBar.add(searchLink);
    }

    public static void becomeModelsLink(Anchor anchor) {
	anchor.setTitle(constants.yourModelsLinkTitle());
	anchor.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		switchTo(BehaviourComposer.modelsPanel, BehaviourComposer.modelsLink);
	    }
	    
	});
    }

    public static void becomeHistoryLink(Anchor anchor) {
	anchor.setTitle(constants.modelHistoryLinkTitle());
	anchor.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		switchTo(historyPanel, modelHistoryLink);
	    }
	    
	});
    }

    public static void becomeResourcesLink(Anchor anchor) {
	anchor.setTitle(constants.resourcesLinkTitle());
	ClickHandler resourcesClickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		instance().switchToConstructionArea();
	    }

	};
	anchor.addClickHandler(resourcesClickHandler);
    }
    
    public static void becomeSettingsLink(final CommandAnchor anchor) {
	anchor.setTitle(constants.settingsLinkTitle());
	anchor.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		switchTo(optionsPanel, settingsLink);
	    }
	    
	});
    }
    
    public static Anchor createSettingsLink() {
	final CommandAnchor anchor = new CommandAnchor(constants.settings());
	becomeSettingsLink(anchor);
	return anchor;
    }
    
    public static void becomeLanguageLink(final CommandAnchor anchor) {
	if (Modeller.instance().isTranslateEnabled()) {
	    anchor.setTitle(constants.noTranslateTitle());
	} else {
	    anchor.setTitle(constants.translateTitle());
	}
	anchor.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		toggleTranslatorEnabled();
//		changeLocale(constants.otherLocale());
	    }
	    
	});
    } 
    
    protected static void changeLocale(String otherLocale) {
	String url = Window.Location.getHref();
	int localeStart = url.indexOf("locale=");
	String newURL;
	if (localeStart < 0) {
	    int parameterStart = url.indexOf("?");
	    newURL = url.substring(0, parameterStart) + "?locale=" + otherLocale + "&" + url.substring(parameterStart+1);
	} else {
	    int localeEqualsEnd = localeStart+"locale=".length();
	    int localeEnd = url.indexOf("&", localeEqualsEnd);
	    newURL = url.substring(0, localeEqualsEnd) + otherLocale + url.substring(localeEnd);
	}
	Window.Location.replace(newURL);
    }
    
    protected static void toggleTranslatorEnabled() {
	if (Modeller.instance().isTranslateEnabled()) {
	    reloadWithoutTranslation();
	} else {
	    reloadWithTranslation();
	}
    }
    
    protected static void reloadWithTranslation() {
	String url = Window.Location.getHref();
	String newURL;
	if (url.indexOf("index.html") >= 0) {
	    newURL = url.replace("index.html", "translate.html");
	} else if (url.indexOf("index-dev.html") >= 0) {
	    newURL = url.replace("index-dev.html", "translate.html");
	} else {
	    int parametersStart = url.indexOf("?");
	    if (parametersStart >= 0) {
		newURL = url.substring(0, parametersStart) + "translate.html" + url.substring(parametersStart);
	    } else {
		newURL = url + "translate.html";
	    }    
	}
	Window.Location.replace(newURL);
    }
    
    protected static void reloadWithoutTranslation() {
	String url = Window.Location.getHref();
	String newURL = url.replace("translate.html", "index.html");
	Window.Location.replace(newURL);
    }
    
    protected static void reloadWithNetLogo2BCChannelToken() {
	AsyncCallback<String> callback = new AsyncCallback<String>() {

	    @Override
	    public void onFailure(Throwable caught) {
		// Not sure what to do		
	    }

	    @Override
	    public void onSuccess(String result) {
		String url = Window.Location.getHref();
		String newURL = CommonUtils.addAttributeToURL(url, "netLogo2BCChannelToken", result);
		Window.Location.replace(newURL);		
	    }
	    
	};
	getResourcePageService().getNetLogo2BCChannelToken(userGuid, sessionGuid, callback);
    }

    public static Anchor createLanguageLink() {
	String label = Modeller.instance().isTranslateEnabled() ? constants.englishOnly() : constants.otherLanguage();
	final CommandAnchor anchor = new CommandAnchor(label);
	anchor.setTitle(constants.clickToReloadInAnotherLanguage());
	becomeLanguageLink(anchor);
	return anchor;
    }
    
    public static void setLastCommand(Anchor link) {
	if (lastCommand != null) {
	    // could use instead
//	    lastCommand.removeStyleDependentName("inactive")
	    lastCommand.removeStyleName("modeller-CommandAnchor-inactive");
	}
	link.addStyleName("modeller-CommandAnchor-inactive");
	lastCommand = link;
    }
    
    public static void switchTo(Widget newContents, Anchor link) {
	if (newContents != Modeller.instance().getMainTabPanel()) {
	    // need the command bar, etc.
	    instance().setAdvancedMode(!BehaviourComposer.epidemicGameMakerMode());
	}
	if (Modeller.partOfSplitPanel(newContents)) {
	    return;
	}
	Widget oldContents = mainContentsPanel.getWidget();
	if (oldContents == mainTabPanel) {
	    mainTabPanel.saveScrollPositionOfCurrentTab();
	}
	if (newContents == mainTabPanel || splitPanel != null) {
	    mainContentsPanel.setWidget(newContents);
	} else {
	    ScrollPanel scrollPanel = new ScrollPanel(newContents);
	    mainContentsPanel.setWidget(scrollPanel);
	}
	setLastCommand(link);
	if (newContents == mainTabPanel) {
	    JavaScript.cleanAnchors(); // work around IE bug
	    // really need it for any page with local anchors
	    if (newContents != oldContents) { 
		mainTabPanel.restoreScrollPositionOfCurrentTab();
	    }
	}
	instance().resize();
    }
    
    public void loadAndReconstructHistory(final String sessionID, 
	                                  final boolean initialising, 
	                                  final boolean readOnly,
	                                  final int whatToIgnore,
	                                  final boolean copyOnUpdate, 
	                                  final ReconstructEventsContinutation continuation) {
	final AsyncCallbackNetworkFailureCapable<String> callback = new AsyncCallbackNetworkFailureCapable<String>() {

	    @Override
	    public void onFailure(Throwable caught) {
		NetworkFailure.instance().networkFailure(caught, Modeller.constants.loadingModel(), getTimer());
//		Modeller.reportException(caught, "In fetching the previous session.");
//		if (initialising) {
//		    createInitialContents(false, true);
//		}
	    }

	    @Override
	    public void onSuccess(String result) {
		super.onSuccess(result);
		if (initialising) {
		    createInitialContents(result != null, false);
		}
		if (result == null) {
		    if (warnIfUnknownSessionId) {
			// only occurrences in the log of this are where a second or two earlier is
			// Recording a new session for DmciOKaGoZljrtAdCavN7f but one already exists in database. Using the old one.
			Modeller.addToErrorLog("Could not find a previous session with an ID of " + sessionID + ". Refresh the page to try again in a few seconds.");
		    }
		    Modeller.instance().switchToConstructionArea();
		    Modeller.setAlertsLine("&nbsp;");
		} else if (CommonUtils.isErrorResponse(result)) {
		    addToErrorLog(result);
		} else {
//		    switchToConstructionArea();
		    ModellerEvent.reconstructHistory(result, true, whatToIgnore, copyOnUpdate, continuation);
		    // now start up process checking for updates if requested.
		    String collaborateOption = Utils.getLocationParameter("collaborate");
		    if (collaborateOption != null && 
			!collaborateOption.isEmpty() && 
			!collaborateOption.equals("0")) {
			setKeepCheckingForNewEvents(true);
		    }	
		}
	    }
	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		getHistoryService().fetchHistory(sessionID, userGuid, readOnly, callback);
	    }
	    
	});
    }
    
    public static boolean adjustLayoutForTranslationBar() {
	Element translationBarIFrame = DOM.getElementById(":2.container");
	if (translationBarIFrame != null) {
	    Element translationBarElement = translationBarIFrame.getParentElement();
	    if (translationBarElement != null) {
		Element bodyElement = translationBarElement.getParentElement();
		if (bodyElement != null) {
		    // following is prevents extra scroll bars from appearing
		    bodyElement.getStyle().setTop(0, Unit.PX);
		    return true;
		}
	    }
	}
	return false;
    }
    
    public void checkForNewEvents() {
	if (!keepCheckingForNewEvents) {
	    return;
	}
	final AsyncCallbackNetworkFailureCapable<String> callback = new AsyncCallbackNetworkFailureCapable<String>() {
	    
	    @Override
	    public void onFailure(Throwable caught) {
		super.onFailure(caught);
//		Modeller.reportException(caught, "fetchHistory failure at " + new Date().toString());
//		checkForNewEvents(); // start check for next change
	    }

	    @Override
	    public void onSuccess(String result) {
		super.onSuccess(result);
		if (CommonUtils.isErrorResponse(result)) {
		    addToErrorLog(result);
		} else if (result != null) {
		    if (!result.isEmpty()) {
			ModellerEvent.reconstructHistory(result, true, Modeller.IGNORE_NO_EVENTS, false, null);
		    }
//		    checkForNewEvents(); // start check for next change
		}
	    }
	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		checkForNewEvents(callback);
	    }
	    
	});
    }

    private void checkForNewEvents(AsyncCallbackNetworkFailureCapable<String> callback) {
	getHistoryService().fetchHistory(sessionGuid, getTimeOfLastUpdate(), userGuid, callback);
    }

    private void listenForSharedEvents() {
	final AsyncCallbackNetworkFailureCapable<String> callback = new AsyncCallbackNetworkFailureCapable<String>() {
	    
	    @Override
	    public void onSuccess(String result) {
		super.onSuccess(result);
		listenToGAEChannel(result);
	    }
	    
	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		getHistoryService().listenForSharedEvents(sessionGuid, userGuid, callback);
	    }
	    
	});
    }
    
    private void listenToGAEChannel(final String token) {
//	System.out.println("BC client listening to " + token); // debug this
	ChannelCreatedCallback channelCallback =
		new ChannelCreatedCallback() {

	    @Override
	    public void onChannelCreated(final Channel channel) {
//		System.out.println("BC client channel created " + token); // debug this
		SocketListener socketListener = new SocketListener() {

		@Override
	        public void onOpen() {
//	            System.out.println("BC noted that channel opened " + token); // debug this
	        }
	        
	        @Override
	        public void onMessage(String message) {
	            String trimmedMessage = message.trim();
		    if ("checkForNewEvents".equals(trimmedMessage)) {
	        	checkForNewEvents();
	            } else if (CommonUtils.MICRO_BEHAVIOUR_UPDATES.equals(trimmedMessage)) {
	        	setAlertsLine(constants.bc2NetLogoChangesReceived());
	        	fetchAndProcessMicroBehaviourUpdates();
	            } else if (trimmedMessage.startsWith(CommonUtils.UPDATE_URL)) {
	        	String newUserAndNewSession[] = trimmedMessage.substring(CommonUtils.UPDATE_URL.length()).split(" ", 3);
	        	String newUser = newUserAndNewSession[0];
	        	String newSession = newUserAndNewSession[1];
	        	String frozenModelGuid = newUserAndNewSession[2];
	        	if (newUser.equals(userGuid) && newSession.equals(sessionGuid)) {
	        	    Utils.popupMessage(constants.sessionAndUserIdsNotChanged());
	        	    return;
	        	}
	        	String newURL = Window.Location.getHref();
	        	newURL = CommonUtils.removeBookmark(newURL);
	        	if (frozenModelGuid.equals("null")) {
	        	    newURL = CommonUtils.addAttributeToURL(newURL, "share", newSession);
	        	    newURL = CommonUtils.addAttributeToURL(newURL, "user", newUser);
	        	} else {
	        	    newURL = CommonUtils.addAttributeToURL(newURL, "user", userGuid);
	        	    newURL = CommonUtils.addAttributeToURL(newURL, "share", "new");
	        	    newURL = CommonUtils.addAttributeToURL(newURL, "sessionGuidToBeReplaced", newSession);
	        	    newURL = CommonUtils.addAttributeToURL(newURL, "frozen", frozenModelGuid);
	        	}
	        	Window.Location.replace(newURL);
	            }
	        }
	      
		@Override
	        public void onError(SocketError error) {
		    // tried many ways to just refresh the channel rather than reload
		    // but none worked
//		    JavaScript.unloadGAEChannelIFrame();
		    // above didn't work so trying the following based upon
		    // http://stackoverflow.com/questions/10919180/channel-api-sometimes-i-dont-get-message
//		    com.google.gwt.user.client.Element iframe = DOM.getElementById("wcs-iframe"); 
//		    if (iframe != null) {
//			iframe.removeFromParent(); 
//		    }
//		    if (channelSocket != null) {
//			channelSocket.close();
//		    } else {
//			return;
//		    }
//		    channel.open(socketListener);
		    if (ignoreChannelErrors) {
			return;
		    }
		    ignoreChannelErrors = true;
	            String description = error.getDescription();
	            Utils.logServerMessage(Level.WARNING, "Socket error on channel " + token + " Error description = " + description + " error code " + error.getCode());
//	            System.out.println("BC connection to channel " + token + " lost. " + description); // debug this
	            String message = "Channel to NetLogo lost. Please wait while we try to create a new communication channel.";
	            if (!description.isEmpty()) {
	        	message += " Reason channel was lost is " + description;
	            }
		    final DecoratedPopupPanel popupMessage = Utils.popupMessage(message);
	            String domain = CommonUtils.getDomainURL(Window.Location.getHref());
	            String urlForNewURL = domain + "/p/bc2netlogo.txt?&user=" + userGuid 
	        	                  + "&session=" + getBc2NetLogoOriginalSessionGuid()
	        	                  + "&domain=" + domain;
	            //  + "&reconnectingFromBC=" + reconstructedChannelNumber
	            AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {

			@Override
			public void onFailure(Throwable caught) {
			    Modeller.addAlert(CommonUtils.stronglyHighlight("The server reported an error when trying to create a new connection to NetLogo. If the problem persists please save your work and restart. Error is " + caught.getMessage()));
			}

			@Override
			public void onSuccess(String[] result) {
			    String newURL = result[0];
			    if (newURL != null) {
				bc2NetLogoChannelToken = CommonUtils.getURLParameter("bc2NetLogoChannelToken", newURL);
				listenToGAEChannel(CommonUtils.getURLParameter("netLogo2BCChannelToken", newURL));
				popupMessage.hide();
//				Modeller.addAlert("Network connection to NetLogo lost and a new connection established.");
				String currentURL = Window.Location.getHref();
				currentURL = CommonUtils.addAttributeToURL(currentURL, "bc2NetLogoChannelToken", CommonUtils.getURLParameter("bc2NetLogoChannelToken", newURL));
				currentURL = CommonUtils.addAttributeToURL(currentURL, "netLogo2BCChannelToken", CommonUtils.getURLParameter("netLogo2BCChannelToken", newURL));
				final String refreshURL = currentURL;
				Button button = new Button(constants.readyToReloadToRestablishConnectionWithNetLogo());
				button.setTitle(constants.readyToReloadToRestablishConnectionWithNetLogoTitle());
				ClickHandler clickHandler = new ClickHandler() {

				    @Override
				    public void onClick(ClickEvent event) {
					Window.Location.replace(refreshURL);
				    }
				    
				};
				button.addClickHandler(clickHandler);
				composerButtonPanel.add(button);
			    } else if (result[1] != null) {
				Modeller.addAlert(CommonUtils.stronglyHighlight("Server reported an error when trying to create a new connection to NetLogo. Save your work and restart. " + result[1]));
			    } else {
				Modeller.addAlert(CommonUtils.stronglyHighlight("Server failed to create a new connection to NetLogo. Save your work and restart."));		
			    }
			}
	        	
	            };
		    getResourcePageService().fetchURLContents(urlForNewURL, callback);
//	            String restartAdvice = "Please save any work, close this tab, and restart. Sorry. ";
//	            if (description.isEmpty()) {
//	        	Modeller.addToErrorLog("Warning. Channel for notifications from server signaled an error but no description given. Perhaps it was a network error. " + restartAdvice);
//	            } else if (!description.startsWith("Token+timed+out")) {
//	        	// Don't want time outs to clutter up the log with errors
//	        	Modeller.addToErrorLog("Error on channel for notifications from server: " + description + " " + restartAdvice);
//	            } else {
//			Modeller.setAlertsLineAndHighlight("Channel to BC2NetLogo has timed out. " + restartAdvice);
//	            }
	        }
		
	        @Override
	        public void onClose() {
//	            System.out.println("Channel closed. Token is " + token); // debug this
	        }
	        
	      };
	      channel.open(socketListener);
	    }
	};
	ChannelFactory.createChannel(token, channelCallback);
    }
    
    private void fetchAndProcessMicroBehaviourUpdates() {
	String staticPagePath = CommonUtils.getStaticPagePath();
	String updatesURL = staticPagePath + "modelDifferences/" + sessionGuid + ".xml";
	AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {

	    @Override
	    public void onFailure(Throwable caught) {
		Utils.popupMessage("Failed to fetch the changes made in BC2NetLogo. Sorry.", true);
	    }

	    @Override
	    public void onSuccess(String[] result) {
		if (result[1] != null) {
		    Utils.popupMessage("Error while fetching the changes made in BC2NetLogo. Sorry. " + result[1], true);
		}
		try {
		    processModelDifferences(result[0]);
		} catch (Exception e) {
		    reportException(e, "An internal error occured processing the changes. Sorry. Perform the edits manually.");
		}
	    }
	    
	};
	getResourcePageService().fetchURLContents(updatesURL, callback);
    }

    protected void processModelDifferences(String xml) {
	if (xml == null || xml.isEmpty()) {
	    Modeller.setAlertsLine("No NetLogo program or interface element differences received.");
	    return;
	}
	Node contents = Utils.parseXML(xml);
	if (contents == null) {
	    // error already reported
	    return;
	}
	boolean anyProcedureDifferences = true;
	boolean anyDeclarationDifferences = true;
	boolean anyWidgetDifferences = true;
	boolean anyInfoDifferences = true;
	String tag = contents.getNodeName();
	if (tag.equals("differences")) {
	    ArrayList<String> namesOfEditedMicroBehaviours = new ArrayList<String>();
	    Node procedureDifferences = Utils.getFirstNodeWithTagName(contents, "procedureDifferences");
	    anyProcedureDifferences = procedureDifferences != null;
	    Node declarationDifferences = Utils.getFirstNodeWithTagName(contents, "declarationDifferences");
	    anyDeclarationDifferences = declarationDifferences != null;
	    Node widgetDifferences = Utils.getFirstNodeWithTagName(contents, "widgetDifferences");
	    anyWidgetDifferences = widgetDifferences != null;
	    Node infoTab = Utils.getFirstNodeWithTagName(contents, "infoTab");
	    anyInfoDifferences = infoTab != null;
	    if (anyProcedureDifferences) {
		NodeList childNodes = procedureDifferences.getChildNodes();
		if (childNodes.getLength() == 0) {
		    anyProcedureDifferences = false;
		} else {
		    updateNextMicroBehaviour(childNodes, 0, namesOfEditedMicroBehaviours, !anyDeclarationDifferences && !anyWidgetDifferences);
		}
	    }
	    if (anyDeclarationDifferences) {
		NodeList childNodes = declarationDifferences.getChildNodes();
		if (childNodes.getLength() == 0) {
		    anyDeclarationDifferences = false;
		} else {
		    updateNextDeclarationMicroBehaviour(childNodes, 0, namesOfEditedMicroBehaviours, anyWidgetDifferences);
		}
	    }
	    if (anyWidgetDifferences) {
		NodeList childNodes = widgetDifferences.getChildNodes();
		if (childNodes.getLength() == 0) {
		    anyWidgetDifferences = false;
		} else {
		    updateNextWidgetMicroBehaviour(childNodes, 0, namesOfEditedMicroBehaviours);
		}
	    }
	    if (anyInfoDifferences) {
		String newTabInfo = infoTab.getFirstChild().getNodeValue();
		BehaviourComposer.infoPanel.setContents(newTabInfo);
	    }
	    if (!anyWidgetDifferences && !anyProcedureDifferences && !anyDeclarationDifferences) {
		if (!anyInfoDifferences) {
		    Modeller.setAlertsLine("No NetLogo program or interface element differences received.");
		} else {
		    Modeller.setAlertsLine(constants.infoTabUpdated());
		}
	    }
	    restoreCursor();
	}
    }

    private void updateNextWidgetMicroBehaviour(final NodeList nodes, final int index, final ArrayList<String> namesOfEditedMicroBehaviours) {
	if (index == nodes.getLength()) {
	    informUserOfEditedMicroBehaviours(namesOfEditedMicroBehaviours);
	    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
		@Override
		public void execute() {
		    mainTabPanel.selectTab(mainTabPanel.getNewBrowsePanel());
		}
	    });
	    return;
	}
	Node node = nodes.item(index);
	if (node instanceof com.google.gwt.xml.client.Element) {
	    final com.google.gwt.xml.client.Element element = (com.google.gwt.xml.client.Element) node;
	    String type = element.getAttribute("type");
	    String oldName = element.getAttribute("oldName");
	    MicroBehaviourView microBehaviourViewOfWidget = getMicroBehaviourViewOfWidget(type, oldName, element);
	    if (microBehaviourViewOfWidget == null) {
		String urls = getURLOfMicroBehaviourForWidgetType(type);
		if (urls != null) {
		    for (String url : urls.split(";")) {
			BrowsePanelCommand command = new BrowsePanelCommand() {

			    @Override
			    public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
				MicroBehaviourView microBehaviour = panel.getMicroBehaviour();
				microBehaviour.updateNetLogoWidget(element, namesOfEditedMicroBehaviours);
				String message = constants.openedAndEditedMicroBehaviour().replace("***name***", microBehaviour.getNameHTML());
				setAlertsLine(CommonUtils.emphasise(message));
				updateNextWidgetMicroBehaviour(nodes, index+1, namesOfEditedMicroBehaviours);
			    }

			};
			Modeller.executeOnNewMicroBehaviourPage(url, command, false, true);
		    }
		    return;
		}
	    }
	    if (microBehaviourViewOfWidget == null) {
		Utils.popupMessage("Updates to the " + type.toLowerCase() + " identified by '" + oldName + 
			           "' not applied. Corresponding micro-behaviour not found. <br>(This bug will be fixed but a workaround is to open the micro-behaviours that should be updated in tabs and try again.)");
	    } else {
		microBehaviourViewOfWidget.updateNetLogoWidget(element, namesOfEditedMicroBehaviours);
	    }
	}
	updateNextWidgetMicroBehaviour(nodes, index+1, namesOfEditedMicroBehaviours);
    }

    protected void informUserOfEditedMicroBehaviours(final ArrayList<String> namesOfEditedMicroBehaviours) {
	if (!namesOfEditedMicroBehaviours.isEmpty()) {
	    String message = Modeller.constants.theFollowingMicroBehavioursWereEdited();
	    // because a button can be defined with either NetLogo code or micro-behaviours it shows up twice here
	    //  if they are both in the list then 'button' is good enough
	    if (namesOfEditedMicroBehaviours.contains("<B>Button</B>") && namesOfEditedMicroBehaviours.contains("<B>NetLogo button</B>")) {
		namesOfEditedMicroBehaviours.remove("<B>NetLogo button</B>");
	    }
	    message += " " + CommonUtils.englishList(namesOfEditedMicroBehaviours);
	    message += ".<br>" + Modeller.constants.beSureTheseMicroBehavioursAreAddedToYourModel();
	    message += " " + Modeller.constants.clickTheHistoryTabForDetails();
	    Modeller.setAlertsLine(message);
	}
    }
    
    private void updateNextDeclarationMicroBehaviour(final NodeList nodes, final int index, final ArrayList<String> namesOfEditedMicroBehaviours, final boolean informUser) {
	if (index == nodes.getLength()) {
	    if (informUser) {
		informUserOfEditedMicroBehaviours(namesOfEditedMicroBehaviours);
	    }
	    return;
	}
	Node node = nodes.item(index);
	if (node instanceof com.google.gwt.xml.client.Element) {
	    final com.google.gwt.xml.client.Element element = (com.google.gwt.xml.client.Element) node;
	    Node newDeclarationNode = Utils.getFirstNodeWithTagName(element, "new");
	    Node oldDeclarationNode = Utils.getFirstNodeWithTagName(element, "old");
	    final String newDeclaration = newDeclarationNode.getFirstChild().getNodeValue();
	    final String oldDeclaration = 
		    oldDeclarationNode == null ? null : oldDeclarationNode.getFirstChild().getNodeValue();
	    MicroBehaviourView microBehaviourViewOfDeclaration = 
		    oldDeclarationNode == null ? null :getMicroBehaviourViewOfDeclaration(newDeclaration, oldDeclaration);
	    if (microBehaviourViewOfDeclaration == null) {
		String url = "http://resources.modelling4all.org/libraries/basic-library/miscellaneous/add-netlogo-declaration";
		BrowsePanelCommand command = new BrowsePanelCommand() {

		    @Override
		    public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
			MicroBehaviourView microBehaviour = panel.getMicroBehaviour();
			microBehaviour.simulateTextAreaUpdate(0, "netlogo-declaration", newDeclaration, oldDeclaration);
			String message = constants.openedAndEditedMicroBehaviour().replace("***name***", microBehaviour.getNameHTML());
			setAlertsLine(CommonUtils.emphasise(message));
			updateNextDeclarationMicroBehaviour(nodes, index+1, namesOfEditedMicroBehaviours, informUser);
		    }

		};
		Modeller.executeOnNewMicroBehaviourPage(url, command, false, true);
		return;
	    } else {
		microBehaviourViewOfDeclaration.simulateTextAreaUpdate(0, "netlogo-declaration", newDeclaration, oldDeclaration);
	    }
	}
	updateNextDeclarationMicroBehaviour(nodes, index+1, namesOfEditedMicroBehaviours, informUser);
    }
    
    public static String getURLOfMicroBehaviourForWidgetType(String type) {
	final String resourceLibrary = "http://resources.modelling4all.org/libraries/";
	if (type.equals("MONITOR")) {
	    return resourceLibrary + "basic-library/interface-gadgets/create-monitor";
	} else if (type.equals("CHOOSER")) {
	    return resourceLibrary + "basic-library/interface-gadgets/create-chooser";
	} else if (type.equals("TEXTBOX")) {
	    return resourceLibrary + "basic-library/interface-gadgets/create-text";
	} else if (type.equals("BUTTON")) {
	    // could be either a micro-behaviour or NetLogo button
	    return resourceLibrary + "basic-library/user-input/netlogo-button-v2;" + 
		   resourceLibrary + "basic-library/user-input/button;" +
		   resourceLibrary + "basic-library/user-input/observer-button";
	} else if (type.equals("SLIDER") || type.equals("INPUTBOX")) {
	    return "http://modelling4all.org/p/en/MB.4/Parameter.html";
	} else if (type.equals("SWITCH")) {
	    return "http://modelling4all.org/p/en/MB.4/Boolean-parameter.html";
	} else if (type.equals("GRAPHICS-WINDOW-LOCATION")) {
	    return resourceLibrary + "basic-library/environment/world-location";
	} else if (type.equals("GRAPHICS-WINDOW-SIZE")) {
	    return resourceLibrary + "basic-library/environment/world-size";
	} else if (type.equals("GRAPHICS-WINDOW-PATCH-SIZE")) {
	    return resourceLibrary + "basic-library/environment/patch-size";
	} else if (type.equals("GRAPHICS-WINDOW-WRAP")) {
	    return resourceLibrary + "basic-library/environment/world-geometry";
	} else if (type.equals("GRAPHICS-WINDOW-VIEW-UPDATE")) {
	    return resourceLibrary + "basic-library/environment/view-update";
	} else if (type.equals("GRAPHICS-WINDOW-FRAME-RATE")) {
	    return resourceLibrary + "basic-library/environment/frame-rate";
	} else if (type.equals("GRAPHICS-SHOW-TICK-COUNTER")) {
	    return resourceLibrary + "basic-library/environment/show-tick-counter";
	} else if (type.equals("GRAPHICS-TICK-COUNTER-LABEL")) {
	    return resourceLibrary + "basic-library/environment/set-tick-counter-label";
	} else if (type.equals("OUTPUT")) {
	    return resourceLibrary + "basic-library/logging/log-area";
	} else {
	    return null;
	}
    }

    public void updateNextMicroBehaviour(NodeList childNodes, int index, ArrayList<String> namesOfEditedMicroBehaviours, boolean informUser) {
	if (index == childNodes.getLength()) {
	    if (informUser) {
		informUserOfEditedMicroBehaviours(namesOfEditedMicroBehaviours);
	    }
	    return;
	}
	Node item = childNodes.item(index);
	if (item instanceof com.google.gwt.xml.client.Element) {
	    com.google.gwt.xml.client.Element element = (com.google.gwt.xml.client.Element) item;
	    String tagName = element.getTagName();
	    if (tagName.equals("procedureChanged")) {
		Node urlNode = Utils.getFirstNodeWithTagName(element, "url");
		Node urlCData = urlNode.getFirstChild();
		String originalURL = urlCData.getNodeValue();
		String procedureName = element.getAttribute("procedureName");
		Node newVersion = Utils.getFirstNodeWithTagName(element, "new");
		Node newVersionCData = newVersion.getFirstChild();
		String newProcedure = newVersionCData.getNodeValue();
		Node oldVersion = Utils.getFirstNodeWithTagName(element, "old");
		Node oldVersionCData = oldVersion.getFirstChild();
		String oldBody = oldVersionCData.getNodeValue();
		// textAreaInfo alternates between text area name and default value
		ArrayList<String> textAreaInfo = new ArrayList<String>();
		Node textAreas = Utils.getFirstNodeWithTagName(element, "textAreas");
		if (textAreas != null) {
		    NodeList textAreaNodes = textAreas.getChildNodes();
		    int length = textAreaNodes.getLength();
		    for (int i = 0; i < length; i++) {
			Node textAreaNode = textAreaNodes.item(i);
			if (textAreaNode instanceof com.google.gwt.xml.client.Element) {
			    com.google.gwt.xml.client.Element textAreaElement = (com.google.gwt.xml.client.Element) textAreaNode;
			    String textAreaName = textAreaElement.getAttribute("name");
			    textAreaInfo.add(textAreaName);
			    Node textAreaFirstChild = textAreaElement.getFirstChild();
			    String textAreaHTML = textAreaFirstChild.getNodeValue();
			    String defaultValue = CommonUtils.removeHTMLMarkup(textAreaHTML);
			    textAreaInfo.add(defaultValue);
			}
		    }
		}
		findAndUpdateMicroBehaviour(procedureName, originalURL, newProcedure, oldBody, childNodes, index, textAreaInfo, informUser, namesOfEditedMicroBehaviours);
	    } else if (tagName.equals("newProcedure")) {
		Widget editor = openNewMicroBehaviourEditor();
		String newProcedure = element.getFirstChild().getNodeValue();
		MicroBehaviourEditorCommand microBehaviourEditorCommand = createEditorCommand(newProcedure, true, childNodes, index, namesOfEditedMicroBehaviours);
		microBehaviourEditorCommand.execute(editor);
	    }
	} else {
	    updateNextMicroBehaviour(childNodes, index+1, namesOfEditedMicroBehaviours, informUser);
	}
    }

    private void findAndUpdateMicroBehaviour(String procedureName,
	                                     final String originalURL, 
	                                     final String newProcedure, 
	                                     final String oldBody, 
	                                     final NodeList childNodes, 
	                                     final int index,
	                                     final List<String> textAreasInfo,
	                                     final boolean informUser,
	                                     final ArrayList<String> namesOfEditedMicroBehaviours) {
	final MicroBehaviourView microBehaviour = getMicroBehaviourViewWithOriginalURL(originalURL);
	if (microBehaviour == null) {
	    addToErrorLog("Warning. Could not find the micro-behaviour to update " + procedureName + ". You will need to copy and paste those edits from BC2NetLogo to the Behaviour Composer. The URL is " + originalURL);
	    return;
	}
	Command commandAfterLoading = new Command() {

	    @Override
	    public void execute() {
		String remainingNewProcedure = microBehaviour.applyEdits(newProcedure, oldBody, textAreasInfo, namesOfEditedMicroBehaviours);
		// ] observed in some cases but not sure what the general test is for nothing really remaining.
		if (remainingNewProcedure == null || remainingNewProcedure.isEmpty() || remainingNewProcedure.equals("]")) {
		    updateNextMicroBehaviour(childNodes, index+1, namesOfEditedMicroBehaviours, informUser);
		} else if (originalURL.contains(CommonUtils.EDITED_HTML)) {
		    setAlertsLine(constants.editsFromBC2NetLogoCopiedHere());
		    MicroBehaviourEditorCommand microBehaviourEditorCommand = 
			    createEditorCommand(remainingNewProcedure, false, childNodes, index, namesOfEditedMicroBehaviours);
		    microBehaviour.openMicroBehaviourEditor(microBehaviourEditorCommand, false);
		} else {
		    // warn that some edits can't be applied?
		    updateNextMicroBehaviour(childNodes, index+1, namesOfEditedMicroBehaviours, informUser); 
		}
	    }
	    
	};
	microBehaviour.openInBrowsePanel(true, commandAfterLoading, true);
    }

    public MicroBehaviourEditorCommand createEditorCommand(final String newProcedure,
	                                                   final boolean openAfterSaving,
	                                                   final NodeList childNodes,
	                                                   final int index,
	      	                                           final ArrayList<String> namesOfEditedMicroBehaviours) {
	return new MicroBehaviourEditorCommand() {

	    @Override
	    public void execute(Widget widget) {
		if (widget instanceof MicroBehaviourEditor) {
		    MicroBehaviourEditor editor = ((MicroBehaviourEditor) widget);
		    editor.getCodeArea().setText(newProcedure);
		    Command commandOnExit = new Command() {

			@Override
			public void execute() {
			    updateNextMicroBehaviour(childNodes, index+1, namesOfEditedMicroBehaviours, false);
			    restoreCursor();
			}

		    };
		    editor.setOpenAfterSaving(openAfterSaving);
		    editor.setCommandOnExit(commandOnExit);
		}
	    }

	};
    }
    
    public Widget openNewMicroBehaviourEditor() {
	NewMicroBehaviourBrowsePanel browsePanel = new NewMicroBehaviourBrowsePanel();
	Widget widget = browsePanel.createAndSwitchToEditor(true, true, true);
	if (widget instanceof RichTextEntry) {
	    ClickHandler addHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
		    Modeller.setAlertsLine(Modeller.constants.newMicroBehaviourBeingSaved());
		}

	    };
	    RichTextEntry richTextEntry = (RichTextEntry) widget;
	    richTextEntry.addSaveButtonClickHandler(addHandler);
	    // leave a little for scroll bars, etc.
	    String width = Modeller.instance().getMainTabPanelWidth()-100 + "px";
	    richTextEntry.getRichTextArea().setWidth(width);
	}
	return widget;
    }

    public MacroBehaviourView createMacroBehaviour() {
	return createMacroBehaviour(null);
    }
    
    /**
     * @param name
     *                HTML for the MacroBehaviourView's name (if null a name
     *                will be generated)
     */
    public MacroBehaviourView createMacroBehaviour(String name) {
	if (name == null) {
	    prototypeCounter++;
	    if (allPrototypes.size() >= prototypeCounter) {
		// skip since there are more (probably from reconstructing the
		// history)
		prototypeCounter = allPrototypes.size() + 1;		
	    }
	    name = "Prototype<sub>" + prototypeCounter + "</sub>";
	}
	MacroBehaviourView macroBehaviour = new MacroBehaviourView(name);
	// only top-level macro-behaviours have a how many instances and check box for hide/show
	macroBehaviour.addControlWidgets();
	return macroBehaviour;
    }

    public void addMacroBehaviour(MacroBehaviourView macroBehaviour) {
	String nameHTML = macroBehaviour.getNameHTML();
	MacroBehaviourView macroBehaviourWithHTMLName = this.getMacroBehaviourWithHTMLName(nameHTML);
	if (macroBehaviourWithHTMLName != null) {
	    // already exists one with the same name
	    String newName = nameHTML + "<sub>2</sub>";
	    String message = constants.prototypeNameConflict();
	    message = message.replace("***oldName***", nameHTML);
	    message = message.replace("***newName***", newName);
	    Utils.popupMessage(message, true);
	    macroBehaviour.setHTML(newName);
	    addMacroBehaviour(macroBehaviour);	    
	} else {
	    allPrototypes.add(macroBehaviour);
	    int index = macroBehaviour.getOriginalIndexPosition();
	    if (index >= 0) {
		BehaviourComposer.prototypesPanel.insert(macroBehaviour, index);
	    } else {
		BehaviourComposer.prototypesPanel.add(macroBehaviour);
	    }
	    macroBehaviour.setOriginalIndexPosition(BehaviourComposer.prototypesPanel.getWidgetIndex(macroBehaviour));
	}
    }
    
    public boolean removeMacroBehaviour(MacroBehaviourView macroBehaviour) {
	if (allPrototypes.remove(macroBehaviour)) {
	    macroBehaviour.setOriginalIndexPosition(BehaviourComposer.prototypesPanel.getWidgetIndex(macroBehaviour));
	    BehaviourComposer.prototypesPanel.remove(macroBehaviour);
	    return true;
	} else {
	    System.out.println("Removed a macro-behaviour that was already removed. " + 
		               macroBehaviour.getNameHTML());
	    return false;
	}
    }
    
    public MacroBehaviourView getMacroBehaviourWithName(String name) {
	if (name == null) return null;
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    String otherName = CommonUtils.replaceNonBreakingSpaces(macroBehaviour.getText()).trim();
	    if (name.equals(otherName)) {
		return macroBehaviour;
	    }  
	}
	return null;
    }
    
    public MicroBehaviourView getMacroBehaviourAsMicroBehaviourWithHTMLName(String nameHTML) {
	MacroBehaviourView macroBehaviour = getMacroBehaviourWithHTMLName(nameHTML);
	if (macroBehaviour == null) {
	    return null;
	} else {
	    return new MacroBehaviourAsMicroBehaviourView(macroBehaviour);
	}
    }
    
    public MacroBehaviourView getMacroBehaviourWithHTMLName(String nameHTML) {
	if (nameHTML == null) return null;
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    String otherNameHTML = macroBehaviour.getNameHTML();
	    if (nameHTML.equalsIgnoreCase(otherNameHTML)) {
		return macroBehaviour;
	    }  
	}
	return getMacroBehaviourIgnoringHTML(nameHTML);
    }
    
    public MacroBehaviourView getMacroBehaviourIgnoringHTML(String nameHTML) {
	// compare without HTML or spaces
	// whitespace removed on 7/7/14 because IE, FireFox, and Chrome differ on how to encode new line in prototype name
	nameHTML = CommonUtils.removeHTMLMarkup(nameHTML).replaceAll("\\s","");
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    String otherNameHTML = CommonUtils.removeHTMLMarkup(macroBehaviour.getNameHTML().replace(" ", "")).replaceAll("\\s","");
	    if (nameHTML.equalsIgnoreCase(otherNameHTML)) {
		return macroBehaviour;
	    }
//	    Utils.logServerMessage(Level.SEVERE, nameHTML + " != " + otherNameHTML);
	}
	return null;
    }
    
    public String getMacroBehaviourNames() {
	String names = "";
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    names += macroBehaviour.getNameHTML() + "; ";
	}
	return names;
    }
    
    public int getMacroBehaviourIndex(MacroBehaviourView behaviour) {
	int size = allPrototypes.size();
	for (int i = 0; i < size; i++) {
	    if (allPrototypes.get(i) == behaviour) {
		return i;
	    }  
	}
	return -1;
    }
    
    public int getMacroBehaviourCount() {
	return allPrototypes.size();
    }
    
    public MacroBehaviourView getMacroBehaviourWithIndex(int index) {
	if (index < 0) {
	    return null;
	}
	int size = allPrototypes.size();
	if (index >= size) {
	    return null;
	}
	return allPrototypes.get(index);
    }
    
    public boolean swapPrototypes(int index1, int index2) {
	// returns false if indices are improper
	int largestIndex = allPrototypes.size()-1;
	if (index1 < 0 || index1 > largestIndex) {
	    return false;
	}
	MacroBehaviourView prototype1 = allPrototypes.get(index1);
	if (index2 < 0) {
	    // moving prototype1 to last position
	    for (int i = 0; i < largestIndex; i++) {
		allPrototypes.set(i, allPrototypes.get(i+1));
	    }
	    allPrototypes.set(largestIndex, prototype1);
	    // removes it and puts it at the end
	    BehaviourComposer.prototypesPanel.add(prototype1);
	    return true;
	} else if (index2 > largestIndex) {
	    // moving to first position
	    for (int i = largestIndex; i > 0; i--) {
		allPrototypes.set(i, allPrototypes.get(i-1));
	    }
	    allPrototypes.set(0, prototype1);
	    BehaviourComposer.prototypesPanel.insert(prototype1, 0);
	    return true;
	}
	MacroBehaviourView prototype2 = allPrototypes.get(index2);
	allPrototypes.set(index2, prototype1);
	allPrototypes.set(index1, prototype2);
	if (index2 < index1) {
	    // need to update the earlier one first since otherwise indices will be changed
	    BehaviourComposer.prototypesPanel.insert(prototype1, index2);
	    BehaviourComposer.prototypesPanel.insert(prototype2, index1);
	} else {
	    BehaviourComposer.prototypesPanel.insert(prototype2, index1);
	    BehaviourComposer.prototypesPanel.insert(prototype1, index2);
	}
	return true;
    }
  
    public static void addToHistoryPanel(HistoryItem historyItem) {
	historyPanel.addToHistoryPanel(historyItem);
	historyPanel.setCurrentIndex(historyPanel.historySize()-1);
    }
    
    public void undoLast(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
//	int currentIndex = historyTokenIndex(BrowserHistory.lastHistoryToken);
//	if (currentIndex < 0) {
//	    Modeller.addToDebugMessages("undoLast could not find token: " + BrowserHistory.lastHistoryToken);
// 	}
	int currentIndex = historyPanel.getCurrentIndex();
	// set the following before doing the undo/redo 
	// so continuation sees it
	historyPanel.setCurrentIndex(currentIndex-1);
	undoUpTo(currentIndex, currentIndex-1, record, justRecord, continuation);
    }
    
    public void redoLast(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
//	Widget widget = historyPanelContents.getWidget(0);
//	if (widget instanceof HistoryItem) {
//	    HistoryItem historyItem = (HistoryItem) widget;
//	    historyItem.getEvent().redo(record, continuation);
//	}
//	int currentIndex = historyTokenIndex(BrowserHistory.lastHistoryToken);
	int currentIndex = historyPanel.getCurrentIndex();
	// set the following before doing the undo/redo 
	// so continuation sees it
	historyPanel.setCurrentIndex(currentIndex+1);
	undoUpTo(currentIndex, currentIndex+1, record, justRecord, continuation);
    }

    public void undoUpTo(String toHistoryToken, 
	                 boolean record, 
	                 boolean justRecord,
	                 ReconstructEventsContinutation continuation) {
	int toHistoryTokenIndex = historyTokenIndex(toHistoryToken);
	int fromHistoryTokenIndex = historyPanel.getCurrentIndex();
	if (toHistoryTokenIndex >= 0) {  
	    undoUpTo(fromHistoryTokenIndex,
		     toHistoryTokenIndex,
		     record,
		     justRecord,
		     continuation);
//	} else {
//	    // obsolete?
//	    undoLast(record, continuation);
	}
	Window.setTitle(applicationTitle + ": " + toHistoryToken);
    }

    public static int historyTokenIndex(String historyToken) {
	return historyPanel.historyTokenIndex(historyToken);
    }
    
    public static int historySize() {
	return historyPanel.historySize();
    }
    
    public static BrowsePanel browseToNewTab(String url, HashMap<Integer, String> textAreaValues, ArrayList<MicroBehaviourEnhancement> enhancements) {
	return browseToNewTab(CommonUtils.getFileName(url), textAreaValues, enhancements, null, url, null, null, null, true, false, true, false);
    }
    
    public static BrowsePanel browseToNewTab(String url, boolean fetchAndUpdate, final Command doAfterUpdateCommand) {
	BrowsePanelCommand commandWhenLoaded = null;
	if (fetchAndUpdate) {
	    commandWhenLoaded = new BrowsePanelCommand() {

		@Override
		public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
		    panel.fetchAndUpdate(doAfterUpdateCommand);    
		}

	    };
	}
	return browseToNewTab(CommonUtils.getFileName(url), null, null, null, url, null, null, commandWhenLoaded, true, false, true, false);
    }
    
    public static BrowsePanel browseToNewTab(String tabName, 
	                                     HashMap<Integer, String> textAreaValues,
	                                     ArrayList<MicroBehaviourEnhancement> enhancements,
	                                     String url) {
	return browseToNewTab(tabName, textAreaValues, enhancements, null, url, null, null, null, true, false, true, false);
    }
        
    public static BrowsePanel browseToNewTab(final MicroBehaviourView microBehaviourInstigatingThis, 
                                             HashMap<Integer, String> textAreaValues,
                                             ArrayList<MicroBehaviourEnhancement> enhancements,
                                             final BrowsePanelCommand command,
                                             boolean switchTo,
                                             boolean reuseExistingPanels, 
                                             boolean copyOnTextAreaUpdate) {
	BrowsePanelCommand newCommand = null;
	// after fetching we may discover that the name has changed
	// TODO: determine if this is still possible
	if (!CommonUtils.hasChangesGuid(microBehaviourInstigatingThis.getUrl())) {
	    // no renaming due to copying so update with the latest name
	    newCommand = new BrowsePanelCommand() {
		@Override
		public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
		    if (answer != null && answer.length >= 7 && answer[6] != null) {
			microBehaviourInstigatingThis.setNameHTMLAndDescription(answer[6]);
		    }
		    if (command != null) {
			command.execute(panel, answer, panelIsNew);
		    }
		}

	    };
	} else {
	    newCommand = command;
	}
	return browseToNewTab(microBehaviourInstigatingThis.getNameHTML(), 
		              textAreaValues,
		              enhancements,
		              microBehaviourInstigatingThis.getMacroBehaviourViews(), 
		              microBehaviourInstigatingThis.getUrl(), 
		              microBehaviourInstigatingThis, 
		              null, 
		              newCommand, 
		              switchTo,
		              !reuseExistingPanels, // is this a good policy to consider panels temporary because !reuseExistingPanels??
		              reuseExistingPanels,
		              copyOnTextAreaUpdate || microBehaviourInstigatingThis.isCopyOnUpdate());
    } 

    public static BrowsePanel browseToNewTab(String tabName, 
	                                     HashMap<Integer, String> textAreaValues, 
	                                     ArrayList<MicroBehaviourEnhancement> enhancements,
	                                     String url,
	                                     BrowsePanel browsePanel) {
	return browseToNewTab(tabName, textAreaValues, enhancements, null, url, browsePanel.getMicroBehaviour(), null, null, true, false, true, false);
    }
       
    public static BrowsePanel browseToNewTab(String tabName, 
	                                     final HashMap<Integer, String> textAreaValues,
	                                     ArrayList<MicroBehaviourEnhancement> enhancements,
	                                     ArrayList<MacroBehaviourView> macroBehaviours,
	                                     String url, 
	                                     final MicroBehaviourView microBehaviourInstigatingThis, 
	                                     BrowsePanelEdited commandAfterSaving,
	                                     final BrowsePanelCommand commandWhenLoaded, 
	                                     final boolean switchTo,
	                                     boolean temporary,
	                                     boolean reuseExistingPanels,
	                                     final boolean copyOnUpdate) {
	tabName = tabName.trim(); // otherwise can get "foo" and "foo " tabs
	mainTabPanel.saveScrollPositionOfCurrentTab();
	BrowsePanel browsePanel = urlPanelMap.get(url);
	if (browsePanel != null && !browsePanel.isAttached()) {
	    // ignore detached panels
	    browsePanel = null;
	}
	int panelIndex = !temporary && reuseExistingPanels && browsePanel == null ? 
		         Modeller.mainTabPanel.indexOfBrowsePanelWithURL(url, copyOnUpdate) : 
		         Integer.MIN_VALUE;
	boolean tabUpdated = false;
	// TODO: determine if the following is useful
//	if (panelIndex < 0 && panelIndex != Integer.MIN_VALUE) {
//	    PreviouslyVisitedTab previouslyOpenTab = Modeller.tabPanel.getPreviouslyOpenTab(url);
//	    if (previouslyOpenTab != null) {
//		textAreaValues = previouslyOpenTab.getTextAreaValues();
//		macroBehaviours = previouslyOpenTab.getMacroBehaviourViews();
//		tabName = previouslyOpenTab.getTabName();
//	    }
//	}
	if (browsePanel == null && panelIndex < 0) {
	    final BrowsePanel newBrowsePanel = 
		new BrowsePanel(url, textAreaValues, enhancements, macroBehaviours);
	    if (!reuseExistingPanels) {
		newBrowsePanel.setExpectingANameChange(true);
	    }
	    if (!temporary && 
		(copyOnUpdate || 
		 (macroBehaviours != null && 
		  macroBehaviours.size() > 0 && 
		  !CommonUtils.hasChangesGuid(url)))) {
		// TODO: determine if this is obsolete
		newBrowsePanel.setCopyOnUpdate(true);
		newBrowsePanel.setMicroBehaviourToShareStateWithCopy(microBehaviourInstigatingThis);
	    } else if (copyOnUpdate) {
		newBrowsePanel.setCopyOnUpdate(true);
	    }
	    newBrowsePanel.setTemporary(temporary);
	    browsePanel = newBrowsePanel;
	    if (!temporary) {
		urlPanelMap.put(url, browsePanel);
	    }
	    newBrowsePanel.setOkToRemoveIfTabBarFull(false);
	    final BrowsePanel containingBrowsePanel = microBehaviourInstigatingThis == null ?
		                                      null : 
		                                      microBehaviourInstigatingThis.getContainingBrowsePanel(true);
	    BrowsePanelCommand fullCommandWhenLoaded = new BrowsePanelCommand() {

		@Override
		public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
		    String newTabName = panel.getTabName();
		    if (panel.getCurrentURL().equals("http://resources.modelling4all.org/libraries/basic-library")) {
			// don't use the file name for the default library
			newTabName = constants.library();
		    } 
		    ClosableTab tabNameWidget = new ClosableTab(newTabName, panel, Modeller.mainTabPanel);
		    tabNameWidget.setMicroBehaviour(microBehaviourInstigatingThis);
		    if (!newBrowsePanel.isTemporary()) {
			int widgetIndex = Modeller.mainTabPanel.getSelectedIndex();
			if (containingBrowsePanel == null) {
			    Modeller.mainTabPanel.add(newBrowsePanel, tabNameWidget);
			} else {
			    Modeller.mainTabPanel.insertAfter(containingBrowsePanel, newBrowsePanel, tabNameWidget);
			}
			if (switchTo) {
			    Modeller.mainTabPanel.selectTab(newBrowsePanel);
			    tabNameWidget.setVisible(true);
			} else if (CommonUtils.extractTabAttribute(originalURL) == null){
			    // in case the above unselected this widget
			    // but not if a tab was specified in the URL
			    Modeller.mainTabPanel.selectTab(widgetIndex);
			}
		    }
		    // new pages start off scrolled to the top (and left)	    
		    Window.scrollTo(0, 0);
		    panel.saveScrollPosition();
		    panel.setOkToRemoveIfTabBarFull(true);
		    MicroBehaviourView microBehaviour = panel.getMicroBehaviour();
		    if (microBehaviour != null) {
			if (textAreaValues != null) {
			    microBehaviour.addTextAreaValues(textAreaValues);
			}
		    }
		    if (commandWhenLoaded != null) {
			commandWhenLoaded.execute(panel, answer, panelIsNew);
		    }
		}
		
		@Override
		public void failed() {
		    super.failed();
		    if (commandWhenLoaded != null) {
			commandWhenLoaded.failed();
		    }
		}
		
	    };
	    newBrowsePanel.browseTo(url, false, copyOnUpdate, fullCommandWhenLoaded);
	    if (switchTo) {
		Modeller.mainTabPanel.switchTo(newBrowsePanel);
	    }
	    newBrowsePanel.setDoAfterSaving(commandAfterSaving);
	} else {
	    if (browsePanel == null) {
		Modeller.mainTabPanel.selectTab(panelIndex);
//	        browsePanel = restoreScrollPositionOfCurrentTab(browsePanel);
		Widget widget = Modeller.mainTabPanel.getWidget(panelIndex);
		if (widget != null && widget instanceof BrowsePanel) {
		    browsePanel = (BrowsePanel) widget;
		}
	    } else {
		ClosableTab tabWidget = browsePanel.getTabWidget();
		if (tabWidget != null) {
		    Modeller.mainTabPanel.insert(browsePanel, tabWidget, Math.max(0, Modeller.mainTabPanel.getWidgetCount()-1));
		    tabWidget.setVisible(true);
		}
		tabUpdated = true;
	    }
	    if (browsePanel != null) {
		browsePanel.restoreScrollPosition();
		browsePanel.setDoAfterSaving(commandAfterSaving);
	    }
	    if (commandWhenLoaded != null) {
		commandWhenLoaded.execute(browsePanel, null, false);
	    }
	}
	if (microBehaviourInstigatingThis != null && !tabUpdated) {
	    MacroBehaviourView containingMacroBehaviour = 
		microBehaviourInstigatingThis.getContainingMacroBehaviour();
	    if (containingMacroBehaviour != null) {
		ClosableTab tabWidget = browsePanel.getTabWidget();
		if (tabWidget != null) {
		    tabWidget.setMicroBehaviour(microBehaviourInstigatingThis);
		}
	    }
	}
//	if (switchTo) {
//	    switchToResourcesPanel();
//	    Modeller.instance().getMainTabPanel().switchTo(browsePanel);
//	}
	return browsePanel;
    }

    public static BrowsePanel restoreScrollPositionOfCurrentTab(BrowsePanel browsePanel) {
	Widget widget = Modeller.getSelectedTab();
	if (widget instanceof BrowsePanel) {
	    browsePanel = (BrowsePanel) widget;
	    browsePanel.restoreScrollPosition();
	}
	return browsePanel;
    }
   
    public static BrowsePanel currentBrowsePanel() {
	Widget widget = Modeller.getSelectedTab();
	if (widget instanceof BrowsePanel) {
	    return (BrowsePanel) widget;
	}
	return null;
    }
    
    public void undoUpTo(
	    int fromHistoryItemIndex, 
	    int toHistoryItemIndex,
            boolean record, 
            boolean justRecord,
            ReconstructEventsContinutation continuation) {
	if (!undoUpToInternal(
		fromHistoryItemIndex, 
		toHistoryItemIndex,
		record, 
		justRecord,
		continuation) &&
            continuation != null) {
	    // continuation wasn't taken care of 
	    continuation.reconstructSubsequentEvents(null);
	} 
    }

    private boolean undoUpToInternal(
	    int fromHistoryItemIndex, 
	    int toHistoryItemIndex,
	    boolean record, 
	    boolean justRecord,
	    ReconstructEventsContinutation continuation) {
	// returns true if continuation taken care of
	if (toHistoryItemIndex < 0 && record) {
	    // if replaying history then record will be false
	    // and won't return prematurely
	    return false;
	}
	if (fromHistoryItemIndex < 0) {
	    fromHistoryItemIndex = 0;
	}
	if (BehaviourComposer.epidemicGameMakerMode() && toHistoryItemIndex == 0) {
	    // first history item in EGM is loading basic model -- can't undo that
	    toHistoryItemIndex = 1;
	}
	boolean handledContinuation = false;
	if (fromHistoryItemIndex == toHistoryItemIndex) {
	    return false; // no change
	} else if (fromHistoryItemIndex > toHistoryItemIndex) {
	    if (toHistoryItemIndex < 0) {
		return false;
	    }
	    if (record) {
		// if not recording probably reconstructing
		// so don't switch tabs
		switchToConstructionArea();
	    }
	    for (int i = fromHistoryItemIndex; i > toHistoryItemIndex; i--) {
		HistoryItem historyItem = historyPanel.getHistoryItem(i);
		if (historyItem != null) {
		    historyPanel.setCurrentIndex(i-1);
		    historyItem.getEvent().undo(record, justRecord, continuation);
		    handledContinuation = true;
		    // only do it once (better to do it on the last one?)
		    continuation = null;
		    if (record) {
			Modeller.setAlertsLine(constants.undid() + historyItem.getEvent().toHTMLString(true));
		    }
		}
	    }
	    return handledContinuation;
	} else {
	    if (record) {
		// if not recording probably reconstructing
		// so don't switch tabs
		switchToConstructionArea();
	    }
	    for (int i = fromHistoryItemIndex+1; i <= toHistoryItemIndex; i++) {
		HistoryItem historyItem = historyPanel.getHistoryItem(i);
		if (historyItem != null) {
		    historyPanel.setCurrentIndex(i);
		    historyItem.getEvent().redo(record, justRecord, continuation);
		    handledContinuation = true;
		    // only do it once (better to do it on the last one?)
		    continuation = null;
		    if (record) {
			Modeller.setAlertsLine(constants.redid() + historyItem.getEvent().toHTMLString(true));
		    }    
		}
	    }
	    return handledContinuation;
	}
    }
    
    public static String defaultResourceURL(String relativePath) {
	relativePath = URL.decodeQueryString(relativePath);
	if (CommonUtils.isCompleteURL(relativePath)) {
	    return relativePath.replace(" ", "%20"); // still need to encode spaces
	}
	StringBuilder fullURL = new StringBuilder();
	String staticPagePath = CommonUtils.getStaticPagePath();
	if (relativePath.startsWith("m/")) {
	    // remove p/ at the end
	    staticPagePath = staticPagePath.substring(0, staticPagePath.length()-2);
	    return CommonUtils.joinPaths(staticPagePath, relativePath);
	} else if (!staticPagePath.endsWith("/p/") && !staticPagePath.endsWith("/p")) {
            staticPagePath = CommonUtils.joinPaths(staticPagePath, "p/");
        }
	fullURL.append(staticPagePath);
	String localizedFolder = instance().getLocalizedFolderName();
	fullURL.append(localizedFolder);
	fullURL.append("/");
	fullURL.append(relativePath);	 
	return fullURL.toString();
    }
    
    protected String getLocalizedFolderName() {
	// can depend upon locale (as it does for MoPiX)
	return "en";
    }

    public static void reportException(Throwable caught, String message) {
	caught.printStackTrace();
	if (caught instanceof StatusCodeException) {
	    StatusCodeException statusCodeException = (StatusCodeException) caught;
	    if (statusCodeException.getStatusCode() == 0) {
		Modeller.addToErrorLog(constants.unableToConnectToServer() + " " + message + " " + caught.toString());
		return;
	    } else if (statusCodeException.getStatusCode() == 500) {
		Modeller.addToErrorLog(constants.serverError() + " " + message + " " + caught.toString());	
	    }
	}
	Modeller.addToErrorLog(constants.internalError() + " " + message + " " + caught.toString());
	// in case user wants to try again due to intermittent network connections
	BehaviourComposer.invalidateRunShareTabs();
    }

    public static long getTimeOfLastUpdate() {
        return timeOfLastUpdate;
    }

    public static void setTimeOfLastUpdate(long timeOfLastUpdate) {
        Modeller.timeOfLastUpdate = timeOfLastUpdate;
    }

    public static boolean keepCheckingForNewEvents() {
        return keepCheckingForNewEvents;
    }

    public void setKeepCheckingForNewEvents(boolean keepCheckingForEventUpdates) {
	if (Modeller.keepCheckingForNewEvents == keepCheckingForEventUpdates) {
	    return;
	}
	collaborateButton.setValue(keepCheckingForEventUpdates);
        Modeller.keepCheckingForNewEvents = keepCheckingForEventUpdates;
        if (keepCheckingForEventUpdates) {
            checkForNewEvents();
            listenForSharedEvents();
        }
    }
    
    public static DisclosurePanel createEmbeddingInfo(String title, String info, String help)  {
	VerticalPanel contents = createShareInfoPanel(help, info);
	DisclosurePanel disclosurePanel = new DisclosurePanel(new HTML(title).getText());
	disclosurePanel.add(contents);
	disclosurePanel.setAnimationEnabled(true);
	return disclosurePanel;
    }

    /**
     * @param info
     * @param help
     * @return
     */
    protected static VerticalPanel createShareInfoPanel(String help, String info) {
	VerticalPanel contents = new VerticalPanel();
	if (help != null) {
	    HTML gadgetHelp = new HTML("<b>" + help + "</b>");
	    contents.add(gadgetHelp);
	}
	TextBox infoTextBox = createDisclosureTextBox(info);
	contents.add(infoTextBox);
	return contents;
    }
    
    public static VerticalPanel createLinkInfo(String title, String info, String help)  {
	VerticalPanel infoPanel = createShareInfoPanel(help, info);
	infoPanel.insert(new HTML(title), 0);
	return infoPanel;
    }

    public static TextBox createDisclosureTextBox(String info) {
	TextBox infoTextBox = new TextBox();
	infoTextBox.setText(info);
	int width = Math.max(100, mainTabPanel.getOffsetWidth()-100);
	infoTextBox.setWidth(width + "px");
	return infoTextBox;
    }

    public void changeCursor(String behaviourCursorStyle, String nonBehaviourCursorStyle) {
	if (this.behaviourCursorStyle == behaviourCursorStyle) {
	    return;
	}
	restoreCursor();
	this.behaviourCursorStyle = behaviourCursorStyle;
	this.nonBehaviourCursorStyle = nonBehaviourCursorStyle;
	if (BehaviourComposer.composerPanel != null) {
	    // might be still initialising
	    BehaviourComposer.composerPanel.addStyleName(nonBehaviourCursorStyle);
	}
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    macroBehaviour.addMicroBehaviourCursor(behaviourCursorStyle);
	}
	for (MacroBehaviourView macroBehaviour : MacroBehaviourView.macroBehaviourViews) {
	    macroBehaviour.addMicroBehaviourCursor(behaviourCursorStyle);
	}
    }
    
    public void restoreCursor() {
	if (this.behaviourCursorStyle == null || isLoadingInProgress() || !BehaviourComposer.isInterfaceEnabled()) {
	    return;
	}
	if (BehaviourComposer.composerPanel != null) {
	    BehaviourComposer.composerPanel.removeStyleName(nonBehaviourCursorStyle);
	}
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    macroBehaviour.restoreCursor();
	}
	for (MacroBehaviourView macroBehaviour : MacroBehaviourView.macroBehaviourViews) {
	    macroBehaviour.restoreCursor();
	}
	if (manualPanel != null) {
	    manualPanel.removeStyleName(behaviourCursorStyle);
	}
	this.behaviourCursorStyle = null;
    }
    
    public void waitCursor() {
	changeCursor("modeller-wait-cursor", "modeller-wait-cursor");
    }

    public static ReconstructEventsContinutation getDummyContinuation() {
        return dummyContinuation;
    }

    public static Widget getSelectedTab() {
	int selectedTabIndex = getSelectedTabIndex();
	if (selectedTabIndex < 0) {
	    return null;
	} else {
	    return mainTabPanel.getWidget(selectedTabIndex);
	}
    }
    
    public static int getSelectedTabIndex() {
	return mainTabPanel.getSelectedIndex();
    }
    
    public static Modeller instance() {
	// overridden by subclasses
	return INSTANCE;
    }

    public void widgetSelected(Widget widgetSelected) {
	if (widgetSelected instanceof HasTabPanelSelection) {
	    ((HasTabPanelSelection) widgetSelected).selectedByTabPanel();
	}
    }
    
    public void historyReconstructed()  {
	// subclasses may care
    }
    
    public void reconstructEvent(String tag,
	    com.google.gwt.xml.client.Element eventElement,
	    boolean restoringHistory,
	    int whatToIgnore,
	    boolean copyOnUpdate,
	    ReconstructEventsContinutation continuation, 
	    int version,
	    String name) {
	// subclasses reconstruct events specific the their semantics
    }

    public void dirtyEventAddedToHistoryDirtiedModel() {
	// subclasses do the work 
    }

    public boolean recordingHistoryInDatabase() {
	// overridden by subclasses
	return true;
    }
    
    /* 
     * @return true if did any processing including taking care of next element
     */
    public boolean processNonGenericCodeElement(
	    Element codeElement, 
	    String innerHTML, 
	    BrowsePanel browsePanel,
	    String id,
	    boolean copyOnUpdate,
	    Command command) {
	// subclasses process those HTML elements specific to their context
	return false;
    }
    
    public String textFontToMatchIcons(String html) {
	// subclasses do more
	return html;
    }

    public static BrowsePanel getProtectedBrowsePanel() {
        return protectedBrowsePanel;
    }

    public static void setProtectedBrowsePanel(BrowsePanel protectedBrowsePanel) {
        Modeller.protectedBrowsePanel = protectedBrowsePanel;
    }

    public static SplitLayoutPanel getSplitPanel() {
        return splitPanel;
    }

    public boolean isAdvancedMode() {
        return advancedMode == null || advancedMode;
    }
    
    public boolean isAdvancedModeTurnedOff() {
	// initialised and off
	return advancedMode != null && advancedMode != true;
    }
    
    public void setAdvancedMode(boolean enabled) {
	advancedMode = enabled;
	if (enabled && explorerPage != null && !explorerPage.isEmpty()) {
	    String fullResourcesURL = defaultResourceURL(explorerPage);
	    String tabName = CommonUtils.getFileName(fullResourcesURL);
	    BrowsePanel resourcesBrowsePanel = 
		    browseToNewTab(tabName, null, null, null, fullResourcesURL, null, null, null, false, false, true, false);
	    resourcesBrowsePanel.setOkToRemoveIfTabBarFull(false);
	    mainTabPanel.addNewTabTab();
	    mainTabPanel.switchTo(resourcesBrowsePanel);
	}
    }

    public SessionEventsCheckBox getCheckBoxWithSessionGuid(String checkBoxSessionGuid, String name) {
	return null; // or warn or throw an exception -- sub class should really do this
    }
    
//    public static String localVersionOfURLsIfRunningLocal(String string) {
//	// TODO: determine if this is obsolete
//	if (Modeller.useLocalHost) {
//	    return
//		string.replaceAll(
//			"http://m.modelling4all.org/p", 
//		        "file://c:/eclipse/Modeller/Static Web Pages/Resources");
//	} else {
//	    return string;
//	}
//    }

    protected void reloadWithSessionID(String newURL) {
	// subclasses do the work
    }

    public boolean needToConfirmUnLoad() {
	return !Modeller.cookiesSaved;
    }

    public boolean isGetLinksPanelToBeAdded() {
	return true;
    }

    public boolean isSplitScreenCheckBoxToBeAdded() {
	return !BehaviourComposer.epidemicGameMakerMode();
    }

    public static String getLocale() {
        return locale;
    }
    
    public void switchToTabIfIndicatedInURL() {
	// sub-classes may do something
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getContextId() {
        return contextId;
    }

    public HorizontalPanel getIconAndCommandBarAndAlertsLine() {
        return iconAndCommandBarAndAlertsLine;
    }

    /**
     * @return width of a tab panel
     */
    public int getTabPanelWidth() {
	return getMainTabPanelWidth()-windowWidthMargins;
    }
    
    /**
     * @return width of the entire tab panel
     */
    public int getMainTabPanelWidth() {
	return getMainTabPanelWidth(isSplitHorizontally());
    }
    
    public int getMainTabPanelWidth(boolean splitHorizontally) {
	if (splitHorizontally) {
	    return Window.getClientWidth()/2-splitPanel.getSplitterSize();
	} else {
	    return Window.getClientWidth();
	}
    }

    /**
     * @return height of any tab panel
     */
    public int getTabPanelHeight() {
        if (isSplitVertically()) {
            return getMainTabPanelHeight(true)-ModellerTabPanel.BAR_HEIGHT;
        } else {
            return getMainTabPanelHeight(false)-(ModellerTabPanel.BAR_HEIGHT+50);
        }
    }

    /**
     * @return height of the entire tab panel
     */
    public int getMainTabPanelHeight() {
        return getMainTabPanelHeight(isSplitVertically());
    }

    public int getMainTabPanelHeight(boolean splitVertically) {
	int topPanelHeight = instance().getIconAndCommandBarAndAlertsLine().getOffsetHeight();
	if (isTranslateEnabled()) {
	    topPanelHeight += 20; // empirical value to cover the translation bar
	}
	if (splitVertically) {
            return Window.getClientHeight()/2-(topPanelHeight+-splitPanel.getSplitterSize());
        } else {
            return Window.getClientHeight()-topPanelHeight;
        }
    }
    
    public void setSpecialAlert(Widget widget) {
	if (widget == null) {
	    BehaviourComposer.specialAlert.setVisible(false);
	} else {
	    BehaviourComposer.specialAlert.setWidget(widget);
	    BehaviourComposer.specialAlert.setVisible(true);
	}
    }

    public ArrayList<MacroBehaviourView> getAllPrototypes() {
        return allPrototypes;
    }

    public int getPrototypeCounter() {
        return prototypeCounter;
    }

    public void setPrototypeCounter(int prototypeCounter) {
        this.prototypeCounter = prototypeCounter;
    }

    public static boolean isCachingEnabled() {
        return cachingEnabled;
    }

    public static boolean isInternetAccess() {
        return internetAccess;
    }

    public boolean isTranslateEnabled() {
        return translateEnabled;
    }

    public static boolean isLoadingInProgress() {
        return loadingInProgress;
    }

    public static void setLoadingInProgress(boolean loadingInProgress) {
        Modeller.loadingInProgress = loadingInProgress;
    }

    public String getNetLogo2BCChannelToken() {
        return bc2NetLogoChannelToken;
    }

    public String getBc2NetLogoOriginalSessionGuid() {
	if (bc2NetLogoOriginalSessionGuid == null) {
	    return sessionGuid;
	} else {
	    return bc2NetLogoOriginalSessionGuid;
	}
    }

    public String getOriginalModelDescription() {
        return originalModelDescription;
    }

    public void setOriginalModelDescription(String originalModelDescription) {
        this.originalModelDescription = originalModelDescription;
        if (BehaviourComposer.modelsPanel != null) {
            BehaviourComposer.modelsPanel.setDescription(originalModelDescription);   
        }
    }

    protected boolean isWarnIfUnknownSessionId() {
        return warnIfUnknownSessionId;
    }
    
    public static BrowsePanel getOpenBrowsePanel(String url) {
	BrowsePanel browsePanel = urlPanelMap.get(url);
	if (browsePanel != null && browsePanel.isAttached()) {
	    return browsePanel;
	} else {
	    return null;
	}
    }

    public static void addURLToPanelMap(String url, BrowsePanel browsePanel) {
	urlPanelMap.put(url, browsePanel);	
    }

//    public String getReconstructedChannelNumber() {
//        return reconstructedChannelNumber;
//    }

}
