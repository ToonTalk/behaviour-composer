/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

/**
 * An abstract class to represent events that refer to the macro behaviour (by name at event time)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class MacroBehaviourServerEvent extends ServerEvent {

    @Persistent
    private String macroBehaviourNameAtEventTime;
    @Persistent
    private Text macroBehaviourNameAtEventTimeText;

    public MacroBehaviourServerEvent(String macroBehaviourNameAtEventTime, String sessionGuid, String userGuid) {
	super(sessionGuid, userGuid);
	this.macroBehaviourNameAtEventTimeText = new Text(macroBehaviourNameAtEventTime);
    }

    public String getMacroBehaviourNameAtEventTime() {
        if (macroBehaviourNameAtEventTimeText != null) {
            return macroBehaviourNameAtEventTimeText.getValue();
        } else {
            return macroBehaviourNameAtEventTime;
        }
    }

}
