/**
 * 
 */
package uk.ac.lkl.server;

import java.util.ArrayList;

/**
 * @author Ken
 *
 */
public class FoundAttribute {
    
    private String attributeName;
    
    private ArrayList<String> readingMicroBehaviours = new ArrayList<String>();
    
    private ArrayList<String> writingMicroBehaviours = new ArrayList<String>();
    
    public FoundAttribute(String attributeName) {
	super();
	this.attributeName = attributeName;
    }
    
    public void addReadingMicroBehaviour(MicroBehaviour microBehaviour) {
	String url = microBehaviour.getBehaviourURL();
	if (!readingMicroBehaviours.contains(url)) {
	    readingMicroBehaviours.add(url);
	}
    }
    
    public void addWritingMicroBehaviour(MicroBehaviour microBehaviour) {
	String url = microBehaviour.getBehaviourURL();
	if (!writingMicroBehaviours.contains(url)) {
	    writingMicroBehaviours.add(url);
	}
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String encodeReadingMicroBehavioursAsString() {
	String result = "";
	for (String readingMicroBehaviour : readingMicroBehaviours) {
	    result += readingMicroBehaviour + ";";
	}
	return result;
    }
    
    public String encodeWritingMicroBehavioursAsString() {
	String result = "";
	for (String writingMicroBehaviour : writingMicroBehaviours) {
	    result += writingMicroBehaviour + ";";
	}
	return result;
    }

}
