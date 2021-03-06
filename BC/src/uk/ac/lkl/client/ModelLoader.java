package uk.ac.lkl.client;

import uk.ac.lkl.client.composer.MenuBarWithDebugID;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

public class ModelLoader extends ButtonWithDebugID {
    protected int type = -1;
    protected String data = null;
    // data can be one of the following
    static final public int MOPIX_MODEL = 0;
    static final public int BEHAVIOUR_COMPOSER_MODEL_ID = 1;
    static final public int SESSION_ID = 2;
    static final public int READ_ONLY_SESSION_ID = 3;

    public ModelLoader(String html, String data, int type) {
	super(html);
	this.data = data;
	this.type = type;
	addStyleName("modeller-model-loader");
	this.addClickHandler(new ClickHandler() {

	    public void onClick(ClickEvent event) {
		createPopupMenu(event);
	    }
	    
	});
    }
    
    protected void createPopupMenu(ClickEvent event) {
	final PopupPanelWithKeyboardShortcuts popupMenu = new PopupPanelWithKeyboardShortcuts(true);
	popupMenu.setAnimationEnabled(true);
	MenuBar menuBar = new MenuBarWithDebugID(true);
	menuBar.setAnimationEnabled(true);
	popupMenu.setWidget(menuBar);
	String replaceModelMenuLabel;
	String replaceModelMenuTitle;
	if (type == SESSION_ID || type == READ_ONLY_SESSION_ID) {
	    replaceModelMenuLabel = Modeller.constants.reconstructSessionFromScratch();
	    replaceModelMenuTitle = Modeller.constants.reconstructSessionFromScratchTitle();
	} else {
	    replaceModelMenuLabel = Modeller.constants.replaceCurrentModelWithThis();
	    replaceModelMenuTitle = Modeller.constants.replaceCurrentModelWithThisTitle();
	}
	Command replaceCommand = new Command() {
	    public void execute() {
		popupMenu.hide();
		Modeller.INSTANCE.switchToConstructionArea();
		addModel(true);
	    }
	};
	MenuItem firstMenuItem = popupMenu.createMenuItem('R', replaceModelMenuLabel, replaceModelMenuTitle, replaceCommand);
	menuBar.addItem(firstMenuItem);
	String addModelMenuLabel;
	String addModelMenuTitle;
	if (type == SESSION_ID || type == READ_ONLY_SESSION_ID) {
	    addModelMenuLabel = Modeller.constants.addReconstructedSessionToCurrent();
	    addModelMenuTitle = Modeller.constants.addReconstructedSessionToCurrentTitle();
	} else {
	    addModelMenuLabel = Modeller.constants.addThisToCurrentModel();
	    addModelMenuTitle = Modeller.constants.addThisToCurrentModelTitle();
	}
	Command addCommand = new Command() {
	    public void execute() {
		popupMenu.hide();
		Modeller.INSTANCE.switchToConstructionArea();
		addModel(false);
	    };
	};
	MenuItem menuItem = popupMenu.createMenuItem('A', addModelMenuLabel, addModelMenuTitle, addCommand);
	menuBar.addItem(menuItem);
	popupMenu.show();
	Utils.positionPopupMenu(event.getClientX(), event.getClientY(), popupMenu);
//	Utils.positionPopupMenu(getAbsoluteLeft()+getOffsetWidth()/2, 
//                                getAbsoluteTop()+getOffsetHeight()/2, 
//                                popupMenu, menuBar, firstMenuItem);
    }
    
    protected void addModel(boolean removeOld) {
	Modeller.INSTANCE.switchToConstructionArea();
	Modeller.addAlert(CommonUtils.emphasise(Modeller.constants.loadingPleaseWait()));
	Modeller.instance().waitCursor();
//	if (type != MOPIX_MODEL && removeOld) {
//	    Modeller.instance().removeAllMacroBehaviours();
//	    Modeller.instance().setPrototypeCounter(0);
//	    MicroBehaviourSharedState.clearUrlToSharedStateMap();
//	    Modeller.instance().addMacroBehaviour(Modeller.instance().createMacroBehaviour());
//	}
	switch (type) {
	case MOPIX_MODEL:
	case BEHAVIOUR_COMPOSER_MODEL_ID:
	    Modeller.INSTANCE.addModel(data, removeOld);
	    break;
	case SESSION_ID:
	case READ_ONLY_SESSION_ID:
	    Modeller.INSTANCE.loadAndReconstructHistory(
		    data, false, (type == READ_ONLY_SESSION_ID), Modeller.IGNORE_NO_EVENTS, false, null);
	    break;
	default:
	    Modeller.addToErrorLog("ModelLoader has unrecognised type: " + type);
	}
    }
}
