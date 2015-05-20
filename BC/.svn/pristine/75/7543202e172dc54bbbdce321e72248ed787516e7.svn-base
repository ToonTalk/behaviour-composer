package uk.ac.lkl.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.Modeller;

import uk.ac.lkl.client.composer.MacroBehaviourView;

@SuppressWarnings("serial")
public class ActivateMacroBehaviourEvent extends ModellerEvent {

    protected String macroBehaviourNameAtEventTime = null;
    
    public ActivateMacroBehaviourEvent(MacroBehaviourView behaviour) {
	super(behaviour);
	macroBehaviourNameAtEventTime = behaviour.getHTML();
    }
//    
//    public ActivateMacroBehaviourEvent(String macroBehaviourNameAtEventTime) {
//	super(Modeller.getMacroBehaviourWithHTMLName(macroBehaviourNameAtEventTime));
//	this.macroBehaviourNameAtEventTime = macroBehaviourNameAtEventTime;
//    }

    @Override
    public String toHTMLString(boolean brief) {
	return Modeller.constants.activated() + Modeller.NON_BREAKING_SPACE + macroBehaviourNameAtEventTime;
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().activateMacroBehaviourEvent(
		Modeller.userGuid, 
		Modeller.sessionGuid, 
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
	    if (!macroBehaviour.isActive()) {
		if (!justRecord) {
		    macroBehaviour.setActive(true);
		}
		if (restoringHistory) {
		    event = new ActivateMacroBehaviourEvent(macroBehaviour);
		}
	    } // else already is -- don't record since then undo will act wrong
	} else {
	    Modeller.addToErrorLog("Could not find a macro behaviour " + macroBehaviourName + 
		                   " in ActivateMacroBehaviourEvent. Known macro-behaviours are: " +
		                   Modeller.instance().getMacroBehaviourNames());
	}
	continuation.reconstructSubsequentEvents(event);
    }

    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setActive(false);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setActive(true);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }

    public MacroBehaviourView getMacroBehaviour() {
	return (MacroBehaviourView) getSource();
    }

}
