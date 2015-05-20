/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.HTML;

/**
 * Used for labels like Run, Share, Download
 * 
 * @author Ken Kahn
 *
 */
public class TabLabel extends HTML {
    
    public TabLabel(String html) {
	super(html);
	setStylePrimaryName("modeller-tab-label");
    }

}
