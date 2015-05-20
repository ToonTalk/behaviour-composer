package uk.ac.lkl.client.event;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Date;
import java.util.logging.Logger;

import uk.ac.lkl.client.AsyncCallbackNetworkFailureCapable;
import uk.ac.lkl.client.BehaviourComposer;
import uk.ac.lkl.client.BrowsePanel;
import uk.ac.lkl.client.BrowsePanelCommand;
import uk.ac.lkl.client.JavaScript;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.HistoryItem;
import uk.ac.lkl.client.RecordSubsequentEventCallback;
import uk.ac.lkl.client.TimerInSequence;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

 
@SuppressWarnings("serial")
public abstract class ModellerEvent extends EventObject {
    public static String addingHistoryToken = ""; 
    public static ModellerEvent noEvent = new StartEvent();
    private static ArrayList<ModellerEvent> compoundEventSubEvents = null;
    protected Date date;
    protected String userGuid = null;
    protected String userName = null;
    protected HistoryItem historyItem = null;

    public ModellerEvent(Object source) {
	super(source);
	date = new Date();
	userGuid = Modeller.userGuid;
    }
    
    public void addToHistory() {
	addToHistory(!BehaviourComposer.epidemicGameMakerMode());
    }
    
    public void addToHistory(final boolean addToStatusLine) {
	if (addToStatusLine) {
	    Modeller.setAlertsLine(toHTMLString(true));
	}
	constructHistory();
	if (dirtyEvent()) {
	    if (eventDirtiesModel()) {
		Modeller.instance().dirtyEventAddedToHistoryDirtiedModel();
	    }
	    final AsyncCallbackNetworkFailureCapable<String[]> eventCallback = 
		    new RecordSubsequentEventCallback();
	    TimerInSequence timer = new TimerInSequence() {

		@Override
		public void run() {
 		    recordInDatabase(eventCallback, true);
		}
		
	    };
	    eventCallback.setAndRunTimer(timer);
	}
    }
    
    public String getDateAttribute() {
	return " date='" + date.getTime() + "'";
    }

    public void constructHistory() {
	if (compoundEventSubEvents != null) {
	    // events need to be in reverse order for CompoundEvent
	    compoundEventSubEvents.add(0, this);
	    return;
	}
	String thisToken = toString();
	if (thisToken == null) {
	    return;
	}
	addingHistoryToken = thisToken;
	History.newItem(addingHistoryToken);
	Window.setTitle(Modeller.applicationTitle + ": " + addingHistoryToken);
	if (dirtyEvent() && Modeller.instance().needToConfirmUnLoad()) {
	    JavaScript.confirmUnload(true);
	}
	HistoryItem historyItem = new HistoryItem(this);
	Modeller.addToHistoryPanel(historyItem);
    }
    
    public String getHistoryItemTitle() {
	return Modeller.constants.clickToJumpHereInHistory() + 
	       " (" + Utils.getTimeDifference(new Date(), date) + " " + Modeller.constants.ago() + ")";
    }

    abstract public String toHTMLString(boolean brief);
    
    public String toHTMLAuthorshipString() {
	if (getUserGuid() == null) { // not known when event was created
	    if (Modeller.userGuid != null) {
		setUserGuid(Modeller.userGuid);
	    } else {
		return "";
	    }
	}
	if (getUserGuid().equals(Modeller.userGuid)) {
	    return "";
	} else {
	    if (getUserName() == null) {
		return " " + Modeller.constants.byUser() + " " + getUserGuid();
	    } else {
		return " " + Modeller.constants.by() + " " + getUserName();
	    }
	}
    }

    public boolean dirtyEvent() {
	// unless override by subclass
	return true;
    }
    
    public boolean eventDirtiesModel() {
	// unless override by subclass
	return true;
    }
    
    abstract public void recordInDatabase(AsyncCallback<String[]> callback, boolean notifyOthers);
       
    public String toString() {
	String htmlString = toHTMLString(false);
	if (htmlString == null) {
	    return null;
	}
	HTML html;
	try {
	    html = new HTML(htmlString);
	} catch (Exception e) {
	    return e.toString() + " in " + htmlString;
	}
	String innerText = html.getElement().getInnerText();
	// following is necessary for FireFox
	// otherwise doesn't recognise self-generated events
	// and undoes them immediately
	innerText = innerText.replaceAll("\n", "");
	return innerText;
    }

    public void undo(final boolean record, final boolean justRecord, final ReconstructEventsContinutation continuation) {
	if (record && dirtyEvent()) {
	    BehaviourComposer.invalidateRunShareTabs();
	    if (Modeller.instance().needToConfirmUnLoad()) {
		// in MoPiX need to keep track of whether the model has been saved
		// so when exiting can get a warning
		JavaScript.confirmUnload(true);
	    }
	    final AsyncCallbackNetworkFailureCapable<String[]> callback = 
		    new RecordSubsequentEventCallback();
	    TimerInSequence timer = new TimerInSequence() {

		@Override
		public void run() {
		    undo(record, justRecord, continuation, callback);
		}
		
	    };
	    callback.setAndRunTimer(timer);
	}
	if (historyItem != null) {
	    historyItem.setUndone(true);
	}
	// subclasses are expected to call this as super
    }
    
    private void undo(final boolean record, final boolean justRecord, final ReconstructEventsContinutation continuation, AsyncCallbackNetworkFailureCapable<String[]> callback) {
	Modeller.getHistoryService().undoOrRedoEvent(Modeller.userGuid, Modeller.sessionGuid, true, true, callback);
//	if (getHistoryItem() != null) {
//	    // make this item grey in the History tab
//	    String historyToken = toHTMLString(false) + toHTMLAuthorshipString();
//	    getHistoryItem().setHTML(CommonUtils.changeColor(historyToken, "#777777"));
//	}
    }
    
    public void redo(final boolean record, final boolean justRecord, final ReconstructEventsContinutation continuation) {
	if (record && dirtyEvent()) {
	    BehaviourComposer.invalidateRunShareTabs();
	    if (Modeller.instance().needToConfirmUnLoad()) {
		JavaScript.confirmUnload(true);
	    }
	    final AsyncCallbackNetworkFailureCapable<String[]> callback = 
		    new RecordSubsequentEventCallback();
	    TimerInSequence timer = new TimerInSequence() {

		@Override
		public void run() {
		    redo(record, justRecord, continuation, callback);
		}
		
	    };
	    callback.setAndRunTimer(timer);
	}
	if (historyItem != null) {
	    historyItem.setUndone(false);
	}
	// expect subclasses to call this as super
    }
    
    private void redo(final boolean record, final boolean justRecord, final ReconstructEventsContinutation continuation, AsyncCallbackNetworkFailureCapable<String[]> callback) {
	Modeller.getHistoryService().undoOrRedoEvent(Modeller.userGuid, Modeller.sessionGuid, false, true, callback);
	if (getHistoryItem() != null) {
	    // restore this item to black in the History tab
	    String historyToken = toHTMLString(false) + toHTMLAuthorshipString();
	    getHistoryItem().setHTML(historyToken);
	}
    }
    
    public static void reconstructHistory(
	    String historyXML, 
	    boolean restoringHistory, 
	    int whatToIgnore, 
	    boolean copyOnUpdate,
	    ReconstructEventsContinutation continuation)  {
	// restoringHistory is false when loading a model
	if (CommonUtils.isErrorResponse(historyXML))  {
	    Modeller.addToErrorLog("Error in fetching history to reconstruct: " + historyXML);
	    return;
	}
	if (historyXML.isEmpty()) {
	    return;
	}
	if (historyXML.length() > 5000) {
	    Modeller.setAlertsLine(Modeller.constants.reconstructingHistoryCanTakeTime());
	}
	int nodesLength = 0;
	try {    
	    Document xml = XMLParser.parse(historyXML);
	    Node node = xml.getFirstChild();
	    if (node != null && node instanceof Element) {
		Element element = (Element) node;
		String lastUpdatedAsString = element.getAttribute("lastUpdated");
		if (lastUpdatedAsString != null && !lastUpdatedAsString.isEmpty()) {
		    try {
			long updateTime = Long.parseLong(lastUpdatedAsString);
			if (updateTime == Modeller.getTimeOfLastUpdate()) {
			    // already processed this -- multiple requests sent out
			    // perhaps due to clicking on check box option a few times
			    return; 
			}
			Modeller.setTimeOfLastUpdate(updateTime);
		    } catch (Exception e) {
			System.err.println("Could not make sense of lastUpdated attribute: " + lastUpdatedAsString);
		    }
		}
		String userIDAsString = element.getAttribute("userID");
		// TODO: determine if this is obsolete
		if (userIDAsString != null && !userIDAsString.isEmpty()) {
		    try {
			int userID = Integer.parseInt(userIDAsString);
			if (userID >= 0) {
			    Modeller.userID = userID;
			}
		    } catch (Exception e) {
			System.err.println("Could not make sense of userID attribute: " + userIDAsString);
		    }
		}
		NodeList nodelist = node.getChildNodes();
		nodesLength = nodelist.getLength();
		if (nodesLength > 0) {
		    Node firstNode = nodelist.item(0);
		    String tag = firstNode.getNodeName();
		    if (tag.equals("parsererror")) {
			Modeller.addToDebugMessages(historyXML);
			Modeller.addToErrorLog("Error parsing history. " + xml.toString());
			return;
		    }
		}
		boolean nonTrivialSession = atLeastNElements(3, nodelist, nodesLength);
		reconstructEvents(
			0, nodesLength, nodelist, restoringHistory, whatToIgnore, copyOnUpdate, nonTrivialSession, continuation);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    Modeller.addToDebugMessages("historyXML is " + historyXML);
	    Modeller.addToErrorLog("Error parsing history " + e.toString());
	    return;
	}
	Modeller.instance().historyReconstructed();
    }

    private static boolean atLeastNElements(int n, NodeList nodelist, int length) {
	int eventCount = 0;
	for (int i = 0; i < length; i++) {
	    if (nodelist.item(i) instanceof Element) {
		eventCount++;
		if (eventCount == n) {
		    return true;
		}
	    }
	}
	return false;
    }

    public static void reconstructEvents(
	    final int index, 
	    final int length, 
	    final NodeList nodelist,
	    final boolean restoringHistory,
	    final int whatToIgnore,
	    final boolean copyOnUpdate,
	    final boolean nonTrivialSession,
	    final ReconstructEventsContinutation oldContinuation) {
	// tail recursive rather than iterative so can embed in a command 
	// so loadModel happens atomically
	// and can reconstruct adding a micro-behaviour to a macro-behaviour on a micro-behaviour page
	if (index == 0 && nonTrivialSession) {
	    Modeller.addAlert(Modeller.constants.restoringYourPreviousSessionPleaseWait());
	}
	if (index >= length) {
	    if (nonTrivialSession || index == 4) {
		// index 4 means that it was very tiny but not really trivial
		Modeller.instance().switchToConstructionArea();
	    }
	    if (restoringHistory) {
		if (!BehaviourComposer.epidemicGameMakerMode()) {
		    // EGM loads base game -- not really 'your' previous session
		    if (nonTrivialSession) {
			Modeller.removeAlert(Modeller.constants.restoringYourPreviousSessionPleaseWait());
		    } else {
			Modeller.removeAlert(Modeller.constants.loadingPleaseWait());
		    }
		}
		Modeller.instance().restoreCursor();
		boolean virginEpidemicGame = 
			BehaviourComposer.epidemicGameMakerMode() && 
			!BehaviourComposer.instance().anyCheckBoxesTicked();
		String message;
		if (virginEpidemicGame) {
		    message = Modeller.constants.tryMakingYourGame() + " " + Modeller.constants.clickFollowingToImproveGame();
		} else if (nonTrivialSession) {
		    message = Modeller.constants.previousSessionRestored();
		} else {
		    message = Modeller.constants.readyToStartModelling();
		}
		String givenName = Modeller.instance().getGivenName();
		if (givenName != null) {
		    message = "Welcome " + givenName + ". " + message;
		}
		Modeller.setAlertsLine(message);
	    } else {
		Modeller.setAlertsLine(Modeller.constants.modelLoaded());
	    }
	    if (oldContinuation != null) {
		oldContinuation.reconstructSubsequentEvents(null);
	    }
	    Modeller.setLoadingInProgress(false);
	    Modeller.instance().restoreCursor();
	    return;
	}
	Node eventNode = nodelist.item(index);
	if (eventNode instanceof Element) {
	    final Element eventElement = (Element) eventNode;
	    final String tag = eventElement.getNodeName();
	    if (tag != null) {
		ReconstructEventsContinutation continuation = new ReconstructEventsContinutation() {
		    public void reconstructSubsequentEvents(ModellerEvent event) {
			if (event == noEvent) {
			    // TODO: determine if the following still need special treatment now that there are continuations?
//			    // those that are handled specially are redo and undo
			    if (!tag.equals("StartEvent")) {
				Modeller.addToErrorLog("Unrecognised tag encountered while reconstructing a session : " + tag);
			    }
			}
			if (restoringHistory && event != null) {
			    String dateString = eventElement.getAttribute("date");
			    if (dateString != null) {
				try {
				    long time = Long.parseLong(dateString);
				    event.setDate(new Date(time));
				} catch (NumberFormatException e) {
				    e.printStackTrace();
				    Modeller.addToErrorLog("Error reconstructing the date of the event. " + e.toString());
				}    
			    }
			    String userGuidString = eventElement.getAttribute("userGuid");
			    if (userGuidString != null) {
				event.setUserGuid(userGuidString);
			    }
			    String userName = eventElement.getAttribute("userName");
			    if (userName != null) {
				event.setUserName(userName);
			    }
			    event.constructHistory();
			}
			reconstructEvents(index + 1, length, nodelist, restoringHistory, whatToIgnore, copyOnUpdate, nonTrivialSession, oldContinuation);
		    }
		};
		reconstructEvent(tag, eventElement, restoringHistory, whatToIgnore, copyOnUpdate, continuation);
	    } // else warn??
	} else {
	    reconstructEvents(
		    index+1, 
		    length, 
		    nodelist,
		    restoringHistory,
		    whatToIgnore,
		    copyOnUpdate,
		    nonTrivialSession,
		    oldContinuation);
	}
    }

    protected static void reconstructEvent(
	    String tag, 
	    Element eventElement, 
	    boolean restoringHistory, 
	    int whatToIgnore, 
	    boolean copyOnUpdate,
	    ReconstructEventsContinutation continuation) {
	int version = Utils.getIntAttribute(eventElement, "version", 1);
	String name = Utils.getElementString("name", eventElement);
	// otherwise should be an element that doesn't need a name attribute
	// e.g. LoadModelEvent
	if (tag.equals("CompoundEvent")) {
	    CompoundEvent.reconstruct(
		    name, eventElement, restoringHistory, whatToIgnore, copyOnUpdate, version, continuation);
	} else {
	    Modeller.instance().reconstructEvent(
		    tag, eventElement, restoringHistory, whatToIgnore, copyOnUpdate, continuation, version, name);    
	}
    }

    public static void reconstruct(
	    final String tag,
	    final String macroBehaviourName,
	    final Element eventElement, 
	    final boolean restoringHistory, 
	    final boolean copyOnUpdate,
	    final boolean justRecord,
	    final int version, 
	    final ReconstructEventsContinutation continuation) {
	final String containingUrl = Utils.getElementString("containingURL", eventElement);
	final String url = ModellerEvent.getURL(eventElement, version);
	if (containingUrl != null) {
	    BrowsePanelCommand command = new BrowsePanelCommand() {

		@Override
		public void execute(BrowsePanel panel, String answer[], boolean PanelIsNew) {
		    final MicroBehaviourView containingMicroBehaviour = panel.getMicroBehaviour();
//		    String urlNeedingFetching = containingMicroBehaviour.getUrl();
//		    if (urlNeedingFetching != null) {
//			BrowsePanelCommand newContinutation = new BrowsePanelCommand() {
//
//			    @Override
//			    public void execute(BrowsePanel panel) {
//				findMacroBehaviourAndReconstruct(tag, macroBehaviourName,
//		                                                 eventElement, restoringHistory, version,
//		                                                 continuation, url, panel.getMicroBehaviour());
//			    }
//			    
//			};
//			Modeller.executeOnMicroBehaviourPage(containingUrl, newContinutation);
//		    } else {
			findMacroBehaviourAndReconstruct(
				tag, 
				macroBehaviourName,
				eventElement,
				restoringHistory, 
				copyOnUpdate,
				version,
				continuation,
				url,
				containingMicroBehaviour);
//		    }
		}

		private void findMacroBehaviourAndReconstruct(final String tag,
			                                      final String macroBehaviourName,
			                                      final Element eventElement,
			                                      final boolean restoringHistory, 
			                                      final boolean copyOnUpdate,
			                                      final int version,
			                                      final ReconstructEventsContinutation continuation,
			                                      final String url,
			                                      MicroBehaviourView containingMicroBehaviour) {
		    AddMicroBehaviourEvent addMicroBehaviourEvent = null;
		    if (containingMicroBehaviour != null) {
			ArrayList<MacroBehaviourView> macroBehaviourViews = 
			    containingMicroBehaviour.getMacroBehaviourViews();
			for (MacroBehaviourView macroBehaviourView : macroBehaviourViews) {
			    if (macroBehaviourView.getNameHTML().equals(macroBehaviourName)) {
				reconstructInMacroBehaviour(tag,
					macroBehaviourView, 
					macroBehaviourName,
					eventElement,
					url,
					restoringHistory,
					copyOnUpdate,
					justRecord,
					version,
					continuation);
				return;
			    }
			}
		    }
		    Logger logger = Logger.getLogger(Utils.MODELLER_LOGGER);
		    logger.warning("While reconstructing an event " + tag + 
			           " could not find a list of micro-behaviours named " + macroBehaviourName);
		    if (restoringHistory) {
			continuation.reconstructSubsequentEvents(addMicroBehaviourEvent);
		    } else if (continuation != null) {
			continuation.reconstructSubsequentEvents(null);
		    }
		}

	    };
//	    Log.info("ModellerEvent.reconstruct url: " + url); // for debugging
	    Modeller.executeOnMicroBehaviourPage(containingUrl, command, true, copyOnUpdate);
	} else {
	    MacroBehaviourView macroBehaviour = Modeller.instance().getMacroBehaviourWithHTMLName(macroBehaviourName);
	    reconstructInMacroBehaviour(tag, macroBehaviour, macroBehaviourName, eventElement, url, restoringHistory, copyOnUpdate, justRecord, version, continuation);
	}
    }
    
    protected static void reconstructInMacroBehaviour(
	    String tag, 
	    MacroBehaviourView macroBehaviour,
	    final String macroBehaviourName, 
	    Element eventElement,
	    String url,
	    boolean restoringHistory, 
	    boolean copyOnUpdate,
	    boolean justRecord,
	    int version, 
	    final ReconstructEventsContinutation continuation) {
	if (macroBehaviour == null || url == null) {
	    String message = "Could not find a macro behaviour " + macroBehaviourName + 
	                     " in " + tag + " of " + url + ". " +
	                     "Known macro-behaviours are: " +
	                     Modeller.instance().getMacroBehaviourNames();
	    Modeller.addToErrorLog(message);
	    return;
	}
	if (tag.equals("AddMicroBehaviourEvent")) {
	    // I think this is the only one that cares about the value of copyOnUpdate
	    // so no need to pass it to the others
	    AddMicroBehaviourEvent.reconstructInMacroBehaviour(
		     macroBehaviour, macroBehaviourName, eventElement, url, restoringHistory, copyOnUpdate, justRecord, version, continuation);
	} else if (tag.equals("RemoveMicroBehaviourEvent")) {
	    RemoveMicroBehaviourEvent.reconstructInMacroBehaviour(
		    macroBehaviour, macroBehaviourName, eventElement, url, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("ActivateMicroBehaviourEvent")) {
	    ActivateMicroBehaviourEvent.reconstructInMacroBehaviour(
		    macroBehaviour, macroBehaviourName, eventElement, url, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("InactivateMicroBehaviourEvent")) {
	    InactivateMicroBehaviourEvent.reconstructInMacroBehaviour(
		    macroBehaviour, macroBehaviourName, eventElement, url, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("MoveMicroBehaviourEvent")) {
	    MoveMicroBehaviourEvent.reconstructInMacroBehaviour(
		    macroBehaviour, macroBehaviourName, eventElement, url, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("EditMicroBehaviourEvent")) {
	    EditMicroBehaviourEvent.reconstructInMacroBehaviour(
		    macroBehaviour, macroBehaviourName, eventElement, url, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("RenameMicroBehaviourEvent")) {
	    RenameMicroBehaviourEvent.reconstructInMacroBehaviour(
		    macroBehaviour, macroBehaviourName, eventElement, url, restoringHistory, justRecord, version, continuation);
	} else {
	    System.err.println("reconstructInMacroBehaviour can't handle tag " + tag);
	}
    }    

    public Date getDate() {
	return date;
    }

    protected void setDate(Date date) {
        this.date = date;
    }
    
    public static String getMicroBehaviourDescription(Element eventElement, int version) {
	return Utils.getElementString("description", eventElement);
    }
    
    public static String getURL(Element eventElement, int version) {
	return Utils.getElementString("url", eventElement);
    }

    public HistoryItem getHistoryItem() {
        return historyItem;
    }

    public void setHistoryItem(HistoryItem historyItem) {
        this.historyItem = historyItem;
    }
    
    public static void startRecordingCompoundEvent() {
	compoundEventSubEvents = new ArrayList<ModellerEvent>();
    }
    
    public static CompoundEvent stopRecordingCompoundEvent() {
	if (compoundEventSubEvents == null) {
	    return null;
	}
	CompoundEvent compoundEvent = new CompoundEvent(compoundEventSubEvents);
	compoundEventSubEvents = null;
	return compoundEvent;
    }

    public String getUserGuid() {
        return userGuid;
    }

    public void setUserGuid(String userGuid) {
        this.userGuid = userGuid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
