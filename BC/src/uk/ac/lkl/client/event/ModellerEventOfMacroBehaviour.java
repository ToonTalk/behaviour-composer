/**
 * 
 */
package uk.ac.lkl.client.event;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.composer.MacroBehaviourView;

/**
 * @author Ken Kahn
 *
 */
public abstract class ModellerEventOfMacroBehaviour extends ModellerEvent {
    
    private static final long serialVersionUID = 4822263062141873042L;
    protected String macroBehaviourNameAtEventTime;
    protected int macroBehaviourIndex;
    protected MacroBehaviourView macroBehaviour;

    public ModellerEventOfMacroBehaviour(MacroBehaviourView macroBehaviour) {
	super(macroBehaviour);
	setMacroBehaviour(macroBehaviour);
    }
    
    public MacroBehaviourView getMacroBehaviour() {
	if (macroBehaviourIndex < 0) {
	    return macroBehaviour;
	} else {
	    return Modeller.instance().getMacroBehaviourWithIndex(macroBehaviourIndex);
	}
    }
    
    protected void setMacroBehaviour(MacroBehaviourView macroBehaviour) {
	if (macroBehaviour != null) {
	    macroBehaviourNameAtEventTime = macroBehaviour.getHTML();
	}
	macroBehaviourIndex = Modeller.instance().getMacroBehaviourIndex(macroBehaviour);
	if (macroBehaviourIndex < 0) {
	    this.macroBehaviour = macroBehaviour;
	}
    }
}
