package uk.ac.lkl.client.rpc;

import java.util.ArrayList;

import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;

import com.google.gwt.user.client.rpc.RemoteService;

public interface HistoryService extends RemoteService {
    public String fetchHistory(String sessionGuid, String userGuid, boolean readOnly);
    public String fetchHistory(String sessionGuid, long excludeEventsBeforeTime, String excludeThoseByUserWithThisGuid);
    
    public String[] startEvent(String userGuid, String sessionGuid, String initialReadOnlySessionGuid, String readOnlySessionGuid, String initialModelGuid, String sessionGuidToBeReplaced, boolean notifyOthers, String moduleBaseURL, boolean cachingEnabled, boolean internetAccess);
    public String[] activateMacroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, boolean notifyOthers);
    public String[] activateMicroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, String containingMicroBehaviourUrl, boolean macroBehaviourAsMicroBehaviour, String url, boolean notifyOthers);
    public String[] inactivateMacroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, boolean notifyOthers);
    public String[] inactivateMicroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, String containingMicroBehaviourUrl, boolean macroBehaviourAsMicroBehaviour, String url, boolean notifyOthers);
    public String[] addMacroBehaviourEvent(String userGuid, String sessionGuid, String nameEncoding, boolean notifyOthers);
    public String[] addMicroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, String containingMicroBehaviourUrl, String nameHTML, String url, boolean macroBehaviourAsMicroBehaviour, int insertionIndex, boolean notifyOthers);
    public String[] loadModelEvent(String userGuid, String sessionGuid, String modelID, boolean replaceOldModel, ArrayList<Boolean> macroBehavioursSelected, boolean notifyOthers);
    public String[] removeMacroBehaviourEvent(String userGuid, String sessionGuid, String nameEncoding, boolean notifyOthers);
    public String[] removeMicroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, String containingMicroBehaviourUrl, boolean macroBehaviourAsMicroBehaviour, String microBehaviourName, String url, boolean notifyOthers);
    public String[] renameMacroBehaviourEvent(String userGuid, String sessionGuid, String oldName, String newName, boolean notifyOthers);
    public String[] renameMicroBehaviourEvent(String userGuid, String sessionGuid, String url, String oldName, String newName, boolean notifyOthers);
    public String[] updateTextAreaEvent(String userGuid, String sessionGuid, String oldContents, String newContents, int indexInCode, String microBehaviourURL, String name, String tabTitle, boolean notifyOthers);
    public String[] enhanceMicroBehaviour(String userGuid, String sessionGuid, MicroBehaviourEnhancement enhancement, String microBehaviourURL, String tabTitle, boolean notifyOthers);
    public String[] moveMicroBehaviourEvent(boolean up, String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, String containingMicroBehaviourUrl, String microBehaviourName, String url, boolean notifyOthers);
    public String[] undoOrRedoEvent(String userGuid, String sessionGuid, boolean undo, boolean notifyOthers);
    public String[] compoundEvent(String userGuid, String sessionGuid, boolean start, boolean notifyOthers);
    public String[] addToModelListBoxMacroBehaviourEvent(
	    boolean addToModelAtEventTime,
	    boolean addToModelBeforeEvent,
	    boolean visibleInModelAtEventTime,
	    boolean visibleInModelBeforeEvent,
	    String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, boolean notifyOthers);
    public String[] addSessionEventsCheckBoxToggledEvent(
	    boolean valueAtEventTime,
	    String checkBoxSessionGuid,
	    String name,
	    String userGuid, 
	    String sessionGuid, 
	    boolean notifyOthers);
    public String[] visibleInModelEvent(
	    boolean visible,
	    String macroBehaviourNameAtEventTime, 
	    String userGuid,
	    String sessionGuid, 
	    boolean notifyOthers);
    public ArrayList<String> fetchAllSessionsOfUser(String userGuid, boolean warnIfUnknownSessionId, boolean internetAccess);
    public String updateSessionInformation(String sessionGuid, String userGuid, String newDescription, String newInfoTab, Boolean visible);
    public String[] updateNumberOfInstancesTextAreaEvent(
	    String userGuid,
	    String sessionGuid, 
	    String oldValue, 
	    String newValue,
	    String macroBehaviourNameAtEventTime, 
	    boolean notifyOthers);
    public String[] removeLastEnhancementMicroBehaviour(
	    String userGuid,
	    String sessionGuid, 
	    MicroBehaviourEnhancement enhancement,
	    String textAreaContents, 
	    int textAreaIndex, 
	    String url,
	    String tabTitle, boolean notifyOthers);
    public String[] swapPrototypesEvent(
	    String userGuid, 
	    String sessionGuid,
	    int index1,
	    int index2, 
	    boolean notifyOthers);
    String listenForSharedEvents(String sessionGuid, String userGuid);
    public String[] replaceURLEvent(String userGuid, String sessionGuid,
                                    String oldURL, String newURL, boolean notifyOthers);
}
