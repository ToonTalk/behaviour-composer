/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * Represents the identity of a user
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class User {
    @PrimaryKey
    private String guidString;
    @Persistent
    // consider replacing this with a one-way hash for privacy reasons
    private String userAgent;
    @Persistent
    private Date creationTime;
    
    public User(String guidString, String userAgent) {
	this.guidString = guidString;
	this.userAgent = userAgent;
	creationTime = new Date();	
    }

    public String getGuidString() {
        return guidString;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Date getCreationTime() {
        return creationTime;
    }
    
}
