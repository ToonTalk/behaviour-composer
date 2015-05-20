package uk.ac.lkl.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.Modeller;

import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.shared.CommonUtils;

@SuppressWarnings("serial")
public class AddMacroBehaviourEvent extends ModellerEvent {

    public AddMacroBehaviourEvent(MacroBehaviourView object) {
	super(object);
    }

    public String toHTMLString(boolean brief) {
	return Modeller.constants.added() + Modeller.NON_BREAKING_SPACE + getMacroBehaviour().getHTML();
    }
    
    public String getXML() {
	return "<AddMacroBehaviourEvent version='2' name='" + CommonUtils.encode(getMacroBehaviour().getHTML()) + "'" +
	       getDateAttribute() + "/>";
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
//	Window.alert("recordInDatabase " + getMacroBehaviour().getHTML());
	Modeller.getHistoryService().addMacroBehaviourEvent(Modeller.userGuid, 
		                                            Modeller.sessionGuid, 
		                                            getMacroBehaviour().getHTML(), 
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
	MacroBehaviourView newMacroBehaviour = Modeller.instance().createMacroBehaviour(macroBehaviourName);
	if (!justRecord) {
	    Modeller.instance().addMacroBehaviour(newMacroBehaviour);
	}
	if (restoringHistory) {
	    continuation.reconstructSubsequentEvents(new AddMacroBehaviourEvent(newMacroBehaviour));
	} else if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }

    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    Modeller.instance().removeMacroBehaviour(getMacroBehaviour());
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    Modeller.instance().addMacroBehaviour(getMacroBehaviour());
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }

    public MacroBehaviourView getMacroBehaviour() {
	return (MacroBehaviourView) getSource();
    }

}
