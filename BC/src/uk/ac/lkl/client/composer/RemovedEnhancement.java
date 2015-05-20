/**
 * 
 */
package uk.ac.lkl.client.composer;

/** 
 * A structure to hold the state of a removed enhancement
 * 
 * @author Ken Kahn
 *
 */
public class RemovedEnhancement {

    private MicroBehaviourEnhancement enhancementRemoved;
    private String textAreaValueRemoved;
    private int textAreaIndex;

    public RemovedEnhancement(MicroBehaviourEnhancement enhancementRemoved, String textAreaValueRemoved, int textAreaIndex) {
	this.enhancementRemoved = enhancementRemoved;
	this.textAreaValueRemoved = textAreaValueRemoved;
	this.textAreaIndex = textAreaIndex;
    }

    public MicroBehaviourEnhancement getEnhancementRemoved() {
        return enhancementRemoved;
    }

    public String getTextAreaValueRemoved() {
        return textAreaValueRemoved;
    }

    public int getTextAreaIndex() {
        return textAreaIndex;
    }

}
