/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import uk.ac.lkl.shared.CommonUtils;

/**
 * Represents renaming a micro-behaviour (JDO implementation).
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerReplaceURLEvent extends ServerEvent {

    @Persistent
    private String oldURL; 
    @Persistent
    private String newURL;
    
    public ServerReplaceURLEvent(
	    String oldURL, 
	    String newURL,
	    String sessionGuid, 
	    String userGuid) {
	super(sessionGuid, userGuid);
	this.oldURL = oldURL;
	this.newURL = newURL;
    }

    @Override
    public String toXML() {
	return "<ReplaceURLEvent version='5'" + 
	       additionalXMLAttributes() + ">" +
	       "<oldURL>" + CommonUtils.createCDATASection(oldURL) + "</oldURL>" +
	       "<newURL>" + CommonUtils.createCDATASection(newURL) + "</newURL>" +
	       "</ReplaceURLEvent>";
    }

}
