/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.Widget;

/**
 * Used to provide a programmatic hook to micro behaviour editor
 * 
 * @author Ken Kahn
 *
 */

public interface MicroBehaviourEditorCommand {
    
    public void execute(Widget widget);

}
