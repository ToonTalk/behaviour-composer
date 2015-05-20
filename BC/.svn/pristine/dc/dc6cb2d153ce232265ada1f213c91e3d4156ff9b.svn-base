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
public class HTMLModelTemplate {
    
    @PrimaryKey
    private String modelGuid;
    @Persistent
    private Text template;
    
    public HTMLModelTemplate(String modelGuid, String template) {
	this.modelGuid = modelGuid;
	this.template = new Text(template);
    }

    public String getModelGuid() {
        return modelGuid;
    }

    public String getTemplate() {
        return template.getValue();
    }

}
