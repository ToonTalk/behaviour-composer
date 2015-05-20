/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.HTML;

/**
 * 
 * A place to control the style and layout of a column of buttons
 * in the Epidemic Game Maker.
 * 
 * @author Ken Kahn
 *
 */
public class EpidemicGameCheckBoxColumn extends VerticalPanelWithDebugID {

    public EpidemicGameCheckBoxColumn(String title) {
	super();
	setStylePrimaryName("modeller-EpidemicGameButtonColumn");
	setSpacing(4);
	add(new HTML(title));
	add(new HTML("<hr></hr>"));
    }

}
