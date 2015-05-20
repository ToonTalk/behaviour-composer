/**
 * 
 */
package uk.ac.lkl.server;

/**
 * @author Ken Kahn
 *
 */
public interface RunOnProductionGAECallback<Type> {
    
    public Type execute();

}
