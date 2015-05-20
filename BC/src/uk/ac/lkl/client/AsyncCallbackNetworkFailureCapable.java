package uk.ac.lkl.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsyncCallbackNetworkFailureCapable<T extends Object> implements
	AsyncCallback<T> {

    protected TimerInSequence timer;

    public AsyncCallbackNetworkFailureCapable() {
	super();
    }

    @Override
    public void onFailure(Throwable caught) {
	NetworkFailure.instance().networkFailure(caught, null, timer);
    }

    @Override
    public void onSuccess(T result) {  
	NetworkFailure.instance().networkOK(timer);
    }

    public void setAndRunTimer(TimerInSequence timer) {
        this.timer = timer;
        if (NetworkFailure.instance().isOutstandingNetworkFailures()) {
            // network has failed 
            // so add this to the queue rather than trying to run it now
            NetworkFailure.instance().addToNetworkFailures(timer);
        } else {
            timer.run(); 
        }
    }

    public TimerInSequence getTimer() {
        return timer;
    }

}
