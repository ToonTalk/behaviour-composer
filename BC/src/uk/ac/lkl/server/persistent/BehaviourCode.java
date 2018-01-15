/**
 * 
 */
package uk.ac.lkl.server.persistent;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.jdo.annotations.Persistent;
//import javax.persistence.Id;

import uk.ac.lkl.shared.CommonUtils;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Id;

/**
 * Stores the NetLogo behaviour code associated with the URL
 * 
 * @author Ken Kahn
 *
 */

@Cached
public class BehaviourCode {
    
    @Id String url;
    @Persistent
    private Text code;
    @Persistent
    // following used to substitute text areas for text area names
    private List<Text> textAreaElements;

    public BehaviourCode(String url, String codeString, List<String> textAreaElements) {
	this.url = url;
	this.code = new Text(codeString);
	setTextAreaElements(textAreaElements);
    }
    
    public BehaviourCode() {}; // for Objectify

    public String getCode() {
        return code.getValue();
    }
    
    public void setCode(String newValue) {
        code = new Text(newValue);
    }
    
    public ArrayList<String> getTextAreaElements() {
        if (textAreaElements == null) {
	    return null;
	} else {
	    ArrayList<String> result = new ArrayList<String>();
	    for (Text textAreaElement : textAreaElements) {
		result.add(textAreaElement.getValue());
	    }
	    return result;
	}  
    }

    private void setTextAreaElements(List<String> textAreaElements) {
	if (textAreaElements == null) {
	    this.textAreaElements = null;
	} else {
	    ArrayList<Text> textAreaElementsAsText = new ArrayList<Text>();
	    for (String textAreaDefaultValue : textAreaElements) {
		textAreaElementsAsText.add(new Text(textAreaDefaultValue));
	    }
	    this.textAreaElements = textAreaElementsAsText;
	}
    }
    
    public static String get(String fullUrl) {
	try {
        	String url = CommonUtils.removeBookmark(fullUrl);
        	BehaviourCode behaviourCode = DataStore.begin().find(BehaviourCode.class, url);
        	if (behaviourCode == null) {
        	    return null;
        	} else {
        	    return behaviourCode.getCode();
        	}
	} catch (Exception e) {
	    System.err.print("BehaviourCode.get threw: " + e.getMessage());
	    return null;
	}
    }
    
    public static ArrayList<String> getTextAreaElements(String fullUrl) {
	String url = CommonUtils.removeBookmark(fullUrl);
	BehaviourCode behaviourCode = DataStore.begin().find(BehaviourCode.class, url);
	if (behaviourCode == null) {
	    return null;
//	    String pageContents = ServerUtils.urlToString(url, new ClientState(), false);
//	    String behaviourCode = ServerUtils.extractBehaviourCode(pageContents);
//	    if (behaviourCode != null) {
//		DataStore.begin().put(url, behaviourCode);
//	    }
//	    return behaviourCode;
	} else {
	    return behaviourCode.getTextAreaElements();
	}
    }

    public static boolean update(String fullUrl, String newBehaviourCode, List<String> newTextAreaElements) {
	String url = CommonUtils.removeBookmark(fullUrl);
	BehaviourCode behaviourCode = DataStore.begin().find(BehaviourCode.class, url);
	if (behaviourCode == null) {
	    behaviourCode = new BehaviourCode(url, newBehaviourCode, newTextAreaElements);
	} else if (behaviourCode.getCode().equals(newBehaviourCode) && 
		   ((newTextAreaElements == null && behaviourCode.getTextAreaElements() == null) ||
		    newTextAreaElements.equals(behaviourCode.getTextAreaElements()))) {
	    return false;
	} else {
	    behaviourCode.setCode(newBehaviourCode);
	    behaviourCode.setTextAreaElements(newTextAreaElements);
	}
	try {
	    DataStore.begin().put(behaviourCode);
	} catch (ConcurrentModificationException e) {
	    // See http://stackoverflow.com/questions/10454467/google-app-engine-hrd-what-if-i-exceed-the-1-write-per-second-limit-for-writin
	    // Seen in logs from 2014-05-08 22:09:49
	    // might help to wait a second
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e1) {
		// was worth trying anyway
	    }
	    DataStore.begin().put(behaviourCode);
	}
	return true;
    }

}
