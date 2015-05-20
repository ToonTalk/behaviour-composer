package uk.ac.lkl.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;

import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.client.BrowsePanel;

@SuppressWarnings("serial")
public class BrowseToPageEvent extends ModellerEvent {

    protected String url;
    protected String baseUrl;
    
    public BrowseToPageEvent(String url, BrowsePanel panel) {
	super(panel);
	this.url = url;
	this.baseUrl = panel.getCurrentURLBase();
    }

    @Override
    public boolean dirtyEvent() {
	return false;
    }
    
    public String toHTMLString(boolean brief) {
	return Utils.textFontToMatchIcons(Modeller.constants.browsedTo() + Modeller.NON_BREAKING_SPACE)
	       + Utils.textFontToMatchIcons(url);
    }
    
    public String getXML() {
	// not clear this is needed
	return "<BrowseToPageEvent url='" + getUrl() + " baseUrl='" + getBaseUrl() + "'" + getDateAttribute() + "/>";
    } 
    
    public void recordInDatabase(AsyncCallback<String[]> recordSubsequentEventCallback, boolean notifyOthers) {
	// nothing to do	
    }

    public BrowsePanel getPanel() {
	return (BrowsePanel) getSource();
    }

    public String getUrl() {
        return url;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

}
