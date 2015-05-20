package uk.ac.lkl.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

public class TopLevelPanel extends VerticalPanelWithDebugID {
    int kindOfChange;
    int mouseCurrentX, mouseCurrentY;
    boolean mouseButtonDown = false;
    // private ArrayList keysDown = new ArrayList();
    private final float keysDown[] = new float[256];

    public TopLevelPanel() {
	super();
	sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS | Event.KEYEVENTS);
    }

    public void onBrowserEvent(Event event) {
	super.onBrowserEvent(event);
	int eventType = DOM.eventGetType(event);
	switch (eventType) {
	case Event.ONMOUSEMOVE:
	    setMouseCurrentX(event.getClientX());
	    setMouseCurrentY(event.getClientY());
	    break;
	case Event.ONKEYDOWN:
	    setKeyDown(event.getKeyCode(), true);
	    break;
	case Event.ONKEYUP:
	    setKeyDown(event.getKeyCode(), false);
	    break;
	case Event.ONMOUSEDOWN:
	    setMouseButtonDown(true);
	    break;
	case Event.ONMOUSEUP:
	    setMouseButtonDown(false);
	    break;
	}
    }

    public int getMouseCurrentX() {
	return mouseCurrentX;
    }

    protected void setMouseCurrentX(int mouseCurrentX) {
	this.mouseCurrentX = mouseCurrentX;
    }

    public int getMouseCurrentY() {
	return mouseCurrentY;
    }

    protected void setMouseCurrentY(int mouseCurrentY) {
	this.mouseCurrentY = mouseCurrentY;
    }

    public void setKeyDown(int keyCode, boolean down) {
	if (keyCode > 255) {
	    System.out.println("keyCode: " + keyCode
		    + " but setKeyDown only can handle codes less than 256.");
	    return;
	}
	if (down) {
	    keysDown[keyCode % 256] = 1.0f;
	} else {
	    keysDown[keyCode % 256] = 0.0f;
	}
    }

    public float getKeyDown(String keyName) {
	int keyCode;
	if (keyName.length() == 1) {
	    keyCode = keyName.charAt(0);
	    if (keyCode > 255) {
		System.out.println("keyCode: "
				   + keyCode
				   + " but getKeyDown only can handle codes less than 256.");
		return 0.0f;
	    } else {
		return keysDown[keyCode % 256];
	    }
	} else if (keyName.equalsIgnoreCase("mouse")) {
	    return isMouseButtonDown() ? 1.0f : 0.0f;
	} else {
	    // what about all sorts of special keys?
	    Modeller.addToErrorLog("Can't handle keyDown(" + keyName + ")");
	    return 0.0f;
	}
    }

    public boolean isMouseButtonDown() {
	return mouseButtonDown;
    }

    protected void setMouseButtonDown(boolean mouseButtonDown) {
	this.mouseButtonDown = mouseButtonDown;
    }

}
