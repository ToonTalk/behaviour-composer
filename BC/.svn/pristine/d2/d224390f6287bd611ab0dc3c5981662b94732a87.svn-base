package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.Iterator;

import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class ModellerTabPanel extends TabLayoutPanel {
    
    public static final int BAR_HEIGHT = 28;

    protected NewBrowsePanel newBrowsePanel = null;
    
    // a list of URLs whose tab has been closed and not subsequently re-opened
    // most recently closed ones at the end
    // TODO: save tab names as well
    protected ArrayList<PreviouslyVisitedTab> previouslyVisitedTabs = 
	new ArrayList<PreviouslyVisitedTab>();

    private HTML newBrowseTabWidget;

    public ModellerTabPanel() {
	super(BAR_HEIGHT, Style.Unit.PX);
//	getDeckPanel().setAnimationEnabled(true);
	animate(2000);
	SelectionHandler<Integer> selectionHandler = new SelectionHandler<Integer>() {

	    @Override
	    public void onSelection(SelectionEvent<Integer> event) {
		Integer selectedIndex = event.getSelectedItem();
		Widget widgetSelected = getWidget(selectedIndex); // Modeller.getSelectedTab();
		Modeller.instance().widgetSelected(widgetSelected);
		if (widgetSelected == newBrowsePanel && widgetSelected != null) {
		    newBrowsePanel.updatePreviouslyVisitedUrls(previouslyVisitedTabs);
		}
	    }
	    
	};
	addSelectionHandler(selectionHandler);
	addStyleName("modeller-tab-panel");
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }
    
    @Override
    public void insert(Widget child, Widget tab, int beforeIndex) {
	super.insert(child, tab, beforeIndex);
	if (!(child instanceof NewBrowsePanel)) { // don't do this for the '+' tab
	    tabAdded(child);
	    if (child instanceof BrowsePanel) {
		int widgetIndex = getWidgetIndex(child);
		Widget tabWidget = getTabWidget(widgetIndex);
		BrowsePanel browsePanel = (BrowsePanel) child;
		// tabWidget was null in the log file
		if (tabWidget != null) {    
		    browsePanel.setTaggingTitle(tabWidget.getElement().getInnerText());
		    if (tabWidget instanceof ClosableTab) {
			ClosableTab closeableTab = (ClosableTab) tabWidget;
			browsePanel.setTabWidget(closeableTab);
		    }
		}
		browsePanel.restoreScrollPosition();
	    }
	}
    }

    public void addNewTabTab() {
	newBrowsePanel = new NewBrowsePanel();
	newBrowseTabWidget = new HTML(CommonUtils.emphasise("+", BehaviourComposer.FIXED_TAB_COLOR));
	add(newBrowsePanel, newBrowseTabWidget);
    }
    
//    @Override
//    public void add(final Widget widget, String tabText) {
//	super.add(widget, tabText);
//	
//	
//    }
    
//    @Override
//    public void add(final Widget widget, Widget nameWidget) {
//	super.add(widget, nameWidget);
//	if (widget instanceof BrowsePanel) {
//	    BrowsePanel panel = (BrowsePanel) widget;
//	    if (nameWidget instanceof ClosableTab) {
//		ClosableTab closeableTab = (ClosableTab) nameWidget;
//		panel.setTabWidget(closeableTab);
//	    }
//	    panel.setTaggingTitle(nameWidget.getElement().getInnerText());
//	}
//	tabAdded(widget);
//    }
    
//    @Override
//    public void insert(final Widget widget, String tabText, int beforeIndex) {
//	// so spaces can indicate permission to break on different lines
//	super.insert(widget, new HTMLPanel(tabText), beforeIndex);
//	if (widget instanceof BrowsePanel) {
//	    ((BrowsePanel) widget).setTaggingTitle(tabText);
//	}
//	tabAdded(widget);
//    }
    
//    @Override
//    public void insert(Widget widget, Widget tabWidget, int beforeIndex) {
//	super.insert(widget, tabWidget, beforeIndex);
//	if (widget instanceof BrowsePanel) {
//	    ((BrowsePanel) widget).setTaggingTitle(tabWidget.getElement().getInnerText());
//	}
//	tabAdded(widget);
//    }
    
    @Override
    public boolean remove(Widget widget) {
	if (!super.remove(widget)) {
	    return false;
	}
	tabRemoved(widget);
	return true;
    }

    public void insertAfter(BrowsePanel currentBrowsePanel, BrowsePanel newBrowsePanel, ClosableTab tabNameWidget) {
	boolean savedOkToRemoveIfTabBarFull = newBrowsePanel.isOkToRemoveIfTabBarFull();
	newBrowsePanel.setOkToRemoveIfTabBarFull(false); // protect it
	int widgetIndex = getWidgetIndex(currentBrowsePanel);
	if (widgetIndex >= 0) {
	    insert(newBrowsePanel, tabNameWidget, widgetIndex+1);
	    newBrowsePanel.setTabWidget(tabNameWidget);
	} else {
	    add(newBrowsePanel, tabNameWidget);
	}
	tabNameWidget.setVisible(true);
	newBrowsePanel.setOkToRemoveIfTabBarFull(savedOkToRemoveIfTabBarFull);
    }

    public void switchTo(Widget panel) {
	if (panel == null) return;
	if (Modeller.partOfSplitPanel(panel)) {
	    // this panel is already available
	    return;
	}
	int index = panel.isAttached() ? getWidgetIndex(panel) : -1;
	if (index >= 0) {
	    selectTab(index);
	} else if (panel instanceof BrowsePanel) {
	    BrowsePanel browsePanel = (BrowsePanel) panel;
	    ClosableTab tabWidget = browsePanel.getTabWidget();
	    if (tabWidget != null) {
		add(panel, tabWidget);
	    } else {
		add(panel, Modeller.constants.loadingDotDotDot());
	    }
	    index = getWidgetIndex(panel);
	    if (index >= 0) {
		selectTab(index);
	    }
	} else {
	    System.out.println("Panel not a tab.");    
	}
//	panel.setWidth((Utils.getAvailableWidth()) + "px");
    }
       
    public int indexOfBrowsePanelWithURL(String url, boolean copyOnUpdate) {
	for (int i = 0; i < getWidgetCount(); i++) {
	    if (getWidget(i) instanceof BrowsePanel) {
		BrowsePanel panel = (BrowsePanel) getWidget(i);
		if (panel.readyToReuse()) {
		    String currentURL = panel.getCurrentURL();
		    if (url.equalsIgnoreCase(currentURL)) { 
			if (panel.getContainingMacroBehaviour() == null) {
			    return i;
			}
			if (copyOnUpdate && !panel.isCopyOnUpdate()) {
			    panel.setCopyOnUpdate(true);
			}
			return i;
		    }
		}
	    }
	}
	return -1;
    }
    
    public BrowsePanel getBrowsePanelWithURL(String url) {
	for (int i = 0; i < getWidgetCount(); i++) {
	    if (getWidget(i) instanceof BrowsePanel) {
		BrowsePanel panel = (BrowsePanel) getWidget(i);
		String currentURL = panel.getCurrentURL();
		if (url.equalsIgnoreCase(currentURL)) {
		    return panel;
		}
	    }
	}
	return null;
    }
    
    public void tabAdded(final Widget widgetBeingAdded) {
	ensureTabPanelWidthLessThanWindow(widgetBeingAdded);
	if (newBrowsePanel != null) {	    
	    // this might be called while adding/removing/selecting a tab so
	    // defer the following which ensures the + tab is always at the end
	    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

		@Override
		public void execute() {
//		    remove(newBrowsePanel);
		    // insure that the '+' tab is the last one
		    insert(newBrowsePanel, newBrowseTabWidget, Math.max(0, getWidgetCount()-1));
//		    selectTab(widgetBeingAdded);
		}
	    });
	}
	if (widgetBeingAdded instanceof BrowsePanel && !(widgetBeingAdded instanceof NewBrowsePanel)) {
	    BrowsePanel browsePanel = (BrowsePanel) widgetBeingAdded;
	    String url = browsePanel.getCurrentURL();
	    if (url != null) {
		// in case it was re-opened
		PreviouslyVisitedTab previouslyVisitedTab = 
		    new PreviouslyVisitedTab(url, browsePanel);
		previouslyVisitedTabs.remove(previouslyVisitedTab);
	    }
	}
    }

    public void tabRemoved(Widget widgetBeingRemoved) {
	if (widgetBeingRemoved instanceof BrowsePanel && !(widgetBeingRemoved instanceof NewBrowsePanel)) {
	    BrowsePanel browsePanel = (BrowsePanel) widgetBeingRemoved;
	    if (!browsePanel.isTemporary()) {
		String url = browsePanel.getCurrentURL();
		// following ensures there are no duplicates
		// and that the most recently closed are at the end of the array
		PreviouslyVisitedTab previouslyVisitedTab = 
		    new PreviouslyVisitedTab(url, browsePanel);
		previouslyVisitedTabs.remove(previouslyVisitedTab);
		previouslyVisitedTabs.add(previouslyVisitedTab);
	    }
	}
    }
    
    public void ensureTabPanelWidthLessThanWindow(Widget widgetBeingAdded) {
	if (!isAttached()) {
	    return;
	}
	// neither the following nor the commented out code below
	// compute the sum of the widths of the tabs
	// following is too wide (includes unused area)
//	int tabBarWidth = 0;
//	for (int i = 0; i < count; i++) {
//	    Widget widget = getTabWidget(i);
//	    tabBarWidth += widget.getOffsetWidth();
//	}
	Widget tabWidget = getTabWidget(getWidgetCount()-1);
	int rightEdgeOfLastTab = tabWidget.getAbsoluteLeft()+tabWidget.getOffsetWidth();
	int availableWidth = Modeller.mainTabPanel.getOffsetWidth();
	while (rightEdgeOfLastTab > availableWidth) {
	    // remove the oldest permissible one
	    for (int i = 0; i < getWidgetCount(); i++) {
		Widget widget = getWidget(i);
		if (widget instanceof BrowsePanel) { 
		    BrowsePanel browsePanel = (BrowsePanel) widget;
		    if (browsePanel.isOkToRemoveIfTabBarFull() && 
			browsePanel != widgetBeingAdded &&
			browsePanel.getTabWidget() != null) {
			remove(browsePanel);
			tabWidget = getTabWidget(getWidgetCount()-1);
			rightEdgeOfLastTab = tabWidget.getAbsoluteLeft()+tabWidget.getOffsetWidth();
			break;
		    }
		}
	    }
	    break; // give up
	}
    }
    
    public void saveScrollPositionOfCurrentTab() {
	Widget currentPanel = getCurrentPanel();
	if (currentPanel instanceof BrowsePanel) {
	    BrowsePanel currentBrowsePanel = (BrowsePanel) currentPanel;
	    currentBrowsePanel.saveScrollPosition();
	}
    }
    
    public void restoreScrollPositionOfCurrentTab() {
	Widget currentPanel = getCurrentPanel();
	if (currentPanel instanceof BrowsePanel) {
	    BrowsePanel currentBrowsePanel = (BrowsePanel) currentPanel;
	    currentBrowsePanel.restoreScrollPosition();
	}
    }

    public Widget getCurrentPanel() {
	int selectedTabIndex = getSelectedIndex();
	if (selectedTabIndex < 0) { // || selectedTabIndex >= getTabCount()) {
	    return null;
	}
	Widget currentPanel = getWidget(selectedTabIndex);
	return currentPanel;
    }

    public PreviouslyVisitedTab getPreviouslyOpenTab(String url) {
	// returns a closed tab for this url 
	for (PreviouslyVisitedTab previouslyVisitedTab : previouslyVisitedTabs) {
	    if (previouslyVisitedTab.getUrl().equals(url)) {
		return previouslyVisitedTab;
	    }
	}
	return null;
    }
    
    @Override
    public void onLoad() {
	super.onLoad();
	resize();
    }

    public void resize() {
	int extra = Modeller.instance().isSplitHorizontally() ? 30 : 4;
	// 30 is necessary to avoid extra scroll bars
	if (BehaviourComposer.epidemicGameMakerMode()) {
	    // need room for advanced mode check box
	    CheckBox advancedCheckBox = BehaviourComposer.getAdvancedCheckBox();
	    if (advancedCheckBox != null && advancedCheckBox.isAttached()) {
		extra += advancedCheckBox.getOffsetHeight();
	    }
	}
	setPixelSize(Modeller.instance().getMainTabPanelWidth(), Modeller.instance().getMainTabPanelHeight()-extra);
    }

    public void refreshMicroBehavioursWithURL(final String urlWithoutChangesGuid) {
	final ArrayList<MicroBehaviourView> microBehaviourViews = new ArrayList<MicroBehaviourView>();
	MicroBehaviourComand command = new MicroBehaviourComand() {

	    @Override
	    public boolean execute(MicroBehaviourView microBehaviourView) {
		if (microBehaviourView.getUrl().startsWith(urlWithoutChangesGuid)) {
		    microBehaviourViews.add(microBehaviourView);
		}
		return true;
	    }
	    
	};
	Modeller.instance().walkMicroBehaviourViews(command);
	openEachMicroBehaviour(microBehaviourViews.iterator());
    }
    
    private void openEachMicroBehaviour(final Iterator<MicroBehaviourView> iterator) {
	// open them one at a time to avoid any interference
	if (iterator.hasNext()) {
	    MicroBehaviourView microBehaviourView = iterator.next();
	    BrowsePanel containingBrowsePanel = Modeller.getOpenBrowsePanel(microBehaviourView.getUrl());
	    boolean reopen = containingBrowsePanel != null;
	    if (reopen) {
		containingBrowsePanel.removePanel();
	    }
	    Command command = new Command() {

		@Override
		public void execute() {
		    openEachMicroBehaviour(iterator);		    
		}
		
	    };
	    // leave them open even though logically should depend upon 'reopen'
	    // but that leaves tab without tab widget
	    // but at least this makes it clear to the user that all of them have been udpated
	    microBehaviourView.openInBrowsePanel(true, command, true);
	}
    }
    
    public void updatePreviouslyVisitedTabsURL(String oldURL, String newURL) {
	for (PreviouslyVisitedTab previouslyVisitedTab : previouslyVisitedTabs) {
	    if (previouslyVisitedTab.replaceURL(oldURL, newURL)) {
		return;
	    }
	}
    }

    public NewBrowsePanel getNewBrowsePanel() {
        return newBrowsePanel;
    }

}
