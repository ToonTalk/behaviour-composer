/**
 * 
 */
package uk.ac.lkl.server.persistent;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

/**
 * This stores a page edited by the user
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class EditedPage {
    
    @PrimaryKey
    private String url;
    @Persistent
    private String oldURL;
    @Persistent
    private Text contents;
    @Persistent
    private String sessionGuid;
    @Persistent
    private Date timeStamp;
    @Persistent
    private Boolean readOnly = false;
    
    public EditedPage(String newURL, String oldURL, String contents, String sessionGuid) {
	this(newURL, oldURL, contents, sessionGuid, new Date());
    }
	
    public EditedPage(String newURL, String oldURL, String contents, String sessionGuid, Date timeStamp) {
	this.url = newURL;
	this.oldURL = oldURL;
	this.contents = new Text(contents);
	this.sessionGuid = sessionGuid;
	this.timeStamp = timeStamp;
    }

    public String getUrl() {
        return url;
    }

    public String getOldURL() {
        return oldURL;
    }

    public String getContents() {
        return contents.getValue();
    }

    public String getSessionGuid() {
        return sessionGuid;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setContents(String contents) {
        this.contents = new Text(contents);
        timeStamp = new Date();
    }

    public boolean isReadOnly() {
	if (readOnly == null) {
	    // old version that doesn't have readOnly member
	    return false;
	}
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

}
