package uk.ac.lkl.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;

import uk.ac.lkl.client.composer.MacroBehaviourView;

@SuppressWarnings("serial")
public class SwapPrototypesEvent extends ModellerEvent {
    
    private int index1;
    private int index2;

    public SwapPrototypesEvent(int index1, int index2) {
	super(Modeller.instance().getAllPrototypes());
	this.index1 = index1;
	this.index2 = index2;
    }

    public String toHTMLString(boolean brief) {
	MacroBehaviourView prototype1 = Modeller.instance().getMacroBehaviourWithIndex(index1);
	if (index2 >= Modeller.instance().getAllPrototypes().size()) {
	    String prototypeMovedToLeftEdge = Modeller.constants.prototypeMovedToLeftEdge();
	    return prototypeMovedToLeftEdge.replace("***prototype***", prototype1.getNameHTML());
	} else if (index2 < 0) {
	    String prototypeMovedToRightEdge = Modeller.constants.prototypeMovedToRightEdge();
	    return prototypeMovedToRightEdge.replace("***prototype***", prototype1.getNameHTML());
	} else {
	    MacroBehaviourView prototype2 = Modeller.instance().getMacroBehaviourWithIndex(index2);
	    if (prototype1 == null || prototype2 == null) {
		String swappedPrototypeIndices = Modeller.constants.swappedPrototypeIndices();
		swappedPrototypeIndices = swappedPrototypeIndices.replace("***index1***", Integer.toString(index1+1));
		swappedPrototypeIndices = swappedPrototypeIndices.replace("***index2***", Integer.toString(index2+1));
		return swappedPrototypeIndices;
	    }
	    String swappedPrototypes = Modeller.constants.swappedPrototypes();
	    swappedPrototypes = swappedPrototypes.replace("***prototype1***", prototype1.getNameHTML());
	    swappedPrototypes = swappedPrototypes.replace("***prototype2***", prototype2.getNameHTML());
	    return swappedPrototypes;
	}
    }
    
    public String getXML() {
	return "<SwapPrototypesEvent version='1' index1='" + index1 + "' index2='" + index2 + "'" + 
	       getDateAttribute() + "/>";
    }
      
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().swapPrototypesEvent(Modeller.userGuid, Modeller.sessionGuid, 
		                                         index1, index2, 
		                                         notifyOthers, 
		                                         recordSubsequentEventCallback);
    }
    
    public static void reconstruct(Element eventElement, 
	                           boolean restoringHistory,
	                           boolean justRecord, 
	                           int version, 
	                           ReconstructEventsContinutation continuation) {
	Integer index1 = Utils.getIntegerAttribute(eventElement, "index1");
	Integer index2 = Utils.getIntegerAttribute(eventElement, "index2");
	ModellerEvent event = null;
	if (index1 != null && index2 != null) {
	    if (!justRecord) {
		if (!Modeller.instance().swapPrototypes(index1, index2)) {
		    // not serious enough for Modeller.addToErrorLog
		    System.out.println("Could not swap prototype number " + (index1+1)  
			               + " with prototype number " + (index2+1)
			               + " in SwapPrototypesEvent.");
		}
	    }
	    if (restoringHistory) {
		event = new SwapPrototypesEvent(index1, index2);
	    }
	}
	continuation.reconstructSubsequentEvents(event);
    }

    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    Modeller.instance().swapPrototypes(index2, index1);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    Modeller.instance().swapPrototypes(index1, index2);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }

    public MacroBehaviourView getMacroBehaviour() {
	return (MacroBehaviourView) getSource();
    }

}
