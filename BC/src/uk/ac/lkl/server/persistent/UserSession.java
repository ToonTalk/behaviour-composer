/**
 * 
 */
package uk.ac.lkl.server.persistent;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import uk.ac.lkl.server.ServerUtils;
import uk.ac.lkl.server.basicLTI.BLTIUser;

import com.google.appengine.api.datastore.Text;

/**
 * Records a new session of a user.
 * 
 * sessionGuid is primary key since it is unique (1-to-many from user to sessions)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class UserSession {
    
    @PrimaryKey
    private String sessionGuid;
    @Persistent
    private String userGuid;
    @Persistent
    private Text description;
    @Persistent
    private Text infoTab;
    @Persistent
    Boolean visible = true;
    @Persistent
    private Date timeStamp;
    @Persistent
    private String bltiContextId;
    
    public UserSession(String userGuid, String sessionGuid, Text description, Text infoTab, boolean visible) {
	this.userGuid = userGuid;
	this.sessionGuid = sessionGuid;
	this.description = description;
	this.infoTab = infoTab;
	this.visible = visible;
	timeStamp = new Date();
	BLTIUser bltiUser = BLTIUser.getBLTIUser(userGuid);
	if (bltiUser != null) {
	    this.bltiContextId = bltiUser.getCurrentContextId();
	}
    }
    
    public UserSession(String userGuid, String sessionGuid) {
	this(userGuid, sessionGuid, ServerUtils.NULL_TEXT, ServerUtils.NULL_TEXT, true);
    }

    public String getUserGuid() {
        return userGuid;
    }

    public String getSessionGuid() {
        return sessionGuid;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getDescription() {
        return description.getValue();
    }

    public void setDescription(Text description) {
        this.description = description;
    }

    public String getBltiContextId() {
        return bltiContextId;
    }

    public boolean isVisible() {
	if (visible == null) {
	    visible = true;
	}
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getInfoTab() {
	if (infoTab == null) {
	    return null;
	} else {
	    return infoTab.getValue();
	}
    }

    public void setInfoTab(Text infoTab) {
        this.infoTab = infoTab;
    }

}
