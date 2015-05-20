package uk.ac.lkl.client.event;

import uk.ac.lkl.client.Modeller;

import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.composer.MacroBehaviourView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

@SuppressWarnings("serial")
public class MoveMicroBehaviourEvent extends ModellerEvent {
    protected String macroBehaviourNameAtEventTime = null;
    protected MacroBehaviourView macroBehaviour;
    protected boolean up;
    
    public MoveMicroBehaviourEvent(MicroBehaviourView microBehaviour, boolean up) {
	super(microBehaviour);
	this.up = up;
	macroBehaviour = getMacroBehaviour();
	if (macroBehaviour != null) {
	    macroBehaviourNameAtEventTime = macroBehaviour.getHTML();
	}
    }

    public String toHTMLString(boolean brief) {
	String briefVersion = Modeller.constants.moved() + " " + (up?Modeller.constants.up():Modeller.constants.down()) + " " + getMicroBehaviour().getHTML();
	if (brief) {
	    return briefVersion;
	} else {
	    return briefVersion + " " + Modeller.constants.in() + " " + macroBehaviourNameAtEventTime;
	}
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	String url = getMicroBehaviour().getAllURLs();
	Modeller.getHistoryService().moveMicroBehaviourEvent(
		up,
		Modeller.userGuid, 
		Modeller.sessionGuid, 
		macroBehaviourNameAtEventTime,
		getMacroBehaviour().getMicroBehaviourUrl(),
		getMicroBehaviour().getNameHTML(),
		url, 
		notifyOthers,
		recordSubsequentEventCallback);
    }
    
    protected static void reconstructInMacroBehaviour(MacroBehaviourView macroBehaviour,
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
	    boolean moveUp = eventElement.getAttribute("up").equals("1");
	    if (!justRecord) {
		macroBehaviour.moveMicroBehaviour(microBehaviour, moveUp);
	    }
	    if (restoringHistory) {
		event = new MoveMicroBehaviourEvent(microBehaviour, moveUp);
	    }   
	} else if (!macroBehaviour.isOnAMicroBehaviour()){
	    // otherwise probably was subsequently removed from the macro-behaviour
	    // so no need to issue a warning
	    Modeller.addToErrorLog("Could not find a micro behaviour " + url + " in " + macroBehaviourName + 
	                           " in MoveMicroBehaviourEvent. Known micro-behaviour URLs are: " +
	                           macroBehaviour.getMicroBehaviourURLs());
	} 
	continuation.reconstructSubsequentEvents(event);
    }

    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    macroBehaviour.moveMicroBehaviour(getMicroBehaviour(), !up);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    macroBehaviour.moveMicroBehaviour(getMicroBehaviour(), up);
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
