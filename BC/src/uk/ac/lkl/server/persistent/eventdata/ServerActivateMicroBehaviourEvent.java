/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

import uk.ac.lkl.shared.CommonUtils;


/**
 * Represents the activation (or inactivation) of a macro behaviour. (Server version)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerActivateMicroBehaviourEvent extends MacroBehaviourServerEvent {

    @Persistent
    private boolean activate;
    @Persistent
    @Deprecated
    private String url;
    @Persistent
    private Text urls;
    @Persistent
    boolean macroBehaviourAsMicroBehaviour;
    @Persistent
    private String containingMicroBehaviourUrl;

    public ServerActivateMicroBehaviourEvent(
	    boolean activate,
	    String urls,
	    String macroBehaviourNameAtEventTime, 
	    boolean macroBehaviourAsMicroBehaviour,
	    String containingMicroBehaviourUrl, 
	    String sessionGuid,
	    String userGuid) {
	super(macroBehaviourNameAtEventTime, sessionGuid, userGuid);
	this.activate = activate;
	this.urls = new Text(urls);
	this.macroBehaviourAsMicroBehaviour = macroBehaviourAsMicroBehaviour;
	this.containingMicroBehaviourUrl = containingMicroBehaviourUrl;
    }
    
    public String toXML() {
	StringBuffer xml = new StringBuffer();
	String elementName = activate ? "ActivateMicroBehaviourEvent" : "InactivateMicroBehaviourEvent";
	xml.append("<");
	xml.append(elementName);
	xml.append(" version='5'");
	xml.append(additionalXMLAttributes());
	xml.append(">");
	if (containingMicroBehaviourUrl != null) {
	    xml.append("<containingURL>" + containingMicroBehaviourUrl + "</containingURL>");
	}
	xml.append("<url>" + CommonUtils.createCDATASection(getUrls()) + "</url>");
	xml.append("<name>" + CommonUtils.createCDATASection(getMacroBehaviourNameAtEventTime()) + "</name>");
	xml.append("</");
	xml.append(elementName);
	xml.append(">");
	return xml.toString();
    }
    
    public boolean getActivate() {
        return activate;
    }
    
    public String getUrls() {
	if (urls != null) {
	    return urls.getValue();
	} else {
	    return url;
	}
    }

    public String getContainingMicroBehaviourUrl() {
        return containingMicroBehaviourUrl;
    }

    public boolean getMacroBehaviourAsMicroBehaviour() {
        return macroBehaviourAsMicroBehaviour;
    }

}
