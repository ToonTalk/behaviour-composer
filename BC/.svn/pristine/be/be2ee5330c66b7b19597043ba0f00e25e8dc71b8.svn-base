/**
 * 
 */
package uk.ac.lkl.client;

import java.util.Date;
import java.util.logging.Level;

import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * Code to deal with network failures
 * 
 * @author Ken Kahn
 *
 */
public class NetworkFailure {
    
    private static final int INITIAL_RETRYING_DELAY = 1000;
    private TimerInSequence outstandingNetworkFailureTimer = null;
    private int delayBeforeRetryingNetwork = INITIAL_RETRYING_DELAY;
    private static NetworkFailure instance;
    
    public NetworkFailure() {
	super();
    }

    public void networkFailure(Throwable caught, String message, TimerInSequence timer) {
	// no longer waiting -- restore default cursor
	Modeller.instance().restoreCursor();
	if (timer != null && caught instanceof StatusCodeException && ((StatusCodeException) caught).getStatusCode() != 500) {
	    HorizontalPanel horizontalPanel = new HorizontalPanel();
	    horizontalPanel.setSpacing(6);
	    final int delay = delayBeforeRetryingNetwork/1000;
	    final HTML alert = new HTML(networkFailureMessage(delay));
	    alert.setStylePrimaryName("modeller-special-alert");
	    horizontalPanel.add(alert);
	    if (delay > 1) {
		final Anchor anchor = new Anchor(Modeller.constants.tryNow());
		final Timer updateMessageTimer = new Timer() {

		    private int delayRemaining = delay-1;

		    @Override
		    public void run() {
			alert.setHTML(networkFailureMessage(delayRemaining));
			if (delayRemaining == 0) {
			    anchor.setVisible(false);
			    cancel();
			}
			delayRemaining--;
		    }

		};
		updateMessageTimer.scheduleRepeating(1000);
		anchor.addClickHandler(new ClickHandler() {

		    public void onClick(ClickEvent event) {
			updateMessageTimer.cancel();
			if (outstandingNetworkFailureTimer != null) {
			    alert.setHTML(Modeller.constants.tryingNow());
			    anchor.setVisible(false);
			    //				delayBeforeRetryingNetwork = INITIAL_RETRYING_DELAY;
			    runNetworkCommunicationTimers();
			    //				outstandingNetworkFailureTimer.run();
			}
		    }
		});
		horizontalPanel.add(anchor);
	    }
	    Modeller.instance().setSpecialAlert(horizontalPanel);
	    boolean firstTimerFailed;
	    outstandingNetworkFailureTimer = firstNotYetRun(outstandingNetworkFailureTimer);
	    if (outstandingNetworkFailureTimer == null) {
		outstandingNetworkFailureTimer = timer;
//		 Window.alert("timer#" + outstandingNetworkFailureTimer.getId() + " first to fail");
		firstTimerFailed = true;
	    } else if (outstandingNetworkFailureTimer != timer) {
		outstandingNetworkFailureTimer.setNextTimer(timer);
//		Window.alert("timer#" + outstandingNetworkFailureTimer.getId() + " added queue");
		firstTimerFailed = false;
	    } else {
//		Window.alert("timer#" + outstandingNetworkFailureTimer.getId() + " already first in queue");
		firstTimerFailed = true;
	    }
	    if (firstTimerFailed) {
		outstandingNetworkFailureTimer.schedule(delayBeforeRetryingNetwork);
//		Window.alert("timer#" + outstandingNetworkFailureTimer.getId() + " scheduled " + delayBeforeRetryingNetwork);
	    }
	    delayBeforeRetryingNetwork *= 2;
	    if (delayBeforeRetryingNetwork > 3600000) {
		// more than an hour
		delayBeforeRetryingNetwork = 3600000;
	    }
	} else {
	    Modeller.clearAlertsLine();
	    Modeller.addAlert(CommonUtils.stronglyHighlight("Server reported an error: " + caught.getMessage() + ". " + message));
	}
    }

    public String networkFailureMessage(int seconds) {
	if (seconds == 0) {
	    return Modeller.constants.tryingNow();
	} else {
	    return Modeller.constants.networkCommunicationProblems().replace("***seconds***",  Integer.toString(seconds));
	}
    }
    
    public void networkOK(TimerInSequence currentTimer) {
//        setWaitingForNetworkResponse(false);
	if (currentTimer != null) {
	    currentTimer.setSucceeded(true);
	}
	if (isOutstandingNetworkFailures()) {
//	    if (currentTimer != null) {
//		Window.alert("timer#" + currentTimer.getId() + " ok");
//	    }
	    runNetworkCommunicationTimers();
	    Modeller.instance().setSpecialAlert(null);
	    if (!isOutstandingNetworkFailures()) {
		Utils.logServerMessage(Level.WARNING, "Network connection restored at " + new Date().toString());
	    }
	}
    }

    public void runNetworkCommunicationTimers() {
	outstandingNetworkFailureTimer = firstNotYetRun(outstandingNetworkFailureTimer);
	if (outstandingNetworkFailureTimer != null) {
//	    Window.alert("timer#" + outstandingNetworkFailureTimer.getId() + " run as outstandingNetworkFailureTimer");
	    TimerInSequence firstTimer = outstandingNetworkFailureTimer;
	    outstandingNetworkFailureTimer = outstandingNetworkFailureTimer.getNextTimer();
	    firstTimer.run();
	    firstTimer.cancel();
//	    if (outstandingNetworkFailureTimer == null) {
//		Window.alert("No more outstandingNetworkFailureTimer");
//	    }
	}
	delayBeforeRetryingNetwork = INITIAL_RETRYING_DELAY;
    }
    
    public static TimerInSequence firstNotYetRun(TimerInSequence timer) {
	if (timer == null) {
	    return null;
	} else if (timer.isSucceeded()) {
	    return firstNotYetRun(timer.getNextTimer());
	} else {
	    return timer;
	}
    }

    public static NetworkFailure instance() {
	if (instance == null) {
	    instance = new NetworkFailure();
	}
        return instance;
    }

    public boolean isOutstandingNetworkFailures() {
        return outstandingNetworkFailureTimer != null;
    }
    
    public void addToNetworkFailures(TimerInSequence timer) {
	if (outstandingNetworkFailureTimer != null) {
	    if (!outstandingNetworkFailureTimer.setNextTimer(timer)) {
		Window.alert(Modeller.constants.TooManyNetworkFailureRecoveryActionsScheduled());
	    }
	} else {
	    outstandingNetworkFailureTimer = timer;
//	    Window.alert("timer#" + outstandingNetworkFailureTimer.getId() + " addToNetworkFailures");
	}
    }

}
