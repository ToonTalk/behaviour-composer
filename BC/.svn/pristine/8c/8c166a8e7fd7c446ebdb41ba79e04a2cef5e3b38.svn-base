/**
 * 
 */
package uk.ac.lkl.client;

import uk.ac.lkl.client.event.UpdateTextAreaEvent;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.dom.client.Element;

/**
 * @author Ken Kahn
 *
 * Implements text areas within the code section of a micro-behaviour
 * Upon loss of focus it communicates the current value (if changed) to the server
 */

public class CodeTextArea extends ResizingTextArea {
    
    protected String currentContents;
    
    private BrowsePanel browsePanel;

    private String name;

    private boolean parameterRadioButton;

//    private int maxVisibleLines;
    
    public CodeTextArea(final int indexInCode, BrowsePanel browsePanel, Element element) {
	super();
	this.browsePanel = browsePanel;
	addStyleName("modeller-code-text-area");
	browsePanel.addCodeTextArea(indexInCode, this);
//	Style elementStyle = element.getStyle();
//	Style myStyle = getElement().getStyle();
//	String property = elementStyle.getProperty("overflow");
	// Does this need to go through every property??
//	myStyle.setProperty("overflow", property);
//	setMinCharacterWidth(20);
//	setMaxCharacterWidth(element.getPropertyInt("cols"));
//	maxVisibleLines = element.getPropertyInt("rows");
	setCurrentContents(element.getInnerText());
	name = element.getAttribute("name");
	if (name.equals("name_of_parameter")) {
	    browsePanel.updateParameterRadioButtons(this);
	    parameterRadioButton = true;
	}
	addValueChangeHandler(new ValueChangeHandler<String>() {

	    @Override
	    public void onValueChange(ValueChangeEvent<String> event) {
		valueChanged(indexInCode, name);	
	    }
	    
	});
//	this.addClickHandler(new ClickHandler() {
//
//	    @Override
//	    public void onClick(ClickEvent event) {
//		// DEFINE-PARAMETER.html triggers a programmatic change in the value
//		// but it seems this event is triggered
//		valueChanged(indexInCode, name);
//	    }
//	    
//	});
    }

    public String getCurrentContents() {
        return currentContents;
    }

    public void setCurrentContents(String currentContents) {
        this.currentContents = currentContents;
        setText(currentContents);
        if (name != null && name.equals("name_of_parameter")) {
	    browsePanel.updateParameterRadioButtons(this);
	}
    }

    public String getNewContents() {
	return getElement().getInnerText();
    }
    
    @Override
    public void onUnload() {
	super.onUnload();
	updateIfParameterName();
    }

    protected void valueChanged(final int indexInCode, final String name) {
	String newContents = getText();
	if (newContents.equals(currentContents)) {
	    return; // didn't change after all
	}
	final String urls = browsePanel.getAllURLs();
	final String tabTitle = browsePanel.getTaggingTitle();
	boolean copyOnUpdate = browsePanel.isCopyOnUpdate();
	updateTextArea(indexInCode, urls, tabTitle, name, currentContents, newContents, copyOnUpdate);
	if (CommonUtils.hasChangesGuid(urls)) {
	    new UpdateTextAreaEvent(currentContents, newContents, indexInCode, urls, name, tabTitle, copyOnUpdate).addToHistory();
	} else {
	    browsePanel.copyMicroBehaviourWhenExportingURL();
	}
	setCurrentContents(newContents);
	browsePanel.updateTextArea(indexInCode, newContents, false);
	if (copyOnUpdate) {
//	    if (CommonUtils.hasChangesGuid(url)) {
		browsePanel.copyMicroBehaviourWhenExportingURL();
//	    } else {
//		browsePanel.copyMicroBehaviour();
//	    }
	}
	// don't clutter the interface with tool tips once the user
	// had edited a field
	setTitle(null);
    }

    public static void updateTextArea(
	    int indexInCode,
	    String url,
	    String tabTitle, 
	    String name, 
	    String oldContents,
	    String newContents, 
	    boolean copyOnUpdate) {
	Modeller.instance().updateTextAreaOfAllMicroBehaviourViews(url, newContents, indexInCode);
    }
    
    public void updateIfParameterName() {
	if (parameterRadioButton) {
	    valueChanged(0, "name_of_parameter");
	}
    }

}
