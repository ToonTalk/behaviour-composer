package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.Date;

import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class ModelsPanel extends VerticalPanelWithDebugID {

    private static final int SESSION_GUID_OFFSET = 0;
    private static final int DESCRIPTION_INDEX_OFFSET = 1;
    private static final int INFO_TAB_INDEX_OFFSET = 2;
    private static final int VISIBLE_INDEX_OFFSET = 3;
    private static final int CONTEXT_INDEX_OFFSET = 4;
    private static final int CREATION_TIME_STAMP_INDEX_OFFSET = 5;
    private static final int LAST_UPDATE_INDEX_OFFSET = 6;
    public static final int STRING_COUNT_PER_SESSION = 7;
    private Grid grid;
    private ArrayList<String> sessions;
    private CheckBox showHiddenModelsCheckBox;
    private CheckBox otherContextModelsCheckBox;
    private String currentSessionGuid;
    private String moduleBaseURL;
    private String userGuid;

    /**
     * @param sessions - a list where every STRING_COUNT_PER_SESSION elements is a session
     * session guid, description HTML, visible ("true" or "false"), Basic LTI context id, 
     * creation time stamp string, and last update time stamp string
     * @param userGuid
     * @param currentSessionGuid
     * @param moduleBaseURL
     */
    public ModelsPanel(ArrayList<String> sessions, String userGuid, String currentSessionGuid, String moduleBaseURL) {
	super();
	setHorizontalAlignment(ALIGN_LEFT);
	setSpacing(12);
	boolean sessionsWasNull = sessions == null;
	if (sessionsWasNull) {
	    sessions = new ArrayList<String>();
	}
	if (sessions.isEmpty()) {
	    sessions.add(Modeller.sessionGuid); // SESSION_GUID_OFFSET
	    sessions.add(""); // DESCRIPTION_INDEX_OFFSET
	    sessions.add(""); // INFO_TAB_INDEX_OFFSET
	    sessions.add("true"); // VISIBLE_INDEX_OFFSET
	    sessions.add(""); // CONTEXT_INDEX_OFFSET
	    sessions.add("0"); // CREATION_TIME_STAMP_INDEX_OFFSET = 5;
	    sessions.add("0"); // LAST_UPDATE_INDEX_OFFSET = 6;
//	    if (sessionsWasNull && Modeller.instance().isWarnIfUnknownSessionId() && CommonUtils.validateGuid(userGuid, "User key") == null) {
//		// no need to warn if warnIfUnknownSessionId=0
//		// and if userGuid is invalid an error about that will appear
//		Modeller.addToErrorLog(Modeller.constants.noPreviousSessionsReturnedByServer());
//	    }
	}
	this.sessions = sessions;
	this.userGuid = userGuid;
	this.currentSessionGuid = currentSessionGuid;
	this.moduleBaseURL = moduleBaseURL;
	Button newModelButton = createNewModelButton(userGuid, moduleBaseURL);
	showHiddenModelsCheckBox = new CheckBox(Modeller.constants.showHiddenModels());
	showHiddenModelsCheckBox.setTitle(Modeller.constants.showHiddenModelsTitle());
	showHiddenModelsCheckBox.setValue(false); // by default hidden models are hidden
	otherContextModelsCheckBox = new CheckBox(Modeller.constants.showModelsAuthoredInOtherContexts());
	otherContextModelsCheckBox.setTitle(Modeller.constants.showModelsAuthoredInOtherContextsTitle());
	otherContextModelsCheckBox.setValue(true);
	ClickHandler populateGridClickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		populateGrid();	
	    }
	    
	};
	showHiddenModelsCheckBox.addClickHandler(populateGridClickHandler);
	otherContextModelsCheckBox.addClickHandler(populateGridClickHandler);
	populateGrid();
	Button importModelButton = new Button(Modeller.constants.useURLToLoadNewModel());
	ClickHandler importModelClickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		final PopupURLEntryPanel popup = 
			new PopupURLEntryPanel(Modeller.constants.createNewModelContainingContentsOfURL(), Modeller.constants.enterAURLOfAFrozenModel());
		Command importModelCommand = new Command() {

		    @Override
		    public void execute() {
			String frozenURL = popup.getURL();
			String modelGuid = CommonUtils.getURLParameter("frozen", frozenURL);
			if (modelGuid != null && !modelGuid.isEmpty()) {
			    String url = CommonUtils.joinPaths(ModelsPanel.this.moduleBaseURL, "?frozen=" + modelGuid + "&user=" + ModelsPanel.this.userGuid);
			    String fullURL = Utils.addContextToLaunchURL(url);
			    Window.open(fullURL, "_blank", "");		    
			} else {
			    Modeller.setAlertsLineAndHighlight(Modeller.constants.OnlyURLsWithFrozenParameterCanBeLoaded());
			}
		    }
		    
		};
		popup.addOKCommand(importModelCommand);
		popup.center();
		popup.show();
	    }
	    
	};
	importModelButton.addClickHandler(importModelClickHandler);	
	HorizontalPanel newModelPanel = new HorizontalPanel();
	newModelPanel.setVerticalAlignment(ALIGN_MIDDLE);
	newModelPanel.add(newModelButton);
	newModelPanel.add(new HTML("&nbsp;" + Modeller.constants.or() + "&nbsp;"));
	newModelPanel.add(importModelButton);
	setHorizontalAlignment(ALIGN_CENTER);
	add(newModelPanel);
	// separator
	add(new HTML("&nbsp;")); // blank line
	setHorizontalAlignment(ALIGN_DEFAULT);
	add(grid);
	setHorizontalAlignment(ALIGN_CENTER);
	add(new HTML(Modeller.constants.hereAreAllYourModels()));
	setHorizontalAlignment(ALIGN_DEFAULT);
	add(showHiddenModelsCheckBox);
	add(otherContextModelsCheckBox);
	grid.addStyleName("modeller_models_grid");
    }

    /**
     * @param sessions
     * @param userGuid
     * @param currentSessionGuid
     * @param moduleBaseURL
     */
    public void populateGrid() {
	int count = sessions==null ? 0 : sessions.size();
	if (grid == null) {
	    grid = new Grid(1+count/STRING_COUNT_PER_SESSION, 3);
	} else {
	    grid.clear();
	    grid.resize(1+count/STRING_COUNT_PER_SESSION, 3);
	}
	int row = 0;
	grid.setWidget(row, 0, new HTML(Modeller.constants.clickToEditDescription()));
	grid.setWidget(row, 1, new HTML(Modeller.constants.clickToLoadModel()));
	grid.setWidget(row, 2, new HTML(Modeller.constants.clickToHideModel()));
	row++;
	String currentContextId = Modeller.instance().getContextId();
	for (int i = 0; i < count; i += STRING_COUNT_PER_SESSION) {
	    final String sessionGuid = sessions.get(i+SESSION_GUID_OFFSET);
	    final int descriptionIndex = i+DESCRIPTION_INDEX_OFFSET;
	    String descriptionHTML = sessions.get(descriptionIndex);
	    boolean visible = Boolean.parseBoolean(sessions.get(i+VISIBLE_INDEX_OFFSET));
	    String contextId = sessions.get(i+CONTEXT_INDEX_OFFSET);
	    if ((visible || showHiddenModelsCheckBox.getValue()) && 
		(otherContextModelsCheckBox.getValue() || 
		 contextId == currentContextId || // both can be null if no Basic LTI context
		 (contextId != null && contextId.equals(currentContextId)))) {
		String creationTimeStamp = sessions.get(i+CREATION_TIME_STAMP_INDEX_OFFSET);
		String lastUpdateTimeStamp = sessions.get(i+LAST_UPDATE_INDEX_OFFSET);
		final String url = CommonUtils.joinPaths(moduleBaseURL, "?share=" + sessionGuid + "&user=" + userGuid);
		// use PRE to ensure it is a fixed width font so the guids line up nicely
		Button button = new Button("<pre>" + sessionGuid + "</pre>");
		button.addClickHandler(new ClickHandler() {

		    @Override
		    public void onClick(ClickEvent event) {
			String fullURL = Utils.addContextToLaunchURL(url);
			Window.open(fullURL, "_blank", "");
		    }

		});
		if (sessionGuid.equals(currentSessionGuid)) {
		    button.setEnabled(false);
		    button.setHTML("<pre>" + Modeller.constants.currentModel() + "</pre>");
//		    button.setTitle(Modeller.constants.thisIsYourCurrentModel());
		} else {
		    try {
			String dateString = stringTimeStampToDateString(creationTimeStamp);
			String title = Modeller.constants.clickToLoadThisModel() + " " +
			               Modeller.constants.firstCreated().replace("***date***", dateString);
			if (lastUpdateTimeStamp != null) {
			    String lastUpdateDate = stringTimeStampToDateString(lastUpdateTimeStamp);
			    title += " " + Modeller.constants.lastUpdated().replace("***date***", lastUpdateDate);
			}
			button.setTitle(title);
		    } catch (NumberFormatException e) {
			System.err.println("Time stamp not a long integer: " + creationTimeStamp);
		    }
		}
		final String initialDescription;
		if (descriptionHTML == null || descriptionHTML.isEmpty()) {
		    descriptionHTML = Modeller.constants.clickToProvideADescription();
		    initialDescription = "";
		} else {
		    initialDescription = descriptionHTML;
		}
		final HTML description = new HTML(descriptionHTML);
		final CheckBox checkBox = new CheckBox(Modeller.constants.hidden());
		description.setTitle(Modeller.constants.clickToEditThis());
		final int currentRow = row;
		description.addClickHandler(new ClickHandler() {

		    @Override
		    public void onClick(ClickEvent event) {
			final RichTextEntry richText = new RichTextEntry(description, initialDescription, 90);
			grid.setWidget(currentRow, 0, richText);
			richText.addSaveButtonClickHandler(new ClickHandler() {
			    @Override
			    public void onClick(ClickEvent event) {
				String newHTML = richText.getRichTextArea().getHTML();
//				description.setHTML(newHTML);
//				grid.setWidget(currentRow, 0, description);
				sessions.set(descriptionIndex, newHTML);
				updateSessionInformation(sessionGuid, !checkBox.getValue(), newHTML);
			    }
			});
			richText.addCancelButtonClickHandler(new ClickHandler() {
			    @Override
			    public void onClick(ClickEvent event) {
				grid.setWidget(currentRow, 0, description);
			    }
			});

		    }

		});
		final int sessionIndex = i+VISIBLE_INDEX_OFFSET;
		ClickHandler clickHandler = new ClickHandler() {

		    @Override
		    public void onClick(ClickEvent event) {
			updateSessionInformation(sessionGuid, !checkBox.getValue(), null);
			Boolean checked = updateHiddenCheckBoxTitle(checkBox);
			sessions.set(sessionIndex, Boolean.toString(!checked));
		    }
		    
		};
		checkBox.addClickHandler(clickHandler);
		checkBox.setValue(!Boolean.parseBoolean(sessions.get(sessionIndex)));
		updateHiddenCheckBoxTitle(checkBox);
		grid.setWidget(row, 0, description);
		grid.setWidget(row, 1, button);
		grid.setWidget(row, 2, checkBox);
		row++;
	    }
	}
	grid.setVisible(row > 0);
	if (row > 0) {
	    grid.resizeRows(row);
	}
    }
    
    public static String stringTimeStampToDateString(String timeStamp) {
	long time = Long.parseLong(timeStamp);
	Date date = new Date(time);
	return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(date);
    }
    
    private Button createNewModelButton(final String userGuid, final String moduleBaseURL) {
	Button newModelButton = new ModellerButton(Modeller.constants.newModelButton());
	newModelButton.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		String newSessionURL = CommonUtils.joinPaths(moduleBaseURL, "?share=new" + "&user=" + userGuid);
		newSessionURL = Utils.addContextToLaunchURL(newSessionURL);
		Window.open(newSessionURL, "_blank", "");
	    }
	});
	newModelButton.setTitle(Modeller.constants.clickToStartANewSessionInANewBrowserTab());
	return newModelButton;
    }

    /**
     * @param userGuid
     * @param sessionGuid
     * @param checkBox
     * @param newHTML
     */
    protected void updateSessionInformation(final String sessionGuid, final boolean visible, final String newHTML) {
	final AsyncCallbackNetworkFailureCapable<String> callback = new AsyncCallbackNetworkFailureCapable<String>() {
	    
	    @Override
	    public void onSuccess(String result) {
		super.onSuccess(result);
		if (result != null) {
		    Modeller.addToErrorLog(result);
		}
		populateGrid();
	    }
	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		updateSessionInformation(sessionGuid, visible, newHTML, callback);
	    }
	    
	});
    }
    
    private void updateSessionInformation(final String sessionGuid, final boolean visible, final String newHTML, final AsyncCallbackNetworkFailureCapable<String> callback) {
	Modeller.getHistoryService().updateSessionInformation(sessionGuid, userGuid, newHTML, null, visible, callback);
    }

    /**
     * @param checkBox
     * @return
     */
    protected Boolean updateHiddenCheckBoxTitle(final CheckBox checkBox) {
	Boolean checked = checkBox.getValue();
	if (checked) {
	    checkBox.setTitle(Modeller.constants.hiddenCheckBoxShowTitle());
	} else {
	    checkBox.setTitle(Modeller.constants.hiddenCheckBoxHideTitle());
	}
	return checked;
    }

    public String getInfoTab() {
	return sessions.get(INFO_TAB_INDEX_OFFSET);
    }
    
    public void setInfoTab(String infoTab) {
	sessions.set(INFO_TAB_INDEX_OFFSET, infoTab);
    }
    
    public String getDescription() {
	return sessions.get(DESCRIPTION_INDEX_OFFSET);
    }
    
    public void setDescription(String description) {
	if (description == null) {
	    return;
	}
	sessions.set(DESCRIPTION_INDEX_OFFSET, description);
	populateGrid();
	// not needed since if a model is loaded it will do this
	// and if reloaded without history the new model will also do this
//	boolean visible = Boolean.parseBoolean(sessions.get(VISIBLE_INDEX_OFFSET));
//	updateSessionInformation(getOriginalSessionGuid(), visible, description);
    }
    
    public String getOriginalSessionGuid() {
   	return sessions.get(SESSION_GUID_OFFSET);
    }

}
