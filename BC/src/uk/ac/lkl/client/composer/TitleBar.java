/**
 * 
 */
package uk.ac.lkl.client.composer;

import uk.ac.lkl.client.CloseButton;
import uk.ac.lkl.client.HorizonalPanelWithDebugID;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;

/**
 * A closable title bar
 * 
 * @author Ken Kahn
 *
 */
public class TitleBar extends HorizonalPanelWithDebugID {
    
    private Image closeButton;
    private HTML title;

    public TitleBar(String titleString) {
	setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	title = new HTML(titleString);
	add(title);
	setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	closeButton = new CloseButton();
	add(closeButton);
	setStylePrimaryName("modeller-title-bar");
    }

    public Image getCloseButton() {
        return closeButton;
    }
    
    @Override
    public void onLoad() {
	super.onLoad();
	setWidth(getParent().getOffsetWidth() + "px");
	String size = title.getOffsetHeight() + "px";
	closeButton.setSize(size, size);
    }

}
