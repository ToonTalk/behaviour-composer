/**
 * 
 */
package uk.ac.lkl.server.persistent;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 * Associates a session guid with the BehaviorSpace experiments
 * 
 * @author Ken Kahn
 *
 */

@Cached
public class SessionExperiments {

    @Id
    private String sessionGuid;
    private String experiments;
    
    public SessionExperiments(String sessionGuid, String experiments) {
	this.sessionGuid = sessionGuid;
	this.experiments = experiments;
    }
    
    public SessionExperiments() {
	// for Objectify
    }

    public String getExperiments() {
        return experiments;
    }

    public void setExperiments(String experiments) {
        this.experiments = experiments;
    }

}
