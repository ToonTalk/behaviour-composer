/**
 * 
 */
package uk.ac.lkl.server.persistent;

import java.util.Date;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import uk.ac.lkl.server.JDO;
import uk.ac.lkl.server.ServerUtils;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.NotFoundException;

/**
 * Stores the XML representation of a model (JDO implementation)
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ModelXML {
    
    @PrimaryKey
    private String modelGuid;
    @Persistent
    private Text modelXML; 
    @Persistent
    private String sessionGuid;
    @Persistent
    private Date timeStamp;
    // the pageTemplate and userGuid are needed to recreate applet 
    @Persistent
    private String pageTemplate;
    @Persistent // was missing prior to 6 August 2013
    private String userGuid;
    @Persistent
    private Integer accessCount;
    @Persistent
    private Integer runCount;

    private ModelXML(String modelXML, String sessionGuid, String modelGuid, String userGuid, String pageTemplate) {
	this.modelXML = new Text(modelXML);
	this.sessionGuid = sessionGuid;
	this.modelGuid = modelGuid;
	this.userGuid = userGuid;
	this.pageTemplate = pageTemplate;
	timeStamp = new Date();
	accessCount = 0;
	runCount = 1;
    }
    
    public static boolean persistModelXML(String modelXMLString, String sessionGuid, String modelGuid, String userGuid, String pageTemplate) {	
	String hash = ServerUtils.hashSHA256(modelXMLString);
	if (hash == null) {
	    ServerUtils.persist(new ModelXML(modelXMLString, sessionGuid, modelGuid, userGuid, pageTemplate));
	    return false;
	}
	ModelXMLStringHash modelXMLStringHash = DataStore.begin().find(ModelXMLStringHash.class, hash);
	if (modelXMLStringHash == null) {
	    modelXMLStringHash = new ModelXMLStringHash(hash, modelGuid);
	    DataStore.begin().put(modelXMLStringHash);
	    ServerUtils.persist(new ModelXML(modelXMLString, sessionGuid, modelGuid, userGuid, pageTemplate));
	} else if (!modelXMLStringHash.knownModelGuid(modelGuid)) {
	    modelXMLStringHash.addModelGuid(modelGuid);
	    String originalModelGuid = modelXMLStringHash.getOriginalModelGuid();
	    ModelGuidCopy modelGuidCopy = new ModelGuidCopy(modelGuid, originalModelGuid);
	    ModelXML modelXML = getModelXML(originalModelGuid, false);
	    if (modelXML == null) {
		ServerUtils.persist(new ModelXML(modelXMLString, sessionGuid, originalModelGuid, userGuid, pageTemplate));
	    } else {
		// yet another copy of the original so increase the counter for how many times it was run
		PersistenceManager persistenceManager = JDO.getPersistenceManager();
		modelXML.incrementRunCount();
		persistenceManager.makePersistent(modelXML);
	    }
	    DataStore.begin().put(modelGuidCopy);
	}
	return true;
    }
    
    public static ModelXML getModelXML(String modelGuid, boolean updateAccessCount) {
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	try {
	    return getModelXML(modelGuid, updateAccessCount, persistenceManager);
	} finally {
	    persistenceManager.close();
	}
    }
    
    public static ModelXML getModelXML(String modelGuid, boolean updateAccessCount, PersistenceManager persistenceManager) throws NotFoundException {
	ModelXML modelXML = null;
	try {
	    modelXML = persistenceManager.getObjectById(ModelXML.class, modelGuid);
	} catch (JDOObjectNotFoundException e) {
	    ModelGuidCopy modelGuidCopy = DataStore.begin().find(ModelGuidCopy.class, modelGuid);
	    if (modelGuidCopy == null) {
		return null;
	    } else {
		try {
		    modelXML = persistenceManager.getObjectById(ModelXML.class, modelGuidCopy.getOriginalModelGuid());
		} catch (JDOObjectNotFoundException e2) {
		    return null;
		}
	    }
	}
	if (updateAccessCount && modelXML != null) {
	    modelXML.incrementAccessCount();
	    persistenceManager.makePersistent(modelXML);
	}
	return modelXML;
    }
	
    public static UsageStatistics getStatistics(String modelGuid) {
	if (modelGuid == null || modelGuid.isEmpty()) {
	    // seen in the system logs -- only for very old models
	    return new UsageStatistics(); 
	}
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	ModelXML modelXML = null;
	try {
	    try {
		modelXML = persistenceManager.getObjectById(ModelXML.class, modelGuid);
	    } catch (JDOObjectNotFoundException e) {
		ModelGuidCopy modelGuidCopy = DataStore.begin().find(ModelGuidCopy.class, modelGuid);
		if (modelGuidCopy != null) {
		    try {
			modelXML = persistenceManager.getObjectById(ModelXML.class, modelGuidCopy.getOriginalModelGuid());
		    } catch (JDOObjectNotFoundException e2) {
			return new UsageStatistics();
		    }
		}
	    }
	} finally {
	    persistenceManager.close();
	}
	if (modelXML == null) {
	    return new UsageStatistics();
	} else {
	    return new UsageStatistics(modelXML.getAccessCount(), modelXML.getRunCount());
	}
    }

    public String getModelXML() {
        String xml = modelXML.getValue();
        xml = insertStatistics(xml);
        if (xml.startsWith("<?xml")) {
            return xml;
        } else {
            return "<?xml version='1.0' encoding='UTF-8'?>" + xml;
        }
    }

    private String insertStatistics(String xml) {
	int modelIndexStart = xml.indexOf("<model ");
	if (modelIndexStart < 0) {
	    return xml;
	} else {
	    int modelIndexEnd = modelIndexStart+"<model ".length();
	    return xml.substring(0, modelIndexEnd) + "loads='" + accessCount + "' runs='" + runCount + "' " + xml.substring(modelIndexEnd);
	}
    }

    public String getSessionGuid() {
        return sessionGuid;
    }

    public String getModelGuid() {
        return modelGuid;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }
    
    public String getPageTemplate() {
        return pageTemplate;
    }

    public String getUserGuid() {
        return userGuid;
    }

    protected int getAccessCount() {
	if (accessCount == null) {
	    return 0;
	} else {
	    return accessCount;
	}
    }

    private void incrementAccessCount() {
	if (accessCount == null) {
	    accessCount = 1;
	} else {
	    accessCount++;
	}
    }

    public int getRunCount() {
	if (runCount == null) {
	    return 1;
	} else {
	    return runCount;
	}
    }
    
    private void incrementRunCount() {
	if (runCount == null) {
	    runCount = 1;
	} else {
	    runCount++;
	}
    }

}
