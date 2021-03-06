/**
 * 
 */
package uk.ac.lkl.client.composer;

import java.util.ArrayList;
import java.util.logging.Level;

import uk.ac.lkl.client.ButtonWithDebugID;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Display of an attribute that has an interface for finding users
 * 
 * @author Ken Kahn
 *
 */
public class AttributeDisplay extends ButtonWithDebugID {

    private ArrayList<String> readingMicroBehaviours;
    private ArrayList<String> writingMicroBehaviours;

    public AttributeDisplay(final String attributeName, String[] readingMicroBehavioursArray, String[] writingMicroBehavioursArray) {
	super(attributeName);
	this.readingMicroBehaviours = CommonUtils.removeEmptyStrings(readingMicroBehavioursArray);
	this.writingMicroBehaviours = CommonUtils.removeEmptyStrings(writingMicroBehavioursArray);
	ClickHandler clickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		
		ArrayList<SearchResultsItem> items = new ArrayList<SearchResultsItem>();
		if (readingMicroBehaviours.size() > 0) {
		    items.add(new SearchResultsLabel(Modeller.constants.microBehavioursReadingThis()));
		}
		for (String url : readingMicroBehaviours) {
		    MicroBehaviourView microBehaviour = Modeller.instance().getMicroBehaviourView(url);
		    if (microBehaviour != null) {
			MicroBehaviourSearchItem microBehaviourSearchItem = new MicroBehaviourSearchItem(microBehaviour, 0);
			items.add(microBehaviourSearchItem);
		    } else {
			items.add(new SearchResultsLabel("Unable to read: "+ url));
			Utils.logServerMessage(Level.WARNING, "Unable to find the micro-behaviour with the URL: " + url);
		    }
		}
		if (writingMicroBehaviours.size() > 0) {
		    items.add(new SearchResultsLabel(Modeller.constants.microBehavioursWritingThis()));
		}
		for (String url : writingMicroBehaviours) {
		    MicroBehaviourView microBehaviour = Modeller.instance().getMicroBehaviourView(url);
		    if (microBehaviour != null) {
			MicroBehaviourSearchItem microBehaviourSearchItem = new MicroBehaviourSearchItem(microBehaviour, 0);
			items.add(microBehaviourSearchItem);
		    } else {
			items.add(new SearchResultsLabel("Unable to read: "+ url));
			Utils.logServerMessage(Level.WARNING, "Unable to find the micro-behaviour with the URL: " + url);
		    }
		}
		String microBehavioursUsingX = Modeller.constants.microBehavioursUsingX();
		String title = microBehavioursUsingX.replace("***attribute name***", attributeName);
		SearchResultPopup searchResultPopup = new SearchResultPopup(items, title);
		searchResultPopup.center();
		searchResultPopup.show();
	    }
	    
	};
	addClickHandler(clickHandler);
	setStylePrimaryName("modeller-attribute-display");
    }

}
