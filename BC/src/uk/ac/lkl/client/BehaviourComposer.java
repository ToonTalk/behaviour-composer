
package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import uk.ac.lkl.client.composer.AttributesDisplay;
import uk.ac.lkl.client.composer.MacroBehaviourAsMicroBehaviourView;
import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.event.ActivateMacroBehaviourEvent;
import uk.ac.lkl.client.event.AddMacroBehaviourEvent;
import uk.ac.lkl.client.event.CompoundEvent;
import uk.ac.lkl.client.event.EnhanceMicroBehaviourEvent;
import uk.ac.lkl.client.event.InactivateMacroBehaviourEvent;
import uk.ac.lkl.client.event.LoadModelEvent;
import uk.ac.lkl.client.event.ModellerEvent;
import uk.ac.lkl.client.event.ReconstructEventsContinutation;
import uk.ac.lkl.client.event.RemoveLastEnhancementMicroBehaviourEvent;
import uk.ac.lkl.client.event.RemoveMacroBehaviourEvent;
import uk.ac.lkl.client.event.RenameMacroBehaviourEvent;
import uk.ac.lkl.client.event.RenameMicroBehaviourEvent;
import uk.ac.lkl.client.event.ReplaceURLEvent;
import uk.ac.lkl.client.event.SessionEventsCheckBoxToggledEvent;
import uk.ac.lkl.client.event.StartEvent;
import uk.ac.lkl.client.event.AddToModelListBoxMacroBehaviourEvent;
import uk.ac.lkl.client.event.SwapPrototypesEvent;
import uk.ac.lkl.client.event.UpdateNumberOfInstancesTextAreaEvent;
import uk.ac.lkl.client.event.UpdateTextAreaEvent;
import uk.ac.lkl.client.event.VisibleInModelEvent;
import uk.ac.lkl.shared.CommonUtils;
import uk.ac.lkl.shared.DeltaPageResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

/**
 * BehaviourComposer-specific main GWT application
 * 
 * 
 * @author Ken Kahn
 *
 */
public class BehaviourComposer extends Modeller {
    
    public static final String FIXED_TAB_COLOR = "Navy";
    private static final int EXTRA_APPLET_HEIGHT = 70;
    private static final int EXTRA_APPLET_WIDTH = 30;
    protected static HorizontalPanel commandBar = new HorizontalPanel();
    protected static SimplePanel specialAlert = new SimplePanel();
    protected static CommandBarPanel commandBarPanel = new CommandBarPanel();
    public static ModellerTabPanel resourcesTabPanel;
    public static ScrollPanelInTabPanel composerPanel = null;
    final public static Anchor behaviourComposerLink = new CommandAnchor(constants.behaviourComposer());
    public static VerticalPanel composerPanelContents = null;
    public static FlowPanel prototypesPanel = null;
    public static ModelsPanel modelsPanel = null;
    final public static Anchor modelsLink = new CommandAnchor(constants.yourModels());
    public static MicroBehaviourView microBehaviourWaitingToBeAdded = null;
    public static RunPanel runPanel = new RunPanel();
    public static RunDownloadSharePanel downloadPanel = new DownloadPanel();
    public static RunDownloadSharePanel sharePanel = new RunDownloadSharePanel(false, true);
    public static InfoPanel infoPanel = new InfoPanel();
    public static TabLabel downloadTabLabel;
    public static TabLabel shareTabLabel;
    private static TabLabel infoTabLabel;
    public static TabLabel runTabLabel;
    public static String initialModelID = null;
    private static int tabsEnabledCount = 0;
    private static CheckBox advancedCheckBox;
    private ModellerButton addPrototypeButton;
    private ModellerButton addNewMicroBehaviour;
    private ModellerButton addNewResource;
    private HTML spaceBetweenNewPrototypeButtonAndNewMicroBehaviourButton;
    private HTML spaceBetweenNewMicroBehaviourButtonAndListAttributesButton;
    // could put this in a subclass with other stuff
    private int numberOfSlidersInEpidemicGame = 0;
    private static boolean okToAuthorLocalResources = "1".equals(Window.Location.getParameter("author-local-resources"));
    
    protected ArrayList<SessionEventsCheckBox> allEnabledCheckBoxes =
	new ArrayList<SessionEventsCheckBox>();
    private int fixedTabCount = 0;
    private String originalURL;
    private HTML addNewResourceSpace;
    private HTML importPrototypesSpace;
    private long runModelStartTime;
    private long runModelSucceededTime;
    private static boolean interfaceEnabled = true;
    public static final String SETTINGS_ANCHOR = "#_Settings_";
    public static final String SETTINGS_PARAMETER = "start=settings";
    public static final String HISTORY_ANCHOR = "#_History_";
    public static final String HISTORY_PARAMETER = "start=history";
    public static final String COMPOSER_ANCHOR = "#_BehaviourComposer_";
    public static final String COMPOSER_PARAMETER = "start=Composer";
    public static final String HELP_ANCHOR = "#_Help_";
    public static final String HELP_PARAMETER = "start=help";
    public static final String SEARCH_ANCHOR = "#_Search_"; // not yet supported
    public static final String RESOURCES_ANCHOR = "#_Resources_";
    public static final String RESOURCES_PARAMETER = "start=resources";
    public static final String MODELS_ANCHOR = "#_Models_";
    public static final String MODELS_PARAMETER = "start=models";
    
    public BehaviourComposer() {
	super();
	// new default to combine Resources and Composer
	resourcesTabPanel = mainTabPanel;
	originalURL = Location.getHref();
    }
    
    public static ComposerResources resources() {
	return (ComposerResources) GWT.create(ComposerResources.class);
    }
   
    @Override
    protected void configure() {
	if (epidemicGameMakerMode()) {
	    applicationTitle = constants.epidemicGameMaker();
	} else {
	    applicationTitle = constants.behaviourComposer();
	}
	Window.setTitle(applicationTitle);
	defaultManualPage = "MB.4/doc.html";
	defaultExplorerPage = "http://resources.modelling4all.org/libraries/basic-library";
	resourceFolderName = "/Composer/";
    }
    
    @Override
    protected void createInitialContents(boolean restoringHistory, boolean failedToFetchHistory) {
	// do following before super so these tabs can get a 'disabled' CSS style
	boolean gameMaker = epidemicGameMakerMode();
	String clickToRunThisModel = 
		gameMaker ? constants.clickToMakeYourGame() : constants.clickToRunThisModel();
	String runLabel = gameMaker ? CommonUtils.emphasise(constants.playYourGame(), FIXED_TAB_COLOR) : constants.runTabLabel();
	runTabLabel = new TabLabel(runLabel);
	runTabLabel.setTitle(clickToRunThisModel);
	if (Modeller.instance().getNetLogo2BCChannelToken() == null) {
	    downloadTabLabel = new TabLabel(constants.downloadTabLabel());
	    downloadTabLabel.setTitle(constants.clickToDownloadThisModel());
	} else {
	    String label = gameMaker ? CommonUtils.emphasise(constants.sendToNetLogoLabelEGM(), FIXED_TAB_COLOR) : constants.sendToNetLogoLabel();
	    downloadTabLabel = new TabLabel(label);
	    downloadTabLabel.setTitle(constants.clickToSendTheModelToNetLogo());
	}
	shareTabLabel = new TabLabel(constants.shareTabLabel());
	shareTabLabel.setTitle(constants.clickToShareThisModel());
	infoTabLabel = new TabLabel(constants.infoTabLabel());
	infoTabLabel.setTitle(constants.clickForInfoTab());
	super.createInitialContents(restoringHistory, failedToFetchHistory);
	// here for now -- could be made to work in MoPiX too
	collaborateButton = new CheckBox(constants.collaborateInRealTime());
	collaborateButton.setTitle(constants.useThisWhenSharingASession());
	collaborateButton.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		Boolean checked = collaborateButton.getValue();
		setKeepCheckingForNewEvents(checked);
		if (checked) {
		    StringBuffer newURL = new StringBuffer(computeNewURL(false));
		    CommonUtils.removeAttributeFromURL("user", newURL);
		    String collaborationURL = CommonUtils.addAttributeToURL(newURL.toString(), "collaborate", "1");
		    collaborationURLHTML = new HTML("&nbsp;&nbsp;" + Modeller.constants.shareThisURLWithYourCollaborators() + 
			                            "<br>&nbsp;&nbsp;&nbsp;&nbsp;" + collaborationURL);
		    optionsPanel.insert(collaborationURLHTML, optionsPanel.getWidgetIndex(collaborateButton)+1);
		} else if (collaborationURLHTML != null) {
		    collaborationURLHTML.removeFromParent();
		}
		// collecting the name here is a big job, not much payoff, and perhaps confusing
//		if (Modeller.instance().getGivenName() == null) {
//		    // let them add their name
//		}
	    }

	});
	if (getNetLogo2BCChannelToken() != null) {
	    collaborateButton.setEnabled(false);
	    collaborateButton.setTitle(constants.collaborationCurrentlyNotPossibleWhenConfiguredToRunNetLogoDirectly());
	}
	enableEditorButton = new CheckBox(constants.enablePageEditor());
	enableEditorButton.setTitle(constants.enableEditorPageButtonTitle());
	enableEditorButton.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		pageEditingEnabled = enableEditorButton.getValue();  
	    }

	});
	enableLocalResourceEditingButton = new CheckBox(constants.enableResourceEditor());
	enableLocalResourceEditingButton.setTitle(constants.enableResourceEditorPageButtonTitle());
	enableLocalResourceEditingButton.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		Boolean enabled = enableLocalResourceEditingButton.getValue();
		BehaviourComposer.setOkToAuthorLocalResources(enabled);
		BehaviourComposer.instance().getAddNewResource().setVisible(enabled);
		BehaviourComposer.instance().getAddNewResourceSpace().setVisible(enabled);
	    }

	});
	optionsPanel.setSpacing(6);
	optionsPanel.add(createReloadWithoutHistory());
	optionsPanel.add(webNetLogoVersionOption());
	optionsPanel.add(collaborateButton);
//	optionsPanel.add(splitScreenCheckBox);
	optionsPanel.add(enableEditorButton);
	optionsPanel.add(enableLocalResourceEditingButton);
	optionsPanel.add(createOldVersionOption());
//	optionsPanel.add(createExportVersionOption());
	if (gameMaker) {
	    advancedCheckBox = new CheckBox(constants.advancedMode());
	    advancedCheckBox.setTitle(constants.advancedModeTitle());
	    ClickHandler clickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
		    setAdvancedMode(advancedCheckBox.getValue());
		}
		
	    };
	    advancedCheckBox.addClickHandler(clickHandler);
	    wholePanel.add(advancedCheckBox);
	    switchToConstructionArea();
	    setAdvancedMode(false);
	}
    }

    /**
     * if the original URL ends with special tokens or contains a 'start' parameter then
     * switch to the appropriate panel
     * Both anchors and parameters are supported since anchors are sometimes stripped away
     * e.g. by VLEs while ANCHORS are used as relative anchors (bookmarks)
     */
    @Override
    public void switchToTabIfIndicatedInURL() {
	if (modelsPanel != null && (originalURL.contains(MODELS_ANCHOR) || originalURL.contains(MODELS_PARAMETER))) {
	    switchTo(modelsPanel, modelsLink);
	} else if (originalURL.contains(RESOURCES_ANCHOR) || originalURL.contains(RESOURCES_PARAMETER)) {
	    switchToResourcesPanel();
	} else if (originalURL.contains(HELP_ANCHOR) || originalURL.contains(HELP_PARAMETER)) {
	    switchTo(manualPanel, helpLink);
	} else if (originalURL.contains(COMPOSER_ANCHOR) || originalURL.contains(COMPOSER_PARAMETER)) {
	    switchTo(resourcesTabPanel, behaviourComposerLink);
	    resourcesTabPanel.switchTo(composerPanel);
	} else if (originalURL.contains(HISTORY_ANCHOR) || originalURL.contains(HISTORY_PARAMETER)) {
	    switchTo(historyPanel, modelHistoryLink);
	} else if (originalURL.contains(SETTINGS_ANCHOR) || originalURL.contains(SETTINGS_PARAMETER)) {
	    switchTo(optionsPanel, settingsLink);
	}
    }
    
    @Override
    public void setAdvancedMode(boolean enabled) {
	if (advancedMode == null || enabled != isAdvancedMode()) {
	    super.setAdvancedMode(enabled);
	    commandBarPanel.setVisible(enabled);
	    if (advancedCheckBox != null) {
		advancedCheckBox.setValue(enabled);
	    }
	    if (addPrototypeButton != null) {
		addPrototypeButton.setVisible(enabled);
	    }
	    if (spaceBetweenNewMicroBehaviourButtonAndListAttributesButton != null) {
		spaceBetweenNewMicroBehaviourButtonAndListAttributesButton.setVisible(enabled);
	    }
	    if (spaceBetweenNewPrototypeButtonAndNewMicroBehaviourButton != null) {
		spaceBetweenNewPrototypeButtonAndNewMicroBehaviourButton.setVisible(enabled);
	    }
	    if (addNewMicroBehaviour != null) {
		addNewMicroBehaviour.setVisible(enabled);
	    }
//	    if (downloadButton != null) {
//		downloadButton.setVisible(enabled);
//	    }
//	    if (shareButton != null) {
//		shareButton.setVisible(enabled);
//	    }
//	    if (composerTabPanel != null) {
//		composerTabPanel.getTabBar().setVisible(enabled);
//	    }
	    if (enabled) {
//		composerTabPanel = resourcesTabPanel;
		resourcesTabPanel.insert(downloadPanel, downloadTabLabel, Math.min(resourcesTabPanel.getWidgetCount()-1, fixedTabCount++));
		resourcesTabPanel.insert(sharePanel, shareTabLabel, Math.min(resourcesTabPanel.getWidgetCount()-1, fixedTabCount++));
		resourcesTabPanel.insert(infoPanel, infoTabLabel, Math.min(resourcesTabPanel.getWidgetCount()-1, fixedTabCount++));
	    } else {
		resourcesTabPanel.remove(downloadPanel);
		resourcesTabPanel.remove(sharePanel);
		resourcesTabPanel.remove(infoPanel);
		BrowsePanel browsePanel = resourcesTabPanel.getBrowsePanelWithURL(explorerPage);
		if (browsePanel != null) {
		    browsePanel.removeFromParent();
		}
		if (fixedTabCount >= 3) {
		    fixedTabCount -= 3; // removed three fixed tabs
		}
		// old scheme
//		composerTabPanel = new ModellerTabPanel();
	    }
	    setVisibilitiyOfEpidemicGameMakerSupportPrototypes(enabled);
	}
    }
    
    @Override
    public void switchToConstructionArea() {	
	switchTo(resourcesTabPanel, behaviourComposerLink);
	resourcesTabPanel.switchTo(composerPanel);	
    }
    
    @Override
    public void switchToErrorsArea() {
	switchTo(resourcesTabPanel, behaviourComposerLink);
	resourcesTabPanel.switchTo(errorsPanel);
    }
    
    @Override
    protected void startNewSession() {
	super.startNewSession();
	// start a new session -- on success will reload the URL with the session ID
	new StartEvent(null, initialReadOnlySessionID, initialModelID).addToHistory();
    }
    
    @Override
    protected void reloadWithSessionID(String newURL) {
	if (needToReload) {
	    newURL = CommonUtils.addAttributeToURL(newURL, CommonUtils.M4A_MODEL_URL_PARAMETER, "1");
	    System.out.println(newURL); // helpful during development
	    Window.Location.replace(newURL);
	}
    }

    @Override
    protected String computeNewURL(boolean defaultNeedToReload) {
	// could enhance this to send to server contents of error panel
	// and then restore it upon reload
	if (sessionGuid == null) return null;
	needToReload = defaultNeedToReload;
	String currentURL = Window.Location.getHref();
	StringBuffer newURL = new StringBuffer(currentURL);
	int sharpIndex = currentURL.indexOf('#');
	if (sharpIndex >= 0) {
	    newURL.replace(sharpIndex, newURL.length(), "");
	}
	CommonUtils.removeAttributeFromURL("copy", newURL);
	CommonUtils.removeAttributeFromURL("frozen", newURL);
	CommonUtils.removeAttributeFromURL("model", newURL); // old name
	int sessionIndex = newURL.indexOf("share=");
	int sessionIndexEnd;
	if (sessionIndex < 0) { // try again with old name
	    sessionIndex = newURL.indexOf("session=");
	    if (sessionIndex >= 0) {
		sessionIndexEnd = sessionIndex + "session=".length();
	    } else {
		sessionIndexEnd = -1;
	    }
	} else {
	    sessionIndexEnd = sessionIndex + "share=".length();
	}
	if (sessionIndexEnd >= 0 && 
            (sessionIndexEnd == newURL.length() || newURL.charAt(sessionIndexEnd) == '&')) {
	    // i.e. URL ends with share= or contains share=&...
	    newURL.insert(sessionIndexEnd, sessionGuid);
	    needToReload = true;
	} else if (sessionIndex < 0) {
	    if (newURL.indexOf("?") < 0) {
		newURL.append("?share=" + sessionGuid);
	    } else {
		newURL.append("&share=" + sessionGuid);
	    }
	    needToReload = true;
	} else {
	    String shareNewAttribute = "share=new";
	    int newIndex = newURL.indexOf(shareNewAttribute);
	    if (newIndex < 0) { // try again with old name
		shareNewAttribute = "session=new";
		newIndex = newURL.indexOf(shareNewAttribute);
	    }
	    if (newIndex >= 0) {
		if (sessionGuidToBeReplaced == null) {
		    newURL.replace(sessionIndex, sessionIndex + shareNewAttribute.length(), "share=" + sessionGuid);
		} else {
		    newURL.replace(sessionIndex, sessionIndex + shareNewAttribute.length(), "share=" + sessionGuidToBeReplaced);
		    CommonUtils.removeAttributeFromURL("sessionGuidToBeReplaced", newURL);
		}
		needToReload = true;
	    }
	}
	int userIndex = newURL.indexOf("user=");
	final String userNewAttribute = "user=new";
	int userNewIndex = newURL.indexOf(userNewAttribute);
	if (userIndex < 0) {
	    if (newURL.indexOf("?") < 0) {
		newURL.append("?user=" + userGuid);
	    } else {
		newURL.append("&user=" + userGuid);
	    }
	    needToReload = true;
	} else if (userNewIndex >= 0) {
	    newURL.replace(userIndex, userIndex + userNewAttribute.length(), "user=" + userGuid);
	    needToReload = true;
	}
	String tabURL = CommonUtils.extractTabAttribute(originalURL);
	if (tabURL != null && !tabURL.isEmpty()) {
	    newURL.append("&tab=" + tabURL);
	}
	return newURL.toString();
    }
    
    @Override
    protected void addModel(String data, boolean removeOld) {
	loadModel(data, false, removeOld, null, null, false, false);
    }
    
    public static BehaviourComposer instance() {
	return (BehaviourComposer) INSTANCE;
    }
    
    @Override
    public void reconstructEvent(
	    String tag,
	    Element eventElement,
	    boolean restoringHistory,
	    int whatToIgnore,
	    boolean copyOnUpdate,
	    ReconstructEventsContinutation continuation, 
	    int version,
	    String name) {
	boolean justRecord = whatToIgnore == Modeller.IGNORE_START_AND_LOAD_EVENTS_AND_DONT_RECONSTRUCT;
	if (tag.equals("AddMicroBehaviourEvent") ||
	    tag.equals("RemoveMicroBehaviourEvent") ||
	    tag.equals("ActivateMicroBehaviourEvent") ||
	    tag.equals("InactivateMicroBehaviourEvent") ||
	    tag.equals("MoveMicroBehaviourEvent") ||
	    tag.equals("EditMicroBehaviourEvent")) {
	    ModellerEvent.reconstruct(tag, name, eventElement, restoringHistory, copyOnUpdate, justRecord, version, continuation);
	} else if (tag.equals("AddMacroBehaviourEvent")) {
	    AddMacroBehaviourEvent.reconstruct(name, eventElement, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("RemoveMacroBehaviourEvent")) {
	    RemoveMacroBehaviourEvent.reconstruct(name, eventElement, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("RenameMacroBehaviourEvent")) {
	    RenameMacroBehaviourEvent.reconstruct(name, eventElement, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("RenameMicroBehaviourEvent")) {
	    RenameMicroBehaviourEvent.reconstruct(
		    eventElement, 
		    restoringHistory, 
		    copyOnUpdate,
		    justRecord,
		    version, 
		    continuation);
	} else if (tag.equals("ActivateMacroBehaviourEvent")) {
	    ActivateMacroBehaviourEvent.reconstruct(name, eventElement, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("InactivateMacroBehaviourEvent")) {
	    InactivateMacroBehaviourEvent.reconstruct(name, eventElement, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("EnhanceMicroBehaviourEvent")) {
	    EnhanceMicroBehaviourEvent.reconstruct(name, eventElement, restoringHistory, copyOnUpdate, justRecord, version, continuation); 
	} else if (tag.equals("RemoveLastEnhancementMicroBehaviourEvent")) {
	    RemoveLastEnhancementMicroBehaviourEvent.reconstruct(name, eventElement, restoringHistory, copyOnUpdate, justRecord, version, continuation); 
	} else if (tag.equals("UpdateTextAreaEvent")) {
	    UpdateTextAreaEvent.reconstruct(name, eventElement, restoringHistory, copyOnUpdate, justRecord, version, continuation); 
	} else if (tag.equals("UpdateNumberOfInstancesTextAreaEvent")) {
	    UpdateNumberOfInstancesTextAreaEvent.reconstruct(name, eventElement, restoringHistory, justRecord, version, continuation);   
	} else if (tag.equals("VisibleInModelEvent")) {
	    VisibleInModelEvent.reconstruct(name, eventElement, restoringHistory, justRecord, version, continuation);   
	} else if (tag.equals("AddToModelListBoxMacroBehaviourEvent")) {
	    AddToModelListBoxMacroBehaviourEvent.reconstruct(name, eventElement, restoringHistory, justRecord, version, continuation);   
	} else if (tag.equals("SessionEventsCheckBoxToggledEvent")) {
	    SessionEventsCheckBoxToggledEvent.reconstruct(eventElement, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("SwapPrototypesEvent")) {
	    SwapPrototypesEvent.reconstruct(eventElement, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("ReplaceURLEvent")) {
	    ReplaceURLEvent.reconstruct(eventElement, restoringHistory, justRecord, version, continuation);
	} else if (tag.equals("undo")) {
	    instance().undoLast(false, justRecord, continuation);
	    //		History.back(); // also change the browser's idea of where it is in the history
	} else if (tag.equals("redo")) {
	    instance().redoLast(false, justRecord, continuation);
	    //		History.forward(); // also change the browser's idea of where it is in the history
	} else if (tag.equals("StartEvent")) {
	    if (whatToIgnore == Modeller.IGNORE_NO_EVENTS) {
		StartEvent.reconstruct(
			eventElement, 
			restoringHistory, 
			version, 
			continuation);
	    } else if (continuation != null) {
		continuation.reconstructSubsequentEvents(null);
	    }
	} else if (tag.equals("LoadModelEvent")) {
	    if (whatToIgnore == Modeller.IGNORE_NO_EVENTS) {
		LoadModelEvent.reconstruct(eventElement, restoringHistory, justRecord, version, continuation);	
	    } else if (continuation != null) {
		continuation.reconstructSubsequentEvents(null);
	    }
	} else {
	    setStatusLineHTMLError("The following event not recognised: " + tag);
	}
    }
    
    private void createBehaviourComposerPanel() {
	composerPanelContents = new VerticalPanel();
	composerPanelContents.setSpacing(2);
	prototypesPanel = new FlowPanel();
	// perhaps MacroBehaviourView should have a border/margin
//	prototypesPanel.setSpacing(3);
	composerPanel = new ScrollPanelInTabPanel(composerPanelContents);
//	composerPanel.setHeight(Utils.getFullWidth() + "px");
	if (epidemicGameMakerMode()) {
	    composerButtonPanel.setSpacing(6);
	    setAdvancedMode(false);
	} else {
	    composerButtonPanel.setSpacing(3);
	}
	addPrototypeButton = new ModellerButton(constants.addPrototype());
	addPrototypeButton.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		MacroBehaviourView macroBehaviour = createMacroBehaviour();
		addMacroBehaviour(macroBehaviour);
		new AddMacroBehaviourEvent(macroBehaviour).addToHistory();
		if (microBehaviourWaitingToBeAdded != null) {
		    macroBehaviour.addMicroBehaviourCursor();
		}
	    }
	});
	addPrototypeButton.setTitle(constants.clickToCreateANewPrototype());
	composerButtonPanel.add(addPrototypeButton);
	composerButtonPanel.add(new HTML(NON_BREAKING_SPACE + NON_BREAKING_SPACE));
	addNewResource = new ModellerButton(constants.createNewResource());
	addNewResource.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		NewMicroBehaviourBrowsePanel browsePanel = new NewMicroBehaviourBrowsePanel();
		Modeller.setAlertsLine(Modeller.constants.editTheTextThenClickSave());
		Widget widget = browsePanel.createAndSwitchToEditor(true, false, true);
		if (widget instanceof RichTextEntry) {
		    ClickHandler addHandler = new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
			    Modeller.setAlertsLine(Modeller.constants.newResourceBeingSaved());
			}

		    };
		    RichTextEntry richTextEntry = (RichTextEntry) widget;
		    richTextEntry.addSaveButtonClickHandler(addHandler);
		    // leave a little for scroll bars, etc.
		    String width = Modeller.instance().getMainTabPanelWidth()-100 + "px";
		    richTextEntry.getRichTextArea().setWidth(width);
		}
	    }
	});
	addNewResource.setTitle(constants.clickToCreateNewResource());
	composerButtonPanel.add(addNewResource);
	addNewResourceSpace = new HTML(NON_BREAKING_SPACE + NON_BREAKING_SPACE);
	composerButtonPanel.add(addNewResourceSpace);
	addNewResource.setVisible(isOkToAuthorLocalResources());
	addNewResourceSpace.setVisible(isOkToAuthorLocalResources());
	Button listAttributesButton = new Button(constants.listAttributes());
	listAttributesButton.setTitle(constants.listAttributesTitle());
	ClickHandler listAttributesHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		final ArrayList<MicroBehaviourView> allMicroBehaviours = getAllMicroBehaviours();
		if (Modeller.isCachingEnabled() && Modeller.isInternetAccess() && !allMicroBehaviours.isEmpty()) {
		    // ensure these are cached
		    String urls[] = new String[allMicroBehaviours.size()];
		    int index = 0;
		    for (MicroBehaviourView microBehaviourView : allMicroBehaviours) {
			urls[index] = microBehaviourView.getUrl();
			index++;
		    }
		    AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
			    Utils.popupMessage("Connection problem prevented cachng micro behaviours.", true);
			}

			public void onSuccess(String result) {
			    Utils.popupMessage("Cached " + allMicroBehaviours.size() + " micro behaviours.", false);
			}
		    };
		    Modeller.getResourcePageService().cacheURLs(urls, Modeller.sessionGuid, Modeller.userGuid, callback);
		}
		BehaviourComposer.displayAttributes(allMicroBehaviours, constants.allPrototypes());
	    }
	    
	};
	listAttributesButton.addClickHandler(listAttributesHandler);
	composerButtonPanel.add(listAttributesButton);
	importPrototypesSpace = new HTML(NON_BREAKING_SPACE + NON_BREAKING_SPACE);
	composerButtonPanel.add(importPrototypesSpace);
	Button importPrototypesButton = new Button(constants.importPrototypes());
	importPrototypesButton.setTitle(constants.importPrototypesTitle());
	ClickHandler importPrototypesHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		final PopupURLEntryPanel popup = 
			new PopupURLEntryPanel(Modeller.constants.enterAURLOfAFrozenModel(), Modeller.constants.enterAUrlOfAMicroBehaviour());
		Command okCommand = new Command() {
		    
		    @Override
		    public void execute() {
			String url = popup.getURL();
			String modelGuid = CommonUtils.getURLParameter("frozen", url);
			if (modelGuid != null && !modelGuid.isEmpty()) {
			    popup.hide();
			    Modeller.setAlertsLine(Modeller.constants.pleaseWaitWhileTheModelIsLoaded());
			    addModel(modelGuid, false);
			} else {
			    Modeller.setAlertsLineAndHighlight(Modeller.constants.OnlyURLsWithFrozenParameterCanBeLoaded());
			}
		    }	    
		};
		popup.addOKCommand(okCommand);
		popup.center();
		popup.show();	
	    }
	    
	};
	importPrototypesButton.addClickHandler(importPrototypesHandler);
	composerButtonPanel.add(importPrototypesButton);
	composerButtonPanel.add(new HTML(NON_BREAKING_SPACE + NON_BREAKING_SPACE));
	addNewMicroBehaviour = new ModellerButton(constants.createNewMicroBehaviour());
	addNewMicroBehaviour.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		Modeller.setAlertsLine(Modeller.constants.editTheTextThenClickSave());
		openNewMicroBehaviourEditor();
	    }
	});
	addNewMicroBehaviour.setTitle(constants.clickToCreateNewMicroBehaviour());
	composerButtonPanel.add(addNewMicroBehaviour);
//	runButton = new Button(runButtonLabel);
//	downloadButton = new ModellerButton(constants.download());
//	shareButton = new ModellerButton(constants.share());
//	runButton.addClickHandler(new ClickHandler() {
//
//	    @Override
//	    public void onClick(ClickEvent event) {
//		runModel(true, false);
//	    }
//	});
	if (epidemicGameMakerMode()) {
//	    buttonPanel.add(runButton);
//	    runButton.addStyleName("modeller-make-your-game-button");
	    // end up in reverse order
	    composerPanelContents.insert(new HTML(constants.introduceComposerPanelEGM()), 0);
	    composerPanelContents.insert(createEpidemicModelButtons(), 0);
//	    Button helpButton = new Button(constants.help());
//	    helpButton.addStyleName("modeller-make-your-game-button");
//	    ClickHandler helpClickHandler = new ClickHandler() {
//
//		@Override
//		public void onClick(ClickEvent event) {
//		    DecoratedPopupPanel decoratedPopupPanel = new DecoratedPopupPanel(true, true);
//		    Frame frame = 
//			new Frame("http://modelling4all.nsms.ox.ac.uk/Resources/Composer/en/egm_help.html");
//		    frame.setSize("800px", "600px");
//		    composerTabPanel.add(frame, constants.help());
//		    decoratedPopupPanel.setWidget(frame);
//		    decoratedPopupPanel.setAnimationEnabled(true);
//		    decoratedPopupPanel.center();
//		    decoratedPopupPanel.show();
//		}
//
//	    };
//	    helpButton.addClickHandler(helpClickHandler);
//	    buttonPanel.add(helpButton);
//	    Button reloadButton = new Button(constants.startAgain());
//	    reloadButton.setTitle(constants.startAgainTitle());
//	    reloadButton.addStyleName("modeller-make-your-game-button");
//	    ClickHandler reloadClickHandler = new ClickHandler() {
//
//		@Override
//		public void onClick(ClickEvent event) {
//		    String newURL = CommonUtils.getModuleBaseURL() + "?frozen=eu1euKfoBeyjsf5c7oWv7f&EGM=1";
//		    if (useLocalHost) {
//			newURL += "&localhost=1";
//		    }
//		    Window.Location.replace(newURL);
//		}
//
//	    };
//	    reloadButton.addClickHandler(reloadClickHandler);
//	    buttonPanel.add(reloadButton);
//	    composerPanelContents.insert(buttonPanel, 0);
	    setInterfaceEnabled(false); // until loaded
	}
	// following takes one to a page with a download link and instructions
	// since directly downloading ends up be treated like a blocked popup
	// since the click only indirectly leads to new window
//	downloadButton.addClickHandler(new ClickHandler() {
//
//	    @Override
//	    public void onClick(ClickEvent event) {
//		runModel(allPrototypes, true, true, runButton, downloadButton, shareButton);
//	    }
//	});
//	downloadButton.setTitle(constants.clickToDownloadThisModelToRunInNetLogo());
//	buttonPanel.add(downloadButton);
//	shareButton.addClickHandler(new ClickHandler() {
//
//	    @Override
//	    public void onClick(ClickEvent event) {
//		runModel(allPrototypes, false, false, runButton, downloadButton, shareButton);
//	    }
//	});
//	shareButton.setTitle(constants.clickToShareThisModel());
//	buttonPanel.add(shareButton);
	addMacroBehaviour(createMacroBehaviour()); // create initial Prototype
	if (!epidemicGameMakerMode()) {
	    // if epidemicGameMakerMode then already inserted in the "correct" location
	    composerPanelContents.add(composerButtonPanel);
	}
	composerPanelContents.add(prototypesPanel);
	resourcesTabPanel.insert(composerPanel, composerTabWidget(), fixedTabCount++);
	if (Modeller.instance().getNetLogo2BCChannelToken() == null) {
	    resourcesTabPanel.insert(new ScrollPanelInTabPanel(runPanel), runTabLabel, fixedTabCount++);
	} else if (epidemicGameMakerMode()) {
	    // EGM and BC2NetLogo so need 'send to NetLogo'
	    resourcesTabPanel.insert(new ScrollPanelInTabPanel(downloadPanel), downloadTabLabel, fixedTabCount++);
	}
	if (isAdvancedMode()) {
	    resourcesTabPanel.insert(new ScrollPanelInTabPanel(downloadPanel), downloadTabLabel, fixedTabCount++);
	    resourcesTabPanel.insert(new ScrollPanelInTabPanel(sharePanel), shareTabLabel, fixedTabCount++);
	    resourcesTabPanel.insert(new ScrollPanelInTabPanel(infoPanel), infoTabLabel, fixedTabCount++);
	}
	if (epidemicGameMakerMode()) {
	    Frame frame = new Frame("http://m.modelling4all.org/p/en/egm_help.html");
	    frame.setSize("800px", "2400px");
	    VerticalPanel verticalPanel = new VerticalPanel();
	    verticalPanel.add(frame);
	    resourcesTabPanel.add(new ScrollPanel(verticalPanel), new HTML(CommonUtils.emphasise(constants.help(), FIXED_TAB_COLOR)));
	    resourcesTabPanel.switchTo(composerPanel);
	}
	// make the tabs ready to do work when selected
	invalidateRunShareTabs(); 
    }

    public HTML composerTabWidget() {
	String buildLabel = epidemicGameMakerMode() ? CommonUtils.emphasise(constants.makeYourGame(), FIXED_TAB_COLOR) : constants.build();
	return new TabLabel(buildLabel);
    }

    public static boolean epidemicGameMakerMode() {
	return Utils.getLocationParameter("EGM") != null;
    }
    
    @Override
    protected ReconstructEventsContinutation afterReconstructingHistory() {
	if (epidemicGameMakerMode()) {
	    return new ReconstructEventsContinutation() {

		@Override
		public void reconstructSubsequentEvents(ModellerEvent event) {
		    hideEpidemicGameDetails();
		    setInterfaceEnabled(true); 
		    Modeller.instance().restoreCursor();
		    Modeller.adjustLayoutForTranslationBar();
		}
	    };
	} else {
	    return new ReconstructEventsContinutation() {

		@Override
		public void reconstructSubsequentEvents(ModellerEvent event) {
		    // need to wait this long so that all models are defined
		    // in case the anchor is _Models_
		    Modeller.instance().switchToTabIfIndicatedInURL();
		    Modeller.instance().restoreCursor();
		    Modeller.adjustLayoutForTranslationBar();
		}
	    };
	}
    }
    
    public Widget createEpidemicModelButtons() {
	HorizontalPanel horizontalPanel = new HorizontalPanel();
	horizontalPanel.setSpacing(12);
	EpidemicGameCheckBoxColumn addNewElementsPanel = 
	    new EpidemicGameCheckBoxColumn(constants.addNewElements());
	horizontalPanel.add(addNewElementsPanel);
	addNewElementsPanel.add(
		new AddWorkPlaceSessionEventsCheckBox(
			"add work places",
			constants.addWorkPlaces(), 
			"LoOmteXLYs2jiKs5fuO-7h", 
			constants.workPlacesAndAdultsAddedToYourGame(), 
			constants.workPlacesAndAdultsRemovedFromYourGame(), 
			constants.addWorkPlacesTitle(), 
			this));
	AddVirusTrailSessionEventsCheckBox virusTrailsCheckBox = 
	    new AddVirusTrailSessionEventsCheckBox(
		    "add virus trails",
		    constants.addVirusTrails(), 
		    "ZLY_y0qCf8Z1WoupcHPB49", 
		    constants.virusTrailsLeftByInfectedPeopleAddedToYourGame(), 
		    constants.virusTrailsLeftByInfectedPeopleRemovedFromYourGame(),
		    constants.addVirusTrailsTitle(),
		    this);
	addNewElementsPanel.add(virusTrailsCheckBox);
	addNewElementsPanel.add(
		new SessionEventsCheckBox(
			"add more schools",
			constants.addMoreSchools(), 
			"b4Ub_eUfZQP5qLsOhOOm55", 
			constants.moreSchoolsAddedToYourGame(), 
			constants.removedTheAdditionalSchools(), 
			constants.addMoreSchoolsTitle(),
			this));
	addNewElementsPanel.add(
		new SessionEventsCheckBox(
			"students go to closest school",
			constants.studentsGoToClosestSchool(), 
			"MZEsReDv89QB0AVlbbMp58", 
			constants.assignStudentsGoTheClosestSchool(), 
			constants.assignStudentsGoAnySchool(), 
			constants.studentsGoToClosestSchoolTitle(),
			this));
	EpidemicGameCheckBoxColumn addButtonsPanel = 
	    new EpidemicGameCheckBoxColumn(constants.addButtons());
	horizontalPanel.add(addButtonsPanel);
	final SessionEventsCheckBox schoolClosingButtonCheckBox = 
	    new SessionEventsCheckBox(
		    "school closing button",
		    constants.schoolClosingButton(), 
		    "VRvx2KgXYOvrxrBtwRgu65", 	
		    constants.schoolClosingButtonAddedToYourGame(),
		    constants.schoolClosingButtonRemovedFromYourGame(), 
		    constants.schoolClosingButtonTitle(), 
		    this);
	addButtonsPanel.add(schoolClosingButtonCheckBox);
	SessionEventsCheckBox qurantineCheckBox = 
	    new SessionEventsCheckBox(
		    "voluntary quarantine",
		    constants.voluntaryQuarantineButton(), 
		    "iN9z_KyXZDtG3wfkKyPZ77", 	
		    constants.voluntaryQuarantineButtonAddedToYourGame(),
		    constants.voluntaryQuarantineButtonRemovedFromYourGame(), 
		    constants.voluntaryQuarantineButtonTitle(), 
		    this);
	addButtonsPanel.add(qurantineCheckBox);
	RequiresVirusTrailsSessionEventsCheckBox handWashingButtonCheckBox = 
	    new RequiresVirusTrailsSessionEventsCheckBox(
		    "hand washing button",
		    constants.handWashingButton(), 
		    "Zc8OLek9Wz03UcvZV0vK5-", 
		    constants.handWashingButtonAddedToYourGame(),
		    constants.handWashingButtonRemovedFromYourGame(),
		    constants.handWashingButtonTitle(),
		    this,
		    virusTrailsCheckBox,
		    constants.withoutEnablingVirusTrailsHandWashingDoesNothing());
	addButtonsPanel.add(handWashingButtonCheckBox);
	RequiresVirusTrailsSessionEventsCheckBox catchItButtonCheckBox =
	    new RequiresVirusTrailsSessionEventsCheckBox(
		    "catch it bin it kill it button",
		    constants.catchItBinItKillItButton(), 
		    "VKYlOK80Ivxr0wWdaW5348", 	
		    constants.catchItBinItKillItButtonAddedToYourGame(),
		    constants.catchItBinItKillItButtonRemovedFromYourGame(), 
		    constants.catchItBinItKillItButtonTitle(),
		    this,
		    virusTrailsCheckBox,
		    constants.withoutEnablingVirusTrailsCatchItBinItKillItDoesNothing());
	addButtonsPanel.add(catchItButtonCheckBox);
	EpidemicGameCheckBoxColumn slidersPanel1 = 
	    new EpidemicGameCheckBoxColumn(constants.addSliders());
	horizontalPanel.add(slidersPanel1);
	slidersPanel1.add(
		new SliderCheckBox(
			"infection odds slider",
			constants.infectionOddsSlider(), 
			"wf_VkeiCS3RLjaEK44Qh6e", 
			constants.infectionOddsSliderAddedToYourGame(), 
			constants.infectionOddsSliderRemovedFromYourGame(), 
			constants.infectionOddsSliderTitle(), 
			this));
	slidersPanel1.add(
		new SliderCheckBox(
			"encounter rate slider",
			constants.encounterRateSlider(), 
			"TvPUK0j8NCdJKQ7o4qmR5b", 
			constants.encounterRateSliderAddedToYourGame(), 
			constants.encounterRateSliderRemovedFromYourGame(), 
			constants.encounterRateSliderTitle(), 
			this));
	slidersPanel1.add(
		new SliderCheckBox(
			"infection duration slider",
			constants.infectionDurationSlider(), 
			"weAxJ0Law8k2qrq0cX6P5-", 
			constants.infectionDurationSliderAddedToYourGame(), 
			constants.infectionDurationSliderRemovedFromYourGame(),
			constants.infectionDurationSliderTitle(), 
			this));
	slidersPanel1.add(
		new SliderCheckBox(
			"symptoms delay slider",
			constants.symptomsDelaySlider(), 
			"QpQTeesAoHoGaKIe1njb58", 
			constants.symptomsDelaySliderAddedToYourGame(), 
			constants.symptomsDelaySliderRemovedFromYourGame(), 
			constants.symptomsDelaySliderTitle(), 
			this));
	EpidemicGameCheckBoxColumn slidersPanel2 = 
	    new EpidemicGameCheckBoxColumn(constants.addSliders());
	horizontalPanel.add(slidersPanel2);
	SliderCheckBox virusDurationCheckBox = 
	    new SliderCheckBox(
		    "virus duration slider",
		    constants.virusDurationSlider(), 
		    "rW3M-0cm5dyUpucZ4LE-68", 
		    constants.virusDurationSliderAddedToYourGame(), 
		    constants.virusDurationSliderRemovedFromYourGame(), 
		    constants.virusDurationSliderTitle(), 
		    this);
	slidersPanel2.add(virusDurationCheckBox);
	virusTrailsCheckBox.disableWhenNotChecked(virusDurationCheckBox);
	SliderCheckBox trailReductionCheckBox = 
	    new SliderCheckBox(
		    "trail reduction slider",
		    constants.trailReductionSlider(), 
		    "lCaSXK6UnQp3hTAOGb_S68", 
		    constants.trailReductionSliderAddedToYourGame(), 
		    constants.trailReductionSliderRemovedFromYourGame(), 
		    constants.trailReductionSliderTitle(), 
		    this);
	slidersPanel2.add(trailReductionCheckBox);
	virusTrailsCheckBox.disableWhenNotChecked(trailReductionCheckBox);
	SliderCheckBox infectionOddsTrailCheckBox = 
	    new SliderCheckBox(
		    "infection trails slider",
		    constants.infectionTrailsSlider(), 
		    "ComPjemW7jUWrkJtH01Z4d", 
		    constants.infectionTrailsSliderAddedToYourGame(), 
		    constants.infectionTrailsSliderRemovedFromYourGame(), 
		    constants.infectionTrailsSliderTitle(), 
		    this);
	slidersPanel2.add(infectionOddsTrailCheckBox);
	virusTrailsCheckBox.disableWhenNotChecked(infectionOddsTrailCheckBox);
	SliderCheckBox handWashingFactorCheckBox = 
	    new SliderCheckBox(
		    "hand washing factor slider",
		    constants.handWashingFactorSlider(), 
		    "RZVdbKKvlA2uLpMOrL7b7d", 
		    constants.handWashingFactorSliderAddedToYourGame(), 
		    constants.handWashingFactorSliderRemovedFromYourGame(),
		    constants.handWashingFactorSliderTitle(), 
		    this);
	slidersPanel2.add(handWashingFactorCheckBox);
	handWashingButtonCheckBox.disableWhenNotChecked(handWashingFactorCheckBox);
	EpidemicGameCheckBoxColumn slidersPanel3 = 
	    new EpidemicGameCheckBoxColumn(constants.addSliders());
	horizontalPanel.add(slidersPanel3);
	SliderCheckBox schoolClosingCostCheckBox = 
	    new SliderCheckBox(
		    "school closing cost slider",
		    constants.schoolClosingCostSlider(), 
		    "J18vmef3d90gyWQ8hmR15c", 
		    constants.schoolClosingCostSliderAddedToYourGame(), 
		    constants.schoolClosingCostSliderRemovedFromYourGame(), 
		    constants.schoolClosingCostSliderTitle(), 
		    this);
	slidersPanel3.add(schoolClosingCostCheckBox);
	schoolClosingButtonCheckBox.disableWhenNotChecked(schoolClosingCostCheckBox);
	SliderCheckBox handWashingAdReachCheckBox = 
	    new SliderCheckBox(
		    "hand washing ad reach slider",
		    constants.handWashingAdReachSlider(), 
		    "HMOHguT80kpWq70smYiZ4a", 
		    constants.handWashingAdReachSliderAddedToYourGame(), 
		    constants.handWashingAdReachSliderRemovedFromYourGame(), 
		    constants.handWashingAdReachSliderTitle(), 
		    this);
	slidersPanel3.add(handWashingAdReachCheckBox);	
	handWashingButtonCheckBox.disableWhenNotChecked(handWashingAdReachCheckBox);
	SliderCheckBox stayHomeAdReach = 
	    new SliderCheckBox(
		    "stay home ad reach slider",
		    constants.stayHomeAdReachSlider(), 
		    "M_cAXuwGtG2hmhQpk04k54", 
		    constants.stayHomeAdReachSliderAddedToYourGame(), 
		    constants.stayHomeAdReachSliderRemovedFromYourGame(),
		    constants.stayHomeAdReachSliderTitle(), 
		    this);
	slidersPanel3.add(stayHomeAdReach);
	qurantineCheckBox.disableWhenNotChecked(stayHomeAdReach);
	SliderCheckBox catchItAdReachCheckBox = 
	    new SliderCheckBox(
		    "catch it ad reach slider",
		    constants.catchItAdReachSlider(), 
		    "48NETut-Ry4ce1r5130069", 
		    constants.catchItAdReachSliderAddedToYourGame(), 
		    constants.catchItAdReachSliderRemovedFromYourGame(),
		    constants.catchItAdReachSliderTitle(),
		    this);
	slidersPanel3.add(catchItAdReachCheckBox);
	catchItButtonCheckBox.disableWhenNotChecked(catchItAdReachCheckBox);
//	EpidemicGameCheckBoxColumn addPlotsPanel = 
//	    new EpidemicGameCheckBoxColumn(constants.addPlots());
//	horizontalPanel.add(addPlotsPanel);
//	SessionEventsCheckBox populationPlots = 
//	    new SessionEventsCheckBox(
//		constants.removePopulationsPlots(), 
//		"mE5bFesCJpwv7c7jFnUl4f",
//		constants.populationsPlotsRemovedFromYourGame(),	 	 
//		constants.populationsPlotsAddedToYourGame(),
//		this);
//	populationPlots.setValue(true); // starts off checked
//	slidersPanel1.add(populationPlots);
//	addPlotsPanel.add(createAddBehavioursButton(
//		constants.plotNewInfections(), 
//		"Observer",
//		new String [] {
//			"http://modelling4all.nsms.ox.ac.uk/Resources/Composer/en/MB.2/ADD-BEHAVIOURS.html#changes=9QSv8KjOCF0Ms_7-_X-n7h"}));
	return horizontalPanel;
	// commented out measurement buttons since overlaps with plotting and we want to keep things simple now
//	EpidemicGameButtonColumn addMeasurementsPanel = 
//	    new EpidemicGameButtonColumn(constants.addMeasurements());
//	horizontalPanel.add(addMeasurementsPanel);
//	addMeasurementsPanel.add(createAddBehavioursButton(
//		constants.numberInfected(), 
//		"Observer",
//		new String [] {
//			"http://modelling4all.nsms.ox.ac.uk/Resources/Composer/en/MB.4/CREATE-EMPTY-PLOT.html#changes=OR6hz00AaIPNs5GnB8cw5d",
//			"http://modelling4all.nsms.ox.ac.uk/Resources/Composer/en/MB.4/ADD-TO-PLOT.html#changes=I6K54elVsXvS0SVwP92l6h"}));
//	addMeasurementsPanel.add(createAddBehavioursButton(
//		constants.maximumNumberInfected(), 
//		"Observer",
//		new String [] {
//			"http://modelling4all.nsms.ox.ac.uk/Resources/Composer/en/MB.4/UPDATE-PARAMETER-REPEATEDLY.html#changes=ULsyE0bmG5Q5dUR83osx58",
//			"http://modelling4all.nsms.ox.ac.uk/Resources/Composer/en/MB.4/DEFINE-PARAMETER.html#changes=y_CQHKbS7LxjfwqjehWh79",
//			"http://modelling4all.nsms.ox.ac.uk/Resources/Composer/en/MB.4/ADD-TO-PLOT.html#changes=Ne8m0uw3eFPLz3XBTyEW64"}));
//	addMeasurementsPanel.add(createAddBehavioursButton(
//		constants.numberSusceptible(), 
//		"Observer",
//		new String [] {
//			"http://modelling4all.nsms.ox.ac.uk/Resources/Composer/en/MB.4/CREATE-EMPTY-PLOT.html#changes=gH9zHubwdKwVV-F4B5J64b",
//			"http://modelling4all.nsms.ox.ac.uk/Resources/Composer/en/MB.4/ADD-TO-PLOT.html#changes=O0K9eKgss1rQ2JC2d3Ui5f"}));
//	addMeasurementsPanel.add(createAddBehavioursButton(
//		constants.numberRecovered(), 
//		"Observer",
//		new String [] {
//			"http://modelling4all.nsms.ox.ac.uk/Resources/Composer/en/MB.4/CREATE-EMPTY-PLOT.html#changes=x90UFuEF0DbQoaK70way4h",
//			"http://modelling4all.nsms.ox.ac.uk/Resources/Composer/en/MB.4/ADD-TO-PLOT.html#changes=n1njfKRC2TFhGe0UbKNc5g"})); 
    }
     
    @Override
    protected void createCommandBar() {
	createBehaviourComposerPanel();
	String spacing = NON_BREAKING_SPACE + NON_BREAKING_SPACE;
	commandBar.add(new HTML(spacing));
	becomeBehaviourComposerLink(behaviourComposerLink);
	commandBar.add(behaviourComposerLink);
	commandBar.add(new HTML(spacing));
	becomeResourcesLink(resourcesLink);
//	commandBar.add(resourcesLink);
//	commandBar.add(new HTML(spacing));
	becomeHistoryLink(modelHistoryLink);
	commandBar.add(modelHistoryLink);
	commandBar.add(new HTML(spacing));
	becomeModelsLink(modelsLink);
	commandBar.add(modelsLink);
	commandBar.add(new HTML(spacing));
	if (searchPanel != null) {
	    becomeSearchLink(searchLink);
	    commandBar.add(new HTML(spacing));
	}	
	commandBar.add(settingsLink);
	commandBar.add(new HTML(spacing));
	if (languageLink != null) {
	    commandBar.add(languageLink);
	}
	commandBar.add(new HTML(spacing));
	becomeHelpLink(helpLink);
	commandBar.add(helpLink);
	commandBar.add(new HTML(spacing));
	commandBar.setStylePrimaryName("modeller-command-bar");
	setLastCommand(helpLink); // default initial state
	specialAlert.setVisible(false);
	commandBar.add(specialAlert);
    }
    
    public static void becomeBehaviourComposerLink(Anchor anchor) {
	anchor.setTitle(constants.behaviourComposerLinkTitle()); 
	anchor.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
//		if (!inSplitPanelMode()) {
		    switchTo(resourcesTabPanel, behaviourComposerLink);
		    resourcesTabPanel.switchTo(composerPanel);
//		}
	    }
	    
	});
    }
    
    public void runModel(final boolean run, final boolean share, final RunDownloadSharePanel initiatingPanel) {
	ModelRunner modelRunner = new ModelRunner() {

	    @Override
	    public void runModelNow(String modelXML, 
		                    boolean run, 
		                    boolean share, 
		                    RunDownloadSharePanel initiatingPanel,
		                    String alert) {
		instance().runModelNow(modelXML, run, share, initiatingPanel, alert);
	    }
	    
	};
	instance().runModel(run, share, initiatingPanel, modelRunner);
    }
    
    public void runModel(final boolean run, final boolean share, final RunDownloadSharePanel initiatingPanel,
	                 final ModelRunner modelRunner) {
	runModelStartTime = System.currentTimeMillis();
	// these panels soon will be clean
	runPanel.modelFilesBeingComputed();
	downloadPanel.modelFilesBeingComputed(); 
	sharePanel.modelFilesBeingComputed();
	enableRunShareTabs(false);
	String message;
	if (epidemicGameMakerMode()) {
	    message = constants.preparingTheGame();
	} else if (!run && !share && initiatingPanel == null) {
	    message = constants.removingHistory();
	} else {
	    message = constants.preparingToRunTheModel();
	}
	final String alert = CommonUtils.emphasise(message);
	addAlert(alert);
	// remove the existing run panel 
	// do this early so that old applet is likely to be shut down by the time this creates a new one
//	removeRunShareTabs();
	final ArrayList<MicroBehaviourView> dirtyMicroBehaviours = new ArrayList<MicroBehaviourView>();
	final String modelXML = getModelXML(allPrototypes, dirtyMicroBehaviours);
	if (dirtyMicroBehaviours.isEmpty()) {
	    modelRunner.runModelNow(modelXML, run, share, initiatingPanel, alert);
	} else {
	    // after delta pages created then try to run the model again
	    Command commandAfterAllCopied = new Command() {

		@Override
		public void execute() {
		    // fetch XML again since URLs have changed
		    String modelXML = getModelXML(allPrototypes, dirtyMicroBehaviours);
		    if (dirtyMicroBehaviours.isEmpty()) {
			modelRunner.runModelNow(modelXML, run, share, initiatingPanel, alert);
		    } else {
			// try again
			dirtyMicroBehaviours.clear();
			modelXML = getModelXML(allPrototypes, dirtyMicroBehaviours);
			createDeltaPages(dirtyMicroBehaviours, false, false, new Command() {

			    @Override
			    public void execute() {
				String modelXML = getModelXML(allPrototypes, dirtyMicroBehaviours);
				// assume fine this time -- if not will warn below
				runModelNow(modelXML, run, share, initiatingPanel, alert);
				if (!dirtyMicroBehaviours.isEmpty()) {
				    Utils.logServerMessage(Level.WARNING, "Micro behaviours are still dirty after creating copies for the dirty ones.");
				}
			    }
			    
			});
			
		    }
		}
		
	    };
	    createDeltaPages(dirtyMicroBehaviours, false, false, commandAfterAllCopied);
	}
    }
    
    private void runModelNow(final String modelXML,
	                     final boolean run,
	                     final boolean share,
	                     final RunDownloadSharePanel initiatingPanel,
	                     final String alert) {
	final AsyncCallbackNetworkFailureCapable<String[]> callback = new AsyncCallbackNetworkFailureCapable<String[]>() {

	    @Override
	    public void onFailure(Throwable caught) {
		Modeller.removeAlert(alert);
		enableRunShareTabs(true);
//		reportException(caught, "In trying to assemble and run a model.");
		// in case user wants to try again due to intermittent network connections
		invalidateRunShareTabs();
		super.onFailure(caught);
	    }

	    @Override
	    public void onSuccess(String modelInfo[]) {
		super.onSuccess(modelInfo);
		Modeller.removeAlert(alert);
		try {
		    String modelID = modelInfo[0];
		    String dimensions = modelInfo[4];
		    String warnings = modelInfo[5];
		    runModelSucceededTime = System.currentTimeMillis();
		    if (CommonUtils.isErrorResponse(modelID)) {
			addToErrorLog(modelID);
			if (modelInfo.length > 5) {
			    addToErrorLog(warnings);
			}
			// in case user wants to try again due to intermittent network connections
			invalidateRunShareTabs();
			if (!modelID.startsWith("Warning")) {
			    addToErrorLog("<hr>"); // separate calls to Run
			    return;
			}
		    }
		    // need to signal to StaticPageServlet where to get these
		    // since not like static HTML files
		    if (initiatingPanel != null) {
			initiatingPanel.setModelGuid(modelID);
		    }
		    String nlogoFilePath = CommonUtils.getStaticPagePath() + modelID;
		    final String nlogoFileName = dimensions.equals("3") ? nlogoFilePath + ".nlogo3d" : nlogoFilePath + ".nlogo";
		    HorizontalPanel nLogoPanel = new HorizontalPanel();
		    HTML nlogoLink = 
			    new HTML("<a href='" + nlogoFileName + "' target='_blank'>" + constants.downloadtheModel() + "</a>");
		    nlogoLink.setTitle(constants.clickToLaunchNetLogoWithYourModel());
		    nLogoPanel.add(nlogoLink);
		    nLogoPanel.add(new HTML(NON_BREAKING_SPACE + constants.toRunInNetLogo()));
		    int appletWidth = 0;
		    int appletHeight = 0;
		    String fullURL = nlogoFilePath + ".html";
		    String webVersionURL = "http://li425-91.members.linode.com:9000/tortoise#" + nlogoFileName; // fullURL.replace(".html", ".template.html");
		    HTML appletLink = 
			    new HTML("<a href='" + webVersionURL + "' target='_blank'>" + constants.runTheModel() + "</a>"); 
		    appletLink.setTitle(constants.copyThisLinkToShareTheApplet());
		    HorizontalPanel appletPanel = new HorizontalPanel();
		    appletPanel.add(appletLink);
		    appletPanel.add(new HTML(NON_BREAKING_SPACE + constants.inANewBrowserWinderOrTab()));
		    boolean noError = warnings.isEmpty() || warnings.startsWith("Warning");
		    if (noError) { 
			if (!dimensions.equals("3")) {
			    if (Modeller.forWebVersion) {
				final Frame frame = new Frame(webVersionURL);
				// tried modelInfo[1]), Integer.parseInt(modelInfo[2] but it wasn't better
				frame.setPixelSize(Modeller.instance().getMainTabPanelWidth(), Modeller.instance().getMainTabPanelHeight());
//				LoadHandler handler = new LoadHandler() {
//
//				    @Override
//                                    public void onLoad(LoadEvent event) {
//					setAlertsLine(...);
//                                    }
//				    
//				};
//				frame.addLoadHandler(handler);
				BehaviourComposer.runPanel.setFrame(frame);
//				setAlertsLine("<i>Copy and paste</i> <b>" + nlogoFileName + "</b> into the bottom input area and press submit. This uses the Web version of NetLogo which is under development and incomplete.");
			    } else {
				// no applet support for 3D
				try {
				    appletWidth = Integer.parseInt(modelInfo[1]);
				    appletHeight = Integer.parseInt(modelInfo[2]);
				} catch (NumberFormatException e) {
				    addToErrorLog("Error while parsing the applet size: " + e.toString());
				    return;
				}
				String agent = Window.Navigator.getUserAgent();
				// IE9 was causing the entire page to reload
				// but instead declaring the home page to be compatible with IE8 fixes the problem
				// see http://code.google.com/p/google-web-toolkit/issues/detail?id=7682
				boolean internetExplorer = false; // agent.indexOf("MSIE") >= 0;
				if (!internetExplorer) {
				    int fireFoxIndex = agent.indexOf("FireFox");
				    boolean browserNeedingMoreRoomForApplet = fireFoxIndex >= 0;
				    if (browserNeedingMoreRoomForApplet) {
					int versionIndex = agent.indexOf("/", fireFoxIndex);
					if (versionIndex >= 0) {
					    // not sure when this problem was fixed (even whether NetLogo 5 fixed it)
					    // but sure that 13+ is OK
					    if (Double.parseDouble(agent.substring(versionIndex+1)) >= 13) {
						browserNeedingMoreRoomForApplet = false; // is a version of FireFox without this problem
					    }
					}
				    }
				    if (!browserNeedingMoreRoomForApplet) {
					// Safari (at least on Window 7) is a mystery and extra space doesn't seem to help
					browserNeedingMoreRoomForApplet = agent.indexOf("Safari") >= 0;
				    }
				    BehaviourComposer.runPanel.clear();
				    String urlForFrame;
				    String appletTemplateURL = Window.Location.getParameter("template");
				    if (epidemicGameMakerMode() || appletTemplateURL != null) {
					if (appletTemplateURL == null) {
					    appletWidth = 1024;
					    appletHeight = 924;
					} else {
					    String widthString = Window.Location.getParameter("template-width");
					    if (widthString != null) {
						try {
						    appletWidth = Integer.parseInt(widthString);
						} catch (NumberFormatException e) {
						    System.err.println("template-width not a number: " + widthString);
						}
					    }
					    String heightString = Window.Location.getParameter("template-height");
					    if (heightString != null) {
						try {
						    appletHeight = Integer.parseInt(heightString);
						} catch (NumberFormatException e) {
						    System.err.println("template-width not a number: " + heightString);
						} 
					    }
					}
					// want the HTML that provides the legend
					// need to signal to StaticPageServlet where to get these
					urlForFrame = fullURL.replace(".html", ".template.html"); 
				    } else {
					urlForFrame = fullURL.replace(".html", ".raw.html");
				    }
				    Frame frame = new Frame(urlForFrame);
				    if (browserNeedingMoreRoomForApplet) {
					// workaround a FireFox bug displaying applets
					frame.setPixelSize(appletWidth+EXTRA_APPLET_WIDTH+90, appletHeight+EXTRA_APPLET_HEIGHT+110);
				    } else {
					frame.setPixelSize(appletWidth+EXTRA_APPLET_WIDTH+10, appletHeight+EXTRA_APPLET_HEIGHT+10);
				    }
				    BehaviourComposer.runPanel.setFrame(frame);
				}
			    }
			} else {
			    BehaviourComposer.runPanel.setFrame(null);
			    runPanel.clear();
			    runPanel.add(new HTML(Modeller.constants.noAppletsFor3D()));
			    runPanel.setDirty(false);
			}
		    } else {
			invalidateRunShareTabs();
		    }  
		    String auxiliaryFileMessage = 
			    "&nbsp;&nbsp;&nbsp;(" + constants.downloadNetLogoAuxiliaryFileBefore() + "&nbsp;" +
				    "<a href='" + CommonUtils.getStaticPagePath() + 
				    CommonUtils.COMMON_BEHAVIOUR_COMPOSER_NLS + 
				    "' target='_blank'>" + 
				    constants.downloadNetLogoAuxiliaryFile() + "</a>" +
				    "&nbsp;" + constants.downloadNetLogoAuxiliaryFileAfter() + ")";
		    downloadPanel.clear();
		    if (Modeller.instance().getNetLogo2BCChannelToken() == null) {
			downloadPanel.add(new HTML(constants.browserDownloadHelp().replace("***NLOGO-FILE-URL***", nlogoFileName)));
//			downloadPanel.add(nLogoPanel);
			if (Modeller.useAuxiliaryFile) {
			    downloadPanel.add(new HTML(auxiliaryFileMessage));
			}
			downloadPanel.add(new HTML(constants.bc2NetLogoBetterThanDownload()));
//			if (!dimensions.equals("3")) {
//			    downloadPanel.add(new HTML(constants.or()));
//			    downloadPanel.add(appletPanel);
//			}
		    } else {
			setAlertsLine(constants.yourNetLogoWillBeUpdatedSoon());
			if (!initiatingPanel.isShare()) {
			    // nothing to see in the 'download' tab if running BC2NetLogo
			    resourcesTabPanel.switchTo(composerPanel);
			}
		    }
		    sharePanel.clear();
		    sharePanel.setSpacing(12);
		    sharePanel.add(new HTML("<br />" + constants.embedYourModelInWikisBlogsEtc()));
		    String linkModelHTML = CommonUtils.joinPaths(moduleBaseURL, "?frozen=" + modelID);
		    linkModelHTML = CommonUtils.addAttributeToURL(linkModelHTML, CommonUtils.M4A_MODEL_URL_PARAMETER, "1");
		    sharePanel.add(createLinkInfo(constants.linkToModelSnapshot(), linkModelHTML, null));
		    String copyModelHTML = CommonUtils.joinPaths(moduleBaseURL, "?copy=" + readOnlySessionID);
		    copyModelHTML = CommonUtils.addAttributeToURL(copyModelHTML, CommonUtils.M4A_MODEL_URL_PARAMETER, "1");
		    sharePanel.add(createLinkInfo(constants.linkToModelCopy(), copyModelHTML, null));
		    String linkSessionHTML = CommonUtils.joinPaths(moduleBaseURL, "?share=" + sessionGuid);
		    linkSessionHTML = CommonUtils.addAttributeToURL(linkSessionHTML, CommonUtils.M4A_MODEL_URL_PARAMETER, "1");
		    sharePanel.add(createLinkInfo(constants.linkToSession(), linkSessionHTML, null));
		    String userHTML = CommonUtils.joinPaths(moduleBaseURL, "?user=" + userGuid + "&start=models");
		    sharePanel.add(createLinkInfo(constants.maintainYourIdentity(), userHTML, null));
		    if (!dimensions.equals("3") && Modeller.forWebVersion) {
//			String appletGadgetHTML = 
//				"<applet code='org.nlogo.lite.Applet' align='baseline' width='" +
//					(appletWidth+10) + "' height='" + (appletHeight+10) +
//					"' archive='" + CommonUtils.getStaticPagePath() +
//					"../netlogo/NetLogoLite.jar'> <param name='DefaultModel' value='" +
//					nlogoFileName + "'>" + 
//					"<param name='java_arguments' value='-Djnlp.packEnabled=true -Xmx1024m'>";
//			appletGadgetHTML += CommonUtils.NOAPPLET + "\n</applet>\n";
//			appletGadgetHTML += CommonUtils.FIREFOX_RESIZING_APPLET_JAVASCRIPT + "\n";
			sharePanel.add(
				createEmbeddingInfo(
					constants.linkToApplet(), 
					webVersionURL, 
					constants.copyAndPasteThisToShareYourApplet()));
//			sharePanel.add(
//				createEmbeddingInfo(
//					constants.embedAsApplet(), 
//					appletGadgetHTML, 
//					constants.copyAndPasteThisToAddYourAppletToAWebPage()));
			// following copied from ResourcePageServiceImpl
			// iframe has an extra EXTRA_APPLET_WIDTHxEXTRA_APPLET_HEIGHT pixels
			String iframeGadgetHTML = 
				"<iframe src='" + webVersionURL + 
				"' width='" + (appletWidth + EXTRA_APPLET_WIDTH) + 
				"' height='" + (appletHeight + EXTRA_APPLET_HEIGHT) +
				"'></iframe>";
			sharePanel.add(createEmbeddingInfo(constants.embedAsIFrame(), iframeGadgetHTML, constants.copyAndPasteThisToAddYourAppletToAWebPage()));
			String linksGadgetHTML = 
				"<a href='" + webVersionURL + "' target='_blank'>" + 
					constants.runTheModel() + " " + 
					constants.inANewBrowserWinderOrTab() + "</a>";
			sharePanel.add(
				createEmbeddingInfo(
					constants.embedAsLinks(), 
					linksGadgetHTML, 
					constants.copyAndPasteThisToAddYourAppletToAWebPage()));
		    }
		    String exportModelHTML = nlogoFilePath + ".xml";
		    String completeExportModelHTML = "<" + constants.urlToAnotherBehaviourComposerServer() + ">?frozen=" + CommonUtils.encodeColonAndSlash(exportModelHTML);
		    sharePanel.add(createEmbeddingInfo(constants.linkToModelXML(), exportModelHTML, null));
		    sharePanel.add(createEmbeddingInfo(constants.exportModelXML(), completeExportModelHTML, null));
		    String flattenedRenamings = modelInfo[3];
		    if (!flattenedRenamings.isEmpty()) {
			String[] renamings = flattenedRenamings.split("<");
			replaceURLs(renamings);
		    }
		    if (noError) { 
			if (run) {
			    if (Modeller.forWebVersion) {
				// no warning -- but instructions will appear
			    } else if (epidemicGameMakerMode()) { 
				setAlertsLine(constants.modelIsReadyToRun() + " " + CommonUtils.stronglyHighlight(constants.javaAppletProblemsEpidemicGameMaker()));
			    } else {
				setAlertsLine(constants.modelIsReadyToRun() + " " + CommonUtils.stronglyHighlight(constants.javaAppletProblems()));
			    }
			} else if (share) {
			    setAlertsLine(constants.modelIsReadyToShare());
			} else if (Modeller.instance().getNetLogo2BCChannelToken() != null) {
			    setAlertsLine(constants.modelIsReadyAndIsBeingSentToNetLogo());
			} else {
			    setAlertsLine(constants.modelIsReadyToDownload());
			}
		    } 
		    if (!warnings.isEmpty()) {
			// some warnings from assembling the NetLogo code
			if (warnings != null && !warnings.startsWith("Warning")) {
			    warnings = "Warning. " + warnings;
			}
			addToErrorLog(warnings);
		    }
		    long currentTimeMillis = System.currentTimeMillis();
		    Utils.logServerMessage(Level.INFO, "runModel generated &frozen=" + modelID + 
			                               " time from start is " +
		                                       (currentTimeMillis-runModelStartTime) + 
		                                       " and time from success is " + (currentTimeMillis-runModelSucceededTime));
		} finally {
		    enableRunShareTabs(true);
		}
//		if (isAdvancedMode()) {
//		    if (runNotShare) {
//			if (download || !modelInfo[3].isEmpty()) {
//			    composerTabPanel.switchTo(runPanel);
//			}
//		    } else {
//			composerTabPanel.switchTo(sharePanel);
//		    }
//		    switchTo(composerTabPanel, behaviourComposerLink);
//		}
	    }

	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		runModelNow(modelXML, run, share, initiatingPanel, alert, callback);
	    }
	    
	});
    }
    
    public void runModelNow(String modelXML, 
                                   boolean run,
                                   boolean share, 
                                   RunDownloadSharePanel initiatingPanel,
                                   String alert,
                                   AsyncCallbackNetworkFailureCapable<String[]> callback) {
//	String localModelXML = Modeller.localVersionOfURLsIfRunningLocal(modelXML);
	String templatePath = Window.Location.getParameter("template");
	if (templatePath == null) {
	    templatePath = epidemicGameMakerMode() ? "en/stop_the_epidemic.html" : "en/model_applet.html";
	}
	getResourcePageService().runModel(sessionGuid,
		                          BehaviourComposer.getOriginalSessionGuid(),
		                          userGuid,
		                          modelXML,
		                          templatePath,
		                          CommonUtils.getHostBaseURL(),
		                          share ? null : Modeller.instance().getNetLogo2BCChannelToken(),
		                          share ? null : Modeller.instance().getBc2NetLogoOriginalSessionGuid(),
		                          Modeller.cachingEnabled,
		                          Modeller.internetAccess,
		                          Modeller.useAuxiliaryFile,
		                          Modeller.forWebVersion,
		                          callback);
    }
    
    public static void createDeltaPages(final ArrayList<MicroBehaviourView> dirtyMicroBehaviours, 
	                                final boolean subMicroBehavioursNeedNewURLs,
	                                final boolean firstDirtyMicroBehaviourIsForCopying,
	                                final Command commandAfterAllCopied) {
	// process dirtyMicroBehaviours in reverse order so that leaves are copied before higher nodes
	// since the higher nodes refer to lower ones
	if (dirtyMicroBehaviours.isEmpty()) {
	    commandAfterAllCopied.execute();
	    return;
	}
	int size = dirtyMicroBehaviours.size();
	final boolean forCopying = firstDirtyMicroBehaviourIsForCopying && size == 1;
	final MicroBehaviourView microBehaviourView = dirtyMicroBehaviours.get(size-1);
	BrowsePanelCommand commandToCreateDeltaCopy = new BrowsePanelCommand() {

	    @Override
	    public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
		if (panel == null) {
		    if (commandAfterAllCopied != null) {
			commandAfterAllCopied.execute();
		    }
		    return;
		}
		CreateDeltaPageCommand createDeltaPageCommand = new CreateDeltaPageCommand() {

		    @Override
		    public void execute(MicroBehaviourView microBehaviourView,
			                DeltaPageResult deltaPageResult,
			                boolean panelIsNew,
			                boolean subMicroBehavioursNeedNewURLs,
			                boolean forCopying) {
			super.execute(microBehaviourView, deltaPageResult, panelIsNew, subMicroBehavioursNeedNewURLs, forCopying);
			MicroBehaviourView whoToRemove = null;
			String url = microBehaviourView.getUrl();
			for (MicroBehaviourView dirtyMicroBehaviour : dirtyMicroBehaviours) {
			    if (dirtyMicroBehaviour.getUrl().equals(url)) {
				whoToRemove = dirtyMicroBehaviour;
				break;
			    }
			}
			if (whoToRemove != null) {
			    dirtyMicroBehaviours.remove(whoToRemove);
			} else {
			    System.err.println("Expected to find a dirty micro behaviour to remove.");
			}
			createDeltaPages(dirtyMicroBehaviours, subMicroBehavioursNeedNewURLs, firstDirtyMicroBehaviourIsForCopying, commandAfterAllCopied);
		    }

		};
		microBehaviourView.setCopyMicroBehaviourWhenExportingURL(false);
		panel.createDeltaPage(microBehaviourView, createDeltaPageCommand, subMicroBehavioursNeedNewURLs, forCopying);
	    }

	};
	Modeller.executeOnMicroBehaviourPage(microBehaviourView.getUrl(), commandToCreateDeltaCopy, true, false);	
    }

    protected void replaceURLs(String[] renamings) {
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    macroBehaviour.replaceURLs(renamings);
	}
    }

    public String getModelXML(ArrayList<MacroBehaviourView> macrobehaviours, 
	                      ArrayList<MicroBehaviourView> dirtyMicroBehaviours) {
	StringBuilder modelString = new StringBuilder("<model version='7'>");
	// 5 uses CDATA for macro behaviour name rather than encoded attribute
	// 6 setCopyMicroBehaviourWhenExportingURL(true) only if dirty
	// older models pay the extra cost since not sure if they are dirty or not
	// 7 creates delta copies for any dirty micro behaviours
	ArrayList<MicroBehaviourView> seenBefore = new ArrayList<MicroBehaviourView>();
	for (MacroBehaviourView macroBehaviour : macrobehaviours) {
	    modelString.append(macroBehaviour.getModelXML(dirtyMicroBehaviours, seenBefore, -1));
	}
	for (SessionEventsCheckBox sessionEventsCheckBox : allEnabledCheckBoxes) {
	    modelString.append(sessionEventsCheckBox.getModelXML());
	}
	String infoTab = infoPanel.getContents();
	if (infoTab != null && !infoTab.isEmpty()) {
	    modelString.append("<infoTab>" + CommonUtils.createCDATASection(infoTab) + "</infoTab>");
	}
	if (modelsPanel != null) {
	    // not created in EGM
	    String description = modelsPanel.getDescription();
	    if (description != null && !description.isEmpty()) {
		modelString.append("<description>" + CommonUtils.createCDATASection(description) + "</description>");
	    }
	}
	modelString.append("</model>");
	return modelString.toString();
    }
    
    public static void invalidateRunShareTabs() {
	runPanel.setDirty(true);
	downloadPanel.setDirty(true);
	sharePanel.setDirty(true);
    }
    
    public static void enableRunShareTabs(boolean enabled) {
	if (enabled) {
	    tabsEnabledCount++;
	    if (tabsEnabledCount >= 0) {
		runPanel.setEnabled(true);
		downloadPanel.setEnabled(true);
		sharePanel.setEnabled(true);
	    }
	} else {
	    tabsEnabledCount--;
	    if (tabsEnabledCount < 0) {
		runPanel.setEnabled(false);
		downloadPanel.setEnabled(false);
		sharePanel.setEnabled(false);
	    }
	}
    } 
    
    @Override
    public void dirtyEventAddedToHistoryDirtiedModel() {
	// remove since the old one probably is invalid and can be confusing
	invalidateRunShareTabs();
    }
    
    /* 
     * @return true if did any processing including taking care of next element
     */
    @Override
    public boolean processNonGenericCodeElement(
	    com.google.gwt.dom.client.Element codeElement, 
	    String innerHTML, 
	    BrowsePanel browsePanel,
	    String id,
	    boolean copyOnUpdate,
	    Command command) {
	String encodedBehaviourName = codeElement.getAttribute("behaviourname");
	if (encodedBehaviourName != null && !encodedBehaviourName.isEmpty()) {
	    String sourceURL = codeElement.getAttribute("SourceURL");
	    MicroBehaviourView microBehaviour = CommonUtils.hasChangesGuid(sourceURL) ? getMicroBehaviourView(sourceURL) : null;
	    // encoded since it may contain HTML
	    // behaviour name may have had subscripts added to it for uniqueness 
	    String behaviourName = 
		microBehaviour == null ?
		CommonUtils.decode(encodedBehaviourName) :
		microBehaviour.getNameHTMLAndDescription();
	    if (microBehaviour == null) {
		HashMap<Integer, String> textAreaValues = browsePanel.getTextAreaValues();
		if (textAreaValues != null) {
		    String updatedName = textAreaValues.get(-1);
		    if (updatedName != null) {
			behaviourName = updatedName;
		    }
		}	
		microBehaviour = 
		    new MicroBehaviourView(
			    behaviourName, 
			    sourceURL, 
			    textAreaValues,
			    null,
			    CommonUtils.hasChangesGuid(sourceURL),
			    browsePanel.textAreasCount(),
			    false); 
	    } else if (microBehaviour.getParent() != null) {
		// already "in use" so make a copy
		microBehaviour = microBehaviour.copy();
	    }
	    browsePanel.replaceElementWithWidget(id, codeElement, Utils.wrapForGoodSize(microBehaviour));
	    TextArea textArea = new DescriptionTextArea(CommonUtils.getDescription(behaviourName), browsePanel);    
	    browsePanel.addDescriptionArea(textArea);
	    browsePanel.setBehaviourName(behaviourName, microBehaviour, encodedBehaviourName);
	    microBehaviour.setCopyMicroBehaviourWhenExportingURL(copyOnUpdate);
	    command.execute();
	    return true;
	}
	String macroBehaviourName = codeElement.getAttribute("macroBehaviour");
	if (macroBehaviourName != null && !macroBehaviourName.isEmpty()) {
	    String microBehaviourUrl = codeElement.getAttribute("url");
	    String name = CommonUtils.decode(macroBehaviourName);
	    MacroBehaviourView existingMacroBehaviourView = browsePanel.getMacroBehaviourView(name);
	    String initialMicroBehavioursEncoded = codeElement.getAttribute("initialMicroBehaviours");
	    // if has no behaviours and source of page specifies them then add them
	    // if copying has already been given all the micro-behaviours it should have
	    boolean commandHandled = false;
	    MacroBehaviourView macroBehaviourView;
	    if (existingMacroBehaviourView != null) {
		String existingMacroBehaviourMicroBehaviourUrl = 
		    existingMacroBehaviourView.getMicroBehaviourUrl();
		if (existingMacroBehaviourMicroBehaviourUrl == null) {
		    existingMacroBehaviourView.setMicroBehaviourUrl(microBehaviourUrl);
		    macroBehaviourView = existingMacroBehaviourView;
		    // following caused a bug where the macro behaviour view
		    // was shared and hence disappeared on one browse panel 
		    // (restorable by close and then re-open)
//		} else if (microBehaviourUrl.equals(existingMacroBehaviourMicroBehaviourUrl)) {
//		    macroBehaviourView = existingMacroBehaviourView;
		} else {
		    macroBehaviourView = existingMacroBehaviourView.copyFor(microBehaviourUrl, null);
		}
		if (!MacroBehaviourView.macroBehaviourViews.contains(macroBehaviourView)) {
		    MacroBehaviourView.macroBehaviourViews.add(macroBehaviourView);
		}	
	    } else {
		macroBehaviourView = new MacroBehaviourView(name, microBehaviourUrl);
		MacroBehaviourView.macroBehaviourViews.add(macroBehaviourView);
		if (!initialMicroBehavioursEncoded.isEmpty()) {
		    String urlsEncoded[] = initialMicroBehavioursEncoded.split("(\\s)+"); // any white space
		    String baseURL = CommonUtils.getBaseURL(microBehaviourUrl);
		    commandHandled = true;
		    loadNextMicroBehaviour(urlsEncoded, macroBehaviourView, baseURL, 0, urlsEncoded.length-1, command);
		}
	    }
	    if (!macroBehaviourView.isAttached()) {
		browsePanel.replaceElementWithWidget(id, codeElement, macroBehaviourView);
	    }
	    MicroBehaviourView microBehaviourView = browsePanel.getMicroBehaviour();
	    if (microBehaviourView == null) {
		System.err.println("Internal error. Browse panel was expected to contain a micro-behaviour but didn't.");
	    } else {
		microBehaviourView.addMacroBehaviourView(macroBehaviourView);
	    }
	    if (command != null && !commandHandled) {
		command.execute();
	    }
	    return true;
	}
	return false;
    }

    protected void loadNextMicroBehaviour(
	    final String[] urlsEncoded,
	    final MacroBehaviourView macroBehaviourView, 
	    final String baseURL,
	    int index, 
	    final int lastIndex,
	    final Command commandAfterLoading) {
	String encodedURL = urlsEncoded[index];
	while (encodedURL.isEmpty()) {
	    if (index == lastIndex) {
		return;
	    }
	    index++;
	    encodedURL = urlsEncoded[index];
	}
	final int currentIndex = index;
	String shortURL = URL.decodeQueryString(encodedURL);
	String prototypeName = CommonUtils.prototypeName(shortURL);
	if (prototypeName != null) {
	    MacroBehaviourAsMicroBehaviourView macroBehaviourAsMicroBehaviourView = 
		new MacroBehaviourAsMicroBehaviourView(prototypeName);
	    macroBehaviourView.addMicroBehaviour(macroBehaviourAsMicroBehaviourView, false, false);
	    commandAfterLoading.execute();
	    return;
	}
	final String fullUrl = CommonUtils.fullUrl(shortURL, baseURL);
	final BrowsePanelCommand command = new BrowsePanelCommand() {
	    @Override
	    public void execute(BrowsePanel panel, String answer[], boolean PanelIsNew) {
		MicroBehaviourView microBehaviour = panel.getMicroBehaviour();
		if (microBehaviour != null) {
		    macroBehaviourView.addMicroBehaviour(microBehaviour.copy(), false, false);
		} else {
		    System.err.println("Expected browse panel to have a micro-behaviour " + fullUrl);
		}
		if (currentIndex != lastIndex) {
		    loadNextMicroBehaviour(urlsEncoded, macroBehaviourView, baseURL, currentIndex+1, lastIndex, commandAfterLoading);
		} else {
		    commandAfterLoading.execute();
		}
		// following is probably unnecessary since executeOnMicroBehaviourPage does it
		if (panel.isTemporary()) {
		    // don't need the panel anymore so close it
		    mainTabPanel.remove(panel);
		}
	    }
	};
	StringBuffer temp = new StringBuffer();
	for (String url : urlsEncoded) {
	    temp.append(url + "  ");
	}
//	Log.info("loadNextMicroBehaviour url: " + fullUrl + " encoded: " + temp.toString()); // for debugging
	executeOnMicroBehaviourPage(fullUrl, command, true, true);
    }

    private void setVisibilitiyOfEpidemicGameMakerSupportPrototypes(boolean visible) {
	if (!epidemicGameMakerMode()) {
	    // don't do this if advanced mode used elsewhere
	    return;
	}
	String supportPrototypeNames[] = {"Support", "World", "Money", "Infected", "Health"};
	for (String prototypeName : supportPrototypeNames) {
	    MacroBehaviourView prototype = getMacroBehaviourWithName(prototypeName);
	    if (prototype != null) {
		prototype.setVisible(visible); 
	    }
	}
	String optionalPrototypeNames[] = {"Work Place", "Virus"};
	// these should be invisible if inactivated
	for (String prototypeName : optionalPrototypeNames) {
	    MacroBehaviourView prototype = getMacroBehaviourWithName(prototypeName);
	    if (prototype != null) {
		prototype.setVisible(visible || prototype.isActive()); 
	    }
	}
    }

    public void setInterfaceEnabled(boolean enable) {
	// TODO: composerTabPanel.setEnabled(enable);
	interfaceEnabled  = enable;
        setEnabledAllCheckBoxes(enable);
        if (enable) {
            restoreCursor();
        } else {
            waitCursor();
        }
    }

    public int getNumberOfSlidersInEpidemicGame() {
        return numberOfSlidersInEpidemicGame;
    }

    public void setNumberOfSlidersInEpidemicGame(int numberOfSlidersInEpidemicGame) {
        this.numberOfSlidersInEpidemicGame = numberOfSlidersInEpidemicGame;
    }

    public void addEnabledCheckBox(SessionEventsCheckBox checkBox) {
	allEnabledCheckBoxes.add(checkBox);	
    }
    
    public void removeEnabledCheckBox(SessionEventsCheckBox checkBox) {
	allEnabledCheckBoxes.remove(checkBox);	
    }
    
    public void setEnabledAllCheckBoxes(boolean enable) {
	for (SessionEventsCheckBox sessionEventsCheckBox : allEnabledCheckBoxes) {
	    sessionEventsCheckBox.setEnabled(enable);
	}
    }
    
    public boolean anyCheckBoxesTicked() {
	for (SessionEventsCheckBox sessionEventsCheckBox : allEnabledCheckBoxes) {
	    if (sessionEventsCheckBox.getValue()) {
		return true;
	    }
	}
	return false;
    }
    
    @Override
    public SessionEventsCheckBox getCheckBoxWithSessionGuid(String checkBoxSessionGuid, String name) {
	// first check guids since they are unchanging
	for (SessionEventsCheckBox sessionEventsCheckBox : allEnabledCheckBoxes) {
	    if (sessionEventsCheckBox.getGuid().equals(checkBoxSessionGuid)) {
		return sessionEventsCheckBox;
	    }
	}
	// but the check box could have been replaced with a new session guid
	// but kept the name so check for that 
	for (SessionEventsCheckBox sessionEventsCheckBox : allEnabledCheckBoxes) {
	    if (sessionEventsCheckBox.getText().equals(name)) {
		return sessionEventsCheckBox;
	    }
	}
	return null;
    }

    @Override
    public void loadModelXML(
	    String modelXML, String modelID, boolean unload, boolean replaceOldModel, ArrayList<Boolean> macroBehavioursSelected, boolean reconstructingHistory) {
	Node contents = Utils.parseXML(modelXML);
	if (contents == null) {
	    return;
	}
	String tag = contents.getNodeName();
	if (tag.equalsIgnoreCase("html")) { 
	    // if the model is given as a URL rather than an ID then HTML tags are added
	    contents = contents.getFirstChild();
	    tag = contents.getNodeName();
	    while (!tag.equalsIgnoreCase("model")) {
		contents = contents.getNextSibling();
		if (contents == null) {
		    if (!modelXML.contains("The server could not find a model or session whose id is ")) {
			addToErrorLog("Missing MODEL tag in XML: " + modelXML);
		    }
		    return;
		}
		tag = contents.getNodeName();
	    }
	}
	if (tag.equalsIgnoreCase("model")) {
	    CompoundEvent removeAllMacroBehavioursEvent = null;
	    if (!unload && replaceOldModel) {
		removeAllMacroBehavioursEvent = removeAllMacroBehaviours();	
	    }
	    int version = 1;
	    int loadCount = 0;
	    int runCount = 1;
	    if (contents instanceof Element) {
		Element modelElement = (Element) contents;
		version = Utils.getIntAttribute(modelElement, "version", version);
		loadCount = Utils.getIntAttribute(modelElement, "loads", loadCount);
		runCount = Utils.getIntAttribute(modelElement, "runs", runCount);
	    }
	    processTopLevelModelXML(contents.getChildNodes(), unload, replaceOldModel, macroBehavioursSelected, modelID, version, null);
	    //		if (unload && allPrototypes.isEmpty()) {
	    //		    Modeller.instance().setPrototypeCounter(0); // back to counting from 1
	    //		    addMacroBehaviour(createMacroBehaviour());
	    //		}
	    if (!reconstructingHistory && replaceOldModel) {
		LoadModelEvent loadModelEvent = new LoadModelEvent(modelID, replaceOldModel, loadCount, runCount);
		if (removeAllMacroBehavioursEvent == null) {
		    loadModelEvent.addToHistory();
		} else {
		    ArrayList<ModellerEvent> events = new ArrayList<ModellerEvent>(2);
		    events.add(removeAllMacroBehavioursEvent);
		    events.add(loadModelEvent);
		    CompoundEvent compoundEvent = new CompoundEvent(events);
		    //			compoundEvent.setAlternativeHTML(loadModelEvent.toHTMLString());
		    compoundEvent.addToHistory();
		} 
	    }
	} else {
	    Modeller.addToErrorLog("Expected the model to have MODEL as the top-level tag: " + modelXML + ". Unrecognized tag: " + tag);
	}
    }
    
    public void processTopLevelModelXML(
	    final NodeList nodes, 
	    boolean unload, 
	    boolean replaceOldModel,
	    final ArrayList<Boolean> macroBehavioursSelected,
	    final String modelID,
	    int version, 
	    MicroBehaviourView microBehaviour) {
	ArrayList<MacroBehaviourView> macroBehaviours = 
	    getMacroBehaviours(nodes, unload, replaceOldModel, version, microBehaviour);
	if (!replaceOldModel && !unload) {
	    if (macroBehavioursSelected == null) {
		final ArrayList<MacroBehaviourCheckBox> checkBoxes = new ArrayList<MacroBehaviourCheckBox>();
		final DecoratedPopupPanel popup = new DecoratedPopupPanel(false, true);
		VerticalPanel prototypesList = new VerticalPanel();
		prototypesList.setSpacing(6);
		prototypesList.add(new HTML(Modeller.constants.selectThosePrototypesToBeLoad()));
		for (MacroBehaviourView macroBehaviour : macroBehaviours) {
		    MacroBehaviourCheckBox checkBox = new MacroBehaviourCheckBox(macroBehaviour);
		    prototypesList.add(checkBox);
		    checkBoxes.add(checkBox);
		}
		Button okButton = new Button("OK");
		prototypesList.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		prototypesList.add(okButton);
		okButton.addClickHandler(new ClickHandler() {
		    @Override
		    public void onClick(ClickEvent event) {
			ArrayList<MacroBehaviourView> selectedMacroBehaviours = new ArrayList<MacroBehaviourView>();
			ArrayList<Boolean> macroBehavioursSelected = new ArrayList<Boolean>();
			for (MacroBehaviourCheckBox checkBox : checkBoxes) {
			    MacroBehaviourView macroBehaviour = checkBox.getMacroBehaviour();
			    macroBehavioursSelected.add(macroBehaviour != null);
			    if (macroBehaviour != null) {
				selectedMacroBehaviours.add(macroBehaviour);
			    }
			}
			if (!selectedMacroBehaviours.isEmpty()) {
			    addMacroBehaviours(selectedMacroBehaviours, nodes, false);
			    LoadModelEvent loadModelEvent = new LoadModelEvent(modelID, false, 0, 1, macroBehavioursSelected);
			    loadModelEvent.addToHistory();
			}
			popup.hide();
		    }	    
		});
		popup.setWidget(prototypesList);
		popup.show();
		popup.center();
	    } else {
		ArrayList<MacroBehaviourView> selectedMacroBehaviours = new ArrayList<MacroBehaviourView>();
		for (int i = 0; i < macroBehavioursSelected.size(); i++) {
		    if (macroBehavioursSelected.get(i)) {
			selectedMacroBehaviours.add(macroBehaviours.get(i));
		    }
		}
		addMacroBehaviours(selectedMacroBehaviours, nodes, false);
	    }
	} else {
	    addMacroBehaviours(macroBehaviours, nodes, unload);
	}
    }

    public void addMacroBehaviours(List<MacroBehaviourView> macroBehaviours, NodeList nodes, boolean unload) {
	for (MacroBehaviourView macroBehaviour : macroBehaviours) {
	    if (unload) {
		removeMacroBehaviour(macroBehaviour);
	    } else {
		addMacroBehaviour(macroBehaviour);
	    }
	}
	processCheckBoxes(nodes);
    }
    
    protected void processCheckBoxes(NodeList nodes) {
	int length = nodes.getLength();
	for (int i = 0; i < length; i++) {
	    Node node = nodes.item(i);
	    if (node instanceof Element) {
		Element element = (Element) node;
		String tag = node.getNodeName();
		if (tag.equals("CheckBox")) {
		    String nameId = element.getAttribute("nameId");
		    String guid = element.getAttribute("guid");
		    SessionEventsCheckBox checkBox = getCheckBoxWithSessionGuid(guid, nameId);
		    if (checkBox != null) {
			// doesn't fire events but needs to enable dependent check boxes
			checkBox.setValue(true, false);
			// may have been ticked and then the enabler was unticked
			checkBox.setEnabled(true); 
			checkBox.tieValueToEnabledForAllDependents();
		    }
		}
	    }
	}
    }

    public ArrayList<MacroBehaviourView> getMacroBehaviours(
	    NodeList nodes, 
	    boolean unload,
	    boolean replaceOldModel,
	    int version, 
	    MicroBehaviourView microBehaviour) {	
	int length = nodes.getLength();
	ArrayList<MacroBehaviourView> result = new ArrayList<MacroBehaviourView>(length);
	for (int i = 0; i < length; i++) {
	    Node node = nodes.item(i);
	    if (node instanceof Element) {
		Element element = (Element) node;
		String tag = node.getNodeName();
		if (tag.equalsIgnoreCase("macrobehaviour")) {
		    String name = Utils.getNameFromElement(element);
		    if (name != null) {
			MacroBehaviourView macroBehaviour = null;
			if (microBehaviour != null) {
			    macroBehaviour = microBehaviour.getMacroBehaviourNamed(name);
			}
			if (unload) {
			    if (macroBehaviour == null) {
				macroBehaviour = Modeller.instance().getMacroBehaviourWithHTMLName(name);
			    }
			    if (macroBehaviour != null) {
				result.add(macroBehaviour);
			    } else {
				String macroBehaviourNames = Modeller.instance().getMacroBehaviourNames();
				if (!macroBehaviourNames.isEmpty()) {
				    // ignore errors removing prototypes if there are none
				    Modeller.addToErrorLog("Could not find a macro behaviour " + name + 
					                   " while unloading a model. Known macro-behaviours are: " +
					                   macroBehaviourNames);
				}
				continue;
			    }
			} else {
			    if (macroBehaviour == null) {
				macroBehaviour = new MacroBehaviourView(name);
				if (microBehaviour == null) {
				    macroBehaviour.addControlWidgets();
				}
			    }
			    loadMicroBehaviours(macroBehaviour, node.getChildNodes(), version);
			    result.add(macroBehaviour);
			}
			macroBehaviour.processFlags(element);
			Node instanceCountNode = Utils.getFirstNodeWithTagName(element, "instanceCount");
			if (instanceCountNode != null) {
			    String instanceCount = instanceCountNode.getFirstChild().getNodeValue();
			    macroBehaviour.setInstanceCountExpressionText(instanceCount);
			}
		    }		    
		} else if (replaceOldModel && !unload && tag.equalsIgnoreCase("infoTab")) {
		    String infoTabContents = node.getFirstChild().getNodeValue();
		    infoPanel.setContents(infoTabContents);
		    if (modelsPanel != null) {
			modelsPanel.setInfoTab(infoTabContents);
		    } // otherwise will be set when modelsPanel is created
		} else if (replaceOldModel && !unload && tag.equalsIgnoreCase("description")) {
		    String modelDescription = node.getFirstChild().getNodeValue();
		    Modeller.instance().setOriginalModelDescription(modelDescription);
		}
	    } 
	}
	return result;
    }
    
    public void loadMicroBehaviours(MacroBehaviourView macroBehaviour, NodeList nodes, int version) {
	int length = nodes.getLength();
	for (int i = 0; i < length; i++) {
	    Node node = nodes.item(i);
	    String tag = node.getNodeName();
	    if (tag.equalsIgnoreCase("microbehaviour")) {
		if (node instanceof Element) {
		    Element element = (Element) node;
		    String url = Utils.getElementString("url", element);
		    String description = Utils.getElementString("description", element);
		    if (url == null || url.equals("null")) {
			addToErrorLog("Ignored a micro-behaviour with a null URL. " + description + 
				      " in " + macroBehaviour.getNameHTML());
			continue;
		    }
		    if (url != null && description != null) {
			Node textAreasNode = Utils.getFirstNodeWithTagName(element, "textareas");
			NodeList textAreaUpdateElements = null;
			Element textAreaElements = null;
			int updateCount = 0;
			if (textAreasNode != null && textAreasNode instanceof Element) {
			    textAreaElements = (Element) textAreasNode;
			    textAreaUpdateElements = textAreaElements.getChildNodes();
			    updateCount = textAreaUpdateElements.getLength();
			}
			MicroBehaviourView microBehaviour = 
			    new MicroBehaviourView(description, url, updateCount, false);
			microBehaviour.setCopyWhenOpened(true);
			String activeString = element.getAttribute("active");
			microBehaviour.setActive(!"false".equals(activeString));
			String dirtyString = element.getAttribute("dirty");
			microBehaviour.setCopyMicroBehaviourWhenExportingURL("true".equals(dirtyString) || version < 6);
			Node enhancementsNode = Utils.getFirstNodeWithTagName(element, "enhancements");
			if (enhancementsNode instanceof Element) {
			    Element enhancementsElement = (Element) enhancementsNode;
			    String indicesString = enhancementsElement.getAttribute("indices");
			    if (indicesString != null) {
				// <enhancements /> is used to indicate no enhancements
				String originalTextAreasCountString = 
				    enhancementsElement.getAttribute("originalTextAreasCount");
				try {
				    int previousTextAreaIndex = Integer.parseInt(originalTextAreasCountString)-1;
				    String[] indicesStringList = indicesString.split(",");
				    microBehaviour.clearEnhancements();
				    for (String indexString : indicesStringList) {
					MicroBehaviourEnhancement enhancement = 
					    MicroBehaviourEnhancement.getEnhancement(Integer.parseInt(indexString));
					previousTextAreaIndex = 
					    microBehaviour.enhanceCode(enhancement, null, false, previousTextAreaIndex);
					microBehaviour.addEnhancement(enhancement);
				    }
				} catch (NumberFormatException e) {
				    System.err.println(enhancementsElement.toString() + " doesn't have a valid originalTextAreasCount. " + url);
				    addToErrorLog("Loaded a model with a micro-behaviour with incorrect XML. " + url + " Enhancements to this micro-behaviour lost.");
				}
			    }
			}
			if (textAreaElements != null) {
			    for (int j = 0; j < updateCount; j++) {
				Node child = textAreaUpdateElements.item(j);
				if (child instanceof Element) {
				    Element textAreaElement = (Element) child;
				    String indexString = textAreaElement.getAttribute("index");
				    try {
					int index = Integer.parseInt(indexString);
					// extract the value from the CDATA
					String newContents = textAreaElement.getFirstChild().getNodeValue();
					microBehaviour.updateTextArea(newContents, index);
				    } catch (NumberFormatException e) {
					System.err.println(textAreaElement.toString() + " doesn't have a numeric index.");
					addToErrorLog("Loaded a model with a micro-behaviour with a modified text area described by a non-number index: " + indexString);
				    }
				}
			    }
			    // following ensures the micro-behaviour 'knows' all its text area values
			    // resolves for example Issue 922
			    microBehaviour.fetchAndUpdateTextAreas();
			}
//			NodeList macroBehaviourNodes = element.getElementsByTagName("macrobehaviours");
			Node macroBehavioursNode = Utils.getFirstNodeWithTagName(element, "macrobehaviours");
			if (macroBehavioursNode != null) { 
			    if (macroBehavioursNode instanceof Element) {
				Element macroBehaviourElement = (Element) macroBehavioursNode;
				NodeList macroBehaviourElements = macroBehaviourElement.getChildNodes();
				ArrayList<MacroBehaviourView> macroBehaviours = 
				    getMacroBehaviours(macroBehaviourElements, false, false, version, microBehaviour);
				microBehaviour.setMacroBehaviourViews(macroBehaviours);
			    }
			}
			macroBehaviour.addMicroBehaviour(microBehaviour, false, false);
		    } else {
			addToErrorLog("Expected micro-behaviour to have url and description attributes: " + node.toString());
		    }
		}
	    } else if (tag.equalsIgnoreCase("MacroBehaviourAsMicroBehaviour")) {
		if (node instanceof Element) {
		    Element element = (Element) node;
		    String name = Utils.getElementString("name", element);
		    if (name != null) {
			MacroBehaviourAsMicroBehaviourView macroBehaviourAsMicroBehaviourView =
			    new MacroBehaviourAsMicroBehaviourView(name);
			macroBehaviour.addMicroBehaviour(macroBehaviourAsMicroBehaviourView, false, false);
		    }
		}
	    } 
	}
    }

    public static void displayAttributes(final List<MicroBehaviourView> microBehaviours, final String attributesOf) {
	Modeller.setAlertsLine(Modeller.constants.searchingPleaseWait());
	final AsyncCallbackNetworkFailureCapable<String[]> callback = new AsyncCallbackNetworkFailureCapable<String[]>() {

	    @Override
	    public void onSuccess(String result[]) {
		super.onSuccess(result);
		Modeller.setAlertsLine(Modeller.constants.attributesFound());
		// result should be a list of triples: attribute name, reader URLs separated by ;, writer URLs
		AttributesDisplay attributesDisplay = new AttributesDisplay(result, attributesOf);
		attributesDisplay.center();
		attributesDisplay.show();
	    }

	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		displayAttributes(microBehaviours, attributesOf, callback);
	    }
	    
	});
    }
    
    private static void displayAttributes(List<MicroBehaviourView> microBehaviours, 
	                                  String attributesOf, 
	                                  AsyncCallbackNetworkFailureCapable<String[]> callback) {
        String microBehaviourURLs[] = new String[microBehaviours.size()];
        int index = 0;
        for (MicroBehaviourView microBehaviourView : microBehaviours) {
            microBehaviourURLs[index] = microBehaviourView.getUrl();
            index++;
        }
        Modeller.getResourcePageService().getAttributesOfMicroBehaviours(microBehaviourURLs, 
        	                                                         Modeller.sessionGuid,
        	                                                         Modeller.cachingEnabled,
        	                                                         Modeller.internetAccess,
        	                                                         callback);
    }

    public static void deleteModel(final String modelGuid) {
	final AsyncCallbackNetworkFailureCapable<String> logMessageCallback = new AsyncCallbackNetworkFailureCapable<String>();
	logMessageCallback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		deleteModel(modelGuid, logMessageCallback);
	    }
	    
	});
    }
    
    private static void deleteModel(final String modelGuid, AsyncCallbackNetworkFailureCapable<String> deleteModelCallback) {
	getResourcePageService().deleteModel(modelGuid, sessionGuid, deleteModelCallback); 
    }

    @Override
    public ModellerTabPanel getMainTabPanel() {
	return BehaviourComposer.resourcesTabPanel;
    }

    public static CheckBox getAdvancedCheckBox() {
        return advancedCheckBox;
    }

    public static boolean isOkToAuthorLocalResources() {
	return BehaviourComposer.okToAuthorLocalResources;
    }

    public static void setOkToAuthorLocalResources(boolean okToAuthorLocalResources) {
        BehaviourComposer.okToAuthorLocalResources = okToAuthorLocalResources;
    }

    public ModellerButton getAddNewResource() {
        return addNewResource;
    }

    public HTML getAddNewResourceSpace() {
        return addNewResourceSpace;
    }

    public void hideEpidemicGameDetails() {
	for (MacroBehaviourView macroBehaviour : allPrototypes) {
	    macroBehaviour.setShowMicroBehaviours(false);
	    macroBehaviour.setShowHideThisCheckBox(false);
	    macroBehaviour.setShowHowManyInstances(false);
	}
	setVisibilitiyOfEpidemicGameMakerSupportPrototypes(false);
    }

    public static boolean isInterfaceEnabled() {
        return interfaceEnabled;
    }

    public static String getOriginalSessionGuid() {
	if (modelsPanel == null) {
	    // not created in Epidemic Game Maker
	    return sessionGuid; // best we can do
	} else {
	    return modelsPanel.getOriginalSessionGuid();
	}
    }

    public static RunPanel getRunPanel() {
        return runPanel;
    }

    public static String getLastModelGuid() {
	String modelGuid = downloadPanel.getModelGuid();
	if (modelGuid == null) {
	    runPanel.getModelGuid();
	}
	return modelGuid;
    }

    public static void setLastModelGuid(String modelGuid) {
	downloadPanel.setModelGuid(modelGuid);
	runPanel.setModelGuid(modelGuid);
    }

    //private static void checkForCompilerErrors(String localFileName, final Anchor fileCheckButton) {
    //fileCheckButton.setEnabled(false); // until this finishes
    //setAlertsLineHTML(CommonUtils.emphasise(constants.pleaseWait()));
    //getResourcePageService().checkNetLogoFile(localFileName, new AsyncCallback<String>() {
    //
    //public void onFailure(Throwable caught) {
    //fileCheckButton.setEnabled(true);
    //setAlertsLine("Sorry something went wrong trying to obtain compiler errors from NetLogo.");		
    //}
    //
    //public void onSuccess(String result) {
    //fileCheckButton.setEnabled(true);
    //if (result == null) {
    //setAlertsLineHTML(constants.noErrorsReported());
    //} else {
    //// following inserted in reverse order:
    //addToErrorLog("<hr>"); // a nice horizontal line
    //addToErrorLog(result);
    //addToErrorLog(constants.theNetLogoCompilerReported());
    //setAlertsLine("");
    //}
    //}  
    //});
    //}
}
