package uk.ac.lkl.client.event;

import java.util.ArrayList;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.BrowsePanel;
import uk.ac.lkl.client.BrowsePanelCommand;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.shared.CommonUtils;

@SuppressWarnings("serial")
public class AddMicroBehaviourEvent extends ModellerEventOfMacroBehaviour {
    /**
     * Represents the addition of a micro-behaviour or a macro-behaviour
     * to a prototype (macro-behaviour).
     */
    protected MicroBehaviourView microBehaviour;
    
    protected int microBehaviourInsertionIndex;
    
    // Used to avoid redundant work
    // see Issue 333
    static private ArrayList<String> urlsFetched = new ArrayList<String>();
    
    public AddMicroBehaviourEvent(MacroBehaviourView macroBehaviour, MicroBehaviourView microBehaviour, int microBehaviourInsertionIndex) {
	super(macroBehaviour);
	this.microBehaviour = microBehaviour;
	this.microBehaviourInsertionIndex = microBehaviourInsertionIndex;
	setMacroBehaviour(macroBehaviour);
    }
   
    public AddMicroBehaviourEvent(MacroBehaviourView macroBehaviourView, MicroBehaviourView microBehaviour) {
	this(macroBehaviourView, microBehaviour, Integer.MAX_VALUE);
    }

    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().removeMicroBehaviour(microBehaviour);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	if (!justRecord) {
	    getMacroBehaviour().addMicroBehaviour(microBehaviour, microBehaviourInsertionIndex, false, true);
	}
	if (continuation != null) {
	    continuation.reconstructSubsequentEvents(null);
	}
    }

    public String toHTMLString(boolean brief) {
	String html = Modeller.constants.added() + " " + microBehaviour.getHTML();
	if (!brief) {
	    html +=  " " + Modeller.constants.to() + " " + macroBehaviourNameAtEventTime;
	    String microBehaviourUrl = getContainingURL();
	    if (microBehaviourUrl != null) {
		html += " " + Modeller.constants.in() + " " + microBehaviourUrl; 
	    }
	}
	return html;
    }
    
    public String getContainingURL() {
	MacroBehaviourView macroBehaviour = getMacroBehaviour();
	if (macroBehaviour != null) {
	    return macroBehaviour.getMicroBehaviourUrl();
	} else {
	    return null;
	}
    }
    
    public void recordInDatabase(final AsyncCallback<String[]> recordSubsequentEventCallback, final boolean notifyOthers) {
	getMicroBehaviour().setExecuteWhenCopied(new Command() {

	    @Override
	    public void execute() {
		String url = getMicroBehaviour().getAllURLs();
		Modeller.getHistoryService().addMicroBehaviourEvent(
			Modeller.userGuid, 
			Modeller.sessionGuid, 
			macroBehaviourNameAtEventTime,
			getContainingURL(),
			getMicroBehaviour().getNameHTMLAndDescription(),
			url,
			getMicroBehaviour().isMacroBehaviour(),
			microBehaviourInsertionIndex, 
			notifyOthers,
			recordSubsequentEventCallback);
	    }
	});
    }
    
    protected static void reconstructInMacroBehaviour(MacroBehaviourView macroBehaviour,
	                                              final String macroBehaviourName, 
	                                              Element eventElement,
	                                              String url,
	                                              boolean restoringHistory, 
	                                              boolean copyOnUpdate,
	                                              boolean justRecord,
	                                              int version, 
	                                              final ReconstructEventsContinutation continuation) {
	final MicroBehaviourView microBehaviour = 
	    createMicroBehaviourView(eventElement, version, url, false);
	if (microBehaviour != null) {
	    Integer insertionIndex = Utils.getIntegerAttribute(eventElement, "insertionIndex");
	    // I think last false below is correct since when reconstructing don't want
	    // to update database with copies
	    if (macroBehaviour.addMicroBehaviour(microBehaviour, insertionIndex, false, false)) {
		String containingMicroBehaviourUrl = macroBehaviour.getMicroBehaviourUrl();
		if (containingMicroBehaviourUrl != null && !justRecord) {
		    MicroBehaviourView containingMicroBehaviour = 
			Modeller.instance().getMicroBehaviourView(containingMicroBehaviourUrl);
		    if (containingMicroBehaviour != null) {
			containingMicroBehaviour.addMacroBehaviourView(macroBehaviour);
		    }
		}
		final ModellerEvent event = 
		    restoringHistory ?
	            new AddMicroBehaviourEvent(macroBehaviour, microBehaviour, insertionIndex) :
		    null;
		if (microBehaviour.isMacroBehaviour()) {
		    continuation.reconstructSubsequentEvents(event);
		    return;
		}
		// following caused much grief in the Epidemic Game Maker
		// where loading of ADD-BUTTON in one session broke others
//		String canonicalUrl = CommonUtils.removeBookmark(url);
		// should the following take into account justRecord?
		if (urlsFetched.contains(url)) {
		    continuation.reconstructSubsequentEvents(event);
		} else if (microBehaviour.getMacroBehaviourViews().isEmpty() && 
		           CommonUtils.hasChangesGuid(url)) {
		    BrowsePanelCommand command = new BrowsePanelCommand() {

			@Override
			public void execute(BrowsePanel panel, String answer[], boolean panelIsNew) {
			    ArrayList<MacroBehaviourView> panelMacroBehaviourViews = 
				panel.getMacroBehaviourViews();
			    microBehaviour.setMacroBehaviourViews(panelMacroBehaviourViews);
			    Modeller.mainTabPanel.remove(panel);
			    Command commandAfterUpdates = new Command() {

				@Override
				public void execute() {
				    continuation.reconstructSubsequentEvents(event);				    
				}
				
			    };
			    panel.fetchAndUpdate(commandAfterUpdates);		    
			}
			
		    };
		    urlsFetched.add(url);
//		    Log.info("AddMicroBehaviourEvent url: " + url); // for debugging
		    Modeller.executeOnMicroBehaviourPage(url, command, true, copyOnUpdate);
		} else {
		    continuation.reconstructSubsequentEvents(event);
		}
	    } else if (continuation != null) {
		// was already added but need to reconstruct subsequent events
		continuation.reconstructSubsequentEvents(null);
	    }
	} else {
	    Modeller.addToErrorLog("Could not create a micro-behaviour" +
		                   " in AddMicroBehaviourEvent of " + url);
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(null);
	    }
	}
    }
    
    private static MicroBehaviourView createMicroBehaviourView(Element eventElement, 
	                                                       int version, 
	                                                       String url, 
	                                                       boolean okToAddSubscripts) {
	String prototypeName = CommonUtils.prototypeName(url);
	if (prototypeName != null) {
	    return Modeller.instance().getMacroBehaviourAsMicroBehaviourWithHTMLName(prototypeName);
	} else {
	    // don't want sharing between different views
	    // of the same url so always create a fresh view
	    String description = ModellerEvent.getMicroBehaviourDescription(eventElement, version);
	    if (description != null) { 
		// is it OK that this makes it have 0 text areas?
		return new MicroBehaviourView(description, url, 0, okToAddSubscripts);
	    } else {
		return null;
	    }
	}
    }

    public MicroBehaviourView getMicroBehaviour() {
        return microBehaviour;
    }

    protected void setMicroBehaviour(MicroBehaviourView microBehaviour) {
        this.microBehaviour = microBehaviour;
    }

}
