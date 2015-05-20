package uk.ac.lkl.client.composer;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.NumberOfInstancesTextArea;
import uk.ac.lkl.client.VerticalPanelWithDebugID;

import com.google.gwt.user.client.ui.HTML;

public class HowManyInstancesPanel extends VerticalPanelWithDebugID {
    
    private NumberOfInstancesTextArea numberOfInstancesTextArea;
    private HTML label;

    public HowManyInstancesPanel(MacroBehaviourView macroBehaviourView) {
	super();
	String howManyInstancesLabel = Modeller.constants.howManyInstances();
	label = new HTML(howManyInstancesLabel);
	add(label);
	numberOfInstancesTextArea = new NumberOfInstancesTextArea(macroBehaviourView, this);
	numberOfInstancesTextArea.setMinCharacterWidth(howManyInstancesLabel.length());
	add(numberOfInstancesTextArea);
	setStylePrimaryName("modeller-how-many-instances-panel");
    }
    
    public void updateWidth() {
	if (label == null || numberOfInstancesTextArea == null) {
	    // initialising -- too early to do this
	    return;
	}
	int offsetWidth = Math.max(label.getOffsetWidth(), numberOfInstancesTextArea.getOffsetWidth());
	if (offsetWidth > 0) {
	    setWidth(offsetWidth + "px");
	}
    }

    public void setText(String instanceCountExpression) {
	numberOfInstancesTextArea.setText(instanceCountExpression);
    }

}
