/**
 * 
 */
package uk.ac.lkl.client;

import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * 
 * Implements area where user can document a model
 * 
 * @author Ken Kahn
 *
 */

// Could enhance this using a Markdown converted -- see http://pathfindersoftware.com/2007/12/markdown-suppor/
// Or https://code.google.com/p/pagedown/wiki/PageDown
// http://dillinger.io/
// http://markitup.jaysalvat.com/downloads/demo.php?id=markupsets/markdown
// https://github.com/coreyti/showdown

public class InfoTabTextArea extends ResizingTextArea {
    
    public InfoTabTextArea() {
	super();
	minCharacterWidth = 40;
	setTitle(Modeller.constants.infoTabTextAreaTitle());
	setStylePrimaryName("modeller-info-tab-text-area");
    }
    
    public String getContents() {
	return getText();
    }
    
    public void setContents(String contents) {
	String oldContents = getText();
	if (contents == null || contents.isEmpty()) {
	    contents = CommonUtils.getDefaultInfoTabContents();
	}
	setText(contents);
	if (oldContents == null || oldContents.isEmpty()) {
	    // initialisation
	    ValueChangeHandler<String> valueChangeHandler = new ValueChangeHandler<String>() {

		@Override
		public void onValueChange(ValueChangeEvent<String> event) {
		    String newValue = getText().trim();
		    updateSessionInformation(newValue);
		}

	    };
	    addValueChangeHandler(valueChangeHandler);
	}
    }
    
    /**
     * @param userGuid
     * @param sessionGuid
     * @param checkBox
     * @param newHTML
     */
    protected void updateSessionInformation(final String newInfoTab) {
	final AsyncCallbackNetworkFailureCapable<String> callback = new AsyncCallbackNetworkFailureCapable<String>() {
	    
	    @Override
	    public void onSuccess(String result) {
		super.onSuccess(result);
		if (result != null) {
		    Modeller.addToErrorLog(result);
		}
	    }
	};
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		updateSessionInfoTab(newInfoTab, callback);
	    }
	    
	});
    }
    
    private void updateSessionInfoTab(String newInfoTab, AsyncCallbackNetworkFailureCapable<String> callback) {
	Modeller.getHistoryService().updateSessionInformation(BehaviourComposer.getOriginalSessionGuid(), 
		                                              Modeller.userGuid, 
		                                              null, 
		                                              newInfoTab, 
		                                              null, // ignore visibility flag
		                                              callback);
    }

}
