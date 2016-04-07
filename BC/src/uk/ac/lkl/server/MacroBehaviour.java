package uk.ac.lkl.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.server.persistent.MacroBehaviourData;
import uk.ac.lkl.server.persistent.MicroBehaviourData;
import uk.ac.lkl.shared.CommonUtils;

import org.w3c.dom.*;

public class MacroBehaviour {
    protected ArrayList<MicroBehaviour> microBehaviours = new ArrayList<MicroBehaviour>();
    protected ArrayList<MicroBehaviour> inactiveMicroBehaviours = new ArrayList<MicroBehaviour>();
    protected boolean active = true;
    protected boolean addToModel = true;
    protected boolean visibleInModel = true;
    protected String objectName = null;
    protected NetLogoModel netLogoModel;
    // the same micro-behaviour may be active in one macro-behaviour and inactive in another
    protected HashMap<MicroBehaviour, Boolean> microBehaviourActiveFlags = new HashMap<MicroBehaviour, Boolean>();
    protected ResourcePageServiceImpl resourcePageServiceImpl = null;
    static private int unnamedCounter = 1;
    protected String instanceCountExpression = "1";
    
    // to prevent infinite recursions when
    // a micro-behaviour references itself
    private boolean generatingNetLogoCode = false;
    private MacroBehaviourData macroBehaviourData;
    private String nameHTML;
    private String quotedBehaviourNames;
    
    public MacroBehaviour(String nameHTML, NetLogoModel netLogoModel, ResourcePageServiceImpl resourcePageServiceImpl) {
	super();
	this.nameHTML = nameHTML;
	objectName = CommonUtils.removeHTMLMarkup(nameHTML);
	// following is the name needed if we switch to making this a breed name
//	objectName = CommonUtils.onlyValidNetLogoCharacters(CommonUtils.getInnerText(objectNameHTML));
	if (objectName.isEmpty()) {
	    objectName = "unnamed-prototype-" + unnamedCounter ++;
	}
	this.netLogoModel = netLogoModel;
	this.resourcePageServiceImpl = resourcePageServiceImpl;
    }
    
    public MacroBehaviour copy(ArrayList<MacroBehaviour> macroBehavioursCopiesSoFar) {
	MacroBehaviour macroBehaviour = new MacroBehaviour(nameHTML, netLogoModel, resourcePageServiceImpl);
	macroBehavioursCopiesSoFar.add(macroBehaviour);
	for (MicroBehaviour microBehaviour : microBehaviours) {
	    macroBehaviour.add(microBehaviour.copy(macroBehavioursCopiesSoFar), macroBehaviour.isActive(microBehaviour));
	}
	return macroBehaviour;
    }
    
    protected ArrayList<MicroBehaviour> getMicroBehaviours() {
	return microBehaviours;
    }
    
    public void add(MicroBehaviour behaviour, boolean active) {
	if (behaviour != null && !microBehaviours.contains(behaviour)) {
	    behaviour.setNetLogoModel(netLogoModel);
	    microBehaviours.add(behaviour);
	    if (!active) {
		inactiveMicroBehaviours.add(behaviour);
	    }
	}
    }
    
    protected boolean processXMLNode(Node node, NetLogoModel netLogoModel) {
	if (node instanceof Element) {
	    Element element = (Element) node;
	    processFlags(element);
	}
	return processXMLNodes(node.getChildNodes(), netLogoModel);
    }
	
    protected boolean processXMLNodes(NodeList nodes, NetLogoModel netLogoModel) {
	int count = nodes.getLength();
	for (int i = 0; i < count; i++) {
	    Node node = nodes.item(i);
	    if (node instanceof Element) {
		Element element = (Element) node;
		String tag = element.getNodeName();
		if (tag == null) {
		    return false;
		} else if (tag.equals("microbehaviour")) {  
		    MicroBehaviour microBehaviour = 
			    processMicroBehaviourXML(element, netLogoModel, this);
		    add(microBehaviour, true);
		} else if (tag.equals("MacroBehaviourAsMicroBehaviour")) {
		    String nameHTML = ServerUtils.getCDATAElementString("name", element);
		    if (nameHTML != null) {
			add(new MacroBehaviourAsMicroBehaviour(nameHTML, netLogoModel), true);
		    } else {
			netLogoModel.warn("Expected to find a name element in a MacroBehaviourAsMicroBehaviour element.");
		    }
		} else if (tag.equals("instanceCount")) {
		    Node firstChild = element.getFirstChild();
		    if (firstChild != null) {
			instanceCountExpression = firstChild.getNodeValue();
		    } else {
			ServerUtils.logError("XML element instanceCount has no CDATA child. " + element.toString());
		    }
		} else if (tag.equals("macrobehaviour")) { 
		    // obsolete? -- retain for old models?
		    processFlags(element);
		    processXMLNode(element, netLogoModel);
		}
	    }
	}
	return true;
    }

    public static MicroBehaviour processMicroBehaviourXML(Element element, 
	                                                  NetLogoModel netLogoModel,
	                                                  MacroBehaviour macroBehaviour) {
	String sourceURL = getElementURL(element);
	if (sourceURL.equals("null")) { // not sure how these are created but ignore them
	    return null;
	}
	if (sourceURL.startsWith("-")) {
	    // is the representation for an inactivated micro behaviour
	    sourceURL = sourceURL.substring(1);
	}
	int idCounters[] = {0, 0, 0};
	ResourcePageServiceImpl resourcePageServiceImpl = netLogoModel.getResourcePageServiceImpl();
	MicroBehaviour microBehaviour = 
	    resourcePageServiceImpl.getMicroBehaviour(
		    sourceURL, CommonUtils.getBaseURL(sourceURL), netLogoModel.getClientState(), idCounters, null, false);
	if (microBehaviour == null) {
	    netLogoModel.warn("Could not fetch the micro-behaviour from " + sourceURL);
	    return null;
	} else {  
	    String activeString = element.getAttribute("active");
	    if (macroBehaviour != null) {
		boolean inactive = "false".equals(activeString);
		macroBehaviour.getMicroBehaviourActiveFlags().put(microBehaviour, !inactive);
	    }
	    // should only be one list of textareas so ignoring the rest
	    Element textAreasElement = (Element) getFirstNodeWithTagName(element, "textareas");
	    if (textAreasElement != null) {
		NodeList textAreaUpdateElements = textAreasElement.getChildNodes(); 
		//getElementsByTagName("textarea") isn't good because it'll find nodes recursively embedded
		int updateCount = textAreaUpdateElements.getLength();
		for (int j = 0; j < updateCount; j++) {
		    Node textAreaNode = textAreaUpdateElements.item(j);
		    if (textAreaNode instanceof Element) {
			Element updateElement = (Element) textAreaNode;
			String indexString = updateElement.getAttribute("index");
			try {
			    int index = Integer.parseInt(indexString);
			    String newContents = updateElement.getTextContent();
			    microBehaviour.updateTextArea(newContents, index);
			} catch (NumberFormatException e) {
			    netLogoModel.logException(e, "Expected to parse " + indexString + " as an integer.");
			}
		    }
		}
	    }
	    Element enhancementsElement = (Element) getFirstNodeWithTagName(element, "enhancements");
	    if (enhancementsElement != null) {
		String indicesString = enhancementsElement.getAttribute("indices");
		String originalTextAreasCountString = enhancementsElement.getAttribute("originalTextAreasCount");
		// this has the "truth" so throw away any old enhancements
		if (!indicesString.isEmpty()) {
		    try {
			int nextTextAreaIndex = Integer.parseInt(originalTextAreasCountString);
			String[] indicesStringList = indicesString.split(",");
			if (indicesStringList.length != microBehaviour.getEnhancements().size()) {
			    microBehaviour.resetEnhancements();
			    for (String indexString : indicesStringList) {
				MicroBehaviourEnhancement enhancement = 
					MicroBehaviourEnhancement.getEnhancement(Integer.parseInt(indexString));
				nextTextAreaIndex = microBehaviour.enhanceCode(enhancement, nextTextAreaIndex);
				microBehaviour.addEnhancement(enhancement);
			    }
			}
		    } catch (NumberFormatException e) {
			netLogoModel.logException(
				e, 
				"Expected to parse " + originalTextAreasCountString + " as an integer and " + 
				indicesString + " as comma separated integers");
		    }
		}
	    }
	    // there may be macro-behaviours on this micro-behaviour
	    Node macroBehavioursNode = getFirstNodeWithTagName(element, "macrobehaviours");
	    if (macroBehavioursNode != null) {
		microBehaviour.resetMacroBehaviours(); // the following is the latest "truth"
		MacroBehaviour macroBehaviourInMicroBehaviour = null;
		NodeList macroBehaviourNodes = macroBehavioursNode.getChildNodes();
		int macroBehaviourCount = macroBehaviourNodes.getLength();
		for (int j = 0; j < macroBehaviourCount; j++) {
		    Node macroBehaviourNode = macroBehaviourNodes.item(j);
		    if (macroBehaviourNode instanceof Element) {
			Element macroBehaviourElement = (Element) macroBehaviourNode;
			String macroBehaviourElementTag = macroBehaviourElement.getNodeName();
			if (macroBehaviourElementTag.equalsIgnoreCase("macrobehaviour")) {
			    NodeList macroBehaviourChildNodes = macroBehaviourElement.getChildNodes();
			    int length = macroBehaviourChildNodes.getLength();
			    for (int k = 0; k < length; k++) {
				Node macroBehaviourChildNode = macroBehaviourChildNodes.item(k);
				if (macroBehaviourChildNode instanceof Element) {
				    Element macroBehaviourChildElement = (Element) macroBehaviourChildNode;
				    String macroBehaviourChildNodeName = macroBehaviourChildElement.getNodeName();
				    if (macroBehaviourChildNodeName.equalsIgnoreCase("name")) {
					// should only be one of these -- probably not worth checking
					String name = macroBehaviourChildElement.getTextContent();
					macroBehaviourInMicroBehaviour = 
					    new MacroBehaviour(name, netLogoModel, resourcePageServiceImpl);
					microBehaviour.addMacroBehaviour(macroBehaviourInMicroBehaviour);
				    } else if (macroBehaviourChildNodeName.equalsIgnoreCase("microbehaviour")) {
					if (macroBehaviourInMicroBehaviour != null) {
					    MicroBehaviour microBehaviourInMacroBehaviour = 
						processMicroBehaviourXML(macroBehaviourChildElement, 
							                 netLogoModel, 
							                 macroBehaviourInMicroBehaviour);
					    if (microBehaviourInMacroBehaviour != null) {
						macroBehaviourInMicroBehaviour.add(microBehaviourInMacroBehaviour, true);
					    }
					} else {
					    netLogoModel.warn("XML for a micro-behaviour contains a micro-behaviour that doesn't have a name tag.");
					}
					macroBehaviourInMicroBehaviour.processFlags(macroBehaviourElement);
				    } else if (macroBehaviourChildNodeName.equalsIgnoreCase("MacroBehaviourAsMicroBehaviour")) {
					String nameHTML = ServerUtils.getCDATAElementString("name", macroBehaviourChildElement);
					if (nameHTML != null) {
					    macroBehaviourInMicroBehaviour.add(new MacroBehaviourAsMicroBehaviour(nameHTML, netLogoModel), true);
					} else {
					    netLogoModel.warn("Expected to find a name element in a MacroBehaviourAsMicroBehaviour element.");
					}
				    } else if (macroBehaviourChildNodeName.equalsIgnoreCase("macrobehaviour") &&
					       macroBehaviour != null) {
					// obsolete??
					macroBehaviour.processXMLNode(macroBehaviourChildElement, netLogoModel);
				    }
				}
			    }
			}
		    }
		}
	    }
	    microBehaviour.setNetLogoModel(netLogoModel);
	    // following is for backwards compatibility (models before version 7)
	    boolean dirty = element.getAttribute("dirty").equals("true");
	    if (dirty || (netLogoModel != null && 
		          !netLogoModel.isOnlyForFetchingCode() &&
		          !CommonUtils.hasChangesGuid(sourceURL))) {
		microBehaviour.updateMicroBehaviourData();
	    }
	    return microBehaviour;
	}
    }

    protected void processFlags(Element macroBehaviourElement) {
	String macroBehaviourActiveString = macroBehaviourElement.getAttribute("active");
	if ("false".equals(macroBehaviourActiveString)) {
	    setActive(false);
	}
	String macroBehaviourAddToModelString = macroBehaviourElement.getAttribute("addToModel");
	if ("false".equals(macroBehaviourAddToModelString)) {
	    setAddToModel(false);
	}
	String visibleInModelString = macroBehaviourElement.getAttribute("visibleInModel");
	if ("false".equals(visibleInModelString)) {
	    setVisibleInModel(false);
	}
    }
    
    public static String getElementURL(Element element) {
	Node urlNode = getFirstNodeWithTagName(element, "url");
	if (urlNode instanceof Element) { // must be since fetched via getFirstNodeWithTagName
	    Element urlElement = (Element) urlNode;
	    return urlElement.getTextContent();
	}
	// older convention
	return element.getAttribute("url");
    }
    
    public static Node getFirstNodeWithTagName(Node node, String tag) {
	// doesn't explore the entire tree as getElementsByTagName does
	// returns the first one found or null
	NodeList childNodes = node.getChildNodes();
	int length = childNodes.getLength();
	for (int i = 0; i < length; i++) {
	    Node childNode = childNodes.item(i);
	    if (tag.equalsIgnoreCase(childNode.getNodeName())) {
		return childNode;
	    }
	}
	return null;
    }

    public boolean isActive(MicroBehaviour microBehaviour) {
        Boolean isMicroBehaviourActiveBoolean = microBehaviourActiveFlags.get(microBehaviour);
        if (isMicroBehaviourActiveBoolean == null) {
            return !inactiveMicroBehaviours.contains(microBehaviour);
        } else {
            return isMicroBehaviourActiveBoolean.booleanValue();
        }
    }
    
    public boolean isActive() {
	return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isGeneratingNetLogoCode() {
        return generatingNetLogoCode;
    }

    public void setGeneratingNetLogoCode(boolean generatingNetLogoCode) {
        this.generatingNetLogoCode = generatingNetLogoCode;
    }

    public boolean isAddToModel() {
        return addToModel && !getInstanceCountExpression().equals("0");
    }

    public void setAddToModel(boolean addToModel) {
        this.addToModel = addToModel;
    }

    public boolean isVisibleInModel() {
        return visibleInModel;
    }

    public void setVisibleInModel(boolean visibleInModel) {
        this.visibleInModel = visibleInModel;
    }

    public MacroBehaviourData getMacroBehaviourData() {
	if (macroBehaviourData == null) {
	    List<MicroBehaviourData> microBehaviourData = new ArrayList<MicroBehaviourData>();
	    for (MicroBehaviour microBehaviour : microBehaviours) {
		MicroBehaviourData microBehaviourDatum = microBehaviour.getMicroBehaviourData();
		if (microBehaviourDatum != null) {
		    microBehaviourData.add(microBehaviourDatum);
		} // else there was an exception already reported
	    }
	    macroBehaviourData = new MacroBehaviourData(objectName, microBehaviourData);
	}	
	return macroBehaviourData;
    }

    public String getInstanceCountExpression() {
        return instanceCountExpression;
    }

    protected String getNameHTML() {
        return nameHTML;
    }

    public String getInitialisationCode() {
	String kindName = getObjectName();
	String objectInitialisation = 
		" create-objects " + getInstanceCountExpression() + "\n" +
		"  [set kind \"" + kindName + "\"\n" +
		"   initialise-object";
	if (!isVisibleInModel()) {
	    objectInitialisation += "\n   set my-visibility false";	
	}
	objectInitialisation += "]";
	return objectInitialisation;
    }

    public String getSetupCode() {
	String kindName = getObjectName();
	return " ask all-of-kind \"" + kindName + "\" \n  [ " + getBehaviourNames() + " ]";
    }
    
    public String getBehaviourNames() {
	if (quotedBehaviourNames == null) {
	    // not sure how this can happen but log showed an instance
	    return null;
	} else {
	    return quotedBehaviourNames.replaceAll("\"", "");
	}
    }

    public String getQuotedBehaviourNames() {
        return quotedBehaviourNames;
    }

    public void setQuotedBehaviourNames(String quotedBehaviourNames) {
        this.quotedBehaviourNames = quotedBehaviourNames;
    }

    protected HashMap<MicroBehaviour, Boolean> getMicroBehaviourActiveFlags() {
        return microBehaviourActiveFlags;
    }

    public boolean isCopyOfElementOf(List<MacroBehaviour> macroBehaviours) {
	String name = this.getNameHTML();
	for (MacroBehaviour macroBehaviour : macroBehaviours) {
	    if (name.equals(macroBehaviour.getNameHTML())) {
		return true;
	    }
	}
	return false;
    }

}
