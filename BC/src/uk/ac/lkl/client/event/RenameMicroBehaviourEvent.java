package uk.ac.lkl.client.event;

import uk.ac.lkl.client.BrowsePanel;
import uk.ac.lkl.client.BrowsePanelCommand;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;

import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

@SuppressWarnings("serial")
public class RenameMicroBehaviourEvent extends ModellerEvent {
    /**
     * Represents events when a micro-behaviour was edited
     * and the HTML "name" of the micro-behaviour was changed.
     */
    protected String oldName;
    protected String nameAtEventTime;
    protected String url;
    
    public RenameMicroBehaviourEvent(MicroBehaviourView microBehaviour, String oldName) {
	super(microBehaviour);
	this.oldName = oldName; // CommonUtils.removePTags(oldName); // After version 444 this shouldn't be necessary to remove P tags
	this.nameAtEventTime = microBehaviour.getNameHTMLAndDescription();
	this.url = getMicroBehaviour().getAllURLs();
    }

    @Override
    public String toHTMLString(boolean brief) {
	String oldNameHTML = CommonUtils.getNameHTML(oldName);
	String newNameHTML = CommonUtils.getNameHTML(nameAtEventTime);
	if (oldNameHTML.equals(newNameHTML)) {
	    return Modeller.constants.descriptionEdited() + " \"" + CommonUtils.getDescription(oldName) + "\" " +
		   Modeller.constants.to() + " \"" + CommonUtils.getDescription(nameAtEventTime);
	} else {
	    return Modeller.constants.renamed() + " " + oldNameHTML + " " + Modeller.constants.to() + " " + newNameHTML;
	}
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().renameMicroBehaviourEvent(
		Modeller.userGuid, 
		Modeller.sessionGuid, 
		url,
                oldName,
                nameAtEventTime, 
                notifyOthers,
                recordSubsequentEventCallback);
    }
    
//    @Deprecated
//    public static void renameInDatabase(
//	    String url, String newName, ReconstructEventsContinutation continuation) {
//	String guid = CommonUtils.changesGuid(url);
//	if (guid != null) {
//	    renameMicroBehaviourInDatabase(newName, guid, continuation);
//	}
//    }

    @Deprecated
    public static void renameMicroBehaviourInDatabase(
	    String newName, String guid, final ReconstructEventsContinutation continuation) {
	Modeller.getResourcePageService().addTextAreaValueToCopy(-1, newName, guid, Modeller.sessionGuid,
	    new AsyncCallback<String>() {

	    public void onFailure(Throwable caught) {
		Modeller.reportException(caught, "In renameMicroBehaviourInDatabase.");
	    }

	    public void onSuccess(String result) {
		if (result != null && CommonUtils.isErrorResponse(result)) {
		    Modeller.addToErrorLog(result);
		    Modeller.setAlertsLine(Modeller.constants.encounteredAnError());
		    return;
		}
		if (continuation != null) {
		    continuation.reconstructSubsequentEvents(null);
		}
	    }
	
	});
    }
    
    public static void reconstruct(
	    Element eventElement, 
	    final boolean restoringHistory, 
	    final boolean copyOnUpdate,
	    final boolean justRecord,
	    int version, 
	    final ReconstructEventsContinutation continuation) {
	final String newName;
	if (version == 1) {
	    newName = Utils.decode(eventElement.getAttribute("newName"));
	} else if (version == 2) {
	    newName = CommonUtils.decode(eventElement.getAttribute("newName"));
	} else {
	    newName = Utils.getElementString("newName", eventElement);
	}
	final String urlString = Utils.getElementString("url", eventElement);
	final String oldName = Utils.getElementString("name", eventElement);
	BrowsePanelCommand command = new BrowsePanelCommand() {

	    @Override
	    public void execute(BrowsePanel browsePanel, String[] answer, boolean PanelIsNew) {
		MicroBehaviourView microBehaviour = browsePanel.getMicroBehaviour();
		ModellerEvent event = null;
		if (microBehaviour == null) {
		    Modeller.addToErrorLog("Could not find a micro behaviour " + urlString + 
			                   " in RenameMicroBehaviourEvent from " + oldName + " to " + newName);
		} else {
		    if (!justRecord) {
			microBehaviour.setNameHTMLAndDescription(newName);
		    }
		    if (restoringHistory) {
			event = new RenameMicroBehaviourEvent(microBehaviour, oldName);
		    }
		}
		continuation.reconstructSubsequentEvents(event);
	    }
	    
	};
//	Log.info("RenameMicroBehaviourEvent url: " + urlString); // for debugging
	Modeller.executeOnMicroBehaviourPage(urlString, command, true, copyOnUpdate);
    }
    
    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMicroBehaviour().setNameHTMLAndDescription(oldName);
//	    renameInDatabase(url, oldName, continuation);
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(null);
	    }
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMicroBehaviour().setNameHTMLAndDescription(nameAtEventTime);
	    // TODO: is the following still a good idea?
//	    renameInDatabase(url, nameAtEventTime, continuation);
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(null);
	    }
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
    
    // for old sessions only:
    protected static void reconstructInMacroBehaviour(MacroBehaviourView macroBehaviour,
	    final String macroBehaviourName, 
	    Element eventElement,
	    String url,
	    boolean restoringHistory, 
	    boolean justRecord,
	    int version, 
	    final ReconstructEventsContinutation continuation) {
	MicroBehaviourView microBehaviour = macroBehaviour.getMicroBehaviourWithURL(url, true, false);
	ModellerEvent event = null;
	if (microBehaviour != null) {
	    String oldName = Utils.getElementString("oldMicroName", eventElement);
	    String newName = Utils.getElementString("newMicroName", eventElement);
	    if (!justRecord) {
		microBehaviour.setNameHTMLAndDescription(newName);
	    }
	    if (restoringHistory) {
		event = new RenameMicroBehaviourEvent(microBehaviour, oldName);
	    }
	} else {
	    Modeller.addToErrorLog("Could not find a micro-behaviour of " + url + 
		    " in " + macroBehaviourName +
		    " while reconstructing RenameMicroBehaviourEvent of " + url +
		    ". Known micro-behaviours are: " +
		    macroBehaviour.getMicroBehaviourNames());
	}
	continuation.reconstructSubsequentEvents(event);
    }

}
