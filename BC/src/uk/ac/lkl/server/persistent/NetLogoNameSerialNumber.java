/**
 * 
 */
package uk.ac.lkl.server.persistent;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 * Maintains the largest serial number assigned to
 * a NetLogo procedure name
 * 
 * @author Ken Kahn
 *
 */
@Cached
public class NetLogoNameSerialNumber {

    @Id
    private String name;
    
    private int serialNumber;
    
    public NetLogoNameSerialNumber(String name, int serialNumber) {
	this.name = name;
	this.serialNumber = serialNumber;
    }
    
    public NetLogoNameSerialNumber() {
	// for Objectify
    }

    public String getName() {
        return name;
    }

    public int getSerialNumber() {
        return serialNumber;
    }
    
    public void incrementSerialNumber() {
	this.serialNumber++;
    }

}
