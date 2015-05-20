/**
 * 
 */
package uk.ac.lkl.server;

import javax.servlet.http.HttpServletRequest;

import uk.ac.lkl.server.persistent.DataStore;

/**
 * @author Ken Kahn
 * 
 * Maintains state specific to each client to the servlets
 *
 */
public class ClientState implements ServerErrorRecorder {
    protected String warningsToSendBackToClient = "";
    protected HttpServletRequest httpServletRequest;
    protected String sessionGuid = null;
    protected String userGuid = null;
    protected boolean cachingEnabled;
    protected boolean internetAccess;
    
    public ClientState(String sessionGuid, HttpServletRequest httpServletRequest, String userGuid, boolean cachingEnabled, boolean internetAccess) {
	this.sessionGuid = sessionGuid;
	this.httpServletRequest = httpServletRequest;
	this.userGuid = userGuid;
	this.cachingEnabled = cachingEnabled;
	this.internetAccess = internetAccess;
	// just in case
	DataStore.ensureClassesRegisteredWithObjectify();
    }

    public ClientState(String sessionGuid, HttpServletRequest httpServletRequest, boolean cachingEnabled, boolean internetAccess) {
	this(sessionGuid, httpServletRequest, null, cachingEnabled, internetAccess);
    }
    
    public ClientState() {
	// used by History Service to talk to Resource Service
	this(null, null, null, false, true);
    }

    public void warn(String message) {
	ServerUtils.logError("Warning:\n " + message + "\n");
	warningsToSendBackToClient += "Warning. " + message + "<br>";
    }
    
    public void logException(Exception e, String message) {
	e.printStackTrace();
	warn("Error " + message + " " + e.toString() + " " + e.getMessage());
    }

    public String getAndRecordWarningsToSendBackToClient() {
	if (!warningsToSendBackToClient.isEmpty()) {
	    ServerUtils.logError(warningsToSendBackToClient, sessionGuid, userGuid);
	}
        return warningsToSendBackToClient;
    }

    public String getAgentDescription() {
	if (httpServletRequest == null) {
	    return "user-agent unknown";
	}
        return httpServletRequest.getHeader("user-agent");
    }
    
    public String getClientInfo() {
	if (httpServletRequest == null) {
	    return "client info unknown";
	}
	return ServerUtils.getClientInfo(httpServletRequest);
    }

    public String getSessionGuid() {
        return sessionGuid;
    }
    
    public String getUserGuid() {
	return userGuid;
    }

    public String getWarningsToSendBackToClient() {
        return warningsToSendBackToClient;
    }

    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    public boolean isInternetAccess() {
        return internetAccess;
    }

    public void setInternetAccess(boolean internetAccess) {
        this.internetAccess = internetAccess;
    }
    
}
