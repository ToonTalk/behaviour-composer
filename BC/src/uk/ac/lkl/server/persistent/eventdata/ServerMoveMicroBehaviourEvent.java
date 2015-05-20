/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

import uk.ac.lkl.server.ServerUtils;
import uk.ac.lkl.shared.CommonUtils;

/**
 * Represents moving a micro-behaviour up or down in a list (in JDO implementation)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerMoveMicroBehaviourEvent extends MicroBehaviourServerEvent {

    @Persistent
    private boolean up; 
    @Persistent
    @Deprecated
    private String url; 
    @Persistent
    private Text urls;
    @Persistent
    private String containingMicroBehaviourUrl;
    
    public ServerMoveMicroBehaviourEvent(
	    boolean up, 
	    String urls, 
	    String microBehaviourName,
	    String containingMicroBehaviourUrl,
	    String macroBehaviourNameAtEventTime, 
	    String sessionGuid, 
	    String userGuid) {
	super(microBehaviourName, macroBehaviourNameAtEventTime, sessionGuid, userGuid);
	this.up = up;
	this.urls = new Text(urls);
	this.containingMicroBehaviourUrl = containingMicroBehaviourUrl;
    }

    @Override
    public String toXML() {
	StringBuffer xml = new StringBuffer();
	xml.append("<MoveMicroBehaviourEvent version='5'");
	xml.append(additionalXMLAttributes());
	xml.append(" up='" + (up?"1":"0") + "'>");
	if (containingMicroBehaviourUrl != null) {
	    xml.append("<containingURL>" + containingMicroBehaviourUrl + "</containingURL>");
	}
	xml.append("<name>" + CommonUtils.createCDATASection(getMacroBehaviourNameAtEventTime()) + "</name>");
	// TODO: rationalise the following
	String allUrls = getUrls();
	String lastURL;
	int semiColonIndex = allUrls.indexOf(';');
	if (semiColonIndex < 0) {
	    lastURL = allUrls;
	} else {
	    lastURL = allUrls.substring(0, semiColonIndex);
	}
	String nameIfMacroBehaviour = CommonUtils.prototypeName(lastURL);
	String description;
	// couldn't microBehaviourName be correct in all situations?
	if (!CommonUtils.hasChangesGuid(lastURL)) {
	    description = getMicroBehaviourName();
	} else {
	    description =
		nameIfMacroBehaviour == null ? ServerUtils.getDescription(lastURL) : CommonUtils.prototypeName(lastURL);
	}
	xml.append("<description>" + CommonUtils.createCDATASection(description) + "</description>");
	xml.append("<url>" + CommonUtils.createCDATASection(allUrls) + "</url>");
	xml.append("</MoveMicroBehaviourEvent>");
	return xml.toString();
    }

    public String getUrls() {
	if (urls != null) {
	    return urls.getValue();
	} else {
	    return url;
	}
    }

    public boolean isUp() {
        return up;
    }

    public String getContainingMicroBehaviourUrl() {
        return containingMicroBehaviourUrl;
    }

}
