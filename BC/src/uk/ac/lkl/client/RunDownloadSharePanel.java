/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.HTML;

/**
 * Implements the panel where URLs and embedding widgets are available to save or share the current model.
 * 
 * @author Ken Kahn
 *
 */

public class RunDownloadSharePanel extends VerticalPanelWithDebugID {
    
    private boolean dirty = true; 
    private boolean enabled = true;   
    private boolean share;
    private boolean run;
    private String modelGuid = null;
    private String savedTitle;
    protected HTML waitingMessage = null;
    
    public RunDownloadSharePanel(boolean run, boolean share) {
	super();
	this.share = share;
	this.run = run;
    }
    
    @Override
    public void setVisible(boolean visible) {
	if (isVisible() == visible) {
	    return;
	}
	super.setVisible(visible);
	if (visible && isAttached()) {
	    BehaviourComposer.runPanel.runDownloadSharePanelDisplayed(this);
	    if (!enabled) {
		this.add(new HTML(Modeller.constants.waitUntilModelFullyLoaded()));
	    } else if (dirty || (!share && Modeller.instance().getNetLogo2BCChannelToken() == null) || (!share && BehaviourComposer.runPanel.runEvenIfClean())) {
		// NetLogo Web should be used if there is a run tab
		// it used to be that switching between run and download didn't recompute the model
		// but unless running the BC to NetLogo tool they need to be recomputed with run is NetLogo web compatible
		// if NetLogo Web compatibility is either explicitly required or the run tab is activated then set the compatiblity
		Modeller.forWebVersion = Modeller.forWebVersionRequested || this.run;
		BehaviourComposer.instance().runModel(run, share, this);
	    } 
	}
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    
    public void modelFilesBeingComputed() {
	clear();
	// IE9 was causing the entire page to reload
	// but instead declaring the home page to be compatible with IE8 fixes the problem
	// see http://code.google.com/p/google-web-toolkit/issues/detail?id=7682
//	boolean internetExplorer = false; // Window.Navigator.getUserAgent().indexOf("MSIE") >= 0;
//	if (internetExplorer) {
//	    // hack to work around problem that run panel reloads the entire page when the applet loads
//	    add(new HTML("Internet Explorer no longer supports the Run tab. Click on the Download tab and use the the 'Run the model' link instead."));
//	} else {
	    waitingMessage  = new HTML(Modeller.constants.pleaseWait());
	    add(waitingMessage);
//	}
	setDirty(false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
	if (this.enabled == enabled) {
	    return;
	}
        this.enabled = enabled;
        HTML tabLabel = null;
        if (this == BehaviourComposer.runPanel) {
            tabLabel = BehaviourComposer.runTabLabel;
        } else if (this == BehaviourComposer.downloadPanel) {
            tabLabel = BehaviourComposer.downloadTabLabel;
        } else if (this == BehaviourComposer.sharePanel) {
            tabLabel = BehaviourComposer.shareTabLabel;
        }
        if (tabLabel != null) {
            if (enabled) {
        	tabLabel.removeStyleDependentName("disabled");
            } else {
        	tabLabel.addStyleDependentName("disabled");
            }
            if (enabled) {
                tabLabel.setTitle(savedTitle);
            } else {
                savedTitle = tabLabel.getTitle();
                tabLabel.setTitle(Modeller.constants.disabledWhileWaitingForServerResponse());
            }
        }
    }

    public String getModelGuid() {
        return modelGuid;
    }

    public void setModelGuid(String modelGuid) {
        this.modelGuid = modelGuid;
    }

    public boolean isShare() {
        return share;
    }

}
