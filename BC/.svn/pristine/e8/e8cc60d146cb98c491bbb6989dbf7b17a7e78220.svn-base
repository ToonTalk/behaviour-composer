/**
 * 
 */
package uk.ac.lkl.client.composer;

import uk.ac.lkl.client.HorizonalPanelWithDebugID;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.TextBoxSelectAll;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;

/**
 * A widget for user entry of a URL
 * 
 * @author Ken Kahn
 *
 */
public class URLEntryBox extends HorizonalPanelWithDebugID {
    
    private Button okButton;
    private TextBoxSelectAll urlTextBox;
    private String initialText;
    private Button cancelButton;

    public URLEntryBox(String title) {
	urlTextBox = new TextBoxSelectAll();
	initialText = "http://";
	urlTextBox.setText(initialText);
	urlTextBox.selectAll();
	urlTextBox.setTitle(title);
	add(urlTextBox);
	urlTextBox.setWidth(Math.max(200, (Window.getClientWidth() - 200)) + "px");
	okButton = new Button("OK");
	add(okButton);
	cancelButton = new Button(Modeller.constants.cancel());
	cancelButton.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		removeFromParent();
	    }	    
	});
	add(cancelButton);
	setSpacing(4);
    }
    
    public void addOKCommand(final Command command) {
	okButton.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		String text = urlTextBox.getText();
		if (!text.equals(initialText)) {
		    command.execute();
		}
	    }
	});
	urlTextBox.addKeyUpHandler(new KeyUpHandler() {
	    @Override
	    public void onKeyUp(KeyUpEvent event) {
		if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) { 
		    String text = urlTextBox.getText();
		    if (!text.equals(initialText)) {
			command.execute();
		    }
		}
	    }
	});
    }
    
    public void addCancelCommand(final Command command) {
	cancelButton.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		command.execute();
	    }
	});
    }
    
    public String getText() {
	return urlTextBox.getText();
    }

}
