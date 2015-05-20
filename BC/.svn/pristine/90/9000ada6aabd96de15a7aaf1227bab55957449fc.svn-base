package uk.ac.lkl.client.rpc;

import java.util.ArrayList;

import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface HistoryServiceAsync {
    
    public void fetchHistory(String sessionGuid, String userGuid, boolean readOnly, AsyncCallback<String> callback);
    public void fetchHistory(String sessionGuid, long excludeEventsBeforeTime, String excludeThoseByUserWithThisGuid, AsyncCallback<String> callback);
    
    public void startEvent(String userGuid, String sessionGuid, String initialReadOnlySessionGuid, String readOnlySessionGuid, String initialModelGuid, String sessionGuidToBeReplaced,
	                   boolean notifyOthers, String moduleBaseURL, boolean cachingEnabled, boolean internetAccess, AsyncCallback<String[]> callback);
    public void activateMacroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime,
	                                    boolean notifyOthers, AsyncCallback<String[]> recordEventCallback);
    public void activateMicroBehaviourEvent(String userGuid, String sessionGuid, 
	                                    String macroBehaviourNameAtEventTime, String containingMicroBehaviourUrl,
	                                    boolean macroBehaviourAsMicroBehaviour, String url, 
	                                    boolean notifyOthers, AsyncCallback<String[]> recordEventCallback);
    public void inactivateMacroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime,
	                                      boolean notifyOthers, AsyncCallback<String[]> recordEventCallback);
    public void inactivateMicroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, String containingMicroBehaviourUrl,
	                                      boolean macroBehaviourAsMicroBehaviour, String url,
                                              boolean notifyOthers, AsyncCallback<String[]> recordEventCallback);
    public void addMacroBehaviourEvent(String userGuid, String sessionGuid, String nameEncoding, 
	                               boolean notifyOthers, AsyncCallback<String[]> recordSubsequentEventCallback);
    public void addMicroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, String containingMicroBehaviourUrl, 
	                               String nameHTML, String url, boolean macroBehaviourAsMicroBehaviour, int insertionIndex,
	                               boolean notifyOthers, AsyncCallback<String[]> recordSubsequentEventCallback);
    public void loadModelEvent(String userGuid, String sessionGuid, String modelID, boolean replaceOldModel,
	                       ArrayList<Boolean> macroBehavioursSelected, boolean notifyOthers, AsyncCallback<String[]> recordSubsequentEventCallback);
    public void removeMacroBehaviourEvent(String userGuid, String sessionGuid, String nameEncoding,
	                                  boolean notifyOthers, AsyncCallback<String[]> recordSubsequentEventCallback);
    public void removeMicroBehaviourEvent(String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, String containingMicroBehaviourUrl,
	                                  boolean macroBehaviourAsMicroBehaviour, String microBehaviourName, String urlAtEventTime,
	                                  boolean notifyOthers, AsyncCallback<String[]> recordSubsequentEventCallback);
    public void renameMacroBehaviourEvent(String userGuid, String sessionGuid, String oldName, String newName,
	                                  boolean notifyOthers, AsyncCallback<String[]> recordSubsequentEventCallback);
    public void renameMicroBehaviourEvent(String userGuid, String sessionGuid, String url, String oldName, String newName,
                                          boolean notifyOthers, AsyncCallback<String[]> recordSubsequentEventCallback);
    public void updateTextAreaEvent(String userGuid, String sessionGuid, String oldContents, String newContents, int indexInCode, String microBehaviourURL, String name, String tabTitle, 
	                            boolean notifyOthers, AsyncCallback<String[]> recordSubsequentEventCallback);
    public void enhanceMicroBehaviour(String userGuid, String sessionGuid, MicroBehaviourEnhancement enhancement, String microBehaviourURL, String tabTitle, boolean notifyOthers, 
	                              AsyncCallback<String[]> recordSubsequentEventCallback);
    public void moveMicroBehaviourEvent(boolean up, 
	    String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime, String containingMicroBehaviourUrl,
	    String microBehaviourName, String url,
            boolean notifyOthers, AsyncCallback<String[]> recordEventCallback);
    public void undoOrRedoEvent(String userGuid, String sessionGuid, boolean undo, 
	                        boolean notifyOthers, AsyncCallback<String[]> recordSubsequentEventCallback);
    public void compoundEvent(String userGuid, String sessionGuid, boolean start,
	                      boolean notifyOthers, AsyncCallback<String[]> recordSubsequentEventCallback);
    
    public void fetchAllSessionsOfUser(String userGuid, boolean warnIfUnknownSessionId, boolean internetAccess, AsyncCallback<ArrayList<String>> recordSubsequentEventCallback);
    public void updateSessionInformation(String sessionGuid, String userGuid, String newDescription, String newInfoTab, Boolean visible, AsyncCallback<String> recordSubsequentEventCallback);
    public void addToModelListBoxMacroBehaviourEvent(	    
	    boolean addToModelAtEventTime,
	    boolean addToModelBeforeEvent,
	    boolean visibleInModelAtEventTime,
	    boolean visibleInModelBeforeEvent,
	    String userGuid, String sessionGuid, String macroBehaviourNameAtEventTime,
            boolean notifyOthers, 
            AsyncCallback<String[]> recordEventCallback);
    public void addSessionEventsCheckBoxToggledEvent(
	    boolean valueAtEventTime,
	    String checkBoxSessionGuid,
	    String name,
	    String userGuid, 
	    String sessionGuid,
	    boolean notifyOthers, 
            AsyncCallback<String[]> recordEventCallback);
    public void visibleInModelEvent(
	    boolean visible,
	    String macroBehaviourNameAtEventTime, 
	    String userGuid,
	    String sessionGuid, 
	    boolean notifyOthers,
	    AsyncCallback<String[]> recordSubsequentEventCallback);
    public void updateNumberOfInstancesTextAreaEvent(
	    String userGuid,
	    String sessionGuid, 
	    String oldValue, 
	    String newValue,
	    String macroBehaviourNameAtEventTime, 
	    boolean notifyOthers,
	    AsyncCallback<String[]> recordSubsequentEventCallback);
    public void removeLastEnhancementMicroBehaviour(
	    String userGuid,
	    String sessionGuid, 
	    MicroBehaviourEnhancement enhancement,
	    String textAreaContents,
	    int textAreaIndex, 
	    String url,
	    String tabTitle,
	    boolean notifyOthers,
	    AsyncCallback<String[]> recordSubsequentEventCallback);
    public void swapPrototypesEvent(
	    String userGuid, 
	    String sessionGuid,
	    int index1, 
	    int index2, 
	    boolean notifyOthers,
	    AsyncCallback<String[]> recordSubsequentEventCallback);
    public void listenForSharedEvents(
	    String sessionGuid, 
	    String userGuid,
	    AsyncCallback<String> asyncCallback);
    public void replaceURLEvent(String userGuid, String sessionGuid,
	                        String oldURL, String newURL, boolean notifyOthers,
	                        AsyncCallback<String[]> recordSubsequentEventCallback);
}
