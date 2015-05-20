package uk.ac.lkl.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.shared.CommonUtils;

@SuppressWarnings("serial")
public class ReplaceURLEvent extends ModellerEvent {

    protected String oldURL;
    protected String newURL;
    
    /**
     * Constructs an event representing the editing of the resource page of a micro-behaviour
     * @param behaviour
     * @param oldName
     */
    public ReplaceURLEvent(MicroBehaviourView behaviour, String oldURL) {
	super(behaviour);
	this.oldURL = CommonUtils.cannonicaliseURL(oldURL);
	this.newURL = CommonUtils.cannonicaliseURL(behaviour.getUrl());
    }
    
    public String toHTMLString(boolean brief) {
	// this is an event that is 'transparent' to the user -- so shouldn't show up in the history
	return null;
    }
    
    public static void reconstruct(
	    Element eventElement,
	    boolean restoringHistory, 
	    boolean justRecord,
	    int version, 
	    final ReconstructEventsContinutation continuation) {
	String newURL = Utils.getElementString("newURL", eventElement);
	String oldURL = Utils.getElementString("oldURL", eventElement);
	ModellerEvent event = null;
	if (newURL != null && oldURL != null) {
	    MicroBehaviourView microBehaviour = Modeller.instance().getMicroBehaviourView(oldURL);
	    if (microBehaviour != null) {
		if (!justRecord) {
		    microBehaviour.setUrl(newURL);
		    microBehaviour.setCopyMicroBehaviourWhenExportingURL(false);
		}
		if (restoringHistory) {
		    event = new ReplaceURLEvent(microBehaviour, newURL);
		}
		// may have been edited before adding to a prototype so ok that it is now found
//	    } else {
//		Modeller.addToErrorLog("Could not find the micro-behaviour from " + oldURL);
	    }
	} else {
	    Modeller.addToErrorLog("Missing oldURL or newURL XML attributes in ReplaceURLEvent.");
	}
	continuation.reconstructSubsequentEvents(event);
    }
    
    @Override
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().replaceURLEvent(
		Modeller.userGuid, 
		Modeller.sessionGuid, 
		oldURL, 
		newURL,
		notifyOthers,
		recordSubsequentEventCallback);	
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
    
    @Override
    public boolean eventDirtiesModel() {
	// an internal event -- not one that 'dirties' a model
	return false;
    }

}
