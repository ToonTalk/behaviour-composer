/**
 * 
 */
package uk.ac.lkl.client;

/**
 * DownloadPanel has its own class since each model sent to NetLogo via BC2NetLogo does not need to be kept
 * 
 * @author Ken Kahn
 *
 */
public class DownloadPanel extends RunDownloadSharePanel {
    
    public DownloadPanel() {
	super(false, false);
    }

    @Override
    public void setDirty(boolean dirty) {
	super.setDirty(dirty);
	if (dirty && Modeller.instance().getNetLogo2BCChannelToken() != null) {
	    String modelGuid = BehaviourComposer.getRunPanel().getModelGuid();
	    if (modelGuid != null) {
		// remove temporary model from data store
		BehaviourComposer.deleteModel(modelGuid);
		BehaviourComposer.setLastModelGuid(null);	
	    }    
	}
    }

}
