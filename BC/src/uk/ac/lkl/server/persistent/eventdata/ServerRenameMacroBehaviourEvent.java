/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

import uk.ac.lkl.shared.CommonUtils;

/**
 * Represents the renaming of a macro-behaviour (prototype). JDO implementation.
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerRenameMacroBehaviourEvent extends ServerEvent {

    @Persistent
    private Text oldNameText;
    @Persistent
    private Text newNameText;
    // for backwards compatibility
    @Persistent
    private String oldName;
    @Persistent
    private String newName;
    
    public ServerRenameMacroBehaviourEvent(
	    String oldName, 
	    String newName,
	    String sessionGuid, 
	    String userGuid) {
	super(sessionGuid, userGuid);
	this.oldNameText = new Text(oldName);
	this.newNameText = new Text(newName);
    }

    @Override
    public String toXML() {
	return "<RenameMacroBehaviourEvent version='5'" + 
	       additionalXMLAttributes() + ">" +
	       "<name>" + CommonUtils.createCDATASection(getOldName()) + "</name>" +
	       "<newName>" + CommonUtils.createCDATASection(getNewName()) + "</newName>" +
	       "</RenameMacroBehaviourEvent>";
    }

    private String getOldName() {
	if (oldNameText != null) {
	    return oldNameText.getValue();
	} else {
	    return oldName;
	}
    }

    private String getNewName() {
	if (newNameText != null) {
	    return newNameText.getValue();
	} else {
	    return newName;
	}
    }

}
