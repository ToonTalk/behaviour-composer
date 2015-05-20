/**
 * 
 */
package uk.ac.lkl.server.persistent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.servlet.ServletRequest;

import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.server.JDO;
import uk.ac.lkl.server.MacroBehaviour;
import uk.ac.lkl.server.MicroBehaviour;
import uk.ac.lkl.server.RemoteAPI;
import uk.ac.lkl.server.ResourcePageServiceImpl;
import uk.ac.lkl.server.RunOnProductionGAECallback;
import uk.ac.lkl.server.ServerUtils;
import uk.ac.lkl.shared.CommonUtils;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;


/**
 * Represents micro-behaviours in JDO database
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class MicroBehaviourData {
   
    @PrimaryKey
    private String url;
    @Persistent
    private Text behaviourDescriptionHTML;
    // instead of each microBehaviour knowing the behaviourCode
    // better to get it using the behaviourURL (without the bookmark) as the key
//    @Persistent
//    private Text behaviourCode;
//    @Persistent
//    // following used to substitute text areas for text area names
//    private List<Text> textAreaElements;
    @Persistent
    private List<Text> textAreaValues;
    @Persistent
    private Text textAreaNameValue;
    @Persistent
    private List<Integer> enhancementIndices;
    @Persistent
    private List<Long> macroBehaviourData;
    @Persistent
    private Date timeStamp;
    @NotPersistent 
    private List<MacroBehaviourData> originalMacroBehaviourData;
    // following not needed anymore and can cause loss of enhancements when loading a copied micro-behaviour URL
//    @NotPersistent 
//    private List<MicroBehaviourEnhancement> originalEnhancements;

    private MicroBehaviourData( 
	    String url,
	    String behaviourDescriptionHTML,
//            List<String> textAreaElements,
            HashMap<Integer, String> textAreaValues,
            List<MicroBehaviourEnhancement> enhancements,
            List<MacroBehaviourData> macroBehaviourData) {
	super();
	this.url = url;
	this.behaviourDescriptionHTML = new Text(behaviourDescriptionHTML);
//	if (behaviourCode == null) {
//	    System.err.println("behaviourCode is null in MicroBehaviourData");
//	}
//	this.behaviourCode = new Text(behaviourCode);
	setEnhancements(enhancements);
//	setTextAreaDefaultValues(textAreaDefaultValues);
//	setTextAreaElements(textAreaElements);
	setTextAreaValues(textAreaValues);
	setMacroBehaviours(macroBehaviourData);
	// used to make copies -- needed for local caching
//	this.originalEnhancements = enhancements;
//	this.originalMacroBehaviourData = macroBehaviourData;
	timeStamp = new Date();
    }
    
    public MicroBehaviourData copy() {
	ArrayList<MacroBehaviourData> macroBehaviourDataCopy = new ArrayList<MacroBehaviourData>();
	if (originalMacroBehaviourData != null) {
	    for (MacroBehaviourData data : originalMacroBehaviourData) {
		macroBehaviourDataCopy.add(data.copy());
	    }
	}
	return new MicroBehaviourData(url, 
		                      behaviourDescriptionHTML.getValue(),
		                      getTextAreaValues(),
		                      getEnhancements(),
		                      macroBehaviourDataCopy);
    }
    
    private MicroBehaviourData withNewURL(String newURL) {
	return new MicroBehaviourData(newURL, 
                                      behaviourDescriptionHTML.getValue(),
                                      getTextAreaValues(),
                                      getEnhancements(),
                                      originalMacroBehaviourData);
    }
    
    public static MicroBehaviourData persistMicroBehaviourData( 
	    String url,
	    String behaviourDescriptionHTML, 
            HashMap<Integer, String> textAreaValues,
            List<MicroBehaviourEnhancement> enhancements,
            List<MacroBehaviourData> macroBehaviourData) {
	MicroBehaviourData microBehaviourData = 
		new MicroBehaviourData(url,
	                               behaviourDescriptionHTML, 
	                               textAreaValues,
	                               enhancements,
	                               macroBehaviourData);
	String serialization = microBehaviourData.getSerialization();
	String hash = ServerUtils.hashSHA256(serialization);
	if (hash != null) {
	    MicroBehaviourDataHash microBehaviourDataHash = 
		    DataStore.begin().find(MicroBehaviourDataHash.class, hash);
	    if (microBehaviourDataHash == null) {
		microBehaviourDataHash = new MicroBehaviourDataHash(hash, url);
		DataStore.begin().put(microBehaviourDataHash);
		ServerUtils.persist(microBehaviourData);
	    } else if (!microBehaviourDataHash.knownURL(url)) {
		microBehaviourDataHash.addURL(url);
		String originalURL = microBehaviourDataHash.getOriginalURL();
		MicroBehaviourURLCopy microBehaviourURLCopy = 
			new MicroBehaviourURLCopy(url, originalURL);
		DataStore.begin().put(microBehaviourURLCopy);
//		microBehaviourData.setOriginalURL(originalURL);
	    }
	} else {
	    ServerUtils.persist(microBehaviourData);
	}
	return microBehaviourData;
    }
    
    public static MicroBehaviourData getMicroBehaviourData(String urlsString) {
	String[] urls = urlsString.split(";");
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	for (String url : urls) {
	    try {
		MicroBehaviourData microBehaviourData = persistenceManager.getObjectById(MicroBehaviourData.class, url);
		persistenceManager.close();
		return microBehaviourData;
	    } catch (JDOObjectNotFoundException exception1) {
		MicroBehaviourURLCopy microBehaviourURLCopy = DataStore.begin().find(MicroBehaviourURLCopy.class, url);
		if (microBehaviourURLCopy == null) {
		    continue;
		} else {
		    try {
			MicroBehaviourData microBehaviourData = 
				persistenceManager.getObjectById(MicroBehaviourData.class, microBehaviourURLCopy.getOriginalURL());
			persistenceManager.close();
			return microBehaviourData.withNewURL(url);
		    } catch (JDOObjectNotFoundException exception2) {
			continue;
		    }
		}	
	    }
	}
	persistenceManager.close();
	return null;
    }

    public String getBehaviourDescriptionHTML() {
        return behaviourDescriptionHTML.getValue();
    }

    public String getBehaviourCode() {
        return BehaviourCode.get(CommonUtils.removeBookmark(url));
    }
    
    public ArrayList<String> getTextAreaElements() {
	return BehaviourCode.getTextAreaElements(CommonUtils.removeBookmark(url));
    }

//    public ArrayList<String> getTextAreaDefaultValues() {
//	if (textAreaDefaultValues == null) {
//	    return null;
//	} else {
//	    ArrayList<String> result = new ArrayList<String>();
//	    for (Text textAreaDefaultValue : textAreaDefaultValues) {
//		result.add(textAreaDefaultValue.getValue());
//	    }
//	    return result;
//	}  
//    }
//
//    public void setTextAreaDefaultValues(List<String> textAreaDefaultValues) {
//	if (textAreaDefaultValues == null) {
//	    this.textAreaDefaultValues = null;
//	} else {
//	    ArrayList<Text> textAreaDefaultValuesAsText = new ArrayList<Text>();
//	    for (String textAreaDefaultValue : textAreaDefaultValues) {
//		textAreaDefaultValuesAsText.add(new Text(textAreaDefaultValue));
//	    }
//	    this.textAreaDefaultValues = textAreaDefaultValuesAsText;
//	}
//    }

    public ArrayList<MacroBehaviour> getMacroBehaviours(final ResourcePageServiceImpl resourcePageServiceImpl, final ServletRequest servletRequest) {
	if (macroBehaviourData == null || macroBehaviourData.isEmpty()) {
	    return null;
	} else {
	    List<MacroBehaviourData> macroBehaviourDataList = 
		ServerUtils.getObjectsById(MacroBehaviourData.class, macroBehaviourData);
	    if (macroBehaviourDataList == null || macroBehaviourDataList.isEmpty()) {
		// if running development server try the production server
		RemoteAPI freeRemoteAPI = ServerUtils.getFreeRemoteAPI(servletRequest);
		if (freeRemoteAPI != null) {
		    RunOnProductionGAECallback<ArrayList<MacroBehaviour>> callback = 
			    new RunOnProductionGAECallback<ArrayList<MacroBehaviour>>() {

			@Override
			public ArrayList<MacroBehaviour> execute() {
			    return getMacroBehaviours(resourcePageServiceImpl, servletRequest);
			}

		    };
		    return freeRemoteAPI.runOnProductionGAE(callback);
		}
	    }
	    ArrayList<MacroBehaviour> macroBehaviours = new ArrayList<MacroBehaviour>();
	    if (macroBehaviourDataList != null) {
		for (MacroBehaviourData macroBehaviourData : macroBehaviourDataList) {
		    macroBehaviours.add(macroBehaviourData.getMacroBehaviour(resourcePageServiceImpl));
		}
	    }
	    return macroBehaviours;    
	}
    }

    private void setMacroBehaviours(List<MacroBehaviourData> macroBehaviourData) {
	if (macroBehaviourData == null || macroBehaviourData.isEmpty()) {
	    this.macroBehaviourData = null;
	} else {
	    ArrayList<Long> macroBehaviourIds = new ArrayList<Long>();
	    for (MacroBehaviourData data : macroBehaviourData) {
		Key key = data.getKey();
		if (key == null) {
		    ServerUtils.persist(data);
		    key = data.getKey();
		}
		if (key != null) {
		    // while debugging key was null and yet it is supposed to be auto-generated
		    macroBehaviourIds.add(key.getId());
		}
	    }
	    this.macroBehaviourData = macroBehaviourIds;
	}
    }

    public String getUrl() {
        return url;
    }

//    public void setBehaviourCode(String behaviourCode) {
//        this.behaviourCode = new Text(behaviourCode);
////	if (behaviourCode == null) {
////	    System.err.println("behaviourCode is null in MicroBehaviourData.setBehaviourCode");
////	}
//    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public MicroBehaviour getMicroBehaviour(ResourcePageServiceImpl resourcePageServiceImpl, ServletRequest servletRequest) {
	String behaviourCode = getBehaviourCode();
	String url = getUrl();
	if (behaviourCode == null) {
	    // for backwards compatibility need to call the following to update the BehaviourCode instance for this URL
	    resourcePageServiceImpl.fetchAndTransformPage(CommonUtils.removeBookmark(url), "sessionGuid", "userGuid", 0, resourcePageServiceImpl.getHostBaseURL(), false, true);
	    behaviourCode = getBehaviourCode();
	    if (behaviourCode == null) {
		// warn??
		return null;
	    }
	}
	return new MicroBehaviour(
		    this,
		    getBehaviourDescriptionHTML(),
		    behaviourCode,
		    url,
		    getTextAreaElements(),
		    getTextAreaValues(),
		    getEnhancements(),
		    getMacroBehaviours(resourcePageServiceImpl, servletRequest),
		    resourcePageServiceImpl);
    }

    private List<MicroBehaviourEnhancement> getEnhancements() {
	ArrayList<MicroBehaviourEnhancement> typedEnhancements = new ArrayList<MicroBehaviourEnhancement>();
	for (Integer enhancementIndex : enhancementIndices) {
	    typedEnhancements.add(MicroBehaviourEnhancement.getEnhancement(enhancementIndex));
	}
	return typedEnhancements;
    }

    public HashMap<Integer, String> getTextAreaValues() {
	// TODO: simplify all this
	HashMap<Integer, String> hashMap = new HashMap<Integer, String>();
	int index = 0;
	for (Text text : textAreaValues) {
	    if (text != null) {
		hashMap.put(index, text.getValue());
	    }
	    index++;
	}
	if (textAreaNameValue != ServerUtils.NULL_TEXT && textAreaNameValue != null) {
	    hashMap.put(-1, textAreaNameValue.getValue());
	}
        return hashMap;
    }

    public void setTextAreaValues(HashMap<Integer, String> textAreaValues) {
	int maxIndex = CommonUtils.maximumIndex(textAreaValues);
	int size = maxIndex+1;
	ArrayList<Text> list = new ArrayList<Text>(size);
	// initialise the array
	for (int i = 0; i < size; i++) {
	    // using null here triggers a bug in org.datanucleus.sco.backed.ArrayList.equals
	    list.add(ServerUtils.NULL_TEXT);
	}
	Set<Entry<Integer, String>> entrySet = textAreaValues.entrySet();
	for (Entry<Integer, String> entry : entrySet) {
	    Integer index = entry.getKey();
	    String value = entry.getValue();
	    if (value != null) {
		if (!value.equals(CommonUtils.TOKEN_FOR_REMOVED_TEXT_AREA)) {
		    if (index == -1) {
			textAreaNameValue = new Text(value);
		    } else {
			list.set(index, new Text(value));
		    }
		}
	    }
	}
        this.textAreaValues = list;
    }

    private void setEnhancements(List<MicroBehaviourEnhancement> enhancements) {
	this.enhancementIndices = new ArrayList<Integer>();
	if (enhancements != null) {
	    for (MicroBehaviourEnhancement enhancement : enhancements) {
		enhancementIndices.add(enhancement.ordinal());
	    }
	}
    }
    
    private String getSerialization() {
	StringBuffer stringBuffer = new StringBuffer();
	stringBuffer.append(CommonUtils.removeBookmark(url) + ";");
	// following is the same for all with the same URL
//	if (behaviourDescriptionHTML != null) {
//	    stringBuffer.append(behaviourDescriptionHTML.getValue() + ";");
//	}
	// following is the same for all with the same URL
//	stringBuffer.append(behaviourCode.getValue());
	// following is the same for all with the same URL
//	if (textAreaElements != null) {
//	    for (Text textAreaElement : textAreaElements) {
//		stringBuffer.append(textAreaElement.getValue() + ";");
//	    }
//	}
	if (textAreaValues != null) {
	    for (Text textAreaValue : textAreaValues) {
		stringBuffer.append(textAreaValue.getValue() + ";");
	    }
	}
	if (textAreaNameValue != null) {
	    stringBuffer.append(textAreaNameValue.getValue() + ";");
	}
	if (enhancementIndices != null) {
	    for (Integer index : enhancementIndices) {
		stringBuffer.append(index + ";");
	    }
	}
	if (macroBehaviourData != null) {
	    for (Long key : macroBehaviourData) {
		stringBuffer.append(key + ";");
	    }
	}
	return stringBuffer.toString();
    }

}
