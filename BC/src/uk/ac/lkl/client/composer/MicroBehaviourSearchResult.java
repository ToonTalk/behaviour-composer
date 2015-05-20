/**
 * 
 */
package uk.ac.lkl.client.composer;

import com.google.gwt.event.dom.client.ClickEvent;
import uk.ac.lkl.client.Modeller;

/**
 * Micro-behaviour search result just open when clicked upon
 * 
 * @author Ken Kahn
 *
 */
public class MicroBehaviourSearchResult extends MicroBehaviourView {

    public MicroBehaviourSearchResult(String nameHTML, MicroBehaviourSharedState sharedState) {
	super(nameHTML, sharedState);
    }
    
    @Override
    protected void createPopupMenu(ClickEvent event) {
	openInBrowsePanel(true);
    }
    
    @Override
    public void onLoad() {
	super.onLoad();
	setTitle(Modeller.constants.clickToOpenThisInATab());
    }

}
