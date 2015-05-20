/**
 * 
 */
package uk.ac.lkl.client;

import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.event.RenameMicroBehaviourEvent;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * @author Ken Kahn
 *
 * Implements text areas within the code section of a micro-behaviour
 * Upon loss of focus it communicates the current value (if changed) to the server
 */

public class DescriptionTextArea extends ResizingTextArea {
    
    protected String currentContents;
    
    private BrowsePanel browsePanel;
    
    public DescriptionTextArea(String description, final BrowsePanel browsePanel) {
	super();
	this.browsePanel = browsePanel;
	setCurrentContents(description);
	addValueChangeHandler(new ValueChangeHandler<String>() {

	    @Override
	    public void onValueChange(ValueChangeEvent<String> event) {
		String currentContents = getCurrentContents();
		setCurrentContents(getNewContents());
		MicroBehaviourView microBehaviour = DescriptionTextArea.this.browsePanel.getMicroBehaviour();
		if (microBehaviour != null) {
		    String nameHTML = microBehaviour.getNameHTML();
		    String nameHTMLAndDescription = CommonUtils.combineNameHTMLAndDescription(nameHTML, getNewContents());
		    microBehaviour.setNameHTMLAndDescription(nameHTMLAndDescription);
		    new RenameMicroBehaviourEvent(microBehaviour, 
			                          CommonUtils.combineNameHTMLAndDescription(nameHTML, currentContents)).addToHistory();
		    boolean copyOnUpdate = browsePanel.isCopyOnUpdate();
		    if (copyOnUpdate) {
			browsePanel.copyMicroBehaviourWhenExportingURL();
		    }
		}
	    }
	    
	});
	setStylePrimaryName("modeller-description-text-area");
	setTitle(Modeller.constants.clickToEditTheDescription());
    }

    public String getCurrentContents() {
        return currentContents;
    }

    public void setCurrentContents(String currentContents) {
        this.currentContents = currentContents;
        setText(currentContents);
    }

    public String getNewContents() {
	return getValue();
    }

}
