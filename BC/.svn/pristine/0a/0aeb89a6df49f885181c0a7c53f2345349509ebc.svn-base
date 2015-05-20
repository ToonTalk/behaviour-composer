package uk.ac.lkl.client.composer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import uk.ac.lkl.client.BehaviourComposer;
import uk.ac.lkl.client.BrowsePanel;
import uk.ac.lkl.client.BrowsePanelCommand;
import uk.ac.lkl.client.CreateDeltaPageCommand;
import uk.ac.lkl.client.MicroBehaviourComand;
import uk.ac.lkl.client.VerticalPanelWithDebugID;
import uk.ac.lkl.client.composer.MicroBehaviourView;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.client.event.AddMicroBehaviourEvent;
import uk.ac.lkl.client.event.RenameMacroBehaviourEvent;
import uk.ac.lkl.client.event.VisibleInModelEvent;
import uk.ac.lkl.shared.CommonUtils;
import uk.ac.lkl.shared.DeltaPageResult;

public class MacroBehaviourView extends VerticalPanelWithDebugID implements HasHTML {
    
    protected MacroBehaviourName name;
    
    protected HTML separator = new HTML("<hr></hr>");
    
    protected HTML spaceAtBottom = new HTML("&nbsp;");
    
    protected boolean active = true;
    
    protected boolean showMicroBehaviours = true;
    
    // where it is in the panel of prototypes
    protected int originalIndexPosition = -1;
    
//    protected ArrayList<MicroBehaviourView> microBehaviours = new ArrayList<MicroBehaviourView>();
    
    // keep the description since using getHTML can canonicalise it in different ways on different browsers
    protected String nameHTML = null;
    
    // if this is on a micro-behaviour page this is non-null
    protected String microBehaviourUrl = null;
    
    protected ArrayList<MacroBehaviourAsMicroBehaviourView> viewsSharingNameHTML =
	new ArrayList<MacroBehaviourAsMicroBehaviourView>();
   
    protected boolean addToModel = false;
    
    protected boolean visibleInModel = true;

    protected CheckBox invisibleCheckBox;

    protected HowManyInstancesPanel howManyInstancesWidget;
    
    protected String instanceCountExpression = "1";

    // following removed because not always up-to-date
    // (e.g. when a containing micro-behaviour is closed then re-opened)
    // instead collect these up from the children widgets
//    private ArrayList<MicroBehaviourView> microBehaviours = new ArrayList<MicroBehaviourView>();
//
//    protected NumberOfInstancesTextArea numberOfInstancesTextArea;

    private String alert;

    private String cursorStyle;
   
    // This is used to change the CSS style to give feedback when holding a micro-behaviour
    // possible memory leak??
    static public ArrayList<MacroBehaviourView> macroBehaviourViews = 
	new ArrayList<MacroBehaviourView>();
    
    public MacroBehaviourView(String nameHTML, String microBehaviourUrl) {
	super();
	// following does extra work that isn't necessary when copying a MacroBehaviourView
	// could optimise it
	setNameHTML(nameHTML);
	this.microBehaviourUrl = microBehaviourUrl;
	name = new MacroBehaviourName(nameHTML, this, microBehaviourUrl == null);
	add(name);
	add(separator);
	setSpacing(4);
	add(spaceAtBottom);
	setStylePrimaryName("modeller-MacroBehaviour");
	DOM.sinkEvents(getElement(), Event.ONCLICK);
	if (BehaviourComposer.microBehaviourWaitingToBeAdded != null) {
	    // created while a micro-behaviour is being added
	    // so change the CSS to give drop feedback
	    addMicroBehaviourCursor();
	}
    }
    
    public MacroBehaviourView(String nameHTML) {
	this(nameHTML, null);
    }
    
    public void addControlWidgets() {
	addHowManyInstancesWidget();
	addVisibleCheckBox();
    }
    
    public void addHowManyInstancesWidget() {
	if (howManyInstancesWidget != null) {
	    return;
	}
	howManyInstancesWidget = new HowManyInstancesPanel(this);
	insert(howManyInstancesWidget, 1);	
    }
    
    public void addVisibleCheckBox() {
	if (invisibleCheckBox != null) {
	    return;
	}
	invisibleCheckBox = new CheckBox(Modeller.constants.invisibleAgent());
	ClickHandler clickHandler = new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		if (BehaviourComposer.microBehaviourWaitingToBeAdded == null) {
		    // ignore the click if dropping a micro-behaviour here
		    boolean visible = !invisibleCheckBox.getValue();
		    setVisibleInModel(visible);
		    new VisibleInModelEvent(visible, MacroBehaviourView.this).addToHistory();
		}
	    }
	    
	};
	invisibleCheckBox.addClickHandler(clickHandler);
	invisibleCheckBox.setTitle(Modeller.constants.invisibleCheckBoxTitle());
	setAddToModel(true);
	// insert after the number of copies widget
	insert(invisibleCheckBox, 2);
    }
    
    public MacroBehaviourView copyFor(String microBehaviourUrl, 
	                              ArrayList<MicroBehaviourView> freshCopies) {
	MacroBehaviourView copy = new MacroBehaviourView(nameHTML, microBehaviourUrl);
	// if shareMicroBehaviourList the copy shares the same list of micro-behaviours (now and in the future)
	copy.addMicroBehaviours(getMicroBehaviours(), freshCopies);
	return copy;
    }
    
    @Override
    public void onBrowserEvent(Event event) {
	super.onBrowserEvent(event);
	if (event.getTypeInt() == Event.ONCLICK) {
	    int index = insertionIndex(event.getClientY()+Window.getScrollTop());
	    acceptOrWaitMicroBehaviourWaitingToBeAdded(index);
	}
    }
    
    protected int insertionIndex(int mouseY) {
	// Finds which micro-behaviour to insert a new one in immediately after
	// If mouseY is below all micro-behaviours then returns "infinity"
	int index = 0;
	int bestIndexSoFar = index;
	int separatorBottom = separator.getAbsoluteTop()+separator.getOffsetHeight();
	int bestDistanceSoFar = Math.abs(mouseY-separatorBottom);
	List<MicroBehaviourView> microBehaviours = getMicroBehaviours();
	for (MicroBehaviourView microBehaviourView : microBehaviours) {
	    index++;
	    int bottom = microBehaviourView.getAbsoluteTop()+microBehaviourView.getOffsetHeight();
	    int distance = Math.abs(mouseY-bottom);
	    if (distance < bestDistanceSoFar) {
		bestDistanceSoFar = distance;
		bestIndexSoFar = index;
	    }	    
	}
	if (bestIndexSoFar > 0 && bestIndexSoFar == microBehaviours.size()) {
	    return Integer.MAX_VALUE;
	} else {
	    return bestIndexSoFar;
	}
    }
    
    public boolean addMicroBehaviour(MicroBehaviourView behaviour, boolean warnIfAlreadyAdded, boolean fireChanges) {
	// insert at end
	return addMicroBehaviour(behaviour, Integer.MAX_VALUE, warnIfAlreadyAdded, fireChanges);
    }

    public boolean addMicroBehaviour(
	    final MicroBehaviourView behaviour, 
	    int insertionIndex, 
	    boolean warnIfAlreadyAdded, 
	    boolean fireChanges) {
	if (behaviour == null) {
	    return false;
	}
	// following commented out on 151113 because should always be a new micro-behaviour
//	if (getEquivalentMicroBehaviour(behaviour) == null) {
	if (getIdenticalBehaviour(behaviour) == null) {
	    // not already there
	    if (insertionIndex >= getMicroBehaviours().size()) {
		// add to the end but not after the spaceAtBottom
		int endInsertionIndex = getWidgetCount()-1;
		insert(behaviour, endInsertionIndex);
//		microBehaviours.add(behaviour);
//		getMicroBehaviours().add(behaviour);
	    } else {
		// need to insert after name and separator
		int indexOfFirstMicroBehaviour = indexOfWidget(separator)+1;
		if (indexOfFirstMicroBehaviour < 0) {
		    indexOfFirstMicroBehaviour = 0;
		}
		insert(behaviour, insertionIndex+indexOfFirstMicroBehaviour);
//		microBehaviours.add(insertionIndex, behaviour);
//		getMicroBehaviours().add(insertionIndex, behaviour);
	    }
	    behaviour.setContainingMacroBehaviour(this);
	    if (fireChanges) {
		macroBehaviourChanged();
	    }
	    if (!showMicroBehaviours) {
		Timer timer = new Timer() {

		    @Override
		    public void run() {
			behaviour.setVisible(false);		
		    }
		    
		};
		timer.schedule(2000);
	    }
	    return true;
	} else {
	    return false;
	}
//	} else {
//	    if (warnIfAlreadyAdded) {
//		Modeller.setAlertsLine(Modeller.constants.thisAgentAlreadyHasThisMicroBehaviour());
//	    }
//	    return false;
//	}
    }
    
//    public void removeMicroBehaviour(MicroBehaviourView behaviour) {
////	if (!getMicroBehaviours().remove(behaviour)) {
////	    GWT.log("removing a micro-behaviour that isn't there", null);
////	}
//	if (!remove(behaviour)) { // from VerticalPanel
//	    GWT.log("Removing a micro-behaviour that isn't there", null);
//	}
//	macroBehaviourChanged();
//    }
    
    public void removeAllMicroBehaviours(ArrayList<String> exceptions) {
	ArrayList<MicroBehaviourView> removed = new ArrayList<MicroBehaviourView>();
	for (MicroBehaviourView microBehaviour : getMicroBehaviours()) {
	    if (!exceptions.contains(microBehaviour.getPlainName())) {
		removeMicroBehaviour(microBehaviour);
		removed.add(microBehaviour);
	    }
	}
//	for (MicroBehaviourView removal : removed) {
//	    getMicroBehaviours().remove(removal);
//	}
	macroBehaviourChanged();
    }
    
    protected void macroBehaviourChanged() {
	BrowsePanel browsePanel = getContainingBrowsePanel();
	if (browsePanel != null) {
//	    if (CommonUtils.hasChangesGuid(browsePanel.getCurrentURL())) {
		browsePanel.copyMicroBehaviourWhenExportingURL();
//	    } else {
//		browsePanel.copyMicroBehaviour();
//	    }
	}
    }
       
    public String getModelXML(ArrayList<MicroBehaviourView> dirtyMicroBehaviours,
	                      ArrayList<MicroBehaviourView> seenBefore,
	                      int level) {
	StringBuilder xml = new StringBuilder("<macrobehaviour");
	if (!isActive()) {
	    xml.append(" active='false'");
	}
	if (!isAddToModel()) {
	    xml.append(" addToModel='false'");
	}
	if (!isVisibleInModel()) {
	    xml.append(" visibleInModel='false'");
	}
	xml.append("><name>"); 
	xml.append(CommonUtils.createCDATASection(name.getHTML()));
	xml.append("</name>");
	for (MicroBehaviourView microBehaviour : getMicroBehaviours()) {
	    microBehaviour.updateTextAreaValues();
	    xml.append(microBehaviour.getModelXML(dirtyMicroBehaviours, seenBefore, level));
	}
	xml.append("<instanceCount>");
	if (getInstanceCountExpression().trim().isEmpty()) {
	    setInstanceCountExpressionText("0");
	}
	xml.append(CommonUtils.createCDATASection(getInstanceCountExpression()));
	xml.append("</instanceCount>");
	xml.append("</macrobehaviour>");
	return xml.toString();
    }

    public void replaceURLs(String[] renamings) {
	for (MicroBehaviourView microBehaviour : getMicroBehaviours()) {
	    microBehaviour.replaceURLs(renamings);
	}
    }
       
//    public void renameAllMicroBehaviourViews(String url, String newName, 
//	                                     String newURL, 
//	                                     MicroBehaviourView newMicroBehaviour, 
//	                                     ArrayList<ModellerEvent> events) {
//	// copy the microBehaviours since renaming may change this list
//	for (MicroBehaviourView microBehaviour : new ArrayList<MicroBehaviourView>(getMicroBehaviours())) {
//	    String otherUrl = microBehaviour.getUrl();
//	    if (otherUrl.equalsIgnoreCase(url)) {
////		String currentName = microBehaviour.getHTML();
////		if (!currentName.equalsIgnoreCase(newName)) {
////		    microBehaviour.setHTML(newName);
////		    microBehaviour.setDatabaseID(newMicroBehaviour.getDatabaseID());
////		    events.add(new RenameMicroBehaviourEvent(microBehaviour, currentName));
////		}
////		microBehaviour.setUrl(newURL);
////		events.add(new EditMicroBehaviourEvent(microBehaviour, url));
//		RemoveMicroBehaviourEvent removeMicroBehaviourEvent = 
//		    new RemoveMicroBehaviourEvent(this, microBehaviour);
//		removeMicroBehaviourEvent.redo(false, false, null);
//		events.add(removeMicroBehaviourEvent);
//		AddMicroBehaviourEvent addMicroBehaviourEvent = 
//		    new AddMicroBehaviourEvent(this, newMicroBehaviour.copy());
//		addMicroBehaviourEvent.redo(false, false, Modeller.getDummyContinuation());
//		events.add(addMicroBehaviourEvent);
//	    }
//	}
//    }
    
    public void updateTextAreaOfAllMicroBehaviourViews(String url, String newContents, int indexInCode) {
	for (MicroBehaviourView microBehaviour : getMicroBehaviours()) {
	    String otherUrl = microBehaviour.getUrl();
	    if (url.equalsIgnoreCase(otherUrl)) {
		microBehaviour.updateTextArea(newContents, indexInCode);
	    }
	}
    }
    
    public void addMicroBehaviourCursor() {
	addMicroBehaviourCursor("modeller-holding-micro-behaviour");
    }

    public void addMicroBehaviourCursor(String cursorStyle) {
	restoreCursor();
	this.cursorStyle = cursorStyle;
	addStyleName(cursorStyle);
	name.addStyleName(cursorStyle);
	separator.addStyleName(cursorStyle);
	spaceAtBottom.addStyleName(cursorStyle);
	for (MicroBehaviourView microBehaviour : getMicroBehaviours()) {
	    microBehaviour.addStyleName(cursorStyle);
	}
    }
    
    public void restoreCursor() {
	if (cursorStyle == null) {
	    return;
	}
	removeStyleName(cursorStyle);
	name.removeStyleName(cursorStyle);
	separator.removeStyleName(cursorStyle);
	spaceAtBottom.removeStyleName(cursorStyle);
	for (MicroBehaviourView microBehaviour : getMicroBehaviours()) {
	    microBehaviour.removeStyleName(cursorStyle);
	}
	this.cursorStyle = null;
    }
    
    protected void acceptHTML(RichTextArea richText) {
	String oldName = getNameHTML();
	String nameHTML = richText.getHTML();
	// don't want the line breaks caused by <p> ... </p>
	setNameHTML(nameHTML);
	name.setHTML(getNameHTML());
	new RenameMacroBehaviourEvent(this, oldName).addToHistory();
    }
    
    public MicroBehaviourView getEquivalentMicroBehaviour(MicroBehaviourView microBehaviourView) {
	if (microBehaviourView.isMacroBehaviour()) {
	    String targetNameHTML = microBehaviourView.getNameHTML();
	    return getMacroBehaviourAsMicroBehaviourWithHTMLName(targetNameHTML);
	} else {
	    return getMicroBehaviourWithURL(microBehaviourView.getUrl(), false, false);
	}
    }
    
    public MicroBehaviourView getIdenticalBehaviour(MicroBehaviourView microBehaviourView) {
	if (microBehaviourView.isMacroBehaviour()) {
	    String targetNameHTML = microBehaviourView.getNameHTML();
	    return getMacroBehaviourAsMicroBehaviourWithHTMLName(targetNameHTML);
	} else {
	    return getMicroBehaviourWithSharedState(microBehaviourView.getSharedState());
	}
    }

    public MicroBehaviourView getMacroBehaviourAsMicroBehaviourWithHTMLName(String targetNameHTML) {
	for (MicroBehaviourView microBehaviourView : getMicroBehaviours()) {
	    if (microBehaviourView.isMacroBehaviourViewedAsMicroBehaviourNamed(targetNameHTML)) {
		return microBehaviourView;
	    } 
	}
	return null;
    }
        
    /**
     * @param urlString - url or token and name of macro-behaviour
     * @param considerOriginals
     * 		If true then returns a micro-behaviour that is a copy of the same micro-behaviour
     * 		if it is unique.
     * @return micro-behaviour with url or null
     */
    public MicroBehaviourView getMicroBehaviourWithURL(
	    String urlString, boolean considerOriginals, boolean considerMacroBehavioursInMicroBehaviours) {
	// if considerOriginals is true first look for an exact match
	// and if none then match 'originals' (micro-behaviour web page)
	MicroBehaviourView microBehaviour = getMicroBehaviourWithURL(urlString, false, true, false, null);
	if (microBehaviour == null && considerOriginals) {
	    return getMicroBehaviourWithURL(urlString, true, true, false, null);
	} else {
	    return microBehaviour;
	}
    }
    
    public boolean walkMicroBehaviourViews(MicroBehaviourComand command) {
	for (MicroBehaviourView behaviour : getMicroBehaviours()) {
	    if (!behaviour.walkMicroBehaviourViews(command)) {
		return false;
	    }
	}
	return true;
    }
    
    public MicroBehaviourView getMicroBehaviourWithSharedState(MicroBehaviourSharedState targetSharedState) {
	for (MicroBehaviourView behaviour : getMicroBehaviours()) {
	    MicroBehaviourSharedState sharedState = behaviour.getSharedState();
	    if (sharedState == targetSharedState) {
		return behaviour;
	    }
	}
	return null;
    }
    
    
    public MicroBehaviourView getMicroBehaviourWithOriginalURL(String urlString, boolean considerMacroBehavioursInMicroBehaviours) {
   	return getMicroBehaviourWithURL(urlString, false, considerMacroBehavioursInMicroBehaviours, true, null);
    }    
	    
    public MicroBehaviourView getMicroBehaviourWithURL(String urlStrings, 
	                                               boolean considerOriginals, 
	                                               boolean considerMacroBehavioursInMicroBehaviours,
	                                               boolean matchNetLogoProcedureNames,
	                                               ArrayList<MicroBehaviourView> microBehavioursAlreadyConsidered) {
	// need to keep track of microBehavioursAlreadyConsidered so don't loop in micro-behaviours refer to each other in a cycle
	if (urlStrings == null) {
	    return null;
	}
	String[] allURLs = urlStrings.split(";");
	for (String urlString : allURLs) {
	    urlString = URL.decode(urlString); // to be safe
	    String unmodifiedURLString = urlString;
	    if (urlString.charAt(0) == '-') {
		// is an inactivated micro-behaviour
		urlString = urlString.substring(1);
	    }
	    String prototypeName = CommonUtils.prototypeName(urlString);
	    if (prototypeName != null) {
		return getMacroBehaviourAsMicroBehaviourWithHTMLName(prototypeName);
	    }
	    String procedureName = CommonUtils.netLogoNameFromURL(urlString);
	    ArrayList<MicroBehaviourView> microBehaviours = getMicroBehaviours();
	    for (MicroBehaviourView behaviour : microBehaviours) {
		if (microBehavioursAlreadyConsidered == null || !alreadyConsidered(behaviour, microBehavioursAlreadyConsidered)) {
		    //		String otherUrlString = behaviour.getUrl();
		    List<String> otherUrlStrings = behaviour.getPreviousURLs();
		    //		if (otherUrlString.charAt(0) == '-') {
		    //		    // is an inactivated micro-behaviour
		    //		    otherUrlString = otherUrlString.substring(1);
		    //		}
		    for (String otherURL : otherUrlStrings) {
			if (otherURL.charAt(0) == '-') {
			    // is an inactivated micro-behaviour
			    otherURL = otherURL.substring(1);
			}
			if (otherURL.equals(urlString)) { 
			    // was startsWith -- triggered Issue 968 -- not clear why I used startsWith
			    // could restore the call to 'contains' below...
			    return behaviour;
			}
		    }
//		    if (otherUrlStrings.contains(urlString)) {
//			return behaviour;
//		    } 
		    if (urlString != unmodifiedURLString && otherUrlStrings.contains(unmodifiedURLString)) {
			return behaviour;
		    } else if (matchNetLogoProcedureNames) {
			if (procedureName != null) {
			    for (String otherUrlString : otherUrlStrings) {
				String previousProcedureName = CommonUtils.netLogoNameFromURL(otherUrlString);
				if (procedureName.equals(previousProcedureName)) {
				    return behaviour;
				}
			    }
			}
		    }
		    if (!CommonUtils.hasChangesGuid(urlString)) {
			// if is an 'original' URL then compare it with others without changes
			// the following caused newly opened micro-behaviours to clobber edited ones
			//		    if (urlString.equals(CommonUtils.removeBookmark(otherUrlString))) {
			//			return behaviour;
			//		    }
			//		} else if (!CommonUtils.hasChangesGuid(otherUrlString)) {
			// if micro-behaviour has an 'original' URL then compare it with target URL without guid
			//		    if (otherUrlString.equals(CommonUtils.removeBookmark(urlString))) {
			//			return behaviour;
			//		    }
		    } else if (considerOriginals) {
			String otherUrlString = behaviour.getUrl();
			if (CommonUtils.removeBookmark(otherUrlString).equals(CommonUtils.removeBookmark(urlString))) {
			    return behaviour;
			}
		    }
		    if (considerMacroBehavioursInMicroBehaviours) {
			ArrayList<MacroBehaviourView> macroBehaviourViews = behaviour.getMacroBehaviourViews();
			if (macroBehaviourViews != null && !macroBehaviourViews.isEmpty()) {
			    if (microBehavioursAlreadyConsidered == null) {
				microBehavioursAlreadyConsidered = new ArrayList<MicroBehaviourView>();
			    }
			    microBehavioursAlreadyConsidered.add(behaviour);
			    for (MacroBehaviourView macroBehaviourView : macroBehaviourViews) {
				MicroBehaviourView microBehaviour =
					macroBehaviourView.getMicroBehaviourWithURL(
						urlString, considerOriginals, considerMacroBehavioursInMicroBehaviours, matchNetLogoProcedureNames, microBehavioursAlreadyConsidered);
				if (microBehaviour != null) {
				    return microBehaviour;
				}
			    }
			}
		    }
		}
	    }
//	String localURL = Modeller.localVersionOfURLsIfRunningLocal(urlString);
//	if (localURL != urlString) {
//	    for (MicroBehaviourView behaviour : microBehaviours) {
//		String otherUrlString = behaviour.getUrl();
//		if (localURL.equals(otherUrlString)) {
//		    return behaviour;
//		} else if (localURL.equals(Modeller.localVersionOfURLsIfRunningLocal(otherUrlString))) {
//		    return behaviour;
//		}
//	    }
//	} else if (urlString.startsWith("file:")) {
//	    for (MicroBehaviourView behaviour : microBehaviours) {
//		String otherUrlString = behaviour.getUrl();
//		if (localURL.equals(Modeller.localVersionOfURLsIfRunningLocal(otherUrlString))) {
//		    return behaviour;
//		}
//	    }
//	}
	    if (considerOriginals && CommonUtils.hasChangesGuid(urlString)) {
		MicroBehaviourView foundOne = null;
		String originalURL = CommonUtils.removeBookmark(urlString);
		for (MicroBehaviourView behaviour : microBehaviours) {
		    String otherUrlString = behaviour.getUrl();
		    if (CommonUtils.removeBookmark(otherUrlString).equals(originalURL)) {
			if (foundOne != null) { // found more than one so things are ambiguous
			    return null;
			} else {
			    foundOne = behaviour;
			}
		    }
		}
		return foundOne;
	    }
	}
	return null;	
    }
    
    public MicroBehaviourView getMicroBehaviourViewOfDeclaration(String newDeclaration, String oldDeclaration) {
	return getMicroBehaviourViewOfDeclaration(newDeclaration, oldDeclaration, new ArrayList<MicroBehaviourView>());
    }
    
    public MicroBehaviourView getMicroBehaviourViewOfDeclaration(String newDeclaration, String oldDeclaration, ArrayList<MicroBehaviourView> microBehavioursAlreadyConsidered) {
	oldDeclaration = oldDeclaration.trim();
	for (MicroBehaviourView behaviour : getMicroBehaviours()) {
	    if (!alreadyConsidered(behaviour, microBehavioursAlreadyConsidered) && behaviour.isActive()) {
		String url = behaviour.getUrl();
		if (url.contains("basic-library/miscellaneous/add-netlogo-declaration")) {
		    String currentDeclaration = behaviour.getTextAreaValue(0);
		    if (currentDeclaration == null) {
			System.err.println("Unable to fetch the current declaration of an instance of add-netlogo-declaration.");
		    } else if (currentDeclaration.trim().equals(oldDeclaration)) {
			return behaviour;
		    }   
		}
	    }
	}
	return null;
    }
		
    public MicroBehaviourView getMicroBehaviourViewOfWidget(String type, String name, Element element) {
	return getMicroBehaviourViewOfWidget(type, name, element, new ArrayList<MicroBehaviourView>());
    }
    
    public MicroBehaviourView getMicroBehaviourViewOfWidget(String type, String identifier, Element element, ArrayList<MicroBehaviourView> microBehavioursAlreadyConsidered) {
	for (MicroBehaviourView behaviour : getMicroBehaviours()) {
	    if (!alreadyConsidered(behaviour, microBehavioursAlreadyConsidered) && behaviour.isActive()) {
		String url = behaviour.getUrl();
		if (type.equals("PLOT")) {
		    // not completely general but the only graphing primitives are those from the standard libraries
		    if (url.contains("basic-library/graphing/create-empty-plot") ||
			url.contains("basic-library/graphing/create-plot") ||
			url.contains("basic-library/graphing/create-auto-plot") ||
			url.contains("basic-library/graphing/create-empty-auto-plot") ||
			url.contains("basic-library/graphing/create-histogram") || 
			// old library
			url.contains("CREATE-EMPTY-PLOT.html") ||
			url.contains("CREATE-PLOT.html") ||
			url.contains("CREATE-AUTO-PLOT.html") ||
			url.contains("CREATE-EMPTY-AUTO-PLOT.html") ||
			url.contains("CREATE-HISTOGRAM.html")) {
			String plotName = behaviour.getTextAreaValue(4);
			if (plotName == null) {
			    System.err.println("Could not find the name of a plot in a plotting micro-behaviour.");
			} else if (plotName.trim().equals(identifier)) {
			    return behaviour;
			}
		    }
		} else if (type.equals("MONITOR")) {
		    if (url.contains("basic-library/interface-gadgets/create-monitor") || url.contains("CREATE-MONITOR.html")) {
			String monitorLabel = behaviour.getTextAreaValue(0);
			if (monitorLabel == null) {
			    System.err.println("Could not find the name of a monitor in a create-monitor micro-behaviour.");
			} else if (monitorLabel.trim().equals(identifier)) {
			    return behaviour;
			}
		    }
		} else if (type.equals("CHOOSER")) {
		    if (url.contains("basic-library/interface-gadgets/create-chooser") || url.contains("CREATE-CHOOSER.html")) {
			String chooserVariable = behaviour.getTextAreaValue(0);
			if (chooserVariable == null) { 
			    System.err.println("Could not find the variable name of a chooser in a create-chooser micro-behaviour.");
			} else if (chooserVariable.trim().equals(identifier)) {
			    return behaviour;
			}
		    }
		} else if (type.equals("TEXTBOX")) {
		    if (url.contains("basic-library/interface-gadgets/create-text") || url.contains("CREATE-TEXT.html")) {
			String currentText = behaviour.getTextAreaValue(7);
			if (currentText == null) { 
			    System.err.println("Could not find the current text in a create-text micro-behaviour.");
			} else if (currentText.trim().equals(identifier)) {
			    return behaviour;
			}
		    }
		} else if (type.equals("BUTTON")) {
		    if (url.contains("basic-library/user-input/button") || url.contains("ADD-BUTTON.html") ||
			url.contains("basic-library/user-input/netlogo-button") || url.contains("NETLOGO-BUTTON.html") ||
			url.contains("basic-library/user-input/observer-button")) {
			String buttonLabel = behaviour.getTextAreaValue(4);
			if (buttonLabel == null) { 
			    System.err.println("Could not find the button label in an add-button micro-behaviour.");
			} else if (buttonLabel.trim().equals(identifier)) {
			    return behaviour;
			}
		    }
		} else if (type.equals("SLIDER") || type.equals("INPUTBOX")) {
		    if (url.contains("Parameter.html") || url.contains("DEFINE-PARAMETER.html")) {
			String variableName = behaviour.getTextAreaValue(0);
			if (variableName == null) { 
			    System.err.println("Could not find the variable name in a parameter micro-behaviour.");
			} else if (removeParentheticalText(variableName).trim().equals(identifier)) {
			    return behaviour;
			} else if (variableName.trim().equals(identifier)) {
			    return behaviour;
			}
		    }
		} else if (type.equals("SWITCH")) {
		    if (url.contains("Boolean-parameter.html") || url.contains("DEFINE-BOOLEAN-PARAMETER.html")) {
			String variableName = behaviour.getTextAreaValue(0);
			if (variableName == null) { 
			    System.err.println("Could not find the variable name in a parameter micro-behaviour.");
			} else if (removeParentheticalText(variableName).trim().equals(identifier)) {
			    return behaviour;
			} else if (variableName.trim().equals(identifier)) {
			    return behaviour;
			}
		    }
		} else if (type.equals("GRAPHICS-WINDOW-LOCATION")) {
		    if (url.contains("environment/world-location")) {
			return behaviour;
		    }
		} else if (type.equals("GRAPHICS-WINDOW-SIZE")) {
		    if (url.contains("environment/world-size")) {
			return behaviour;
		    }
		} else if (type.equals("GRAPHICS-WINDOW-PATCH-SIZE")) {
		    if (url.contains("environment/patch-size")) {
			return behaviour;
		    }
		} else if (type.equals("GRAPHICS-WINDOW-WRAP")) {
		    if (url.contains("environment/world-geometry")) {
			return behaviour;
		    }
		} else if (type.equals("GRAPHICS-WINDOW-VIEW-UPDATE")) {
		    if (url.contains("environment/view-update")) {
			return behaviour;
		    }
		} else if (type.equals("GRAPHICS-WINDOW-FRAME-RATE")) {
		    if (url.contains("environment/frame-rate")) {
			return behaviour;
		    }
		} else if (type.equals("GRAPHICS-SHOW-TICK-COUNTER")) {
		    if (url.contains("environment/show-tick-counter")) {
			return behaviour;
		    }
		} else if (type.equals("GRAPHICS-TICK-COUNTER-LABEL")) {
		    if (url.contains("environment/set-tick-counter-label")) {
			return behaviour;
		    }
		} else if (type.equals("OUTPUT")) {
		    if (url.contains("logging/log-area")) {
			return behaviour;
		    }
		}; 
		ArrayList<MacroBehaviourView> innerMacroBehaviours = behaviour.getMacroBehaviourViews();
		if (!innerMacroBehaviours.isEmpty()) {
		    for (MacroBehaviourView innerMacroBehaviour : innerMacroBehaviours) {
			MicroBehaviourView microBehaviourViewOfWidget = 
				innerMacroBehaviour.getMicroBehaviourViewOfWidget(type, identifier, element, microBehavioursAlreadyConsidered);
			if (microBehaviourViewOfWidget != null) {
			    return microBehaviourViewOfWidget;
			}
		    }
		}
	    }
	}
	return null;
    }

    private String removeParentheticalText(String variableName) {
	int openParenStart = variableName.indexOf('(');
	if (openParenStart < 0) {
	    return variableName;
	} else {
	    return variableName.substring(0, openParenStart);
	}
    }

    private boolean alreadyConsidered(MicroBehaviourView behaviour, ArrayList<MicroBehaviourView> microBehavioursAlreadyConsidered) {
	String url = behaviour.getUrl();
	for (MicroBehaviourView alreadyConsideredBehaviour : microBehavioursAlreadyConsidered) {
	    if (alreadyConsideredBehaviour.getUrl().equals(url)) {
		return true;
	    }
	}
	return false;
    }

    public String getHTML() {
	return name.getHTML();
    }
    
    public void setHTML(String html) {
	name.setHTML(html);
	setNameHTML(html);
    }
    
    public String getText() {
	return name.getText();
    }
    
    public void setText(String text) {
	name.setText(text);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            removeStyleName("modeller-MacroBehaviour-inactive");
        } else {
            addStyleName("modeller-MacroBehaviour-inactive");
        }
    }
    
    public void acceptOrWaitMicroBehaviourWaitingToBeAdded() {
	acceptOrWaitMicroBehaviourWaitingToBeAdded(Integer.MAX_VALUE);
    }
    
    public void acceptOrWaitMicroBehaviourWaitingToBeAdded(final int insertionIndex) {
	if (BehaviourComposer.microBehaviourWaitingToBeAdded == null) {
	    return;
	}
	if (BehaviourComposer.microBehaviourWaitingToBeAdded.isWaitingToBeCopied()) {
	    Command acceptWhenCopiedCommand = new Command() {

		@Override
		public void execute() {
		    acceptMicroBehaviourWaitingToBeAdded(insertionIndex);
		}
		
	    };
	    Modeller.instance().waitCursor();
	    BehaviourComposer.microBehaviourWaitingToBeAdded.setExecuteWhenCopied(acceptWhenCopiedCommand);
	    alert = Modeller.constants.mustWaitForServerBeforeAddingMicroBehaviour();
	    Modeller.addAlert(alert);
	} else {
	    Modeller.instance().restoreCursor();
	    Modeller.removeAlert(alert);
	    acceptMicroBehaviourWaitingToBeAdded(insertionIndex);
	}	
    }
    
    public void acceptMicroBehaviourWaitingToBeAdded(int insertionIndex) {
	Modeller.clearAlertsLine();
	Modeller.instance().restoreCursor();
	BrowsePanelCommand command = null;
//	if (BehaviourComposer.originalMicroBehaviourWaitingToBeAdded != null) {
//	    // need to update the browse panel that the micro-behaviour originally came from
//	    // (if there is one)
//	    final BrowsePanel containingBrowsePanelOfCriginalMicroBehaviour = 
//		BehaviourComposer.originalMicroBehaviourWaitingToBeAdded.getContainingBrowsePanel(true);
//	    if (containingBrowsePanelOfCriginalMicroBehaviour != null) {
//		ClosableTab tabWidget = containingBrowsePanelOfCriginalMicroBehaviour.getTabWidget();
//		if (tabWidget != null) {
//		    // the easiest way to ensure that the browse panel that 
//		    // started this is up-to-date after possible new URLs, etc
//		    // is to close it and open it again
//		    final int tabIndex = 
//			Modeller.resourcesTabPanel.getDeckPanel().getWidgetIndex(containingBrowsePanelOfCriginalMicroBehaviour);
//		    final BrowsePanel containingBrowsePanel = this.getContainingBrowsePanel();
//		    MicroBehaviourView microBehaviour = 
//			containingBrowsePanelOfCriginalMicroBehaviour.getMicroBehaviour();
//		    // if name has been changed (e.g. subscript added)
//		    // then restore it after retrieving the most recent version
//		    final String currentNameHTMLAndDescription = BehaviourComposer.microBehaviourWaitingToBeAdded.getNameHTMLAndDescription();
//		    command = new BrowsePanelCommand() {
//
//			@Override
//			public void execute(
//				BrowsePanel panel, 
//				String[] answer,
//				boolean panelIsNew) {
//			    // put new one where old one was
//			    Modeller.resourcesTabPanel.insert(panel, panel.getTabWidget(), tabIndex);
//			    // remove original now that the up-to-date one has been inserted
//			    containingBrowsePanelOfCriginalMicroBehaviour.removeFromParent();
//			    if (containingBrowsePanel != null) {
//				Modeller.resourcesTabPanel.switchTo(containingBrowsePanel);
//			    } else if (Modeller.resourcesTabPanel != Modeller.instance().getMainTabPanel()) {
//				Modeller.resourcesTabPanel.switchTo(panel);
//			    }
//			    BehaviourComposer.microBehaviourWaitingToBeAdded.setNameHTMLAndDescription(currentNameHTMLAndDescription);
//			    BehaviourComposer.microBehaviourWaitingToBeAdded = null;
//			}
//			
//		    };
//		    String currentURL = containingBrowsePanelOfCriginalMicroBehaviour.getCurrentURL();
//		    Modeller.browseToNewTab(
//			    tabWidget.getTabName(),
//			    microBehaviour.getTextAreaValues(),
//			    microBehaviour.getEnhancements(),
//			    microBehaviour.getMacroBehaviourViews(),
//			    currentURL,
//			    null,
//			    null,
//			    command,
//			    false,
//			    false,
//			    false,
//			    true);   
//		}
//	    }
//	    BehaviourComposer.originalMicroBehaviourWaitingToBeAdded = null;
//	}
	if (addMicroBehaviour(BehaviourComposer.microBehaviourWaitingToBeAdded, insertionIndex, true, true)) {
	    if (microBehaviourUrl == null || CommonUtils.hasChangesGuid(microBehaviourUrl)) {
		AddMicroBehaviourEvent addMicroBehaviourEvent = 
			new AddMicroBehaviourEvent(
				this, 
				BehaviourComposer.microBehaviourWaitingToBeAdded, 
				insertionIndex);
		addMicroBehaviourEvent.addToHistory();
	    } else {
		// hasn't been copied yet
		BehaviourComposer.microBehaviourWaitingToBeAdded.setCopyMicroBehaviourWhenExportingURL(true);
	    }
	}
	if (command == null) {
	    BehaviourComposer.microBehaviourWaitingToBeAdded = null;
	    Modeller.instance().restoreCursor();
	} // otherwise command will reset it
    }
    
    public boolean moveMicroBehaviour(MicroBehaviourView microBehaviour, boolean moveUp) {
	if (moveUp) {
	    return moveUp(microBehaviour);
	} else {
	    return moveDown(microBehaviour);
	}
    }
    
    public boolean moveUp(MicroBehaviourView microBehaviour) {
	// returns true if moved microBehaviour up in the list
	// or if first element moved to bottom
	ArrayList<MicroBehaviourView> microBehaviours = getMicroBehaviours();
	int size = microBehaviours.size();
	if (size == 1) {
	    // am the only one
	    return false;
	}
	MicroBehaviourView firstMicroBehaviour = microBehaviours.get(0);
	String url = microBehaviour.getUrl();
	if (firstMicroBehaviour.getUrl().equals(url)) {
	    add(microBehaviour);
	    microBehaviours.remove(0);
	    microBehaviours.add(microBehaviour);
	    macroBehaviourChanged();
	    return true;	    
	}
	int indexOfFirstMicroBehaviour = indexOfFirstMicroBehaviourView();
	for (int i = 1; i < size; i++) {
	    if (microBehaviours.get(i).getUrl().equals(url)) {
		MicroBehaviourView previousMicroBehaviour = microBehaviours.get(i-1);
//		microBehaviours.set(i-1, microBehaviour);
//		microBehaviours.set(i, previousMicroBehaviour);
		// use indices rather than the widget themselves in case they have been copied
		remove(i+indexOfFirstMicroBehaviour);
		remove(i+indexOfFirstMicroBehaviour-1);
		insert(microBehaviour, i+indexOfFirstMicroBehaviour-1);
		insert(previousMicroBehaviour, i+indexOfFirstMicroBehaviour);
		macroBehaviourChanged();
		return true;
	    }
	}
	return false;
    }
    
    public boolean moveDown(MicroBehaviourView microBehaviour) {
	// returns true if moved microBehaviour down in the list or
	// if is last element and moved it to the top
	ArrayList<MicroBehaviourView> microBehaviours = getMicroBehaviours();
	int size = microBehaviours.size();
	if (size == 1) {
	    // am the only one
	    return false;
	}
	int indexOfFirstMicroBehaviour = indexOfFirstMicroBehaviourView();
	String url = microBehaviour.getUrl();
	for (int i = 0; i < size-1; i++) {
	    if (microBehaviours.get(i).getUrl().equals(url)) {
		MicroBehaviourView nextMicroBehaviour = microBehaviours.get(i+1);
//		microBehaviours.set(i+1, microBehaviour);	
//		microBehaviours.set(i, nextMicroBehaviour);
		// use indices rather than the widget themselves in case they have been copied
		remove(i+indexOfFirstMicroBehaviour);
		remove(i+indexOfFirstMicroBehaviour);
		insert(microBehaviour, i+indexOfFirstMicroBehaviour);
		insert(nextMicroBehaviour, i+indexOfFirstMicroBehaviour);
		macroBehaviourChanged();
		return true;
	    }
	}
	insert(microBehaviour, indexOfFirstMicroBehaviour);
	microBehaviours.remove(size-1);
	microBehaviours.add(0, microBehaviour);
	macroBehaviourChanged();
	return true;
    }
    
    protected int indexOfWidget(Widget widget) {
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    if (this.getWidget(i) == widget) {
		return i;
	    }
	}
	return -1;
    }
    
    protected int indexOfFirstMicroBehaviourView() {
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    if (getWidget(i) instanceof MicroBehaviourView) {
		return i;
	    }
	}
	return -1;
    }
    
    public void fetchAndAddMicroBehaviour(String url, final int index) {
	final String goodURL = Utils.urlCheckingTabAttribute(url);
	if (getMicroBehaviourWithURL(goodURL, false, false) != null) {
	    Modeller.setAlertsLine(Modeller.constants.thisAgentAlreadyHasThisMicroBehaviour());
	    return;
	}
	final String loadingPleaseWait = Modeller.constants.loadingPleaseWait();
	Modeller.instance().waitCursor();
	Modeller.addAlert(loadingPleaseWait);
	BrowsePanelCommand command = new BrowsePanelCommand() {
	    @Override
	    public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
		Modeller.removeAlert(loadingPleaseWait);
		Modeller.instance().restoreCursor();
		panel.setCopyOnUpdate(true);
		MicroBehaviourView microBehaviour = panel.getMicroBehaviour();
		if (microBehaviour != null) {
		    BrowsePanel.addBrowsedURLs(goodURL);
		    CreateDeltaPageCommand createDeltaPageCallback =
			new CreateDeltaPageCommand() {

			    @Override
			    public void execute(MicroBehaviourView microBehaviourView, DeltaPageResult deltaPageResult, boolean panelIsNew, boolean subMicroBehavioursNeedNewURLs, boolean forCopying) {
				if (deltaPageResult.getErrorMessage() != null) {
				    Modeller.addToErrorLog(deltaPageResult.getErrorMessage());
				}
				String newURL = deltaPageResult.getNewURL();
				if (newURL != null) {
				    BrowsePanelCommand microBehaviourCopyCommand = 
					    new BrowsePanelCommand() {

					@Override
					public void execute(BrowsePanel panel, String[] answer, boolean panelIsNew) {
					    MicroBehaviourView microBehaviourCopy = panel.getMicroBehaviour();
					    if (microBehaviourCopy != null) {
						if (addMicroBehaviour(microBehaviourCopy, index, true, false)) {
						    AddMicroBehaviourEvent addMicroBehaviourEvent = 
							    new AddMicroBehaviourEvent(MacroBehaviourView.this, microBehaviourCopy);
						    addMicroBehaviourEvent.addToHistory();
						}
					    } else {
						Modeller.addToErrorLog(
							"Expected to receive a panel with a non-null micro-behaviour");
					    }
					}    
				    };
				    Modeller.executeOnNewMicroBehaviourPage(newURL, microBehaviourCopyCommand, true, true);
				}
			    }    
			};
			panel.createDeltaPage(microBehaviour,
				              microBehaviour.getNameHTML(),
			                      goodURL,
			                      microBehaviour.getTextAreaValues(),
			                      microBehaviour.getEnhancements(),
			                      microBehaviour.getMacroBehaviourViews(),
			                      false,
			                      createDeltaPageCallback,
			                      false);
//		    Modeller.tabPanel.remove(panel);
		} else {
		    Modeller.addToErrorLog(Modeller.constants.couldNotFindAMicroBehaviour() + " url=" + goodURL
			                   + " " + Modeller.constants.clickOnResourcesToSeeThePage());
		    // didn't work -- not very important
//		    Modeller.addWdigetToErrorLog(new CommandAnchor(Modeller.constants.resources()));
		    // leave the panel if there is an error
		    panel.setTemporary(false);
		    Modeller.mainTabPanel.switchTo(panel);
		}
//		Modeller.clearAlertsLine();
		Modeller.setAlertsLine("Behaviour added.");
	    }
	};
//	Log.info("fetchAndAddMicroBehaviour url: " + goodURL); // for debugging
	Modeller.executeOnMicroBehaviourPage(goodURL, command, true, true);
    }
    
    public BrowsePanel getContainingBrowsePanel() {
	Widget ancestor = getParent();
	while (ancestor != null) {
	    if (ancestor instanceof BrowsePanel) {
		return (BrowsePanel) ancestor;
	    }
	    ancestor = ancestor.getParent();
	}
	return null;
    }

    public int getOriginalIndexPosition() {
        return originalIndexPosition;
    }

    public void setOriginalIndexPosition(int originalIndexPosition) {
        this.originalIndexPosition = originalIndexPosition;
    }

    public String getNameHTML() {
        return nameHTML;
    }

    protected void setNameHTML(String description) {
        nameHTML = CommonUtils.removePTags(description);
        for (MacroBehaviourAsMicroBehaviourView macroBehaviourAsMicroBehaviourView : viewsSharingNameHTML) {
            macroBehaviourAsMicroBehaviourView.setHTML(nameHTML);
        }
    }
    
    public boolean isEmpty() {
	return getMicroBehaviours().isEmpty();
    }

    public ArrayList<MicroBehaviourView> getMicroBehaviours() {
	ArrayList<MicroBehaviourView> microBehaviours = new ArrayList<MicroBehaviourView>();
	for (Widget widget : getChildren()) {
	    if (widget instanceof MicroBehaviourView) {
		microBehaviours.add((MicroBehaviourView) widget);
	    }
	}
        return microBehaviours;
    }
    
    public ArrayList<MicroBehaviourView> getMicroBehavioursRecursively() {
	ArrayList<MicroBehaviourView> microBehaviours = new ArrayList<MicroBehaviourView>();
	addMicroBehavioursRecursively(microBehaviours);
        return microBehaviours;
    }

    public void addMicroBehavioursRecursively(ArrayList<MicroBehaviourView> microBehavioursSoFar) {
	for (MicroBehaviourView microBehaviour : getMicroBehaviours()) {
	    if (!microBehavioursSoFar.contains(microBehaviour)) {
		microBehavioursSoFar.add(microBehaviour);
		ArrayList<MacroBehaviourView> macroBehaviourViews = microBehaviour.getMacroBehaviourViews();
		for (MacroBehaviourView macroBehaviourView : macroBehaviourViews) {
		    macroBehaviourView.addMicroBehavioursRecursively(microBehavioursSoFar);
		}
	    }
	}	
    }

    protected void addMicroBehaviours(ArrayList<MicroBehaviourView> microBehaviours,
	                              ArrayList<MicroBehaviourView> freshCopies) {
        for (MicroBehaviourView behaviour : microBehaviours) {
	    MicroBehaviourView copy;
	    if (freshCopies == null) { // sharing, e.g. when loading/restoring
		copy = behaviour.copy();
	    } else {
		copy = behaviour.copyWithoutSharing(freshCopies);
	    }
	    copy.setContainingMacroBehaviour(this);
	    copy.setActive(behaviour.isActive());
	    addMicroBehaviour(copy, freshCopies == null);
        }
//        if (freshCopies == null) {
//            this.microBehaviours = microBehaviours;
//        }
    }
    
    protected void addMicroBehaviour(MicroBehaviourView microBehaviour, boolean shareMicroBehaviourList) {
	add(microBehaviour);
//	if (!shareMicroBehaviourList) {
//	    // TODO: determine if this should check first that a copy with the same URL isn't there already?
//	    microBehaviours.add(microBehaviour);
//	}
    }
    
    public void removeMicroBehaviour(MicroBehaviourView microBehaviour) {
	if (microBehaviour == null) {
	    return;
	}
	remove(microBehaviour);
//	String urlOfRemoved = microBehaviour.getUrl();
//	for (MicroBehaviourView addedMicroBehaviour : microBehaviours) {
//	    if (addedMicroBehaviour.getUrl().equals(urlOfRemoved)) {
//		microBehaviours.remove(addedMicroBehaviour);
//		break;
//	    }
//	}
	macroBehaviourChanged();
    }
    
    public boolean isOnAMicroBehaviour() {
	return microBehaviourUrl != null;
    }
    
    public String getMicroBehaviourUrl() {
        return microBehaviourUrl;
    }

    public void setMicroBehaviourUrl(String microBehaviourUrl) {
        this.microBehaviourUrl = microBehaviourUrl;
    }

    public boolean showMicroBehaviours() {
        return showMicroBehaviours;
    }

    public void setShowMicroBehaviours(boolean showMicroBehaviours) {
	if (this.showMicroBehaviours == showMicroBehaviours) {
	    return;
	}
	for (MicroBehaviourView behaviour : getMicroBehaviours()) {
	    behaviour.setVisible(showMicroBehaviours);
	}
	separator.setVisible(showMicroBehaviours);
        this.showMicroBehaviours = showMicroBehaviours;
    }
    
    public void setShowHideThisCheckBox(boolean visible) {
	if (invisibleCheckBox != null) {
	    invisibleCheckBox.setVisible(visible);
	}
    }
    
    public void setShowHowManyInstances(boolean visible) {
	if (howManyInstancesWidget != null) {
	    howManyInstancesWidget.setVisible(visible);
	}
    }
    
    public void addViewSharingNameHTML(MacroBehaviourAsMicroBehaviourView macroBehaviourAsMicroBehaviourView) {
	viewsSharingNameHTML.add(macroBehaviourAsMicroBehaviourView);
    }

    public void processFlags(Element element) {
	String activeString = element.getAttribute("active");
	if ("false".equals(activeString)) {
	    setActive(false);
	}
	String addToModelString = element.getAttribute("addToModel");
	if ("false".equals(addToModelString)) {
	    setAddToModel(false);
	}
	String visibleInModelString = element.getAttribute("visibleInModel");
	if ("false".equals(visibleInModelString)) {
	    setVisibleInModel(false);
	}
    }

    public boolean isAddToModel() {
        return addToModel;
    }

    public void setAddToModel(boolean addToModel) {
        this.addToModel = addToModel;
        if (!addToModel) {
            // old behaviour list loads as 0 copies
            this.setInstanceCountExpressionText("0");
        }
    }
    
    public int getAddToModelListBoxIndex() {
	return getAddToModelListBoxIndex(isAddToModel(), isVisibleInModel());
    }

    public int getAddToModelListBoxIndex(boolean addToModel, boolean visibleInModel) {
	if (addToModel) {
	    if (visibleInModel) {
		return 0;
	    } else {
		return 1;
	    }
	}
	return 2;
    }

    public boolean isVisibleInModel() {
        return visibleInModel;
    }

    public void setVisibleInModel(boolean visibleInModel) {
        this.visibleInModel = visibleInModel;
        if (invisibleCheckBox != null) {
            invisibleCheckBox.setValue(!visibleInModel);
        }
    }

    public String getInstanceCountExpression() {
        return instanceCountExpression;
    }

    public void setInstanceCountExpressionText(String instanceCountExpression) {
	if (instanceCountExpression == null) {
	    Utils.logServerMessage(Level.SEVERE, "Shouldn't set setInstanceCountExpressionText to null");
	    return;
	}
	if (!this.instanceCountExpression.equals(instanceCountExpression)) {
	    this.instanceCountExpression = instanceCountExpression;
	    if (howManyInstancesWidget != null) {
		howManyInstancesWidget.setText(instanceCountExpression);
	    }
	}
    }
    /**
     * @param searchItems
     * 
     * Adds to microBehaviours those reachable from here
     * @param depth 
     */

    public void addReachableMicroBehaviours(ArrayList<SearchResultsItem> searchItems, int depth) {
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    Widget widget = getWidget(i);
	    if (widget instanceof MicroBehaviourView) {
		MicroBehaviourView microBehaviourView = (MicroBehaviourView) widget;
		MicroBehaviourSearchItem microBehaviourSearchItem = new MicroBehaviourSearchItem(microBehaviourView, depth);
		if (!searchItems.contains(microBehaviourSearchItem)) {
		    searchItems.add(microBehaviourSearchItem);
		    microBehaviourView.addReachableMicroBehaviours(searchItems, depth+1);
		}
	    }
	}	
    }
    
    protected List<SearchResultsItem> getReachableMicroBehaviours() {
	ArrayList<SearchResultsItem> result = new ArrayList<SearchResultsItem>();
	addReachableMicroBehaviours(result, 0);
	return result;
    }
    
    @Override
    public void add(Widget w) {
	super.add(w);
	// force it to the bottom
	super.add(spaceAtBottom);
    }

//    public void setMicroBehaviours(ArrayList<MicroBehaviourView> microBehaviours) {
//        this.microBehaviours = microBehaviours;
//    }

    public String getMicroBehaviourNames() {
	String names = "";
	for (MicroBehaviourView microBehaviour : getMicroBehaviours()) {
	    names += microBehaviour.getNameHTML() + "; ";
	}
	if (names.isEmpty()) {
	    names = "No micro-behaviours";
	}
	names += ". ";
	if (microBehaviourUrl == null) {
	    names += "On the prototype named: " + getNameHTML();
	} else {
	    names += "On the micro-behaviour with the URL: " + microBehaviourUrl;
	}
	MicroBehaviourView microBehaviourView = Modeller.instance().getMicroBehaviourView(microBehaviourUrl);
	if (microBehaviourView != null) {
	    names += " and with name: " + microBehaviourView.getNameHTML();
	}
	return names;
    }
    
    public String getMicroBehaviourURLs() {
	String urls = "";
	for (MicroBehaviourView microBehaviour : getMicroBehaviours()) {
	    urls += microBehaviour.getUrl() + "; ";
	}
	if (urls.isEmpty()) {
	    urls = "No micro-behaviours";
	}
	if (microBehaviourUrl != null) {
	    urls += ". On the micro-behaviour with the URL: " + microBehaviourUrl;
	    MicroBehaviourView microBehaviourView = Modeller.instance().getMicroBehaviourView(microBehaviourUrl);
	    if (microBehaviourView != null) {
		urls += " and with name: " + microBehaviourView.getNameHTML();
	    }
	}
	return urls;
    }

    public boolean containsURL(String url, boolean withoutChangesGuid) {
	for (MicroBehaviourView microBehaviour : getMicroBehaviours()) {
	    if (microBehaviour.containsURL(url, withoutChangesGuid)) {
		return true;
	    }
	}
	return false;
    }

}
