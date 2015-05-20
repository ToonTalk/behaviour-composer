/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;

import uk.ac.lkl.client.composer.MacroBehaviourView;

/**
 * When a virus is added then hide its micro-behaviours
 * 
 * @author Ken Kahn
 *
 */
public class AddVirusTrailSessionEventsCheckBox extends SessionEventsCheckBox {

    public AddVirusTrailSessionEventsCheckBox(
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
	super.checkActionCompleted(checked);
	MacroBehaviourView virus = Modeller.instance().getMacroBehaviourWithName("Virus");
	if (checked) {
	    if (virus != null) {
		virus.setShowMicroBehaviours(false);
		virus.setShowHideThisCheckBox(false);
		virus.setShowHowManyInstances(false);
		virus.setVisible(true);
	    }
	}
    }
    
    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
	super.onValueChange(event);
	if (!getValue()) { // unticked
	    MacroBehaviourView virus = Modeller.instance().getMacroBehaviourWithName("Virus");
	    if (virus != null) {
		virus.setVisible(false);
	    }
	}
    }

}
