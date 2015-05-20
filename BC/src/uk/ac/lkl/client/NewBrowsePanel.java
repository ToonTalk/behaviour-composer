/**
 * 
 */
package uk.ac.lkl.client;

import java.util.ArrayList;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This is a panel for adding new panels
 * Similar to the '+' tab in many browsers.
 * 
 * @author Ken Kahn
 *
 */
public class NewBrowsePanel extends BrowsePanel implements HasTabPanelSelection {
    
    // tried FlowPanel but didn't look good
    // and there shouldn't be so many...
    protected VerticalPanel previousLinks = new VerticalPanel();
    private UrlBox urlBox;
    
    public NewBrowsePanel() {
	super();
	panelContents.add(new HTML(Modeller.constants.enterALinkUrl()));
	urlBox = new UrlBox("http://", this, false, false);
	panelContents.add(urlBox);
	panelContents.add(previousLinks);
    }
    
    @Override
    public void selectedByTabPanel() {
	Timer timer = new Timer() {

	    @Override
	    public void run() {
		urlBox.getUrlTextBox().setFocus(true);
		urlBox.getUrlTextBox().selectAll();		
	    }
	    
	};
	timer.schedule(500);
    }
    
    @Override
    public void loadNewURL(String urlString, Command doAfterUpdateCommand) {
	// creates new tab and fetches updates from the database
	Modeller.browseToNewTab(urlString, true, doAfterUpdateCommand);
	BrowsePanel.addBrowsedURLs(urlString);
	setCopyOnUpdate(true);
    }

    public void updatePreviouslyVisitedUrls(ArrayList<PreviouslyVisitedTab> previouslyVisitedTabs) {
	previousLinks.clear();
	if (!previouslyVisitedTabs.isEmpty()) {
	    previousLinks.add(new HTML(Modeller.constants.previouslyOpenedTabs()));
	    // go thru it backwards since last is the most recent
	    int size = previouslyVisitedTabs.size();
	    for (int i = size-1; i >= 0; i--)  {
		PreviouslyVisitedTab previouslyVisitedTab = previouslyVisitedTabs.get(i);
		BrowsePanel newBrowsePanel = 
		    new BrowsePanel(previouslyVisitedTab.getTextAreaValues(),
			            previouslyVisitedTab.getEnhancements(),
			            previouslyVisitedTab.getMacroBehaviourViews());
		InternalHyperlink link = 
		    new InternalHyperlink(
			previouslyVisitedTab.getTabName(), 
			previouslyVisitedTab.getUrl(), 
			newBrowsePanel, 
			true);
		String macroBehaviourName = previouslyVisitedTab.getMacroBehaviourName();
		if (macroBehaviourName != null) {
		    link.setTitle(Modeller.constants.clickToReopenTheMicroBehaviourThatWasIn() + " " + macroBehaviourName);
		} else {
		    link.setTitle(Modeller.constants.clickToReopenThisMicroBehaviour());
		}
		previousLinks.add(link);
	    }
	}
    }
    
    @Override
    public void restoreScrollPosition() {
	// do nothing
    }

}
