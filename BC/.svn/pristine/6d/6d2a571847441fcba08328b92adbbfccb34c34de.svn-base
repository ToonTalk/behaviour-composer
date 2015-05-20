/**
 * 
 */
package uk.ac.lkl.server.persistent;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Represents sessions that have been replaced by newer (compact) ones
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ReplacedSession {
    
    @PrimaryKey
    String replacedSessionGuid;
    @Persistent
    String newSessionGuid;
    
    public ReplacedSession(String replacedSessionGuid, String newSessionGuid) {
	super();
	this.replacedSessionGuid = replacedSessionGuid;
	this.newSessionGuid = newSessionGuid;
    }

    public String getNewSessionGuid() {
        return newSessionGuid;
    }

}
