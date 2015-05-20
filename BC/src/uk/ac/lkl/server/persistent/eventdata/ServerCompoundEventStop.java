/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;

/**
 * Represents the end of a compound event (using JDO)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerCompoundEventStop extends ServerEvent {

    public ServerCompoundEventStop(String sessionGuid, String userGuid) {
	super(sessionGuid, userGuid);
    }

    @Override
    public String toXML() {
	return "</CompoundEvent>";
    }

}
