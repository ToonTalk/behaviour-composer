/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.HTML;

/**
 * 
 * An Info tab compatible with the NetLogo info tab
 * 
 * @author Ken Kahn
 *
 */
public class InfoPanel extends VerticalPanelWithDebugID {
    
    private InfoTabTextArea infoTextArea;
    
    public InfoPanel() {
	super();
	setSpacing(10);
	add(new HTML(Modeller.constants.infoTabIntro()));
	infoTextArea = new InfoTabTextArea();
	add(infoTextArea);
    }

    public String getContents() {
	return infoTextArea.getContents();
    }
    
    public void setContents(String contents) {
	infoTextArea.setContents(contents);
    }

    public void initialise() {
	infoTextArea.setContents(null);	
    }

}
