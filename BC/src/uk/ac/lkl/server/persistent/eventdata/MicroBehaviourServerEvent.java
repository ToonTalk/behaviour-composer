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
 * @author Ken Kahn
 *
 */

@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class MicroBehaviourServerEvent extends MacroBehaviourServerEvent {
    
    @Persistent
    private String microBehaviourName;
    @Persistent
    private Text microBehaviourNameText;
    
    public MicroBehaviourServerEvent(String microBehaviourName, String macroBehaviourNameAtEventTime, String sessionGuid, String userGuid) {
	super(macroBehaviourNameAtEventTime, sessionGuid, userGuid);
	microBehaviourNameText = new Text(microBehaviourName);
    }

    public String getMicroBehaviourName() {
        if (microBehaviourNameText != null) {
            return microBehaviourNameText.getValue();
        } else {
            return microBehaviourName;
        }
    }
    

}
