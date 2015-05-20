/**
 * 
 */
package uk.ac.lkl.client;

import java.util.ArrayList;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author Ken Kahn
 *
 */
public class Warning extends HTML {
    
    protected ArrayList<Command> loadCommands = new ArrayList<Command>();
    
    public Warning(String html) {
	super(html);
    }
    
    public void addLoadCommand(Command command) {
	loadCommands.add(command);
    }
    
    @Override
    public void onLoad() {
	for (Command command : loadCommands) {
	    command.execute();
	}
	loadCommands.clear();
    }

}
