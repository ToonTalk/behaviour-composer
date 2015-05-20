/**
 * 
 */
package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import uk.ac.lkl.client.composer.MacroBehaviourView;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.event.ReplaceURLEvent;
import uk.ac.lkl.shared.DeltaPageResult;

/**
 * Like Command but passes in the panel involved
 * 
 * @author Ken Kahn
 *
 */
public class CreateDeltaPageCommand {
    
    public CreateDeltaPageCommand() {
	
    }
    
    public void execute(MicroBehaviourView microBehaviour, DeltaPageResult deltaPageResult, boolean panelIsNew, boolean subMicroBehavioursNeedNewURLs, boolean forCopying) {
	if (deltaPageResult.getErrorMessage() != null) {
	    Modeller.addToErrorLog(deltaPageResult.getErrorMessage());
	} else {
	    String newURL = deltaPageResult.getNewURL();
	    microBehaviour.newURL(newURL, forCopying);
	    if (subMicroBehavioursNeedNewURLs) {
		try {
		    // following needed when a micro-behaviour with macro-behaviours is copied
		    // the micro-behaviours need new URLs
		    ArrayList<ArrayList<String>> listsOfMicroBehavioursCopy = 
			    deltaPageResult.getListsOfMicroBehavioursCopy();
		    if (listsOfMicroBehavioursCopy != null) {
			Iterator<ArrayList<String>> iterator = listsOfMicroBehavioursCopy.iterator();
			ArrayList<MacroBehaviourView> macroBehaviourViews = microBehaviour.getMacroBehaviourViews();
			for (MacroBehaviourView macroBehaviourView : macroBehaviourViews) {
			    // if there are macro-behaviours then need to update their micro-behaviour URLs as well
			    ArrayList<String> newURLs = iterator.next();
			    ArrayList<MicroBehaviourView> microBehaviours = macroBehaviourView.getMicroBehaviours();
			    Iterator<String> newURLIterator = newURLs.iterator();
			    newURLIterator.next(); // first element is macro-behaviour name
			    ArrayList<MicroBehaviourView> freshCopies = new ArrayList<MicroBehaviourView>();
			    for (int i = 0; i < microBehaviours.size(); i++) {
				MicroBehaviourView childMicroBehaviourView = microBehaviours.get(i);
				MicroBehaviourView copy = childMicroBehaviourView.copyWithoutSharing(freshCopies);
				microBehaviours.set(i, copy);
				if (newURLIterator.hasNext()) {
				    String oldURL = copy.getUrl();
				    copy.newURL(newURLIterator.next(), forCopying);
				    //				String nextURL = newURLIterator.next();
				    //				childMicroBehaviourView.setUrl(nextURL);
				    //				childMicroBehaviourView.setCopyMicroBehaviourWhenExportingURL(false);
				    new ReplaceURLEvent(copy, oldURL).addToHistory();
				} else {
				    String errorMessge = "Server's list of micro-behaviours doesn't match the client state:\n " + 
					    listsOfMicroBehavioursCopy +
					    " server's list: ";
				    for (int j = 0; j < microBehaviours.size(); j++) {
					MicroBehaviourView child = microBehaviours.get(j);
					errorMessge += child.getNameHTML() + " ";
				    }
				    Utils.logServerMessage(Level.SEVERE, errorMessge);
				}
			    }
			}
		    }
		} catch (Exception e) {
		    Modeller.reportException(e, "While updating micro behaviours.");
		}
	    }
	}
    }
    
}
