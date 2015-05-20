package uk.ac.lkl.client.event;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;

@SuppressWarnings("serial")
public class RemoveMicroBehaviourEvent extends ModellerEventOfMacroBehaviour {

    protected MicroBehaviourView microBehaviour;
    
    public RemoveMicroBehaviourEvent(MacroBehaviourView macroBehaviour, MicroBehaviourView microBehaviour) {
	super(macroBehaviour);
	this.microBehaviour = microBehaviour;
	macroBehaviourNameAtEventTime = macroBehaviour.getHTML();
	setMacroBehaviour(macroBehaviour);
    }
    
    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().addMicroBehaviour(microBehaviour, false, true);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().removeMicroBehaviour(microBehaviour);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    public String toHTMLString(boolean brief) {
	String html = Modeller.constants.removed() + " " + microBehaviour.getHTML();
	if (!brief) {
	    html += " " + Modeller.constants.from() + " " +  macroBehaviourNameAtEventTime; 
	    String microBehaviourUrl = getContainingURL();
	    if (microBehaviourUrl != null) {
		html += " " + Modeller.constants.in() + " " + microBehaviourUrl; 
	    }
	}
	return html;
    }
    
    public String getContainingURL() {
	MacroBehaviourView macroBehaviour = getMacroBehaviour();
	if (macroBehaviour != null) {
	    return macroBehaviour.getMicroBehaviourUrl();
	} else {
	    return null;
	}
    }
       
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	String url = getMicroBehaviour().getAllURLs();
	Modeller.getHistoryService().removeMicroBehaviourEvent(
		Modeller.userGuid, 
		Modeller.sessionGuid, 
		macroBehaviourNameAtEventTime,
		getMacroBehaviour().getMicroBehaviourUrl(),
		getMicroBehaviour().isMacroBehaviour(),
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
	MicroBehaviourView microBehaviour = macroBehaviour.getMicroBehaviourWithURL(url, false, false);
	if (microBehaviour == null && !macroBehaviour.isOnAMicroBehaviour()) {
	    // perhaps has been renamed or copied since so find a match ignoring copy identity
	    // TODO: determine if this should check that it is unique?
	    microBehaviour = macroBehaviour.getMicroBehaviourWithURL(url, true, false);
	}
	ModellerEvent event = null;
	if (microBehaviour != null) {
	    if (!justRecord) {
		macroBehaviour.removeMicroBehaviour(microBehaviour);
	    }
	    if (restoringHistory) {
		event = new RemoveMicroBehaviourEvent(macroBehaviour, microBehaviour);
	    }
	} else if (macroBehaviour.isOnAMicroBehaviour()) {
	    // Don't need to remove the micro-behaviour when reconstructing
	    // the removal of a micro-behaviour from a list on a micro-behaviour page
	    // since the removal also updated the micro-behaviour copy in the database
	    if (restoringHistory) {
		String nameHTML = Utils.getElementString("description", eventElement);
		// is it OK that this makes it have 0 text areas?
		MicroBehaviourView newMicroBehaviour = 
		    new MicroBehaviourView(nameHTML, url, null, null, 0, false);
		if (!justRecord) {
		    newMicroBehaviour.setContainingMacroBehaviour(macroBehaviour);
		}
		event = new RemoveMicroBehaviourEvent(macroBehaviour, newMicroBehaviour);
	    }
	} else {
	    String error = "Could not find a micro behaviour of " + macroBehaviourName;
	    String microBehaviourUrl = macroBehaviour.getMicroBehaviourUrl();
	    if (microBehaviourUrl != null) {
		error += " inside of " + microBehaviourUrl; 
	    }
	    error += " in RemoveMicroBehaviourEvent of " + url;
	    // don't bother the user about these since inability to remove something that isn't
	    // there doesn't matter -- but log it 
	    // TODO: track down and fix the problem
//	    Modeller.addToErrorLog(error);
	    Log.warn(error);
	}
	continuation.reconstructSubsequentEvents(event);
    }

    public MicroBehaviourView getMicroBehaviour() {
        return microBehaviour;
    }

}
