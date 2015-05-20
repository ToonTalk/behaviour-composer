package uk.ac.lkl.client;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

public class MenuItemWithDebugId extends MenuItem {

    public MenuItemWithDebugId(SafeHtml html, MenuBar subMenu) {
	super(html, subMenu);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

    public MenuItemWithDebugId(SafeHtml html, ScheduledCommand cmd) {
	super(html, cmd);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

    public MenuItemWithDebugId(SafeHtml html) {
	super(html);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

    public MenuItemWithDebugId(String text, boolean asHTML, MenuBar subMenu) {
	super(text, asHTML, subMenu);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

    public MenuItemWithDebugId(String text, MenuBar subMenu) {
	super(text, subMenu);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

    public MenuItemWithDebugId(String text, ScheduledCommand cmd) {
	super(text, cmd);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

    public MenuItemWithDebugId(String text, boolean asHTML, ScheduledCommand cmd) {
	super(text, asHTML, cmd);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }

}
