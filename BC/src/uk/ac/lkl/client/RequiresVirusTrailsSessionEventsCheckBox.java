package uk.ac.lkl.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;

public class RequiresVirusTrailsSessionEventsCheckBox extends SessionEventsCheckBox {
    
    protected SessionEventsCheckBox virusTrails;
    
    protected String messageToEnableVirusTrails;

    public RequiresVirusTrailsSessionEventsCheckBox(
	    String nameId,
	    String label, 
	    String guid,
	    String doMessage, 
	    String undoMessage,
	    String title,
	    BehaviourComposer behaviourComposer, 
	    SessionEventsCheckBox virusTrails,
	    String messageToEnableVirusTrails) {
	super(nameId, label, guid, doMessage, undoMessage, title, behaviourComposer);
	this.virusTrails = virusTrails;
	this.messageToEnableVirusTrails = messageToEnableVirusTrails;
    }
    
    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
	if (!getValue() || virusTrails.getValue()) {
	    super.onValueChange(event);
	} else {
	    Modeller.setAlertsLineAndHighlight(messageToEnableVirusTrails);
	    setValue(false);
	}
    }

}
