/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for requesting the splitting (or unsplitting) of a panel and the rest of the interface
 * Provides both horizontal and vertical options
 * 
 * @author Ken Kahn
 *
 */
public class SplitScreenOptions extends HorizonalPanelWithDebugID {
    
    private CheckBox composerPanelSplitScreenCheckBox;
    private RadioButton horizontally;
    private RadioButton vertically;
    static private int counter = 0;
   
    public SplitScreenOptions(final Widget widget) {
	String groupName = "split-screen-options-" + counter++;
	horizontally = new RadioButton(groupName, Modeller.constants.sideBySide());
	vertically = new RadioButton(groupName, Modeller.constants.oneAboveAnother());
	ValueChangeHandler<Boolean> directionHandler = new ValueChangeHandler<Boolean>() {

	    @Override
	    public void onValueChange(ValueChangeEvent<Boolean> event) {
		if (composerPanelSplitScreenCheckBox.getValue()) {
		    Modeller.instance().unsplitPanel(false);
		    Modeller.instance().splitPanelFromRest(widget, true, horizontally.getValue());
		    widget.setVisible(true);
		}
	    }
	    
	};
	horizontally.addValueChangeHandler(directionHandler);
	vertically.addValueChangeHandler(directionHandler);
	horizontally.setValue(true);
	composerPanelSplitScreenCheckBox = 
		new CheckBox(Modeller.constants.splitScreenWithThis(), true);
	ValueChangeHandler<Boolean> splitterHandler = new ValueChangeHandler<Boolean>() {
	    @Override
	    public void onValueChange(ValueChangeEvent<Boolean> event) {
		Modeller.instance().splitPanelFromRest(widget, 
			                               event.getValue(), 
			                               horizontally.getValue());
		widget.setVisible(true);
	    };
	};
	composerPanelSplitScreenCheckBox.addValueChangeHandler(splitterHandler);
	setSpacing(6);
	add(composerPanelSplitScreenCheckBox);
	add(horizontally);
	add(vertically);
    }

    public void removeTicks() {
	composerPanelSplitScreenCheckBox.setValue(false);
    }

}
