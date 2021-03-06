/**
 * 
 */
package uk.ac.lkl.client;


import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Creates a panel with a text box for the url, a go button, and a close button
 * 
 * 
 * @author Ken Kahn
 *
 */
public class UrlBox extends HorizonalPanelWithDebugID {
    // TODO: consider combining with URLEntryBox
    protected TextBox urlTextBox;
    protected Button goButton;
    protected Image closeButton = null;
    
    public UrlBox(String url, final BrowsePanel panel, boolean closable, boolean addGoButton) {
	super();
	setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	urlTextBox = new TextBoxSelectAll();
	urlTextBox.setText(url);
	panel.getElement().getStyle().setDisplay(Display.INLINE);
//	DOM.setStyleAttribute(urlTextBox.getElement(), "display", "inline");
	if (closable) {
	    closeButton = new CloseButton();
	    closeButton.addClickHandler(new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
		    panel.removeFromParent();
		}
	    }); 
	}
	if (addGoButton) {
	    goButton = new Button(Modeller.constants.go().toUpperCase());
	    goButton.addClickHandler(new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
		    loadNewURL(panel);
		}});
	}
	urlTextBox.addKeyUpHandler(new KeyUpHandler() {
	    @Override
	    public void onKeyUp(KeyUpEvent event) {
		if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) { // entered a new line
		    loadNewURL(panel);
		}		
	    };
	});
	panel.panelContents.add(urlTextBox);
	// go button is very wide if not wrapped
//	panel.add(Utils.wrapForGoodSize(goButton));
	if (goButton != null) {
	    panel.add(goButton);
	}
	if (closeButton != null) {
	    panel.add(closeButton);
	}
	setSpacing(2);
    }

    public TextBox getUrlTextBox() {
        return urlTextBox;
    }

    public Button getGoButton() {
        return goButton;
    }

    public Image getCloseButton() {
        return closeButton;
    }

    protected void loadNewURL(BrowsePanel panel) {
	final String url = Utils.urlCheckingTabAttribute(urlTextBox.getText());
	if (!url.equals("http://")) { // not return without entering a URL
	    panel.loadNewURL(url, null);
	}
    }
    
    @Override
    public void onLoad() {
	super.onLoad();
	adjustSizeOfUrlTextBox();
	urlTextBox.setFocus(true);
	urlTextBox.selectAll();
    }
    
    protected void adjustSizeOfUrlTextBox() {
	// TODO: simplify
	int widthOfButtons = 0;
	if (closeButton != null) {
	    widthOfButtons += closeButton.getOffsetWidth();
	}
	if (goButton != null) {	 
//	    goButton.setWidth((goButton.getText().length() + 2) + "em");
	    widthOfButtons += goButton.getOffsetWidth();
	}
	int urlWidth = getOffsetWidth()-(widthOfButtons+100);
	if (urlWidth <= 50) {
	    urlWidth = Math.max(200, Modeller.instance().getTabPanelWidth()-150);
	}
	TextBox urlTextBox = getUrlTextBox();
	urlTextBox.setWidth(urlWidth + "px");
    }

}
