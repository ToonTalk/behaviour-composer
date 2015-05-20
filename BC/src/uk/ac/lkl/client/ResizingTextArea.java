/**
 * 
 */
package uk.ac.lkl.client;

import uk.ac.lkl.client.composer.CustomisationPopupPanel;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Subclass of GWT's TextArea that adjusts its size automatically
 * 
 * @author Ken Kahn
 *
 */
public class ResizingTextArea extends TextArea {
    
    private static final int DEFAULT_MAXIMUM_WIDTH = 1000;
    protected int minCharacterWidth = 3;
    protected int maxCharacterWidth = DEFAULT_MAXIMUM_WIDTH;
    
    public ResizingTextArea() {
	super();
	KeyUpHandler handler = new KeyUpHandler() {

	    @Override
	    public void onKeyUp(KeyUpEvent event) {
		updateSize();		
	    }
	    
	};
	addKeyUpHandler(handler);
    }
    
    @Override
    public void setText(String text) {
	super.setText(text);
	updateSize();
    }

    public void updateSize() {
	String text = getText();
	if (text != null) {
	    // length of text with a minimum of length+1 and maximum specified in source page
	    Integer[] lineCountAndLineMax = CommonUtils.lineCountAndLineMax(text, getMaxCharacterWidth());
	    int characterWidth = Math.max(minCharacterWidth, Math.min(maxCharacterWidth, lineCountAndLineMax[1]+1));
	    setCharacterWidth(characterWidth);
	    if (lineCountAndLineMax[0] == 1) {
		setVisibleLines(Math.max(1, (int) Math.ceil(lineCountAndLineMax[1]/(double) characterWidth)));
	    } else {
		// following added an extra line when nearly never needed
//		if (Utils.getAncestorWidget(this, CustomisationPopupPanel.class) != null) {
//		    // following prevents embedded vertical scroll bars
//		    lineCountAndLineMax[0]++;
//		}
		setVisibleLines(lineCountAndLineMax[0]);
	    }
	} else {
	    setCharacterWidth(minCharacterWidth);
	}
    }
    
    @Override
    public void onLoad() {
	super.onLoad();
	CustomisationPopupPanel customisationPanel = Utils.getAncestorWidget(this, CustomisationPopupPanel.class);
	if (customisationPanel != null) {
	    // don't have as much room inside a customisation panel
	    // Scrolling works in FireFox, IE, and Opera but not Chrome and Safari
	    // See Issue 676
	    setMaxCharacterWidth(customisationPanel.getOffsetWidth()/11);
	} else {
	    setMaxCharacterWidth(DEFAULT_MAXIMUM_WIDTH);
	}
	updateSize();
    }

    public int getMaxCharacterWidth() {
        return maxCharacterWidth;
    }

    public void setMaxCharacterWidth(int maxCharacterWidth) {
        this.maxCharacterWidth = maxCharacterWidth;
    }

    public int getMinCharacterWidth() {
        return minCharacterWidth;
    }

    public void setMinCharacterWidth(int minCharacterWidth) {
        this.minCharacterWidth = minCharacterWidth;
    }

}
