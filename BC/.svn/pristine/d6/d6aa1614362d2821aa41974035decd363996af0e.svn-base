/**
 * 
 */
package uk.ac.lkl.server;

import java.util.ArrayList;

import uk.ac.lkl.shared.CommonUtils;

/**
 * Represents a macro-behaviour used as a micro-behaviour (really a list of them) 
 * 
 * @author Ken Kahn
 *
 */
public class MacroBehaviourAsMicroBehaviour extends MicroBehaviour {
    
    protected String name;
    
    public MacroBehaviourAsMicroBehaviour(String nameHTML, NetLogoModel netLogoModel) {
	super();
	this.name = CommonUtils.removeHTMLMarkup(nameHTML).trim();
    } 
    
    @Override
    public MicroBehaviour copy(ArrayList<MacroBehaviour> macroBehavioursCopiesSoFar) {
	return new MacroBehaviourAsMicroBehaviour(name, null);
    }
    
    @Override
    public String getBehaviourName(boolean quote) {
	return getBehaviourName(quote, new ArrayList<MicroBehaviour>());
    }
    
    public String getBehaviourName(boolean quote, ArrayList<MicroBehaviour> visitedMicroBehaviours) {
	// this actually can return many names
	if (netLogoModel == null) {
	    return "";
	}
	MacroBehaviour macroBehaviour = netLogoModel.getMacroBehaviourNamed(name);
	if (macroBehaviour == null) {
	    if (!netLogoModel.getMacroBehaviours().isEmpty()) {
		// will be empty if fetching code for title of micro behaviour view
		netLogoModel.warn("Could not find the list of micro-behaviours named " + name);
	    }
	    return "";
	} else {
	    StringBuilder names = new StringBuilder();
	    for (MicroBehaviour microBehaviour : macroBehaviour.getMicroBehaviours()) {
		if (macroBehaviour.isActive(microBehaviour) && !visitedMicroBehaviours.contains(microBehaviour)) {
		    visitedMicroBehaviours.add(microBehaviour);
		    names.append(microBehaviour.getBehaviourName(quote, visitedMicroBehaviours));
		    names.append("\n ");
		}
	    }
	    return names.toString();
	}
    }
    
    @Override
    public boolean isMacroBehaviourAsMicroBehaviour() {
	return true;
    }
    
    @Override
    protected String generateNetLogo(NetLogoModel netLogoModel, boolean prototypeActive) 
     throws NetLogoException {
	// no additional code needed since micro-behaviours are listed elsewhere
	return "";
    }
    
    @Override
    protected boolean isRawNetLogoCode() {
	return false;
    }
    
    @Override
    protected void updateMicroBehaviourData() {
	// nothing to do since these are immutable
    }

}
