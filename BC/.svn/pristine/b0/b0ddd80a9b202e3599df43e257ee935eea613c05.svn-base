/**
 * 
 */
package uk.ac.lkl.server.persistent;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * For associating serial numbers with model GUIDs for the Epidemic Game Maker
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class EGMSerialNumber {
    @PrimaryKey
//    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)  
    private Long serialNumber;
    @Persistent
    private String guid;
    
    public EGMSerialNumber(String guid) {
	serialNumber = ServerCounter.nextCount("EGMSerialNumber", 1500L);
	this.guid = guid;
    }
    
    public long getSerialNumber() {
        return serialNumber;
    }

    public String getGuid() {
        return guid;
    }


}
