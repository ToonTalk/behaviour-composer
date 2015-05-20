/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.user.client.ui.SplitLayoutPanel;

/**
 * @author Ken Kahn
 *
 */
public class ModellerSplitLayoutPanel extends SplitLayoutPanel {
    
    @Override
    public void onLoad() {
	super.onLoad();
	resize();
    }
    
    @Override
    public void onResize() {
	super.onResize();
	Modeller.instance().splitterMoved();
    }
    
    public void resize() {
	setPixelSize(Modeller.instance().getMainTabPanelWidth(false), 
		     // subtract a bit to look better (so no text is right at the bottom of the screen
		     Modeller.instance().getMainTabPanelHeight(false)-4);
//	int windowWidth = Window.getClientWidth();
//	int windowHeight = Window.getClientHeight();
//	int splitterSize = getSplitterSize();
//	if (Modeller.isSplitHorizontally()) {
//	    // subtract from the width due to the splitter widget itself
//	    setPixelSize(windowWidth-splitterSize, windowHeight);   
//	} else {
//	    // subtract from the height due to the splitter widget itself
//	    setPixelSize(windowWidth, windowHeight-splitterSize);   
//	}
    }
    
//    if (isSplitVertically()) {
//	// need to subtract a bit from the client width to avoid an unneeded slider
//	splitPanelOtherWidget.setWidth((clientWidth-30) + "px");
//	otherScrollPanel.setWidth(clientWidth-10 + "px");
////	restScrollPanelAfterSplit.setWidth(clientWidth-10 + "px");
//	wholePanel.setPixelSize(clientWidth-30, clientHeight/2);
//	splitPanel.setWidth(clientWidth + "px");
//	mainTabPanel.makeFullWidth();
//    } else if (isSplitHorizontally()) {
//	// need to subtract a bit from the client width to avoid an unneeded slider
//	int widgetHeight = clientHeight-25;
////	splitPanelOtherWidget.setPixelSize(clientWidth/2, widgetHeight);
//	wholePanel.setPixelSize(clientWidth/2, widgetHeight);
//	splitPanel.setPixelSize(clientWidth, widgetHeight);
//    }

}
