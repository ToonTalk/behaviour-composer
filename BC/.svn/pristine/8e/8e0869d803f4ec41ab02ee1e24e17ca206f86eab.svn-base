/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * 
 * Represents undo or redo in the history of a session (JDO implementation)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerUndoRedoEvent extends ServerEvent {
    @Persistent
    private boolean undo;

    public ServerUndoRedoEvent(boolean undo, String sessionGuid, String userGuid) {
	super(sessionGuid, userGuid);
	this.undo = undo;
    }

    @Override
    public String toXML() {
	String elementName = undo ? "undo" : "redo";
	return "<" + elementName + " version='5' " + additionalXMLAttributes() + " />";	
    }

}
