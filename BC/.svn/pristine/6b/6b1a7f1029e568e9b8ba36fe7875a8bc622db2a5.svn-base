/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

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
public class ServerUpdateNumberOfInstancesTextAreaEvent extends MacroBehaviourServerEvent {
    
    @Persistent
    private Text oldExpressionText;
    @Persistent
    private Text newExpressionText;
    // for backwards compatibility
    @Persistent
    private String oldValue;
    @Persistent
    private String newValue;

    public ServerUpdateNumberOfInstancesTextAreaEvent(
	    String oldExpression,
	    String newExpression,
	    String macroBehaviourNameAtEventTime, 
	    String sessionGuid,
	    String userGuid) {
	super(macroBehaviourNameAtEventTime, sessionGuid, userGuid);
	this.oldExpressionText = new Text(oldExpression);
	this.newExpressionText = new Text(newExpression);
    }

    @Override
    public String toXML() {
	return "<UpdateNumberOfInstancesTextAreaEvent version='5'" + 
	       additionalXMLAttributes() + ">" +
	       "<oldExpression>" + CommonUtils.createCDATASection(getOldExpression()) + "</oldExpression>" +
	       "<newExpression>" + CommonUtils.createCDATASection(getNewExpression()) + "</newExpression>" +
	       "<name>" + CommonUtils.createCDATASection(getMacroBehaviourNameAtEventTime()) + "</name>" +
	       "</UpdateNumberOfInstancesTextAreaEvent>";
    }

    private String getOldExpression() {
	if (oldExpressionText != null) {
	    return oldExpressionText.getValue();
	} else {
	    return oldValue;
	}
    }
    
    private String getNewExpression() {
	if (newExpressionText != null) {
	    return newExpressionText.getValue();
	} else {
	    return newValue;
	}
    }

}
