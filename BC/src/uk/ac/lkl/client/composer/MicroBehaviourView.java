package uk.ac.lkl.client.composer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import uk.ac.lkl.client.AsyncCallbackNetworkFailureCapable;
import uk.ac.lkl.client.BehaviourComposer;
import uk.ac.lkl.client.BrowsePanel;
import uk.ac.lkl.client.BrowsePanelCommand;
import uk.ac.lkl.client.ButtonWithDebugID;
import uk.ac.lkl.client.ClosableTab;
import uk.ac.lkl.client.CloseButton;
import uk.ac.lkl.client.CodeTextArea;
import uk.ac.lkl.client.CreateDeltaPageCommand;
import uk.ac.lkl.client.Dimensions;
import uk.ac.lkl.client.MicroBehaviourComand;
import uk.ac.lkl.client.MicroBehaviourEditorCommand;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.NetworkFailure;
import uk.ac.lkl.client.RichTextEntry;
import uk.ac.lkl.client.PopupPanelWithKeyboardShortcuts;
import uk.ac.lkl.client.TimerInSequence;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.client.event.EnhanceMicroBehaviourEvent;
import uk.ac.lkl.client.event.MoveMicroBehaviourEvent;
import uk.ac.lkl.client.event.RemoveLastEnhancementMicroBehaviourEvent;
import uk.ac.lkl.client.event.RemoveMicroBehaviourEvent;
import uk.ac.lkl.client.event.ActivateMicroBehaviourEvent;
import uk.ac.lkl.client.event.InactivateMicroBehaviourEvent;
import uk.ac.lkl.client.event.RenameMicroBehaviourEvent;
import uk.ac.lkl.shared.CommonUtils;
import uk.ac.lkl.shared.DeltaPageResult;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Node;

public class MicroBehaviourView extends ButtonWithDebugID {
    
    // there is a space because some browsers ignore the new line
    // see for example http://www.cs.tut.fi/~jkorpela/html/alt.html#length
    protected static final String TITLE_NEW_LINE_SEPARATOR = " \n";
    protected MicroBehaviourSharedState sharedState; 
    
    private boolean waitingToBeCopied = false;
    private Command executeWhenCopied = null;
    
    // true, for example, when loaded from a model
    // TODO: determine if the following should be replaced by 'true'
    private boolean copyWhenOpened = false;
    
    private boolean containsNetLogoCode = true; // unless informed otherwise
    
    private boolean dirty = false;
          
    public MicroBehaviourView(String nameHTML, String url, int originalTextAreasCount, boolean okToAddSubscripts) {
	this(nameHTML, url, null, null, true, originalTextAreasCount, okToAddSubscripts);
    }
    
    public MicroBehaviourView(
	    String nameHTML, 
	    String url,
	    HashMap<Integer, String> textAreaValues,
	    ArrayList<MicroBehaviourEnhancement> enhancements,
	    int originalTextAreasCount, 
	    boolean okToAddSubscripts) {
	this(nameHTML, url, textAreaValues, enhancements, true, originalTextAreasCount, okToAddSubscripts);
    }
    
    public MicroBehaviourView(String nameHTML,
	                      String url,
	                      HashMap<Integer, String> textAreaValues, 
	                      ArrayList<MicroBehaviourEnhancement> enhancements,
	                      boolean shareState,
	                      int originalTextAreasCount,
	                      boolean okToAddSubscripts) {
	this(nameHTML,
	     shareState ? MicroBehaviourSharedState.findOrCreateSharedState(nameHTML, url, textAreaValues, enhancements, originalTextAreasCount, okToAddSubscripts) :
          		  MicroBehaviourSharedState.createSharedState(nameHTML, url, textAreaValues, enhancements, false, originalTextAreasCount, okToAddSubscripts));
    }
    
    public MicroBehaviourView(String nameHTMLAndDescription, MicroBehaviourSharedState sharedState) {
	super();
	this.sharedState = sharedState;
	if (sharedState != null) {
	    sharedState.addMicroBehaviourView(this);
	    setHTML(getNameHTML());
	} else {
	    setNameHTMLAndDescription(nameHTMLAndDescription);
	}
	addStyleName("modeller-MicroBehaviour");
	popUpMenuWhenClicked();
	if (!Modeller.instance().isTranslateEnabled()) {
	    // won't be translated if updated so don't update
	    updateTitleWhenMouseOver();
	}
    }
    
    @Override
    public void onLoad() {
	super.onLoad();
	if (Modeller.instance().isTranslateEnabled()) {
	    // set title while loading so can be translated
	    updateTitle();
	}
    }

    protected void updateTitleWhenMouseOver() {
	MouseOverHandler mouseOverHandler = new MouseOverHandler() {

	    @Override
	    public void onMouseOver(MouseOverEvent event) {
		updateTitle();
	    }
	    
	};
	addMouseOverHandler(mouseOverHandler);
    }

    protected void popUpMenuWhenClicked() {
	ClickHandler clickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		createPopupMenu(event); // getAbsoluteLeft()+getOffsetWidth()/2, getAbsoluteTop()+getOffsetHeight());
		if (BehaviourComposer.microBehaviourWaitingToBeAdded != null) {
		    // don't let the containing macro-behaviour see this click
		    event.stopPropagation();
		}
	    }

	};
	addClickHandler(clickHandler);
    }

    public MicroBehaviourView(String nameHTML) {
	// Needed by MacroBehaviourAsMicroBehaviourView
	super(nameHTML);
	popUpMenuWhenClicked();
	updateTitleWhenMouseOver();
    }
       
    public MicroBehaviourView copy() {
	// copying an inactive mB will make an active copy 
	return new MicroBehaviourView(getNameHTMLAndDescription(), sharedState);
    }
    
    public MicroBehaviourView createResultButton() {
	// a view that just opens when clicked upon
	MicroBehaviourSearchResult microBehaviourSearchResult = new MicroBehaviourSearchResult(getNameHTMLAndDescription(), sharedState);
	if (!isActive()) {
	    microBehaviourSearchResult.setEnabled(false);
	}
	return microBehaviourSearchResult;
    }
    
    public MicroBehaviourView copyWithoutSharing(HashMap<MicroBehaviourView, MicroBehaviourView> freshCopies) {
	// copying an inactive mB will make an active copy 
	// among other purposes freshCopies prevents infinite recursion when copying a recursive micro-behaviour
	MicroBehaviourView microBehaviourViewAlreadyCopied = freshCopies.get(this);
	if (microBehaviourViewAlreadyCopied != null) {
	    return microBehaviourViewAlreadyCopied;
	}
	String url = getUrl();
	HashMap<Integer, String> textAreaValues = getTextAreaValues();
//	MicroBehaviourView preexistingCopy = Modeller.getMicroBehaviourFromTabPanel(url);
//	int originalTextAreasCount = textAreaValues == null ? 0 : textAreaValues.size();
//	if (originalTextAreasCount == 0 && preexistingCopy != null) {
//	    // might only have default values but check if the browse panel has text areas
//	    BrowsePanel containingBrowsePanel = preexistingCopy.getContainingBrowsePanel(true);
//	    if (containingBrowsePanel != null) {
//		originalTextAreasCount = containingBrowsePanel.textAreasCount();
//	    }
//	}	
	ArrayList<MicroBehaviourEnhancement> enhancements = getEnhancements();
	ArrayList<MicroBehaviourEnhancement> enhancementsCopy = 
		enhancements == null ? null : new ArrayList<MicroBehaviourEnhancement>(enhancements);
	MicroBehaviourView copy = 
	    new MicroBehaviourView(getNameHTMLAndDescription(), 
		                   url, 
		                   textAreaValues,
		                   // give a copy of the list of enhancements to the copy
		                   enhancementsCopy,
		                   false,
		                   getOriginalTextAreasCount(),
		                   true);
	copy.setActive(isActive());
	freshCopies.put(this, copy);
//	if (preexistingCopy != null) {
//	    // no need to clone the following since found a copy of the same micro-behaviour
//	    copy.setMacroBehaviourViews(preexistingCopy.getMacroBehaviourViews());
//	} else {
	    // don't clone this since every copy of the same micro-behaviour has the same macro behaviours
	    // note the following typically does nothing since it is already set above via preexistingCopy
	copy.addMacroBehaviourViews(getMacroBehaviourViews(), freshCopies);
//	}
	return copy;
    }
    
    public boolean setNameHTMLAndDescription(String nameHTMLAndDescription) {
	if (nameHTMLAndDescription == null) {
	    return false;
	}
        boolean nameChanged = sharedState.setNameHTMLAndDescription(nameHTMLAndDescription);
        setHTML(getNameHTML());
        if (nameChanged) {
            setCopyMicroBehaviourWhenExportingURL(true);
        }
        return nameChanged;
    }
    
    public String getModelXML() {	
	return getModelXML(null, new ArrayList<MicroBehaviourView>(), 2);
    }
    
    public String getModelXML(HashMap<MicroBehaviourView, MicroBehaviourView> dirtyMicroBehaviours, 
	                      ArrayList<MicroBehaviourView> seenBefore,
	                      int level) {
	if (seenBefore.contains(this)) {
	    if (level != 0) {
		level = 1; // last level
	    }
	} else {
	    seenBefore.add(this);
	}
	StringBuffer xml = new StringBuffer();
	xml.append("<microbehaviour");
	if (!isActive()) {
	    xml.append(" active='false'");
	}
	if (dirtyMicroBehaviours != null && isCopyMicroBehaviourWhenExportingURL()) {
	    // so that changes aren't clobbered by fetchAndUpdateTextAreas when loaded
//	    xml.append(" dirty='true'");
	    dirtyMicroBehaviours.put(this, this);
	}
	xml.append(">");
	String url = getUrl();
	xml.append("<url>" + CommonUtils.createCDATASection(url) + "</url>");
	// following may become stale
	// but something is needed before it is fetched
	// see Issue 311
	xml.append("<description>" + CommonUtils.createCDATASection(getNameHTMLAndDescription()) + "</description>");
	HashMap<Integer, String> textAreaValues = getTextAreaValues();
	if (!textAreaValues.isEmpty()) {
	    xml.append("<textareas>");
	    Set<Entry<Integer, String>> entries = textAreaValues.entrySet();
	    for (Entry<Integer, String> entry : entries) {
		String value = entry.getValue();
		if (value != null) {
		    xml.append("<textarea index='" + entry.getKey() + "'>");
		    xml.append(CommonUtils.createCDATASection(value));
		    xml.append("</textarea>");
		}
	    }
	    xml.append("</textareas>");
	}
	if (!getMacroBehaviourViews().isEmpty() && level != 0) { 
	    xml.append("<macrobehaviours>");
	    for (MacroBehaviourView macroBehaviourView : getMacroBehaviourViews()) {
		xml.append(macroBehaviourView.getModelXML(dirtyMicroBehaviours, seenBefore, level-1));
	    }
	    xml.append("</macrobehaviours>");
	}
	ArrayList<MicroBehaviourEnhancement> enhancements = getEnhancements();
	if (enhancements != null && !enhancements.isEmpty()) {
	    xml.append("<enhancements indices='");
	    for (MicroBehaviourEnhancement enhancement : enhancements) {
		xml.append(enhancement.ordinal() + ",");
	    }
	    xml.append("' originalTextAreasCount='" + getOriginalTextAreasCount());
	    xml.append("' />");
	} else {
	    // need to inform the server it is empty so can reset it if 
	    // used to have enhancements
	    xml.append("<enhancements />");
	}
	xml.append("</microbehaviour>");
	return xml.toString();
    }

    protected void updateTitle() {
	if (isContainsNetLogoCode()) {
	    // if it hasn't been copied to the server to create a 'delta page' don't get the title
	    String description = inAPrototypeOrList() ? getDescription() : null;
	    if (isActive()) {
		setTitle(createTitle(description, Modeller.constants.clickOnThisForMoreOptions()));
	    } else {
		setTitle(createTitle(description, Modeller.constants.inactivatedClickOnThisForMoreOptions()));
	    }
	} else {
	    setTitle(Modeller.constants.clickToEditThis());
	}
    }
    
    protected String createTitle(String description, String advice) {
	if (description != null) {// && !Modeller.instance().isTranslateEnabled()) {
	    // won't be translated if updated so don't update if translating
	    addNetLogoCodeToTitle();
	}
//	String copyNumberDescription;
//	int currentNameCount = sharedState.getCurrentNameCount(getNameHTML());
//	if (currentNameCount < 2) {
//	    copyNumberDescription = "";
//	} else {
//	    copyNumberDescription = Modeller.constants.thisIsCopyIOfN();
//	    copyNumberDescription = copyNumberDescription.replace("***rank***", Integer.toString(sharedState.getNameCount()));
//	    copyNumberDescription = copyNumberDescription.replace("***current-count***", Integer.toString(currentNameCount));
//	    copyNumberDescription += TITLE_NEW_LINE_SEPARATOR;
//	}
	if (description == null || description.trim().isEmpty()) {
	    return advice;
	} else {
	    return description + TITLE_NEW_LINE_SEPARATOR + advice;
	}
    }
    
    protected void addNetLogoCodeToTitle() {
	final AsyncCallbackNetworkFailureCapable<String[]> callback = new AsyncCallbackNetworkFailureCapable<String[]>() {
	    
	    @Override
	    public void onSuccess(String[] result) {
		NetworkFailure.instance().networkOK(getTimer());
		final String code = result[0];
		if (code != null) {
		    if (Modeller.instance().isTranslateEnabled()) {
			// need to delay the update until it has been translated
			// 5 seconds seems to be more than enough
			Timer delayUpdateTitle = new Timer() {

			    @Override
			    public void run() {
				String title = getTitle();
				setTitle(title + TITLE_NEW_LINE_SEPARATOR + cleanUpCode(code));
			    }

			};
			delayUpdateTitle.schedule(5000);
		    } else {
			setTitle(getTitle() + TITLE_NEW_LINE_SEPARATOR + cleanUpCode(code));
		    }  
		}
		// otherwise ignore the error -- not important
	    }

	    private String cleanUpCode(String code) {
		// remove contributions from radio button labels and comments
		code = code.replace(" no interface\n", "");
		code = code.replace(" add slider\n", "");
		code = code.replace(" add input box (remember to change the box coordinates to be tall enough to see properly)\n", "");
		code = code.replace("Units displayed on the slider\n", "");
		code = code.replace("Displayed horizontally rather than vertically (true or false)\n", "");
		code = code.replace("Can be \nNumber\n, \nString\n, \nColor\n, \nString (reporter)\n, or \nString (commands)\n. The last two check if the code is runnable.\n", "");
		code = code.replace("1\n for multi-line, \n0\n for single line.\n", "");
		// remove blanks lines
		code = code.replace("\n \n", "\n");
		return code.replaceAll("[\n]+", "\n");
	    }
	    
	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		addNetLogoCodeToTitle(callback);
	    }
	    
	});
    }
    
    protected void addNetLogoCodeToTitle(AsyncCallbackNetworkFailureCapable<String[]> callback) {
	Modeller.getResourcePageService().getNetLogoCode(getModelXML(), 
		                                         Modeller.sessionGuid, 
		                                         Modeller.userGuid, 
		                                         CommonUtils.getHostBaseURL(),
		                                         Modeller.cachingEnabled,
		                                         Modeller.internetAccess,
		                                         Modeller.useAuxiliaryFile, 
		                                         callback);
    }

    protected void waitForClickOnAMacroBehaviour(boolean toAPrototype) {
	// if toAPrototype is true then facilitate adding to a prototype 
	// in the BehaviourComposer area
	// else to a list in the Resources area
//	Element rootElement = RootPanel.get().getElement();
//	DOM.setStyleAttribute(rootElement, "cursor", "move");
	String clickOnTheMacroBehaviourYouWantToAddThisTo = 
	    toAPrototype ?
	    Modeller.constants.clickOnThePrototypeYouWantToAddThisTo() :
	    Modeller.constants.clickOnTheListYouWantToAddThisTo();
	Modeller.setAlertsLineAndHighlight(clickOnTheMacroBehaviourYouWantToAddThisTo);
	BrowsePanel containingBrowsePanel = getContainingBrowsePanel(false);
	if (CommonUtils.hasChangesGuid(getUrl()) || containingBrowsePanel == null) {
	    createMicroBehaviourWaitingToBeAdded();
	} else {
	    // can be a fresh URL (without changes guid) so need to create a delta page of it now
	    CreateDeltaPageCommand createDeltaPageCommand = new CreateDeltaPageCommand() {

		    @Override
		    public void execute(MicroBehaviourView microBehaviourView,
			                DeltaPageResult deltaPageResult,
			                boolean panelIsNew,
			                boolean subMicroBehavioursNeedNewURLs,
			                boolean forCopying) {
			super.execute(microBehaviourView, deltaPageResult, panelIsNew, subMicroBehavioursNeedNewURLs, forCopying);
			microBehaviourView.createMicroBehaviourWaitingToBeAdded();
		    }
	    };
	    containingBrowsePanel.createDeltaPage(MicroBehaviourView.this, createDeltaPageCommand, false, false);
	}
	if (toAPrototype) {
	    Modeller.instance().switchToConstructionArea();
	    Modeller.instance().getMainTabPanel().switchTo(BehaviourComposer.composerPanel);
	// best to just stay where one is if Add to a list
//	} else {
//	    // TODO: shouldn't the following also become non-static and use instance()?
//	    Modeller.switchToResourcesPanel();
	}
	Modeller.instance().changeCursor("modeller-holding-micro-behaviour", "modeller-holding-micro-behaviour-wrong");
    }

    protected void createMicroBehaviourWaitingToBeAdded() {
	boolean onABrowsePanel = getContainingMacroBehaviour() == null;
	CustomisationPopupPanel customisationPanel = Utils.getAncestorWidget(this, CustomisationPopupPanel.class);
	BehaviourComposer.microBehaviourWaitingToBeAddedOriginal = this;
	if (customisationPanel != null && 
            Utils.getAncestorWidget(this, MacroBehaviourView.class) == null &&
            this.getContainingMacroBehaviour() == null) {
	    // is the top-level micro behaviour on the customisation panel
	    // panel is about to be closed so use its micro behaviour view
	    BehaviourComposer.microBehaviourWaitingToBeAdded = this;
	} else {    
	    if (onABrowsePanel) { // && CommonUtils.hasChangesGuid(getUrl())) {    
		// just a plain copy that shares state is enough so that two instances of this
		// micro-behaviour are created (on browse panel and macro-behaviour)
		BehaviourComposer.microBehaviourWaitingToBeAdded = copy();
//		if (!CommonUtils.hasChangesGuid(getUrl())) {
//		    // if it doesn't have a changes guid then it needs to be copied to a new URL
//		    BehaviourComposer.microBehaviourWaitingToBeAdded.waitingToBeCopied();
//		}
	    } else {
		// really copying from a list of micro-behaviours to elsewhere
		final HashMap<MicroBehaviourView, MicroBehaviourView> freshCopies = new HashMap<MicroBehaviourView, MicroBehaviourView>();
		BehaviourComposer.microBehaviourWaitingToBeAdded = copyWithoutSharing(freshCopies);
		for (MicroBehaviourView copy : freshCopies.values()) {
		    // since about to inform server
		    copy.setCopyMicroBehaviourWhenExportingURL(false);
		    copy.setWaitingToBeCopied(true);
		}
		Command commandAfterAllCopied = new Command() {

		    @Override
		    public void execute() {
			for (MicroBehaviourView copy : freshCopies.values()) {
			    // since about to inform server
			    copy.setWaitingToBeCopied(false);
			}
		    }
		    
		};
		BehaviourComposer.createDeltaPages(freshCopies, true, true, commandAfterAllCopied);
	    }
	}
	// loading the micro-behaviour can reset its name
	// by saving it now we can reset it after it is loaded
//	final String nameHTML = 
//	    BehaviourComposer.microBehaviourWaitingToBeAdded.getNameHTMLAndDescription();	
//	BrowsePanelCommand command = new BrowsePanelCommand() {
//
//	    @Override
//	    public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
//  		if (BehaviourComposer.microBehaviourWaitingToBeAdded.isWaitingToBeCopied()) {
//		    panel.copyMicroBehaviour(BehaviourComposer.microBehaviourWaitingToBeAdded, nameHTML);
//		}
//		if (panel.isTemporary()) {
//		    // don't need the panel anymore so close it
//		    Modeller.mainTabPanel.remove(panel);
//		}
//		BehaviourComposer.microBehaviourWaitingToBeAdded.setNameHTMLAndDescription(nameHTML);
//	    }
//
//	};
//	if (customisationPanel != null) {
//	    command.execute(customisationPanel.getBrowsePanel(), null, false);
//	} else {
//	    BehaviourComposer.microBehaviourWaitingToBeAdded.runAndBrowseIfNeeded(command, onABrowsePanel, onABrowsePanel);
//	}
    }
    
    protected void createPopupMenu(ClickEvent event) {
	final PopupPanelWithKeyboardShortcuts popupMenu = new PopupPanelWithKeyboardShortcuts(true);
	popupMenu.setAnimationEnabled(true);
	MenuBar menu = new MenuBarWithDebugID(true);
	menu.setAnimationEnabled(true);
	MenuItem firstMenuItem = null;
	final boolean inAPrototypeOrList = inAPrototypeOrList();
	MenuItem menuItem = null;
	final CustomisationPopupPanel customisationPanel = Utils.getAncestorWidget(this, CustomisationPopupPanel.class);
	final boolean inCustomisationPanel = customisationPanel != null;
	MenuItem openMenuItem = null;
	if (isContainsNetLogoCode()) {
	    Command openCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    openTabPanel(customisationPanel, inCustomisationPanel, true);
		};
	    };
	    openMenuItem = popupMenu.createMenuItem('O', Modeller.constants.openAsTab(), Modeller.constants.openTitle(), openCommand);
	    if (inAPrototypeOrList && okToAddCustomisationMenuItem()) {
		Command customiseCommand = new Command() {

		    @Override
		    public void execute() {
			popupMenu.hide();
			openCustomisePanel();
		    }

		};
		MenuItem customiseMenuItem = popupMenu.createMenuItem('C', Modeller.constants.customise(), Modeller.constants.customiseTitle(), customiseCommand);
		menu.addItem(customiseMenuItem);
		firstMenuItem = customiseMenuItem;
		menu.addItem(openMenuItem);
	    }
	    Command addToPrototypeCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    CustomisationPopupPanel customisationPanel = Utils.getAncestorWidget(MicroBehaviourView.this, CustomisationPopupPanel.class);
		    if (customisationPanel != null && 
			    // is in a customisation panel but not in a macro behaviour on that panel
			    Utils.getAncestorWidget(MicroBehaviourView.this, MacroBehaviourView.class) == null) {
			customisationPanel.hide();
		    }
		    waitForClickOnAMacroBehaviour(true);
		}
	    };
	    Command addToListCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    CustomisationPopupPanel customisationPanel = Utils.getAncestorWidget(MicroBehaviourView.this, CustomisationPopupPanel.class);
		    if (customisationPanel != null && !(getParent() instanceof MacroBehaviourView)) {
			// if this micro behaviour is on a list that is in turn in customisation panel then don't close it
			customisationPanel.hide();
		    }
		    waitForClickOnAMacroBehaviour(false);
		}
	    };
	    menuItem = popupMenu.createMenuItem('A', Modeller.constants.addToPrototype(), Modeller.constants.addToPrototypeTitle(), addToPrototypeCommand);
	    menu.addItem(menuItem);
	    if (firstMenuItem == null) {
		firstMenuItem = menuItem;
	    }
	    menuItem = popupMenu.createMenuItem('L', Modeller.constants.addToList(), Modeller.constants.addToListTitle(), addToListCommand);
	    menu.addItem(menuItem);
	}
	if (Modeller.pageEditingEnabled || getUrl().contains(CommonUtils.EDITED_HTML)) {
	    Command editCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    Modeller.setAlertsLine(Modeller.constants.editTheTextThenClickSave());
		    openMicroBehaviourEditor(null, false);
		}
	    };
	    // E shortcut is used by Enhance
	    menuItem = popupMenu.createMenuItem('p', Modeller.constants.edit(), Modeller.constants.editTitle(), editCommand);
	    menu.addItem(menuItem);
	}
	if (!isMacroBehaviour() && okToAddRenameMenuItem() && isContainsNetLogoCode()) {
	    final int microBehaviourButtonHeight = getOffsetHeight();
	    Command renameCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    BrowsePanel containingBrowsePanel = getContainingBrowsePanel(false);
		    if (containingBrowsePanel == null) {
			BrowsePanelCommand command = new BrowsePanelCommand() {

			    @Override
			    public void execute(final BrowsePanel browsePanel, String[] answer, boolean panelIsNew) {
				// if this micro-behaviour view is in a list then
				// the copy in the browse panel is used
				MicroBehaviourView microBehaviour = browsePanel.getMicroBehaviour();
				if (CommonUtils.hasChangesGuid(microBehaviour.getUrl())) {
				    // renaming a copy doesn't create another copy -- why?????
				    browsePanel.setCopyOnUpdate(false);
				}
				microBehaviour.renameMicroBehaviour(browsePanel, microBehaviourButtonHeight);
			    }

			};
			BrowsePanel browsePanel = runAndBrowseIfNeeded(command, true, true);
			Modeller.setProtectedBrowsePanel(browsePanel);
		    } else {
			renameMicroBehaviour(containingBrowsePanel, microBehaviourButtonHeight);
		    }
		};
	    };
	    menuItem = popupMenu.createMenuItem('R', Modeller.constants.rename(), Modeller.constants.renameTitle(), renameCommand);
	    menu.addItem(menuItem);
	}
	if (inAPrototypeOrList && isContainsNetLogoCode()) {
	    if (isActive()) {   
		Command inactivateCommand = new Command() {
		    public void execute() {
			popupMenu.hide();
			MicroBehaviourView.this.setActive(false);
			new InactivateMicroBehaviourEvent(MicroBehaviourView.this).addToHistory();
		    };
		};
		menuItem = popupMenu.createMenuItem('I', Modeller.constants.inactivate(), Modeller.constants.inactivateTitle(), inactivateCommand);
	    } else { 
		Command activateCommand = new Command() {
		    public void execute() {
			popupMenu.hide();
			MicroBehaviourView.this.setActive(true);
			new ActivateMicroBehaviourEvent(MicroBehaviourView.this).addToHistory();
		    };
		};
		menuItem = popupMenu.createMenuItem('A', Modeller.constants.activate(), Modeller.constants.activateTitle(), activateCommand);
	    }
	}
	menu.addItem(menuItem);
	if (inCustomisationPanel && isContainsNetLogoCode()) {
	    // add it near the end
	    menu.addItem(openMenuItem);
	}
	if (inAPrototypeOrList && isContainsNetLogoCode()) {
	    Command removeCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    MacroBehaviourView containingMacroBehaviour = getParentMacroBehaviour();
		    if (containingMacroBehaviour != null) {
			containingMacroBehaviour.removeMicroBehaviour(MicroBehaviourView.this);
			new RemoveMicroBehaviourEvent(containingMacroBehaviour, MicroBehaviourView.this).addToHistory();
		    }
		};
	    };
	    menuItem = popupMenu.createMenuItem('D', Modeller.constants.delete(), Modeller.constants.removeTitle(), removeCommand);
	    menu.addItem(menuItem);
	}
	if (inAPrototypeOrList && isContainsNetLogoCode()) {
	    Command upCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    MacroBehaviourView containingMacroBehaviour = getParentMacroBehaviour();
		    if (containingMacroBehaviour != null) {
			containingMacroBehaviour.moveUp(MicroBehaviourView.this);
			new MoveMicroBehaviourEvent(MicroBehaviourView.this, true).addToHistory();
		    }
		};
	    };
	    boolean moveToBottom = false;
	    boolean moveToTop = false;
	    MacroBehaviourView containingMacroBehaviour = getParentMacroBehaviour();
	    int macroBehaviourListSize = 0;
	    if (containingMacroBehaviour != null) {
		macroBehaviourListSize = containingMacroBehaviour.getMicroBehaviours().size();
		if (macroBehaviourListSize > 1) {
		    int myIndex = containingMacroBehaviour.getWidgetIndex(MicroBehaviourView.this);
		    myIndex -= containingMacroBehaviour.indexOfFirstMicroBehaviourView();
		    if (myIndex == 0) {
			moveToBottom = true;
		    }
		    if (!moveToBottom) {
			if (myIndex+1 == macroBehaviourListSize) {
			    moveToTop = true;
			}
		    }
		}
	    }
	    String moveUp = moveToBottom ? Modeller.constants.moveToBottom() : Modeller.constants.moveUp();
	    String moveUpTitle = moveToBottom ? Modeller.constants.moveToBottomTitle() : Modeller.constants.moveUpTitle();
	    char shortcutKeyUp = moveToBottom ? 'B' : 'U';
	    menuItem = popupMenu.createMenuItem(shortcutKeyUp, moveUp, moveUpTitle,  upCommand);
	    menu.addItem(menuItem);
	    if (macroBehaviourListSize == 1) {
		menuItem.setEnabled(false);
	    }
	    Command downCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    MacroBehaviourView containingMacroBehaviour = getParentMacroBehaviour();
		    if (containingMacroBehaviour != null) {
			containingMacroBehaviour.moveDown(MicroBehaviourView.this);
			new MoveMicroBehaviourEvent(MicroBehaviourView.this, false).addToHistory();
		    }
		};
	    };
	    String moveDown = moveToTop ? Modeller.constants.moveToTop() : Modeller.constants.moveDown();
	    String moveDownTitle = moveToTop ? Modeller.constants.moveToTopTitle() : Modeller.constants.moveDownTitle();
	    // move down's short cut is 'w' since 'd' is taken by 'Delete'. See Issue 764
	    char shortcutKeyDown = moveToTop ? 'T' : 'w';
	    menuItem = popupMenu.createMenuItem(shortcutKeyDown, moveDown, moveDownTitle, downCommand);
	    menu.addItem(menuItem);
	    if (macroBehaviourListSize == 1) {
		menuItem.setEnabled(false);
	    }
	}
	if (!inAPrototypeOrList && isContainsNetLogoCode()) {
	    final BrowsePanel browsePanel = getContainingBrowsePanel(false);
	    MenuBar menuBar = new MenuBarWithDebugID(true);
	    Command repeatCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    enhanceCode(MicroBehaviourEnhancement.DO_EVERY, browsePanel);
		};
	    };
	    // y is a poor short cut choice but nearly no alternatives
	    menuItem = popupMenu.createMenuItem('y', Modeller.constants.repeat(), Modeller.constants.repeatEnhancementTitle(), repeatCommand);
	    menuBar.addItem(menuItem);
	    Command delayCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    enhanceCode(MicroBehaviourEnhancement.DO_AFTER, browsePanel);
		};
	    };
	    menuItem = popupMenu.createMenuItem('D', Modeller.constants.delay(), Modeller.constants.delayEnhancementTitle(), delayCommand);
	    menuBar.addItem(menuItem);
	    Command scheduleCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    enhanceCode(MicroBehaviourEnhancement.DO_AT_TIME, browsePanel);		    
		};
	    };
	    menuItem = popupMenu.createMenuItem('T', Modeller.constants.doAtTime(), Modeller.constants.doAtTimeEnhancementTitle(), scheduleCommand);
	    menuBar.addItem(menuItem);
	    Command probabilityCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    enhanceCode(MicroBehaviourEnhancement.DO_WITH_PROBABILITY, browsePanel);
		};
	    };
	    menuItem = popupMenu.createMenuItem('P', Modeller.constants.doWithProbability(), Modeller.constants.doWithProbabilityEnhancementTitle(), probabilityCommand);
	    menuBar.addItem(menuItem);
	    Command conditionalCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    enhanceCode(MicroBehaviourEnhancement.DO_IF, browsePanel);
		};
	    };
	    menuItem = popupMenu.createMenuItem('C', Modeller.constants.doConditionally(), Modeller.constants.doConditionallyEnhancementTitle(), conditionalCommand);
	    menuBar.addItem(menuItem);
	    Command whenCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    enhanceCode(MicroBehaviourEnhancement.DO_WHEN, browsePanel);
		};
	    };
	    menuItem = popupMenu.createMenuItem('W', Modeller.constants.doWhen(), Modeller.constants.doWhenEnhancementTitle(), whenCommand);
	    menuBar.addItem(menuItem);
	    Command wheneverCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    enhanceCode(MicroBehaviourEnhancement.DO_WHENEVER, browsePanel);
		};
	    };
	    menuItem = popupMenu.createMenuItem('e', Modeller.constants.doWhenever(), Modeller.constants.doWheneverEnhancementTitle(), wheneverCommand);
	    menuBar.addItem(menuItem);
	    Command addVariableCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    enhanceCode(MicroBehaviourEnhancement.ADD_VARIABLE, browsePanel);
		};
	    };
	    menuItem = popupMenu.createMenuItem('V', Modeller.constants.addVariable(), Modeller.constants.addVariableEnhancementTitle(), addVariableCommand);
	    menuBar.addItem(menuItem);
	    Command addCommentCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    enhanceCode(MicroBehaviourEnhancement.ADD_COMMENT, browsePanel);
		};
	    };
	    menuItem = popupMenu.createMenuItem('N', Modeller.constants.addComment(), Modeller.constants.addCommentEnhancementTitle(), addCommentCommand);
	    menuBar.addItem(menuItem);    
	    menu.setAutoOpen(Window.Location.getParameter("selenium") == null);
	    menu.addItem(Modeller.constants.enhance(), menuBar);
	}
	if (!inAPrototypeOrList && isContainsNetLogoCode()) {
	    Command removeEnhancementCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    BrowsePanel containingBrowsePanel = getContainingBrowsePanel(false);
		    RemovedEnhancement enhancementRemoved = removeLastEnhancement();
		    if (containingBrowsePanel != null) {
			containingBrowsePanel.copyMicroBehaviourWhenExportingURL();
			containingBrowsePanel.refreshRegardless();
			new RemoveLastEnhancementMicroBehaviourEvent(enhancementRemoved, getAllURLs(), containingBrowsePanel.getTaggingTitle()).addToHistory();
		    }
		};
	    };
	    menuItem = popupMenu.createMenuItem('l', Modeller.constants.removeLastEnhancement(), Modeller.constants.removeLastEnhancementTitle(), removeEnhancementCommand);
	    menu.addItem(menuItem);
	    if (getEnhancements() == null || getEnhancements().isEmpty()) {
		menuItem.setEnabled(false);
	    }
	}
	if (isContainsNetLogoCode()) {
	    Command searchReachableMicroBehaviours = new Command() {

		@Override
		public void execute() {
		    popupMenu.hide();
		    // TODO: add filters to the results (e.g. substring match)
		    List<SearchResultsItem> searchItems = getReachableMicroBehaviours();
		    SearchResultPopup searchResultPopup = new SearchResultPopup(searchItems, Modeller.constants.searchResults());
		    searchResultPopup.center();
		    searchResultPopup.show();
		}

	    };
	    menuItem = popupMenu.createMenuItem('S', Modeller.constants.searchReachableMicroBehaviours(), Modeller.constants.searchReachableMicroBehavioursTitle(), searchReachableMicroBehaviours);
	    menu.addItem(menuItem);
	}
	popupMenu.setWidget(menu);
	popupMenu.show();
	Utils.positionPopupMenu(event.getClientX(), event.getClientY(), popupMenu);
//	Utils.positionPopupMenu(menuX, menuY, popupMenu, menu, firstMenuItem);
    }

    protected boolean okToAddRenameMenuItem() {
	return true;
    }

    protected boolean okToAddCustomisationMenuItem() {
	return true;
    }

    protected List<SearchResultsItem> getReachableMicroBehaviours() {
	ArrayList<SearchResultsItem> result = new ArrayList<SearchResultsItem>();
	addReachableMicroBehaviours(result, 0);
	return result;
    }

    /**
     * @param searchItems
     * 
     * Adds to searchItems those reachable from here
     */
    public void addReachableMicroBehaviours(ArrayList<SearchResultsItem> searchItems, int depth) {
	ArrayList<MacroBehaviourView> macroBehaviourViews = getMacroBehaviourViews();
	for (MacroBehaviourView macroBehaviourView : macroBehaviourViews) {
	    macroBehaviourView.addReachableMicroBehaviours(searchItems, depth);
	}
    }

    /**
     * @return true if is in a list (e.g. not a browser or customisation panel)
     */
    protected boolean inAPrototypeOrList() {
	return getParent() instanceof MacroBehaviourView;
    }
    
    public void renameMicroBehaviour(final BrowsePanel browsePanel, int microBehaviourButtonHeight) {
	final VerticalPanel wrapper = getWrapper();
	if (wrapper == null) {
	    System.err.println("Expected micro-behaviour button to be wrapped in a Vertical Panel");
	    return;
	}
	final String nameHTML = getNameHTML();
	// height doubled - see Issue 902
	final RichTextEntry richText = new RichTextEntry(this, nameHTML, microBehaviourButtonHeight*2);
	final ScrollPanel richTextPanel = new ScrollPanel(richText);
	final VerticalPanel editorPanel = new VerticalPanel();
	editorPanel.add(new HTML(CommonUtils.emphasise(Modeller.constants.pleaseEditTheFollowingToHaveADifferentName())));
	editorPanel.add(richTextPanel);
//	String changesGuid = CommonUtils.changesGuid(getUrl());
//	if (changesGuid == null) {
//	    // can't rename the original micro-behaviour to create a copy
//	}
	richText.addSaveButtonClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		wrapper.clear();
		wrapper.add(MicroBehaviourView.this);
		final String newNameHTML = richText.getRichTextArea().getHTML();
		String nameHTMLAndDescription = CommonUtils.combineNameHTMLAndDescription(newNameHTML, MicroBehaviourView.this.getDescription());
		setNameHTMLAndDescription(nameHTMLAndDescription);
	        setCopyMicroBehaviourWhenExportingURL(true);
		ClosableTab tabWidget = browsePanel.getTabWidget();
		if (tabWidget != null) {
		    tabWidget.setTabName(newNameHTML);
		}
		MacroBehaviourView containingMacroBehaviour = getContainingMacroBehaviour();
		if (containingMacroBehaviour != null) {
		    MicroBehaviourView microBehaviourInMacroBehaviour = 
			containingMacroBehaviour.getMicroBehaviourWithURL(getUrl(), false, false);
		    if (microBehaviourInMacroBehaviour != null) {
			microBehaviourInMacroBehaviour.setNameHTMLAndDescription(nameHTMLAndDescription);
		    }
		}
		MicroBehaviourView microBehaviourToShareStateWithCopy = 
		    browsePanel.getMicroBehaviourToShareStateWithCopy();
		if (microBehaviourToShareStateWithCopy != null) {
		    microBehaviourToShareStateWithCopy.setNameHTMLAndDescription(nameHTMLAndDescription);
		}
		if (CommonUtils.hasChangesGuid(getUrl())) {
		    // don't record changes to uncopied micro-behaviours
		    new RenameMicroBehaviourEvent(MicroBehaviourView.this, nameHTML).addToHistory();
		} else {
		    browsePanel.copyMicroBehaviourWhenExportingURL();
		}
		browsePanel.updateTextArea(-1, newNameHTML, false);
	    }
	});
	richText.addCancelButtonClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		wrapper.clear();
		wrapper.add(MicroBehaviourView.this);
	    }
	});
	wrapper.clear();
	wrapper.add(editorPanel);
    }
    
    protected VerticalPanel getWrapper() {
	Widget parent = getParent();
	if (parent instanceof VerticalPanel) {
	    return (VerticalPanel) parent;
	} else {
	    return null;
	}
    }

    protected BrowsePanel runAndBrowseIfNeeded(BrowsePanelCommand command, boolean switchTo, boolean reuseExistingPanels) {
	// if in browse panel just runs command
	// otherwise goes to (if reuseExistingPanels) or creates browse panel first
	return Modeller.browseToNewTab(this, getTextAreaValues(), getEnhancements(), command, switchTo, reuseExistingPanels, true);
    }

    public boolean isActive() {
	if (sharedState == null) {
	    return false;
	}
        return sharedState.isActive();
    }

    public void setActive(boolean active) {
        sharedState.setActive(active);
        if (active) {
            removeStyleName(getInactiveStyle());
        } else {
            addStyleName(getInactiveStyle());           
        }
    }
    
    public void inactivateAll() {
	sharedState.inactivateAll();
    }
    
    public String getInactiveStyle() {
	return "modeller-MicroBehaviour-inactive";
    }

    public String getUrl() {
        return sharedState.getUrl();
    }
    
    public String getAllURLs() {
        return sharedState.getAllURLs();
    }
    
    public String getUrlOrMacroBehaviourName() {
	if (isMacroBehaviour()) {
	    return Modeller.constants.thePrototype();
	} else {
	    return getUrl();
	}
    }

    public void setUrl(String url) {
	sharedState.setUrl(url, waitingToBeCopied);
    }
    
    public String getNameHTMLAndDescription() {
	return sharedState.getNameHTMLAndDescription();
    }
    
    public String getNameHTML() {
        return sharedState.getNameHTML();
    }
    
    public String getDescription() {
	if (sharedState == null) {
	    return null;
	}
	return sharedState.getDescription();
    }
    
    public String getPlainName() {
	return CommonUtils.removeHTMLMarkup(getNameHTML());
    }
    
    public HashMap<Integer, String> getTextAreaValues() {
        return sharedState.getTextAreaValues();
    }
    
    public int nextTextAreaIndex() {
	return Math.max(CommonUtils.maximumIndex(getTextAreaValues())+1,
		        getOriginalTextAreasCount());
    }
      
    public void updateTextArea(String newContents, int indexInCode) {
	getTextAreaValues().put(indexInCode, newContents);
	dirty = true;
    }
    
    public String getTextAreaValue(int indexInCode) {
	return getTextAreaValues().get(indexInCode);
    }
    
    public void addTextAreaValues(HashMap<Integer, String> textAreaValues) {
	sharedState.addTextAreaValues(textAreaValues);
    }

    public void addMacroBehaviourView(MacroBehaviourView macroBehaviourView) {
	sharedState.addMacroBehaviourView(macroBehaviourView);
    }

    public ArrayList<MacroBehaviourView> getMacroBehaviourViews() {
	return sharedState.getMacroBehaviourViews();
    }

    public void addMacroBehaviourViews(ArrayList<MacroBehaviourView> macroBehaviourViews,
	                               HashMap<MicroBehaviourView, MicroBehaviourView> freshCopies) {
	sharedState.addMacroBehaviourViews(macroBehaviourViews, freshCopies);
    }
    
    public void setMacroBehaviourViews(ArrayList<MacroBehaviourView> macroBehaviourViews) {
	sharedState.setMacroBehaviourViews(macroBehaviourViews);
    }

    public MacroBehaviourView getMacroBehaviourNamed(String name) {
	return sharedState.getMacroBehaviourNamed(name);
    }

    public String urlNeedsFetching() {
	return sharedState.urlNeedsFetching();
    }
    
    public int getOriginalTextAreasCount() {
        return sharedState.getOriginalTextAreasCount();
    }

    public void setOriginalTextAreasCount(int originalTextAreasCount) {
	sharedState.setOriginalTextAreasCount(originalTextAreasCount);
    }
    
//    public boolean isTextAreasUpdated() {
//        return sharedState.isTextAreasUpdated();
//    }
//
//    public void setTextAreasUpdated(boolean textAreasUpdated) {
//	sharedState.setTextAreasUpdated(textAreasUpdated);
//    }

    public BrowsePanel getContainingBrowsePanel(boolean ignoreMacroBehaviours) {
	Widget ancestor = getParent();
	while (ancestor != null) {
	    if (ancestor instanceof BrowsePanel) {
		return (BrowsePanel) ancestor;
	    } else if (!ignoreMacroBehaviours && ancestor instanceof MacroBehaviourView) {
		// is inside a list so containing browse panel isn't appropriate
		return null;
	    }
	    ancestor = ancestor.getParent();
	}
	return null;
    }

    public MicroBehaviourSharedState getSharedState() {
        return sharedState;
    }

    public void setSharedState(MicroBehaviourSharedState sharedState) {
	if (sharedState != this.sharedState) {
	    this.sharedState.removeMicroBehaviourView(this);
	    this.sharedState = sharedState;
	    this.sharedState.addMicroBehaviourView(this);
	}
    }

    public void newURL(String newURL, boolean dueToCopying) {
	BrowsePanel browsePanel = getContainingBrowsePanel(false);
	boolean containedInBrowsePanel = browsePanel != null;
	if (!containedInBrowsePanel && isAttached()) {
	    browsePanel = Modeller.instance().getMainTabPanel().getBrowsePanelWithURL(getUrl());
	}
	if (dueToCopying) {
	    // since has been copied old URLs are meaningless
	    sharedState.resetPreviousURLs();
	}
	setUrl(newURL);
	if (browsePanel != null) {
	    browsePanel.setCurrentURL(newURL);
	}
	setCopyMicroBehaviourWhenExportingURL(false);
	setWaitingToBeCopied(false);
	if (browsePanel != null && !containedInBrowsePanel) {
	    // see Issue 867
	    browsePanel.refresh();
	}
    }
    
    public void refresh() {
	sharedState.refresh();
    }

    public void setWaitingToBeCopied(boolean waitingToBeCopied) {
	if (!waitingToBeCopied && executeWhenCopied != null) {
	    Command currentExecuteWhenCopied = executeWhenCopied;
	    executeWhenCopied = null;
	    currentExecuteWhenCopied.execute();
	}
	this.waitingToBeCopied = waitingToBeCopied;
    }

    public boolean isWaitingToBeCopied() {
        return waitingToBeCopied;
    }

    public void setExecuteWhenCopied(final Command additionalExecuteWhenCopied) {
	if (waitingToBeCopied) {
	    if (this.executeWhenCopied != null) {
		final Command previousCommand = this.executeWhenCopied;
		new Command() {

		    @Override
		    public void execute() {
			previousCommand.execute();
			additionalExecuteWhenCopied.execute();			
		    }
		    
		};
	    } else {
		this.executeWhenCopied = additionalExecuteWhenCopied;
	    }
	} else {
	    additionalExecuteWhenCopied.execute();
	}
    }

    public MacroBehaviourView getContainingMacroBehaviour() {
        return sharedState.getContainingMacroBehaviour();
    }
    
    public MacroBehaviourView getParentMacroBehaviour() {
        Widget parent = getParent();
        if (parent instanceof MacroBehaviourView) {
            return (MacroBehaviourView) parent;
        } else {
            return null;
        }
    }

    public void setContainingMacroBehaviour(MacroBehaviourView macroBehaviour) {
        sharedState.setContainingMacroBehaviour(macroBehaviour);
    }

    public String getMacroBehaviourName() {
	return sharedState.getMacroBehaviourName();
    }

    public boolean isCopyWhenOpened() {
        return copyWhenOpened;
    }

    public void setCopyWhenOpened(boolean copyWhenOpened) {
        this.copyWhenOpened = copyWhenOpened;
    }

    public boolean isCopyMicroBehaviourWhenExportingURL() {
        return sharedState.isCopyMicroBehaviourWhenExportingURL();
    }

    public void setCopyMicroBehaviourWhenExportingURL(boolean copyMicroBehaviourWhenExportingURL) {
	if (sharedState != null) {
	    sharedState.setCopyMicroBehaviourWhenExportingURL(copyMicroBehaviourWhenExportingURL);
	    if (!copyMicroBehaviourWhenExportingURL) {
		dirty = false;
	    }
	} // otherwise is a MacroBehaviourAsAMicroBehaviour
    }

    public boolean isCopyOnUpdate() {
        return sharedState.isCopyOnUpdate();
    }

    public void setCopyOnUpdate(boolean copyOnUpdate) {
        sharedState.setCopyOnUpdate(copyOnUpdate);
        if (!copyOnUpdate) {
            dirty = false;
        }
    }
    
    public void addEnhancement(MicroBehaviourEnhancement enhancement) {
	sharedState.addEnhancement(enhancement);
	dirty = true;
    }
    
    public void clearEnhancements() {
	sharedState.clearEnhancements();
	// not really dirty since this is only used when loading
    }
    
    public RemovedEnhancement removeLastEnhancement() {
	dirty = true;
	// when undoing an enhancement or directly called from the menu
	return sharedState.removeLastEnhancement();
    }

    public ArrayList<MicroBehaviourEnhancement> getEnhancements() {
        return sharedState.getEnhancements();
    }
    
    public void setEnhancements(ArrayList<MicroBehaviourEnhancement> enhancements) {
	sharedState.setEnhancements(enhancements);
    }
    
    public MacroBehaviourView getMacroBehaviourViewedAsMicroBehaviour(boolean warn) {
	return null;
    }
    
    public boolean isMacroBehaviourViewedAsMicroBehaviourNamed(String nameHTML) {
	return false;
    }

    public boolean isMacroBehaviour() {
	return false;
    }
    
    public int enhanceCode(MicroBehaviourEnhancement enhancement, BrowsePanel browsePanel) {
	browsePanel.setEnhancementsInstalled(false);
	browsePanel.copyMicroBehaviourWhenExportingURL();
	addEnhancement(enhancement);
	return enhanceCode(enhancement, browsePanel, true, nextTextAreaIndex()-1);
    }
    
    public int enhanceCode(MicroBehaviourEnhancement enhancement, BrowsePanel browsePanel, boolean record, int previousIndex) {
	if (previousIndex < 0) { // not yet initialised
	    int textAreasCount = getOriginalTextAreasCount();
	    previousIndex = textAreasCount-1;
	}
	switch (enhancement) {
	case DO_EVERY:
	    previousIndex = 
		enhanceCode(enhancementStrings(enhancement), 
			    "1", 
			    enhancementTextAreaName(enhancement),
			    "1", "76",
			    "#3366FF",
			    Modeller.constants.doEveryTextAreaTitle(),
			    true,
			    previousIndex,
			    record,
			    browsePanel);
	    break;
	case DO_AFTER:
	    previousIndex = 
		enhanceCode(enhancementStrings(enhancement), 
			    "1", 
			    enhancementTextAreaName(enhancement), 
			    "1", "76",
			    "#6666FF",
			    Modeller.constants.doAfterTextAreaTitle(),
			    true, 
			    previousIndex,
			    record,
			    browsePanel);
	    break;
	case DO_AT_TIME:
	    previousIndex = 
		enhanceCode(enhancementStrings(enhancement),
			    "1", 
			    enhancementTextAreaName(enhancement),
			    "1", "76",
			    "#9966FF",
			    Modeller.constants.doAtTimeTextAreaTitle(),
			    true,
			    previousIndex,
			    record,
			    browsePanel);
	    break;
	case DO_WITH_PROBABILITY:
	    previousIndex = 
		enhanceCode(enhancementStrings(enhancement), 
			    "0.5", 
			    enhancementTextAreaName(enhancement), 
			    "1", "76",
			    "#CC33FF",
			    Modeller.constants.doWithProbabilityTextAreaTile(),
			    true,
			    previousIndex,
			    record,
			    browsePanel);
	    break;
	case DO_IF:
	    previousIndex = 
		enhanceCode(enhancementStrings(enhancement), 
			    "true", 
			    enhancementTextAreaName(enhancement), 
			    "5", "76",
			    "#FF00FF",
			    Modeller.constants.doIfTextAreaTitle(),
			    true, 
			    previousIndex,
			    record,
			    browsePanel);
	    break;
	case DO_WHEN:
	    previousIndex = 
		enhanceCode(enhancementStrings(enhancement), 
			   "true", 
			   enhancementTextAreaName(enhancement),
			   "5", "76",
			   "#CC0099",
			   Modeller.constants.whenTextAreaTitle(),
			    true,
			   previousIndex,
			   record,
			   browsePanel);
	    break;
	case DO_WHENEVER:
	    previousIndex = 
		enhanceCode(enhancementStrings(enhancement), 
			   "true", 
			   enhancementTextAreaName(enhancement), 
			   "5", "76",
			   "#CC3399",
			   Modeller.constants.wheneverTextAreaTitle(),
			    true,
			   previousIndex,
			   record,
			   browsePanel);
	    break;
	case ADD_VARIABLE:
	    previousIndex = 
		enhanceCode(enhancementStrings(enhancement), 
			   "name value", 
			   enhancementTextAreaName(enhancement), 
			   "1", "76",
			   "DarkBlue",
			   Modeller.constants.addVariableTextAreaTitle(),
			   false,
			   previousIndex,
			   record,
			   browsePanel);
	    break;
	case ADD_COMMENT:
	    previousIndex = 
		enhanceCode(enhancementStrings(enhancement), 
			   ";", 
			   enhancementTextAreaName(enhancement), 
			   "1", "76",
			   "Black",
			   Modeller.constants.addCommentTextAreaTitle(),
			   false,
			   previousIndex,
			   record,
			   browsePanel);
	    break;
	}
	if (record) {
	    new EnhanceMicroBehaviourEvent(enhancement, getAllURLs(), browsePanel.getTaggingTitle()).addToHistory();
	}
	return previousIndex;
    }
    
    public String[] enhancementStrings(MicroBehaviourEnhancement enhancement) {
	switch (enhancement) {
	case DO_EVERY:
	    return new String[]{"do-every (", ")", "[", "]"};
	case ADD_COMMENT:
	    return new String[]{" ", "", "<br>", ""};
	case ADD_VARIABLE:
	    return new String[]{"let", "", "<br>", ""};
	case DO_AFTER:
	    return new String[]{"do-after (", ")", "[", "]"};
	case DO_AT_TIME:
	    return new String[]{"do-at-time (", ")", "[", "]"};
	case DO_IF:
	    return new String[]{"do-if (", ")", "[", "]"};
	case DO_WHEN:
	    return new String[]{"when [", "]", "[", "]"};
	case DO_WHENEVER:
	    return new String[]{"whenever [", "]", "[", "]"};
	case DO_WITH_PROBABILITY:
	    return new String[]{"do-with-probability (", ")", "[", "]"};
	default:
	    return null;	
	}
    }
    
    public String enhancementTextAreaName(MicroBehaviourEnhancement enhancement) {
	switch (enhancement) {
	case ADD_COMMENT:
	    return Modeller.constants.notes();
	case ADD_VARIABLE:
	    return Modeller.constants.nameAndValue();
	case DO_AFTER:
	    return Modeller.constants.delayDuration();
	case DO_AT_TIME:
	    return Modeller.constants.scheduledTime();
	case DO_EVERY:
	    return Modeller.constants.cycleDuration();
	case DO_IF:
	    return Modeller.constants.condition();
	case DO_WHEN:
	    return Modeller.constants.condition();
	case DO_WHENEVER:
	    return Modeller.constants.condition();
	case DO_WITH_PROBABILITY:
	    return Modeller.constants.odds();
	default:
	    return null;
	}
    }

    public int enhanceCode(
	    final String[] strings, 
	    String textAreaContents, 
	    final String name, 
	    final String rows,
	    final String columns, 
	    final String color,
	    final String title,
	    final boolean insertBreak,
	    final int previousIndex,
	    final boolean copyOnUpdate,
	    BrowsePanel browsePanel) {
	if (browsePanel == null) {
	    browsePanel = getContainingBrowsePanel(true);
	}
	int indexInCode = previousIndex;
	if (browsePanel == null) {
	    final String finalTextAreaContents = textAreaContents;
	    BrowsePanelCommand command = new BrowsePanelCommand() {

		@Override
		public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
		    enhanceCode(strings, finalTextAreaContents, name, rows, columns, color, title, insertBreak, previousIndex, copyOnUpdate, panel);		    
		}
		
	    };
//	    Log.info("enhanceCode getUrl: " + getUrl()); // for debugging
	    Modeller.executeOnNewMicroBehaviourPage(getUrl(), command, true, copyOnUpdate);
	    return indexInCode+1;
	}
	NodeList<Element> preElements = browsePanel.getPreElements();
	int length = preElements.getLength();
	if (length > 0) {
	    String fontStart = "<font color='" + color + "' ><b>";
	    String fontEnd = "</b></font>";
	    Document document = preElements.getItem(0).getOwnerDocument();
	    for (int i = 0; i < length; i++) {
		Element preElement = preElements.getItem(i);
		Element parentElement = preElement.getParentElement();
		String id = preElement.getAttribute("id");
		String previousBeforeCode = preElement.getInnerHTML();
		if (id.equals(CommonUtils.BEFORE_CODE_ELEMENT)) {
		    Element firstStringElement = document.createElement("SPAN");
		    String innerHTML = fontStart + Utils.addLinksToDocumentation(strings[0]) + fontEnd;
		    if (!insertBreak) {
			// PRE element cause a line break 
			innerHTML = innerHTML.replace("<pre>", "<pre style='display: inline;'>");
			// but do want a space
			innerHTML = innerHTML.replace("</pre>", "</pre>&nbsp;");
		    }
		    firstStringElement.setInnerHTML(innerHTML);
//		    Style style = firstStringElement.getStyle();
//		    style.setColor(color);
		    parentElement.insertAfter(firstStringElement, preElement);
		    Element secondStringElement = document.createElement("SPAN");
		    // force a new line before the "["
		    String breakString = insertBreak ? "<br>" : "";
		    secondStringElement.setInnerHTML(strings[1] + breakString + fontStart + strings[2] + fontEnd);
//		    style = secondStringElement.getStyle();
//		    style.setColor(color);
		    if (textAreaContents != null) {
			Element textAreaElement = document.createElement("TEXTAREA");
			// TODO: remove this obsolete rows and columns stuff (or is it still occasionally useful?)
			textAreaElement.setAttribute("rows", rows);
			textAreaElement.setAttribute("cols", columns);
			textAreaElement.setAttribute("name", name);
			HashMap<Integer, String> textAreaValues = getTextAreaValues();
			if (previousIndex < 0 && getOriginalTextAreasCount() > 0) {
			    // nextIndex could be larger than textAreasCount if multiple
			    // enhancements added
			    // previousIndex can be -1 if there are no original text areas
			    int nextIndex = CommonUtils.maximumIndex(textAreaValues)+1;
			    int textAreasCount = getOriginalTextAreasCount();
			    indexInCode = Math.max(nextIndex, textAreasCount);
			} else {
			    // installing/opening - can be several enhancements
			    indexInCode = previousIndex+1;
			}
			String currentValue = textAreaValues.get(indexInCode);
			if (currentValue == null || CommonUtils.TOKEN_FOR_REMOVED_TEXT_AREA.equals(currentValue)) {
			    textAreaValues.put(indexInCode, textAreaContents);
			} else {
			    textAreaContents = currentValue;
			}
			// not sure the following is needed
			textAreaElement.setAttribute("index", Integer.toString(indexInCode));
			CodeTextArea codeTextArea = new CodeTextArea(indexInCode, browsePanel, textAreaElement);
			codeTextArea.setCurrentContents(textAreaContents);
			codeTextArea.setTitle(title);
			Element codeTextPlaceHolder = document.createDivElement();
			String temporaryId = "temporaryID";
			codeTextPlaceHolder.setId(temporaryId);
			parentElement.insertAfter(codeTextPlaceHolder, firstStringElement);
			parentElement.insertAfter(secondStringElement, codeTextPlaceHolder);
			browsePanel.getBrowsePanelHTML().addAndReplaceElement(codeTextArea, temporaryId);
		    } else {
			parentElement.insertAfter(secondStringElement, firstStringElement);
		    }
		} else if (id.equals(CommonUtils.AFTER_CODE_ELEMENT)) {
		    preElement.setInnerHTML(fontStart + strings[3] + fontEnd + previousBeforeCode);
		    break;
		} else {
		    if (insertBreak && !preElement.getParentElement().getTagName().equalsIgnoreCase("BLOCKQUOTE")) {
			// don't do multiple nesting
			Element blockquote = document.createElement("BLOCKQUOTE");
			parentElement.replaceChild(blockquote, preElement);
			blockquote.appendChild(preElement);
		    }
		    // add final element (typically ']')
		    if (i+1 == length && strings.length >= 4) {
			SpanElement spanElement = document.createSpanElement();
			spanElement.setInnerHTML(fontStart + strings[3] + fontEnd);
			preElement.appendChild(spanElement);
		    }
		}
	    }
	}
	return indexInCode;
    }
    
    public BrowsePanel reopenCustomisationPanelAsTab() {
	CustomisationPopupPanel customisationPopupPanel = Utils.getAncestorWidget(this, CustomisationPopupPanel.class);
	if (customisationPopupPanel != null) {
	    customisationPopupPanel.hide();
	}
	return openInBrowsePanel(true, null, true);
    }

    public BrowsePanel openInBrowsePanel(boolean switchTo) {
	return openInBrowsePanel(switchTo, null, true);
    }
    
    /**
     * Opens a browse panel for this microBehaviour
     */
    public BrowsePanel openInBrowsePanel(final boolean switchTo, final Command commandAfterLoading, boolean reuseExistingPanel) {
	if (isMacroBehaviour()) {
	    Modeller.instance().switchToConstructionArea();
	    // TODO: highlight macroBehaviour
	    return null;
	}
//	BrowsePanel containingBrowsePanel = getContainingBrowsePanel(false);
	boolean copyOnUpdate = false; // obsolete???
//	    isCopyWhenOpened() ||
//	    (containingBrowsePanel != null ? containingBrowsePanel.isCopyOnUpdate() : isCopyOnUpdate());
	// following meant that edits were lost when saved as a model
	// and reloaded
	// but without it micro-behaviours revert sometimes to original values
	// so now it is conditional on copyMicroBehaviourWhenExportingURL
	// since if isCopyMicroBehaviourWhenExportingURL then has been edited
	BrowsePanelCommand command = 
		new BrowsePanelCommand() {

	    @Override
	    public void execute(final BrowsePanel panel, final String[] answer, final boolean panelIsNew) {
		if (panelIsNew) { // && !panel.isCopyMicroBehaviourWhenExportingURL()) {
		    Command commandAfterUpdate = new Command() {

			@Override
			public void execute() {
			    panel.scrollToTop();
			    if (switchTo) {
				// defer it since sometimes runs before it is added so getWidgetIndex returns -1 (I think)
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				    
				    @Override
				    public void execute() {
					int widgetIndex = Modeller.mainTabPanel.getWidgetIndex(panel);
					if (widgetIndex >= 0) {
					    Modeller.mainTabPanel.selectTab(widgetIndex);
					}
				    }
				    
				});
			    }
			    if (commandAfterLoading != null) {
				commandAfterLoading.execute();
			    }
			    BrowsePanel.addBrowsedURLs(MicroBehaviourView.this.getUrl());
			}
			
		    };
		    panel.fetchAndUpdate(commandAfterUpdate);
		} else {
		    if (switchTo) {
			Modeller.mainTabPanel.switchTo(panel);
		    }
		    if (commandAfterLoading != null) {
			commandAfterLoading.execute();
		    }
		}
	    }

	};
	BrowsePanel browsePanel = 
	    Modeller.browseToNewTab(
		    MicroBehaviourView.this, 
		    getTextAreaValues(),
		    getEnhancements(),
		    command,
		    switchTo,
		    reuseExistingPanel,
		    copyOnUpdate);
	Modeller.setProtectedBrowsePanel(browsePanel);
	return browsePanel;
    }

    public void replaceURLs(String[] renamings) {
	if (sharedState != null) {
	    sharedState.replaceURLs(renamings);
	}
    }

    public void openCustomisePanel() {
 	final CustomisationPopupPanel customisationPanel = new CustomisationPopupPanel();
 	customisationPanel.setWidget(new HTML(Modeller.constants.loadingPleaseWait()));
 	customisationPanel.show();
 	BrowsePanelCommand command = new BrowsePanelCommand() {

	    @Override
	    public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
		updateCustomisationPanel(panel, customisationPanel);
		panel.scrollToTop();
		panel.fetchAndUpdate(null);
	    }
	    
	    @Override
	    public void failed() {
		super.failed();
		customisationPanel.hide();
	    }
 	    
 	};
 	Modeller.browseToNewTab(
		    MicroBehaviourView.this, 
		    getTextAreaValues(),
		    getEnhancements(),
		    command,
		    false,
		    true,
		    false);
    }

    public void updateCustomisationPanel(final BrowsePanel browsePanel, 
	                                 final CustomisationPopupPanel customisationPanel) {
	customisationPanel.setAnimationEnabled(true);
	customisationPanel.setBrowsePanel(browsePanel);
	String caption = Modeller.constants.customisePanelCaption().replace("***micro-behaviour-name***", "<b>" + getPlainName() + "</b>");
	customisationPanel.setHTML(caption);
	VerticalPanel verticalPanel = new VerticalPanel();
	Image closeButton = new CloseButton();
	ClickHandler clickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		customisationPanel.hide();
		customisationPanel.removeFromParent();
//		browsePanel.panelClosed(true);
	    }

	};
	closeButton.addClickHandler(clickHandler);
	// close button is aligned right
	verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	verticalPanel.add(closeButton);
	verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
	browsePanel.setExtraWidgetsVisible(false);
	browsePanel.setVisible(true);
	int clientWidth = Window.getClientWidth();
	int clientHeight = Window.getClientHeight();
	int popupWidth =  clientWidth/2;
	int popupHeight = clientHeight/2;
	verticalPanel.add(browsePanel);
	browsePanel.setPixelSize(popupWidth, popupHeight);
	customisationPanel.setWidget(verticalPanel);
	customisationPanel.show();
	Integer preElementsRight = null;
	Dimensions preElementsDimension = browsePanel.getPreElementsDimensions();
	preElementsRight = preElementsDimension.getWidth();
	if (preElementsRight != null && preElementsRight > popupWidth) {
	    browsePanel.setWidth(Math.min(preElementsRight+20, clientWidth-20) + "px");
	}
	MicroBehaviourView microBehaviourViewInBrowsePanel = sharedState.getMicroBehaviourViewInBrowsePanel();
	if (microBehaviourViewInBrowsePanel != null) {
	    browsePanel.ensureVisible(microBehaviourViewInBrowsePanel);
	}
	CustomisationPopupPanel.arrangePopupPanels();
    }

    public boolean isSubMicroBehavioursNeedNewURLs() {
	return sharedState.isSubMicroBehavioursNeedNewURLs();
    }

    public String getCode(BrowsePanel browsePanel) {
	NodeList<Element> preElements = browsePanel.getPreElements();
	int length = preElements.getLength();
	if (length > 0) {
	    StringBuffer code = new StringBuffer();
	    boolean started = false;
	    for (int i = 0; i < length; i++) {
		Element preElement = preElements.getItem(i);
		String id = preElement.getAttribute("id");
		if (id.equals(CommonUtils.BEFORE_CODE_ELEMENT)) {
		    started = true;
		} else if (id.equals(CommonUtils.AFTER_CODE_ELEMENT)) {
		    return code.toString();
		} else if (started) {
		    code.append(preElement.getInnerText());
		}
	    }
	}
	return null;
    }
    
    public boolean isWarnThatTextAreasHaveChanged() {
        return sharedState.isWarnThatTextAreasHaveChanged();
    }

    public void setWarnThatTextAreasHaveChanged(boolean warnThatTextAreasHaveChanged) {
	sharedState.setWarnThatTextAreasHaveChanged(warnThatTextAreasHaveChanged);
    }

    public boolean isContainsNetLogoCode() {
        return containsNetLogoCode;
    }

    public void setContainsNetLogoCode(boolean containsNetLogoCode) {
        this.containsNetLogoCode = containsNetLogoCode;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void openMicroBehaviourEditor(final MicroBehaviourEditorCommand microBehaviourEditorCommand, final boolean firstTime) {
	CustomisationPopupPanel customisationPopupPanel = 
		Utils.getAncestorWidget(MicroBehaviourView.this, CustomisationPopupPanel.class);
	if (customisationPopupPanel != null) {
	    customisationPopupPanel.hide();
	}
	// following needs to happen after the panel is loaded
	BrowsePanelCommand browsePanelCommand = new BrowsePanelCommand() {
	    public void execute(BrowsePanel panel, String answer[], boolean PanelIsNew) {
		Modeller.mainTabPanel.remove(panel);
		Widget editor = panel.createAndSwitchToEditor(false, isContainsNetLogoCode(), firstTime);
		if (microBehaviourEditorCommand != null) {
		    microBehaviourEditorCommand.execute(editor);
		}
		BehaviourComposer.invalidateRunShareTabs();
	    }
	};
	Modeller.browseToNewTab(
		MicroBehaviourView.this.getText(),
		getTextAreaValues(),
		getEnhancements(),
		MicroBehaviourView.this.getMacroBehaviourViews(),
		MicroBehaviourView.this.getUrl(), 
		null,
		null,
		browsePanelCommand, 
		false, 
		false,
		true,
		true);
    }

    public String applyEdits(String newBody, String oldBody, List<String> textAreasInfo, ArrayList<String> namesOfEditedMicroBehaviours) {
	HashMap<Integer, String> textAreaValues = getTextAreaValues();
	int textAreaIndex = nextTextAreaIndex()-1;
	boolean edited = false;
	ArrayList<MicroBehaviourEnhancement> enhancements = getEnhancements();
	int lastEnhancementIndex = enhancements == null ? -1 : enhancements.size()-1;
	for (int i = lastEnhancementIndex; i >= 0; i--) {
	    // consider outermost (most recent) enhancement first
	    MicroBehaviourEnhancement enhancement = enhancements.get(i);
	    if (enhancement == MicroBehaviourEnhancement.ADD_COMMENT) {
		// doesn't really insert any code
		continue;
	    }
	    String[] enhancementStrings = enhancementStrings(enhancement);
	    String beforeTextArea = enhancementStrings[0];
	    String afterTextArea = enhancementStrings[1];
	    if (enhancement == MicroBehaviourEnhancement.DO_IF) {
		beforeTextArea = "if ("; // is compiled from do-if to if
	    }
	    String newValue;
	    int[] startEnd = startEnd(newBody, beforeTextArea, afterTextArea);
	    if (startEnd == null) {
		continue;
	    }
	    if (!afterTextArea.isEmpty()) {
		// trim left and right space if there
		if (newBody.charAt(startEnd[0]) == ' ') {
		    startEnd[0]++;		
		}
		if (newBody.charAt(startEnd[1]) == ' ') {
		    startEnd[1]--;		
		}
		newValue = newBody.substring(startEnd[0], startEnd[1]);
	    } else if (enhancement == MicroBehaviourEnhancement.ADD_VARIABLE) {
		int closingParenIndex = CommonUtils.closingParenthesisIndex(newBody);
		if (closingParenIndex < 0) {
		    newValue = newBody.substring(startEnd[0]);
		} else {
		    newValue = newBody.substring(startEnd[0], closingParenIndex);
		    newValue = removeFirstOpenParenAndSpace(newValue);
		}
	    } else {
		newValue = newBody.substring(startEnd[0]);
	    }
	    newBody = newBody.substring(startEnd[1]+afterTextArea.length());
	    String currentValue = textAreaValues.get(textAreaIndex);
	    if (!newValue.trim().equals(currentValue.trim())) {
		simulateTextAreaUpdate(textAreaIndex, enhancementTextAreaName(enhancement), newValue, currentValue);
		String nameHTML = getNameHTML();
		if (!namesOfEditedMicroBehaviours.contains(nameHTML)) {
		    namesOfEditedMicroBehaviours.add(nameHTML);
		}
		edited = true;
	    }
	    textAreaIndex--;
	    if (enhancement == MicroBehaviourEnhancement.DO_IF) {
		// doesn't have a task argument
		String endOfLine = "\r";
		int endOfLineIndex = newBody.indexOf(endOfLine);
		if (endOfLineIndex >= 0) {
		    newBody = newBody.substring(endOfLineIndex+1);
		    endOfLineIndex = oldBody.indexOf(endOfLine);
		    if (endOfLineIndex >= 0) {
			oldBody = oldBody.substring(endOfLineIndex+1);
		    }
		}
	    }
	}
	if (edited) {
	    closeOutDatedBrowsePanel();
	}
	newBody = bodyOfInnermostTask(newBody);
	oldBody = bodyOfInnermostTask(oldBody);
	if (newBody.trim().equals(oldBody.trim())) {
	    return null;
	} else {
	    return applyEditsToTextAreas(newBody, oldBody, textAreasInfo, namesOfEditedMicroBehaviours);
	}
    }

    private String removeFirstOpenParenAndSpace(String code) {
	// I tried to use replaceFirst but couldn't quote the open paren in the regular expression
	// http://stackoverflow.com/questions/5010172/java-escape-parenthesis
	// suggests using Pattern.quote but that isn't available in GWT clients
	String result = "";
	int length = code.length();
	for (int i = 0; i < length; i++) {
	    char character = code.charAt(i);
	    if (character == '(') {
		// skip ( and the following space
		return result + code.substring(i+2);
	    } else {
		result += character;
	    }
	}
	return code;
    }

    public void simulateTextAreaUpdate(int textAreaIndex, String textAreaName, String newValue, String currentValue) {
	if (textAreaName.equals("netlogo-code") &&
	    (getUrl().contains("basic-library/user-input/button") || getUrl().contains("ADD-BUTTON.html"))) {
	    // button has a text area called 'recipients' which is just part of the code
	   String newRecipients = extractRecipients(newValue);
	   String currentRecipients = extractRecipients(currentValue);
	   if (newRecipients != null) {
	       newValue = newRecipients;
	   }
	   if (currentRecipients != null) {
	       currentValue = currentRecipients;
	   }
	}
	if (currentValue.equals(newValue)) {
	    return;
	}
	String urls = getAllURLs();
	Modeller.instance().updateTextArea(urls, newValue, textAreaIndex, false, true, null);
	CodeTextArea.updateTextArea(textAreaIndex, urls, getPlainName(), textAreaName, currentValue, newValue, true);
	setCopyMicroBehaviourWhenExportingURL(true);
    }
    
    private String extractRecipients(String code) {
	int askStart = code.indexOf("ask ");
	if (askStart < 0) {
	    return null;
	}
	int messageStart = code.indexOf("[", askStart);
	if (messageStart < 0) {
	    return null;
	}
	return code.substring(askStart+4, messageStart).trim();
    }

    protected void closeOutDatedBrowsePanel() {
	String url = getUrl();
	BrowsePanel openBrowsePanel = Modeller.getOpenBrowsePanel(url);
	if (openBrowsePanel != null) {
	    openBrowsePanel.removePanel();
	}
//	Modeller.browseToNewTab(url, false, null);
//	BrowsePanelCommand command = new BrowsePanelCommand() {
//
//	    @Override
//	    public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
//		panel.createDeltaPage();		    
//	    }
//
//	};
//	Modeller.executeOnNewMicroBehaviourPage(getUrl(), command, false, true);
    }
    
    public String applyEditsToTextAreas(String newBody, String oldBody, List<String> textAreasInfo, ArrayList<String> namesOfEditedMicroBehaviours) {
	// returns remainder of newBody or null if the remainders are equal
	newBody = newBody.trim();
	oldBody = oldBody.trim();
	int index = CommonUtils.firstWordDifference(newBody, oldBody);
	int textAreasCount = 0;
	HashMap<Integer, String> textAreaValues = getTextAreaValues();
	Set<Integer> keySet = textAreaValues.keySet();
	for (Integer key : keySet) {
	    if (key >= 0) {
		textAreasCount++;
	    } // ignore the negative ones since not editable in BC2NetLogo
	}
	ArrayList<Command> editCommands = new ArrayList<Command>();
	for (int i = 0; i < textAreasCount; i++) {
	    String oldValue = getTextAreaValue(i);
	    if (oldValue == null && i*2+1 < textAreasInfo.size()) {
		// never set so use default value (odd values in textAreasInfo)
		oldValue = textAreasInfo.get(i*2+1);
	    }
	    if (oldValue == null) {
		Utils.logServerMessage(Level.WARNING, "Text area #" + i + " is null in " + getUrl());
		continue;
	    }
	    String oldValueRegularExpression = toRegularExpression(oldValue.trim());
	    int oldValueStartEnd[] = regularExpressionIndexOf(oldBody, oldValueRegularExpression, index);
	    while (oldValueStartEnd == null) {
		if (index > 1) {
		    // perhaps first difference was previous space
		    index = newBody.lastIndexOf(' ', index-1);
		    oldValueStartEnd = regularExpressionIndexOf(oldBody, oldValueRegularExpression, Math.max(0, index));
		} else {
		    break;
		}
	    }
	    if (oldValueStartEnd == null && index <= 1) {
		if (editCommands.isEmpty()) {
		    Utils.logServerMessage(Level.WARNING, oldValue + " not found in " + oldBody 
			                   + " with index " + index + " using regular expression " + oldValueRegularExpression);
		}
		continue;
	    }	
//	    if (valueStart > index) {
//		System.out.println("Difference between " + newBody + " and " + oldBody + " found where there is no text area.");
//		continue;
//	    }
	    int oldValueStart = oldValueStartEnd[0];
	    int oldValueEnd = oldValueStartEnd[1];
	    String oldBodyAfterValue = oldBody.substring(oldValueEnd);
	    int newValueEnd;
	    if (i+1 == textAreasCount) {
		// last text area so entire oldBodyAfterValue should occur at the end of newBody
		newValueEnd = newBody.lastIndexOf(oldBodyAfterValue);
	    } else {
		String nextOldValue = getTextAreaValue(i+1);
		if (nextOldValue == null) {
		    continue;
		}
		String nextOldValueRegularExpression = toRegularExpression(nextOldValue.trim());
		int nextValueStartEnd[] = regularExpressionIndexOf(oldBody, nextOldValueRegularExpression, 0);
		if (nextValueStartEnd == null) {
		    // error will be handled when i is incremented
		    continue;
		}
		int nextValueStart = nextValueStartEnd[0];			
		if (oldValueEnd >= nextValueStart) {
		    // not using the right text area above
		    continue;
		}
		String oldBodyBetweenAreas = oldBody.substring(oldValueEnd, nextValueStart);
		newValueEnd = newBody.indexOf(oldBodyBetweenAreas, oldValueStart);
//		if (newValueEnd >= 0) {
//		    // change index from beginning of newBody
//		    newValueEnd += nextValueStart;
//		}
	    }
	    if (newValueEnd < 0) {
		Utils.logServerMessage(Level.WARNING, "Expected " + newBody + " to end with " + oldBodyAfterValue);
		continue;
	    }
	    String nameHTML = getNameHTML();
	    if (!namesOfEditedMicroBehaviours.contains(nameHTML)) {
		namesOfEditedMicroBehaviours.add(nameHTML);
	    }
	    final String newValue = newBody.substring(oldValueStart, newValueEnd);
	    if (newValue.equals(oldValue)) {
		continue; // no need to change anything
	    }
	    final int textAreaIndex = i;
	    final String finalOldValue = oldValue;
	    final String textAreaName = textAreasInfo.get(i*2);
	    Command command = new Command() {

		@Override
		public void execute() {
		    simulateTextAreaUpdate(textAreaIndex, textAreaName, newValue, finalOldValue);
		}

	    };
	    editCommands.add(command);    
	    newBody = newBody.substring(newValueEnd).trim();
	    oldBody = oldBodyAfterValue.trim();
	    index = CommonUtils.firstWordDifference(newBody, oldBody);
	}
	if (editCommands.isEmpty()) {
	    Modeller.setAlertsLine(Modeller.constants.editsFromNetLogoNotApplied());
	} else {
	    for (Command command : editCommands) {
		command.execute();
	    }
	    closeOutDatedBrowsePanel();
	}
	return newBody;	
    }
    
    private int[] regularExpressionIndexOf(String string, String regularExpression, int startIndex) {
	if (string == null || startIndex >= string.length()) {
	    return null;
	}
	int startEnd[] = new int[2];
	String substring = string.substring(startIndex);
	int initialSpaces = 0;
	while (substring.charAt(0) == ' ') {
	    initialSpaces++;
	    substring = substring.substring(1);
	}
	String[] split = substring.split(regularExpression, 2);
	if (split.length == 2) {
	    startEnd[0] = split[0].length()+initialSpaces+startIndex;
	    startEnd[1] = string.lastIndexOf(split[1]);
	    return startEnd;
	} else {
	    return null;
	}
    }

    private String toRegularExpression(String s) {
	// since matching code may have been pretty printed need to create regular expression using \s
	final String anyWhiteSpace = "[\\s]*"; // TODO: determine if it should be * or + ??
	String regularExpression = "";
	for (int i = 0; i < s.length(); i++) {
	    char character = s.charAt(i);
	    if (character == ' ') {
		regularExpression += anyWhiteSpace;
	    } else if (character == '[') {
		regularExpression += "\\[" + anyWhiteSpace;
	    } else if (character == ']') {
		regularExpression += anyWhiteSpace + "\\]";
	    } else if (character == '(') {
		regularExpression += "\\(" + anyWhiteSpace;
	    } else if (character == ')') {
		regularExpression += anyWhiteSpace + "\\)";
	    } else if ((character >= '0' && character <= '9') ||
	               (character >= 'a' && character <= 'z') ||
	               (character >= 'A' && character <= 'Z'))  {
		// don't want to quote it
	        regularExpression += character;
	    } else {
		// need to quote non alpha-numeric
		regularExpression += "\\" + character;
	    }
	}
	return regularExpression;
    }

    public void updateNetLogoWidget(com.google.gwt.xml.client.Element element, ArrayList<String> namesOfEditedMicroBehaviours) {	
	com.google.gwt.xml.client.NodeList childNodes = element.getChildNodes();
	int length = childNodes.getLength();
	List<Integer> skipTextAreaIndices = skipTextAreaIndices();
	List<Integer> skipTextAreas = skipTextAreas();
	int textAreaIndex = 0;
	for (int i = 0; i < length; i++) {
	    while (skipTextAreaIndices.contains(textAreaIndex)) {
		textAreaIndex++;
	    }
	    Node node = childNodes.item(i);
	    if (node instanceof com.google.gwt.xml.client.Element) {
		com.google.gwt.xml.client.Element change = (com.google.gwt.xml.client.Element) node;
		if (skipTextAreas.contains(i)) {
		    textAreaIndex--;
		} else {
		    simulateTextAreaUpdate(textAreaIndex, change.getTagName(), change.getAttribute("newValue"), change.getAttribute("oldValue"));
		}
	    }
	    textAreaIndex++;
	}
	String nameHTML = getNameHTML();
	if (!namesOfEditedMicroBehaviours.contains(nameHTML)) {
	    namesOfEditedMicroBehaviours.add(nameHTML);
	}
	closeOutDatedBrowsePanel();
    }

    private List<Integer> skipTextAreas() {
	ArrayList<Integer> skip = new ArrayList<Integer>();
	if (getUrl().contains("create-auto-plot") || getUrl().contains("CREATE-AUTO-PLOT.html") ||
	    getUrl().contains("create-empty-auto-plot") || getUrl().contains("CREATE-EMPTY-AUTO-PLOT.html")) {
	    // skip the code that isn't part of NetLogo's widget format
	    skip.add(7);
	    skip.add(8);
	    skip.add(9);
	    skip.add(10);
	}
	return skip;
    }

    private List<Integer> skipTextAreaIndices() {
	ArrayList<Integer> skip = new ArrayList<Integer>();
	if (getUrl().contains("create-plot") || 
	    getUrl().contains("create-auto-plot") ||
	    getUrl().contains("create-histogram") || 
	    getUrl().contains("CREATE-PLOT.html") ||
	    getUrl().contains("CREATE-AUTO-PLOT.html") ||
	    getUrl().contains("CREATE-HISTOGRAM.html")) {
	    // skip the code that isn't part of NetLogo's widget format
	    skip.add(7);
	    skip.add(8);
	}
	return skip;
    }

    private String bodyOfInnermostTask(String code) {
	int lastTaskStart = code.lastIndexOf("task [");
	if (lastTaskStart < 0) {
	    return code;
	}
	int lastTaskEnd = code.indexOf("]", lastTaskStart);
	if (lastTaskEnd < 0) {
	    return code;
	}
	// TODO: compensate for [] inside the body
	return code.substring(lastTaskStart+6, lastTaskEnd);
    }

//    private String removeTask(String newBody) {
//	int[] startEnd = startEnd(newBody, "task [", "]");
//	if (startEnd == null) {
//	    return newBody;
//	} else {
//	    return newBody.substring(startEnd[0], startEnd[1]);
//	}
//    }

    public static int[] startEnd(String string, String before, String after) {
	if (before == null || after == null) {
	    return null;
	}
	int startIndex = string.indexOf(before);
	if (startIndex < 0) {
	    return null;
	}
	startIndex += before.length();
	int endIndex = string.indexOf(after, startIndex);
	if (endIndex < 0) {
	    return null;
	}
	int startEnd[] = new int[2];
	startEnd[0] = startIndex;
	startEnd[1] = endIndex;
	return startEnd;
    }
    
    public List<String> getPreviousURLs() {
	return sharedState.getPreviousURLs();
    }

    public boolean containsURL(String url, boolean withoutChangesGuid) {
	return sharedState.containsURL(url, withoutChangesGuid);
    }

    public void updateTextAreaValues() {
	BrowsePanel browsePanel =  Modeller.instance().getMainTabPanel().getBrowsePanelWithURL(getUrl());
	if (browsePanel != null) {
	    browsePanel.updateTextAreaValues();
	}	
    }
    
    public void fetchAndUpdateTextAreas() {
	final AsyncCallbackNetworkFailureCapable<HashMap<Integer, String>> callback = 
		new AsyncCallbackNetworkFailureCapable<HashMap<Integer, String>>() {

	    @Override
	    public void onFailure(Throwable caught) {
		NetworkFailure.instance().networkFailure(caught, Modeller.constants.fetchingMicroBehaviourUpdates(), getTimer());
	    }

	    @Override
	    public void onSuccess(HashMap<Integer, String> textAreaUpdates) {
		super.onSuccess(textAreaUpdates);
		if (textAreaUpdates != null) {
		    String error = textAreaUpdates.get(-2);
		    if (error != null) {
			Modeller.addToErrorLog(error);
		    } else {
			Set<Entry<Integer, String>> entrySet = textAreaUpdates.entrySet();
			int originalTextAreasCount = getOriginalTextAreasCount();
			for (Entry<Integer, String> entry : entrySet) {
			    Integer index = entry.getKey();
			    String value = entry.getValue();
			    if (index >= originalTextAreasCount || // a check if already updated
				getTextAreaValue(index) == null) {
				if (index == -1) {
				    setNameHTMLAndDescription(value);
				    // and make sure micro-behaviour is in synch
				    updateTextArea(value, index);
				}
				updateTextArea(value, index);
			    }
			}
		    }
		}
	    }
	};
	BrowsePanel.fetchAndUpdateTextAreas(this, callback);
    }
    
    public boolean walkMicroBehaviourViews(MicroBehaviourComand command) {
	return command.execute(this);
    }

    public void openTabPanel(CustomisationPopupPanel customisationPanel, boolean inCustomisationPanel, final boolean switchTo) {
	if (isDirty() && inCustomisationPanel) {
	    // create a delta page before reopening as a tab
	    CreateDeltaPageCommand createDeltaPageCommand = new CreateDeltaPageCommand() {
		@Override
		public void execute(MicroBehaviourView microBehaviour, DeltaPageResult deltaPageResult, boolean panelIsNew, boolean subMicroBehavioursNeedNewURLs, boolean forCopying) {
		    super.execute(microBehaviour, deltaPageResult, panelIsNew, subMicroBehavioursNeedNewURLs, forCopying);
		    microBehaviour.openInBrowsePanel(switchTo);
		}

	    };
	    customisationPanel.getBrowsePanel().createDeltaPage(MicroBehaviourView.this, createDeltaPageCommand, false, false);
	} else {
	    reopenCustomisationPanelAsTab();
	}
	if (customisationPanel != null) {
	    customisationPanel.setBrowsePanelAboutToBeReopened(true);
	    customisationPanel.hide();
	    customisationPanel.setBrowsePanelAboutToBeReopened(false);
	}
    }

}
