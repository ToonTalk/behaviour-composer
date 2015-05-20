/**
 * 
 */
package uk.ac.lkl.server.persistent;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 * Associates a session guid with the most recent differences
 * created in B2CNetLogo to be processed by the Behaviour Composer
 * 
 * @author Ken Kahn
 *
 */

@Cached
public class ModelDifferences {

    @Id
    private String sessionGuid;
    private String procedureDifferences;
    private String declarationDifferences;
    private String widgetDifferences;
    private String infoTab;
    
    public ModelDifferences(String sessionGuid, String procedureDifferences, String declarationDifferences, String widgetDifferences, String infoTab) {
	this.sessionGuid = sessionGuid;
	this.procedureDifferences = procedureDifferences;
	this.declarationDifferences = declarationDifferences;
	this.widgetDifferences = widgetDifferences;
	this.infoTab = infoTab;
    }
    
    public ModelDifferences() {
	// for Objectify
    }

    public String getProcedureDifferences() {
        return procedureDifferences;
    }

    public String getWidgetDifferences() {
        return widgetDifferences;
    }

    public String getDeclarationDifferences() {
        return declarationDifferences;
    }

    public String getInfoTab() {
        return infoTab;
    }

}
