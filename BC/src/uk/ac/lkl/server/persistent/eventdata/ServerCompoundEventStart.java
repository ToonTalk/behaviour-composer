/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;

/**
 * Represents the start of a compound event (using JDO)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerCompoundEventStart extends ServerEvent {

    public ServerCompoundEventStart(String sessionGuid, String userGuid) {
	super(sessionGuid, userGuid);
    }

    @Override
    public String toXML() {
	// just for the 'start'
	return "<CompoundEvent>";
    }

}
