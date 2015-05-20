/**
 * 
 */
package uk.ac.lkl.server.persistent;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

/**
 * Represents lists of URLs that are copies of a micro-behaviour
 * 
 * This is obsolete but kept so that models prior to 20 March 2011 still work
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable

public class MicroBehaviourCopyMicroBehaviours {

    @PrimaryKey
    private String guid;
    @Persistent
    private Text name;
    @Persistent
    private String[] urls;
    
    public MicroBehaviourCopyMicroBehaviours(
	    String guid, 
	    String name,
	    String urls) {
	super();
	this.guid = guid;
	this.name = new Text(name);
	this.urls = urls.split(" ");
    }

    public String getGuid() {
        return guid;
    }

    public String getName() {
        return name.getValue();
    }

    public String[] getUrls() {
        return urls;
    }

}
