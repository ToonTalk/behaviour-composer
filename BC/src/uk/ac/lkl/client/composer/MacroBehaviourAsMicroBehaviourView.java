/**
 * 
 */
package uk.ac.lkl.client.composer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.lkl.client.BehaviourComposer;
import uk.ac.lkl.client.MicroBehaviourComand;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.shared.CommonUtils;

/**
 * This supports adding a macro behaviour to a a list
 * of micro-behaviours where it behaves as the macro-behaviour's list semantically
 * but has its own special interface.
 * 
 * @author Ken Kahn
 *
 */
public class MacroBehaviourAsMicroBehaviourView extends MicroBehaviourView {
    
    protected MacroBehaviourView macroBehaviour = null;
    
    // Might while loading only know its name but too soon to look up the macroBehaviour
    protected String macroBehaviourName = null;
    
    protected boolean active = true;
    
    public MacroBehaviourAsMicroBehaviourView(MacroBehaviourView macroBehaviour) {
	super(macroBehaviour.getNameHTML());
	this.macroBehaviour = macroBehaviour;
	macroBehaviourName = macroBehaviour.getNameHTML();
	macroBehaviour.addViewSharingNameHTML(this);
	setStylePrimaryName("modeller-prototype-name");
    }
    
    public MacroBehaviourAsMicroBehaviourView(String macroBehaviourName) {
	// may not yet have loaded the MacroBehaviour so look it up when needed
	super(macroBehaviourName);
	this.macroBehaviourName = macroBehaviourName;
	setStylePrimaryName("modeller-prototype-name");
    }

    @Override
    public MacroBehaviourView getMacroBehaviourViewedAsMicroBehaviour(boolean warn) {
	if (macroBehaviour == null) {
	    macroBehaviour = Modeller.instance().getMacroBehaviourWithHTMLName(macroBehaviourName);
	    if (macroBehaviour == null && warn) {
		Modeller.addToErrorLog(
			"A micro-behaviour refers to a list of micro-behaviours named " +
			macroBehaviourName + " but it isn't in the Composer's Build panel.");
	    }
	}
	return macroBehaviour;
    }
    
    @Override
    public boolean walkMicroBehaviourViews(MicroBehaviourComand command) {
	if (macroBehaviour == null) {
	    return macroBehaviour.walkMicroBehaviourViews(command);
	} else {
	    return true; // continue
	}
    }
    
    @Override
    public String getDescription() {
	return Modeller.constants.macroBehaviourAsMicroBehaviourTitle();
    }
    
    @Override
    protected String createTitle(String description, String advice) {
	return description + TITLE_NEW_LINE_SEPARATOR + advice;
    }
    
    @Override
    public boolean isMacroBehaviourViewedAsMicroBehaviourNamed(String nameHTML) {
	return nameHTML.equals(macroBehaviourName);
    }
    
    @Override
    public String getInactiveStyle() {
	return "modeller-prototype-name-inactive";
    }
    
    @Override
    public String getModelXML(HashMap<MicroBehaviourView, MicroBehaviourView> dirtyMicroBehaviours, 
	                      ArrayList<MicroBehaviourView> seenBefore,
	                      int level) {
	return "<MacroBehaviourAsMicroBehaviour><name>" +
	       CommonUtils.createCDATASection(getNameHTML()) +
	       "</name></MacroBehaviourAsMicroBehaviour>";
    }
    
    @Override
    protected void createMicroBehaviourWaitingToBeAdded() {
	BehaviourComposer.microBehaviourWaitingToBeAdded = copy();
    }
    
    @Override
    public ArrayList<MacroBehaviourView> getMacroBehaviourViews() {
	// this is acting like a micro-behaviour but is itself a macro-behaviour
	ArrayList<MacroBehaviourView> macroBehaviourViews = new ArrayList<MacroBehaviourView>();
	MacroBehaviourView macroBehaviour = getMacroBehaviourViewedAsMicroBehaviour(true);
	if (macroBehaviour != null) {
	    macroBehaviourViews.add(macroBehaviour);
	}
	return macroBehaviourViews;
    }
    
    @Override 
    public MicroBehaviourView copy() {
	if (macroBehaviour != null) {
	    return new MacroBehaviourAsMicroBehaviourView(macroBehaviour);
	} else {
	    return new MacroBehaviourAsMicroBehaviourView(macroBehaviourName);
	}
    }
    
    @Override
    public MicroBehaviourView copyWithoutSharing(HashMap<MicroBehaviourView, MicroBehaviourView> freshCopies) {
	MicroBehaviourView copy = copy();
	freshCopies.put(this, copy);
	return copy;
    }
    
    @Override
    protected boolean okToAddCustomisationMenuItem() {
	return false;
    }
    
    @Override
    protected boolean okToAddRenameMenuItem() {
	return false;
    }
    
    @Override
    public String getNameHTML() {
	return getHTML();
    }
    
    @Override
    public String getNameHTMLAndDescription() {
	return getHTML();
    }
    
    @Override
    public boolean setNameHTMLAndDescription(String description) {
	return false; // do nothing
    }
    
    @Override
    public void setContainingMacroBehaviour(MacroBehaviourView macroBehaviour) {
        // might want to store it
    }
    
    @Override
    public String getUrl() {
	// encode the HTML so no risk of it confusing later processing 
	// (especially the flattening of lists of lists of micro-behaviours in the data store)
        return CommonUtils.TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN + CommonUtils.encode(getNameHTML());
    }
    
    @Override
    public String getAllURLs() {
        return getUrl();
    }
    
    @Override
    public void setUrl(String url) {
	// ignore
    }
    
    @Override
    public boolean isMacroBehaviour() {
	return true;
    }
    
    @Override
    protected void addNetLogoCodeToTitle() {
	// ignore
    }
    
    @Override
    public boolean isCopyMicroBehaviourWhenExportingURL() {
	return false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public List<String> getPreviousURLs() {
	return new ArrayList<String>();
    }
    
}
