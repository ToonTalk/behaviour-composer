/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * A convenient way to create editor areas and give them a CSS style
 * 
 * @author Ken Kahn
 *
 */
public class MicroBehaviourEditorArea extends VerticalPanelWithDebugID {

    public MicroBehaviourEditorArea(String title, Widget editArea) {
	super();
	HTML descriptionTitle = new MicroBehaviourEditorHeader(title);
	add(descriptionTitle);
	add(editArea);
	editArea.setWidth("100%");
	setWidth("100%");
	setStylePrimaryName("modeller-micro-behaviour-editor-area");
    }

}
