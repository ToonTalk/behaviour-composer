package uk.ac.lkl.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class OpenAppletDialogBox extends DialogBox {
    
    // TODO: remove since is obsolete (wait until sure we don't need it)
    protected String appletURL;
    
    public OpenAppletDialogBox(final String appletURL, final int appletWidth, final int appletHeight) {
	super();
	this.appletURL = appletURL;
	setAnimationEnabled(true);
	String text = BehaviourComposer.epidemicGameMakerMode() ?
		      Modeller.constants.yourGameIsReady() : 
	              Modeller.constants.yourModelIsReadyToRun();
	setHTML(text);
	String buttonLabel = BehaviourComposer.epidemicGameMakerMode() ?
		             Modeller.constants.goToYourGame() :
		             Modeller.constants.goToYourModelAppletPage();
	Button okButton = new Button(buttonFont(buttonLabel));
	ClickHandler okHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		hide();
		if (BehaviourComposer.epidemicGameMakerMode()) {
		    Window.open(appletURL, "_blank", "");
		} else {
		    BehaviourComposer.runPanel.clear();
		    Frame frame = new Frame(appletURL);
		    frame.setPixelSize(appletWidth, appletHeight);
		    BehaviourComposer.runPanel.add(frame);
		}
	    }

	};
	okButton.addClickHandler(okHandler);
	Button cancelButton = new Button(buttonFont(Modeller.constants.cancel()));
	ClickHandler cancelHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		hide();
	    }

	};
	cancelButton.addClickHandler(cancelHandler);
	setStylePrimaryName("modeller-run-applet-dialog");
	HorizontalPanel buttonsHolder = new HorizontalPanel();
	buttonsHolder.setSpacing(4);
	buttonsHolder.add(okButton);
	buttonsHolder.add(cancelButton);
//	buttonsHolder.setStylePrimaryName("modeller-run-applet-dialog-ok-button");
	setWidget(buttonsHolder);
	addStyleName("modeller-run-applet-dialog-ok-button");
	center();
    }
    
    public static String buttonFont(String text) {
	return "<b><font size='4' color='#000080'>" + text + "</font></b>";
    }
    
    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
	int key = event.getNativeEvent().getKeyCode();
	if (key == KeyCodes.KEY_ENTER) {	    
	    hide();
	    Window.open(appletURL, "_blank", "");
	}
	super.onPreviewNativeEvent(event);
    }

}
