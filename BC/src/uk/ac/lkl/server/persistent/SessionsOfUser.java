/**
 * 
 */
package uk.ac.lkl.server.persistent;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

/**
 * Represents all the sessions loaded by the user identified by userGuid
 * Note that these sessions can be shared by users
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class SessionsOfUser {
    
    @PrimaryKey
    String userGuid;
    @Persistent
    // I think String is better than String[] in the data store
    // Maybe this was a mistake and String[] would have been best...
    @Deprecated
    private String sessionGuids = "";
    // above didn't support more than a couple dozen models
    @Persistent
    private Text sessionGuidsAsText = null;
    
    public SessionsOfUser(String userGuid, String sessionGuids) {
	super();
//	this.sessionGuids = sessionGuids;
	this.sessionGuidsAsText = new Text(sessionGuids);
	this.userGuid = userGuid;
    }

    public String[] getSessionGuids() {
	String sessions = sessionGuids != null ? sessionGuids : sessionGuidsAsText.getValue();
        return sessions.split(";");
    }

    public void setSessionGuids(String sessionGuids) {
        this.sessionGuidsAsText = new Text(sessionGuids);
    }

    /**
     * @param sessionGuid
     * 
     * Adds session to the list of sessionGuids if not already there
     * @return true if any change made
     */
    public boolean addSession(String sessionGuid) {
	if (sessionGuid == null) {
	    return false;
	}
	if (this.sessionGuids != null) {
	    // transit from String to Text
	    this.sessionGuidsAsText = new Text(this.sessionGuids);
	    this.sessionGuids = null;
	}
	String sessions = sessionGuidsAsText.getValue();
	if (!sessions.contains(sessionGuid)) {
	    // most recent first since that is how we want to present the list to the user
	    sessions = sessionGuid + ";" + sessions;
	    sessionGuidsAsText = new Text(sessions);
	    return true;
	}
	return false;
    }
    
    public boolean removeSession(String sessionGuid) {
	if (sessionGuid == null) {
	    return false;
	}
	String sessions = sessionGuidsAsText.getValue();
	if (sessions.contains(sessionGuid)) {
	    sessions = sessions.replace(sessionGuid + ";", "");
	    sessionGuidsAsText = new Text(sessions);
	    return true;
	}
	return false;
    }

}
