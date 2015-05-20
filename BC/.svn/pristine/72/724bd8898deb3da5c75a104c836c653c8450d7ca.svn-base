package uk.ac.lkl.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.Utils;
import uk.ac.lkl.client.Modeller;

import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.shared.CommonUtils;

@SuppressWarnings("serial")
public class RenameMacroBehaviourEvent extends ModellerEvent {

    protected String newName = null;
    protected String oldName = null;
    
    public RenameMacroBehaviourEvent(MacroBehaviourView object, String oldName) {
	super(object);
	this.oldName = oldName;
	this.newName = getMacroBehaviour().getNameHTML();
    }

    @Override
    public String toHTMLString(boolean brief) {
	return Modeller.constants.renamed() + Modeller.NON_BREAKING_SPACE + oldName + Modeller.NON_BREAKING_SPACE + 
	       Modeller.constants.to() + Modeller.NON_BREAKING_SPACE + newName;
    }
    
    public String getXML() {
	return "<RenameMacroBehaviourEvent version='2' newName='" + CommonUtils.encode(newName) +
	       "' name='" + CommonUtils.encode(oldName) + "'" +
	       getDateAttribute() + "/>";
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().renameMacroBehaviourEvent(Modeller.userGuid, Modeller.sessionGuid, 
		                                               oldName,
		                                               newName, 
		                                               notifyOthers,
		                                               recordSubsequentEventCallback);
    }
    
    public static void reconstruct(
	    String macroBehaviourName, Element eventElement, 
	    boolean restoringHistory, 
	    boolean justRecord, 
	    int version, 
	    ReconstructEventsContinutation continuation) {
	MacroBehaviourView macroBehaviour = Modeller.instance().getMacroBehaviourWithHTMLName(macroBehaviourName);
	String newName;
	if (version == 1) {
	    newName = Utils.decode(eventElement.getAttribute("newName"));
	} else if (version == 2) {
	    newName = CommonUtils.decode(eventElement.getAttribute("newName"));
	} else {
	    newName = Utils.getElementString("newName", eventElement);
	}
	ModellerEvent event = null;
	if (macroBehaviour != null) {
	    if (!justRecord) {
		macroBehaviour.setHTML(newName);
	    }   
	    if (restoringHistory) {
		event = new RenameMacroBehaviourEvent(macroBehaviour, macroBehaviourName);
	    }
	} else {
	    Modeller.addToErrorLog("Could not find a macro behaviour " + macroBehaviourName + 
		                   " in RenameMacroBehaviourEvent to " + newName + ". " +
		                   "Known macro-behaviours are: " +
		                   Modeller.instance().getMacroBehaviourNames());;
	}
	continuation.reconstructSubsequentEvents(event);
    }

    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setHTML(oldName);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().setHTML(newName);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }

    public MacroBehaviourView getMacroBehaviour() {
	return (MacroBehaviourView) getSource();
    }

}
