/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.shared.CommonUtils;

/**
 * Represents the enhancement of a micro-behaviour with scheduling 
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerEnhanceMicroBehaviourEvent extends ServerEvent {
    @Persistent
    private MicroBehaviourEnhancement enhancement;
    @Deprecated
    // not clear why the following was marked as persistent
    private String url;
    @Persistent
    private Text urls;
    private String tabTitle; // better always be less than 500 characters
    
    public ServerEnhanceMicroBehaviourEvent(MicroBehaviourEnhancement enhancement, String urls, String tabTitle, String sessionGuid, String userGuid) {
	super(sessionGuid, userGuid);
	this.enhancement = enhancement;
	this.urls = new Text(urls);
	this.tabTitle = tabTitle;
    }

    @Override
    public String toXML() {
	return "<EnhanceMicroBehaviourEvent version='5' " + 
	       "enhancement='" + Integer.toString(enhancement.ordinal()) + "' " +
	       additionalXMLAttributes() + ">" +
	       "<url>" + getUrls() + "</url>" +
	       "<tabTitle>" + CommonUtils.createCDATASection(tabTitle) + "</tabTitle>" +
	       "</EnhanceMicroBehaviourEvent>";	
    }
    
    private String getUrls() {
	if (urls != null) {
	    return urls.getValue();
	} else {
	    return url;
	}
    }

}
