/**
 * 
 */
package uk.ac.lkl.server.persistent;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

/**
 * Stores the generated NetLogo code using JDO
 * 
 * If deleted it is recomputed 
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ModelNetLogo {
    
    @PrimaryKey
    private String uniqueID;
    @Persistent
    private Text netLogoFileContents;
    @Persistent
    private Date timeStamp;


    public ModelNetLogo(String uniqueID, String netLogoFileContents) {
	this.uniqueID = uniqueID;
	this.netLogoFileContents = new Text(netLogoFileContents);
	timeStamp = new Date();
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public String getNetLogoFileContents() {
        return netLogoFileContents.getValue();
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

}
