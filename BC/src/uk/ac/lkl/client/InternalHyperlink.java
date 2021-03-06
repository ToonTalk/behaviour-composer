package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.HashMap;

import uk.ac.lkl.client.composer.CustomisationPopupPanel;
import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;

/**
 * Implements links that open in resource tabs
 * 
 * @author Ken Kahn
 *
 */
public class InternalHyperlink extends Anchor {
//    protected String url;
//    protected BrowsePanel browsePanel;
    
    public InternalHyperlink(final String html, final String url, final BrowsePanel browsePanel, final boolean reuseBrowsePanel) {
	// if reuseBrowsePanel is false then browsePanel is considered the instigatingBrowsePanel
	// reuseBrowsePanel is true when re-opening a tab via the '+' tab
	super(html, true);
//	this.url = url;
//	this.browsePanel = browsePanel;
	setStylePrimaryName("modeller-InternalHyperLink");
	setTitle(Modeller.constants.clickToOpenThisInANewTab());
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
	addClickHandler(new ClickHandler() {

	    public void onClick(ClickEvent event) {
		// following may be needed if for example in split screen and Help tab is in other panel
		Modeller.switchToResourcesPanel();
		// TODO: determine if this can instead make use of openInBrowsePanel
		final String urlString = CommonUtils.joinPaths(browsePanel.getCurrentURLBase(), url);
		final BrowsePanel openBrowsePanel = Modeller.getOpenBrowsePanel(urlString);
		final int selectedIndex = Modeller.mainTabPanel.getSelectedIndex();
		if (openBrowsePanel != null) {
		    CustomisationPopupPanel customisationPopupPanel = Utils.getAncestorWidget(openBrowsePanel, CustomisationPopupPanel.class);
		    if (customisationPopupPanel != null) {
			customisationPopupPanel.setVisible(false);
			Timer timer = new Timer() {

			    @Override
			    public void run() {
				openBrowsePanel.getMicroBehaviour().openCustomisePanel();
			    }
			    
			};
			timer.schedule(500);
			return;
		    }
		}
		HashMap<Integer, String> textAreaValues = 
		    reuseBrowsePanel ? browsePanel.getTextAreaValues() : null;
		ArrayList<MicroBehaviourEnhancement> enhancements =
		    reuseBrowsePanel ? browsePanel.getEnhancements() : null;
		ArrayList<MacroBehaviourView> macroBehaviours = 
		    reuseBrowsePanel ? browsePanel.getMacroBehaviourViews() : null;
		String tabName = reuseBrowsePanel ? html : Utils.goodTabNameFromURL(url);
		BrowsePanelCommand commandWhenLoaded = 
			new BrowsePanelCommand() {

		    @Override
		    public void execute(final BrowsePanel panel, String[] answer, boolean panelIsNew) {
			panel.scrollToTop();
			BrowsePanel.addBrowsedURLs(urlString);
			if (CommonUtils.hasChangesGuid(urlString)) {
			    Command commandAfterUpdate = new Command() {

				@Override
				public void execute() {
				    customisationPanelOrTab(panel);
				}

			    };
			    panel.fetchAndUpdate(commandAfterUpdate);
			} else {
			    customisationPanelOrTab(panel);
			}
		    }

		    /**
		     * If panel holds a micro-behaviour then creates a customisation panel
		     * otherwise selects the browse panel
		     * 
		     * @param panel
		     */
		    public void customisationPanelOrTab(final BrowsePanel panel) {
			MicroBehaviourView microBehaviour = panel.getMicroBehaviour();
			if (microBehaviour != null) {
			    Modeller.mainTabPanel.remove(panel);
			    CustomisationPopupPanel customisationPopupPanel = Utils.getAncestorWidget(InternalHyperlink.this, CustomisationPopupPanel.class);
			    if (customisationPopupPanel == null) {
				BrowsePanel containingBrowsePanel = Utils.getAncestorWidget(InternalHyperlink.this, BrowsePanel.class);
				if (containingBrowsePanel != null && Modeller.mainTabPanel.getWidgetIndex(containingBrowsePanel) >= 0) {
				    // restore panel that this link lives on
				    Modeller.mainTabPanel.selectTab(containingBrowsePanel);
				}
			    } else if (Modeller.mainTabPanel.getWidgetCount() > selectedIndex) {
				Modeller.mainTabPanel.selectTab(selectedIndex);
			    }
			    if (reuseBrowsePanel || Utils.urlAttributeNotZero("OpenTabsForLinks", false)) {
				// reuseBrowsePanel re-opens previously closed tabs
				// addresses Issue 972
				microBehaviour.openInBrowsePanel(true);
			    } else {
				CustomisationPopupPanel customisationPanel = new CustomisationPopupPanel();
				microBehaviour.updateCustomisationPanel(panel, customisationPanel);
			    }
			    panel.scrollToTop();
			} else {
			    Modeller.mainTabPanel.selectTab(panel);
			}
		    }

		};
		BrowsePanel newBrowsePanel = 
		    Modeller.browseToNewTab(
			tabName, 
			textAreaValues,
			enhancements,
			macroBehaviours,
			urlString,
			// the following is NOT an instigating micro-behaviour
			// just because it has a link to a micro-behaviour
			null, // browsePanel.getMicroBehaviour(),
			null,
			commandWhenLoaded,
			true,
			false,
			true,
			false);
		Modeller.setProtectedBrowsePanel(newBrowsePanel);
	    }
	    
	});
    }
 
}
