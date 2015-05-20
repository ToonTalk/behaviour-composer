/**
 * 
 */
package uk.ac.lkl.server;

/**
 * 
 * Exceptions thrown due to problems with the NetLogo code
 * 
 * @author Ken Kahn
 *
 */
public class NetLogoException extends Exception {
    
    private static final long serialVersionUID = 5955114785553754791L;

    public NetLogoException(String message) {
	super(message);
    }

}
