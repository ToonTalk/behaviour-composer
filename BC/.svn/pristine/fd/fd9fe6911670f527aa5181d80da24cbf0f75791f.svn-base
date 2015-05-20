package uk.ac.lkl.client;

import java.util.HashMap;

import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.user.client.Command;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.core.client.GWT;

public class LoadResourcePage {
    protected BrowsePanel browsePanel;
    protected HTMLPanel htmlPanel;
    protected int elementCount;
    protected int elementCounter;
    protected int prefixId;
    protected HashMap<Integer, String> textAreaValues;
    private boolean copyOnUpdate;
    protected Command commandWhenLoaded;
//    protected JavaScriptObject mopixElements = null;
    
    protected Command nextElementCommand = new Command() {

	@Override
	public void execute() {
	    executeRepeatedly();		    
	}
	
    };

    public LoadResourcePage(
	    BrowsePanel browsePanel, 
	    int endId, 
	    int prefixId, 
	    HashMap<Integer, String> textAreaValues,
	    boolean copyOnUpdate,
	    Command commandWhenLoaded) {
	elementCounter = 1;
	elementCount = endId;
	this.browsePanel = browsePanel;
	this.prefixId = prefixId;
	this.textAreaValues = textAreaValues;
	this.copyOnUpdate = copyOnUpdate;
	this.commandWhenLoaded = commandWhenLoaded;
//	displayProgress();
    }
    
//    protected void displayProgress() {
//	if (browsePanel == null || browsePanel.getLoadingHTML() == null) {
//	    return;
//	}
//	int countLeft = elementCount - elementCounter;
//	if (countLeft > 1) {
//	    String newHTML = CommonUtils.emphasise(Modeller.constants.loadingPleaseWait() + 
//		                                   Modeller.NON_BREAKING_SPACE + "(" + countLeft + ")");
//	    browsePanel.getLoadingHTML().setHTML(newHTML);
//	}
//    }

    public void execute() {
	htmlPanel = browsePanel.getBrowsePanelHTML();
	if (htmlPanel == null) {
	    Modeller.addToErrorLog("Browse panel has no HTMLPanel");
	    return;
	}
	// need to 'protect' HTML markup and NetLogo comments from this processing
	String html = browsePanel.getProcessedHTML();
	String startToken = "<pre id=\"before_code_element\"></pre>";
	String endToken = "<pre id=\"after_code_element\"></pre>";
	String lowerCaseHTML = html.toLowerCase();
	int startTokenIndex = lowerCaseHTML.indexOf(startToken);
	if (startTokenIndex >= 0) {
	    int codeStart = startTokenIndex + startToken.length();
	    int codeEnd = lowerCaseHTML.lastIndexOf(endToken);
	    if (codeEnd > 0) {
		String code = html.substring(codeStart, codeEnd);
		String documentedCode = Utils.addLinksToDocumentation(code);
		if (documentedCode != code) {
		    String newHTML = html.substring(0, codeStart) + documentedCode + html.substring(codeEnd);
		    browsePanel.setHTML(newHTML);
		    browsePanel.setContainsNetLogoCode(!documentedCode.equals("<pre></pre>"));
		} else {
		    browsePanel.setContainsNetLogoCode(true);
		}
	    }
	}
	executeRepeatedly();
    }
    
    public void executeRepeatedly() {
	while (executeOnce()) { 
	}
    }

    /* 
     * @return true if caller should process next element
     * false if either finished or an asynchronous call will take care of further processing
     */
    public boolean executeOnce() {
	if (elementCounter > elementCount) { // done
	    runCommandWhenLoaded();
	    return false;
	}
	Element codeElement = null;
	try {
	    String id = CommonUtils.MODELLER_ID_PREFIX + prefixId + "_" + elementCounter++;
	    codeElement = htmlPanel.getElementById(id);
	    if (codeElement == null) {
		browsePanel.setIncrementalLoadResourcePage(null);
		System.out.println("No element found with ID: " + id);
		return true; // ignore -- shouldn't have been generated? warn??
	    }
	    String innerHTML = codeElement.getInnerHTML();
	    if (Modeller.instance().processNonGenericCodeElement(
		    codeElement, innerHTML, browsePanel, id, copyOnUpdate, nextElementCommand)) {
		// processNonGenericCodeElement took care of next element
		return false;
	    }
	    String hyperlink = codeElement.getAttribute("hyperlink");
	    if (hyperlink != null && !hyperlink.isEmpty()) {
		Widget widget = null;
		CommandAnchor anchor = new CommandAnchor(innerHTML);
		if (hyperlink.contains(BehaviourComposer.RESOURCES_ANCHOR)) {
		    Modeller.becomeResourcesLink(anchor);
		    widget = anchor;
		} else if (hyperlink.contains(BehaviourComposer.COMPOSER_ANCHOR)) {
		    BehaviourComposer.becomeBehaviourComposerLink(anchor);
		    widget = anchor;
		} else if (hyperlink.contains(BehaviourComposer.HISTORY_ANCHOR)) {
		    BehaviourComposer.becomeHistoryLink(anchor);
		    widget = anchor;
		} else if (hyperlink.contains(BehaviourComposer.MODELS_ANCHOR)) {
		    BehaviourComposer.becomeModelsLink(anchor);
		    widget = anchor;
		} else if (hyperlink.contains(BehaviourComposer.SETTINGS_ANCHOR)) {
		    BehaviourComposer.becomeSettingsLink(anchor);
		    widget = anchor;
		} else if (hyperlink.contains(BehaviourComposer.HELP_ANCHOR)) {
		    BehaviourComposer.becomeHelpLink(anchor);
		    widget = anchor;	
		} else if (hyperlink.contains(BehaviourComposer.SEARCH_ANCHOR)) {
		    BehaviourComposer.becomeSearchLink(anchor);
		    widget = anchor;
		} else if (hyperlink.charAt(0) == '#') {
		    return true;
//	            widget = new LocalHyperlink(innerHTML, bookmark);
		}
		if (widget == null) {
		    widget = new InternalHyperlink(innerHTML, hyperlink, browsePanel, false);
		}
		browsePanel.replaceElementWithWidget(id, codeElement, widget);
		return true;
	    }
	    String originalAction = codeElement.getAttribute("formaction");
	    if (originalAction != null && !originalAction.isEmpty()) {
		FormPanel form = new FormPanel();
		String url = GWT.getModuleBaseURL() + "ResourcePage";
		form.setAction(url);
		String method = codeElement.getAttribute("method");
		if (method != null && method.equalsIgnoreCase("POST")) {
		    form.setMethod(FormPanel.METHOD_POST);
		    form.setEncoding(FormPanel.ENCODING_MULTIPART);
//		    Modeller.addToErrorLog("Can't yet handle POST action in forms.");
		} else {
		    form.setMethod(FormPanel.METHOD_GET);
		}
		if (codeElement.hasChildNodes()) {
		    Element innerElement = codeElement.getFirstChildElement(); // DOM.getChild(codeElement, 0);
		    if (innerElement != null) {
			String actionURL = codeElement.getAttribute("actionurl");
			VerticalPanel panel = new VerticalPanel();
			if (actionURL != null && !actionURL.isEmpty()) {
			    panel.add(new Hidden("actionURL", actionURL));
			}
			panel.add(new HTMLPanel(innerHTML));
			form.setWidget(panel);
			// Add an event handler to the form.
			SubmitCompleteHandler submitHandler = new SubmitCompleteHandler() {

			    @Override
			    public void onSubmitComplete(SubmitCompleteEvent event) {
				String results = event.getResults();
				if (results != null) {
				    if (results.indexOf("Not Found") >= 0) {
					// at least Apache returns this as an error message
					// might be better to just search for ' '
					Modeller.addToErrorLog(results);
				    } else {
					if (Modeller.searchResultsPanel != null) {
					    // so there is only one search results panel
					    Modeller.mainTabPanel.remove(Modeller.searchResultsPanel);
					}
					Modeller.browseToNewTab(Modeller.constants.searchResults(), null, null, CommonUtils.removeHTMLMarkup(results));
					Modeller.searchResultsPanel = Modeller.getSelectedTab();
				    }
				} 			    
			    }	
			};
			form.addSubmitCompleteHandler(submitHandler);    
			browsePanel.replaceElementWithWidget(id, codeElement, form);
		    }
		}
		return true;
	    }
	    String modelID = codeElement.getAttribute("modelRef");
	    if (modelID != null && !modelID.isEmpty()) {
		ModelLoader modelLoader = new ModelLoader(innerHTML, modelID, ModelLoader.BEHAVIOUR_COMPOSER_MODEL_ID);
		modelLoader.setTitle(Modeller.constants.clickToAddThisModel());
		browsePanel.replaceElementWithWidget(id, codeElement, modelLoader);
		return true;		
	    }
	    String sessionID = codeElement.getAttribute("sessionRef");
	    int type = ModelLoader.SESSION_ID;
	    if (sessionID == null || sessionID.isEmpty()) {
		sessionID = codeElement.getAttribute("sessionRefReadOnly");
		type = ModelLoader.READ_ONLY_SESSION_ID;
	    }
	    if (sessionID != null && !sessionID.isEmpty()) {
		ModelLoader modelLoader = new ModelLoader(innerHTML, sessionID, type);
		modelLoader.setTitle(Modeller.constants.clickToReconstructThisSession());
		browsePanel.replaceElementWithWidget(id, codeElement, modelLoader);
//		modelLoader.getElement().setPropertyString("style", "float:left");
//		htmlPanel.addAndReplaceElement(modelLoader, id);
		return true;		
	    }	
	    String tagName = codeElement.getTagName();
	    if (tagName != null && tagName.equalsIgnoreCase("TEXTAREA")) {
		Integer index = Utils.getIntegerAttribute(codeElement, "index");
		if (index == null) {
		    index = 0;
		}
		CodeTextArea textArea = new CodeTextArea((int) index, browsePanel, codeElement);
		String newContents = null;
		if (textAreaValues != null) {
		    newContents = textAreaValues.get((int) index);
		    if (newContents != null) {
			textArea.setCurrentContents(newContents);
		    }
		}
		String title = codeElement.getPropertyString("title");
		if (title == null) {
		    title = Modeller.constants.clickHereToEdit();
		}
		textArea.setTitle(title);
		browsePanel.replaceElementWithWidget(id, codeElement, textArea);
		return true;
	    }
	    if (tagName != null && tagName.equalsIgnoreCase("pre")) {
		String sessionEventsCheckBoxID = codeElement.getAttribute("SessionEventsCheckBoxID");
		if (!sessionEventsCheckBoxID.isEmpty()) {
		    String sessionEventsCheckBoxLabel = codeElement.getAttribute("SessionEventsCheckBoxLabel");
		    String sessionEventsCheckBoxSessionID = codeElement.getAttribute("SessionEventsCheckBoxSessionID");
		    String sessionEventsCheckBoxDoMessage = codeElement.getAttribute("SessionEventsCheckBoxDoMessage");
		    String sessionEventsCheckBoxUndoMessage = codeElement.getAttribute("SessionEventsCheckBoxUndoMessage");
		    String sessionEventsCheckBoxTitle = codeElement.getAttribute("SessionEventsCheckBoxTitle");
		    SessionEventsCheckBox sessionEventsCheckBox = 
			    new SessionEventsCheckBox(sessionEventsCheckBoxID,
				                      sessionEventsCheckBoxLabel, 
				                      sessionEventsCheckBoxSessionID,
				                      sessionEventsCheckBoxDoMessage, 
				                      sessionEventsCheckBoxUndoMessage, 
				                      sessionEventsCheckBoxTitle,
				                      (BehaviourComposer) Modeller.instance());
		    browsePanel.replaceElementWithWidget(id, codeElement, sessionEventsCheckBox);
		}
	    }
	    return true;
	} catch (Exception e) {
	    Modeller.addToDebugMessages(
		    "Exception in executeOnce in IncrementalLoadResourcePage: " + e.toString() +
		    " in " + codeElement == null ? "null" : codeElement.getInnerHTML());
	    // not sure how much help this is since is client JavaScript
	    StackTraceElement[] stackTrace = e.getStackTrace();
	    for (StackTraceElement stackTraceElement : stackTrace) {
		Modeller.addToDebugMessages(stackTraceElement.toString());
	    }
	    e.printStackTrace();
	    return true; // still work on the next one
	}
    }

    private void runCommandWhenLoaded() {
	if (commandWhenLoaded != null) {
	    try {
		commandWhenLoaded.execute();
	    } catch (Exception e) {
		Modeller.addToDebugMessages("Error executing code to run after loading a page: " + e.toString());
		e.printStackTrace();
	    }
	    commandWhenLoaded = null;
	}
    }

    public void cancel() {
	elementCount = 0;
    }
	
}
