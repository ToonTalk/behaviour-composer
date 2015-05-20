/**
 * 
 */
package uk.ac.lkl.server.persistent;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 * Links a micro-behaviour URL with the original copy that holds the data
 * 
 * @author Ken Kahn
 *
 */

@Cached
public class MicroBehaviourURLCopy {

    @Id String url;
    private String originalURL;

    public MicroBehaviourURLCopy(String url, String originalURL) {
	this.url = url;
	this.originalURL = originalURL;
    }
    
    public MicroBehaviourURLCopy() {}; // for Objectify

    public String getOriginalURL() {
        return originalURL;
    }

}
