package uk.ac.lkl.client.event;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;

@SuppressWarnings("serial")
public class EnhanceMicroBehaviourEvent extends ModellerEvent {
    /**
     * Represents the enhancement of a micro-behaviour page
     */

    protected String microBehaviourURL;
    protected String tabTitle;
    
    public EnhanceMicroBehaviourEvent(
	    MicroBehaviourEnhancement enhancement,
	    String microBehaviourURL, 
	    String tabTitle) {
	super(enhancement);
	this.microBehaviourURL = microBehaviourURL;
	this.tabTitle = tabTitle;
    }
    
    @Override
    public void undo(boolean record, boolean justRecord, final ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	Command doAfterCommand = new Command() {

	    @Override
	    public void execute() {
		if (continuation != null) {
		    continuation.reconstructSubsequentEvents(EnhanceMicroBehaviourEvent.this);
		}
	    }
	    
	};
	if (!justRecord) {
	    Modeller.removeMicroBehaviourEnhancement(microBehaviourURL, doAfterCommand);
	} else {
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(EnhanceMicroBehaviourEvent.this);
	    }
	}
//	updateTextAreaValueInDataBase(oldContents);
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, final ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	Command doAfterCommand = new Command() {

	    @Override
	    public void execute() {
		if (continuation != null) {
		    continuation.reconstructSubsequentEvents(EnhanceMicroBehaviourEvent.this);
		}
	    }
	    
	};
	if (!justRecord) {
	    Modeller.enhanceMicroBehaviour(getEnhancement(), microBehaviourURL, doAfterCommand);
	} else {
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(EnhanceMicroBehaviourEvent.this);
	    }	    
	}
    }

    public String toHTMLString(boolean brief) {
	String description = MicroBehaviourEnhancement.getAdditionalDescription(getEnhancement());
	return description.replace("***", tabTitle == null ? "" : tabTitle);
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().enhanceMicroBehaviour(
		Modeller.userGuid, 
		Modeller.sessionGuid, 
		getEnhancement(), 
		microBehaviourURL, 
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
	final MicroBehaviourEnhancement enhancement = MicroBehaviourEnhancement.getEnhancement(enhancementIndex);
	final String microBehaviourURL = Utils.getElementString("url", eventElement);
	final String tabTitle = Utils.getElementString("tabTitle", eventElement);
	if (enhancement != null && microBehaviourURL != null) {  
	    Command doAfterCommand = new Command() {

		@Override
		public void execute() {
		    ModellerEvent event = null;
		    if (restoringHistory) {
			event = new EnhanceMicroBehaviourEvent(enhancement, microBehaviourURL, tabTitle);
		    }
		    continuation.reconstructSubsequentEvents(event);
		}
		
	    };
	    if (justRecord) {
		doAfterCommand.execute();
	    } else if (continuation != null ) {
		Modeller.enhanceMicroBehaviour(enhancement, microBehaviourURL, doAfterCommand);
	    }
	}
    }
    
    public MicroBehaviourEnhancement getEnhancement() {
	return (MicroBehaviourEnhancement) getSource();
    }

}
