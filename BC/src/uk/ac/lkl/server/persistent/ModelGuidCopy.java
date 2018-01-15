/**
 * 
 */
package uk.ac.lkl.server.persistent;

//import javax.persistence.Id;
import com.googlecode.objectify.annotation.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 * Stores a model GUID that is an identical copy of an older guid
 * 
 * @author Ken Kahn
 *
 */

@Cached
public class ModelGuidCopy {
    
    @Id String modelGuid;
    private String originalModelGuid;

    public ModelGuidCopy(String modelGuid, String originalModelGuid) {
	this.modelGuid = modelGuid;
	this.originalModelGuid = originalModelGuid;
    }
    
    public ModelGuidCopy() {}; // for Objectify

    public String getOriginalModelGuid() {
        return originalModelGuid;
    }

}
