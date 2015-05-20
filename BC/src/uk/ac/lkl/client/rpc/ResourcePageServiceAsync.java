package uk.ac.lkl.client.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.shared.DeltaPageResult;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ResourcePageServiceAsync {
    public void fetchAndTransformPage(String url,
	                              String sessionGuid, 
	                              String userGuid,
	                              int idPrefix,
	                              String hostPageBaseURL,
	                              boolean cachingEnabled,
	                              boolean internetAccess, 
	                              AsyncCallback<String[]> callback);
    public void transformPage(String html, String oldURL, boolean replaceOld, String sessionGuid, String hostBaseURL, int idPrefix, boolean cachingEnabled, boolean internetAccess,
	                      AsyncCallback<String[]> callback);
    public void runModel(
	    String sessionGuid,
	    String originalSessionGuid,
	    String userGuid, 
	    String modelString, 
	    String pageTemplate,
	    String hostPageBaseURL,
	    String bc2NetLogoChannelToken,
	    String bc2NetLogoOriginalSessionGuid,
	    boolean cachingEnabled, 
	    boolean internetAccess,
	    boolean useAuxiliaryFile,
	    boolean forWebVersion,
	    AsyncCallback<String[]> callback);
    public void fetchModel(String ModelURL, String sessionGuid, String hostPageBaseURL, boolean cachingEnabled, boolean internetAccess, AsyncCallback<String> callback);
    public void deleteModel(String modelGuid, String sessionGuid, AsyncCallback<String> callback);
//    public void checkNetLogoFile(String fileName, AsyncCallback<String> callback);
    public void createDeltaPage(String nameHTML, 
	                        String url,
	                        String userGuid,
	                        String sessionGuid, 
	                        HashMap<Integer, String> textAreaValues,
	                        List<MicroBehaviourEnhancement> enhancements,
	                        ArrayList<ArrayList<String>> listsOfMicroBehaviours,
	                        boolean listsOfMicroBehavioursNeedNewURLs,
	                        String hostPageBaseURL, boolean cachingEnabled, boolean internetAccess,
                                AsyncCallback<DeltaPageResult> callback);
    public void addTextAreaValueToCopy(int index, String value, String guid, String sessionGuid, AsyncCallback<String> callback);
    public void fetchTextAreaValues(String url, String sessionGuid, String hostPageBaseURL, boolean cachingEnabled, boolean internetAccess, AsyncCallback<HashMap<Integer, String>> callback);
    public void fetchEnhancements(String url, String sessionGuid, String hostBaseURL, boolean cachingEnabled, boolean internetAccess, AsyncCallback<List<MicroBehaviourEnhancement>> callback);
    public void logMessage(String level, String message, AsyncCallback<String> callback);
    public void getAttributesOfMicroBehaviours(String[] microBehaviourURLs, String sessionGuid, boolean cachingEnabled, boolean internetAccess, AsyncCallback<String[]> callback);
    public void getNetLogoCode(String xml, String sessionGuid, String userGuid, String hostBaseURL, boolean cachingEnabled, boolean internetAccess, boolean useAuxiliaryFile, AsyncCallback<String[]> callback);
    public void cacheURLs(String urls[], String sessionGuid, String userGuid, AsyncCallback<String> callback);
    public void fetchURLContents(String url, AsyncCallback<String[]> callback);
    public void getNetLogo2BCChannelToken(String userGuid, String sessionGuid, AsyncCallback<String> callback);
    public void copyMicroBehaviourCustomisations(String oldURL, String replacementURL,
	                                         ArrayList<ArrayList<String>> listsOfMicroBehaviours,
                                                 String userGuid, String sessionGuid, String hostPageBaseURL, 
                                                 boolean cachingEnabled, boolean internetAccess, AsyncCallback<DeltaPageResult> callback);
    public void makeReadOnly(String url, String sessionGuid, String hostBaseURL, boolean cachingEnabled, boolean internetAccess, AsyncCallback<String> callback);
//    public void getModelStatistics(String modelGuid, AsyncCallback<int[]> callback);
}
