/**
 * 
 */
package uk.ac.lkl.shared;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Implements the response from createDeltaPage from server to client
 * 
 * @author Ken Kahn
 *
 */
public class DeltaPageResult implements Serializable {
    
    static final long serialVersionUID = 47360261L;
    
    private String newURL;
    
    private String errorMessage;
    
    private ArrayList<ArrayList<String>> listsOfMicroBehavioursCopy;
    
    public DeltaPageResult(String newURL,
	                   ArrayList<ArrayList<String>> listsOfMicroBehavioursCopy, 
	                   String errorMessage) {
	this.newURL = newURL;
	this.listsOfMicroBehavioursCopy = listsOfMicroBehavioursCopy;
	this.errorMessage = errorMessage;
    }
    
    public DeltaPageResult(String errorMessage) {
	this(null, null, errorMessage);
    }
    
    public DeltaPageResult() {
	// for serialization
    }

    public String getNewURL() {
        return newURL;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ArrayList<ArrayList<String>> getListsOfMicroBehavioursCopy() {
        return listsOfMicroBehavioursCopy;
    }

}
