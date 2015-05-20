package uk.ac.lkl.server;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Text;
import com.google.apphosting.api.DeadlineExceededException;
import com.google.apphosting.api.ApiProxy.ApiProxyException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.client.rpc.HistoryService;
import uk.ac.lkl.server.persistent.DataStore;
import uk.ac.lkl.server.persistent.ModelXML;
import uk.ac.lkl.server.persistent.ReplacedSession;
import uk.ac.lkl.server.persistent.Session;
import uk.ac.lkl.server.persistent.SessionsOfUser;
import uk.ac.lkl.server.persistent.UserSession;
import uk.ac.lkl.server.persistent.eventdata.ServerActivateMacroBehaviourEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerActivateMicroBehaviourEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerAddMacroBehaviourEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerAddMicroBehaviourEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerAddToModelListBoxMacroBehaviourEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerCompoundEventStart;
import uk.ac.lkl.server.persistent.eventdata.ServerCompoundEventStop;
import uk.ac.lkl.server.persistent.eventdata.ServerEnhanceMicroBehaviourEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerLoadModelEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerMoveMicroBehaviourEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerRemoveLastEnhancementMicroBehaviourEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerRenameMacroBehaviourEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerRenameMicroBehaviourEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerReplaceURLEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerSessionEventsCheckBoxToggledEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerStartEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerUndoRedoEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerUpdateNumberOfInstancesTextAreaEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerUpdateTextAreaEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerVisibleInModelEvent;
import uk.ac.lkl.server.persistent.eventdata.ServerSwapPrototypesEvent;
import uk.ac.lkl.shared.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletRequest;

@SuppressWarnings("serial")
public class HistoryServiceImpl extends RemoteServiceServlet implements HistoryService {

    @SuppressWarnings("unused")
    private static final String HISTORY_SERVICE_NAME = "History Service";
    private boolean sentQuotaExceededMessage;

//    private class Updated {
//	// used together with outstandingChecksForUpdates
//	// to respond to update checks when updates are reported
//	private Updated() {    
//	}
//    }
    
    // mapping of sessionGuids so that when a session is updated outstanding requests for updates are serviced.
//    private static ConcurrentHashMap<String, Updated> outstandingChecksForUpdates =
//	new ConcurrentHashMap<String, Updated>();    
    
    /* 
     * All inputs can be null
     * Returns
     *    readWrite session guid
     *    readOnly  session guid
     *    user guid
     *    user id
     *    warnings
     */
    public String[] startEvent(
	    String userGuid,
	    String sessionGuid, 
	    String initialReadOnlySessionGuid, 
	    String readOnlySessionGuid, 
	    String initialModelGuid,
	    String sessionGuidToBeReplaced,
	    boolean notifyOthers,
	    String hostBaseURL,
	    boolean cachingEnabled,
	    boolean internetAccess) {
	DataStore.ensureClassesRegisteredWithObjectify();
	CommonUtils.setHostBaseURL(hostBaseURL);
	String result[] = new String[5];
	final int WARNINGS = 4;
	result[WARNINGS] = ""; 
	boolean newSession = sessionGuid == null;
	boolean invalidGuid = false;
	if (newSession) {
	    sessionGuid = ServerUtils.generateGUIDString();
	    while (getSession(sessionGuid, null)  != null) {
		// somehow this guid is already in use so generate a new one
		// seen rare errors in the system log about this so best to be safe
		sessionGuid = ServerUtils.generateGUIDString();
	    }
	} else {
	    // this should only be called now with sessionGuid == null
	    String validationError = CommonUtils.validateGuid(sessionGuid, "Session key");
	    if (validationError != null) {
		result[WARNINGS] += validationError;
		invalidGuid = true;
	    }
	}
	if (readOnlySessionGuid == null) {
	    readOnlySessionGuid = ServerUtils.generateGUIDString();
	} else {
	    String validationError = CommonUtils.validateGuid(readOnlySessionGuid, "Read only session key");
	    if (validationError != null) {
		result[WARNINGS] += validationError;
		invalidGuid = true;
	    }
	}
	// test whether better to return if invalidGuid 
	boolean newUserGuid = (userGuid == null || userGuid.equals("undefined"));
	if (newUserGuid) {
	    userGuid = ServerUtils.generateGUIDString();
	} else {
	    String validationError = CommonUtils.validateGuid(userGuid, "User key");
	    if (validationError != null) {
		result[WARNINGS] += validationError;
		invalidGuid = true;
	    }
	}
	if (newSession && !invalidGuid) {
	    UserSession userSession = new UserSession(userGuid, sessionGuid);
	    ServerUtils.persist(userSession);
	}
	if (sessionGuidToBeReplaced == null && !invalidGuid) {
	    updateSessionsOfUser(userGuid, sessionGuid);
	}
	if (initialModelGuid != null) {
	    if (CommonUtils.probablyAURL(initialModelGuid)) {
		ClientState clientState = new ClientState(sessionGuid, getThreadLocalRequest(), userGuid, cachingEnabled, internetAccess);
		String modelXML = ServerUtils.fetchPageFromEncodedOrRelativeURL(initialModelGuid, clientState, true);
		if (modelXML != null) {
		    if (CommonUtils.isErrorResponse(modelXML)) {
			result[WARNINGS] = modelXML;
			return result;
		    } else {
			// give the model a guid
			initialModelGuid = ServerUtils.generateGUIDString();
			ModelXML.persistModelXML(modelXML, sessionGuid, initialModelGuid, userGuid, null);
		    }
		}
	    } else {
		try {
		    // check first if it is a serial number
		    int serialNumber = Integer.parseInt(initialModelGuid);
		    String guid = ResourcePageServiceImpl.fetchGuidFromSerialNumber(serialNumber);
		    if (guid != null) {
			initialModelGuid = guid;
		    } else {
			result[WARNINGS] += "Could not find a model with the serial number " + initialModelGuid;
		    }
		} catch (NumberFormatException e) {
		    String validationError = CommonUtils.validateGuid(initialModelGuid, "Frozen model key");
		    if (validationError != null) {
			result[WARNINGS] += validationError;
		    }
		}
	    }
	}
	result[0] = sessionGuid;
	result[1] = readOnlySessionGuid;
	result[2] = userGuid;
	recordNewSession(sessionGuid, readOnlySessionGuid);
	persist(sessionGuid,
		userGuid,
		false, // notifyOthers, doesn't make sense to notify the others of a start event, right?
		result,
		WARNINGS,
		-1,
		new ServerStartEvent(
			readOnlySessionGuid, 
			initialReadOnlySessionGuid, 
			initialModelGuid, 
			sessionGuid, 
			userGuid));
	result[3] = "0"; // ignored now -- TODO: remove it
	if (sessionGuidToBeReplaced != null) {
	    replaceSessionGuid(sessionGuidToBeReplaced, sessionGuid);
	}
	return result;
    }

    private void replaceSessionGuid(String sessionGuidToBeReplaced, String sessionGuid) {
	ServerUtils.persist(new ReplacedSession(sessionGuidToBeReplaced, sessionGuid));
    }

    /**
     * @param userGuid
     * @param sessionGuid
     */
    public void updateSessionsOfUser(String userGuid, String sessionGuid) {
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	try {
	    SessionsOfUser sessionsOfUser = persistenceManager.getObjectById(SessionsOfUser.class, userGuid);
	    // add this session if not already there
	    sessionsOfUser.addSession(sessionGuid);
	    persistenceManager.makePersistent(sessionsOfUser);
	} catch (JDOObjectNotFoundException e) {
	    // first session of this user
	    SessionsOfUser newSessionOwners = new SessionsOfUser(userGuid, sessionGuid);
	    ServerUtils.persist(newSessionOwners);
	} finally {
	    persistenceManager.close();
	}
    }
    
    public String[] activateMacroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerActivateMacroBehaviourEvent(
			true,
			macroBehaviourNameAtEventTime, 
			sessionGuid, 
			userGuid));
	return result;
    }

    
    @Override
    public String[] activateMicroBehaviourEvent(
	    String userGuid,
	    String sessionGuid, 
	    String macroBehaviourNameAtEventTime, 
	    String containingMicroBehaviourUrl, 
	    boolean macroBehaviourAsMicroBehaviour,
	    String url,
	    boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid, 
		notifyOthers,
		result,
		0,
		1,
		new ServerActivateMicroBehaviourEvent(
			true,
			url,
			macroBehaviourNameAtEventTime,
			macroBehaviourAsMicroBehaviour,
			containingMicroBehaviourUrl,
			sessionGuid, 
			userGuid));
	return result;
    }
    
    protected void persist(String sessionGuid, String userGuid, boolean notifyOthers, String[] result, int errorIndex, int timeStampIndex, ServerEvent event) {
	if (notifyOthers) {
	    informOpenChannels(sessionGuid, userGuid);
	}
	persist(sessionGuid, notifyOthers, result, errorIndex, timeStampIndex, event, 0);
    }
    
    protected void persist(String sessionGuid, boolean notifyOthers, String[] result, int errorIndex, int timeStampIndex, ServerEvent event, int attempCount) {
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	try {
	    persistenceManager.makePersistent(event);
	    String timeStamp = Long.toString(updateSession(sessionGuid, notifyOthers, persistenceManager));
	    if (timeStampIndex >= 0) {
		result[timeStampIndex] = timeStamp;
	    }
	} catch (DatastoreTimeoutException e) {
	    if (attempCount < 5) {
		ServerUtils.logException(e, "Timed out -- trying again");
		persist(sessionGuid, notifyOthers, result, errorIndex, timeStampIndex, event, attempCount+1);
	    } else {
		ServerUtils.logException(e, "Timed out 5 times. Giving up.");
		if (timeStampIndex >= 0) {
		    result[timeStampIndex] = Long.toString(new Date().getTime());
		}
	    }
	} catch (ApiProxyException e) { 
	    String errorMessage = ServerUtils.handleApiProxyException(e, sentQuotaExceededMessage);
	    result[errorIndex] = ServerUtils.logError(errorMessage);
	    sentQuotaExceededMessage = true;
	    // following produces less information in the logs since callers are lost
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    // TODO: implement custom toString methods for events
//	    System.err.println("Exception persisting event. " + event.toString());
	} finally {
	    if (!persistenceManager.isClosed()) {
		persistenceManager.close();
	    }
	}
    }
    
    public String[] addMacroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid, 
		notifyOthers,
		result,
		0,
		1,
		new ServerAddMacroBehaviourEvent(
			true,
			macroBehaviourNameAtEventTime,
			sessionGuid, 
			userGuid));
	return result;
    }

    public String[] addMicroBehaviourEvent(
	    String userGuid, 
	    String sessionGuid, 
	    String macroBehaviourName,                                    
	    String containingMicroBehaviourUrl, 
	    String microBehaviourName, 
	    String url,
	    boolean macroBehaviourAsMicroBehaviour,
	    int insertionIndex, 
	    boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid, 
		notifyOthers,
		result,
		0,
		1,
		new ServerAddMicroBehaviourEvent(
			true,
			macroBehaviourName,
			url,
			microBehaviourName,
			macroBehaviourAsMicroBehaviour,
			containingMicroBehaviourUrl,
			insertionIndex,
			sessionGuid, 
			userGuid));
	return result;
    }
    
    public String[] removeMacroBehaviourEvent(String userGuid, String sessionGuid, 
	                                      String macroBehaviourNameAtEventTime, 
	                                      boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerAddMacroBehaviourEvent(
			false,
			macroBehaviourNameAtEventTime,
			sessionGuid, 
			userGuid));
	return result;
    }

    public String[] removeMicroBehaviourEvent(String userGuid, String sessionGuid, 
	                                      String macroBehaviourName, String containingMicroBehaviourUrl, 
	                                      boolean macroBehaviourAsMicroBehaviour,  
	                                      String microBehaviourName, String url, boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid, 
		notifyOthers,
		result,
		0,
		1,
		new ServerAddMicroBehaviourEvent(
			false,
			macroBehaviourName,
			url,
			microBehaviourName,
			macroBehaviourAsMicroBehaviour,
			containingMicroBehaviourUrl,
			0,
			sessionGuid, 
			userGuid));
	return result;
    }

    public String[] inactivateMacroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerActivateMacroBehaviourEvent(
			false, // inactivate
			macroBehaviourNameAtEventTime, 
			sessionGuid, 
			userGuid));
	return result;
    }

    public String[] inactivateMicroBehaviourEvent(
	    String userGuid, 
	    String sessionGuid, 
	    String macroBehaviourNameAtEventTime, 
	    String containingMicroBehaviourUrl, 
	    boolean macroBehaviourAsMicroBehaviour,
	    String url, 
	    boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerActivateMicroBehaviourEvent(
			false, // inactivate
			url,
			macroBehaviourNameAtEventTime,
			macroBehaviourAsMicroBehaviour,
			containingMicroBehaviourUrl,
			sessionGuid, 
			userGuid));
	return result;
    }
    
    @Override
    public String[] swapPrototypesEvent(String userGuid, 
	                                String sessionGuid,
	                                int index1, 
	                                int index2, 
	                                boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid); 
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1, 
		new ServerSwapPrototypesEvent(index1,
			index2,
			sessionGuid, 
			userGuid));
	return result;
    }
    
    public String[] moveMicroBehaviourEvent(
	    boolean up,
	    String userGuid, String sessionGuid, 
	    String macroBehaviourNameAtEventTime,
	    String containingMicroBehaviourUrl, 
	    String microBehaviourName, 
	    String url,
	    boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerMoveMicroBehaviourEvent(
			up, 
			url,
			microBehaviourName,
			containingMicroBehaviourUrl,  
			macroBehaviourNameAtEventTime, 
			sessionGuid, 
			userGuid));
	return result;
    }

    public String[] loadModelEvent(String userGuid, String sessionGuid, String modelGuid, boolean replaceOldModel, ArrayList<Boolean> macroBehavioursSelected, boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerLoadModelEvent(
			modelGuid, 
			replaceOldModel,
			macroBehavioursSelected,
			sessionGuid, 
			userGuid));
	return result;
    }

   public String[] renameMacroBehaviourEvent(String userGuid, String sessionGuid, String oldName, String newName, boolean notifyOthers) {
       sessionGuid = getCurrentSessionGuid(sessionGuid);
       String[] result = createResponse(userGuid, sessionGuid);
       if (result[0] != null) { // error message
	    return result;
	}
       persist(sessionGuid,
	       userGuid,
	       notifyOthers,
	       result,
	       0,
	       1,
	       new ServerRenameMacroBehaviourEvent(
		       oldName, 
		       newName, 
		       sessionGuid, 
		       userGuid));
	return result;
    }

    public String[] renameMicroBehaviourEvent(String userGuid, 
	                                      String sessionGuid, 
	                                      String url,
	                                      String oldName, 
	                                      String newName, 
	                                      boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerRenameMicroBehaviourEvent(
			url,
			oldName, 
			newName, 
			sessionGuid, 
			userGuid));
	return result;
    }
    
    @Override
    public String[] updateTextAreaEvent(String userGuid, String sessionGuid, String oldContents, String newContents, int indexInCode, String microBehaviourURL, String name, String tabTitle, boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerUpdateTextAreaEvent(
			oldContents,
			newContents, 
			indexInCode, 
			microBehaviourURL,
			name,
			tabTitle,
			sessionGuid, 
			userGuid));
	return result;
    }

    @Override
    public String[] enhanceMicroBehaviour(
	    String userGuid,
	    String sessionGuid, 
	    MicroBehaviourEnhancement enhancement, 
	    String url, 
	    String tabTitle,
	    boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerEnhanceMicroBehaviourEvent(
			enhancement,
			url,
			tabTitle,
			sessionGuid, 
			userGuid));
	return result;
    }
    

    @Override
    public String[] removeLastEnhancementMicroBehaviour(
	    String userGuid,
	    String sessionGuid,
	    MicroBehaviourEnhancement enhancement,
	    String textAreaContents, 
	    int textAreaIndex, 
	    String url,
	    String tabTitle, 
	    boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerRemoveLastEnhancementMicroBehaviourEvent(
			enhancement,
			textAreaContents, 
			textAreaIndex,
			url,
			tabTitle,
			sessionGuid, 
			userGuid));
	return result;
    }

    public String[] undoOrRedoEvent(String userGuid, String sessionGuid, boolean undo, boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerUndoRedoEvent(
			undo,
			sessionGuid, 
			userGuid));
	return result;
    }
    
    public String[] compoundEvent(String userGuid, String sessionGuid, boolean start, boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	if (start) {
	    persist(sessionGuid,
		    userGuid,
		    notifyOthers,
		    result,
		    0,
		    1,
		    new ServerCompoundEventStart(sessionGuid, userGuid));
	} else {
	    persist(sessionGuid,
		    userGuid, 
		    notifyOthers,
		    result,
		    0,
		    1,
		    new ServerCompoundEventStop(sessionGuid, userGuid));
	}
	return result;
    }
    
    public String[] addToModelListBoxMacroBehaviourEvent(
	    boolean addToModelAtEventTime,
	    boolean addToModelBeforeEvent,
	    boolean visibleInModelAtEventTime,
	    boolean visibleInModelBeforeEvent,
	    String userGuid, 
	    String sessionGuid, 
	    String macroBehaviourNameAtEventTime, 
	    boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerAddToModelListBoxMacroBehaviourEvent(
			addToModelAtEventTime,
			addToModelBeforeEvent,
			visibleInModelAtEventTime,
			visibleInModelBeforeEvent,
			macroBehaviourNameAtEventTime,
			sessionGuid, 
			userGuid));
	return result;
    }
    
    @Override
    public String[] visibleInModelEvent(
	    boolean visible,
	    String macroBehaviourNameAtEventTime, 
	    String userGuid,
	    String sessionGuid, 
	    boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerVisibleInModelEvent(
			visible,
			macroBehaviourNameAtEventTime,
			sessionGuid, 
			userGuid));
	return result;
    }
    
    @Override
    public String[] updateNumberOfInstancesTextAreaEvent(
	    String userGuid,
	    String sessionGuid,
	    String oldValue,
	    String newValue,
	    String macroBehaviourNameAtEventTime,
	    boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerUpdateNumberOfInstancesTextAreaEvent(
			oldValue,
			newValue,
			macroBehaviourNameAtEventTime,
			sessionGuid, 
			userGuid));
	return result;
    }
    
    public String[] addSessionEventsCheckBoxToggledEvent(
	    boolean valueAtEventTime,
	    String checkBoxSessionGuid,
	    String nameId,
	    String userGuid, 
	    String sessionGuid, 
	    boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		new ServerSessionEventsCheckBoxToggledEvent(
			valueAtEventTime,
			checkBoxSessionGuid,
			nameId,
			sessionGuid, 
			userGuid));
	return result;
    }
    
    private String getReadWriteSessionGuid(String readOnlyGuid) {
	// returns sessionGuid if it exists -- else null
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	try {
	    Session session = persistenceManager.getObjectById(Session.class, readOnlyGuid);
	    return session.getSessionGuid();
	} catch (JDOObjectNotFoundException e) {
		return null;
	} finally {
	    persistenceManager.close();
	}
    }
    
    private String getCurrentSessionGuid(String sessionGuid) {
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	ReplacedSession replacedSession = null;
	try {
	    replacedSession = persistenceManager.getObjectById(ReplacedSession.class, sessionGuid);
	    while (true) {
		replacedSession = persistenceManager.getObjectById(ReplacedSession.class, replacedSession.getNewSessionGuid());
	    }
	} catch (JDOObjectNotFoundException e) {
		// nothing special to do
	} finally {
	    persistenceManager.close();
	}
	if (replacedSession == null) {
	    return sessionGuid;
	} else {
	    return replacedSession.getNewSessionGuid();
	}
    }
 
    public void recordNewSession(String sessionGuid, String readOnlyGuid) {
	Session session = getSession(sessionGuid, null);
	if (session != null) {
	    ServerUtils.logError("Warning. Recording a new session for " + sessionGuid + " but one already exists in database. Using the old one. (This has been observed only when a second or so away from 'Could not find a previous session with an ID of ...'");
	    return;
	}
	if (readOnlyGuid == null) {
	    readOnlyGuid = ServerUtils.generateGUIDString();
	}
	ServerUtils.persist(new Session(sessionGuid, readOnlyGuid));
    }
    
    private long updateSession(String sessionGuid, boolean notifyOthers, PersistenceManager persistenceManager) {
	Session session = getSession(sessionGuid, persistenceManager);
	Date date;
	if (session != null) {
	    date = session.updateTimeStamp();
//	    if (notifyOthers) {
//		Updated updateChecksOutstanding = outstandingChecksForUpdates.get(sessionGuid);
//		if (updateChecksOutstanding != null) {
//		    synchronized (updateChecksOutstanding) {
//			updateChecksOutstanding.notifyAll();
//		    }
//		}
//	    }
	} else {
	    date = new Date();
	}
	return date.getTime();
    }

    /**
     * @param sessionGuid
     * @param persistenceManager -- if null fetch it and close it
     * @return
     */
    protected Session getSession(String sessionGuid, PersistenceManager persistenceManager) {
	boolean persistenceManagerProvided = (persistenceManager != null);
	if (!persistenceManagerProvided) {
	    persistenceManager = JDO.getPersistenceManager();
	}
	Query query = persistenceManager.newQuery(Session.class);
	try {
	    query.setFilter("sessionGuid == sessionGuidParam");
	    query.declareParameters("String sessionGuidParam");
	    @SuppressWarnings("unchecked")
	    List<Session> sessions = (List<Session>) query.execute(sessionGuid);
	    int sessionsCount = sessions.size();
	    if (sessionsCount == 0) {
		return null;
	    } else if (sessionsCount > 1) {
		ServerUtils.logError("Found more than one session in the data store with id " + sessionGuid + ". Using the most recent one.");
		Session mostRecentSession = sessions.get(0);
		Date mostRecentTimeStamp = mostRecentSession.getTimeStamp();
		for (int i = 1; i < sessionsCount; i++) {
		    Date timeStamp = sessions.get(i).getTimeStamp();
		    if (timeStamp.after(mostRecentTimeStamp)) {
			mostRecentSession = sessions.get(i);
			mostRecentTimeStamp = timeStamp;
		    }
		}
		return mostRecentSession;
	    } else {
		return sessions.get(0);
	    }
	    // following caused errors -- not clear how
//	    query.setUnique(true);
//	    Session session = (Session) query.execute(sessionGuid);
//	    return session;
	} finally {
	    if (!persistenceManagerProvided) {
		persistenceManager.close();
	    }
	}
    }
    
    private long getSessionLastUpdateTime(String sessionGuid, PersistenceManager persistenceManager) {
	Session session = getSession(sessionGuid, persistenceManager);
	if (session == null) {
	    return -1;
	} else {
	    return session.getTimeStamp().getTime();
	}
    }
    
    /**
     * @param guid
     *     either the read-write session guid or the read-only version
     * @param userGuid
     *     
     * @param readOnly
     *     guid is for the read only access to this history
     * @return
     *    XML of events that occurred since time 0
     *    or Error string
     */
    
    @Override
    public String fetchHistory(String guid, final String userGuid, boolean readOnly) {
	String error = CommonUtils.validateGuid(userGuid, "User key");
	if (error != null) {
	    return error;
	}
	error = CommonUtils.validateGuid(guid, "Session key");
	if (error != null) {
	    return error;
	}
	final String sessionGuid;
	if (readOnly) {
	    sessionGuid = getReadWriteSessionGuid(guid);
	    updateSessionsOfUser(userGuid, sessionGuid);
	} else {
	    // dereference if guid has been replaced
	    sessionGuid = getCurrentSessionGuid(guid);
	    if (sessionGuid == guid) {
		updateSessionsOfUser(userGuid, sessionGuid);
	    }
	}
	try {
	    return getEvents(sessionGuid, 0, null, userGuid, getThreadLocalRequest());
	} catch (DeadlineExceededException e) {
	    return ServerUtils.reportDeadlineExceededError(e);
	}
    }
      
    /**
     * @param guid
     * @param excludeEventsBeforeTime 
     *    only consider events after this time (if 0 then all events)
     * @param excludeThoseByUserWithThisGuid
     *    exclude those events performed by this user
     * @return
     *    XML of events that occurred since excludeEventsBeforeTime
     */

    public String fetchHistory(String guid, long excludeEventsBeforeTime, String excludeThoseByUserWithThisGuid) {
	String error = CommonUtils.validateGuid(guid, "Session key");
	if (error != null) {
	    return error;
	}
	return getEvents(guid, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, null, getThreadLocalRequest());
    }
    
    public static String getEvents(String sessionGuid, ServletRequest servletRequest) {
	return new HistoryServiceImpl().getEvents(sessionGuid, 0, null, "", servletRequest);
    }

    public String getEvents(
	    final String sessionGuid, 
	    long excludeEventsBeforeTimeLong, 
	    String excludeThoseByUserWithThisGuid, 
	    final String userGuid,
	    ServletRequest servletRequest) {
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	try {
	    long updateTime = getSessionLastUpdateTime(sessionGuid, persistenceManager);
	    if (updateTime > 0 && updateTime <= excludeEventsBeforeTimeLong) {
		// nothing has happened since excludeEventsBeforeTime
		return "";
	    }
	    // the following should be capable of being much simpler by
	    // making a single query to ServerEvent.class and sort it in the query
	    // but I couldn't get around the errors that resulted
	    ArrayList<EventXML> xmlEvents = new ArrayList<EventXML>();
	    Date excludeEventsBeforeTime = new Date(excludeEventsBeforeTimeLong);
	    int startEventCount = getEventsOfClass(sessionGuid, ServerStartEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    if (startEventCount == 0) {
		// if running development server try the production server
		RemoteAPI freeRemoteAPI = ServerUtils.getFreeRemoteAPI(getThreadLocalRequest());
		if (freeRemoteAPI == null) {
		    // already remote or exception encountered
		    return null;
		}
		RunOnProductionGAECallback<String> callback = new RunOnProductionGAECallback<String>() {

		    @Override
		    public String execute() {
			return getEvents(sessionGuid, 0, null, userGuid, getThreadLocalRequest());
		    }

		};
		return freeRemoteAPI.runOnProductionGAE(callback);
	    }
	    int compoundEventStartCount = 
		getEventsOfClass(sessionGuid, ServerCompoundEventStart.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerActivateMacroBehaviourEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerActivateMicroBehaviourEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerAddMacroBehaviourEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerAddMicroBehaviourEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerMoveMicroBehaviourEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerLoadModelEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerRenameMacroBehaviourEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerRenameMicroBehaviourEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerUpdateTextAreaEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerEnhanceMicroBehaviourEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerRemoveLastEnhancementMicroBehaviourEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerUndoRedoEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerUpdateNumberOfInstancesTextAreaEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerVisibleInModelEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerAddToModelListBoxMacroBehaviourEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerSessionEventsCheckBoxToggledEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerSwapPrototypesEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    getEventsOfClass(sessionGuid, ServerReplaceURLEvent.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
	    int compoundEventStopCount = 
		getEventsOfClass(sessionGuid, ServerCompoundEventStop.class, excludeEventsBeforeTime, excludeThoseByUserWithThisGuid, persistenceManager, xmlEvents);
//	    if (xmlEvents.isEmpty()) {
//		String historyFromServer = getHistoryFromServer(sessionGuid, userGuid, servletRequest);
//		if (historyFromServer != null) {
//		    return historyFromServer;
//		}
//	    }
	    Collections.sort(xmlEvents);
	    StringBuffer result = 
		new StringBuffer("<history lastUpdated='" + updateTime + "' userGuid='" + userGuid + "'>");
	    for (EventXML event : xmlEvents) {
		result.append(event.getXML() + "\n");
	    }
	    if (compoundEventStopCount < compoundEventStartCount) {
		// attempt to recover from aborted database update (or previous bugs)
		int missing = compoundEventStartCount-compoundEventStopCount;
		for (int i = 0; i < missing; i++) {
		    result.append("</CompoundEvent>");
		}
	    }
	    result.append("</history>");
	    return result.toString();
	} finally {
	    persistenceManager.close();
	}
    }

    // Not needed not that Remote API does this better
//    /**
//     * @param sessionGuid
//     * @param userGuid
//     * @param servletRequest
//     * @return 
//     */
//    protected String getHistoryFromServer(String sessionGuid, String userGuid, ServletRequest servletRequest) {
//	// check if running local server (i.e. development) and fetch from modelling4all.org
//	String serverName = servletRequest.getServerName();
//	if (serverName.equals("127.0.0.1")) {
//	    // is there a better way to test if local?
//	    ClientState clientState = new ClientState(sessionGuid, null, userGuid);
//	    String resultFromServer = ServerUtils.urlToString("http://m.modelling4all.org/p/" + sessionGuid + ".xml", clientState, false);
//	    return resultFromServer;
//	} else {
//	    return null;
//	}
//    }

    /**
     * @param sessionGuid
     * @param c
     * @param excludeThoseByUserWithThisGuid 
     * @param excludeEventsBeforeTime 
     * @param persistenceManager
     * @param xmlEvents
     * @return the number of events found
     */
    @SuppressWarnings("unchecked")
    protected static int getEventsOfClass(
	    String sessionGuid,
	    Class<?> c,
	    Date excludeEventsBeforeTime, 
	    String excludeThoseByUserWithThisGuid,
	    PersistenceManager persistenceManager, 
	    ArrayList<EventXML> xmlEvents) {
	Query query = persistenceManager.newQuery(c);
	query.setFilter("sessionGuid == sessionGuidParam && timeStamp > timeStampParam");
	query.declareParameters("String sessionGuidParam, Long timeStampParam");
	int count = 0;
	try {
	    List<ServerEvent> events = (List<ServerEvent>) query.execute(sessionGuid, excludeEventsBeforeTime);
	    for (ServerEvent event : events) {
		long time = event.getTimeStamp().getTime();
		xmlEvents.add(new EventXML(time, event.toXML()));
		count++;
		List<String> additionalXML = event.additionalXML();
		if (additionalXML != null) {
		    for (String xml : additionalXML) {
			xmlEvents.add(new EventXML(time, xml));
			count++;
		    }
		}
	    }
	} finally {
	    query.closeAll();
	}
	return count;
    } 
    
    /* (non-Javadoc)
     * @see uk.ac.lkl.client.rpc.HistoryService#fetchAllSessionsOfUser(java.lang.String)
     * 
     * returns an ArrayList of Strings: 6 Strings for each session
     */
    public ArrayList<String> fetchAllSessionsOfUser(final String userGuid, final boolean warnIfUnknownSessionId, boolean internetAccess) {
	// TODO: extend this with a flag indicating whether the user is authorised
	// to inspect the models of other users with this context id (e.g. roles contains instructor)
	// if so then the client could produce a tab with a list of users in this context
	String error = CommonUtils.validateGuid(userGuid, "User key");
	if (error != null) {
	    return null;
	}
	ArrayList<String> result = new ArrayList<String>();
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	SessionsOfUser sessionsOfUser;
	try {
	    try {
		sessionsOfUser = persistenceManager.getObjectById(SessionsOfUser.class, userGuid);
	    } catch (JDOObjectNotFoundException e) {
		// for compatibility with older sessions (prior to 9 January 2012)
		// fetch private sessions (shared sessions are not recoverable)
		Query query = persistenceManager.newQuery(UserSession.class);
		query.setFilter("userGuid == userGuidParam");
		query.declareParameters("String userGuidParam");
		query.setOrdering("timeStamp desc");
		@SuppressWarnings("unchecked")
		List<UserSession> userSessions = (List<UserSession>) query.execute(userGuid);
		StringBuffer sessionGuidsStringBuffer = new StringBuffer();
		for (UserSession userSession : userSessions) {
		    sessionGuidsStringBuffer.append(userSession.getSessionGuid() + ";");
		}
		sessionsOfUser = new SessionsOfUser(userGuid, sessionGuidsStringBuffer.toString());
		persistenceManager.makePersistent(sessionsOfUser);		
	    }
	    String[] sessionGuids = sessionsOfUser.getSessionGuids();
	    boolean firstSession = true;
	    for (String sessionGuid : sessionGuids) {
		try {
		    if (sessionGuid.isEmpty()) {
			// seems to only happen while debugging using RemoteAPI so ignore this
			continue;
		    }
		    //		sessionGuid = getCurrentSessionGuid(sessionGuid);
		    UserSession session = persistenceManager.getObjectById(UserSession.class, sessionGuid);
		    result.add(session.getSessionGuid());
		    result.add(session.getDescription());
		    // to reduce bandwidth only send info tab of the current session
		    if (firstSession) {
			result.add(session.getInfoTab());
			firstSession = false;
		    } else {
			result.add(null);
		    }
		    result.add(Boolean.toString(session.isVisible()));
		    result.add(session.getBltiContextId());
		    result.add(Long.toString(session.getTimeStamp().getTime()));
		    // find out when it was last updated
		    Query query = persistenceManager.newQuery(Session.class);
		    query.setFilter("sessionGuid == sessionGuidParam");
		    query.declareParameters("String sessionGuidParam");
		    @SuppressWarnings("unchecked")
		    List<Session> sessionRecords = (List<Session>) query.execute(sessionGuid);
		    if (sessionRecords.isEmpty()) {
			result.add(null);
		    } else {
			result.add(Long.toString(sessionRecords.get(0).getTimeStamp().getTime()));
		    }	
		} catch (JDOObjectNotFoundException e) {
		    if (!internetAccess) {
			return null;
		    }
		    RemoteAPI freeRemoteAPI = ServerUtils.getFreeRemoteAPI(getThreadLocalRequest());
		    if (freeRemoteAPI == null) {
			// already remote or exception encountered
			if (warnIfUnknownSessionId) {
			    System.out.println("Expected to find the guid for the session in the data store: " + sessionGuid);
			}
			return null;
		    }
		    RunOnProductionGAECallback<ArrayList<String>> callback = new RunOnProductionGAECallback<ArrayList<String>>() {

			@Override
			public ArrayList<String> execute() {
			    return fetchAllSessionsOfUser(userGuid, warnIfUnknownSessionId, true);
			}

		    };
		    return freeRemoteAPI.runOnProductionGAE(callback);
		}
	    }
	} finally {
	    persistenceManager.close();
	}
	return result;
    }
    
    @Override
    public String updateSessionInformation(String sessionGuid, String userGuid, String newDescription, String newInfoTab, Boolean visible) {
//	sessionGuid = getCurrentSessionGuid(sessionGuid);
	String error = CommonUtils.validateGuid(userGuid, "User key");
	if (error != null) {
	    return error;
	}
	error = CommonUtils.validateGuid(sessionGuid, "Session key");
	if (error != null) {
	    return error;
	}
	Text description = null;
	if (newDescription != null) {
	    description = new Text(newDescription);
	}
	Text infoTab = null;
	if (newInfoTab != null) {
	    infoTab = new Text(newInfoTab);
	}
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	try {
	    UserSession userSession = persistenceManager.getObjectById(UserSession.class, sessionGuid);
	    if (newDescription != null) {
		userSession.setDescription(description);
	    }
	    if (newInfoTab != null) {
		userSession.setInfoTab(infoTab);
	    }
	    if (visible != null) {
		userSession.setVisible(visible);
	    }
	    ServerUtils.persist(userSession);
	} catch (JDOObjectNotFoundException e) {
	    // not sure how this can happen if at all
	    if (visible == null) {
		visible = true; // visible by default
	    }
	    persistenceManager.makePersistent(new UserSession(userGuid, sessionGuid, description, infoTab, visible));
	} finally {
	    persistenceManager.close();
	}
	// no errors to report
	return "";
    }

    @Override
    public String listenForSharedEvents(String sessionGuid, String userGuid) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	ChannelService channelService = ChannelServiceFactory.getChannelService();
	String clientId = createChannelId(sessionGuid, userGuid);
	String channelToken = channelService.createChannel(clientId);
	OpenSessionChannels openChannels = DataStore.begin().find(OpenSessionChannels.class, sessionGuid);
	if (openChannels == null) {
	    openChannels = new OpenSessionChannels(sessionGuid, clientId);
	} else {
	    openChannels.addChannel(clientId);
	}
	DataStore.begin().put(openChannels);
	return channelToken;
    }
    
    public void informOpenChannels(String sessionGuid, String exceptUserGuid) {
	String[] clientIds = OpenSessionChannels.getClientIds(sessionGuid);
	if (clientIds == null) {
	    return;
	}
	for (String clientId : clientIds) {
	    if (!clientId.startsWith(exceptUserGuid)) {
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		channelService.sendMessage(new ChannelMessage(clientId, "checkForNewEvents"));
	    }
	}
    }

    /**
     * @param sessionGuid
     * @param userGuid
     * @return channel id
     */
    protected String createChannelId(String sessionGuid, String userGuid) {
	return userGuid + sessionGuid;
    }
    
    private String[] createResponse(String userGuid, String sessionGuid) {
	String result[] = new String[3];
	result[0] = CommonUtils.validateGuid(userGuid, "User key");
	if (result[0] != null) { // error message
	    return result;
	}
	result[0] = CommonUtils.validateGuid(sessionGuid, "Session key");
	return result;
    }

    @Override
    public String[] replaceURLEvent(String userGuid, String sessionGuid,
	                            String oldURL, String newURL, boolean notifyOthers) {
	sessionGuid = getCurrentSessionGuid(sessionGuid);
	ServerReplaceURLEvent serverReplaceURLEvent = new ServerReplaceURLEvent(userGuid, sessionGuid, oldURL, newURL);
	String[] result = createResponse(userGuid, sessionGuid);
	if (result[0] != null) { // error message
	    return result;
	}
	persist(sessionGuid,
		userGuid,
		notifyOthers,
		result,
		0,
		1,
		serverReplaceURLEvent);
	return result;
    }
    
}