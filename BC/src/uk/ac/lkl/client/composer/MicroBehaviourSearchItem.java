/**
 * 
 */
package uk.ac.lkl.client.composer;

/**
 * Represents micro-behaviours found by search
 * 
 * 
 * @author Ken Kahn
 *
 */
public class MicroBehaviourSearchItem implements SearchResultsItem {
    
    private MicroBehaviourView microBehaviourView;
    
    // tree depth this micro-behaviour was found
    private int depth;
    
    public MicroBehaviourSearchItem(MicroBehaviourView microBehaviourView, int depth) {
	this.microBehaviourView = microBehaviourView;
	this.depth = depth;
    }
    
    @Override
    public boolean equals(Object other) {
	if (other instanceof MicroBehaviourSearchItem) {
	    MicroBehaviourSearchItem otherMicroBehaviourSearchItem = (MicroBehaviourSearchItem) other;
	    return microBehaviourView.getUrl().equals(otherMicroBehaviourSearchItem.getMicroBehaviourView().getUrl());
	}
	return false;
    }

    public MicroBehaviourView getMicroBehaviourView() {
        return microBehaviourView;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public String getText() {
	// doesn't need one since has non-null microBehaviourView
	return null;
    }

}
