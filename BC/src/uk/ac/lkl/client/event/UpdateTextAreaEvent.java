package uk.ac.lkl.client.event;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.shared.CommonUtils;

@SuppressWarnings("serial")
public class UpdateTextAreaEvent extends ModellerEvent {
    /**
     * Represents the editing of a text area in the code area of a micro-behaviour page
     */
    protected int indexInCode;
    protected String microBehaviourURL;
    // name is optionally provided in the original HTML for the page to make the history easier to read
    protected String name;
    protected String oldContents;
    protected String tabTitle;
    protected boolean copyOnUpdate;
    
    public UpdateTextAreaEvent(
	    String oldContents, 
	    String newContents, 
	    int indexInCode, 
	    String microBehaviourURL, 
	    String name, 
	    String tabTitle,
	    boolean copyOnUpdate) {
	super(newContents);
	this.indexInCode = indexInCode;
	this.microBehaviourURL = microBehaviourURL;
	this.name = name;
	this.oldContents = oldContents;
	this.tabTitle = tabTitle;
	this.copyOnUpdate = copyOnUpdate;
    }
    
    @Override
    public void undo(boolean record, boolean justRecord, final ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	Command doAfterCommand = new Command() {

	    @Override
	    public void execute() {
		if (continuation != null) {
		    continuation.reconstructSubsequentEvents(UpdateTextAreaEvent.this);
		}
	    }
	    
	};
	if (!justRecord) {
	    Modeller.instance().updateTextArea(microBehaviourURL, oldContents, indexInCode, false, true, doAfterCommand);
	} else {
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(UpdateTextAreaEvent.this);
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
		    continuation.reconstructSubsequentEvents(UpdateTextAreaEvent.this);
		}
	    }
	    
	};
	if (!justRecord) {
	    String newContents = getNewContents();
	    Modeller.instance().updateTextArea(microBehaviourURL, newContents, indexInCode, false, true, doAfterCommand);
	} else {
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(UpdateTextAreaEvent.this);
	    }	    
	}
//	updateTextAreaValueInDataBase(newContents);
    }

    public String toHTMLString(boolean brief) {
	StringBuffer html = new StringBuffer();
	// the new 'enhance' feature makes it hard to keep track of the area number
	html.append(Modeller.constants.editedTextArea());
	if (!brief && tabTitle != null && !tabTitle.isEmpty()) {
	    html.append(Modeller.NON_BREAKING_SPACE);
	    html.append(Modeller.constants.in());
	    html.append(Modeller.NON_BREAKING_SPACE);
	    html.append(tabTitle);
	}
	if (!brief && name != null && !name.isEmpty()) {
	    html.append(Modeller.NON_BREAKING_SPACE);
	    html.append("(");
	    // HTML doesn't seem to like spaces in the name of a textarea
	    // but space looks nicer than _ so replace here
	    html.append(name.replace("_", " "));
//	    html.append(Modeller.NON_BREAKING_SPACE);
//	    html.append(Modeller.constants.in());
//	    html.append(Modeller.NON_BREAKING_SPACE);
//	    html.append(microBehaviourURL);
	    html.append(")");
	}
	html.append(".");
	html.append(Modeller.NON_BREAKING_SPACE);
	html.append(Modeller.constants.newValueIs());
	html.append(Modeller.NON_BREAKING_SPACE);
	html.append(CommonUtils.truncateIfTooLong(getNewContents(), 120));
	html.append(".");
	return html.toString();
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	String newContents = getNewContents();
	Modeller.getHistoryService().updateTextAreaEvent(
		Modeller.userGuid, 
		Modeller.sessionGuid, 
		oldContents, 
		newContents, 
		indexInCode, 
		microBehaviourURL, 
		name, 
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
	final Integer indexInCode = Utils.getIntegerAttribute(eventElement, "index");
	final String microBehaviourURL = Utils.getElementString("url", eventElement);
	final String newContents = Utils.getElementString("newContents", eventElement);
	final String oldContents = Utils.getElementString("oldContents", eventElement);
	final String name = eventElement.getAttribute("name");
	final String tabTitle = Utils.getElementString("tabTitle", eventElement);
	if (indexInCode != null && microBehaviourURL != null && newContents != null) {  
	    Command doAfterCommand = new Command() {

		@Override
		public void execute() {
		    ModellerEvent event = null;
		    if (restoringHistory) {
			event = new UpdateTextAreaEvent(oldContents, newContents, indexInCode, microBehaviourURL, name, tabTitle, true);
		    }
		    continuation.reconstructSubsequentEvents(event);
		}
		
	    };
	    if (justRecord) {
		doAfterCommand.execute();
	    } else if (continuation != null ) {
		Modeller.instance().updateTextArea(microBehaviourURL, newContents, (int) indexInCode, false, copyOnUpdate, doAfterCommand);
	    }
	}
    }
    
    public String getNewContents() {
	return (String) getSource();
    }

}
