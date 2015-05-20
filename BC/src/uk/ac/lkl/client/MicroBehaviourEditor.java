/**
 * 
 */
package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.List;

import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Implements the editor for locally defined micro-behaviours
 * 
 * @author Ken Kahn
 *
 */
public class MicroBehaviourEditor extends ScrollPanel {
    
    private RichTextEntry nameEntry;
    private TextArea descriptionTextArea;
    private TextArea codeArea;
    private RichTextEntry additionalInfoEntry;
    private Command commandOnExit = null;
    private boolean openAfterSaving = true;

    public MicroBehaviourEditor(final BrowsePanel browsePanel, final boolean editMicroBehaviour, boolean firstTime) {
	final Grid editor = new Grid(editMicroBehaviour ? 6 : 4, 1);
	editor.setCellSpacing(10);
	editor.setBorderWidth(2);
	setWidget(editor);
	final Button saveButtonTop = new ModellerButton(Modeller.constants.save().toUpperCase());
	saveButtonTop.setTitle(Modeller.constants.saveTitle());
	Button saveAsButtonTop = new ModellerButton(Modeller.constants.saveAs().toUpperCase());
	saveAsButtonTop.setTitle(Modeller.constants.saveAsTitle());
	final Button readOnlyButtonTop = new ModellerButton(Modeller.constants.readOnly().toUpperCase());
	readOnlyButtonTop.setTitle(Modeller.constants.readOnlyTitle());
	Button cancelButtonTop = new ModellerButton(Modeller.constants.cancel().toUpperCase());
	saveButtonTop.setWidth("50px");
	saveAsButtonTop.setWidth("80px");
	readOnlyButtonTop.setWidth("80px");
	cancelButtonTop.setWidth("80px");
	final Button saveButtonBottom = new ModellerButton(Modeller.constants.save().toUpperCase());
	saveButtonBottom.setTitle(Modeller.constants.saveTitle());
	Button saveAsButtonBottom = new ModellerButton(Modeller.constants.saveAs().toUpperCase());
	saveAsButtonBottom.setTitle(Modeller.constants.saveAsTitle());
	final Button readOnlyButtonBottom = new ModellerButton(Modeller.constants.readOnly().toUpperCase());
	readOnlyButtonBottom.setTitle(Modeller.constants.readOnlyTitle());
	Button cancelButtonBottom = new ModellerButton(Modeller.constants.cancel().toUpperCase());
	saveButtonBottom.setWidth("50px");
	saveAsButtonBottom.setWidth("80px");
	readOnlyButtonBottom.setWidth("80px");
	cancelButtonBottom.setWidth("80px");
	HorizontalPanel buttonPanelTop = new HorizontalPanel();
	buttonPanelTop.add(saveButtonTop);
	if (!firstTime) {
	    buttonPanelTop.add(saveAsButtonTop);
	    buttonPanelTop.add(readOnlyButtonTop);
	}
	buttonPanelTop.add(cancelButtonTop);
	buttonPanelTop.setSpacing(6);
	int row = 0;
	editor.setWidget(row++, 0, buttonPanelTop);
	int endOfCodeIndex;
	final String nameHTML = browsePanel.getNameHTML();
	nameEntry = new RichTextEntry(null, nameHTML==null?"":nameHTML, 70);
	final int margin = 50;
	nameEntry.setWidth((Modeller.instance().getTabPanelWidth()-margin) + "px");
	editor.setWidget(row++, 0, new MicroBehaviourEditorArea(Modeller.constants.microBehaviourNameTitle(), new ScrollPanel(nameEntry)));
	final String description = browsePanel.getDescription();
	if (editMicroBehaviour) {
	    descriptionTextArea = new TextArea();
	    if (description != null) {
		descriptionTextArea.setText(description);
	    }
	    editor.setWidget(row++, 0, new MicroBehaviourEditorArea(Modeller.constants.microBehaviourDescriptionTitle(), descriptionTextArea));
	    codeArea = new TextArea();
	    String code = browsePanel.getCode();
	    if (code != null) {
		codeArea.setText(code);
	    }
	    MicroBehaviourEditorArea codeAreaPanel = new MicroBehaviourEditorArea(Modeller.constants.microBehaviourCodeTitle(), codeArea);
	    codeArea.setHeight("250px");
	    ModellerButton insertTextAreaButton = new ModellerButton(Modeller.constants.insertTextArea());
	    ClickHandler insertTextAreaHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
		    int cursorPosition = codeArea.getCursorPos();
		    String currentText = codeArea.getText();
		    String newText = currentText.substring(0, cursorPosition) + "substitute-text-area-for REPLACE-WITH-NAME-OF-AREA REPLACE-WITH-DEFAULT-VALUE\n" + currentText.substring(cursorPosition);
		    codeArea.setText(newText);
		    codeArea.setSelectionRange(cursorPosition+"substitute-text-area-for".length()+1, "replace-with-default-value".length()-1);
		}

	    };
	    insertTextAreaButton.addClickHandler(insertTextAreaHandler);
	    ModellerButton insertBehaviourListButton = new ModellerButton(Modeller.constants.insertBehaviourList());
	    ClickHandler insertBehaviourListHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
		    int cursorPosition = codeArea.getCursorPos();
		    String currentText = codeArea.getText();
		    String newText = currentText.substring(0, cursorPosition) + "list-of-micro-behaviours \"REPLACE-WITH-A-DESCRIPTION-OF-THE-LIST\" []\n" + currentText.substring(cursorPosition);
		    codeArea.setText(newText);
		    codeArea.setSelectionRange(cursorPosition+"list-of-micro-behaviours".length()+2, "replace-with-a-description-of-this-list".length()-1);
		}

	    };
	    insertBehaviourListButton.addClickHandler(insertBehaviourListHandler);
	    HorizontalPanel insertButtonsPanel = new HorizontalPanel();
	    insertButtonsPanel.setSpacing(6);
	    insertButtonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	    insertButtonsPanel.add(insertTextAreaButton);
	    insertButtonsPanel.add(insertBehaviourListButton);
	    codeAreaPanel.add(insertButtonsPanel);
	    editor.setWidget(row++, 0, codeAreaPanel);
	} else {
	    // to avoid annoying warnings:
	    descriptionTextArea = null;
	    codeArea = null;
	}
	String processedHTML = browsePanel.getProcessedHTML();
	endOfCodeIndex = processedHTML == null ? -1 : processedHTML.indexOf(CommonUtils.AFTER_CODE_ELEMENT);
	if (endOfCodeIndex >= 0) {
	    // typically embedded in <PRE id="after_code_element"></PRE>
	    endOfCodeIndex = processedHTML.indexOf("\n", endOfCodeIndex)+1;
	}
	String additionalInfo = endOfCodeIndex < 0 || processedHTML == null ? "" : processedHTML.substring(endOfCodeIndex);
	additionalInfoEntry = new RichTextEntry(null, additionalInfo, 200);
	editor.setWidget(row++, 0, new MicroBehaviourEditorArea(Modeller.constants.microBehaviourAdditionalInfoTitle(), additionalInfoEntry));
	HorizontalPanel buttonPanelBottom = new HorizontalPanel();
	buttonPanelBottom.add(saveButtonBottom);
	if (!firstTime) {
	    buttonPanelBottom.add(saveAsButtonBottom);
	    buttonPanelBottom.add(readOnlyButtonBottom);
	}
	buttonPanelBottom.add(cancelButtonBottom);
	buttonPanelBottom.setSpacing(6);
	editor.setWidget(row++, 0, buttonPanelBottom);
	ClickHandler saveClickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		save(browsePanel, editMicroBehaviour, nameHTML, description, false);
	    } 
	    
	};
	ClickHandler saveAsClickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		save(browsePanel, editMicroBehaviour, nameHTML, description, true);
	    } 
	    
	};
	ClickHandler readOnlyClickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		MicroBehaviourView microBehaviour = browsePanel.getMicroBehaviour();
		String url = microBehaviour.getUrl();
		AsyncCallback<String> callback = new AsyncCallback<String>() {

		    @Override
		    public void onFailure(Throwable caught) {
			Modeller.setAlertsLine(Modeller.constants.networkErrorTryAgainLater());			
		    }

		    @Override
		    public void onSuccess(String result) {
			if (result == null) {
			    disableSaveButtons(saveButtonTop, saveButtonBottom, readOnlyButtonTop, readOnlyButtonBottom);
			    Modeller.setAlertsLine(Modeller.constants.thisPageIsNowReadOnly());
			} else {
			    Modeller.setAlertsLine(result);
			}
		    }
		    
		};
		Modeller.getResourcePageService().makeReadOnly(url, Modeller.sessionGuid, GWT.getHostPageBaseURL(), Modeller.cachingEnabled, Modeller.internetAccess, callback);
	    }
	    
	};
	saveButtonTop.addClickHandler(saveClickHandler);
	saveButtonBottom.addClickHandler(saveClickHandler);
	saveAsButtonTop.addClickHandler(saveAsClickHandler);
	saveAsButtonBottom.addClickHandler(saveAsClickHandler);
	readOnlyButtonTop.addClickHandler(readOnlyClickHandler);
	readOnlyButtonBottom.addClickHandler(readOnlyClickHandler);
	ClickHandler cancelClickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		browsePanel.editCanceled(MicroBehaviourEditor.this);
		Modeller.mainTabPanel.remove(browsePanel);
		if (commandOnExit != null) {
		    commandOnExit.execute();
		}
	    }
	    
	};
	cancelButtonTop.addClickHandler(cancelClickHandler);
	cancelButtonBottom.addClickHandler(cancelClickHandler);
	Handler attachHandler = new Handler() {

	    @Override
	    public void onAttachOrDetach(AttachEvent event) {
		if (event.isAttached()) {
		    // make rich text entry areas as wide as can be (30 pixels for borders, scroll bars, etc)
		    // defer to do this after the width of the editor is known
		    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
			    if (editor.isAttached()) {
				int width = editor.getOffsetWidth();
				if (width > 30) {
				    String entryWidth = (width-30) + "px";
				    nameEntry.getRichTextArea().setWidth(entryWidth);
				    additionalInfoEntry.getRichTextArea().setWidth(entryWidth);
				}
			    }
			}
		    });	
		}
	    }

	};
	additionalInfoEntry.getRichTextArea().addAttachHandler(attachHandler);
	if (browsePanel.isReadOnly()) {
	    disableSaveButtons(saveButtonTop, saveButtonBottom, readOnlyButtonTop, readOnlyButtonBottom);
	    Modeller.setAlertsLine(Modeller.constants.editTheTextThenClickSaveAs());
	}
    }
    
    private void save(final BrowsePanel browsePanel,
	              final boolean editMicroBehaviour, 
	              final String nameHTML,
	              final String description,
	              boolean saveAs) {
	Modeller.setAlertsLine(Modeller.constants.newMicroBehaviourBeingSaved());
	Modeller.instance().waitCursor();
	Modeller.mainTabPanel.remove(MicroBehaviourEditor.this);
	String newHTML = editMicroBehaviour ? BehaviourComposer.resources().microBehaviourTemplate().getText() : 
	                                      BehaviourComposer.resources().resourceTemplate().getText();
	String newNameHTML = nameEntry.getHTML();
	if (newNameHTML == null || newNameHTML.isEmpty() || CommonUtils.removeHTMLMarkup(newNameHTML).isEmpty()) {
	    newNameHTML = editMicroBehaviour ? Modeller.constants.unnamedMicroBehaviour() : Modeller.constants.unnamedResource();
	}
	if (editMicroBehaviour) {
	    String code = codeArea.getText();
	    if (code.isEmpty()) {
		code = Modeller.constants.enterNetLogoCodeHere();
	    }
	    newHTML = newHTML.replace("***description***", descriptionTextArea.getText())
		             .replace("***name***", newNameHTML)
		             .replace("***code***", code)
		             .replace("***additionalInfo***", additionalInfoEntry.getHTML());
	} else {
	    newHTML = newHTML.replace("***name***", newNameHTML)
	                     .replace("***additionalInfo***", additionalInfoEntry.getHTML());
	    browsePanel.setTabWidget(new ClosableTab(newNameHTML, browsePanel, Modeller.mainTabPanel));
	}
	String originalHTML = browsePanel.getOriginalHTML();
	MicroBehaviourView microBehaviour = browsePanel.getMicroBehaviour();
	if (!newHTML.equals(originalHTML)) {
	    if (microBehaviour != null &&
		(!descriptionTextArea.getText().equals(description) ||
	         !newNameHTML.equals(nameHTML))) {
		microBehaviour.updateTextArea(null, -1); // force name+description to be re-computed
	    }
	    BrowsePanelEdited command = new BrowsePanelEdited() {

		@Override
		public void edited(final BrowsePanel editedPanel) {
		    Modeller.instance().restoreCursor();
		    final MicroBehaviourView microBehaviour = editedPanel.getMicroBehaviour();
		    if (microBehaviour != null) {
			Command afterOpeningCommand = new Command() {

			    @Override
			    public void execute() {
//				Modeller.mainTabPanel.remove(editedPanel);
//				Modeller.mainTabPanel.switchTo(editedPanel);
				ClosableTab panelTabWidget = editedPanel.getTabWidget();
				if (panelTabWidget != null) {
				    panelTabWidget.setVisible(true);
				}
				if (microBehaviour.isWarnThatTextAreasHaveChanged()) {
				    editedPanel.getMicroBehaviour().setWarnThatTextAreasHaveChanged(true);
				    editedPanel.addWarningIfNeeded();
				}
				Modeller.instance().restoreCursor();
				Modeller.mainTabPanel.refreshMicroBehavioursWithURL(CommonUtils.removeBookmark(microBehaviour.getUrl()));
				if (commandOnExit != null) {
				    commandOnExit.execute();
				}
			    }

			};
			if (openAfterSaving) {
			    microBehaviour.openInBrowsePanel(true, afterOpeningCommand, true);
			} else if (commandOnExit != null) {
			    commandOnExit.execute();
			}
		    } else {
			BrowsePanelCommand restoreCursorCommand = new BrowsePanelCommand() {

			    @Override
			    public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
				Modeller.instance().restoreCursor();
				if (commandOnExit != null) {
				    commandOnExit.execute();
				}
			    }
			    
			};
			browsePanel.browseTo(browsePanel.getCurrentURL(), false, true, restoreCursorCommand);
		    } 
		}

	    };
	    browsePanel.setDoAfterSaving(command);
	    if (microBehaviour != null && !saveAs) {
		List<String> oldTextAreaNames = collectTextAreaNames(originalHTML);
		List<String> newTextAreaNames = collectTextAreaNames(newHTML);
		String pageURL = CommonUtils.removeBookmark(microBehaviour.getUrl());
		if (!oldTextAreaNames.equals(newTextAreaNames)) {
		    ArrayList<MicroBehaviourView> allMicroBehaviours = Modeller.instance().getAllMicroBehaviours();
		    for (MicroBehaviourView microBehaviourView : allMicroBehaviours) {
			warnIfCustomisationOfPage(pageURL, microBehaviourView);
		    }
		    warnIfCustomisationOfPage(pageURL, microBehaviour);
		} else {
		    List<String> oldTextAreaValues = collectTextAreaValues(originalHTML);
		    List<String> newTextAreaValues = collectTextAreaValues(newHTML);
		    if (!oldTextAreaValues.equals(newTextAreaValues)) {
			String message = Modeller.constants.editsOfTextAreaValuesOnlyAffectsFutureInstances();
			Utils.popupMessage(message, true);
		    }
		}
	    }
	    browsePanel.updateContents(Utils.replaceHTMLBody(newHTML, originalHTML), !saveAs);
	}
    }

    private void warnIfCustomisationOfPage(String pageURL, MicroBehaviourView microBehaviourView) {
	String url = microBehaviourView.getUrl();
	// microBehaviourView was browsePanel.getmicroBehaviourView()
	if (CommonUtils.hasChangesGuid(url) || microBehaviourView.isCopyMicroBehaviourWhenExportingURL()) {
	    if (pageURL.equals(CommonUtils.removeBookmark(url))) {
		microBehaviourView.setWarnThatTextAreasHaveChanged(true);
	    }
	}
    }
    
    private List<String> collectTextAreaNames(String string) {
	ArrayList<String> textAreaNames = new ArrayList<String>();
	String[] declarations = string.split(CommonUtils.SUBSTITUTE_TEXT_AREA_FOR);
	// don't count the stuff before the declaration
	for (int i = 1; i < declarations.length; i++) {
	    String declaration = declarations[i].trim();
	    int nextSpace = declaration.indexOf(" ", 1);
	    if (nextSpace > 0) {
		textAreaNames.add(declaration.substring(0, nextSpace+1));
	    }
	}
	return textAreaNames;
    }
    
    private List<String> collectTextAreaValues(String string) {
	ArrayList<String> textAreaValues = new ArrayList<String>();
	String[] declarations = string.split(CommonUtils.SUBSTITUTE_TEXT_AREA_FOR);
	// don't count the stuff before the declaration
	for (int i = 1; i < declarations.length; i++) {
	    String declaration = declarations[i].trim();
	    int nextSpace = declaration.indexOf(" ", 1);
	    if (nextSpace > 0) {
		int secondSpace = declaration.indexOf(" ", nextSpace);
		if (secondSpace >= 0) {
		    int lineEnd = declaration.indexOf("\n", secondSpace+1);
		    if (lineEnd > 0) {
			textAreaValues.add(declaration.substring(secondSpace+1, lineEnd));
		    }
		}
	    }
	}
	return textAreaValues;
    }

    public RichTextEntry getNameEntry() {
        return nameEntry;
    }

    public TextArea getDescriptionTextArea() {
        return descriptionTextArea;
    }

    public TextArea getCodeArea() {
        return codeArea;
    }

    public RichTextEntry getAdditionalInfoEntry() {
        return additionalInfoEntry;
    }

    public Command getCommandOnExit() {
        return commandOnExit;
    }

    public void setCommandOnExit(Command commandOnExit) {
        this.commandOnExit = commandOnExit;
    }

    public boolean isOpenAfterSaving() {
        return openAfterSaving;
    }

    public void setOpenAfterSaving(boolean openAfterSaving) {
        this.openAfterSaving = openAfterSaving;
    }

    public void disableSaveButtons(Button saveButtonTop, Button saveButtonBottom, Button readOnlyButtonTop, Button readOnlyButtonBottom) {
	saveButtonTop.setEnabled(false);
	saveButtonBottom.setEnabled(false);
	saveButtonTop.setTitle(Modeller.constants.saveDisabledTitle());
	saveButtonBottom.setTitle(Modeller.constants.saveDisabledTitle());
	readOnlyButtonTop.setEnabled(false);
	readOnlyButtonBottom.setEnabled(false);
    }

}
