/**
 * 
 */
package uk.ac.lkl.client;

import uk.ac.lkl.client.composer.MacroBehaviourView;

import com.google.gwt.user.client.ui.CheckBox;

/**
 * Check boxes indicating whether to add or ignore the prototype
 * 
 * @author Ken Kahn
 *
 */
public class MacroBehaviourCheckBox extends CheckBox {
    
    private MacroBehaviourView macroBehaviour;

    public MacroBehaviourCheckBox(MacroBehaviourView macroBehaviour) {
	super(macroBehaviour.getNameHTML(), true);
	setValue(true); // include by default
	this.macroBehaviour = macroBehaviour;
	setTitle(Modeller.constants.clickToToggleWhetherToAddThisPrototypeToTheCurrentModel());
	setStylePrimaryName("modeller-macro-behaviour-check-box");
    }
    
    public MacroBehaviourView getMacroBehaviour() {
	if (getValue()) {
	    return macroBehaviour;
	} else {
	    return null;
	}
    }

}
