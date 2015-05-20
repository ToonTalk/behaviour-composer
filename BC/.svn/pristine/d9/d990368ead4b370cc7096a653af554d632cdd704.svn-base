/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

import uk.ac.lkl.shared.CommonUtils;

/**
 * Represents updating of a text area on a micro-behaviour (JDO implementation)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerUpdateTextAreaEvent extends ServerEvent {
    
    @Persistent
    private Text oldContents;
    @Persistent
    private Text newContents;
    @Persistent
    private int indexInCode; 
    @Persistent
    @Deprecated
    private String microBehaviourURL;
    @Persistent
    private Text urls;
    @Persistent
    private Text nameText;
    @Persistent
    // for backwards compatibility
    private String name;
    @Persistent
    private String tabTitle; 

    public ServerUpdateTextAreaEvent(
	    String oldContents, 
	    String newContents,
	    int indexInCode, 
	    String urls, 
	    String name,
	    String tabTitle, 
	    String sessionGuid, 
	    String userGuid) {
	super(sessionGuid, userGuid);
	this.oldContents = new Text(oldContents);
	this.newContents = new Text(newContents);
	this.indexInCode = indexInCode;
	this.urls = new Text(urls);
	this.nameText = new Text(name);
	this.tabTitle = tabTitle;	
    }

    @Override
    public String toXML() {
	return "<UpdateTextAreaEvent version='5'" + 
		" index='" + indexInCode + "'" +
		" name='" + getName() + "'" +
		additionalXMLAttributes() + ">" +
		"<oldContents>" + CommonUtils.createCDATASection(oldContents.getValue()) + "</oldContents>" +
		"<newContents>" + CommonUtils.createCDATASection(newContents.getValue()) + "</newContents>" +
		"<url>" + getUrls() + "</url>" +
		"<tabTitle>" + CommonUtils.createCDATASection(tabTitle) + "</tabTitle>" +
		"</UpdateTextAreaEvent>";
    }

    private String getName() {
	if (nameText != null) {
	    return nameText.getValue();
	} else {
	    return name;
	}
    }
    
    private String getUrls() {
	if (urls != null) {
	    return urls.getValue();
	} else {
	    return microBehaviourURL;
	}
    }

}
