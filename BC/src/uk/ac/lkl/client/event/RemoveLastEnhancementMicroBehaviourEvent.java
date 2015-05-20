/**
 * 
 */
package uk.ac.lkl.client.event;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.client.composer.RemovedEnhancement;

/**
 * Represents the action of removing the last micro-behaviour scheduling enhancement
 * 
 * @author Ken Kahn
 *
 */
public class RemoveLastEnhancementMicroBehaviourEvent extends ModellerEvent {

    private static final long serialVersionUID = -6298143540623278550L;
    private String url;
    private String tabTitle;

    public RemoveLastEnhancementMicroBehaviourEvent(RemovedEnhancement enhancementRemoved, String url, String tabTitle) {
	super(enhancementRemoved);
	this.url = url;
	this.tabTitle = tabTitle;
    }
    
    @Override
    public void undo(boolean record, boolean justRecord, final ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	Command doAfterCommand = new Command() {

	    @Override
	    public void execute() {
		Modeller.instance().updateTextArea(url, getTextAreaContents(), getTextAreaIndex(), false, true, null);
		if (continuation != null) {
		    continuation.reconstructSubsequentEvents(RemoveLastEnhancementMicroBehaviourEvent.this);
		}
	    }
	    
	};
	if (!justRecord) {
	    Modeller.enhanceMicroBehaviour(getEnhancement(), url, doAfterCommand);
	} else {
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(RemoveLastEnhancementMicroBehaviourEvent.this);
	    }	    
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, final ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	Command doAfterCommand = new Command() {

	    @Override
	    public void execute() {
		if (continuation != null) {
		    continuation.reconstructSubsequentEvents(RemoveLastEnhancementMicroBehaviourEvent.this);
		}
	    }
	    
	};
	if (!justRecord) {
	    Modeller.removeMicroBehaviourEnhancement(url, doAfterCommand);
	} else {
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(RemoveLastEnhancementMicroBehaviourEvent.this);
	    }
	}
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().removeLastEnhancementMicroBehaviour(
		Modeller.userGuid, 
		Modeller.sessionGuid, 
		getEnhancement(),
		getTextAreaContents(), 
		getTextAreaIndex(),
		url, 
		tabTitle,
		notifyOthers,
		recordSubsequentEventCallback);
    }
        
    public static void reconstruct(String macroBehaviourName, 
	                           Element eventElement, 
	                           final boolean restoringHistory,
	                           final boolean copyOnUpdate,
	                           boolean justRecord, 
	                           int version, 
	                           final ReconstructEventsContinutation continuation) {
	Integer enhancementIndex = Utils.getIntegerAttribute(eventElement, "enhancement");
	final Integer textAreaIndex = Utils.getIntegerAttribute(eventElement, "textAreaIndex");
	final MicroBehaviourEnhancement enhancement = MicroBehaviourEnhancement.getEnhancement(enhancementIndex);
	final String microBehaviourURL = Utils.getElementString("url", eventElement);
	final String tabTitle = Utils.getElementString("tabTitle", eventElement);
	final String textAreaContents = Utils.getElementString("textAreaContents", eventElement);
	if (enhancement != null && microBehaviourURL != null) { 
	    final RemovedEnhancement removedEnhancement = new RemovedEnhancement(enhancement, textAreaContents, textAreaIndex);
	    Command doAfterCommand = new Command() {

		@Override
		public void execute() {
		    ModellerEvent event = null;
		    if (restoringHistory) {
			event = new RemoveLastEnhancementMicroBehaviourEvent(removedEnhancement, microBehaviourURL, tabTitle);
		    }
		    continuation.reconstructSubsequentEvents(event);
		}
		
	    };
	    if (justRecord) {
		doAfterCommand.execute();
	    } else if (continuation != null ){
		Modeller.removeMicroBehaviourEnhancement(microBehaviourURL, doAfterCommand);
	    }
	}
    }

    @Override
    public String toHTMLString(boolean brief) {
	String description = MicroBehaviourEnhancement.getRemovalDescription(getEnhancement());
	return description.replace("***", tabTitle == null ? "" : tabTitle);
    }

    private MicroBehaviourEnhancement getEnhancement() {
	return getRemovedEnhancement().getEnhancementRemoved();
    }
    
    private String getTextAreaContents() {
	return getRemovedEnhancement().getTextAreaValueRemoved();
    }
    
    private int getTextAreaIndex() {
	return getRemovedEnhancement().getTextAreaIndex();
    }
    
    public RemovedEnhancement getRemovedEnhancement() {
	return (RemovedEnhancement) getSource();
    }

}
