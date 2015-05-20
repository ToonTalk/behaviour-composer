/**
 * 
 */
package uk.ac.lkl.server;

/**
 * 
 * Defines an interface for reporting errors and warnings.
 * And fetching them back.
 * 
 * @author Ken Kahn
 *
 */
public interface ServerErrorRecorder {
    
    public void warn(String message);
    
    public void logException(Exception e, String message);

    public String getAndRecordWarningsToSendBackToClient();

}
