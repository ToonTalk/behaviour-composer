/**
 * 
 */
package uk.ac.lkl.server;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 * Uses Objectify to maintain store the channels in a BC2NetLogo connection
 * Used to re-establish communication after failures
 * 
 * @author Ken Kahn
 *
 */

@Cached
public class BC2NetLogoChannels {
    
    @Id private String sessionGuidAndUserGuid;
    private String channelToNetLogo;
    private String channelFromNetLogo;
    private String reconnectionCounter;
    
    public BC2NetLogoChannels(String sessionGuidAndUserGuid, String channelToNetLogo, String channelFromNetLogo, String reconnectionCounter) {
	this.sessionGuidAndUserGuid = sessionGuidAndUserGuid;
	this.channelToNetLogo = channelToNetLogo;
	this.channelFromNetLogo = channelFromNetLogo;
	this.reconnectionCounter = reconnectionCounter;
    }
   
    public BC2NetLogoChannels() {}  // for Objectify

    public String getChannelToNetLogo() {
        return channelToNetLogo;
    }

    public void setChannelToNetLogo(String channelToNetLogo) {
        this.channelToNetLogo = channelToNetLogo;
    }

    public String getChannelFromNetLogo() {
        return channelFromNetLogo;
    }

    public void setChannelFromNetLogo(String channelFromNetLogo) {
        this.channelFromNetLogo = channelFromNetLogo;
    }

    public String getReconnectionCounter() {
        return reconnectionCounter;
    }

    public void setReconnectionCounter(String reconnectionCounter) {
        this.reconnectionCounter = reconnectionCounter;
    };



}
