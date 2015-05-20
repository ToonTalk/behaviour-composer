/**
 * 
 */
package uk.ac.lkl.server.persistent;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Indexed;

/**
 * Associates urls with NetLogo procedure names
 * 
 * By ensuring that for any given url the same name is generated
 * we can find the url from the procedure name
 * 
 * @author Ken Kahn
 *
 */
@Cached
public class MicroBehaviourNetLogoName {

    @Id
    private String url;
    @Indexed
    private String netNogoName;
    
    public MicroBehaviourNetLogoName(String url, String name) {
	this.url = url;
	this.netNogoName = name;
    }
    
    public MicroBehaviourNetLogoName() {
	// for Objectify
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return netNogoName;
    }

}
