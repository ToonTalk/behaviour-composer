/**
 * 
 */
package uk.ac.lkl.client.composer;

/**
 * @author Ken
 *
 */
public class SearchResultsLabel implements SearchResultsItem {

    private String text;
    
    public SearchResultsLabel(String text) {
	super();
	this.text = text;
    }

    @Override
    public MicroBehaviourView getMicroBehaviourView() {
	return null;
    }

    @Override
    public String getText() {
	return text;
    }

    @Override
    public int getDepth() {
	return 0;
    }

}
