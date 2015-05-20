/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import uk.ac.lkl.client.event.ModellerEvent;
import uk.ac.lkl.client.event.StartEvent;
import uk.ac.lkl.server.persistent.ModelXML;
import uk.ac.lkl.server.persistent.UsageStatistics;

/**
 * Represents the start of a session.
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerStartEvent extends ServerEvent {
    @Persistent
    private String readOnlySessionGuid;
    @Persistent
    private String initialReadOnlySessionGuid;
    @Persistent
    private String initialModelGuid;
    
    public ServerStartEvent(
	    String readOnlySessionGuid, 
	    String initialReadOnlySessionGuid,
	    String initialModelGuid,
	    String sessionGuid,
	    String userGuid) {
	super(sessionGuid, userGuid);
	this.readOnlySessionGuid = readOnlySessionGuid;
	this.initialReadOnlySessionGuid = initialReadOnlySessionGuid;
	this.initialModelGuid = initialModelGuid;
    }
    
    public ModellerEvent getClientEvent() {
	return new StartEvent(readOnlySessionGuid, initialReadOnlySessionGuid, initialModelGuid);
    }
    
    public String toXML() {
	StringBuffer xml = new StringBuffer();
	xml.append("<StartEvent version='5'");
	xml.append(additionalXMLAttributes());
	if (initialReadOnlySessionGuid != null) {
	    xml.append("initialReadOnlySessionID='" + initialReadOnlySessionGuid + "' ");
	}
	if (readOnlySessionGuid != null) {
	    xml.append("readOnlySessionID='" + readOnlySessionGuid + "' ");
	}
	xml.append("/>");
	return xml.toString();
    }
    
    @Override
    public List<String> additionalXML() {
	if (initialModelGuid == null) {
	    return null;
	}
	StringBuffer loadModelXML = new StringBuffer();
	loadModelXML.append("<LoadModelEvent version='5'");
	loadModelXML.append(" modelID='" + initialModelGuid + "'");
	UsageStatistics statistics = ModelXML.getStatistics(initialModelGuid);
	loadModelXML.append(" loads='" + statistics.loadCount + "'");
	loadModelXML.append(" runs='" + statistics.runCount + "'");
	loadModelXML.append(" replaceOldModel='1'");
	loadModelXML.append(additionalXMLAttributes());
	loadModelXML.append(" />");
	ArrayList<String> result = new ArrayList<String>();
	result.add(loadModelXML.toString());
	return result;
    }

    public String getReadOnlySessionGuid() {
        return readOnlySessionGuid;
    }

    public String getInitialReadOnlySessionGuid() {
        return initialReadOnlySessionGuid;
    }

    public String getInitialModelGuid() {
        return initialModelGuid;
    }

}
