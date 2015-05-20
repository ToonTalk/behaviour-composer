package uk.ac.lkl.client;

import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.logical.shared.ValueChangeEvent;

public class SliderCheckBox extends SessionEventsCheckBox {
    
    public SliderCheckBox(
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
    public void onValueChange(ValueChangeEvent<Boolean> event) {
	super.onValueChange(event);
	int numberOfSlidersInEpidemicGame = behaviourComposer.getNumberOfSlidersInEpidemicGame();
	if (getValue()) {
	    behaviourComposer.setNumberOfSlidersInEpidemicGame(numberOfSlidersInEpidemicGame+1);
	    if (numberOfSlidersInEpidemicGame >= 8) {
		extraDoMessage = CommonUtils.stronglyHighlight(Modeller.constants.onlyRoomFor8Sliders());
	    }
	} else {
	    behaviourComposer.setNumberOfSlidersInEpidemicGame(numberOfSlidersInEpidemicGame-1);
	}
    }

}
