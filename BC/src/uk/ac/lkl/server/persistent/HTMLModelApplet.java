/**
 * 
 */
package uk.ac.lkl.server.persistent;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

/**
 * Stores the HTML template that the model's applet uses
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class HTMLModelApplet {
    
    @PrimaryKey
    private String modelGuid;
    @Persistent
    private Text appletString;
    
    public HTMLModelApplet(String modelGuid, String appletString) {
	this.modelGuid = modelGuid;
	this.appletString = new Text(appletString);
    }

    public String getModelGuid() {
        return modelGuid;
    }

    public String getAppletString() {
        return "<html>" + appletString.getValue() + "</html>";
    }



}
