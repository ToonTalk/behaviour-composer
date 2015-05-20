/**
 * 
 */
package uk.ac.lkl.client.composer;

import java.util.ArrayList;

import uk.ac.lkl.client.BrowsePanel;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;

/**
 * Panels created to pop up for quick customisation of a micro-behaviour.
 * Extends DialogBox to get drag and close
 * 
 * @author Ken Kahn
 *
 */
public class CustomisationPopupPanel extends DialogBox {
    
    private static ArrayList<CustomisationPopupPanel> customisationPopupPanels = 
	    new ArrayList<CustomisationPopupPanel>();
    
    private BrowsePanel browsePanel;
    
    private boolean browsePanelAboutToBeReopened = false;
    
    public CustomisationPopupPanel() {
	super(false, false);
	// following doesn't help since DialogBox doesn't support resize...
//	setStylePrimaryName("modeller-micro-behaviour-customisation-panel");
    }
    
    @Override
    public void show() {
	super.show();
	if (!customisationPopupPanels.contains(this)) {
	    customisationPopupPanels.add(this);
	}
	arrangePopupPanels();
    }
    
    @Override
    public void hide() {
	super.hide();
	boolean removed = customisationPopupPanels.remove(this);
	if (removed) {
	    arrangePopupPanels();
	    if (browsePanel != null && !isBrowsePanelAboutToBeReopened()) {
		browsePanel.panelClosed(true);
	    }
	}
    }

    public static ArrayList<CustomisationPopupPanel> getCustomisationPopupPanels() {
        return customisationPopupPanels;
    }

    public static void arrangePopupPanels() {  
	if (customisationPopupPanels.size() > 0) {
	    int newestIndex = customisationPopupPanels.size()-1;
	    CustomisationPopupPanel newestPanel = customisationPopupPanels.get(newestIndex);
	    int panelWidth = newestPanel.getOffsetWidth();
	    int panelHeight = newestPanel.getOffsetHeight();
	    int clientWidth = Window.getClientWidth();
	    int clientHeight = Window.getClientHeight();
	    if (panelWidth < clientWidth/3 || panelHeight < clientHeight/3) {
		// just a notification (about loading)
		newestPanel.center();
		return;
	    }
	    int nextLeft = Integer.MAX_VALUE;
	    int nextTop = Integer.MAX_VALUE;
	    int offset = 28;
	    for (int i = newestIndex; i >= 0; i--) {
		// place each panel above and to the left of the previous one
		// by at least offset but by more if extra wide panel
		// (because contents wouldn't fit)
		CustomisationPopupPanel panel = customisationPopupPanels.get(i);
		int left = Math.min(nextLeft, clientWidth-panel.getOffsetWidth());
		int top = Math.min(nextTop, clientHeight-panel.getOffsetHeight());
		panel.setPopupPosition(left, top);
		nextLeft = left-offset;
		nextTop = top-offset;
	    }
	}	
    }

    public BrowsePanel getBrowsePanel() {
        return browsePanel;
    }

    public void setBrowsePanel(BrowsePanel browsePanel) {
        this.browsePanel = browsePanel;
    }

    public boolean isBrowsePanelAboutToBeReopened() {
        return browsePanelAboutToBeReopened;
    }

    public void setBrowsePanelAboutToBeReopened(boolean browsePanelAboutToBeReopened) {
        this.browsePanelAboutToBeReopened = browsePanelAboutToBeReopened;
    }

}
