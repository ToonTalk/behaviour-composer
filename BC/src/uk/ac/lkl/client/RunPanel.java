/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.Frame;

/**
 * The special behaviour for the Run tab where the Java applet is displayed
 * 
 * This subclass ensures that the frame isn't added until the Run tab is selected.
 * Previously when the Download or Share tab became visible the Run tab added the frame
 * which was costly, generated errors in the server log if the model creation didn't go right,
 * and sometimes reported Java errors even when the Run tab was never accessed.
 * 
 * @author Ken Kahn
 *
 */
public class RunPanel extends RunDownloadSharePanel {
    
    private Frame frame = null;

    public RunPanel() {
	super(true, false);
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
	releaseFrame();
        this.frame = frame;
        if (isVisible() && frame != null && !isDirty()) {
            add(frame);
            frame = null;
        }
    }

    protected void releaseFrame() {
	if (frame != null) {
	    // this also should prevent an attempt to fetch a model's NLOGO after it has been deleted
	    // the following should force the reclamation of memory from the IFrame
	    // but it didn't seem to help
	    // Actually just removing it from the parent does cause the Java JRE
	    // to exit on FireFox within a few minutes.
//	    JavaScript.killFrame(frame.getElement());
	    frame.removeFromParent();
	    // tried the following but it didn't seem to help
//	    frame.setUrl("");
	    frame = null;
	}
    }
    
    @Override
    public void setVisible(boolean visible) {
	super.setVisible(visible);
	if (frame != null && visible) {
	    add(frame);
	    frame = null;
	}
    }
    
    @Override
    public void setDirty(boolean dirty) {
	super.setDirty(dirty);
	if (dirty) {
	    if (getModelGuid() != null && isAttached()) {
		// remove temporary model from data store
		BehaviourComposer.deleteModel(getModelGuid());
		BehaviourComposer.setLastModelGuid(null);
	    }    
	}
    }

    public void runDownloadSharePanelDisplayed(RunDownloadSharePanel runDownloadSharePanel) {
	if (runDownloadSharePanel != this)  {
	    if (isAttached() || runDownloadSharePanel.isShare()) {
		// another panel has been displayed that may provide URLs to the model
		// so it is no longer temporary
		BehaviourComposer.setLastModelGuid(null);
	    } else {
		// isn't being used -- presumably running connected to BC2NetLogo
		BehaviourComposer.setLastModelGuid(runDownloadSharePanel.getModelGuid());
	    }
	}	
    }

    public boolean runEvenIfClean() {
	return Modeller.instance().getNetLogo2BCChannelToken() != null;
    }
    
    @Override
    public void setModelGuid(String modelGuid) {
        super.setModelGuid(modelGuid);
        if (modelGuid == null) {
            releaseFrame();
        }
    }

    public void removeWaitingMessage() {
	waitingMessage.removeFromParent();	
    }

}
