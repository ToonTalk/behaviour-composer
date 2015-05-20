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
 * Represents the addition or removal of a micro behaviour. (Server version)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerAddMicroBehaviourEvent extends MicroBehaviourServerEvent {
    @Persistent
    private boolean add;
    @Persistent
    // kept for backwards compatibility
    private String url;
    @Persistent
    private Text urlText;
    @Persistent
    boolean macroBehaviourAsMicroBehaviour;
    @Persistent
    private String containingMicroBehaviourUrl;
    @Persistent
    private int insertionIndex;

    public ServerAddMicroBehaviourEvent(
	    boolean add, 
	    String macroBehaviourNameAtEventTime, 
	    String url,
	    String microBehaviourName,
	    boolean macroBehaviourAsMicroBehaviour,
	    String containingMicroBehaviourUrl,
	    int insertionIndex,
	    String sessionGuid,
	    String userGuid) {
	super(microBehaviourName, macroBehaviourNameAtEventTime, sessionGuid, userGuid);
	this.add = add;
	// if URL is for a macro-behaviour acting like a micro-behaviour
	// it can be longer than the 500 character limit
	this.urlText = new Text(url);
	this.macroBehaviourAsMicroBehaviour = macroBehaviourAsMicroBehaviour;
	this.containingMicroBehaviourUrl = containingMicroBehaviourUrl;
	this.insertionIndex = insertionIndex;
    }
    
    public String toXML() {
	StringBuffer xml = new StringBuffer();
	String elementName = add ? "AddMicroBehaviourEvent" : "RemoveMicroBehaviourEvent";
	xml.append("<" + elementName + " version='5'");
	if (add) {
	    xml.append(" insertionIndex='" + getInsertionIndex() + "'");
	}
	xml.append(additionalXMLAttributes());
	xml.append(">");
	if (containingMicroBehaviourUrl != null) {
	    xml.append("<containingURL>" + containingMicroBehaviourUrl + "</containingURL>");
	}
	xml.append("<name>" + CommonUtils.createCDATASection(getMacroBehaviourNameAtEventTime()) + "</name>");
	// TODO: rationalise the following
	String urlString = getUrl();
	String nameIfMacroBehaviour = CommonUtils.prototypeName(urlString);
	String description;
	String microBehaviourName = getMicroBehaviourName();
	if (microBehaviourName != null) {
	    description = microBehaviourName;
	} else {
	    description =
		nameIfMacroBehaviour == null ? ServerUtils.getDescription(urlString) : CommonUtils.prototypeName(urlString);
	}
	xml.append("<description>" + CommonUtils.createCDATASection(description) + "</description>");
	xml.append("<url>" + CommonUtils.createCDATASection(urlString) + "</url>");
	xml.append("</" + elementName + ">");
	return xml.toString();
    }
    
    public boolean getAdd() {
	return add;
    }
    
    public String getUrl() {
	if (urlText == null) {
	    return url;
	} else {
	    return urlText.getValue();
	}
    }

    public String getContainingMicroBehaviourUrl() {
        return containingMicroBehaviourUrl;
    }

    public boolean getMacroBehaviourAsMicroBehaviour() {
        return macroBehaviourAsMicroBehaviour;
    }

    public int getInsertionIndex() {
        return insertionIndex;
    }

}
