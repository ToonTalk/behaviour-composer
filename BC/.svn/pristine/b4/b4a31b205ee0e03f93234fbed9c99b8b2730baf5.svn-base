/**
 * 
 */
package uk.ac.lkl.server.persistent;

import java.util.ArrayList;
import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Indexed;

/**
 * 
 * Associates a cryptographic hash of the model XML and model GUIDs
 * 
 * Used to share identical XML
 * 
 * @author Ken Kahn
 *
 */

@Cached
public class ModelXMLStringHash {
    
    @Id String hash;
    @Indexed private ArrayList<String> modelGuids;
    // first guid - the one with a ModelXML record
    private String originalModelGuid;

    public ModelXMLStringHash(String hash, String modelGuid) {
	this.hash = hash;
	this.modelGuids = new ArrayList<String>();
	this.modelGuids.add(modelGuid);
	this.originalModelGuid = modelGuid;
    }
    
    public ModelXMLStringHash() {}; // for Objectify

    public boolean knownModelGuid(String modelGuid) {
        return modelGuids.contains(modelGuid);
    }
    
    public void addModelGuid(String modelGuid) {
	if (!modelGuids.contains(modelGuid)) {
	    modelGuids.add(modelGuid);
	    DataStore.begin().put(this);
	}
    }

    public String getOriginalModelGuid() {
        return originalModelGuid;
    }

}
