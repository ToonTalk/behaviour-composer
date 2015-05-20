package uk.ac.lkl.server;

import javax.persistence.Id;

import uk.ac.lkl.server.persistent.DataStore;

import com.googlecode.objectify.annotation.Cached;

/**
 * Only used for the development environment to fall back on the production datastore
 * 
 * @author Ken Kahn
 *
 */

@Cached
public class GAEPassword {
    
    @Id String key;
    
    String password;
    
    public GAEPassword() { }; // for objectify
    
    public GAEPassword(String key, String password) {
	this.key = key;
	this.password = password;
    }
    
    public static String getPassword(String key) {
	GAEPassword passwordRecord = DataStore.begin().find(GAEPassword.class, key);
	return (passwordRecord==null ? null : passwordRecord.password);
    }

}
