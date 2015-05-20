/**
 * 
 */
package uk.ac.lkl.client;

import uk.ac.lkl.client.BrowsePanel;

/**
 * Like Command but passes in the panel involved
 * 
 * @author Ken Kahn
 *
 */
public abstract class BrowsePanelCommand {
    
    public abstract void execute(BrowsePanel panel, String answer[], boolean panelIsNew);

    public void failed() {
	// overridden by those with something special to do
    }
}
