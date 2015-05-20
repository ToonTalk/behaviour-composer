package uk.ac.lkl.client;

import java.util.ArrayList;

import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class ClosableTab extends HorizonalPanelWithDebugID {
    private final static int MAXIMUM_TAB_LENGTH = 12;
    protected Image closeButton = new CloseButton();
    protected Widget tabContents = null;
    protected TabLabel html = null;
    protected int originalWidth = 0;
    
    // used for producing up-to-date informative titles
    protected MicroBehaviourView microBehaviour = null;
    private String nameHTML;
    private boolean abbreviated;
    
    /**
     * @param htmlString-- HTML string labelling this tab
     * @param tabContents -- widget that this tab contains
     * @param tabPanel -- TabPanel this is part of
     */
    public ClosableTab(String htmlString, final Widget tabContents, final ModellerTabPanel tabPanel) {
	super();
	setSpacing(4);
	this.tabContents = tabContents;
	htmlString = URL.decodePathSegment(htmlString);
	this.html = new TabLabel(htmlString);
	add(html);
	add(closeButton);
	setTabName(htmlString);
	closeButton.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		int index = tabPanel.getWidgetIndex(tabContents);
		if (index >= 0) {
		    // If the tab contains a Java applet then IE gets stuck
		    // unless the tab is selected first
		    tabPanel.selectTab(index);
		    if (tabPanel.remove(tabContents)) {
			tabPanel.selectTab(Math.min(tabPanel.getWidgetCount()-1, index));
		    }
		    if (tabContents instanceof BrowsePanel) {
			((BrowsePanel) tabContents).panelClosed(false);
		    } else if (tabContents instanceof ErrorsPanel) {
			Modeller.removeAlerts();
			Modeller.clearAlertsLine();
		    }
		}
	    }});
	setStylePrimaryName("modeller-closeable-tab");
	// TODO: replace the following with addMouseOverHander
	DOM.sinkEvents(getElement(), Event.ONMOUSEOVER);
    }
    
    @Override
    public void onLoad() {
	super.onLoad();
	resize();
	// work around a strange problem with TabLayoutPanel that non-HTML tabs are raised up
	getParent().addStyleName("modeller-closeable-tab");
	if (Modeller.instance().isTranslateEnabled()) {
	    final String currentHTML = html.getHTML();
	    Timer timer = new Timer() {

		@Override
		public void run() {
		    // hack to fix the length of the tab name if translated
		    String newHTML = html.getHTML();
		    if (!currentHTML.equals(newHTML)) {
			if (CommonUtils.htmlStringToText(newHTML).length() > MAXIMUM_TAB_LENGTH) {
			    setTabName(newHTML);
			}
			cancel();
		    }
		}
		
	    };
	    timer.scheduleRepeating(1000);
	}
    }

    protected void resize() {
	int buttonLeft = html.getOffsetWidth()+4;
	setPixelSize(buttonLeft+closeButton.getOffsetWidth(), html.getOffsetHeight());
//	setWidgetPosition(html, 0, 0);
//	setWidgetPosition(closeButton, buttonLeft, 0);
    }
    
    @Override
    public void onBrowserEvent(Event event) {
	int eventType = DOM.eventGetType(event);
	if (eventType == Event.ONMOUSEOVER) {
	    String title;
	    if (abbreviated) {
		String fullText = CommonUtils.htmlStringToText(getTabName());
		title = fullText + " -- ";
	    } else {
		title = "";
	    }
	    String macroBehaviourName = null;
	    if (microBehaviour != null) {
		macroBehaviourName = microBehaviour.getMacroBehaviourName();
		if (macroBehaviourName != null) {
		    title += CommonUtils.upperCaseFirstLetter(Modeller.constants.in()) + " " + macroBehaviourName;
		}
	    } 
	    if (macroBehaviourName == null) {
		title += Modeller.constants.clickToSelectThisTab();
	    }
	    setTitle(title);
	}
	super.onBrowserEvent(event);
    }
    
    public String getTabName() {
	return nameHTML;
    }
    
    public void setTabName(String nameHTML) {
	nameHTML = nameHTML.replace(" ", "&nbsp;");
	nameHTML = nameHTML.replace("-", "&nbsp;");
	nameHTML = nameHTML.replace("<DIV>", "&nbsp;");
	nameHTML = nameHTML.replace("<div>", "&nbsp;");
	nameHTML = nameHTML.replace("<BR>", "&nbsp;");
	nameHTML = nameHTML.replace("<br>", "&nbsp;");
	nameHTML = nameHTML.replace("<BR/>", "&nbsp;");
	nameHTML = nameHTML.replace("<br/>", "&nbsp;");
	this.nameHTML = nameHTML;
	html.setHTML(nameHTML);
	String fullText = CommonUtils.htmlStringToText(nameHTML);
	if (fullText.length() > MAXIMUM_TAB_LENGTH) {
	    fullText = CommonUtils.replaceNonBreakingSpaces(fullText);
	     // split by any white space -- ignore empty lines
	    ArrayList<String> words = CommonUtils.removeEmptyLines(fullText.split("(\\s)+"));
	    String abbreviatedName = words.get(0);
	    int index = 1;
	    while (index < words.size() && abbreviatedName.length()+words.get(index).length() < MAXIMUM_TAB_LENGTH) {
		abbreviatedName += "&nbsp;" + words.get(index);
		index++;
	    }
	    if (index < words.size()) {
		if (Modeller.instance().isTranslateEnabled()) {
		    // space is more precious when translated -- also not sure that ... works for all languages
		    html.setHTML(abbreviatedName);
		} else {
		    html.setHTML(abbreviatedName + "...");
		}
		abbreviated = true;
	    }
	} else {
	    abbreviated = false;
	}
	if (isAttached()) {
	    resize();
	}
    }

    public void setMicroBehaviour(MicroBehaviourView microBehaviour) {
        this.microBehaviour = microBehaviour;
    }

}
