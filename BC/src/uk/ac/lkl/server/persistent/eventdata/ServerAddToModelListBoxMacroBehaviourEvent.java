/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import uk.ac.lkl.shared.CommonUtils;

/**
 * Represents the selection of a prototype menu to choose between
 * visible, invisible, or list (JDO implementation)
 * 
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerAddToModelListBoxMacroBehaviourEvent extends MacroBehaviourServerEvent {
    
    @Persistent
    private boolean addToModelAtEventTime;
    @Persistent
    private boolean addToModelBeforeEvent;
    @Persistent
    private boolean visibleInModelAtEventTime;
    @Persistent
    private boolean visibleInModelBeforeEvent;

    public ServerAddToModelListBoxMacroBehaviourEvent(
	    boolean addToModelAtEventTime,
	    boolean addToModelBeforeEvent,
	    boolean visibleInModelAtEventTime,
	    boolean visibleInModelBeforeEvent,
	    String macroBehaviourNameAtEventTime, 
	    String sessionGuid,
	    String userGuid) {
	super(macroBehaviourNameAtEventTime, sessionGuid, userGuid);
	this.addToModelAtEventTime = addToModelAtEventTime;
	this.addToModelBeforeEvent = addToModelBeforeEvent;
	this.visibleInModelAtEventTime = visibleInModelAtEventTime;
	this.visibleInModelBeforeEvent = visibleInModelBeforeEvent;
    }

    @Override
    public String toXML() {
	return "<AddToModelListBoxMacroBehaviourEvent version='5'" + 
	       " addToModel='" + (addToModelAtEventTime?"1":"0") + "'" +
	       " addToModelBeforeEvent='" + (addToModelBeforeEvent?"1":"0") + "'" +
	       " visibleInModel='" + (visibleInModelAtEventTime?"1":"0") + "'" +
	       " visibleInModelBeforeEvent='" + (visibleInModelBeforeEvent?"1":"0") + "'" +
	       additionalXMLAttributes() + ">" +
	       "<name>" + CommonUtils.createCDATASection(getMacroBehaviourNameAtEventTime()) + "</name>" +
	       "</AddToModelListBoxMacroBehaviourEvent>";
    }

}
