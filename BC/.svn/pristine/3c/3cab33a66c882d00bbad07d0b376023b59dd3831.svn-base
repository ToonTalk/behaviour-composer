package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.Button;

/**
 * A button when GWT module includes <inherits name="com.google.gwt.user.Debug"/> will add a consistent id
 * Useful to tests using Selenium 
 * 
 * @author Ken  Kahn
 *
 */
public class ButtonWithDebugID extends Button {
    public ButtonWithDebugID(String html) {
	super(html);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

    public ButtonWithDebugID() {
	super();
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

}
