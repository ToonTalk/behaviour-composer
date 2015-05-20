/**
 * 
 */
package uk.ac.lkl.server;

import java.io.IOException;

import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;


/**
 * Enables access to the production data store while in the development server
 * Based upon http://code.google.com/appengine/docs/java/tools/remoteapi.html
 * 
 * @author Ken Kahn
 *
 */
public class RemoteAPI {
    
    private final RemoteApiOptions options;
    private boolean remoteAPIInUse;

    public RemoteAPI()
        throws IOException {
        String username = "ToonTalk@gmail.com";
//	DataStore.begin().put(new GAEPassword("ken", "..."));
	String password = GAEPassword.getPassword("ken");
	if (password == null) {
	    System.err.println("Need to put password in local datastore.");
	    options = null;
	    return;
	}
	// Authenticating with username and password is slow, so we'll do it
        // once during construction and then store the credentials for reuse.
        this.options = new RemoteApiOptions()
            .server("m4a-gae.appspot.com", 443)
            .credentials(username, password);
        RemoteApiInstaller installer = new RemoteApiInstaller();
        installOptions(installer);
        try {
            // Update the options with reusable credentials so we can skip
            // authentication on subsequent calls.
            options.reuseCredentials(username, installer.serializeCredentials());
        } finally {
            installer.uninstall();
        }
    }

    protected void installOptions(RemoteApiInstaller installer) throws IOException {
	try {
	    installer.install(options);
        } catch (java.net.SocketTimeoutException timeOutException) {
            System.err.println("Time out installing remote API options. Retrying... ");
            installOptions(installer);
        }
    }
    
    public <Type extends Object> Type runOnProductionGAE(RunOnProductionGAECallback<Type> callback) {
	if (options == null) {
	    System.err.println("Not connected to remote API.");
	    return null;
	}
	RemoteApiInstaller installer = null;
        try {
            installer = new RemoteApiInstaller();
            installer.install(options);
            remoteAPIInUse = true;
            Type result = callback.execute();
	    return result;
        } catch (java.net.SocketTimeoutException timeOutException) {
            System.err.println("Time out running remote API. Retrying... " + callback);
            return runOnProductionGAE(callback);
        } catch (Exception exception) {
            System.err.println("Exception while using connection to remote API.");
            exception.printStackTrace();
            return null;
        } finally {
            if (remoteAPIInUse) {
        	remoteAPIInUse = false;
        	if (installer != null) {
        	    try {
        		installer.uninstall();
        	    } catch (Exception e) {
        		System.err.println("Error uninstalling remote API.");
        		e.printStackTrace();
        	    }
        	}
            }
        }
    }

    public boolean isRemoteAPIInUse() {
        return remoteAPIInUse;
    }

}
