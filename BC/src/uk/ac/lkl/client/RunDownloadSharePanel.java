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
	    if (dirty || (!share && BehaviourComposer.runPanel.runEvenIfClean()) && enabled) {
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
	    add(new HTML(Modeller.constants.pleaseWait()));
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
