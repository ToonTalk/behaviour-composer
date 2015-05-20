/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Represents swapping top-level prototypes (in JDO implementation)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerSwapPrototypesEvent extends ServerEvent {

    @Persistent
    private int index1; 
    @Persistent
    private int index2; 

    public ServerSwapPrototypesEvent(int index1,
	                             int index2,
	                             String sessionGuid, 
	                             String userGuid) {
	super(sessionGuid, userGuid);
	this.index1 = index1;
	this.index2 = index2;
    }

    @Override
    public String toXML() {
	StringBuffer xml = new StringBuffer();
	xml.append("<SwapPrototypesEvent version='1'");
	xml.append(additionalXMLAttributes());
	xml.append(" index1='" + index1 + "'");
	xml.append(" index2='" + index2 + "'>");
	xml.append("</SwapPrototypesEvent>");
	return xml.toString();
    }

}
