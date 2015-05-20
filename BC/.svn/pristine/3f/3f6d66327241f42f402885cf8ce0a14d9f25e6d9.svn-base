/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import uk.ac.lkl.shared.CommonUtils;

/**
 * Represents the activation (or inactivation) of a macro behaviour. (Server version)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerActivateMacroBehaviourEvent extends MacroBehaviourServerEvent {

    @Persistent
    private boolean activate;

    public ServerActivateMacroBehaviourEvent(
	    boolean activate,
	    String macroBehaviourNameAtEventTime, 
	    String sessionGuid,
	    String userGuid) {
	super(macroBehaviourNameAtEventTime, sessionGuid, userGuid);
	this.activate = activate;
    }
      
    public boolean getActivate() {
        return activate;
    }

    public String toXML() {
	String elementName = activate ? "ActivateMacroBehaviourEvent" : "InactivateMacroBehaviourEvent";
	return "<" + elementName + " version='5'" + 
	        additionalXMLAttributes() + ">" +
	        "<name>" + CommonUtils.createCDATASection(getMacroBehaviourNameAtEventTime()) + "</name>" +
	        "</" + elementName + ">";
    }

//    @Override
//    public ModellerEvent getClientEvent() {
//	return new uk.ac.lkl.client.event.ActivateMacroBehaviourEvent(macroBehaviourNameAtEventTime);
//    }

}
