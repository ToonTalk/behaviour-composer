/**
 * 
 */
package uk.ac.lkl.client.event;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.composer.MacroBehaviourView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

/**
 * Represents the ticking (or unticking) of the hide/show check box of a prototype
 * 
 * @author Ken Kahn
 *
 */
public class VisibleInModelEvent extends ModellerEventOfMacroBehaviour {
    
    private static final long serialVersionUID = -5238609164508736546L;
    boolean visible;

    public VisibleInModelEvent(boolean visible, MacroBehaviourView macroBehaviourView) {
	super(macroBehaviourView);
	this.visible = visible;
    }

    @Override
    public String toHTMLString(boolean brief) {
	String template = visible ? Modeller.constants.visibleCheckBoxEvent() : Modeller.constants.invisibleCheckBoxEvent();
	return template.replace("***prototype name***", macroBehaviourNameAtEventTime);
    }
    
    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setVisibleInModel(!visible);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setVisibleInModel(visible);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }

    @Override
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().visibleInModelEvent(
		visible,
		macroBehaviourNameAtEventTime,
		Modeller.userGuid, 
		Modeller.sessionGuid,		
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
	    String visibleInModelAttribute = eventElement.getAttribute("visibleInModel");
	    boolean visibleInModel = visibleInModelAttribute.equals("1");
	    if (!justRecord) {
		macroBehaviour.setVisibleInModel(visibleInModel);
	    }
	    if (restoringHistory) {
		event = new VisibleInModelEvent(visibleInModel, macroBehaviour);
	    }
	} else {
	    Modeller.addToErrorLog("Could not find a macro behaviour " + macroBehaviourName + 
		                   " in VisibleInModelEvent. Known macro-behaviours are: " +
		                   Modeller.instance().getMacroBehaviourNames());
	}
	continuation.reconstructSubsequentEvents(event);
    }

}
