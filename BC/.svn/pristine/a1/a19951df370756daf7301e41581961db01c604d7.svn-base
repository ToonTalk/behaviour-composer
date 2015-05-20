/**
 * 
 */
package uk.ac.lkl.client.composer;

import java.util.List;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Generates a popup display of the search items
 * 
 * @author Ken Kahn
 *
 */
public class SearchResultPopup extends DecoratedPopupPanel {

    private TitleBar titleBar;
    
    // currently showing pop up or null
    // used to display only one of these at a time
    private static SearchResultPopup poppedUp = null;

    public SearchResultPopup(List<SearchResultsItem> searchItems, String title) {
	setAnimationEnabled(true);
	VerticalPanel verticalPanel = new VerticalPanel();
	verticalPanel.setSpacing(4);
	titleBar = new TitleBar(title);
	ClickHandler clickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		hide();
		removeFromParent();
	    }
	    
	};
	titleBar.getCloseButton().addClickHandler(clickHandler);
	verticalPanel.add(titleBar);
	if (searchItems.isEmpty()) {
	    verticalPanel.add(new HTML(Modeller.constants.noMicroBehavioursReachableFromHere()));
	} else {
	    for (SearchResultsItem item : searchItems) {
		int depth = item.getDepth();
		MicroBehaviourView microBehaviourView = item.getMicroBehaviourView();
		if (microBehaviourView != null) {
		    MicroBehaviourView resultButton = microBehaviourView.createResultButton();
		    HTML indentation = Utils.indentHTML(depth);
		    HorizontalPanel horizontalPanel = new HorizontalPanel();
		    horizontalPanel.add(indentation);
		    horizontalPanel.add(resultButton);
		    verticalPanel.add(horizontalPanel);
		} else {
		    // just a label
		    String text = item.getText();
		    verticalPanel.add(new HTML(text));
		}
	    }
	}
	setWidget(verticalPanel);
    }
    
    @Override
    public void show() {
	if (poppedUp != null) {
	    poppedUp.hide();
	}
	super.show();
	poppedUp = this;
    }
    
    @Override
    public void hide() {
	super.hide();
	poppedUp = null;
    }

}
