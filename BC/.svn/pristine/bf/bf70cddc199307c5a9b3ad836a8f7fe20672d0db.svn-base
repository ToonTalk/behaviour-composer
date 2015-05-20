/**
 * 
 */
package uk.ac.lkl.server.persistent;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Represents a session in the JDO database
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class Session {
    
    // lookup the sessionGuid given the readOnlyGuid
    @PrimaryKey
    String readOnlyGuid;
    @Persistent
    String sessionGuid;
    @Persistent
    private Date timeStamp;
    
    public Session(String sessionGuid, String readOnlyGuid) {
	super();
	this.sessionGuid = sessionGuid;
	this.readOnlyGuid = readOnlyGuid;
	timeStamp = new Date();	
    }

    public String getSessionGuid() {
        return sessionGuid;
    }

    public String getReadOnlyGuid() {
        return readOnlyGuid;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }
    
    public Date updateTimeStamp() {
	timeStamp = new Date();
	return timeStamp;
    }

}
