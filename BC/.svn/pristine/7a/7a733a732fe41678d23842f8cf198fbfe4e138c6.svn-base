/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;

import uk.ac.lkl.client.composer.MacroBehaviourView;

/**
 * When a work place is added make it hide its micro-behaviours
 * 
 * @author Ken Kahn
 *
 */
public class AddWorkPlaceSessionEventsCheckBox extends SessionEventsCheckBox {

    public AddWorkPlaceSessionEventsCheckBox(
	    String nameId,
	    String label, 
	    String guid,
	    String doMessage, 
	    String undoMessage,
	    String title,
	    BehaviourComposer behaviourComposer) {
	super(nameId, label, guid, doMessage, undoMessage, title, behaviourComposer);
    }
    
    @Override
    protected void checkActionCompleted(boolean checked) {
	MacroBehaviourView workPlace = Modeller.instance().getMacroBehaviourWithName("Work Place");
	super.checkActionCompleted(checked);
	if (checked) {
	    if (workPlace != null) {
		workPlace.setShowMicroBehaviours(false);
		workPlace.setShowHideThisCheckBox(false);
		workPlace.setShowHowManyInstances(false);
		workPlace.setVisible(true);
	    }
	}
    }
    
    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
	super.onValueChange(event);
	if (!getValue()) { // unticked
	    MacroBehaviourView workPlace = Modeller.instance().getMacroBehaviourWithName("Work Place");
	    if (workPlace != null) {
		workPlace.setVisible(false);
	    }
	}
    }

}
