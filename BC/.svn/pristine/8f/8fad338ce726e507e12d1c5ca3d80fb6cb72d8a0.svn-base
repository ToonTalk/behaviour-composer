package uk.ac.lkl.server;

public class ServerErrorRecorderImpl implements ServerErrorRecorder {
    
    protected String warningsToSendBackToClient = "";
    
    public ServerErrorRecorderImpl() {
    }
    
    public void warn(String message) {
	ServerUtils.logError("Warning:\n " + message + "\n");
	warningsToSendBackToClient += message + "<br>";
    }
    
    public void logException(Exception e, String message) {
	e.printStackTrace(System.err);
	warn("Error " + message + " " + e.toString());
    }

    public String getAndRecordWarningsToSendBackToClient() {
	// doesn't record warnings in database
        return warningsToSendBackToClient;
    }
}
