package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.Anchor;

public class CommandAnchor extends Anchor {
    public CommandAnchor(String text) {
	super();
	// don't break on spaces within an anchor
	this.setHTML(text.replace(" ", "&nbsp;")); 
	addStyleName("modeller-CommandAnchor");
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

}
