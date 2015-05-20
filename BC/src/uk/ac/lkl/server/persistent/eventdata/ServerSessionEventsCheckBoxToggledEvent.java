/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Represents clicking on a session check box. JDO implementation
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerSessionEventsCheckBoxToggledEvent extends ServerEvent {

    @Persistent
    private boolean valueAtEventTime;
    @Persistent
    private String checkBoxSessionGuid; 
    @Persistent
    private String nameId; 
    
    public ServerSessionEventsCheckBoxToggledEvent(
	    boolean valueAtEventTime,
	    String checkBoxSessionGuid, 
	    String nameId, 
	    String sessionGuid,
	    String userGuid) {
	super(sessionGuid, userGuid);
	this.valueAtEventTime = valueAtEventTime;
	this.checkBoxSessionGuid = checkBoxSessionGuid;
	this.nameId = nameId;
    }

    @Override
    public String toXML() {
	return "<SessionEventsCheckBoxToggledEvent version='4'" + 
		" valueAtEventTime='" + (valueAtEventTime?1:0) + "'" +
		" checkBoxSessionGuid='" + checkBoxSessionGuid + "'" +
		" nameId='" + nameId + "'" +
		additionalXMLAttributes() + " />";
    }

}
