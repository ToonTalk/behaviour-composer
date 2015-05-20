package uk.ac.lkl.client.event;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Node;

import uk.ac.lkl.client.RecordSubsequentEventCallback;
import uk.ac.lkl.client.TimerInSequence;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.client.Modeller;

@SuppressWarnings("serial")
public class CompoundEvent extends ModellerEvent {
    
    private class NextEventCallback extends RecordSubsequentEventCallback {
	    
	private RecordSubsequentEventCallback nextEventCallBack;
	private Iterator<ModellerEvent> iterator;
	private RecordSubsequentEventCallback finalEventCallback;
	
	public NextEventCallback(Iterator<ModellerEvent> iterator) {
	    super();
	    this.iterator = iterator;
	}
	
	@Override
	protected void onSuccess() {
	    recordNextEvent(iterator, nextEventCallBack);
	}
	
	private void recordNextEvent(Iterator<ModellerEvent> iterator,
		                     RecordSubsequentEventCallback nextEventCallBack) {
	    if (iterator.hasNext()) {
		ModellerEvent event = iterator.next();
		if (event != null) {
		    event.recordInDatabase(nextEventCallBack, false);
		} else {
		    nextEventCallBack.onSuccess(null);
		}
	    } else if (finalEventCallback != null) {
		finalEventCallback.onSuccess(null);
	    }
	}
	
	public void setNextEventCallBack(RecordSubsequentEventCallback nextEventCallBack) {
	    this.nextEventCallBack = nextEventCallBack;
	}

	public void setFinalEventCallback(RecordSubsequentEventCallback finalEventCallback) {
	    this.finalEventCallback = finalEventCallback;  
	}
    }
    
    // these events are in reverse chronological order
    // A design decision from long ago that is hard to change
    protected ArrayList<ModellerEvent> events;
    protected String alternativeHTML = null;
    
    public CompoundEvent(ArrayList<ModellerEvent> events) {
	super(events);
	this.events = events;
    }

    @Override
    public boolean dirtyEvent() {
	for (ModellerEvent event : events) {
	    if (event != null && event.dirtyEvent()) {
		return true;
	    }
	}
	return false;
    }

    public String toHTMLString(boolean brief) {
	if (alternativeHTML != null) {
	    return alternativeHTML;
	}
	StringBuilder answer = new StringBuilder();
	int eventCount = events.size();
	for (int i = 0; i < eventCount; i++) {
	    if (events.get(i) != null) {
		answer.append(events.get(i).toHTMLString(brief));
		if (i+1 != eventCount && events.get(i+1) != null) { // not the last one nor is the next one null
		    answer.append(Utils.textFontToMatchIcons(Modeller.NON_BREAKING_SPACE + Modeller.constants.and() + "<br>" + Modeller.NON_BREAKING_SPACE));
		}
	    }
	}
	return answer.toString();
    }
       
    public void recordInDatabase(final AsyncCallback<String[]> recordSubsequentEventCallback, final boolean notifyOthers) {
	// important that these events are received in order
	// so use callbacks to ensure it
	if (events.isEmpty()) {
	    // don't bother to generate <CompoundEvent></CompoundEvent>
	    return;
	}
	final Iterator<ModellerEvent> iterator = events.iterator();
	final NextEventCallback nextEventCallBack = new NextEventCallback(iterator);
	nextEventCallBack.setNextEventCallBack(nextEventCallBack);
//	TimerInSequence timer = new TimerInSequence() {
//
//	    @Override
//	    public void run() {
//		recordInDatabase(recordSubsequentEventCallback, notifyOthers, nextEventCallBack, iterator);
//	    }
//	    
//	};
//	nextEventCallBack.setAndRunTimer(timer);
	TimerInSequence finalEventTimer = new TimerInSequence() {

	    @Override
	    public void run() {
		Modeller.getHistoryService().compoundEvent(Modeller.userGuid, Modeller.sessionGuid, true, notifyOthers, nextEventCallBack);	
	    }

	};
	RecordSubsequentEventCallback finalEventCallback = new RecordSubsequentEventCallback() {
	    
	    @Override
	    public void onSuccess(String[] result) {
		Modeller.getHistoryService().compoundEvent(Modeller.userGuid, Modeller.sessionGuid, false, notifyOthers, recordSubsequentEventCallback);
	    }
	};
	nextEventCallBack.setFinalEventCallback(finalEventCallback);
	// following can cause everything to reload in hosted mode only
	// e.g. second renaming of a micro-behaviour added to a prototype
	finalEventCallback.setAndRunTimer(finalEventTimer);
    }
    
    public static void reconstruct(
	    String macroBehaviourName,
	    Element outerEventElement, 
	    boolean restoringHistory,
	    int whatToIgnore,
	    boolean copyOnUpdate,
	    int version, 
	    ReconstructEventsContinutation continuation) {
	NodeList nodelist = outerEventElement.getChildNodes();
	int length = nodelist.getLength();
	final ArrayList<ModellerEvent> events = new ArrayList<ModellerEvent>(length);
	reconstructEachSubEvent(nodelist, 0, events, restoringHistory, whatToIgnore, copyOnUpdate, continuation);
    }

    private static void reconstructEachSubEvent(final NodeList nodelist,
	                                        final int index,
	                                        final ArrayList<ModellerEvent> events, 
	                                        final boolean restoringHistory,
	                                        final int whatToIgnore,
	                                        final boolean copyOnUpdate,
	                                        final ReconstructEventsContinutation continuation) {
	if (index >= nodelist.getLength()) {
	    continuation.reconstructSubsequentEvents(new CompoundEvent(events));
	}
	Node node = nodelist.item(index);
	if (node instanceof Element) {
	    Element eventElement = (Element) node;
	    String tag = eventElement.getNodeName();
	    if (tag != null) {
		ReconstructEventsContinutation newContinuation = new ReconstructEventsContinutation() {

		    @Override
		    public void reconstructSubsequentEvents(ModellerEvent event) {
			if (event != null) {
			    events.add(event);
			}
			reconstructEachSubEvent(nodelist, index+1, events, restoringHistory, whatToIgnore, copyOnUpdate, continuation);
		    }

		};
		reconstructEvent(tag, eventElement, restoringHistory, whatToIgnore, copyOnUpdate, newContinuation);
	    }
	} else if (node != null) {
	    // skip non-element in list
	    reconstructEachSubEvent(nodelist, index+1, events, restoringHistory, whatToIgnore, copyOnUpdate, continuation);
	}
    }
    
    @Override
    public void undo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.undo(record, justRecord, continuation);
	undoSubEvents(0, justRecord, continuation);
    }

    protected void undoSubEvents(
	    final int index, 
	    final boolean justRecord, 
	    final ReconstructEventsContinutation continuation) {
	// do these in event order (which is reverse chronological order)
	if (index >= events.size()) {
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(null);
	    }
	} else {
	    ModellerEvent event = events.get(index);
	    if (event != null) {
		ReconstructEventsContinutation newContinuation = new ReconstructEventsContinutation() {

		    @Override
		    public void reconstructSubsequentEvents(ModellerEvent event) {
			undoSubEvents(index+1, justRecord, continuation);		
		    }
		    
		};
		event.undo(false, justRecord, newContinuation);
	    } else {
		undoSubEvents(index+1, justRecord, continuation);
	    }
	}
    }
    
    @Override
    public void redo(boolean record, boolean justRecord, ReconstructEventsContinutation continuation) {
	super.redo(record, justRecord, continuation);
	redoSubEvents(events.size()-1, justRecord, continuation);
    }
    
    protected void redoSubEvents(
	    final int index, 
	    final boolean justRecord,
	    final ReconstructEventsContinutation continuation) {
	// do these in reverse order (which is chronological order)
	if (index < 0) {
	    if (continuation != null) {
		continuation.reconstructSubsequentEvents(null);
	    }
	} else {
	    ModellerEvent event = events.get(index);
	    if (event != null) {
		ReconstructEventsContinutation newContinuation = new ReconstructEventsContinutation() {

		    @Override
		    public void reconstructSubsequentEvents(ModellerEvent event) {
			redoSubEvents(index-1, justRecord, continuation);			
		    }
		    
		};
		event.redo(false, justRecord, newContinuation);
	    } else {
		redoSubEvents(index-1, justRecord, continuation);
	    }
	}
    }

    public String getAlternativeHTML() {
        return alternativeHTML;
    }

    public void setAlternativeHTML(String alternativeHTML) {
        this.alternativeHTML = alternativeHTML;
    }

}
