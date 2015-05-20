package uk.ac.lkl.client.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;

import uk.ac.lkl.client.Utils;
import uk.ac.lkl.client.Modeller;

@SuppressWarnings("serial")
public class StartEvent extends ModellerEvent {
    
    // this is only used when initialising with a copy=... URL attribute
    private String initialReadOnlySessionGuid = null;
    
    // this is the read only ID for this session
    private String readOnlySessionGuid = null;
    
    // this is the model that should be loaded initially (specified in the URL)
    private String initialModelGuid = null;

    public StartEvent() {
	super("");
    }
    
    public StartEvent(String readOnlySessionGuid, String initialReadOnlySessionGuid, String initialModelGuid) {
	this();
	this.readOnlySessionGuid = readOnlySessionGuid;
	this.initialReadOnlySessionGuid = initialReadOnlySessionGuid;
	this.initialModelGuid = initialModelGuid;
    }

    public String toHTMLString(boolean brief) {
	StringBuffer html = new StringBuffer(Utils.textFontToMatchIcons(Modeller.constants.started()));
	if (initialReadOnlySessionGuid != null) {
	    html.append(" with copy=" + initialReadOnlySessionGuid);
	} else if (initialModelGuid != null) {
	    html.append(" with frozen=" + initialModelGuid);
	}
	return html.toString();
    }
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	Modeller.getHistoryService().startEvent(Modeller.userGuid,
		                                Modeller.sessionGuid, 
		                                initialReadOnlySessionGuid, 
		                                readOnlySessionGuid, 
		                                initialModelGuid,
		                                Modeller.sessionGuidToBeReplaced,
		                                notifyOthers,
		                		// passed along before other services so server knows this
		                		GWT.getHostPageBaseURL(),
		        			Modeller.cachingEnabled,
		        			Modeller.internetAccess,
		                                // start event needs a special callback:
		                                Modeller.instance().getRecordFirstEventCallback());
    }
    
    public String getXML() {
	StringBuffer result = new StringBuffer("<StartEvent" + getDateAttribute());
	if (initialReadOnlySessionGuid != null) {
	    // so can restart with sessionID without the read only copy attribute
	   result.append(" initialReadOnlySessionID='" + initialReadOnlySessionGuid + "'");
	}
	if (readOnlySessionGuid != null) {
	    // so can always find the read-only version of a session
	   result.append(" readOnlySessionID='" + readOnlySessionGuid + "'");
	}
	if (initialModelGuid != null) {
	    result.append(" initialModelID='" + initialModelGuid + "'");
	}
	result.append(" />");
	return result.toString();
    }
    
    public static void reconstruct(
	    Element eventElement, 
	    boolean restoringHistory,
	    int version, 
	    final ReconstructEventsContinutation continuation) {
	final String readOnlyVersion = eventElement.getAttribute("readOnlySessionID");
	if (readOnlyVersion != null) {
	    Modeller.readOnlySessionID = readOnlyVersion;
	}
	String initialReadOnlySessionID = eventElement.getAttribute("initialReadOnlySessionID");
	if (initialReadOnlySessionID != null) {
	    ReconstructEventsContinutation newContinuation =
		new ReconstructEventsContinutation() {

		@Override
		public void reconstructSubsequentEvents(ModellerEvent event) {
		    // this throws the away the initial read-only session ID and model ID since it has done its job already
		    continuation.reconstructSubsequentEvents(new StartEvent(readOnlyVersion, null, null));
		}

	    };
	    Modeller.instance().loadAndReconstructHistory(
		    initialReadOnlySessionID, false, true, Modeller.IGNORE_NO_EVENTS, false, newContinuation); 
	} else {
	    // following is only for old sessions and sessions starting with a blank model 
	    // instead loading of model is a separate event
	    String initialModelID = eventElement.getAttribute("initialModelID");
	    if (initialModelID != null) {
		Modeller.instance().loadModel(initialModelID, false, true, null, null, false, false);
	    }
	    // since this is for very old sessions don't worry about the load finishing before
	    // running the following
	    continuation.reconstructSubsequentEvents(new StartEvent(readOnlyVersion, null, null));
	    Modeller.instance().switchToTabIfIndicatedInURL();
	}
    }

}
