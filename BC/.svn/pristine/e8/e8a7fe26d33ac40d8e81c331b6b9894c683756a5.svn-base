/**
 * 
 */
package uk.ac.lkl.client;

import java.util.ArrayList;

import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * Area where alerts and status messages are posted
 * 
 * @author Ken Kahn
 *
 */
public class AlertsLine extends HTML {
    
    protected String currentHTML;
    
    // activeAlerts used to manage multiple overlapping alerts
    protected ArrayList<String> activeAlerts = new ArrayList<String>();

    protected boolean alertsLineClear = true;

    private DecoratedPopupPanel popupBeforeAlertLineAttached;
    
    public AlertsLine() {
	super(Modeller.NON_BREAKING_SPACE);
	setStylePrimaryName("modeller-alerts-line");
    }
    
    @Override
    public void setHTML(String newHTML) {
	if (isAttached()) {
	    super.setHTML(newHTML);
	    currentHTML = newHTML;
	    alertsLineClear  = false;
	    setTitle(CommonUtils.removeHTMLMarkup(newHTML));
	    if (popupBeforeAlertLineAttached != null) {
		popupBeforeAlertLineAttached.hide();
		popupBeforeAlertLineAttached = null;
	    }
	} else if (!newHTML.equals("&nbsp;")) {
	    popupBeforeAlertLineAttached = Utils.popupMessage(newHTML, false);
	}
    }
    
    public void clearAlertsLine() {
	// remove any text but leave the space
	activeAlerts.remove(currentHTML); // in case it is there
	if (activeAlerts.isEmpty()) {
	    setHTML(Modeller.NON_BREAKING_SPACE);
	    alertsLineClear = true;
	    currentHTML = null;
	} else {
	    // display oldest one
	    setHTML(activeAlerts.get(0));
	}
    }
    
    public String addAlert(String alert) {
	boolean noAlerts = activeAlerts.isEmpty();
	if (activeAlerts.contains(alert)) {
	    return alert;
	}
	activeAlerts.add(alert);
	if (isAlertsLineClear() || noAlerts) {
	    setHTML(alert);
	}
	return alert;
    }
    
    public void removeAlert(String alert) {
	activeAlerts.remove(alert);
	if (currentHTML == alert) {
	    if (activeAlerts.isEmpty()) {
		clearAlertsLine();
	    } else {
		// display oldest one
		setHTML(activeAlerts.get(0));
	    }
	}
    }
    
    public void removeAlerts() {
	activeAlerts.clear();
    }

    protected boolean isAlertsLineClear() {
        return alertsLineClear;
    }
    
    public boolean isActiveAlertsEmpty() {
	return activeAlerts.isEmpty();
    }
    
//    @Override
//    public void onLoad() {
//	super.onLoad();
//	resize();
//    }

}
