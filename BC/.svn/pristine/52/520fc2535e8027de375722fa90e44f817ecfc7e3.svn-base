/**
 * 
 */
package uk.ac.lkl.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.BehaviourComposer;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.SessionEventsCheckBox;

/**
 * Represents events of clicking on a check box that runs a session.
 * Currently used only in the Epidemic Game Maker.
 * 
 * 
 * @author Ken Kahn
 *
 */

public class SessionEventsCheckBoxToggledEvent extends ModellerEvent {
    
    private static final long serialVersionUID = -779309898660821019L;

    protected SessionEventsCheckBox sessionEventsCheckBox;
    
    protected boolean valueAtEventTime;

    public SessionEventsCheckBoxToggledEvent(boolean valueAtEventTime, SessionEventsCheckBox sessionEventsCheckBox) {
	super(sessionEventsCheckBox);
	this.sessionEventsCheckBox = sessionEventsCheckBox;
	this.valueAtEventTime = valueAtEventTime;
    }

    @Override
    public void recordInDatabase(
	    AsyncCallback<String[]> recordSubsequentEventCallback,
	    boolean notifyOthers) {
	Modeller.getHistoryService().addSessionEventsCheckBoxToggledEvent(
		valueAtEventTime,
		sessionEventsCheckBox.getGuid(),
		sessionEventsCheckBox.getNameId(),
		Modeller.userGuid, 
		Modeller.sessionGuid,
		notifyOthers,
		recordSubsequentEventCallback);
    }
     
    public static void reconstruct(
	    Element eventElement, 
	    boolean restoringHistory, 
	    boolean justRecord, 
	    int version, 
	    ReconstructEventsContinutation continuation) {
	String valueAtEventTimeAttribute = eventElement.getAttribute("valueAtEventTime");
	boolean valueAtEventTime = valueAtEventTimeAttribute.equals("1");
	String checkBoxSessionGuid = eventElement.getAttribute("checkBoxSessionGuid");
	String nameId = eventElement.getAttribute("nameId");
	ModellerEvent event = null;
	if (restoringHistory) {
	    SessionEventsCheckBox checkBox = Modeller.instance().getCheckBoxWithSessionGuid(checkBoxSessionGuid, nameId);
	    if (checkBox == null) {
		if (BehaviourComposer.epidemicGameMakerMode()) { // only thing using check boxes now
		    Modeller.addToErrorLog("Could not find a session check box for the session with the id: " + checkBoxSessionGuid);
		}
	    } else {
		event = new SessionEventsCheckBoxToggledEvent(valueAtEventTime, checkBox);
		if (!justRecord) {
		    // ensure that subsequent events are run until this has finished
		    checkBox.setContinuation(continuation);
		    continuation = null;
		    checkBox.setValue(valueAtEventTime, true); // fire events
		}
	    } 
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(event);
	}
    }

    @Override
    public String toHTMLString(boolean brief) {
	String labelInParentheses = " (" + sessionEventsCheckBox.getText() + ")";
	if (valueAtEventTime) {
	    return Modeller.constants.checkBoxTicked() + labelInParentheses;
	} else {
	    return Modeller.constants.checkBoxUnticked() + labelInParentheses;
	}
    }
	
    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    sessionEventsCheckBox.setValue(!valueAtEventTime);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    sessionEventsCheckBox.setValue(valueAtEventTime);
	}
    }

}
