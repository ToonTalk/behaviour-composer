package uk.ac.lkl.client.composer;

import uk.ac.lkl.client.MenuItemWithDebugId;
import uk.ac.lkl.client.Modeller;

import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

public class MenuBarWithDebugID extends MenuBar {

    public MenuBarWithDebugID() {
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

    public MenuBarWithDebugID(boolean vertical) {
	super(vertical);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

    public MenuBarWithDebugID(Resources resources) {
	super(resources);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

    public MenuBarWithDebugID(boolean vertical, Resources resources) {
	super(vertical, resources);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }
    
    public MenuItem addItem(String text, MenuBar popup) {
	return addItem(new MenuItemWithDebugId(text, popup));
    }

}
