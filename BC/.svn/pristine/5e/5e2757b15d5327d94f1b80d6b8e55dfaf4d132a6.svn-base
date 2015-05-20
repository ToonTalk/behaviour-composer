package uk.ac.lkl.client.event;

import uk.ac.lkl.client.Modeller;

import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.composer.MacroBehaviourView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

@SuppressWarnings("serial")
public class InactivateMicroBehaviourEvent extends ModellerEvent {
    protected String macroBehaviourNameAtEventTime = null;
    
    public InactivateMicroBehaviourEvent(MicroBehaviourView microBehaviour) {
	super(microBehaviour);
	MacroBehaviourView macroBehaviour = getMacroBehaviour();
	if (macroBehaviour != null) {
	    macroBehaviourNameAtEventTime = macroBehaviour.getHTML();
	}
    }

    public String toHTMLString(boolean brief) {
	String briefVersion = Modeller.constants.inactivated() + " " + getMicroBehaviour().getHTML();
	if (brief) {
	    return briefVersion;
	} else {
	    return briefVersion + " " + Modeller.constants.in() + " " + macroBehaviourNameAtEventTime;
	}
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	String url = getMicroBehaviour().getAllURLs();
	Modeller.getHistoryService().inactivateMicroBehaviourEvent(
		Modeller.userGuid, 
		Modeller.sessionGuid, 
		macroBehaviourNameAtEventTime,
		getMacroBehaviour().getMicroBehaviourUrl(),
		getMicroBehaviour().isMacroBehaviour(),
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
	    if (microBehaviour.isActive()) {
		if (restoringHistory) {
		    event = new InactivateMicroBehaviourEvent(microBehaviour);
		} 
		if (!justRecord) {
		    microBehaviour.setActive(false);
		}
	    } // else don't record since is already inactive
	} else if (macroBehaviour.isOnAMicroBehaviour()) {
	    // Don't need to remove the micro-behaviour when reconstructing
	    // the removal of a micro-behaviour from a list on a micro-behaviour page
	    // since the removal also updated the micro-behaviour copy in the database
	    // description isn't available -
	    // TODO: get the server to add it
//	    if (restoringHistory) {
//		String nameHTML = Utils.getCDATAElementString("description", eventElement);
//		MicroBehaviourView newMicroBehaviour = new MicroBehaviourView(nameHTML, url, null, false);
//		newMicroBehaviour.setContainingMacroBehaviour(macroBehaviour);
//		event = new InactivateMicroBehaviourEvent(newMicroBehaviour);
//	    }
	} else {
	    Modeller.addToErrorLog("Could not find a micro behaviour " + url + " in " + macroBehaviourName + 
	                           " in InactivateMicroBehaviourEvent. micro-behaviour URLs are: " +
	                           macroBehaviour.getMicroBehaviourURLs());
	} 
	continuation.reconstructSubsequentEvents(event);
    }

    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMicroBehaviour().setActive(true);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMicroBehaviour().setActive(false);
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
