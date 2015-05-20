package uk.ac.lkl.client.event;

import java.util.ArrayList;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

@SuppressWarnings("serial")
public class LoadModelEvent extends ModellerEvent {
    
    private boolean replaceOldModel;
    private ArrayList<Boolean> macroBehavioursSelected;
    private int loadCount;
    private int runCount;

    public LoadModelEvent(String modelID, boolean replaceOldModel, int loadCount, int runCount, ArrayList<Boolean> macroBehavioursSelected) {
	super(modelID);
	this.replaceOldModel = replaceOldModel;
	this.macroBehavioursSelected = macroBehavioursSelected;
	this.loadCount = loadCount;
	this.runCount = runCount;
    }

    public LoadModelEvent(String modelID, boolean replaceOldModel, int loadCount, int runCount) {
	this(modelID, replaceOldModel, loadCount, runCount, null);
    }

    public void undo(boolean record, boolean justRecord, final ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	Command loadModelContinuation = new Command() {

	    @Override
	    public void execute() {
		if (continuation != null) {
		    continuation.reconstructSubsequentEvents(LoadModelEvent.this);	
		}
	    }
	    
	};
	if (!justRecord) {
	    Modeller.instance().loadModel(getModelID(), true, replaceOldModel, macroBehavioursSelected, loadModelContinuation, false, true);
	}
    }
    
    public void redo(boolean record, boolean justRecord, final ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	Command loadModelContinuation = new Command() {

	    @Override
	    public void execute() {
		if (continuation != null) {
		    continuation.reconstructSubsequentEvents(LoadModelEvent.this);
		}
	    }
	    
	};
	if (!justRecord) {
	    Modeller.instance().loadModel(getModelID(), false, replaceOldModel, macroBehavioursSelected, loadModelContinuation, false, true);
	}
    }    
    
    public String getXML() {
	return "<LoadModelEvent version='2' modelID='" + getModelID() + "'" +
	       " replaceOldModel='" + (replaceOldModel?"1":"0") + "'" +
               getDateAttribute() + "/>";
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().loadModelEvent(Modeller.userGuid, Modeller.sessionGuid, 
		                                    getModelID(), 
		                                    replaceOldModel,
		                                    macroBehavioursSelected,
		                                    notifyOthers,
		                                    recordSubsequentEventCallback);
    }
    
    public static void reconstruct(
	    Element eventElement, 
	    final boolean restoringHistory, 
	    boolean justRecord, 
	    int version, 
	    final ReconstructEventsContinutation continuation) {
	final String modelID = eventElement.getAttribute("modelID");
	String replaceOldModelString = eventElement.getAttribute("replaceOldModel");
	if (replaceOldModelString == null) {
	    replaceOldModelString = "1"; // replace old by default
	}
	final int loadCount = Utils.getIntAttribute(eventElement, "loads", 0);
	final int runCount = Utils.getIntAttribute(eventElement, "runs", 1);
	final boolean replaceOldModel = !replaceOldModelString.equals("0");
	final ArrayList<Boolean> macroBehavioursSelected = toMacroBehavioursSelected(eventElement.getAttribute("macroBehavioursSelected"));
	Command newContinuation = new Command() {

	    @Override
	    public void execute() {
		if (restoringHistory) {
		    continuation.reconstructSubsequentEvents(new LoadModelEvent(modelID, replaceOldModel, loadCount, runCount, macroBehavioursSelected));
		} else if (continuation != null) {
		    continuation.reconstructSubsequentEvents(null);
		}
	    }
	    
	};
	if (!justRecord) {
	    Modeller.instance().loadModel(modelID, false, replaceOldModel, macroBehavioursSelected, newContinuation, false, true);
	}
    }

    private static ArrayList<Boolean> toMacroBehavioursSelected(String selected) {
	if (selected == null || selected.equals("null")) {
	    return null;
	}
	ArrayList<Boolean> macroBehavioursSelected = new ArrayList<Boolean>();
	for (int i = 0; i < selected.length(); i++) {
	    macroBehavioursSelected.add(selected.charAt(i) == '1');	    
	}
	return macroBehavioursSelected;
    }

    public String toHTMLString(boolean brief) {
	// tried using "loading model" instead of "loaded model" in case an error is encountered and reported
	// but it now reports this much latter and in the history it looks wrong to saying loading
	StringBuffer html = new StringBuffer();
	html.append(Modeller.constants.loadedModel());
	html.append(" ");
	html.append(getModelID());
	if (!brief) {
	    html.append(" (");
	    if (replaceOldModel) {
		html.append(Modeller.constants.replacedCurrentModel());
	    } else {
		html.append(Modeller.constants.addedToCurrentModel());
	    }
	    if (macroBehavioursSelected != null && !macroBehavioursSelected.isEmpty()) {
		int selectionCount = 0;
		for (Boolean selected : macroBehavioursSelected) {
		    if (selected) {
			selectionCount++;
		    }
		}
		if (selectionCount > 0 && selectionCount < macroBehavioursSelected.size()) {
		    html.append(" " + selectionCount + " " + Modeller.constants.of() + " " + macroBehavioursSelected.size() + " " + Modeller.constants.prototypes());
		}
	    }
	    if (loadCount > 0) {
		if (loadCount == 1) {
		    html.append(" " + Modeller.constants.loadedOnce());
		} else {
		    html.append(" " + Modeller.constants.loadedNTimes().replace("***n***", "" + loadCount));
		}
	    }
	    if (runCount > 1) {
		if (loadCount > 0) {
		    html.append(" " + Modeller.constants.and());
		}
		html.append(" " + Modeller.constants.runNTimes().replace("***n***", "" + runCount));
	    }
	    html.append(")");
	}
	return html.toString();
    }
    
    public String getModelID() {
	return (String) getSource();
    }

}
