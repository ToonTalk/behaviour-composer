/**
 * 
 */
package uk.ac.lkl.client.composer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import uk.ac.lkl.client.BrowsePanel;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.shared.CommonUtils;

/**
 * Every micro-behaviour view shares some state captured here.
 * 
 * @author Ken Kahn
 *
 */
public class MicroBehaviourSharedState {
    // external identity of the micro-behaviour
    protected String url;
    // use a hash map rather than an array since the values can come in any order
    // and don't know here the full size
    protected HashMap<Integer, String> textAreaValues = null;
    protected boolean active = true;
    // following no longer initialised as an empty array since null is now used to indicate not yet initialised
    protected ArrayList<MicroBehaviourEnhancement> enhancements = null;
    protected ArrayList<MacroBehaviourView> macroBehaviourViews = new ArrayList<MacroBehaviourView>();
    // following could be replaced by a listener scheme
    protected ArrayList<MicroBehaviourView> microBehaviourViews = new ArrayList<MicroBehaviourView>();
    protected String nameHTMLAndDescription;
    // following used to guarantee uniqueness of names
    private static HashMap<String, Integer> nameHTMLCounts = new HashMap<String, Integer>();
    // used for producing up-to-date informative titles for tabs
    protected MacroBehaviourView containingMacroBehaviour = null;
    // need to store the number of text areas before any enhancements
    protected int originalTextAreasCount = -1; // negative value indicates uninitialised
    
    // avoids redundant work and perhaps prevents infinite recursion (Issue 333)
//    protected boolean textAreasUpdated = false;
    
    // so disclosure of URL is maintained properly
    private boolean copyMicroBehaviourWhenExportingURL = false;
    
    // default if browse panel isn't around
    private boolean copyOnUpdate = false;
    private Integer nameCount;
    private boolean subMicroBehavioursNeedNewURLs = false;
    
    private boolean warnThatTextAreasHaveChanged = false;
    private ArrayList<String> previousURLs = new ArrayList<String>();
    
    private static HashMap<String, MicroBehaviourSharedState> urlToSharedStateMap =
	new HashMap<String, MicroBehaviourSharedState>();
    
    @SuppressWarnings("unchecked")
    public MicroBehaviourSharedState(
	    String nameHTMLAndDescription, 
	    String url, 
	    HashMap<Integer, String> originalTextAreaValues,
	    ArrayList<MicroBehaviourEnhancement> enhancements,
	    boolean recordNameChangeInDatabase, // TODO: determine if this is obsolete
	    int originalTextAreasCount,
	    boolean okToAddSubscripts) {
	setUrl(url, false);
	if (originalTextAreaValues != null) {
	    this.textAreaValues = (HashMap<Integer, String>) originalTextAreaValues.clone();
	}
	if (enhancements != null) {
	    this.enhancements = enhancements;
	}
	this.originalTextAreasCount = originalTextAreasCount;
	String nameHTML = CommonUtils.getNameHTML(nameHTMLAndDescription);
	nameCount = getCurrentNameCount(nameHTML)+1;
	nameHTMLCounts.put(nameHTML, nameCount);
//	setCopyMicroBehaviourWhenExportingURL(true);
	setNameHTMLAndDescription(nameHTMLAndDescription);
    }

    /**
     * @param nameHTML
     * @return
     */
    public int getCurrentNameCount(String nameHTML) {
	Integer count = nameHTMLCounts.get(nameHTML);
	if (count == null) {
	    return 0;
	} else {
	    return count;
	}
    }

    public static MicroBehaviourSharedState findOrCreateSharedState(String nameHTML, 
	                                                            String allURLs,
	                                                            HashMap<Integer, String> textAreaValues,
	                                                            ArrayList<MicroBehaviourEnhancement> enhancements,
	                                                            int originalTextAreasCount,
	                                                            boolean okToAddSubscripts) {
	String url = CommonUtils.firstURL(allURLs);
//	String[] urls = allURLs.split(";");
//	for (String url : urls) {
	    MicroBehaviourSharedState microBehaviourSharedState = urlToSharedStateMap.get(url);
	    if (microBehaviourSharedState != null) {
		return microBehaviourSharedState;
	    }
//	}
//	MicroBehaviourView microBehaviourView = Modeller.instance().getMicroBehaviourView(allURLs);
//	if (microBehaviourView != null) {
//	    return microBehaviourView.getSharedState();
//	}	
	return createSharedState(
		nameHTML, allURLs, textAreaValues, enhancements, true, originalTextAreasCount, okToAddSubscripts);	
    }
    
    public static void clearUrlToSharedStateMap() {
	urlToSharedStateMap.clear();
    }
    
    public static MicroBehaviourSharedState createSharedState(String nameHTML, 
                                                              String url,
                                                              HashMap<Integer, String> textAreaValues,
                                                              ArrayList<MicroBehaviourEnhancement> enhancements,
                                                              boolean shareState,
                                                              int originalTextAreasCount,
                                                              boolean okToAddSubscripts) {
	MicroBehaviourSharedState microBehaviourSharedStateCopy = 
	    new MicroBehaviourSharedState(
		    nameHTML, url, textAreaValues, enhancements, shareState, originalTextAreasCount, okToAddSubscripts);
	if (shareState) {
	    addToSharedStateMap(url, microBehaviourSharedStateCopy);
	}
	return microBehaviourSharedStateCopy;	
    }

    protected static void addToSharedStateMap(String url, MicroBehaviourSharedState microBehaviourSharedState) {
	if (CommonUtils.hasChangesGuid(url) || microBehaviourSharedState.getContainingMacroBehaviour() != null) { 
	    // don't share state for uncopied micro-behaviours
	    // unless is coming from a macro-behaviour (e.g. WANDER-RANDOMLY-BY-TURNING)
	    // this had been commented out but shouldn't maintain state for originals
	    urlToSharedStateMap.put(url, microBehaviourSharedState);
	}
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url, boolean waitingToBeCopied) {
	// not sure how white space ended up on these URLs but interferes with fetching by url
	// TODO: determine is this is still an issue
	url = url.trim();
	if (url.equals(this.url)) {
	    return; // already knew this
	}
//	if (url != null) {
	    int index = url.indexOf(';');
	    if (index > 0) {
		url = url.substring(0, index);
	    }
//	}
//	System.out.println(this + " had old url: " + this.url);
	if (!waitingToBeCopied && urlToSharedStateMap.get(this.url) == this) {
	    // this shared state being re-used for a different url
	    urlToSharedStateMap.remove(this.url);
	}
//	if (url.charAt(0) == '-') {
//	    url = url.substring(1);
//	}
	// following commented out since can now paste a new URL into the Links section of a micro-behaviour page
	// in order to update the underlying web page
	// if editing of micro-behaviours is enabled the following can happen without there being any problem
//	if (this.url != null &&
//            !differsOnlyByInactivedMarker(url, this.url) &&
//            !Modeller.pageEditingEnabled &&
//	    // should differ only in changes part of URL
//	    !CommonUtils.removeBookmark(url).equals(CommonUtils.removeBookmark(this.url))) {
//	    Utils.logServerMessage(Level.SEVERE, "Changing URL from " + this.url + " to " + url);
//	}
	if (!previousURLs.contains(url)) { 
	    previousURLs.add(url);
	}
	this.url = url;
	for (MacroBehaviourView macroBehaviourView : macroBehaviourViews) {
	    macroBehaviourView.setMicroBehaviourUrl(url);
	}
//	System.out.println(this + " has new url: " + this.url);
	addToSharedStateMap(url, this);
    }

    public List<String> getPreviousURLs() {
        return previousURLs;
    }
    
    public void resetPreviousURLs() {
	previousURLs.clear();
    }

//    private boolean differsOnlyByInactivedMarker(String url1, String url2) {
//	if (url1.charAt(0) == '-') {
//	    return url1.substring(1).equals(url2);
//	} else if (url2.charAt(0) == '-') {
//	    return url2.substring(1).equals(url1);
//	} else {
//	    return false;
//	}
//    }

    public String getUpdatedTextArea(int indexInCode) {
	if (textAreaValues == null) {
	    return null;
	} else {
	    return textAreaValues.get(indexInCode);
	}
    }
    
    public String getNameHTMLAndDescription() {
	return nameHTMLAndDescription;
    }    

    public boolean setNameHTMLAndDescription(String nameHTMLAndDescription) {
	if (this.nameHTMLAndDescription != null && this.nameHTMLAndDescription.equals(nameHTMLAndDescription)) {
	    return false;
	}
        this.nameHTMLAndDescription = nameHTMLAndDescription;
        if (textAreaValues == null) {
            textAreaValues = new HashMap<Integer, String>();
        }
        textAreaValues.put(-1, nameHTMLAndDescription);
        String nameHTML = getNameHTML();
        for (MicroBehaviourView microBehaviourView : microBehaviourViews) {
            microBehaviourView.setHTML(nameHTML);
//            // if this microBehaviourView is in a list on another micro-behaviour set the 'dirty' flag of that micro-behaviour
//            BrowsePanel containingBrowsePanel = microBehaviourView.getContainingBrowsePanel(true);
//            if (containingBrowsePanel != null && containingBrowsePanel != microBehaviourView.getParent()) {
//        	MicroBehaviourView containingMicroBehaviour = containingBrowsePanel.getMicroBehaviour();
//        	if (containingMicroBehaviour != null) {
//        	    containingMicroBehaviour.setCopyMicroBehaviourWhenExportingURL(true);
//        	}
//            }
        }
        return true;
    }
    
    public String getNameHTML() {
	return CommonUtils.getNameHTML(nameHTMLAndDescription);
    }
    
    public String getDescription() {
	return CommonUtils.getDescription(nameHTMLAndDescription);
    }
    
    protected void inactivateAll() {
	for (MicroBehaviourView microBehaviourView : microBehaviourViews) {
            microBehaviourView.setActive(false);
        }
    }
    
    public MicroBehaviourView getMicroBehaviourViewInBrowsePanel() {
	for (MicroBehaviourView microBehaviourView : microBehaviourViews) {
            if (!microBehaviourView.inAPrototypeOrList()) {
        	return microBehaviourView;
            }
        }
	return null;
    }
    
    public HashMap<Integer, String> getTextAreaValues() {
        return textAreaValues;
    }
    
    public void addTextAreaValues(HashMap<Integer, String> additionalTextAreaValues) {
	if (textAreaValues == null) {
	    textAreaValues = new HashMap<Integer, String>();
	}
	Set<Entry<Integer, String>> entrySet = additionalTextAreaValues.entrySet();
	for (Entry<Integer, String> entry : entrySet) {
	    textAreaValues.put(entry.getKey(), entry.getValue());
	}
    }

    public void addMacroBehaviourView(MacroBehaviourView macroBehaviourViewToAdd) {
	String nameHTMLOfAddition = macroBehaviourViewToAdd.getNameHTML();
	int size = macroBehaviourViews.size();
	for (int i = 0; i < size; i++) {
	    MacroBehaviourView macroBehaviourView = macroBehaviourViews.get(i);
	    if (macroBehaviourView.getNameHTML().equals(nameHTMLOfAddition)) {
		// already there -- replace the old one
		macroBehaviourViews.set(i, macroBehaviourViewToAdd);
		return;
	    }
	}
	macroBehaviourViews.add(macroBehaviourViewToAdd);
    }

    public ArrayList<MacroBehaviourView> getMacroBehaviourViews() {
        return macroBehaviourViews;
    }

    public void addMacroBehaviourViews(ArrayList<MacroBehaviourView> newMacroBehaviourViews,
	                               ArrayList<MicroBehaviourView> freshCopies) {
	if (newMacroBehaviourViews != null) {
	    for (MacroBehaviourView macroBehaviourView : newMacroBehaviourViews) {
		MacroBehaviourView copy = macroBehaviourView.copyFor(url, freshCopies);
		macroBehaviourViews.add(copy);
	    }
	    // need new URLs for the micro-behaviours under this
	    subMicroBehavioursNeedNewURLs = !newMacroBehaviourViews.isEmpty();
	}
    }
    
    public void setMacroBehaviourViews(ArrayList<MacroBehaviourView> newMacroBehaviourViews) {
	macroBehaviourViews = newMacroBehaviourViews;	
    }
    
    public MacroBehaviourView getMacroBehaviourNamed(String name) {
	for (MacroBehaviourView macroBehaviourView : macroBehaviourViews) {
	    if (macroBehaviourView.getNameHTML().equals(name)) {
		return macroBehaviourView;
	    }
	}
	return null;
    }

    public String urlNeedsFetching() {
	String changesGuid = CommonUtils.changesGuid(url);
	if (changesGuid == null) {
	    return null; // no need to fetch
	} else {
	    return url;
	}
    }

//    public boolean isTextAreasUpdated() {
//        return textAreasUpdated;
//    }
//
//    public void setTextAreasUpdated(boolean textAreasUpdated) {
//        this.textAreasUpdated = textAreasUpdated;
//    }
    
    public void addMicroBehaviourView(MicroBehaviourView microBehaviourView) {
	microBehaviourViews.add(microBehaviourView);
    }
    
    public void removeMicroBehaviourView(MicroBehaviourView microBehaviourView) {
	microBehaviourViews.remove(microBehaviourView);
    }

    public MacroBehaviourView getContainingMacroBehaviour() {
        return containingMacroBehaviour;
    }

    public void setContainingMacroBehaviour(MacroBehaviourView macroBehaviour) {
        this.containingMacroBehaviour = macroBehaviour;
        if (macroBehaviour != null) {
            // if uncopied but part of a macro-behaviour then add it to the table
            addToSharedStateMap(getUrl(), this);
        }
    }
    
    public String getMacroBehaviourName() {
	if (containingMacroBehaviour != null) {
	    // this ensures that multi-line names become reasonable plain text
	    return CommonUtils.htmlStringToText(containingMacroBehaviour.getHTML());
	} else {
	    return null;
	}
    }

    public boolean isCopyMicroBehaviourWhenExportingURL() {
        return copyMicroBehaviourWhenExportingURL;
    }

    public void setCopyMicroBehaviourWhenExportingURL(boolean copyMicroBehaviourWhenExportingURL) {
        this.copyMicroBehaviourWhenExportingURL = copyMicroBehaviourWhenExportingURL;
        // if this micro-behaviour has changed then the containing micro-behaviour (if there is one)
        // also has changed
        if (copyMicroBehaviourWhenExportingURL) {
            if (containingMacroBehaviour != null) {
        	BrowsePanel containingBrowsePanel = containingMacroBehaviour.getContainingBrowsePanel();
        	if (containingBrowsePanel != null) {
        	    containingBrowsePanel.copyMicroBehaviourWhenExportingURL();
        	}
            }
        } else {
            subMicroBehavioursNeedNewURLs = false;
        }
    }

    public boolean isCopyOnUpdate() {
        return copyOnUpdate;
    }

    public void setCopyOnUpdate(boolean copyOnUpdate) {
        this.copyOnUpdate = copyOnUpdate;
    }
    
    public void addEnhancement(MicroBehaviourEnhancement enhancement) {
	if (enhancements == null) {
	    enhancements = new ArrayList<MicroBehaviourEnhancement>();
	}
	enhancements.add(enhancement);
    }
    
    public void clearEnhancements() {
	if (enhancements == null) {
	    enhancements = new ArrayList<MicroBehaviourEnhancement>();
	} else {
	    enhancements.clear();
	}
    }
    
    public RemovedEnhancement removeLastEnhancement() {
	// when undoing an enhancement
	// or explicitly removing the last enhancement
	if (enhancements == null) {
	    // not an error if reconstructing a session and loaded a MB with enhancements 
	    // and then remove the last one
//	    System.err.println("Enhancements not yet initialised. Can't remove last one.");
	    return null;
	}
	if (textAreaValues == null) {
//	    System.err.println("Text areas not yet initialised. Can't remove last enhancement.");
	    return null;
	}
	int size = enhancements.size();
	if (size > 0) {
	    int enhancementIndex = size-1;
	    // also remove the value of the added text area
            //	NOTE this assumes that all enhancements add a single text area -- true currently
	    int textAreaIndex = CommonUtils.maximumIndex(textAreaValues);
	    String textAreaValueRemoved = textAreaValues.get(textAreaIndex);
	    textAreaValues.put(textAreaIndex, CommonUtils.TOKEN_FOR_REMOVED_TEXT_AREA);
	    MicroBehaviourEnhancement enhancementRemoved = enhancements.remove(enhancementIndex);
	    return new RemovedEnhancement(enhancementRemoved, textAreaValueRemoved, textAreaIndex);
	} else {
	    System.err.println("There are no enhancements to remove.");
	    return null;
	}
    }

    public ArrayList<MicroBehaviourEnhancement> getEnhancements() {
        return enhancements;
    }

    public void setEnhancements(ArrayList<MicroBehaviourEnhancement> enhancements) {
	this.enhancements = enhancements;
    }

    public int getOriginalTextAreasCount() {
        return originalTextAreasCount;
    }

    public void setOriginalTextAreasCount(int originalTextAreasCount) {
        this.originalTextAreasCount = originalTextAreasCount;
    }

    public void replaceURLs(String[] renamings) {
	// alternates old and new URLs
	// returns true if URL replaced
	for (int i = 0; i < renamings.length; i += 2) {
	    if (renamings[i].equals(url)) {
		url = renamings[i+1];
		// no need to worry about the browse panel since 
		// if it is dirty the links disclosure panel is closed
		setCopyMicroBehaviourWhenExportingURL(false);
		break;
	    }
	}
	for (MacroBehaviourView  macroBehaviourView : macroBehaviourViews) {
	    macroBehaviourView.replaceURLs(renamings);
	}
    }

    public Integer getNameCount() {
        return nameCount;
    }

    public boolean isSubMicroBehavioursNeedNewURLs() {
        return subMicroBehavioursNeedNewURLs;
    }

    public boolean isWarnThatTextAreasHaveChanged() {
        return warnThatTextAreasHaveChanged;
    }

    public void setWarnThatTextAreasHaveChanged(boolean warnThatTextAreasHaveChanged) {
        this.warnThatTextAreasHaveChanged = warnThatTextAreasHaveChanged;
    }

    protected boolean isActive() {
        return active;
    }

    protected void setActive(boolean active) {
        this.active = active;
    }

    public boolean containsURL(String otherURL, boolean withoutChangesGuid) {
	if (withoutChangesGuid) {
	    return getUrl().startsWith(otherURL);
	}
	if (otherURL.equals(getUrl())) {
	    return true;
	}
	for (MacroBehaviourView macroBehaviour : macroBehaviourViews) {
	    if (macroBehaviour.containsURL(otherURL, withoutChangesGuid)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * @return a string of all the URLs this micro-behaviour has had separated by semicolons
     * with the current one first and the rest in the order they were added
     */
    public String getAllURLs() {
	if (previousURLs == null || previousURLs.isEmpty()) {
	    return url;
	} else if (url != null ) {
	    int size = previousURLs.size();
	    String all = url; // start with the current one
	    for (int i = 0; i < size; i++) {
		String nextURL = previousURLs.get(i);
		if (!url.equals(nextURL) && CommonUtils.hasChangesGuid(nextURL)) {
		    all += ";" + nextURL;
		}
	    }
	    return all;
	} else {
	    return "";
	}
    }

    public void refresh() {
	BrowsePanel containingBrowsePanel = containingMacroBehaviour.getContainingBrowsePanel();
	if (containingBrowsePanel != null) {
	    containingBrowsePanel.refreshRegardless();
	} else {
	    Modeller.executeOnNewMicroBehaviourPage(getUrl(), null, false, false);
	}
    }

}
