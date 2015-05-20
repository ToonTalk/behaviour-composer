package uk.ac.lkl.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Composite;

public class RichTextEntry extends Composite {
    private RichTextArea richTextArea = new RichTextArea();
    private RichTextToolbar toolBar = new RichTextToolbar(richTextArea);
    private Button saveButton = new ModellerButton(Modeller.constants.save().toUpperCase());
    private Button cancelButton = new ModellerButton(Modeller.constants.cancel().toUpperCase());
    
    public RichTextEntry(final Widget editee) {
	this(editee, null, editee.getOffsetHeight());
    }
    
    public RichTextEntry(final Widget editee, String initialHTML, int height) {
	richTextArea.setStylePrimaryName("modeller-rich-text-entry");
	VerticalPanel panel = new VerticalPanel();
	panel.add(toolBar);
	panel.getElement().getStyle().setMarginRight(4, Unit.PX);
//	DOM.setStyleAttribute(panel.getElement(), "marginRight", "4px");
	if (initialHTML != null) {
	    richTextArea.setHTML(initialHTML);
	} else if (editee instanceof HasHTML) {
	    String html = ((HasHTML) editee).getHTML();
	    if (html != null) {
		richTextArea.setHTML(html);
	    }
	}
	panel.add(richTextArea);
	HorizontalPanel buttonPanel = new HorizontalPanel();
	saveButton.setWidth("50px");
	cancelButton.setWidth("80px");
	// visible only if there is an event handler
	saveButton.setVisible(false);
	cancelButton.setVisible(false);
	buttonPanel.add(saveButton);
	buttonPanel.add(cancelButton);
	buttonPanel.setSpacing(6);
	panel.add(buttonPanel);
	// following looks nicer but buttons can be hard to reach if horizontally scrolling
	// is going on
//	panel.setCellHorizontalAlignment(buttonPanel, HasHorizontalAlignment.ALIGN_CENTER);
	richTextArea.setFocus(true);
	// All composites must call initWidget() in their constructors.
	initWidget(panel);
	// add 50 to have a bit of a margin for growth and to minimise the need for
	// scroll bars but don't get too large
//	int windowWidth = Window.getClientWidth();
//	int richTextWidth = Math.min(windowWidth, windowWidth/2 + editee.getOffsetWidth());
	BrowsePanel containingBrowsePanel = editee == null ? null : Utils.getAncestorWidget(editee, BrowsePanel.class);
	if (containingBrowsePanel != null) {
	    int containerWidth = containingBrowsePanel.getOffsetWidth();
	    if (containerWidth > 100) {
		richTextArea.setWidth(containerWidth-30 + "px");
	    }
	}
	richTextArea.setHeight(height + "px");
	KeyPressHandler keyPressHandler = new KeyPressHandler() {

	    @Override
	    public void onKeyPress(KeyPressEvent event) {
		// if tab then don't add the tab but select the save button
		// enables accessibility without a mouse
		if (event.getCharCode() == '\t') {
		    richTextArea.setFocus(false);
		    saveButton.setFocus(true);
		    event.preventDefault();
		}	
	    }
	    
	};
	richTextArea.addKeyPressHandler(keyPressHandler);
	ensureDebugId(Integer.toString(Modeller.debugIDCounter++));
    }
    
    public void addSaveButtonClickHandler(ClickHandler addHandler) {
	saveButton.addClickHandler(addHandler);	
	saveButton.setVisible(true);
    }
    
    public void addCancelButtonClickHandler(ClickHandler cancelHandler) {
	cancelButton.addClickHandler(cancelHandler);	
	cancelButton.setVisible(true);
    }

    public RichTextArea getRichTextArea() {
        return richTextArea;
    }

    public String getHTML() {
	return richTextArea.getHTML();
    }

};
