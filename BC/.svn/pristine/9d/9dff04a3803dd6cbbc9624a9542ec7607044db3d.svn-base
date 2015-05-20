package uk.ac.lkl.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Widget;

import uk.ac.lkl.client.event.ModellerEvent;

public class BrowserHistory {
    
    static public void enableHistory() {
	History.addValueChangeHandler(new ValueChangeHandler<String>() {

	    @Override
	    public void onValueChange(ValueChangeEvent<String> event) {
		String historyToken = event.getValue();
		if (historyToken.equals(ModellerEvent.addingHistoryToken)) {
		    ModellerEvent.addingHistoryToken = "";
		    return;
		}
//		Window.alert("token: " + historyToken + " addingHistoryToken: " + ModellerEvent.addingHistoryToken);
		String browsedToString = Modeller.constants.browsedTo();
		if (historyToken.startsWith(browsedToString)) {
		    Widget widget = Modeller.getSelectedTab();
		    if (widget instanceof BrowsePanel) {
			BrowsePanel browsePanel = (BrowsePanel) widget;
			if (browsePanel.getIncrementalLoadResourcePage() != null) {
			    browsePanel.getIncrementalLoadResourcePage().cancel();
			}
			Modeller.browseToFromHistory(historyToken.substring(browsedToString.length() + 1));
			// + 1 to include the space
		    }
		} else {
		    Modeller.INSTANCE.undoUpTo(historyToken, true, false, Modeller.getDummyContinuation());
		}	
	    }
	});
    }

}
