/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import uk.ac.lkl.shared.CommonUtils;

/**
 * Represents the selection of a prototype menu to choose between
 * visible, invisible, or list (JDO implementation)
 * 
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerVisibleInModelEvent extends MacroBehaviourServerEvent {
    
    @Persistent
    private boolean visible;

    public ServerVisibleInModelEvent(
	    boolean visible,
	    String macroBehaviourNameAtEventTime, 
	    String sessionGuid,
	    String userGuid) {
	super(macroBehaviourNameAtEventTime, sessionGuid, userGuid);
	this.visible = visible;
    }

    @Override
    public String toXML() {
	return "<VisibleInModelEvent version='5'" + 
	       " visibleInModel='" + (visible?"1":"0") + "'" +
	       additionalXMLAttributes() + ">" +
	       "<name>" + CommonUtils.createCDATASection(getMacroBehaviourNameAtEventTime()) + "</name>" +
	       "</VisibleInModelEvent>";
    }

}
