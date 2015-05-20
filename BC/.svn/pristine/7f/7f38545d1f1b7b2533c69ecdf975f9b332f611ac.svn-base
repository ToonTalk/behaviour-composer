package uk.ac.lkl.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.shared.CommonUtils;

@SuppressWarnings("serial")
public class EditMicroBehaviourEvent extends ModellerEvent {

    protected String oldURL;
    protected String newURL;
    protected String microBehaviourNameAtEventTime = null;
    protected String macroBehaviourNameAtEventTime = null;
    
    /**
     * Constructs an event representing the editing of the resource page of a micro-behaviour
     * @param behaviour
     * @param oldName
     */
    public EditMicroBehaviourEvent(MicroBehaviourView behaviour, String oldURL) {
	super(behaviour);
	this.oldURL = CommonUtils.cannonicaliseURL(oldURL);
	this.newURL = CommonUtils.cannonicaliseURL(behaviour.getUrl());
	this.microBehaviourNameAtEventTime = behaviour.getHTML();
	MacroBehaviourView macroBehaviour = getMacroBehaviour();
	if (macroBehaviour != null) {
	    macroBehaviourNameAtEventTime = macroBehaviour.getHTML();
	} else {
	    System.out.println("No macro-behaviour found in EditMicroBehaviourEvent constructor.");
	}
    }
    
    public String toHTMLString(boolean brief) {
	return Modeller.constants.changedURLOf() + " " + getMicroBehaviour().getHTML() + 
	       " " + Modeller.constants.in() + " " + macroBehaviourNameAtEventTime +
	       " " + Modeller.constants.to() + " " + newURL;
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	// obsolete
    }
    
    protected static void reconstructInMacroBehaviour(MacroBehaviourView macroBehaviour,
                                                      final String macroBehaviourName, 
                                                      Element eventElement,
                                                      String url,
                                                      boolean restoringHistory, 
                                                      boolean justRecord,
                                                      int version, 
                                                      final ReconstructEventsContinutation continuation) {
	String newURL = eventElement.getAttribute("newURL");
	String oldURL = eventElement.getAttribute("oldURL");
	ModellerEvent event = null;
	if (newURL != null && oldURL != null) {
	    MicroBehaviourView microBehaviour = macroBehaviour.getMicroBehaviourWithURL(oldURL, true, false);
	    if (microBehaviour != null) {
		if (!justRecord) {
		    microBehaviour.setUrl(newURL);
		}
		if (restoringHistory) {
		    event = new EditMicroBehaviourEvent(microBehaviour, oldURL);
		}
	    } else {
		Modeller.addToErrorLog("Could not find the micro-behaviour from " + oldURL +
			               ". Micro-behaviour URLs are: " + macroBehaviour.getMicroBehaviourURLs());
	    }
	} else {
	    Modeller.addToErrorLog("Missing oldURL or newURL XML attributes in EditMicroBehaviourEvent.");
	}
	continuation.reconstructSubsequentEvents(event);
    }

    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMicroBehaviour().setUrl(oldURL);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMicroBehaviour().setUrl(newURL);
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
