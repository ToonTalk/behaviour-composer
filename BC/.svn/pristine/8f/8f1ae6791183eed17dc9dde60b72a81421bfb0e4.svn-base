/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import uk.ac.lkl.shared.CommonUtils;

/**
 * Represents the addition or removal of a macro behaviour (prototype). (Server version)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerAddMacroBehaviourEvent extends MacroBehaviourServerEvent {

    @Persistent
    private boolean add;

    public ServerAddMacroBehaviourEvent(
	    boolean add, 
	    String macroBehaviourNameAtEventTime, 
	    String sessionGuid,
	    String userGuid) {
	super(macroBehaviourNameAtEventTime, sessionGuid, userGuid);
	this.add = add;
    }
    
    public String toXML() {
	String elementName = add ? "AddMacroBehaviourEvent" : "RemoveMacroBehaviourEvent";
	return "<" + elementName + " version='5'" + 
	        additionalXMLAttributes() + ">" +
	        "<name>" + CommonUtils.createCDATASection(getMacroBehaviourNameAtEventTime()) + "</name>" +
	        "</" + elementName + ">";
    }

    public boolean getAdd() {
        return add;
    }

}
