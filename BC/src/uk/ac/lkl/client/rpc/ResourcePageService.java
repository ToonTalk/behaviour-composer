package uk.ac.lkl.client.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.shared.DeltaPageResult;

import com.google.gwt.user.client.rpc.RemoteService;

public interface ResourcePageService extends RemoteService {
    public String[] fetchAndTransformPage(String url,
	                                  String sessionGuid, 
	                                  String userGuid,
	                                  int idPrefix, 
	                                  String hostPageBaseURL,
	                                  boolean cachingEnabled, 
	                                  boolean internetAccess);
    public String[] transformPage(String html, String oldURL, boolean replaceOld, String sessionGuid, String hostBaseURL, int idPrefix, boolean cachingEnabled, boolean internetAccess);
    public String[] runModel(
	    String sessionGuid,
	    String originalSessionGuid,
	    String userGuid, 
	    String modelString, 
	    String pageTemplate,
	    String moduleBaseURL,
	    String bc2NetLogoChannelToken,
	    String bc2NetLogoOriginalSessionGuid,
	    boolean cachingEnabled, 
	    boolean internetAccess,
	    boolean useAuxiliaryFile,
	    boolean forWebVersion);
    public String fetchModel(String ModelURL, String sessionGuid, String hostPageBaseURL, boolean cachingEnabled, boolean internetAccess);
    public String deleteModel(String modelGuid, String sessionGuid);
//    public String checkNetLogoFile(String fileName);
    public DeltaPageResult createDeltaPage(
	    String nameHTML, 
	    String url,
	    String userGuid,
	    String sessionGuid, 
	    HashMap<Integer, String> textAreaValues, 
            List<MicroBehaviourEnhancement> enhancements,
	    ArrayList<ArrayList<String>> listsOfMicroBehaviours,
	    boolean listsOfMicroBehavioursNeedNewURLs,
	    String hostPageBaseURL, boolean cachingEnabled, boolean internetAccess);
    public String addTextAreaValueToCopy(int index, String value, String guid, String sessionGuid);
    public HashMap<Integer, String> fetchTextAreaValues(String url, String sessionGuid, String hostPageBaseURL, boolean cachingEnabled, boolean internetAccess);
    public List<MicroBehaviourEnhancement> fetchEnhancements(String url, String sessionGuid, String hostBaseURL, boolean cachingEnabled, boolean internetAccess);
    public String logMessage(String level, String message);
    public String[] getAttributesOfMicroBehaviours(String[] microBehaviourURLs, String sessionGuid, boolean cachingEnabled, boolean internetAccess);
    public String[] getNetLogoCode(String xml, String sessionGuid, String userGuid, String hostBaseURL, boolean cachingEnabled, boolean internetAccess, boolean useAuxiliaryFile);
    public String cacheURLs(String urls[], String sessionGuid, String userGuid);
    public String[] fetchURLContents(String url);
    public String getNetLogo2BCChannelToken(String userGuid, String sessionGuid);
    public DeltaPageResult copyMicroBehaviourCustomisations(String oldURL, String replacementURL, 
                                                            ArrayList<ArrayList<String>> listsOfMicroBehaviours,
                                                            String userGuid, String sessionGuid, 
                                                            String hostPageBaseURL, boolean cachingEnabled, boolean internetAccess);
    public String makeReadOnly(String url, String sessionGuid, String hostBaseURL, boolean cachingEnabled, boolean internetAccess);
//    public int[] getModelStatistics(String modelGuid);
}
