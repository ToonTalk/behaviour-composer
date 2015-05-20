package uk.ac.lkl.client.composer;

import uk.ac.lkl.client.Modeller;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Displays a list of AttributeViews
 * 
 * @author Ken Kahn
 *
 */
public class AttributesDisplay extends DecoratedPopupPanel {

    private TitleBar titleBar;

    public AttributesDisplay(String[] attributeNamesAndMicroBehaviours, String attributesOf) {
	setAnimationEnabled(true);
	VerticalPanel verticalPanel = new VerticalPanel();
	verticalPanel.setSpacing(4);
	String title = Modeller.constants.attributesOfX();
	titleBar = new TitleBar(title.replace("***prototype name***", attributesOf));
	ClickHandler clickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		hide();
		removeFromParent();
	    }
	    
	};
	titleBar.getCloseButton().addClickHandler(clickHandler);
	verticalPanel.add(titleBar);
	if (attributeNamesAndMicroBehaviours == null || attributeNamesAndMicroBehaviours.length == 0) {
	    verticalPanel.add(new HTML(Modeller.constants.noAttributesFound()));
	} else {
	    for (int i = 0; i < attributeNamesAndMicroBehaviours.length; i += 3) {
		String attributeName = attributeNamesAndMicroBehaviours[i];
		String encodedReadingMicroBehaviours = attributeNamesAndMicroBehaviours[i+1];
		String encodedWritingMicroBehaviours = attributeNamesAndMicroBehaviours[i+2];
		String[] readingMicroBehaviours = encodedReadingMicroBehaviours.split(";");
		String[] writingMicroBehaviours = encodedWritingMicroBehaviours.split(";");
		verticalPanel.add(new AttributeDisplay(attributeName, readingMicroBehaviours, writingMicroBehaviours));
	    }
	}
	setWidget(verticalPanel);
    }

}
