/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

import uk.ac.lkl.shared.CommonUtils;

/**
 * Represents renaming a micro-behaviour (JDO implementation).
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerRenameMicroBehaviourEvent extends ServerEvent {

    @Deprecated
    @Persistent
    private String url; 
    @Persistent
    private Text urls;
    @Persistent
    private Text oldNameText;
    @Persistent
    private Text newNameText;
    // for backwards compatibility:
    @Persistent
    private String oldName;
    @Persistent
    private String newName;
    
    public ServerRenameMicroBehaviourEvent(
	    String urls, 
	    String oldName,
	    String newName, 
	    String sessionGuid, 
	    String userGuid) {
	super(sessionGuid, userGuid);
	this.urls = new Text(urls);
	this.oldNameText = new Text(oldName);
	this.newNameText = new Text(newName);
    }

    @Override
    public String toXML() {
	return "<RenameMicroBehaviourEvent version='5'" + 
	       additionalXMLAttributes() + ">" +
	       "<url>" + CommonUtils.createCDATASection(getUrls()) + "</url>" +
	       "<name>" + CommonUtils.createCDATASection(getOldName()) + "</name>" +
	       "<newName>" + CommonUtils.createCDATASection(getNewName()) + "</newName>" +
	       "</RenameMicroBehaviourEvent>";
    }

    /**
     * @return
     */
    protected String getNewName() {
	if (newNameText == null) {
	    return newName;
	} else {
	    return newNameText.getValue();
	}
    }

    /**
     * @return
     */
    protected String getOldName() {
	if (oldNameText == null) {
	    return oldName;
	} else {
	    return oldNameText.getValue();
	}
    }
    
    public String getUrls() {
	if (urls != null) {
	    return urls.getValue();
	} else {
	    return url;
	}
    }

}
