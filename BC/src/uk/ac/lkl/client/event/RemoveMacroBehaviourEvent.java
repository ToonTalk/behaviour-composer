package uk.ac.lkl.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.Modeller;

import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.shared.CommonUtils;

@SuppressWarnings("serial")
public class RemoveMacroBehaviourEvent extends ModellerEvent {

    protected String macroBehaviourNameAtEventTime = null;
    
    public RemoveMacroBehaviourEvent(MacroBehaviourView behaviour) {
	super(behaviour);
	macroBehaviourNameAtEventTime = behaviour.getHTML();
    }

    public String toHTMLString(boolean brief) {
	return Modeller.constants.removed() + Modeller.NON_BREAKING_SPACE + macroBehaviourNameAtEventTime;
    }
    
    public String getXML() {
	return "<RemoveMacroBehaviourEvent version='2' name='" + 
	       CommonUtils.encode(macroBehaviourNameAtEventTime) + "'" +
	       getDateAttribute() + "/>";
    }
      
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().removeMacroBehaviourEvent(Modeller.userGuid, Modeller.sessionGuid, 
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
	MacroBehaviourView macroBehaviour = Modeller.instance().getMacroBehaviourWithHTMLName(macroBehaviourName);
	ModellerEvent event = null;
	String macroBehaviourNames = Modeller.instance().getMacroBehaviourNames();
	if (macroBehaviour != null) {
	    if (!justRecord) {
		if (!Modeller.instance().removeMacroBehaviour(macroBehaviour)) {
		    // not serious enough for Modeller.addToErrorLog
		    // and it seems to happen sometimes when reconstructing a session that
		    // began by replacing the current model
		    System.out.println("Could not remove a macro behaviour " + macroBehaviourName + 
		                       " in RemoveMacroBehaviourEvent. Known macro-behaviours are: " +
		                       macroBehaviourNames);
		}
	    }
	    if (restoringHistory) {
		event = new RemoveMacroBehaviourEvent(macroBehaviour);
	    }
	} else {
	    // not serious enough for Modeller.addToErrorLog
	    // and it seems to happen sometimes when reconstructing a session that
	    // began by replacing the current model
	    if (!macroBehaviourNames.isEmpty()) {
		// ignore errors removing prototypes if there are none
		System.out.println("Could not find a macro behaviour " + macroBehaviourName + 
			           " in RemoveMacroBehaviourEvent. Known macro-behaviours are: " +
			           macroBehaviourNames);
	    }
	}
	continuation.reconstructSubsequentEvents(event);
    }

    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    Modeller.instance().addMacroBehaviour(getMacroBehaviour());
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    Modeller.instance().removeMacroBehaviour(getMacroBehaviour());
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }

    public MacroBehaviourView getMacroBehaviour() {
	return (MacroBehaviourView) getSource();
    }

}
