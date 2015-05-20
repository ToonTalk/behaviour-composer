package uk.ac.lkl.client;

public class RecordSubsequentEventCallback extends AsyncCallbackNetworkFailureCapable<String[]> {

    private TimerInSequence nextTimer = null;

    public RecordSubsequentEventCallback() {
	super();
    }
    
    @Override
    public void onFailure(Throwable caught) {
	NetworkFailure.instance().networkFailure(caught, Modeller.constants.recordingHistory(), timer);
    }

    @Override
    public void onSuccess(String[] result) {
	super.onSuccess(result);
	String errorMessage = result[0];
	if (errorMessage != null) {
	    Modeller.addToErrorLog(errorMessage);
	    return;
	} else {
	    onSuccess();
	}
	String updateTimeAsString = result[1];
	try {
	    Modeller.setTimeOfLastUpdate(Long.parseLong(updateTimeAsString));
	} catch (Exception e) {
	    System.err.println("Expected a long integer as update time rather than: " + updateTimeAsString);
	    e.printStackTrace();
	}
	String userIDAsString = result[2];
	if (userIDAsString != null) {
	    try {
		int userID = Integer.parseInt(userIDAsString);
		if (userID >= 0) {
		    Modeller.userID = userID;
		}
	    } catch (Exception e) {
		System.err.println("Expected an integer as user ID rather than: " + userIDAsString);
		e.printStackTrace();
	    }
	}
	if (nextTimer != null) {
	    nextTimer = nextTimer.getNextTimer();
	    if (nextTimer != null) {
		nextTimer.run();
	    }
	}
    }
    
    @Override
    public void setAndRunTimer(TimerInSequence nextTimer) {
	if (this.nextTimer == null) {
	    // nobody waiting to send so record this and run it
	    this.nextTimer = nextTimer;
	    super.setAndRunTimer(nextTimer);
	} else {
	    // store this to run when this.nextTimer completes
	    this.nextTimer.setNextTimer(nextTimer);
	}
    }

    protected void onSuccess() {
	// overridden by subclasses
    }
}
