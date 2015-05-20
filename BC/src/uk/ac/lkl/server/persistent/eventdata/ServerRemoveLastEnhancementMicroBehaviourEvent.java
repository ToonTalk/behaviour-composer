package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.shared.CommonUtils;

@PersistenceCapable
public class ServerRemoveLastEnhancementMicroBehaviourEvent extends ServerEvent {
    @Persistent
    private MicroBehaviourEnhancement enhancement;
    @Deprecated
    private String url;
    @Persistent
    private Text urls;
    private String tabTitle; // better always be less than 500 characters
    private Text textAreaContents;
    private int textAreaIndex;

    public ServerRemoveLastEnhancementMicroBehaviourEvent(
	    MicroBehaviourEnhancement enhancement, 
	    String textAreaContents,
	    int textAreaIndex, 
	    String urls, 
	    String tabTitle, 
	    String sessionGuid,
	    String userGuid) {
	super(sessionGuid, userGuid);
	this.enhancement = enhancement;
	this.urls = new Text(urls);
	this.tabTitle = tabTitle;
	this.textAreaContents = new Text(textAreaContents);
	this.textAreaIndex = textAreaIndex;
    }

    @Override
    public String toXML() {
	return "<RemoveLastEnhancementMicroBehaviourEvent version='5' " + 
	       "enhancement='" + Integer.toString(enhancement.ordinal()) + "' " +
	       "textAreaIndex='" + textAreaIndex + "' " +
	       additionalXMLAttributes() + ">" +
	       "<url>" + getUrls() + "</url>" +
	       "<tabTitle>" + CommonUtils.createCDATASection(tabTitle) + "</tabTitle>" +
	       "<textAreaContents>" + CommonUtils.createCDATASection(textAreaContents.getValue()) + "</textAreaContents>" +
	       "</RemoveLastEnhancementMicroBehaviourEvent>";	
    }
    
    public String getUrls() {
	if (urls != null) {
	    return urls.getValue();
	} else {
	    return url;
	}
    }

}
