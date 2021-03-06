package uk.ac.lkl.client;

import java.util.ArrayList;

public class HistoryPanelContents extends VerticalPanelWithDebugID {
    
    private ArrayList<HistoryItem> historyItems =
	new ArrayList<HistoryItem>();
    
    private int currentIndex = 0;
    
    public HistoryPanelContents() {
	super();
//	Utils.updateWidthWhenAttached(this);
    }
    
    public void addToHistoryPanel(HistoryItem historyItem) {
	insert(historyItem, 0);
	historyItems.add(historyItem);
    }
    
    public int historySize() {
	return getWidgetCount();
    }
    
    public int historyTokenIndex(String historyToken) {
	if (historyToken == null) {
	    return -1;
	}	
	int count = historySize();
	// search from the end so the most recent one
	// is returned if two have the same token
	// TODO: should avoid all name conflicts
	for (int i = count-1; i >= 0; i--) {
	    HistoryItem historyItem = historyItems.get(i);
	    if (historyItem.matches(historyToken)) {
		return i;
	    }
	}
	return -1;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public HistoryItem getHistoryItem(int index) {
	if (index < 0 || index >= historyItems.size()) {
	    return null;
	}
	return historyItems.get(index);
    }
    
    public int getTotalHistoryCount() {
	return historyItems.size();
    }

}
