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
 * Associates a cryptographic hash of the contents of the micro-behaviour and the URL
 * 
 * Used to share identical micro-behaviour records
 * 
 * @author Ken Kahn
 *
 */

@Cached
public class MicroBehaviourDataHash {

    @Id String hash;
    // TODO: determine why the following is indexed (and either comment out or remove)
    @Indexed private ArrayList<String> urls;
    private String originalURL;

    public MicroBehaviourDataHash(String hash, String url) {
	this.hash = hash;
	this.urls = new ArrayList<String>();
	this.urls.add(url);
	this.originalURL = url;
    }
    
    public MicroBehaviourDataHash() {}; // for Objectify
    
    public boolean knownURL(String modelGuid) {
        return urls.contains(modelGuid);
    }
    
    public void addURL(String modelGuid) {
	if (!urls.contains(modelGuid)) {
	    urls.add(modelGuid);
	    DataStore.begin().put(this);
	}
    }

    public String getOriginalURL() {
        return originalURL;
    }

}
