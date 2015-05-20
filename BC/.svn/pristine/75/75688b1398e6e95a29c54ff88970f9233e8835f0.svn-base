/**
 * 
 */
package uk.ac.lkl.client;

import uk.ac.lkl.client.composer.HowManyInstancesPanel;
import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.event.UpdateNumberOfInstancesTextAreaEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * Interface to specify the expression for the number of instances to make of a prototype
 * 
 * @author Ken Kahn
 *
 */
public class NumberOfInstancesTextArea extends ResizingTextArea {
    
    private HowManyInstancesPanel howManyInstancesPanel;
    
    public NumberOfInstancesTextArea(final MacroBehaviourView macroBehaviourView, HowManyInstancesPanel howManyInstancesPanel) {
	super();
	this.howManyInstancesPanel = howManyInstancesPanel;
	minCharacterWidth = 16;
	setText(macroBehaviourView.getInstanceCountExpression());
	ValueChangeHandler<String> valueChangeHandler = new ValueChangeHandler<String>() {

	    @Override
	    public void onValueChange(ValueChangeEvent<String> event) {
		String oldValue = macroBehaviourView.getInstanceCountExpression();
		String newValue = getText().trim();
		macroBehaviourView.setInstanceCountExpressionText(newValue);
		new UpdateNumberOfInstancesTextAreaEvent(oldValue, newValue, macroBehaviourView).addToHistory();
	    }
	    
	};
	addValueChangeHandler(valueChangeHandler);
	setTitle(Modeller.constants.numberOfInstancesTextAreaTitle());
	setVisibleLines(1);
    }
    
    @Override
    public void setText(String text) {
	super.setText(text);
	howManyInstancesPanel.updateWidth();
    }

}
