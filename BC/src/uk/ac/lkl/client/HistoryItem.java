package uk.ac.lkl.client;

import uk.ac.lkl.client.event.ModellerEvent;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Hyperlink;

public class HistoryItem extends Hyperlink {
    protected ModellerEvent event;
    protected String historyToken;

    public HistoryItem(ModellerEvent event) {
	super(event.toHTMLString(false) + event.toHTMLAuthorshipString(), true, event.toString());
	// store the following so it doesn't change if names are changed
	historyToken = this.getTargetHistoryToken();
	this.event = event;
	event.setHistoryItem(this);
	sinkEvents(Event.MOUSEEVENTS);
	addStyleName("modeller-HistoryItem");
    }
    
    public void onBrowserEvent(Event e) {
	super.onBrowserEvent(e);
	int eventType = DOM.eventGetType(e);
	if (eventType == Event.ONMOUSEOVER) {
	    setTitle(event.getHistoryItemTitle());
	}
    }
    
    public void setUndone(boolean undone) {
	String html = event.toHTMLString(false) + event.toHTMLAuthorshipString();
	if (undone) {
	    // gray
	    setHTML(CommonUtils.changeColor(html, "#777777"));
	} else {
	    // restore default
	    setHTML(html);
	}
    }

    public boolean matches(String otherHistoryToken) {
	return historyToken.equals(otherHistoryToken);
    }

    public ModellerEvent getEvent() {
	return event;
    }

    public String getHistoryToken() {
        return historyToken;
    }

}
