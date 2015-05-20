/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import uk.ac.lkl.server.basicLTI.BLTIUser;


import com.google.appengine.api.datastore.Key;

/**
 * Super class for storing events using JDO
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class ServerEvent {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)  
    private Key id;
    @Persistent
    private String sessionGuid;
    @Persistent
    private String userGuid;
    @Persistent
    private Date timeStamp;
    
    /**
     * @param sessionGuid
     * @param userGuid
     */
    public ServerEvent(String sessionGuid, String userGuid) {
	this.sessionGuid = sessionGuid;
	this.userGuid = userGuid;
	timeStamp = new Date();	
    }
    
    public int compareTo(ServerEvent other) {
	return getTimeStamp().compareTo(other.getTimeStamp());
    }

    public String getUserGuid() {
        return userGuid;
    }
//
//    public Key getId() {
//        return id;
//    }

    public String getSessionGuid() {
        return sessionGuid;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    abstract public String toXML();

    protected String additionalXMLAttributes() {
	String additionalAttributes = " date='" + getTimeStamp().getTime() + "' userGuid='" + userGuid + "' ";
	BLTIUser bltiUser = BLTIUser.getBLTIUser(userGuid);
	if (bltiUser != null) {
	    String fullName = bltiUser.getFullName();
	    additionalAttributes += " userName='" + fullName + "' ";
	}
	return additionalAttributes;
    }

    public List<String> additionalXML() {
	// subclasses may override this
	return null;
    }
}
