/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.Timer;

/**
 * Used to make a sequence of timers
 * 
 * @author Ken Kahn
 *
 */
public abstract class TimerInSequence extends Timer {

    abstract public void run();
    
    private TimerInSequence nextTimer = null;
    
    private boolean succeeded = false;
    
    public TimerInSequence() {
	super();
    }

    public TimerInSequence getNextTimer() {
        return nextTimer;
    }
    
    public boolean setNextTimer(TimerInSequence nextTimer) {
	return setNextTimer(nextTimer, 0);
    }

    private boolean setNextTimer(TimerInSequence nextTimer, int count) {
	// put the timer at the end of the list
	if (nextTimer == this.nextTimer) {
	    // already there -- ignore
	    return true;
	} else if (nextTimer == null || this.nextTimer == null) {
	    this.nextTimer = nextTimer;
	    return true;
	} else if (count > 100) {
	    // too many failures
	    return false;
	} else {
	    return this.nextTimer.setNextTimer(nextTimer, count+1);
	}
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

}
