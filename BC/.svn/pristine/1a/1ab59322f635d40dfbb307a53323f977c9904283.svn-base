/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

import uk.ac.lkl.client.composer.URLEntryBox;

/**
 * 
 * A convenient way of asking the user for a URL
 * 
 * @author Ken Kahn
 *
 */
public class PopupURLEntryPanel extends DecoratedPopupPanel {
    
    private URLEntryBox urlEntryPanel;
    
    public PopupURLEntryPanel(String heading, String title) {
	super();
	urlEntryPanel = new URLEntryBox(title);
	Command cancelCommand = new Command() {

	    @Override
	    public void execute() {
		hide();			
	    }
	    
	};
	urlEntryPanel.addCancelCommand(cancelCommand);
	VerticalPanel dialog = new VerticalPanel();
	dialog.setSpacing(6);
	dialog.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	dialog.add(new HTML(heading));
	dialog.add(urlEntryPanel);
	setWidget(dialog);
    }
    
    public void addOKCommand(Command command) {
	urlEntryPanel.addOKCommand(command);
    }

    public String getURL() {
	return urlEntryPanel.getText();
    }

}
