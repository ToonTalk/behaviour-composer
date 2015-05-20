/**
 * 
 */
package uk.ac.lkl.server.persistent;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import uk.ac.lkl.server.ClientState;
import uk.ac.lkl.server.MacroBehaviour;
import uk.ac.lkl.server.ResourcePageServiceImpl;
import uk.ac.lkl.shared.CommonUtils;

import com.google.appengine.api.datastore.Key;

/**
 * 
 * Represents macro-behaviours in JDO database
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class MacroBehaviourData {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)  
    private Key key;
    @Persistent
    private String name;
    @Persistent
    private List<String> microBehaviourURLs;
    @NotPersistent 
    private List<MicroBehaviourData> orginalMicroBehaviours;
    
    public MacroBehaviourData(String name, List<MicroBehaviourData> microBehaviours) {
	super();
	this.name = name;
	setMicroBehaviours(microBehaviours);
	this.orginalMicroBehaviours = microBehaviours;
    }

    public MacroBehaviourData copy() {
	ArrayList<MicroBehaviourData> microBehavioursCopy = new ArrayList<MicroBehaviourData>();
	if (orginalMicroBehaviours != null) {
	    for (MicroBehaviourData data : orginalMicroBehaviours) {
		microBehavioursCopy.add(data.copy());
	    }
	}
	return new MacroBehaviourData(name, microBehavioursCopy );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMicroBehaviours(List<MicroBehaviourData> microBehaviourDataList) {
        if (microBehaviourDataList == null) {
	    this.microBehaviourURLs = null;
	} else {
	    ArrayList<String> microBehaviourIds = new ArrayList<String>();
	    for (MicroBehaviourData data : microBehaviourDataList) {
		microBehaviourIds.add(data.getUrl());
	    }
	    this.microBehaviourURLs = microBehaviourIds;
	}
    }

    public MacroBehaviour getMacroBehaviour(ResourcePageServiceImpl resourcePageServiceImpl) {
	MacroBehaviour macroBehaviour = 
	    new MacroBehaviour(getName(), resourcePageServiceImpl.getNetLogoModel(), resourcePageServiceImpl);
	if (microBehaviourURLs != null) {
	    for (String url : microBehaviourURLs) {
		String withoutMinusSign = CommonUtils.withoutMinusSign(url);
		boolean active = (url == withoutMinusSign);
		url = withoutMinusSign;
		macroBehaviour.add(
			resourcePageServiceImpl.getMicroBehaviourFromCacheOrDatabase(url, new ClientState()), active);
	    }
	}
	return macroBehaviour;
    }

    public Key getKey() {
        return key;
    }

}
