/**
 * 
 */
package uk.ac.lkl.server.persistent;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import uk.ac.lkl.server.JDO;

/**
 * Maintains counters in the JDO store
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class ServerCounter {
    @PrimaryKey
    private String name;
    @Persistent
    private Long count;
    
    public ServerCounter(String name, Long count) {
	this.name = name;
	this.count = count;
    }
    
    public Long getCount() {
	return count;
    }
    
    private Long incrementCount() {
	count++;
	return count;
    }
    
    public static Long nextCount(String name, Long initialCount) {
	PersistenceManager persistenceManager = JDO.getPersistenceManager();
	try {
	    ServerCounter counter = persistenceManager.getObjectById(ServerCounter.class, name);
	    return counter.incrementCount();
	} catch (JDOObjectNotFoundException e) {
	    ServerCounter counter = new ServerCounter(name, initialCount);
	    persistenceManager.makePersistent(counter);
	    return initialCount;
	} finally {
	    persistenceManager.close();
	}
    }
    
}
