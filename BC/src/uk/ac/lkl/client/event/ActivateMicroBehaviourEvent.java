package uk.ac.lkl.client.event;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.composer.MacroBehaviourView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

@SuppressWarnings("serial")
public class ActivateMicroBehaviourEvent extends ModellerEvent {

    protected String macroBehaviourNameAtEventTime = null;
    
    public ActivateMicroBehaviourEvent(MicroBehaviourView microBehaviour) {
	super(microBehaviour);
	MacroBehaviourView macroBehaviour = getMacroBehaviour();
	if (macroBehaviour != null) {
	    macroBehaviourNameAtEventTime = macroBehaviour.getHTML();
	}
    }

    public String toHTMLString(boolean brief) {
	String briefVersion = Modeller.constants.activated() + " " + getMicroBehaviour().getHTML();
	if (brief) {
	    return briefVersion;
	} else {
	    return briefVersion + " " + Modeller.constants.in() + " " + macroBehaviourNameAtEventTime;
	}
    }
        
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	String url = getMicroBehaviour().getAllURLs();
	Modeller.getHistoryService().activateMicroBehaviourEvent(
		Modeller.userGuid, 
		Modeller.sessionGuid, 
		macroBehaviourNameAtEventTime,
		getMacroBehaviour().getMicroBehaviourUrl(),
		getMicroBehaviour().isMacroBehaviour(),
		url, 
		notifyOthers,
		recordSubsequentEventCallback);
    }
    
    protected static void reconstructInMacroBehaviour(
	    MacroBehaviourView macroBehaviour,
	    final String macroBehaviourName, 
	    Element eventElement,
	    String url,
	    boolean restoringHistory, 
	    boolean justRecord,
	    int version, 
	    final ReconstructEventsContinutation continuation) {
	MicroBehaviourView microBehaviour = macroBehaviour.getMicroBehaviourWithURL(url, true, false);
	ModellerEvent event = null;
	if (microBehaviour != null) {
	    if (!microBehaviour.isActive()) {
		if (!justRecord) {
		    microBehaviour.setActive(true);
		}
		if (restoringHistory) {
		    event = new ActivateMicroBehaviourEvent(microBehaviour);
		}
	    } // else don't record since is already active
	} else {
	    Modeller.addToErrorLog("Could not find a micro behaviour " + url + " in " + macroBehaviourName + 
	                           " in ActivateMicroBehaviourEvent. Known micro-behaviour URLs are: " +
	                           macroBehaviour.getMicroBehaviourURLs());
	}
	continuation.reconstructSubsequentEvents(event);
    }

    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMicroBehaviour().setActive(false);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMicroBehaviour().setActive(true);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }

    public MicroBehaviourView getMicroBehaviour() {
	return (MicroBehaviourView) getSource();
    }
    
    public MacroBehaviourView getMacroBehaviour() {
	Widget parent = getMicroBehaviour().getParent();
	if (parent instanceof MacroBehaviourView) {
	    return (MacroBehaviourView) parent;
	} else {
	    return null;
	}
    }

}
