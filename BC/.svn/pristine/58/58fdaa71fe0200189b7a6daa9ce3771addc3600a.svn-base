package uk.ac.lkl.server.persistent;

import java.util.ArrayList;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

/**
 * Represents lists of lists of behaviour name followed by URLs that are copies of a micro-behaviour
 * 
 * Fixes several problems with MicroBehaviourCopyMicroBehaviours
 * 
 * @author Ken Kahn
 *
 */

@PersistenceCapable
public class MicroBehaviourCopyListsOfMicroBehaviours {
    
    @PrimaryKey
    private String guid;
    @Persistent
    private Text[] listsOfMicroBehavioursFlattened;
    
    public MicroBehaviourCopyListsOfMicroBehaviours(String guid, ArrayList<ArrayList<String>> listsOfMicroBehaviours) {
	this.guid = guid;
	setListsOfMicroBehaviours(listsOfMicroBehaviours);
    }

    private void setListsOfMicroBehaviours(ArrayList<ArrayList<String>> listsOfMicroBehaviours) {
	// JDO doesn't support ArrayList...
	if (listsOfMicroBehaviours.isEmpty()) {
	    return;
	}
	listsOfMicroBehavioursFlattened = new Text[listsOfMicroBehaviours.size()];
	int index = 0;
	for (ArrayList<String> list : listsOfMicroBehaviours) {
	    StringBuffer listFlattened = new StringBuffer();
	    for (String nameOrURL : list) {
		listFlattened.append(nameOrURL);
		listFlattened.append('<'); // triangle brackets safe since HTML removed from name and not used in URL encoding
	    }
	    listsOfMicroBehavioursFlattened[index] = new Text(listFlattened.toString());
	    index++;
	}
    }

    public String getGuid() {
        return guid;
    }

    public ArrayList<ArrayList<String>> getListsOfMicroBehaviours() {
	ArrayList<ArrayList<String>> listsOfMicroBehaviours = new ArrayList<ArrayList<String>>();
	for (Text listAsText : listsOfMicroBehavioursFlattened) {
	    ArrayList<String> listOfMicroBehaviours = new ArrayList<String>();
	    String list = listAsText.getValue();
	    String[] nameOrURLs = list.split("<");
	    for (String nameOrURL : nameOrURLs) {
		listOfMicroBehaviours.add(nameOrURL);
	    }
	    listsOfMicroBehaviours.add(listOfMicroBehaviours);
	}
        return listsOfMicroBehaviours;
    }

}
