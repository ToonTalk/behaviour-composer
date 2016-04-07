package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import uk.ac.lkl.client.composer.CustomisationPopupPanel;
import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.event.BrowseToPageEvent;
import uk.ac.lkl.client.event.ReplaceURLEvent;
import uk.ac.lkl.shared.CommonUtils;
import uk.ac.lkl.shared.DeltaPageResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.http.client.URL;

public class BrowsePanel extends ScrollPanelInTabPanel {
    // put vertical panel in a scroll panel in case longer than available space
    protected VerticalPanel panelContents = new VerticalPanel();
    protected HTMLPanel browsePanelHTML = null;
    protected int scrollTop = 0;
    protected int scrollLeft = 0;
    protected LoadResourcePage incrementalLoadResourcePage = null;
    protected String currentURLBase = null;
    protected Button editButton = new ModellerButton(Modeller.constants.editAll());
    protected String currentURL = null;
    protected ClosableTab tabWidget = null;
    protected String originalHTML = null;
    protected BrowsePanelEdited doAfterSaving = null;
    protected String taggingTitle = "A Modelling4All resource"; // typically overridden before tagging buttons generated
    // the following is to hold updated values for text areas before the micro-behaviour is set
    protected HashMap<Integer, String> textAreaValuesOfPanel = null;
    protected ArrayList<MicroBehaviourEnhancement> enhancementsOfPanel = null;
    // the following is to update the panel when a text area is changed externally (e.g. by undo or collaborating user)
    protected HashMap<Integer, CodeTextArea> codeTextAreas = new HashMap<Integer, CodeTextArea>();
    // the following is to keep track of micro-behaviours added or removed from this page
//    protected ArrayList<MacroBehaviourView> macroBehaviourViews = new ArrayList<MacroBehaviourView>();
    // following assumes there is only one microBehaviour per page
    // initially used only to inform about text areas updates
    protected MicroBehaviourView microBehaviour = null;
    // temporary one's will be removed automatically so no need to count them
    // when ensuring there is enough room for the current set of panels
    protected boolean temporary = false;
//    protected HorizontalPanel taggingAndEditing = new HorizontalPanel();
    // following needed to copy a micro-behaviour (with text areas)
    protected int endId;
    protected int prefixId;
    protected String processedHTML;
    protected String encodedBehaviourName;
    protected boolean okToRemoveIfTabBarFull = true;
//    protected HorizontalPanel taggingButtons = null;
    protected boolean okToAddTaggingButtons = true;
    protected DisclosurePanel urlDisclosurePanel = null;
    protected SplitScreenOptions splitScreenOptions = null;
    private VerticalPanel browsePanelToolBar;   
    protected MicroBehaviourView microBehaviourToShareStateWithCopy = null;
    private boolean expectingANameChange = false;
    protected boolean copyOnUpdate = false;
    private CodeTextArea parameterTextArea = null;
    private boolean refreshInProgress = false;
    private HorizontalPanel urlDisclosureAndTaggingButtons;
    // this is in browse panel because it is whether they have been displayed (viewed)
    // and if the panel is closed and opened then it needs to re-install them
    private boolean enhancementsInstalled = false;
    private TextArea descriptionArea;
    private SimplePanel warningPanel;
    private boolean containsNetLogoCode;
    private boolean readOnly = false;
    protected static ArrayList<String> browsedURLs = new ArrayList<String>();
    
    private static HashMap<String, HashMap<String, String>> pageTranslations = new HashMap<String, HashMap<String, String>>();
    
    static {
	HashMap<String, String> brTranslations = new HashMap<String, String>();
	pageTranslations.put("br", brTranslations);
	brTranslations.put("http://m.modelling4all.org/p/en/MB.4/doc.html", "http://resources.modelling4all.org/pt-br/help");
	// and for debugging
	brTranslations.put("http://127.0.0.1:8888/p/en/MB.4/doc.html", "http://resources.modelling4all.org/pt-br/help");
    }

    private static int loadCounter = 1;
    
    public BrowsePanel() {
	this(null, null, null);
    }
    
    public BrowsePanel(HashMap<Integer, String> textAreaValues, ArrayList<MicroBehaviourEnhancement> enhancements) {
	this(null, textAreaValues, enhancements, null);
    }
    
    public BrowsePanel(HashMap<Integer, String> textAreaValues, ArrayList<MicroBehaviourEnhancement> enhancements, List<MacroBehaviourView> macroBehaviourViews) {
	this(null, textAreaValues, enhancements, macroBehaviourViews);
    }

    public BrowsePanel(String url, 
	               HashMap<Integer, String> textAreaValues,
	               ArrayList<MicroBehaviourEnhancement> enhancements,
	               List<MacroBehaviourView> macroBehaviourViews) {
	super();
	setWidget(panelContents);
	this.textAreaValuesOfPanel = textAreaValues;
	this.enhancementsOfPanel = enhancements;
	this.currentURL = url;
//	Utils.updateWidthWhenAttached(this);
	addStyleName("modeller-browse-panel");
    }
   
//    public HorizontalPanel taggingButtons() {
//	HorizontalPanel panel = new HorizontalPanel();
////	panel.setSpacing(8);
//	panel.setStylePrimaryName("modeller-tagging-buttons-panel");
//	String url = CommonUtils.joinPaths(getCurrentURLBase(), currentURL);
//	url = URL.encode(url);
//	String htmlString = 
//	     "<a title='Tag this page on Delicious" + 
//	       "' href='http://delicious.com/post' onclick=\"window.open('http://delicious.com/post?v=5&noui&jump=close&url='+encodeURIComponent('" + 
//	       url + 
//	       "')+'&title='+encodeURIComponent('" + 
//	       getTaggingTitle() + 
//	       "')+'&tags=m4a+&notes=Please%20add%20modelling4all_resource%20to%20tags', 'delicious','toolbar=no,width=550,height=550'); return false;\">" +
//	       "<img border='0' src='http://images.del.icio.us/static/img/delicious.small.gif'>" + 
//	       "</a>" + Modeller.NON_BREAKING_SPACE;
//	panel.add(new HTML(htmlString));
//	htmlString = 
//	    "<a target='_blank' title='Save this page on Facebook" + 
//	       "' href='http://www.facebook.com/sharer.php?u=" + url + "'>" +
//	       "<img border='0' src='http://static.ak.facebook.com/images/share/facebook_share_icon.gif?6:26981'>" + 
//               "</a>" + Modeller.NON_BREAKING_SPACE;
//	panel.add(new HTML(htmlString));	
//	return panel;
//    }
    
    public void loadNewURL(String newURL, final Command doAfterUpdateCommand) {
	if (!newURL.equals(getCurrentURL())) {
	    if (newURL.indexOf("://") < 0) {
		newURL = "http://" + newURL;
	    }
	    // browse to the URL and fetch and update
	    BrowsePanelCommand commandWhenLoaded = new BrowsePanelCommand() {

		@Override
		public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
		    panel.fetchAndUpdate(doAfterUpdateCommand);	    
		}

	    };
	    browseTo(URL.encode(newURL), false, true, commandWhenLoaded);
//	    setPreviousURL(null); // so this isn't treated like editing the current page
	}
    }
    
    private void acceptHTML(RichTextArea richText) {
	String richTextString = richText.getHTML();
	if (!richTextString.equals(originalHTML)) {
	    updateContents(Utils.replaceHTMLBody(richTextString, originalHTML), false);
	}
    }
    
    public void browseTo(String shortURL, 
	                 final boolean addToHistory, 
	                 final boolean copyOnUpdate, 
	                 final BrowsePanelCommand commandWhenLoaded) {
	// shortURL may be a relative URL
	if (shortURL == null) {
	    return;
	}
	final String withoutMinusSign = CommonUtils.withoutMinusSign(shortURL);
	final boolean active = (withoutMinusSign == shortURL);
	shortURL = withoutMinusSign;
	final String urlString = Utils.fullURL(shortURL);
	if (getCurrentURL() != null && urlString.equals(getCurrentURL()) && browsePanelHTML != null && !refreshInProgress) {
	    if (!isAttached()) {
		addEditButton();
		Modeller.mainTabPanel.add(this, (tabWidget == null ? Modeller.constants.resourceEditor() : tabWidget.getTabName()));
		Modeller.mainTabPanel.switchTo(this);
	    }
	    return;
	}
	refreshInProgress = false;
	setCurrentURL(urlString);
	if (addToHistory) {
	    BrowseToPageEvent event = new BrowseToPageEvent(urlString, this);
	    Modeller.browseToPageEvents.add(event);
	    History.newItem(event.toString());
	}
	if (urlString.startsWith("#")) {
	} else {
	    final String alert = Modeller.constants.loadingPleaseWait();
	    Modeller.instance().waitCursor();
//	    Modeller.addAlert(alert);
	    final HTML loadingNotice = new HTML(alert + "<br>" + urlString);
	    panelContents.add(loadingNotice);
	    setExtraWidgetsVisible(false);
	    final AsyncCallbackNetworkFailureCapable<String[]> callback = new AsyncCallbackNetworkFailureCapable<String[]>() {
		
		@Override
		public void onFailure(Throwable caught) {
		    Modeller.instance().restoreCursor();
		    BehaviourComposer.enableRunShareTabs(true);
		    NetworkFailure.instance().networkFailure(caught, Modeller.constants.browsingTo().replace("***URL***", withoutMinusSign), getTimer());
//		    if (commandWhenLoaded != null) {
//			commandWhenLoaded.failed();
//		    }
		}

		@Override
		public void onSuccess(final String answer[]) {
		    super.onSuccess(answer);
		    String processedHTML = answer[0];
		    if (processedHTML == null || processedHTML.isEmpty()) {
			processedHTML = "Error: No HTML returned from " + urlString;
		    }
		    Modeller.addToErrorLog(answer[5]); // warnings if any
		    if (CommonUtils.isErrorResponse(processedHTML)) {
			BehaviourComposer.enableRunShareTabs(true);
			Modeller.addToErrorLog(processedHTML);
			removePanel();
			if (commandWhenLoaded != null) {
			    commandWhenLoaded.failed();
			}
			Modeller.instance().restoreCursor();
			return;
		    }
		    panelContents.remove(loadingNotice);
		    setExtraWidgetsVisible(true);
		    if ("true".equals(answer[7])) {
			BrowsePanel.this.setReadOnly(true);
		    }
		    try {
			final int beginId = Integer.parseInt(answer[2]);
			final int prefixId = Integer.parseInt(answer[3]);
			originalHTML = answer[1];
			// defer the following so the page is loaded and processed
			// in browseHTMLString
			BrowsePanelCommand commandAfterProcessing = new BrowsePanelCommand() {

			    @Override
			    public void execute(BrowsePanel panel, final String[] answer, final boolean panelIsNew) {
				try {
				    updateTaggingAndURLWidgets();
				    Modeller.instance().restoreCursor();
//				    Modeller.removeAlert(alert);
				    // following works around a FireFox 3 bug
				    // that left the entire application invisible
				    Element bodyElement = RootPanel.getBodyElement();
				    if (bodyElement != null) {
					Style style = bodyElement.getStyle();
					if (style != null) {
					    style.setProperty("visibility", "visible");
					}
				    }
				    if (microBehaviour != null) {
					Command command = new Command() {

					    @Override
					    public void execute() {
						if (commandWhenLoaded != null) {
						    commandWhenLoaded.execute(BrowsePanel.this, answer, true);
						}
					    }
					    
					};
					if (isContainsNetLogoCode()) {
					    // don't fetch latest if edited and not yet saved
					    if (!microBehaviour.isCopyMicroBehaviourWhenExportingURL()) {
						fetchAndUpdate(command);
					    } else if (command != null) {
						command.execute();
					    }
					} else {
					    if (command != null) {
						command.execute();
					    }
					    if (microBehaviour != null) {
						// could it be null?
						microBehaviour.setContainsNetLogoCode(false);
						if (!BehaviourComposer.isOkToAuthorLocalResources()) {
						    microBehaviour.removeFromParent();
						}
					    }
					    if (descriptionArea != null) {
						descriptionArea.removeFromParent();
					    }
					}
				    } else if (commandWhenLoaded != null) {
					commandWhenLoaded.execute(BrowsePanel.this, answer, true);
//					if (BehaviourComposer.okToAuthorLocalResources()) {
//					    addEditButton();
//					}
				    }
				} finally {
				    BehaviourComposer.enableRunShareTabs(true);
				}
			    }
			    
			};
			try {
			    browseHTMLString(processedHTML, beginId, prefixId, copyOnUpdate, commandAfterProcessing);
			    if (!answer[5].isEmpty()) {
				// browseHTMLString may have added a loading message -- warning is more important
				Modeller.setAlertsLine(answer[5]);
			    }
			} catch (Exception e) {
			    Modeller.instance().restoreCursor();
			    BehaviourComposer.enableRunShareTabs(true);
			    Modeller.addToDebugMessages(Modeller.constants.errorWhileProcessingContentsOfURL() + " " + e.toString() + " \nprocessedHTML: " + processedHTML);
			}
			if (!CommonUtils.hasChangesGuid(urlString)) {
			    // use the name provided by the page if not copied
			    String newName = answer[6];
			    if (newName != null) {				
				if (microBehaviour != null) {
//				    // don't want the line breaks caused by <p> ... </p>
//				    String cleanNameHTML = CommonUtils.removePTags(newName).trim();
//				    microBehaviour.setNameHTMLAndDescription(cleanNameHTML);
				    microBehaviour.setNameHTMLAndDescription(newName);
				    // following is 'sanitised' version of newName
				    setTabName(microBehaviour.getNameHTML());
				} else {
				    setTabName(newName);
				}
			    }
			}
			if (microBehaviour != null && !active) {
			    microBehaviour.inactivateAll();
			}
		    } catch (Exception e) {
			Modeller.instance().restoreCursor();
			BehaviourComposer.enableRunShareTabs(true);
			e.printStackTrace();
			Modeller.addToErrorLog(Modeller.constants.errorWhileProcessingContentsOfURL()  + " " +  
				               urlString + " " + e.getMessage() +
				               "\nbeginId: " + answer[2] +
				               "\nprefixId: " + answer[3] +
				               "\noriginalHTML: " + answer[1] +
				               "\nprocessedHTML: " + processedHTML);
			// keep going
			if (commandWhenLoaded != null) {
			    Modeller.addToErrorLog(Modeller.constants.continuingReconstructionOfPreviousSession());
			    commandWhenLoaded.execute(BrowsePanel.this, answer, true);
			}
//			for (int i = 0; i < 7; i++) {
//			    Modeller.addToErrorLog(i + ": " + answer[i]);
//			}	               
		    }
		}
		
	    };
	    callback.setAndRunTimer(new TimerInSequence() {

		@Override
		public void run() {
		    BehaviourComposer.enableRunShareTabs(false);
		    loadURL(urlString, callback);
		}

	    });
	}
    }

    protected void addEditButton() {
	// TODO: determine if this is obsolete
	ModellerButton editResourceButton = new ModellerButton(Modeller.constants.edit());
	editResourceButton.setWidth("100px");
	ClickHandler clickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		Modeller.mainTabPanel.remove(BrowsePanel.this);
		Modeller.setAlertsLine(Modeller.constants.editTheTextThenClickSave());
		createAndSwitchToEditor(false, false, false);
	    }
	    
	};
	editResourceButton.addClickHandler(clickHandler);
	panelContents.add(editResourceButton);
    }
    
    public void fetchAndUpdate(final Command commandAfterUpdates) {
	Command fetchTextAreaUpdates = new Command() {

	    @Override
	    public void execute() {
		fetchAndUpdateTextAreas(commandAfterUpdates);		
	    }

	};
	fetchEnhancements(fetchTextAreaUpdates);
    }
    
    protected void fetchAndUpdateTextAreas(final Command commandAfterFetchingUpdates) {
	if (getCurrentURL() == null) {
	    commandAfterFetchingUpdates.execute();
	    return;
	}
	if (microBehaviour == null || !CommonUtils.hasChangesGuid(getCurrentURL())) {
	    if (commandAfterFetchingUpdates != null) {
		commandAfterFetchingUpdates.execute();
	    }
	    return;
	}
	final AsyncCallbackNetworkFailureCapable<HashMap<Integer, String>> callback = 
		new AsyncCallbackNetworkFailureCapable<HashMap<Integer, String>>() {

	    @Override
	    public void onFailure(Throwable caught) {
		NetworkFailure.instance().networkFailure(caught, Modeller.constants.fetchingMicroBehaviourUpdates(), getTimer());
	    }

	    @Override
	    public void onSuccess(HashMap<Integer, String> textAreaUpdates) {
		super.onSuccess(textAreaUpdates);
		if (textAreaUpdates == null) {
		    // error??
		} else {
		    String error = textAreaUpdates.get(-2);
		    if (error != null) {
			Modeller.addToErrorLog(error);
		    } else {
			Set<Entry<Integer, String>> entrySet = textAreaUpdates.entrySet();
			int originalTextAreasCount = microBehaviour.getOriginalTextAreasCount();
			for (Entry<Integer, String> entry : entrySet) {
			    Integer index = entry.getKey();
			    String value = entry.getValue();
			    if (index >= originalTextAreasCount || // a check if already updated
				microBehaviour.getTextAreaValue(index) == null) {
				// update those that are part of enhancements regardless of whether
				// they have a default value or not
				// TODO: determine if this should also be conditional
				// on not dirty (i.e. !microBehaviour.isCopyMicroBehaviourWhenExportingURL())
				if (index >= 0) {
				    updateTextArea(index, value, true);
				} else {
				    setTabName(value);
				    microBehaviour.setNameHTMLAndDescription(value);
				    // and make sure micro-behaviour is in synch
				    microBehaviour.updateTextArea(value, index);
				}
			    }
			}
		    }
		}
		if (commandAfterFetchingUpdates != null) {
		    commandAfterFetchingUpdates.execute();
		}
	    }
	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		fetchAndUpdateTextAreas(microBehaviour, callback);
	    }

	});
    }
	
    public static void fetchAndUpdateTextAreas(MicroBehaviourView microBehaviour, AsyncCallbackNetworkFailureCapable<HashMap<Integer, String>> callback) {
	Modeller.getResourcePageService().fetchTextAreaValues(
		microBehaviour.getUrl(), 
		Modeller.sessionGuid,
		GWT.getHostPageBaseURL(),
		Modeller.cachingEnabled,
		Modeller.internetAccess,
		callback);
    }
    
    protected void fetchEnhancements(final Command commandAfterFetchingEnhancements) {
	if (getCurrentURL() == null || 
	    microBehaviour == null || 
	    !CommonUtils.hasChangesGuid(getCurrentURL()) ||
	    (microBehaviour.getEnhancements() != null  && !microBehaviour.getEnhancements().isEmpty())) {
	    if (commandAfterFetchingEnhancements != null) {
		commandAfterFetchingEnhancements.execute();
	    }
	    return;
	}
	final AsyncCallbackNetworkFailureCapable<List<MicroBehaviourEnhancement>> callback =
		new AsyncCallbackNetworkFailureCapable<List<MicroBehaviourEnhancement>>() {
		
	    @Override
	    public void onFailure(Throwable caught) {
		NetworkFailure.instance().networkFailure(caught, Modeller.constants.fetchingMicroBehaviourEnhancements(), getTimer());
	    }

	    @Override
	    public void onSuccess(List<MicroBehaviourEnhancement> enhancements) {
		super.onSuccess(enhancements);
		if (enhancements != null && !enhancements.equals(microBehaviour.getEnhancements())) {
		    microBehaviour.setEnhancements(new ArrayList<MicroBehaviourEnhancement>(enhancements));
		    installEnhancements();
		}
		if (commandAfterFetchingEnhancements != null) {
		    commandAfterFetchingEnhancements.execute();
		}
	    }
	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		Modeller.getResourcePageService().fetchEnhancements(
			getCurrentURL(), 
                        Modeller.sessionGuid, GWT.getHostPageBaseURL(),
                        Modeller.cachingEnabled,
                        Modeller.internetAccess,
                        callback);
	    }
	    
	});
    }

    public void updateContents(final String html, final boolean replaceOld) {
	final BrowsePanel browsePanel = this;
	currentURLBase = CommonUtils.getBaseURL(getCurrentURL());
	final AsyncCallbackNetworkFailureCapable<String[]> callback = 
		new AsyncCallbackNetworkFailureCapable<String[]>() {
		    
		    @Override
		    public void onFailure(Throwable caught) {
			NetworkFailure.instance().networkFailure(caught, Modeller.constants.updatingAResourcePanel(), getTimer());
		    }
		    
		    @Override
		    public void onSuccess(String answer[]) {
			super.onSuccess(answer);
			Modeller.addToErrorLog(answer[5]); // warnings if any
			if (answer != null && !CommonUtils.isErrorResponse(answer[0])) {
			    final int beginId = Integer.parseInt(answer[2]);
			    final int prefixId = Integer.parseInt(answer[3]);
			    originalHTML = answer[1];
			    //			    BrowsePanelCommand commandWhenLoaded = new BrowsePanelCommand() {
	//
//				@Override
//				public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
//				    MicroBehaviourView newMicroBehaviour = panel.getMicroBehaviour();
//				    newMicroBehaviour.setWarnThatTextAreasHaveChanged(microBehaviour.isWarnThatTextAreasHaveChanged());
//				    panel.addWarningIfNeeded();
//				}
//				
//			    };
			    BrowsePanelCommand commandWhenLoaded = new BrowsePanelCommand() {

				@Override
				public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
				    if (answer != null && answer[4] != null) {
					if (answer[4].equals(browsePanel.getCurrentURL())) {
					    Modeller.mainTabPanel.refreshMicroBehavioursWithURL(CommonUtils.removeBookmark(answer[4]));
					} else {
					    browsePanel.setCurrentURL(answer[4]);
					}
				    }
				}
				
			    };
			    browsePanel.browseHTMLString(answer[0], beginId, prefixId, true, commandWhenLoaded);
			    if (doAfterSaving != null) {
				doAfterSaving.edited(browsePanel);
			    }
			    updateTaggingAndURLWidgets();
			    if (answer[5].isEmpty()) {
				Modeller.setAlertsLine(Modeller.constants.editsSaved());
			    } else {
//				Modeller.removeAlerts(); // because the following is important and shouldn't be interfered with
				Modeller.setAlertsLine(answer[5]);
			    }
			} else {
			    Modeller.addToErrorLog(Modeller.constants.failedToProcessPage());
			    // don't try to process it but leave it there
			    browsePanel.setHTML(html);
			}
		    }
		};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		transformPage(html, getCurrentURL(),  replaceOld, callback);
	    }
	    
	});
    }
    
    protected void browseHTMLString(String html, int endId, int prefixId, boolean copyOnUpdate, final BrowsePanelCommand commandWhenLoaded) {
	if (CommonUtils.isErrorResponse(html)) {
	    Modeller.addToErrorLog(html);
	    commandWhenLoaded.execute(this, null, false);
	    return;
	}
	final String alert = 
	    Modeller.getSelectedTab() == this ? 
	    Modeller.addAlert(Modeller.constants.loadingPleaseWait()) : null;
	// store these in case the page is copied
	this.endId = endId;
	this.prefixId = prefixId;
	setProcessedHTML(html);
	final HTMLPanel newPanel;
	try {
	    newPanel = new HTMLPanel(html);
	} catch (Exception e) {
	    e.printStackTrace();
	    Modeller.addToErrorLog(Modeller.constants.errorProcessingHtml() + e.toString());
	    Modeller.addToErrorLog(html);
	    commandWhenLoaded.execute(null, null, false);
	    return;
	}
	if (getBrowsePanelHTML() != null) {
	    getBrowsePanelHTML().removeFromParent();
	}
	setBrowsePanelHTML(newPanel);
	panelContents.insert(newPanel, 0);
	if (getWidget() == null) {
	    // not sure how this ever happens
	    setWidget(panelContents);
	}
	// following works around an IE bug (version 6 and 7 apparently not 8)
	// see http://code.google.com/p/google-web-toolkit/issues/detail?id=2152
	JavaScript.cleanAnchors();
	Command newCommandWhenLoaded = new Command() {

	    @Override
	    public void execute() {
//		remove(loadingHTML);
		installEnhancements();
		if (commandWhenLoaded != null) {
		    commandWhenLoaded.execute(BrowsePanel.this, null, true);
		}
		if (alert != null) {
		    Modeller.removeAlert(alert);
		}
		if (!temporary) {
		    Window.scrollTo(0, 0);
		}
		setVisible(true);
		if (tabWidget != null) {
		    tabWidget.setVisible(true);
		}
	    }
	    
	};
	if (endId <= 0) { // was 1 -- why?
	    // nothing more to do
	    newCommandWhenLoaded.execute();
	    return;
	}
	// following could be when selecting the Explorer tab:
	// can take a while so do it in the background
	setVisible(false); // so loading is faster and you don't see the changes as they occur
	LoadResourcePage incrementalLoadResourcePage = 
	    new LoadResourcePage(this, endId, prefixId, getTextAreaValues(), copyOnUpdate, newCommandWhenLoaded);
	incrementalLoadResourcePage.execute();
    }  

    public String getProcessedHTML() {
        return processedHTML;
    }

    public void setProcessedHTML(String html) {
	this.processedHTML = html;
    }
    
    public void browseTo(final String urlString) {
	browseTo(urlString, false, false, null);
    }

//    public void browseToBookmark(String url) {
//	// obsolete??
//	Window.alert("browseToBookmark needs to be re-implemented");
//    }
    
//  static public void browseToBookmark(String url, final BrowsePanel browsePanel) {
//	try {
//	    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
//	    builder.sendRequest("", new RequestCallback() {
//		public void onError(Request request, Throwable exception) {
//		    exception.printStackTrace();
//		}
//
//		public void onResponseReceived(Request request,
//			Response response) {
//		    browseHTMLString(response.getText(), browsePanel);
//		}
//	    });
//	} catch (Exception exception) {
//	    exception.printStackTrace();
//	}
//  }
    
    public void loadURL(String urlString, AsyncCallback<String[]> callback) {
	String fullUrl = CommonUtils.fullUrl(urlString, getCurrentURLBase());
	fullUrl = localeVersion(fullUrl);
	if (fullUrl.startsWith("http://http://")) {
	    // users do this -- sloppy copy and paste
	    fullUrl = fullUrl.substring(7); 
	}
	// check for version specific URLs and revise to use default version
	String[] split = fullUrl.split("\\d+.m4a-gae.appspot.com", 2);
	if (split.length == 2) {
	    fullUrl = "http://m4a-gae.appspot.com" + split[1];
	}
	currentURLBase = CommonUtils.getBaseURL(fullUrl);
	Modeller.getResourcePageService().fetchAndTransformPage(fullUrl, 
		                                                Modeller.sessionGuid,
		                                                Modeller.userGuid,
		                                                loadCounter++,
		                                                GWT.getHostPageBaseURL(),
		                        			Modeller.cachingEnabled,
		                        			Modeller.internetAccess,
		                                                callback);
    }
    
    private String localeVersion(String url) {
	HashMap<String, String> translations = pageTranslations.get(Modeller.getLocale());
	if (translations == null) {
	    return url;
	}
	String translatedURL = translations.get(url);
	if (translatedURL == null) {
	    return url;
	} else {
	    return translatedURL;
	}
    }

    public static void transformPage(String html, String oldUrl, boolean replaceOld, AsyncCallback<String[]> callback) {
	Modeller.getResourcePageService().transformPage(html, 
		                                        oldUrl, 
		                                        replaceOld,
		                                        Modeller.sessionGuid,
		                                        GWT.getHostPageBaseURL(),
		                                        loadCounter++,
		                			Modeller.cachingEnabled,
		                			Modeller.internetAccess,		                                        
		                                        callback);
    }
    
    public String getHTML() {
	if (getBrowsePanelHTML() == null) {
	    return null;
	}
	return getBrowsePanelHTML().getElement().getInnerHTML();
    }
    
    public void setHTML(String newHTML) {
	if (getBrowsePanelHTML() == null) {
	    return;
	}
	getBrowsePanelHTML().getElement().setInnerHTML(newHTML);
    }
    
    public String getText() {
	return getBrowsePanelHTML().getElement().getInnerText();
    }
    
    public void setText(String text) {
	getBrowsePanelHTML().getElement().setInnerText(text);
    }

    public HTMLPanel getBrowsePanelHTML() {
	return browsePanelHTML;
    }

    public void setBrowsePanelHTML(HTMLPanel browsePanelHTML) {
	this.browsePanelHTML = browsePanelHTML;
//	if (browsePanelHTML != null) {
//	    Utils.updateWidthWhenAttached(browsePanelHTML);
//	}
    }

    public LoadResourcePage getIncrementalLoadResourcePage() {
	return incrementalLoadResourcePage;
    }

    public void setIncrementalLoadResourcePage(LoadResourcePage incrementalLoadResourcePage) {
	this.incrementalLoadResourcePage = incrementalLoadResourcePage;
    }

    public String getCurrentURLBase() {
	if (currentURLBase == null) {
	    if (Modeller.defaultURLBase == null) {
		String locationHREF = Window.Location.getHref();
		currentURLBase = CommonUtils.getBaseURL(locationHREF);
	    } else {
		currentURLBase = Modeller.defaultURLBase;
	    }
	}
	return currentURLBase;
    }

    public void setCurrentURLBase(String currentURLBase) {
	this.currentURLBase = currentURLBase;
    }

    protected BrowsePanelEdited getDoAfterSaving() {
        return doAfterSaving;
    }

    protected void setDoAfterSaving(BrowsePanelEdited doAfterSaving) {
        this.doAfterSaving = doAfterSaving;
    }

    public String getCurrentURL() {
	if (microBehaviour != null) {
            return microBehaviour.getUrl();
        } else {
            return currentURL;
        }
    }
    
    public String getAllURLs() {
	if (microBehaviour != null) {
            return microBehaviour.getAllURLs();
        } else {
            return currentURL;
        }
    }

    public void setCurrentURL(String newURL) {
	if (newURL.equals(currentURL)) {
	    return;
	}
        currentURL = newURL;
        Modeller.addURLToPanelMap(newURL, this);
        if (microBehaviour != null) {
            microBehaviour.setUrl(newURL);
        }
        updateTaggingAndURLWidgets();
    }
    
    protected void setBehaviourName(String newName, MicroBehaviourView newMicroBehaviour, String encodedBehaviourName) {
	this.encodedBehaviourName = encodedBehaviourName; // for copying micro-behaviours
	setTabName(newName); 
	// was Utils.goodTabName(newName) but since this is HTML
	// other things (e.g. src='fd_test.png') may get changed
	// following is obsolete now that micro-behaviours are copied when added
//	if (previousURL != null && !previousURL.equals(currentURL)) {
//	    Modeller.renameAllMicroBehaviourViews(previousURL, newName, newMicroBehaviour, currentURL);
//	}
	setMicroBehaviour(newMicroBehaviour);
	if (newMicroBehaviour != null) { 
	    newMicroBehaviour.setNameHTMLAndDescription(newName);
//	    copyButton.addClickHandler(new ClickHandler() {
//		
//		public void onClick(ClickEvent event) {
//		    copyMicroBehaviour();
//		} 
//		
//	    });
//	    if (codeTextAreas.isEmpty() && newMicroBehaviour.getMacroBehaviourViews().isEmpty()) { 
//		// no point to doing this without text areas or lists of micro-behaviours
//		copyButton.setEnabled(false);
//		copyButton.setTitle(Modeller.constants.thisPageHasNoEditAreasToCopy());
//	    } else {
//		if (codeTextAreas.isEmpty()) {
//		    copyButton.setTitle(Modeller.constants.clickHereToCreateACopyOfThisPageNoTextAreas());
//		} else if (newMicroBehaviour != null && 
//			   newMicroBehaviour.getMacroBehaviourViews() != null && 
//			   newMicroBehaviour.getMacroBehaviourViews().isEmpty()) {
//		    copyButton.setTitle(Modeller.constants.clickHereToCreateACopyOfThisPageNoMicroBehaviours());
//		} else {
//		    copyButton.setTitle(Modeller.constants.clickHereToCreateACopyOfThisPage());
//		}	    
//	    }
//	    setCellHorizontalAlignment(copyButton, HasHorizontalAlignment.ALIGN_CENTER);
//	    taggingAndEditing.add(Utils.wrapForGoodSize(copyButton));
//	    taggingAndEditing.add(Utils.wrapForGoodSize(editNameButton));
//	    editNameButton.setTitle(Modeller.constants.ClickToGiveThisMicroBehaviourANewName());
	}
//	if (Modeller.pageEditingEnabled && newMicroBehaviour != null) {
//	    // no longer create edit buttons for pages that don't contain a micro-behaviour
//	    editButton.addClickHandler(new ClickHandler() {
//		
//		public void onClick(ClickEvent event) {
//		    createAndSwitchToEditor();
//		}
//	    });
//	    editButton.setTitle(Modeller.constants.clickHereToEditACopyOfThisWebPage());
//	    setCellHorizontalAlignment(editButton, HasHorizontalAlignment.ALIGN_CENTER);
//	    taggingAndEditing.add(Utils.wrapForGoodSize(editButton));
//	}
    }
    
    protected String getTabName() {
	if (tabWidget != null) {
	    return tabWidget.getTabName();
	} else if (getCurrentURL() != null) {
	    return CommonUtils.getFileName(getCurrentURL());
	} else {
	    return "";
	}
    }
    
    protected void setTabName(String nameHTMLAndDescription) {
	if (tabWidget != null) {
	    String cleanNewName = CommonUtils.getNameHTML(nameHTMLAndDescription);
	    tabWidget.setTabName(cleanNewName);
	}
	// if null maybe should warn since hasn't been attached
	// or something else is wrong
    }

    public ClosableTab getTabWidget() {
        return tabWidget;
    }

    public void setTabWidget(ClosableTab tabWidget) {
        this.tabWidget = tabWidget;
        if (!isVisible()) {
            tabWidget.setVisible(false);
        }
        setTaggingTitle(tabWidget.getElement().getInnerText());
    }
    
    public void replaceElementWithWidget(String id, com.google.gwt.dom.client.Element element, Widget widget) {
//	this, unlike addAndReplaceElement, leaves the id behind for further updates
	Utils.replaceElementWithWidget(id, element, widget, getBrowsePanelHTML());
    }
    
    public void createDeltaPage() {
	createDeltaPage(microBehaviour, new CreateDeltaPageCommand(), false, false);
    }
       
    public void createDeltaPage(MicroBehaviourView microBehaviourView, 
	                        CreateDeltaPageCommand createDeltaPageCommand, 
	                        boolean subMicroBehavioursNeedNewURLs,
	                        boolean forCopying) {
   	createDeltaPage(microBehaviourView,
   		        microBehaviourView.getNameHTML(), 
   		        microBehaviourView.getUrl(), 
   		        microBehaviourView.getTextAreaValues(),
   		        microBehaviourView.getEnhancements(),
   		        microBehaviourView.getMacroBehaviourViews(),
   		        subMicroBehavioursNeedNewURLs,
   		        createDeltaPageCommand,
   		        forCopying);
       }
    
    public void createDeltaPage(final MicroBehaviourView microBehaviourView, 
	    final String nameHTML, 
	    final String oldURL, 
	    final HashMap<Integer, String> textAreaValues, 
	    final ArrayList<MicroBehaviourEnhancement> enhancements, 
	    final ArrayList<MacroBehaviourView> macroBehaviours,
	    final boolean subMicroBehavioursNeedNewURLs,
	    final CreateDeltaPageCommand browsePanelCommand,
	    final boolean forCopying) {
	microBehaviourView.setWaitingToBeCopied(true);
	final AsyncCallbackNetworkFailureCapable<DeltaPageResult> callback = 
		new AsyncCallbackNetworkFailureCapable<DeltaPageResult>() {
	    
	    @Override
	    public void onFailure(Throwable caught) {
		NetworkFailure.instance().networkFailure(caught, Modeller.constants.copyingAMicroBehaviour(), getTimer());		    
	    }

	    @Override
	    public void onSuccess(DeltaPageResult deltaPageResult) {
		super.onSuccess(deltaPageResult);
		String error = deltaPageResult.getErrorMessage();
		if (error != null) {
		    Modeller.addToErrorLog(error);
		} else {
		    String newURL = deltaPageResult.getNewURL();
		    if (browsePanelCommand != null) {
			browsePanelCommand.execute(microBehaviourView, deltaPageResult, false, subMicroBehavioursNeedNewURLs, forCopying);
			microBehaviourView.setCopyMicroBehaviourWhenExportingURL(false);
//		        System.out.println("delta page created. old = " + oldURL + " new= " + newURL); // debug this
			Modeller.mainTabPanel.updatePreviouslyVisitedTabsURL(oldURL, newURL);
			if (browsedURLs.contains(oldURL)) {
			    // oldURL of some resource page
			    if (!oldURL.equals(newURL)) {
//				microBehaviour.copied(newURL);
				new ReplaceURLEvent(microBehaviourView, oldURL).addToHistory();
			    }
			}
		    } else {
			microBehaviour.newURL(newURL, forCopying);
		    }
		}
		microBehaviourView.setWaitingToBeCopied(false);
	    }
	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		createDeltaPage(microBehaviourView, 
			        nameHTML, 
			        oldURL, 
			        textAreaValues, 
			        enhancements, 
			        macroBehaviours,
			        subMicroBehavioursNeedNewURLs,
			        browsePanelCommand,
			        callback);
	    }

	});
    }
    
    public static void addBrowsedURLs(String urlString) {
	if (!browsedURLs.contains(urlString)) {
	    browsedURLs.add(urlString);
	}
    }
	
    private void createDeltaPage(final MicroBehaviourView microBehaviourView, 
                                 final String nameHTML, 
                                 final String oldURL, 
                                 final HashMap<Integer, String> textAreaValues, 
                                 final ArrayList<MicroBehaviourEnhancement> enhancements, 
                                 final ArrayList<MacroBehaviourView> macroBehaviours,
                                 final boolean subMicroBehavioursNeedNewURLs,
                                 final CreateDeltaPageCommand browsePanelCommand,
                                 final AsyncCallbackNetworkFailureCapable<DeltaPageResult> callback) {
	final HashMap<MicroBehaviourView, MicroBehaviourView> dirtyMicroBehaviours = new HashMap<MicroBehaviourView, MicroBehaviourView>();
	MicroBehaviourListCommand command = new MicroBehaviourListCommand() {

	    @Override
	    public void createDeltaPage(ArrayList<ArrayList<String>> microBehaviourURLs) {
		if (dirtyMicroBehaviours.isEmpty()) {
		    Modeller.getResourcePageService().createDeltaPage(
			    nameHTML,
			    oldURL,
			    Modeller.userGuid,
			    Modeller.sessionGuid, 
			    textAreaValues,
			    enhancements,
			    microBehaviourURLs,
			    subMicroBehavioursNeedNewURLs,
			    GWT.getHostPageBaseURL(),
			    Modeller.cachingEnabled,
			    Modeller.internetAccess,
			    callback);
		} else {   
		    Command commandToCreateDeltaPage = new Command() {

			@Override
			public void execute() {
			    BrowsePanel.this.createDeltaPage(
				    microBehaviourView, 
				    nameHTML, 
				    oldURL, 
				    textAreaValues, 
				    enhancements, 
				    macroBehaviours,
				    false,
				    browsePanelCommand,
				    callback);			    
			}

		    };
		    BehaviourComposer.createDeltaPages(dirtyMicroBehaviours, false, false, commandToCreateDeltaPage);
		}
	    }
	    
	};
	getMicroBehaviours(command, macroBehaviours, dirtyMicroBehaviours);
    }

    private void getMicroBehaviours(MicroBehaviourListCommand command, 
	                            ArrayList<MacroBehaviourView> macroBehaviours,
	                            HashMap<MicroBehaviourView, MicroBehaviourView> dirtyMicroBehaviours) {
	if (macroBehaviours.isEmpty()) {
	    command.createDeltaPage(null);
	    return;
	}
	ArrayList<ArrayList<String>> listOfMicrobehaviours = new ArrayList<ArrayList<String>>(macroBehaviours.size());
	Iterator<MacroBehaviourView> macroBehavioursIterator = macroBehaviours.iterator();
	listOfListOfMicroBehaviours(macroBehavioursIterator, null, null, listOfMicrobehaviours, dirtyMicroBehaviours, command);
    }
    
    public void listOfListOfMicroBehaviours(final Iterator<MacroBehaviourView> macroBehavioursIterator,
	                                    Iterator<MicroBehaviourView> microBehavioursIterator, 
	                                    ArrayList<String> nameAndUrls,
	                                    final ArrayList<ArrayList<String>> listOfMicrobehaviours, 
	                                    final HashMap<MicroBehaviourView, MicroBehaviourView> dirtyMicroBehaviours,
	                                    final MicroBehaviourListCommand command) {
        while (microBehavioursIterator != null || macroBehavioursIterator.hasNext()) {
            // if there are 'inner' iterations to do (microBehavioursIterator) then
            // don't 'outer' iterate (macroBehavioursIterator)
            if (microBehavioursIterator == null) {
        	MacroBehaviourView macroBehaviourView = macroBehavioursIterator.next();
        	String name = macroBehaviourView.getNameHTML();
        	List<MicroBehaviourView> microBehaviours = macroBehaviourView.getMicroBehaviours();
        	if (nameAndUrls == null) {
        	    nameAndUrls = new ArrayList<String>(microBehaviours.size()+1);
        	}
        	listOfMicrobehaviours.add(nameAndUrls);
        	nameAndUrls.add(name);
        	microBehavioursIterator = microBehaviours.iterator();
            }
            while (microBehavioursIterator.hasNext()) {
        	MicroBehaviourView microBehaviour = microBehavioursIterator.next();
        	if (microBehaviour.isCopyMicroBehaviourWhenExportingURL()) {
        	    // dirtyMicroBehaviours doubles as freshCopies when copying micro behaviours and it needs the hash table
        	    dirtyMicroBehaviours.put(microBehaviour, microBehaviour);
        	}
        	if (microBehaviour.isWaitingToBeCopied()) {
        	    if (microBehaviour.getSharedState() != this.getMicroBehaviour().getSharedState()) {
        		Utils.logServerMessage(Level.SEVERE, "Sending server the URLs on a micro behaviour page but page is waiting for a new URL. Old URL=" + microBehaviour.getUrl());
        	    }
        	    // continue added as an attempt to address Issue 841
        	    continue;
        	}
//        	if (microBehaviour.isMacroBehaviour()) {
        	    // no need to do anything since 'URL' encoding its name will be added below
//        	    // TODO: make sure these micro-behaviours are up-to-date
//        	    MacroBehaviourView macroBehaviourViewedAsMicroBehaviour = microBehaviour.getMacroBehaviourViewedAsMicroBehaviour(true);
//        	    ArrayList<MicroBehaviourView> microBehaviours = macroBehaviourViewedAsMicroBehaviour.getMicroBehaviours();
//        	    for (MicroBehaviourView innerMicroBehaviour : microBehaviours) {
//                	String url = innerMicroBehaviour.getUrl();
//                	String activeSign = innerMicroBehaviour.isActive() ? "" : "-";
//                	nameAndUrls.add(activeSign + URL.encode(url));
//        	    }
//        	} else if (microBehaviour.isCopyMicroBehaviourWhenExportingURL()) {
        	    // shouldn't happen any more when running a model or 
        	    // when copying a micro-behaviour with dirty 'children'
//        	    ArrayList<MicroBehaviourView> dirtyMicroBehaviours = new ArrayList<MicroBehaviourView>();
//        	    if (!microBehaviour.getMacroBehaviourViews().isEmpty()) {
//        		// a bit wasteful but one way to see what micro-behaviours can be reached
//        		// that are also dirty
//			microBehaviour.getModelXML(dirtyMicroBehaviours);
//        	    } else {
//        		dirtyMicroBehaviours.add(microBehaviour);
//        	    }
//        	    final Iterator<MicroBehaviourView> currentMicroBehavioursIterator = microBehavioursIterator;
//        	    final ArrayList<String> currentNameAndUrls = nameAndUrls;
//        	    Command commandAfterAllCopied = new Command() {
//
//			@Override
//			public void execute() {
//			    String activeSign = microBehaviour.isActive() ? "" : "-";
//			    String url = microBehaviour.getUrl();
//	        	    currentNameAndUrls.add(activeSign + URL.encode(url));
//			    listOfListOfMicroBehaviours(
//				    macroBehavioursIterator,
//				    currentMicroBehavioursIterator,
//				    currentNameAndUrls,
//				    listOfMicrobehaviours, 
//				    command);
//			}
//        		
//        	    };
//		    BehaviourComposer.createDeltaPages(dirtyMicroBehaviours, false, commandAfterAllCopied);
		    // commandAfterAllCopied will continue the iterations
//		    return;
//        	} 
        	String url = microBehaviour.getUrl();
        	boolean inactiveURL = url.charAt(0) == '-';
               	boolean inactive = !microBehaviour.isActive();
        	String activeSign = (inactive && !inactiveURL) ? "-" : "";
        	if (!inactive && inactiveURL) {
        	    url = url.substring(1);
        	}
        	// could add micro-behaviour name info here so that server
        	// has to construct names based upon NetLogo procedure names
        	// but these URLs are typically (always?) in this format already
        	// See Issue 818
        	nameAndUrls.add(activeSign + URL.encode(url));
            }
            microBehavioursIterator = null;
            nameAndUrls = null;
        }
        if (command != null) {
            command.createDeltaPage(listOfMicrobehaviours);
        }
    }
    
    public Widget createAndSwitchToEditor(boolean newPage, boolean editMicroBehaviour, boolean firstTime) {
	Widget widget;
	if (microBehaviour == null && !newPage) {
	    widget = createAndSwitchToFullPageEditor();
	} else {
	    widget = createAndSwitchToMicroBehaviourEditor(editMicroBehaviour, firstTime);
	}
	Modeller.mainTabPanel.add(widget, Modeller.constants.resourceEditor());
	Modeller.mainTabPanel.switchTo(widget);
	return widget;
    }

    protected Widget createAndSwitchToMicroBehaviourEditor(boolean editMicroBehaviour, boolean firstTime) {
	return new MicroBehaviourEditor(this, editMicroBehaviour, firstTime);
    }

    protected String getCode() {
	int codeTokenStart = originalHTML.indexOf("Begin NetLogo code:");
	if (codeTokenStart < 0) {
	    return null;
	}
	int codeStart = originalHTML.indexOf("\n", codeTokenStart);
	if (codeStart < 0) {
	    return null;
	}
	codeStart++; // skip new line character
	int endTokenStart = originalHTML.indexOf("End NetLogo code", codeStart);
	if (endTokenStart < 0) {
	    return null;
	}
	while (endTokenStart > 0) {
	    endTokenStart--;
	    if (originalHTML.charAt(endTokenStart) == '\n') {
		// remove markup and empty lines
		return CommonUtils.removeHTMLMarkup(originalHTML.substring(codeStart, endTokenStart)).replace("\r\n", "\n").replaceAll("[\n]+", "\n");
	    }
	}
	return null;
    }

    protected String getDescription() {
	return microBehaviour.getDescription();
    }
    
    protected String getNameHTML() {
	return microBehaviour.getNameHTML();
    }

    protected Widget createAndSwitchToFullPageEditor() {
	// TODO: determine if this can still be called
	final RichTextEntry richText = new RichTextEntry(this, originalHTML, getRichTextEntryHeight());
	final ScrollPanel panel = new ScrollPanel(richText);
	richText.addSaveButtonClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		Modeller.mainTabPanel.remove(panel);
		BrowsePanel.this.acceptHTML(richText.getRichTextArea());
		if (BrowsePanel.this instanceof NewMicroBehaviourBrowsePanel) {
		    BrowsePanelEdited command = new BrowsePanelEdited() {

			@Override
			public void edited(final BrowsePanel panel) {
			    final MicroBehaviourView microBehaviour = panel.getMicroBehaviour();
			    if (microBehaviour != null) {
				Command afterOpeningCommand = new Command() {

				    @Override
				    public void execute() {
					Modeller.mainTabPanel.remove(panel);
					Modeller.mainTabPanel.switchTo(panel);
					panel.getTabWidget().setVisible(true);
				    }
				    
				};
				microBehaviour.openInBrowsePanel(true, afterOpeningCommand, true);		
			    }
			}
			
		    };
		    setDoAfterSaving(command);
		}
		Modeller.mainTabPanel.switchTo(BrowsePanel.this);
	    }
	});
	richText.addCancelButtonClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		editCanceled(panel);
	    }
	});
	return richText;
    }
    
    protected int getRichTextEntryHeight() {
	// subclasses may override this
	return getOffsetHeight();
    }

    public void addCodeTextArea(int indexInCode, CodeTextArea codeTextArea) {
	codeTextAreas.put(indexInCode, codeTextArea);
    }
    
    public void updateTextArea(int indexInCode, String newContents, boolean initialising) {
//	if (microBehaviour != null && initialising && microBehaviour.textHasBeenUpdated(indexInCode)) {
//	    return; // event reconstruction has already updated this
//	}
	if (indexInCode >= 0) {
	    // not the name (index -1)
	    CodeTextArea codeTextArea = codeTextAreas.get(indexInCode);
	    if (codeTextArea != null) { // could it be otherwise?
		codeTextArea.setCurrentContents(newContents);
		if (microBehaviour != null) {
		    microBehaviour.updateTextArea(newContents, indexInCode);
		}
		// commented out the following since it produced spurious warnings when the micro-behaviour was enhanced
		// since while opening this may be called before the enhancement is 'installed'
//	    } else {
//		System.out.println(
//			"Didn't expect codeTextArea in updateTextArea to be null. Browsing " + 
//			getTabName() + " " + currentURL);
	    }
	}
	if (!initialising) {
	    if (CommonUtils.hasChangesGuid(getCurrentURL())) {
		copyMicroBehaviourWhenExportingURL();
	    } else if (microBehaviour != null && microBehaviour.getContainingMacroBehaviour() != null) {
		// we have an uncopied micro-behaviour that is a part of a macro-behaviour
		// so replace it with a fresh copy
		microBehaviour.setCopyMicroBehaviourWhenExportingURL(false);
		createDeltaPage();
	    }
	}
    }

    public String getTaggingTitle() {
        return taggingTitle;
    }

    public void setTaggingTitle(String taggingTitle) {
        this.taggingTitle = taggingTitle;
    }
    
    protected void updateTaggingAndURLWidgets() {
	if (!okToAddTaggingButtons) {
	    return;
	}
	if (!Modeller.instance().isGetLinksPanelToBeAdded()) {
	    return;
	}
	if (browsePanelToolBar == null) {
	    browsePanelToolBar = new VerticalPanel();
	}
	browsePanelToolBar.clear();
//	if (!isTemporary()) {
//	    if (taggingButtons == null) {
//		taggingButtons = taggingButtons();
//	    }
//	}
	if (splitScreenOptions == null) {
	    splitScreenOptions = new SplitScreenOptions(this);  
	}
	addGetLinksPanel();
	browsePanelToolBar.add(splitScreenOptions);
	panelContents.add(browsePanelToolBar);
    }

    protected void addGetLinksPanel() {
	if (!Modeller.instance().isGetLinksPanelToBeAdded()) {
	    return;
	}
	if (urlDisclosurePanel == null) {    
	    urlDisclosurePanel = 
		    Modeller.createEmbeddingInfo(
			    Modeller.constants.getLinks(),
			    Modeller.constants.loadingPleaseWait(),
			    Modeller.constants.thisURLCanBeUsedToStartTheBehaviourComposerWithThisPageAdded());
	    addOpenListenerToCreateTabLink();
//	} else {
//	    urlDisclosurePanel.setOpen(true);
	}
	if (urlDisclosureAndTaggingButtons == null) {
	    urlDisclosureAndTaggingButtons = new HorizontalPanel();
	    urlDisclosureAndTaggingButtons.setSpacing(6);
	    urlDisclosureAndTaggingButtons.add(urlDisclosurePanel);
	}
	browsePanelToolBar.add(urlDisclosureAndTaggingButtons);
//	urlDisclosureAndTaggingButtons.add(new HTML(Modeller.NON_BREAKING_SPACE + Modeller.constants.tagThisPage()));
//	if (!isTemporary()) {
//	    urlDisclosureAndTaggingButtons.add(taggingButtons);
//	}
	
    }
    
    private void addOpenListenerToCreateTabLink() {
	OpenHandler<DisclosurePanel> handler = new OpenHandler<DisclosurePanel>() {

	    @Override
	    public void onOpen(OpenEvent<DisclosurePanel> event) {
		if (isCopyMicroBehaviourWhenExportingURL()) { // copyOnUpdate && 
		    CreateDeltaPageCommand browsePanelCommand =
			new CreateDeltaPageCommand() {

			    @Override
			    public void execute(MicroBehaviourView microBehaviourView,
				                DeltaPageResult deltaPageResult, 
				                boolean panelIsNew,
				                boolean subMicroBehavioursNeedNewURLs,
				                boolean forCopying) {
				String newURL = deltaPageResult.getNewURL();
				updateURLDisclosurePanelContent(newURL);
				super.execute(microBehaviourView, deltaPageResult, panelIsNew, subMicroBehavioursNeedNewURLs, forCopying);
				// is already open so no need to try to reset it
//				urlDisclosurePanel.setOpen(true);
			    }
			
		    };
		    createDeltaPage(microBehaviour, browsePanelCommand, false, false);
		} else {  
		    updateURLDisclosurePanelContent(getCurrentURL());
		}	
	    }
	    
	};
	urlDisclosurePanel.addOpenHandler(handler);
    }

    public boolean isCopyMicroBehaviourWhenExportingURL() {
	if (microBehaviour == null) {
	    return false;
	} else {
	    return microBehaviour.isCopyMicroBehaviourWhenExportingURL();
	}
    }

    protected String getEncodedBehaviourName() {
        return encodedBehaviourName;
    }

    protected void setEncodedBehaviourName(String encodedBehaviourName) {
        this.encodedBehaviourName = encodedBehaviourName;
    }
    
    public MicroBehaviourView getMicroBehaviour() {
        return microBehaviour;
    }

    public void setMicroBehaviour(MicroBehaviourView microBehaviour) {
//	if (this.microBehaviour != null) {
//	    System.out.println(this.hashCode() + " had old micro-behaviour: " + this.microBehaviour.getUrl());
//	}
	if (this.microBehaviour != null && microBehaviour != null) {
	    if (CommonUtils.removeBookmark(this.microBehaviour.getUrl()).equals(CommonUtils.removeBookmark(microBehaviour.getUrl()))) {
		microBehaviour.setWarnThatTextAreasHaveChanged(this.microBehaviour.isWarnThatTextAreasHaveChanged());	
	    }
	}
        this.microBehaviour = microBehaviour;
//        System.out.println(this.hashCode() + " has new micro-behaviour: " + this.microBehaviour.getUrl());
        if (tabWidget != null) {
            tabWidget.setMicroBehaviour(microBehaviour);
        }
        if (microBehaviour != null) {
            if (Modeller.debuggingEnabled) {
        	String allURLs = microBehaviour.getAllURLs();
        	boolean urlFound = false;
        	for (String url : allURLs.split(";")) {
        	    if (getCurrentURL().contains(url)) {
        		urlFound = true;
        		break;
        	    }
        	}
        	if (!urlFound) {
        	    // this can happen when after editing a local MB page one clicks on 'save as copy'
        	    Modeller.addToDebugMessages("Browse panel's URL: " + currentURL + " doesn't share a URL with " + allURLs);
        	}
            }
            microBehaviour.setCopyOnUpdate(isCopyOnUpdate());
            microBehaviour.setOriginalTextAreasCount(textAreasCount());
            //  this fixes the bug that ?tab= didn't use the updated name
            if (tabWidget == null) {
        	setTabWidget(new ClosableTab(microBehaviour.getPlainName(), this, Modeller.mainTabPanel));
        	tabWidget.setMicroBehaviour(microBehaviour);
            }
        }
    }
    
    protected HashMap<Integer, String> getTextAreaValues() {
	if (microBehaviour == null) {
	    return textAreaValuesOfPanel;
	} else {
	    return microBehaviour.getTextAreaValues();
	}
    }
    
    protected ArrayList<MicroBehaviourEnhancement> getEnhancements() {
	if (microBehaviour == null) {
	    return enhancementsOfPanel;
	} else {
	    return microBehaviour.getEnhancements();
	}
    }
    
    public ArrayList<MacroBehaviourView> getMacroBehaviourViews() {
	if (microBehaviour == null) {
	    return null; // macroBehaviourViews;
	} else {
	    return microBehaviour.getMacroBehaviourViews();
	}
    }
    
    public MacroBehaviourView getMacroBehaviourView(String name) {
	ArrayList<MacroBehaviourView> macroBehaviours = getMacroBehaviourViews();
	if (macroBehaviours == null) {
	    return null;
	}
	for (MacroBehaviourView macroBehaviour : macroBehaviours) {
	    if (macroBehaviour.getNameHTML().equals(name)) {
		return macroBehaviour;
	    }
	}
	return null;
    }

    public boolean isOkToRemoveIfTabBarFull() {
	// don't automatically remove tabs that are guides or libraries
	// if temporary will be removed anyway so don't consider it 
        return okToRemoveIfTabBarFull && 
               !temporary && 
               microBehaviour != null &&
               this != Modeller.getProtectedBrowsePanel() &&
               !microBehaviour.isDirty(); // and not edited since last save
    }

    public void setOkToRemoveIfTabBarFull(boolean okToRemoveIfTabBarFull) {
        this.okToRemoveIfTabBarFull = okToRemoveIfTabBarFull;
    }

    protected void setMacroBehaviourViews(ArrayList<MacroBehaviourView> macroBehaviourViews) {
//        this.macroBehaviourViews = macroBehaviourViews;
        if (microBehaviour != null) {
            // ok to share the same list they are the same
            // but since the panel can be created with macroBehaviourViews
            // before the microBehaviour is found there needs to be two copies
            // at least initially
            microBehaviour.setMacroBehaviourViews(macroBehaviourViews);
        }
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }
    
    @Override
    public void onLoad() {
	super.onLoad();
	updateRadioButtonSelection();
	restoreScrollPosition();
//	setPixelSize(Utils.getTabPanelWidth(), Utils.getTabPanelHeight());
    }

    public void saveScrollPosition() {
	scrollTop = Window.getScrollTop();
	scrollLeft = Window.getScrollLeft();
    }
    
    public void restoreScrollPosition() {
	if (isTemporary()) {
	    return;
	}
	// this works around a problem that the panel may be rendering while
	// this is called so the scroll position doesn't exist yet
	// so it delays and tries twice
	// See discussion of this issue at
	// http://www.mail-archive.com/google-web-toolkit-contributors@googlegroups.com/msg02117.html
//	if (!isVisible() && Modeller.isAlertsLineEmpty()) {
//	    // still loading
//	    Modeller.setAlertsLine(Modeller.constants.loadingPleaseWait());
//	}
	final Timer timer = new Timer() {
	    
	    private int secondDelay = 500;

	    @Override
	    public void run() {
		ModellerTabPanel tabPanel = getModellerTabPanel();
		if (tabPanel != null) {
		    if (tabPanel.getCurrentPanel() == BrowsePanel.this) {
			Window.scrollTo(scrollLeft, scrollTop);
//			System.out.println("restored top: " + scrollTop + " " + currentURL);
		    }
		}
		if (secondDelay > 0 && (Window.getScrollLeft() != scrollLeft || Window.getScrollTop() != scrollTop)) {
		    // try one more time
		    schedule(secondDelay);
		    // don't want to keep repeating this
		    // which might repeatedly move the scroll bar
		    secondDelay = 0;
		}
	    }
	    
	};
	timer.schedule(100);
    }
    
//    public void copyMicroBehaviour(MicroBehaviourView microBehaviour, String nameHTML) {
//	createCopy(microBehaviour, processedHTML, endId, prefixId, nameHTML);
//    }
    
    public ModellerTabPanel getModellerTabPanel() {
	Widget ancestor = getParent();
	while (ancestor != null && !(ancestor instanceof ModellerTabPanel)) {
	    ancestor = ancestor.getParent();
	}
	if (ancestor != null) {
	    return (ModellerTabPanel) ancestor;
	} else {
	    return null;
	}
    }
    
//    public void renameMicroBehaviour(String newNameHTML) {
//	createAndSwitchToNameEditor(processedHTML, endId, prefixId, currentURL, newNameHTML);
//    }

    public UrlBox addURLBox(String url, boolean select, boolean atTop, boolean closable, boolean addGoButton) {
	String addTabLink = GWT.getHostPageBaseURL() + "?tab=" + CommonUtils.encodeColonAndSlash(url);
	UrlBox urlBox = new UrlBox(addTabLink, this, closable, addGoButton);
	if (atTop) {
	    panelContents.insert(urlBox, 0);
	} else {
	    panelContents.add(urlBox);
	}
	TextBox urlTextBox = urlBox.getUrlTextBox();
	if (select) {
	    urlTextBox.selectAll();
	}
	return urlBox;
    }

    public boolean isOkToAddTaggingButtons() {
        return okToAddTaggingButtons;
    }

    public void setOkToAddTaggingButtons(boolean okToAddTaggingButtons) {
        this.okToAddTaggingButtons = okToAddTaggingButtons;
    }

    public SplitScreenOptions getSplitScreenCheckBox() {
        return splitScreenOptions;
    }
  
    public boolean readyToReuse() {
	if (microBehaviour == null) {
	    return true;
	} else {
	    // ok to reuse if not being copied
	    return !microBehaviour.isWaitingToBeCopied();
	}
    }

    public boolean isExpectingANameChange() {
        return expectingANameChange;
    }

    public void setExpectingANameChange(boolean expectingANameChange) {
        this.expectingANameChange = expectingANameChange;
    }

    public boolean isCopyOnUpdate() {
        return copyOnUpdate;
    }

    public void setCopyOnUpdate(boolean copyOnUpdate) {
        this.copyOnUpdate = copyOnUpdate;
        if (microBehaviour != null) {
            microBehaviour.setCopyOnUpdate(copyOnUpdate);
        }
    }

    public MicroBehaviourView getMicroBehaviourToShareStateWithCopy() {
        return microBehaviourToShareStateWithCopy;
    }

    public void setMicroBehaviourToShareStateWithCopy(MicroBehaviourView microBehaviourToShareStateWithCopy) {
        this.microBehaviourToShareStateWithCopy = microBehaviourToShareStateWithCopy;
    }
    
    public MacroBehaviourView getContainingMacroBehaviour() {
	if (microBehaviour == null) {
	    return null;
	} else {
	    return microBehaviour.getContainingMacroBehaviour();
	}
    }
    
    public int textAreasCount() {
	Set<Entry<Integer, CodeTextArea>> entrySet = codeTextAreas.entrySet();
	int count = 0;
	for (Entry<Integer, CodeTextArea> entry : entrySet) {
	    if (!CommonUtils.TOKEN_FOR_REMOVED_TEXT_AREA.equals(entry.getValue())) {
		count++;
	    }
	}
	return count;
    }

    public void copyMicroBehaviourWhenExportingURL() {
	if (microBehaviour != null) {
	    microBehaviour.setCopyMicroBehaviourWhenExportingURL(true);
	}
	if (urlDisclosurePanel != null) {
	    // close it if it is open since out-of-date
	    urlDisclosurePanel.setOpen(false);
	}
    }

    protected void updateURLDisclosurePanelContent(final String url) {
	String baseURL = CommonUtils.getBaseURL(Window.Location.getHref());
	String addTabLink = CommonUtils.joinPaths(baseURL, "/?tab=") + CommonUtils.encodeColonAndSlash(url);
	VerticalPanel links = new VerticalPanel();
	TextBox composerURLTextBox = Modeller.createDisclosureTextBox(addTabLink);
	composerURLTextBox.setTitle(Modeller.constants.composerURLTextBoxTitle());
	links.add(composerURLTextBox);
	final TextBox simpleURLTextBox = Modeller.createDisclosureTextBox(url);
	if (CommonUtils.hasChangesGuid(url)) {
	    simpleURLTextBox.setTitle(Modeller.constants.simpleURLChangesTextBoxTitle());
	    ChangeHandler changeHandler = new ChangeHandler() {

		@Override
		public void onChange(ChangeEvent event) {
		    final String newURL = simpleURLTextBox.getText();
		    if (!CommonUtils.hasChangesGuid(newURL)) {
			final String alert = Modeller.addAlert(Modeller.constants.newMicroBehaviourBeingCreatedWithCurrentCustomisations());
			final HashMap<MicroBehaviourView, MicroBehaviourView> dirtyMicroBehaviours = new HashMap<MicroBehaviourView, MicroBehaviourView>();
			MicroBehaviourListCommand command = new MicroBehaviourListCommand() {

			    @Override
			    public void createDeltaPage(ArrayList<ArrayList<String>> microBehaviourURLs) {
				AsyncCallbackNetworkFailureCapable<DeltaPageResult> callback = new AsyncCallbackNetworkFailureCapable<DeltaPageResult>() {

				    @Override
				    public void onSuccess(DeltaPageResult deltaPageResult) {
					super.onSuccess(deltaPageResult);
					if (deltaPageResult.getErrorMessage() != null) {
					    Modeller.addToErrorLog(deltaPageResult.getErrorMessage());
					}
					String newURL = deltaPageResult.getNewURL();
					if (newURL != null) {
					    Modeller.removeAlert(alert);
					    if (microBehaviour != null) {
//						String oldURL = microBehaviour.getUrl();
						MacroBehaviourView containingMacroBehaviour = microBehaviour.getContainingMacroBehaviour();
						if (containingMacroBehaviour != null) {
						    String containingURL = containingMacroBehaviour.getMicroBehaviourUrl();
						    if (containingURL != null) {
							MicroBehaviourView containingMicroBehaviourView = BehaviourComposer.instance().getMicroBehaviourView(containingURL);
							if (containingMicroBehaviourView != null) { // should always be true
							    containingMicroBehaviourView.setCopyMicroBehaviourWhenExportingURL(true);
							} else {
							    System.err.println("Expected to find the containing micro-behaviour.");
							}
						    }
						}
						microBehaviour.newURL(newURL, false);
						updateURLDisclosurePanelContent(newURL);
						
					    }
					    Modeller.browseToNewTab(newURL, true, null);
					}
				    }
				    
				};
				Modeller.getResourcePageService().copyMicroBehaviourCustomisations(
					url, 
					newURL, 
					microBehaviourURLs,
					Modeller.userGuid,
					Modeller.sessionGuid,
					GWT.getHostPageBaseURL(),
					Modeller.cachingEnabled,
					Modeller.internetAccess,
					callback);
			    }
			};
			getMicroBehaviours(command, microBehaviour.getMacroBehaviourViews(), dirtyMicroBehaviours);
		    }
		}
		
	    };
	    simpleURLTextBox.addChangeHandler(changeHandler);
	} else {
	    simpleURLTextBox.setTitle(Modeller.constants.simpleURLTextBoxTitle());
	}
	links.add(simpleURLTextBox);
	if (url.contains(CommonUtils.EDITED_HTML)) {
	    String sourceURL = url.replace(".html", ".txt");
	    Anchor anchor = new Anchor(Modeller.constants.openTheHTMLOfThisPage(), sourceURL, "_blank");
	    links.add(anchor);	    
	}
	urlDisclosurePanel.setContent(links);
    }

    public void panelClosed(boolean regardlessOfTemporariness) {
	Modeller.removeFromUrlPanelMap(this.getCurrentURL());
	if (microBehaviour != null && (!temporary|| regardlessOfTemporariness) && isCopyMicroBehaviourWhenExportingURL()) {
	    createDeltaPage(microBehaviour, new CreateDeltaPageCommand(), true, false);
	}
    }

    public void installEnhancements() {
	if (microBehaviour == null || isEnhancementsInstalled()) {
	    // already done
	    return;
	}
	List<MicroBehaviourEnhancement> enhancements = getEnhancements();
	int originalTextAreasCount = microBehaviour.getOriginalTextAreasCount();
	if (enhancements != null) {
	    int previousTextAreaIndex = originalTextAreasCount-1;
	    for (MicroBehaviourEnhancement enhancement : enhancements) {
		previousTextAreaIndex = microBehaviour.enhanceCode(enhancement, this, false, previousTextAreaIndex);
	    }
	    if (!enhancements.isEmpty()) {
		setEnhancementsInstalled(true);
	    }
	}
	addWarningIfNeeded();
    }

    public void addWarningIfNeeded() {
	if (microBehaviour.isWarnThatTextAreasHaveChanged()) { 
	    String warning_string = Modeller.constants.differentTextAreasCountWarning();
	    Warning warning = new Warning(warning_string);
	    Command loadCommand = new Command() {

		@Override
		public void execute() {
		    // been shown - arrange that it won't be shown again
		    microBehaviour.setWarnThatTextAreasHaveChanged(false);
		}
		
	    };
	    warning.addLoadCommand(loadCommand);
	    warningPanel.setWidget(warning);
	    // warningPanel isn't always attached so added following:
	    Modeller.addAlert(warning_string);
	} else {
	    warningPanel.setWidget(null);
	}
    }

    public void updateParameterRadioButtons(CodeTextArea codeTextArea) {
	parameterTextArea = codeTextArea;
	if (isAttached()) {
	    updateRadioButtonSelection();
	}
    }

    @SuppressWarnings("deprecation")
    protected void updateRadioButtonSelection() {
	if (parameterTextArea != null) {
	    String parameterName = parameterTextArea.getCurrentContents();
	    if (parameterName.indexOf("(") < 0) {
		return;
	    }
	    if (parameterName.indexOf(" (controlled by a slider)") >= 0) {
		// behave as if clicked
		com.google.gwt.user.client.Element sliderRadioButton = DOM.getElementById("slider radio button");
		if (sliderRadioButton != null) {
		    JavaScript.click(sliderRadioButton);
		}
	    } else if (parameterName.indexOf(" (controlled by an input box)") >= 0) {
		// behave as if clicked
		com.google.gwt.user.client.Element sliderRadioButton = DOM.getElementById("input box radio button");
		if (sliderRadioButton != null) {
		    JavaScript.click(sliderRadioButton);
		}
	    } else if (parameterName.indexOf(" (controlled by a switch)") >= 0) {
		// behave as if clicked
		com.google.gwt.user.client.Element sliderRadioButton = DOM.getElementById("switch radio button");
		if (sliderRadioButton != null) {
		    JavaScript.click(sliderRadioButton);
		}
	    }
	    // the default is no interface so shouldn't need to do anything
	}
    }

    public void refresh() {
	// should act like closing the panel and re-opening it
	if (isCopyMicroBehaviourWhenExportingURL() || !CommonUtils.hasChangesGuid(currentURL)) {
	    // currentURL is not up-to-date so create new one and use it
	    refreshRegardless();
	} else {
	    refreshCurrentURL();
	}
    }

    public void refreshRegardless() {
	CreateDeltaPageCommand command = new CreateDeltaPageCommand() {

	    @Override
	    public void execute(MicroBehaviourView microBehaviourView, DeltaPageResult deltaPageResult, boolean panelIsNew, boolean subMicroBehavioursNeedNewURLs, boolean forCopying) {
		setCurrentURL(deltaPageResult.getNewURL());
		super.execute(microBehaviourView, deltaPageResult, panelIsNew, subMicroBehavioursNeedNewURLs, forCopying);
		refreshCurrentURL();
	    }

	};
	microBehaviour.setCopyMicroBehaviourWhenExportingURL(false);
	createDeltaPage(microBehaviour, command, false, false);
    }
    
    public NodeList<Element> getPreElements() {
	if (getBrowsePanelHTML() == null) {
	    // not (yet) loaded
	    return null;
	}
	Element element = getBrowsePanelHTML().getElement();
	NodeList<Element> preElements = element.getElementsByTagName("pre");
	if (preElements.getLength() == 0) {
	    return element.getElementsByTagName("PRE");
	} else {
	    return preElements;
	}
    }
    
    /**
     * @return the dimensions of the rightmost and bottommost PRE elements 
     * relative to the browse panel's HTML
     */
    public Dimensions getPreElementsDimensions() {
	NodeList<Element> preElements = getPreElements();
	if (preElements == null) {
	    return new Dimensions(0, 0);
	}
	int bottom = 0;
	int right = 0;
	int length = preElements.getLength();
	for (int i = 0; i < length; i++) {
	    Element element = preElements.getItem(i);
	    int elementRight = element.getAbsoluteRight();
	    if (elementRight > right) {
		right = elementRight;
	    }
	    int elementBottom = element.getAbsoluteBottom();
	    if (elementBottom > bottom) {
		bottom = elementBottom;
	    }
	}
	int panelLeft = getBrowsePanelHTML().getAbsoluteLeft();
	int panelTop = getBrowsePanelHTML().getAbsoluteTop();
	return new Dimensions(right-panelLeft, bottom-panelTop);
    }

    /**
     * Changes the visibility of extra widgets (tab, tags, urls, ...)
     * 
     * @param visible
     */
    public void setExtraWidgetsVisible(boolean visible) {
	if (tabWidget != null) {
	    tabWidget.setVisible(visible);
	}
//	if (taggingButtons != null) {
//	    taggingButtons.setVisible(visible);
//	}
	if (urlDisclosurePanel != null) {
	    urlDisclosurePanel.setVisible(visible);
	}
	if (splitScreenOptions != null) {
	    splitScreenOptions.setVisible(visible);
	}
	if (urlDisclosureAndTaggingButtons != null) {
	    urlDisclosureAndTaggingButtons.setVisible(visible);
	}
    }

    public void refreshCurrentURL() {
	refreshInProgress = true;
	enhancementsOfPanel = null;
	codeTextAreas.clear();
	clear();
	setEnhancementsInstalled(false);
	// and now browse again
	CustomisationPopupPanel customisationPopupPanel = Utils.getAncestorWidget(this, CustomisationPopupPanel.class);
	if (customisationPopupPanel == null) {
	    browseTo(getCurrentURL());
	} else {
	    // close current and re-open
	    customisationPopupPanel.hide();
	    microBehaviour.openCustomisePanel();
	}
    }

    public void addDescriptionArea(TextArea textArea) {
	if (descriptionArea != null) {
	    descriptionArea.removeFromParent();
	}
	descriptionArea = textArea;
	panelContents.insert(textArea, 0);
	warningPanel = new SimplePanel();
	panelContents.insert(warningPanel, 0);
    }

    public boolean isEnhancementsInstalled() {
        return enhancementsInstalled;
    }

    public void setEnhancementsInstalled(boolean enhancementsInstalled) {
        this.enhancementsInstalled = enhancementsInstalled;
    }

    public String getOriginalHTML() {
        return originalHTML;
    }

    public void setOriginalHTML(String originalHTML) {
        this.originalHTML = originalHTML;
    }

    public void editCanceled(final ScrollPanel panel) {
	Modeller.setAlertsLine(Modeller.constants.editCanceled());
	Modeller.mainTabPanel.remove(panel);
	if (BrowsePanel.this instanceof NewMicroBehaviourBrowsePanel) {
	    Modeller.mainTabPanel.remove(BrowsePanel.this);
	} else {
	    Modeller.mainTabPanel.switchTo(BrowsePanel.this);
	}
    }

    public boolean isContainsNetLogoCode() {
        return containsNetLogoCode;
    }

    public void setContainsNetLogoCode(boolean containsNetLogoCode) {
        this.containsNetLogoCode = containsNetLogoCode;
    }

    public boolean containsURL(String url) {
	if (microBehaviour == null) {
	    return false;
	} else {
	    return microBehaviour.containsURL(url, true);
	}
    }

    public void removePanel() {
	int index = Modeller.mainTabPanel.getWidgetIndex(this);
	if (Modeller.mainTabPanel.remove(this)) {
	    Modeller.mainTabPanel.selectTab(Math.min(Modeller.mainTabPanel.getWidgetCount()-1, index));
	}
	panelClosed(false);
    }

    public void updateTextAreaValues() {
	// currently only those that used for the name of a parameter may be out of date
	// due to problems communicating back from JavaScript on Parameter.html to GWT
	Set<Entry<Integer, CodeTextArea>> entrySet = codeTextAreas.entrySet();
	for (Entry<Integer, CodeTextArea> entry : entrySet) {
	    entry.getValue().updateIfParameterName();
	}
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
}
