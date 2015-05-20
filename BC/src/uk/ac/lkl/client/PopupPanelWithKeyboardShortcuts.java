package uk.ac.lkl.client;

import java.util.HashMap;

import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

public class PopupPanelWithKeyboardShortcuts extends PopupPanel { 
    private HashMap<Character, Command> keyCommandMap = 
        new HashMap<Character, Command>();
    
    public PopupPanelWithKeyboardShortcuts(boolean autoHide) { 
        super(autoHide);
        ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }
    
    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
	int key = event.getNativeEvent().getKeyCode();
	if (key == KeyCodes.KEY_ESCAPE) {
	    // user dismisses the popup menu
	    hide();
	} else if (key > 32) {
	    Command command = keyCommandMap.get((char) key); 
	    if (command != null) { 
		command.execute();
		hide();
	    }
	}
	super.onPreviewNativeEvent(event);
    }
    
//    @Override 
//    public boolean onKeyPressPreview(char key, int modifiers) { 
//        Command command = keyCommandMap.get(key); 
//        if (command != null) { 
//            command.execute(); 
//        }
//        hide(); 
//        return super.onKeyPressPreview(key, modifiers); 
//    } 
    
    public void addKeyCommand(Character character, Command command) { 
        keyCommandMap.put(Character.toLowerCase(character), command);
        keyCommandMap.put(Character.toUpperCase(character), command);
    }

    public MenuItem createMenuItem(char shortcutKey, String text, String title, Command command) {
//	final StringBuffer underline = new StringBuffer();
//	underline.append(shortcutKey);
	// if translation is enabled then just add key at the end underlined and in parentheses
	// otherwise underline first occurrences of the letter
	String label = Modeller.instance().isTranslateEnabled() ? 
		       text + " (<u>" + Character.toUpperCase(shortcutKey) + "</u>) :" :
		       CommonUtils.underline(shortcutKey + "", text); // converts char to String
	MenuItem menuItem = new MenuItemWithDebugId(label, true, command);
	menuItem.setTitle(title);
	addKeyCommand(shortcutKey, command);
	return menuItem;
    } 
} 
