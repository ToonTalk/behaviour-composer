package uk.ac.lkl.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.Modeller;

import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.shared.CommonUtils;

@SuppressWarnings("serial")
public class InactivateMacroBehaviourEvent extends ModellerEvent {

    protected String macroBehaviourNameAtEventTime;
    
    public InactivateMacroBehaviourEvent(MacroBehaviourView behaviour) {
	super(behaviour);
	macroBehaviourNameAtEventTime = behaviour.getHTML();
    }

    public String toHTMLString(boolean brief) {
	return Modeller.constants.inactivated() + Modeller.NON_BREAKING_SPACE + macroBehaviourNameAtEventTime;
    }
    
    public String getXML() {
	return "<InactivateMacroBehaviourEvent version='2' name='" + 
	       CommonUtils.encode(macroBehaviourNameAtEventTime) + "'" +
	       getDateAttribute() + "/>";
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().inactivateMacroBehaviourEvent(Modeller.userGuid, Modeller.sessionGuid, 
		                                                   macroBehaviourNameAtEventTime, 
		                                                   notifyOthers, 
		                                                   recordSubsequentEventCallback);
    }
    
    public static void reconstruct(
	    String macroBehaviourName, 
	    Element eventElement, 
	    boolean restoringHistory, 
	    boolean justRecord, 
	    int version, 
	    ReconstructEventsContinutation continuation) {
	MacroBehaviourView  macroBehaviour = Modeller.instance().getMacroBehaviourWithHTMLName(macroBehaviourName);
	ModellerEvent event = null;
	if (macroBehaviour != null) {
	    if (macroBehaviour.isActive()) {
		if (!justRecord) {
		    macroBehaviour.setActive(false);
		}
		if (restoringHistory) {
		    event = new InactivateMacroBehaviourEvent(macroBehaviour);
		}
	    } // else don't record since is already inactive
	} else {
	    Modeller.addToErrorLog("Could not find a macro behaviour " + macroBehaviourName + 
	                           " in InactivateMacroBehaviourEvent. Known macro-behaviours are: " +
	                           Modeller.instance().getMacroBehaviourNames());
	}
	continuation.reconstructSubsequentEvents(event);
    }

    @Override
     public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setActive(true);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setActive(false);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }

    public MacroBehaviourView getMacroBehaviour() {
	return (MacroBehaviourView) getSource();
    }

}
