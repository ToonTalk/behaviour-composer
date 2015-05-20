package uk.ac.lkl.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.Modeller;

import uk.ac.lkl.client.composer.MacroBehaviourView;

/**
 * Captures events that change whether a macro-behaviour is added to the model
 * and if so whether it should be visible.
 * 
 * @author Ken Kahn
 *
 */
@SuppressWarnings("serial")
public class AddToModelListBoxMacroBehaviourEvent extends ModellerEvent {

    protected String macroBehaviourNameAtEventTime = null;
    
    protected boolean addToModelBeforeEvent;
    protected boolean visibleInModelBeforeEvent;
    protected boolean addToModelAtEventTime;
    protected boolean visibleInModelAtEventTime;
    
    protected String eventDescription;
    
    public AddToModelListBoxMacroBehaviourEvent(
	    MacroBehaviourView macroBehaviour, 
	    boolean addToModelBeforeEventTime,
	    boolean visibleInModelBeforeEventTime) {
	super(macroBehaviour);
	macroBehaviourNameAtEventTime = macroBehaviour.getHTML();
	this.addToModelBeforeEvent = addToModelBeforeEventTime;
	this.visibleInModelBeforeEvent = visibleInModelBeforeEventTime;
	addToModelAtEventTime = macroBehaviour.isAddToModel();
	visibleInModelAtEventTime = macroBehaviour.isVisibleInModel();
	switch (macroBehaviour.getAddToModelListBoxIndex(addToModelAtEventTime, visibleInModelAtEventTime)) {
	case 0:
	    eventDescription = Modeller.constants.selectedOptionAddToModelVisible();
	    break;
	case 1:
	    eventDescription = Modeller.constants.selectedOptionAddToModelNotVisible();
	    break;
	default:
	    eventDescription = Modeller.constants.selectedOptionOnlyAListOfMicroBehaviours();
	}
    }

    public String toHTMLString(boolean brief) {
	return eventDescription.replace("***prototype name***", macroBehaviourNameAtEventTime);
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().addToModelListBoxMacroBehaviourEvent(
		addToModelAtEventTime,
		addToModelBeforeEvent,
		visibleInModelAtEventTime,
		visibleInModelBeforeEvent,
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
	    String addToModelAttribute = eventElement.getAttribute("addToModel");
	    boolean addToModel = addToModelAttribute.equals("1");   
	    String visibleInModelAttribute = eventElement.getAttribute("visibleInModel");
	    boolean visibleInModel = visibleInModelAttribute.equals("1");
	    if (!justRecord) {
		macroBehaviour.setAddToModel(addToModel);
		macroBehaviour.setVisibleInModel(visibleInModel);
	    }
	    String addToModelBeforeEventAttribute = eventElement.getAttribute("addToModelBeforeEvent");
	    boolean addToModelBeforeEvent = addToModelBeforeEventAttribute.equals("1");
	    String visibleInModelBeforeEventAttribute = eventElement.getAttribute("visibleInModelBeforeEvent");
	    boolean visibleInModelBeforeEvent = visibleInModelBeforeEventAttribute.equals("1");
	    if (restoringHistory) {
		event = 
		    new AddToModelListBoxMacroBehaviourEvent(
			    macroBehaviour, 
			    addToModelBeforeEvent, 
			    visibleInModelBeforeEvent);
	    }
	} else {
	    Modeller.addToErrorLog("Could not find a macro behaviour " + macroBehaviourName + 
		                   " in addToModelListBoxMacroBehaviourEvent. Known macro-behaviours are: " +
		                   Modeller.instance().getMacroBehaviourNames());
	}
	continuation.reconstructSubsequentEvents(event);
    }

    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setAddToModel(isAddToModelBeforeEvent());
	    getMacroBehaviour().setVisibleInModel(isVisibleInModelBeforeEvent());
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setAddToModel(isAddToModelAtEventTime());
	    getMacroBehaviour().setVisibleInModel(isVisibleInModelAtEventTime());
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }

    public MacroBehaviourView getMacroBehaviour() {
	return (MacroBehaviourView) getSource();
    }

    public boolean isVisibleInModelAtEventTime() {
        return visibleInModelAtEventTime;
    }

    public boolean isAddToModelAtEventTime() {
        return addToModelAtEventTime;
    }

    public boolean isAddToModelBeforeEvent() {
        return addToModelBeforeEvent;
    }

    public boolean isVisibleInModelBeforeEvent() {
        return visibleInModelBeforeEvent;
    }

}
