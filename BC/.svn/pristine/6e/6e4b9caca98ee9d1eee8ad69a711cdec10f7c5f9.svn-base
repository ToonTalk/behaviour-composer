/**
 * 
 */
package uk.ac.lkl.server.persistent.eventdata;

import java.util.ArrayList;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import uk.ac.lkl.server.persistent.ModelXML;
import uk.ac.lkl.server.persistent.UsageStatistics;

/**
 * Represents the loading a model (using JDO) 
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerLoadModelEvent extends ServerEvent {

    @Persistent 
    private String modelGuid;
    @Persistent
    private boolean replaceOldModel;
    @Persistent
    private String macroBehavioursSelected;

    public ServerLoadModelEvent(
	    String modelGuid, 
	    boolean replaceOldModel,
	    ArrayList<Boolean> macroBehavioursSelected,
	    String sessionGuid, 
	    String userGuid) {
	super(sessionGuid, userGuid);
	this.modelGuid = modelGuid;
	this.replaceOldModel = replaceOldModel;
	this.macroBehavioursSelected = stringifyMacroBehavioursSelected(macroBehavioursSelected);
    }

    @Override
    public String toXML() {
	UsageStatistics statistics = ModelXML.getStatistics(modelGuid);
	return 	"<LoadModelEvent version='5'" + 
	        additionalXMLAttributes() +
	        " replaceOldModel='" + (replaceOldModel ? "1" : "0") + "'" +
	        " macroBehavioursSelected='" + macroBehavioursSelected + "'" +
	        " loads='" + statistics.loadCount + "'" +
	        " runs='" + statistics.runCount + "'" +
	        " modelID='" + modelGuid + "'/>";
    }

    private String stringifyMacroBehavioursSelected(ArrayList<Boolean> macroBehavioursSelected) {
	if (macroBehavioursSelected == null || macroBehavioursSelected.isEmpty()) {
	    return "null";
	} else {
	    String result = "";
	    for (Boolean selected : macroBehavioursSelected) {
		result += selected ? "1" : "0";
	    }
	    return result;
	}
    }

    public String getModelGuid() {
        return modelGuid;
    }

    public boolean isReplaceOldModel() {
        return replaceOldModel;
    }

}
