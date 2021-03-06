/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.Image;

/**
 * A button for closing panels
 * 
 * @author Ken Kahn
 *
 */
public class CloseButton extends Image {
    
    public CloseButton() {
	// see http://fvisticot.blogspot.com/2010/01/scalable-image-with-gwt20-and.html
	super(Modeller.instance().getImages().closeTab().getSafeUri());
	setTitle(Modeller.constants.closeThisTab());
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

}
