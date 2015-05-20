/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Ken Kahn
 *
 */
public class ScrollPanelInTabPanel extends ScrollPanel {
    
    public ScrollPanelInTabPanel() {
	super();
    }
    
    public ScrollPanelInTabPanel(Widget widget) {
	super(widget);
    }
    
    @Override
    public void setVisible(boolean visible) {
	super.setVisible(visible);
	if (getWidget() != null) {
	    getWidget().setVisible(visible);
	}
    }

}
