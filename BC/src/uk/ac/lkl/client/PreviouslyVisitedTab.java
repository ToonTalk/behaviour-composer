/**
 * 
 */
package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.HashMap;

import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.client.composer.MicroBehaviourView;

/**
 * 
 * A record of a previously opened micro-behaviour
 * 
 * @author Ken Kahn
 *
 */
public class PreviouslyVisitedTab {
    
    private String url;
    private String tabName;
    private HashMap<Integer, String> textAreaValues;
    private ArrayList<MicroBehaviourEnhancement> enhancements;
    private ArrayList<MacroBehaviourView> macroBehaviourViews;
    private String macroBehaviourName = null;
    
    public PreviouslyVisitedTab(String url, BrowsePanel browsePanel) {
	this.url = url;
	this.tabName = browsePanel.getTabName();
	this.textAreaValues = browsePanel.getTextAreaValues();
	this.enhancements = browsePanel.getEnhancements();
	this.macroBehaviourViews = browsePanel.getMacroBehaviourViews();
	MicroBehaviourView microBehaviour = browsePanel.getMicroBehaviour();
	if (microBehaviour != null) {
	    macroBehaviourName = microBehaviour.getMacroBehaviourName();
	} 
    }
    
    @Override
    public boolean equals(Object other) {
	if (other instanceof PreviouslyVisitedTab) {
	    return url.equals(((PreviouslyVisitedTab) other).getUrl());
	} else {
	    return false;
	}
    }

    public String getUrl() {
        return url;
    }

    public String getTabName() {
        return tabName;
    }

    public HashMap<Integer, String> getTextAreaValues() {
        return textAreaValues;
    }

    public ArrayList<MacroBehaviourView> getMacroBehaviourViews() {
        return macroBehaviourViews;
    }
    
    public String getMacroBehaviourName() {
	return macroBehaviourName;
    }

    public ArrayList<MicroBehaviourEnhancement> getEnhancements() {
        return enhancements;
    }

    public boolean replaceURL(String oldURL, String newURL) {
	if (url.equals(oldURL)) {
	    url = newURL;
	    return true;
	} else {
	    return false;
	}
    }

}
