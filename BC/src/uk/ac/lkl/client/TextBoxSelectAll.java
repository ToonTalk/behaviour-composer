/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TextBox;

/**
 * TextBox whose contents are selected very soon after it is loaded
 * 
 * @author Ken Kahn
 *
 */
public class TextBoxSelectAll extends TextBox {
    
    public TextBoxSelectAll() {
	super();
    }
    
    @Override
    public void onLoad() {
	super.onLoad();
	// need to delay for this to work -- deferred command wasn't good enough
	Timer timer = new Timer() {

	    @Override
	    public void run() {
		setFocus(true);
		selectAll();		
	    }
	    
	};
	timer.schedule(500);
    }

}
