/**
 * 
 */
package uk.ac.lkl.client.event;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.client.composer.MacroBehaviourView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

/**
 * Represents the updating of the text area for the number of instances of a kind 
 * 
 * @author Ken Kahn
 *
 */
public class UpdateNumberOfInstancesTextAreaEvent extends ModellerEventOfMacroBehaviour {

    private static final long serialVersionUID = -4535059190865359261L;
    protected String oldValue;
    protected String newValue;

    public UpdateNumberOfInstancesTextAreaEvent(String oldValue, String newValue, MacroBehaviourView macroBehaviour) {
	super(macroBehaviour);
	this.oldValue = oldValue;
	this.newValue = newValue;
	setMacroBehaviour(macroBehaviour);
    }

    @Override
    public String toHTMLString(boolean brief) {
	return Modeller.constants.editedNumberOfInstances().
	              replace("***prototype name***", macroBehaviourNameAtEventTime).
	              replace("***new value***", newValue == null ? "***value missing***" : newValue);
    }
    
    @Override
    public void undo(boolean record, boolean justRecord, final ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setInstanceCountExpressionText(oldValue);
	} else {
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(UpdateNumberOfInstancesTextAreaEvent.this);
	    }
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, final ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setInstanceCountExpressionText(newValue);
	} else {
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(UpdateNumberOfInstancesTextAreaEvent.this);
	    }
	}
    }
    
    @Override
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().updateNumberOfInstancesTextAreaEvent(
		Modeller.userGuid, 
		Modeller.sessionGuid, 
		oldValue, 
		newValue, 
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
	    String oldValue = Utils.getElementString("oldExpression", eventElement);
	    if (oldValue == null) {
		// older convention
		oldValue = Utils.getElementString("oldValue", eventElement);
	    }
	    String newValue = Utils.getElementString("newExpression", eventElement);
	    if (newValue == null) {
		newValue = Utils.getElementString("newValue", eventElement);
	    }
	    if (!justRecord) {
		macroBehaviour.setInstanceCountExpressionText(newValue);
	    }
	    if (restoringHistory) {
		event = new UpdateNumberOfInstancesTextAreaEvent(oldValue, newValue, macroBehaviour);
	    }
	} else {
	    Modeller.addToErrorLog("Could not find a macro behaviour " + macroBehaviourName + 
		                   " in UpdateNumberOfInstancesTextAreaEvent. Known macro-behaviours are: " +
		                   Modeller.instance().getMacroBehaviourNames());
	}
	continuation.reconstructSubsequentEvents(event);
    }

}
