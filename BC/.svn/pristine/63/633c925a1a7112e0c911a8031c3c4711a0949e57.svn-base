/**
 * 
 */
package uk.ac.lkl.server;

import javax.persistence.Id;

import uk.ac.lkl.server.persistent.DataStore;

import com.googlecode.objectify.annotation.Cached;

/**
 * Uses Objectify to maintain a list of open channels associated with a BC session
 * 
 * @author Ken Kahn
 *
 */

@Cached
public class OpenSessionChannels {
    
    @Id private String sessionGuid;
    private String clientIds;
    
    public OpenSessionChannels(String sessionGuid, String clientIds) {
	this.sessionGuid = sessionGuid;
	this.clientIds = clientIds;
    }
   
    public OpenSessionChannels() {}; // for Objectify

    public String getClientIds() {
        return clientIds;
    }

    public void setClientIds(String clientIds) {
        this.clientIds = clientIds;
    }

    public void addChannel(String channelId) {
	if (!clientIds.contains(channelId)) {
	    clientIds += ";" + channelId;
	}
    }

    public String getSessionGuid() {
        return sessionGuid;
    }
    
    public static String[] getClientIds(String sessionGuid) {
	OpenSessionChannels openChannels = DataStore.begin().find(OpenSessionChannels.class, sessionGuid);
	if (openChannels == null) {
	    return null;
	} else {
	    return openChannels.getClientIds().split(";");
	}
    }

}
