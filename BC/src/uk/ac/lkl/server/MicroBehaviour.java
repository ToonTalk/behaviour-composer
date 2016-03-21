package uk.ac.lkl.server;

import uk.ac.lkl.client.composer.MicroBehaviourEnhancement;
import uk.ac.lkl.server.persistent.BehaviourCode;
import uk.ac.lkl.server.persistent.DataStore;
import uk.ac.lkl.server.persistent.MacroBehaviourData;
import uk.ac.lkl.server.persistent.MicroBehaviourData;
import uk.ac.lkl.server.persistent.MicroBehaviourURLCopy;
import uk.ac.lkl.server.persistent.NetLogoNameSerialNumber;
import uk.ac.lkl.shared.CommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class MicroBehaviour {

    protected String behaviourCode;
    protected String originalBehaviourCode;
    protected String transformedBehaviourCode;
    protected String behaviourURL = null;
//    protected String behaviourName = null;
    protected String behaviourDescription;
    // above can be used within NetLogo while the following preserves rich text formatting
    protected String behaviourDescriptionAndNameHTML;
    protected ArrayList<MicroBehaviour> referencedMicroBehaviours = new ArrayList<MicroBehaviour>();
    protected ResourcePageServiceImpl resourcePageServiceImpl = null;
    protected NetLogoModel netLogoModel;
    // use a hash map rather than an array since the values can come in any order
    // and don't know here the full size
    protected HashMap<Integer, String> textAreaValues = new HashMap<Integer, String>();
    protected ArrayList<String> textAreaElements = null; 
    protected List<MicroBehaviourEnhancement> enhancements = new ArrayList<MicroBehaviourEnhancement>();
    protected ArrayList<MacroBehaviour> macroBehaviours;
    
    // to avoid infinite recursions when
    // a micro-behaviour refers to itself
    private boolean gettingBehaviourCode = false;
    // micro-behaviour may be shared so client state shouldn't be
//    protected ClientState clientState;
    private MicroBehaviourData microBehaviourData;
    private boolean enhancementsInstalled = false;
    private String netLogoName = null;
    
    private static int unnamedCounter = 1;
    
    protected static String[] operationsExpectingABracketedExpression = 
	{ "do-now", "do-every", "do-every-dynamic", "do-after", "do-with-probability", "do-with-probabilities",
	  "do-repeatedly", "do-for-n", "do-at-time", "do-at-setup", "do-after-setup",
	  "do-if", "do-if-else", "ask-every-patch", "when", "whenever", "anyone-is", 
	  "anyone-who-is", "all-who-are", "create-objects", "create-agents", "create-agents-from-data"};
    protected static String[] operationsExpectingASecondBracketedExpression = 
	    // above is a superset of this list
	{ "when", "whenever", "anyone-who-is", "all-who-are", "do-if-else", "do-every-dynamic"};
    protected static String[] operationsExpectingBracketedExpressionAsFirstArgument = 
	    // used to produce warnings if first argument is not a square bracket
	{ "do-now", "do-at-setup", "do-after-setup", "do-with-probabilities",
	  "when", "whenever", "anyone-who-is", "all-who-are", "do-every-dynamic" };
    protected static String[] topLevelNetLogoPrimitives =
	{ "to", "to-report", "extensions", "globals"};
    //public static Pattern operationExpectingSquareBracketedExpression = Pattern.compile(
    //	    "\bdo-now\b|\bdo-every\b|\bdo-after\b|\bdo-with-probability\b|\bdo-repeatedly\b|\bdo-for-n\b|\bdo-at-time\b|\bdo-after-setup\b|\bdo-if\b|\bask-every-patch\b");
    protected static int commandNumber = 1;
    protected static int totalMicroBehaviourCount = 0;
        
    public static final Pattern askSelfPattern = Pattern.compile("ask(\\s)+self(\\s)+", Pattern.CASE_INSENSITIVE);
    public static final Pattern askPatchesPattern = Pattern.compile("ask(\\s)+patches(\\s)*\\[", Pattern.CASE_INSENSITIVE);
    private static final boolean NETLOGO_5 = true;
//    private static ArrayList<String> debugURLs = new ArrayList<String>();

    public MicroBehaviour(String behaviourDescriptionHTML, 
	                  String behaviourCode, 
	                  String url, 
	                  ResourcePageServiceImpl resourcePageServiceImpl,
	                  NetLogoModel netLogoModel,
	                  ClientState clientState,
	                  ArrayList<String> textAreaElements,
	                  List<MicroBehaviourEnhancement> enhancements,
	                  ArrayList<MacroBehaviour> macroBehaviours,
	                  boolean addToDataBase) { 
	if (behaviourCode == null) {
	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
		    "Micro behaviour created with null code. Constructor 1");
	}
	this.textAreaElements = textAreaElements;
	if (enhancements != null) {
	    this.enhancements = enhancements;
//	} else {
//	    System.err.println("Server micro-behaviour has a null value for enhancement rather than an empty list.");
	}
	this.macroBehaviours = macroBehaviours;
	this.netLogoModel = netLogoModel;
	setBehaviourURL(url);
//	this.clientState = clientState;
	String updatedNameHTML = resourcePageServiceImpl.fetchUpdatedNameHTML(url, clientState);
	setBehaviourCode(behaviourCode);
        // above used to call CommonUtils.removeHTMLMarkup(behaviourCode);
	// but the HTML mark-up is now removed by getTextContent()
	// and the above misbehaved if < or > was in the code (e.g. less than)
	if (updatedNameHTML == null) {
	    setBehaviourDescriptionAndNameHTML(CommonUtils.removePTags(behaviourDescriptionHTML));
	} else {
	    setBehaviourDescriptionAndNameHTML(CommonUtils.decode(updatedNameHTML));	    
	}
	HashMap<Integer, String> fetchedTextAreaValues = resourcePageServiceImpl.fetchTextAreaValues(url, clientState);
	if (fetchedTextAreaValues != null) {
	    textAreaValues = fetchedTextAreaValues;
	}
	if (textAreaElements != null) {
	    int size = textAreaElements.size();
	    int index = 0;
	    for (int i = 1; i < size; i += 2) { // every other is a value
		if (textAreaValues.get(index) == null) {
		    // has no value so give it the default one
		    // is wrapped in HTML so remove that
		    textAreaValues.put(index, CommonUtils.removeHTMLMarkup(textAreaElements.get(i)));
		    index++;
		}
	    }
	}
	setBehaviourDescription();
	this.resourcePageServiceImpl = resourcePageServiceImpl;
	if (url != null) {
	    if (addToDataBase) {
		createMicroBehaviourDataIfNeeded();
	    }
	    resourcePageServiceImpl.rememberMicroBehaviour(this, url);
	} // else should be a required micro-behaviour which will set this
    }
    

    public MicroBehaviour(
	    String behaviourDescriptionHTML, 
            String behaviourCode, 
            String behaviourURL, 
            ResourcePageServiceImpl resourcePageServiceImpl,
            NetLogoModel netLogoModel,
            ArrayList<String> textAreaElements,
            List<MicroBehaviourEnhancement> enhancements,
            ArrayList<MacroBehaviour> macroBehaviours,
            boolean addToDataBase) {
	this(behaviourDescriptionHTML, 
	     behaviourCode, 
	     behaviourURL, 
	     resourcePageServiceImpl,
	     netLogoModel,
	     netLogoModel.getClientState(),
	     textAreaElements,
	     enhancements,
	     macroBehaviours,
	     addToDataBase);
    }
    
    public MicroBehaviour(
	    MicroBehaviourData microBehaviourData,
	    String behaviourDescriptionAndNameHTML, 
            String behaviourCode, 
            String url,
            ArrayList<String> textAreaElements,
            HashMap<Integer, String> textAreaValues,
            List<MicroBehaviourEnhancement> enhancements,
            ArrayList<MacroBehaviour> macroBehaviours,
            ResourcePageServiceImpl resourcePageServiceImpl) {
	if (behaviourCode == null) {
	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
		    "Micro behaviour created with null code. Constructor 2");
	}
	// constructor used to reconstruct a micro-behaviour from the database
	this.microBehaviourData = microBehaviourData;
	if (behaviourDescriptionAndNameHTML == null) {
	    ServerUtils.logError("No name or description given to micro behaviour: " + url);
	}
	setBehaviourDescriptionAndNameHTML(behaviourDescriptionAndNameHTML);
	this.behaviourCode = behaviourCode;
	this.originalBehaviourCode = behaviourCode;
	this.resourcePageServiceImpl = resourcePageServiceImpl;
	if (resourcePageServiceImpl != null) {
	    this.netLogoModel = resourcePageServiceImpl.getNetLogoModel();
	}
	// following only used while transforming the code
	this.textAreaElements = textAreaElements;
	this.textAreaValues = textAreaValues == null ? new HashMap<Integer, String>() : textAreaValues;
	if (enhancements != null) {
	    this.enhancements = enhancements;
//	} else {
//	    System.err.println("Server micro-behaviour has a null value for enhancement rather than an empty list.");
	}
	this.macroBehaviours = macroBehaviours;
	setBehaviourDescription();
	setBehaviourURL(url);
    }

    public MicroBehaviour() {
	// only used by dummyMicroBehaviour and MacroBehaviourAsMicroBehaviour
    }

    protected void updateMicroBehaviourData() {
	// similar to createDeltaPage except discovered while running a model
//	resourcePageServiceImpl.removeMicroBehaviourCache(behaviourURL);
	DataStore.begin().delete(MicroBehaviourURLCopy.class, behaviourURL);
//	String guid = ServerUtils.generateGUIDString();
	String guid = ServerUtils.generateUniqueIdWithProcedureName(behaviourURL, getName());
	String oldURL = behaviourURL;
	behaviourURL = CommonUtils.addAttributeOrHashAttributeToURL(behaviourURL, "changes", guid);
	if (netLogoModel != null) {
	    netLogoModel.microBehaviourRenamed(oldURL, behaviourURL);
	}
	createMicroBehaviourData();
	resourcePageServiceImpl.addListsOfMicroBehavioursToCopy(guid, listOfListOfMicroBehaviours(macroBehaviours));
	resourcePageServiceImpl.rememberMicroBehaviour(this, behaviourURL);
    }
    
    public static ArrayList<ArrayList<String>> listOfListOfMicroBehaviours(ArrayList<MacroBehaviour> macroBehaviours) {
	if (macroBehaviours == null) {
	    return null;
	}
        ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>(macroBehaviours.size());
        for (MacroBehaviour macroBehaviour : macroBehaviours) {
            List<MicroBehaviour> microBehaviours = macroBehaviour.getMicroBehaviours();
            if (!microBehaviours.isEmpty()) {
        	String name = macroBehaviour.getNameHTML();
        	ArrayList<String> nameAndUrls = new ArrayList<String>(microBehaviours.size()+1);
        	result.add(nameAndUrls);
        	nameAndUrls.add(name);
        	for (MicroBehaviour microBehaviour : microBehaviours) {
        	    String url = microBehaviour.getBehaviourURL();
//        	    String activeSign = microBehaviour.isActive() ? "" : "-";
        	    nameAndUrls.add(url);
        	}
            }
        }
        return result;
    }
    
    public void createMicroBehaviourDataIfNeeded() {
	if (microBehaviourData == null && CommonUtils.hasChangesGuid(behaviourURL)) {
	    createMicroBehaviourData();
	}
    }
    
    public void updateMicroBehaviourDataIfNeeded(HashMap<Integer, String> textAreaKeysAndValues) {
	if (microBehaviourData != null) {
//	    if (!behaviourCode.equals(microBehaviourData.getBehaviourCode())) {
////		microBehaviourData.setBehaviourCode(behaviourCode);
////		ServerUtils.persist(microBehaviourData);
//		setBehaviourCode(behaviourCode);
//		String url = getBehaviourURL();
//		BehaviourCode.update(url, behaviourCode);
//		resourcePageServiceImpl.behaviourCodeChanged(url, behaviourCode);
//	    }
	    if (textAreaKeysAndValues != null &&
		!textAreaKeysAndValues.equals(microBehaviourData.getTextAreaValues())) {
		microBehaviourData.setTextAreaValues(textAreaKeysAndValues);
	    }
	}
    }
    
    public void createMicroBehaviourData() {
	ArrayList<MacroBehaviourData> macroBehaviourData = new ArrayList<MacroBehaviourData>();
	if (macroBehaviours != null) {
	    for (MacroBehaviour macroBehaviour : macroBehaviours) {
		macroBehaviourData.add(macroBehaviour.getMacroBehaviourData());
	    }
	}
	String url = CommonUtils.firstURL(behaviourURL);
	BehaviourCode.update(url, this.originalBehaviourCode, this.textAreaElements);
	microBehaviourData = 
	    MicroBehaviourData.persistMicroBehaviourData(
		    url, 
		    this.behaviourDescriptionAndNameHTML,
		    this.textAreaValues,
		    this.enhancements,
		    macroBehaviourData);
    }
     
    public MicroBehaviour copy(ArrayList<MacroBehaviour> macroBehavioursCopiesSoFar) {
	String guid = ServerUtils.generateUniqueIdWithProcedureName(behaviourURL, CommonUtils.getNameHTML(behaviourDescriptionAndNameHTML));
	String newURL = ServerUtils.createURLCopy(behaviourURL, guid);
	return new MicroBehaviour(
		null,
		behaviourDescriptionAndNameHTML, 
		originalBehaviourCode, 
		newURL,
		textAreaElements,
		textAreaValues,
		enhancements,
		copyMacroBehaviours(macroBehavioursCopiesSoFar),
		resourcePageServiceImpl);
    }

    public ArrayList<MacroBehaviour> copyMacroBehaviours(ArrayList<MacroBehaviour> macroBehavioursCopiesSoFar) {
	if (macroBehaviours == null) {
	    return null;
	} else {
	    ArrayList<MacroBehaviour> macroBehavioursCopy = new ArrayList<MacroBehaviour>();
	    for (MacroBehaviour macroBehaviour : macroBehaviours) {
		if (macroBehavioursCopiesSoFar != null && 
		    macroBehaviour.isCopyOfElementOf(macroBehavioursCopiesSoFar)) {
		    macroBehavioursCopy.add(macroBehaviour);
		} else {
		    MacroBehaviour copy = macroBehaviour.copy(macroBehavioursCopiesSoFar);
		    if (macroBehavioursCopiesSoFar == null) {
			macroBehavioursCopiesSoFar = new ArrayList<MacroBehaviour>();
		    }
		    macroBehavioursCopiesSoFar.add(copy);
		    macroBehavioursCopy.add(copy);
		}
	    }
	    return macroBehavioursCopy;
	}
    }

    public MicroBehaviour copy(
	    String newURL, HashMap<Integer, String> newTextAreaValues, List<MicroBehaviourEnhancement> newEnhancements) {
	return new MicroBehaviour(
		    null,
		    behaviourDescriptionAndNameHTML, 
		    originalBehaviourCode, 
	            newURL,
	            textAreaElements, 
	            CommonUtils.transferTextAreaValues(newTextAreaValues, textAreaValues),
	            newEnhancements,
	            copyMacroBehaviours(null),
	            resourcePageServiceImpl);
    }
    
    protected void setBehaviourDescription() {
	String behaviourDescriptionWithoutHTML = CommonUtils.removeHTMLMarkup(behaviourDescriptionAndNameHTML);
//	behaviourDescription = CommonUtils.onlyValidNetLogoCharacters(behaviourDescriptionWithoutHTML.trim(), "-");
	behaviourDescription = behaviourDescriptionWithoutHTML.trim();
	if (behaviourDescription.isEmpty()) {
	    behaviourDescription = "UnNamed-" + unnamedCounter ++;
	}
    }

    protected boolean isRawNetLogoCode() {
	if (gettingBehaviourCode) {
	    // if recursively referred to can't be raw NetLogo code
	    return false;
	}
	try {
	    gettingBehaviourCode = true;
	    String code = getTransformedBehaviourCode();
	    gettingBehaviourCode = false;
	    if (code == null) {
		if (this == resourcePageServiceImpl.getDummyMicroBehaviour()) {
		    netLogoModel.warn("Dummy micro behaviour asked for its code.");
		    return true;
		} else {
		    netLogoModel.warn("isRawNetLogoCode called on null code. Name = " +
			              CommonUtils.getName(behaviourDescription) + "; URL = " + behaviourURL +
			              ". This behaviour is being ignored during compilation. Open it and make any change to work around this problem.");
		}
		// seen this is logs -- better to recover than throw an exception later
		code = "";
		if (behaviourURL == null) {
		    behaviourURL = "";
		}
		return true; // kinda
	    }
	    if (code.length() < 10) {
		return false;
	    }
	    ArrayList<String> parts = CommonUtils.splitIntoNonEmptyLinesWithoutNetLogoComments(code);
	    if (parts.isEmpty()) {
		return false;
	    }
	    String firstLineParts[] = CommonUtils.splitByFirstWhiteSpace(parts.get(0));
	    if (firstLineParts.length == 0) { // can never happen. right?
		return false;
	    }
	    String operation = firstLineParts[0];
	    for (String topLevelNetLogoPrimitive : topLevelNetLogoPrimitives) {
		if (operation.equalsIgnoreCase(topLevelNetLogoPrimitive)) {
		    return true;
		}
	    }
	    return false;
	} catch (Exception e) {
	    netLogoModel.logException(e, "In getBehaviourCode ");
	    gettingBehaviourCode = false;
	    return false;
	}
    }

    protected String generateNetLogo(NetLogoModel netLogoModel, boolean prototypeActive) throws NetLogoException {
	this.netLogoModel = netLogoModel;
	if (behaviourCode == null) {
	    netLogoModel.warn("Micro behaviour has null code.");
	    if (this == resourcePageServiceImpl.getDummyMicroBehaviour()) {
		System.err.println("generateNetLogo called on dummy micro behaviour");
	    }
	    return "";
	}
	if (isRawNetLogoCode()) {
	    String netLogoCode = getBehaviourCode();
	    netLogoModel.findAllKindsOfVariables(netLogoCode);
	    return netLogoCode + "\n"; // no need to do any of the following
	}
	updateVariables(netLogoModel);
	updateRequiredBehaviours(netLogoModel);
	String code = getTransformedBehaviourCode().trim();
	String body = transformCode(code, getBehaviourName(false), prototypeActive, netLogoModel, netLogoModel.getClientState());
	body = CommonUtils.removeAlreadyProcessedMarkers(body);
	if (body.contains("output-") || 
	    body.contains("log-attributes") ||
            body.contains("log-patch-attributes")) { 
	    netLogoModel.setOutputAreaNeeded();
	}
	if (body.isEmpty() || CommonUtils.splitIntoNonEmptyLinesWithoutNetLogoComments(body).isEmpty()) {
	    netLogoModel.addUnneededCommands(getBehaviourName(false));
	    return "";
	}
	// just the first occurrence (if there is one)
	if (body.contains("just-created-agents")) {
	    if (body.startsWith("set just-created-agents nobody")) {
		// change 'set' to 'let'
		body = "l" + body.substring(1);
	    } else {
		body = "let just-created-agents nobody\n" + body;
	    }
	}
	// following is one for each occurrence of a "procedure body"
	StringBuilder behaviourCommand = new StringBuilder();
	behaviourCommand.append("to ");
	behaviourCommand.append(getBehaviourName(false));
	behaviourCommand.append('\n');
	behaviourCommand.append(body);
	if (body.charAt(body.length()-1) != '\n') {
	    behaviourCommand.append('\n');
	}
	behaviourCommand.append("end");
	netLogoModel.getGeneratedCommands().add(behaviourCommand.toString());
	return "";
    }
    
    protected String transformCode(
	    String originalCode, 
	    String behaviourName, 
	    boolean prototypeActive, 
	    NetLogoModel netLogoModel, 
	    ClientState clientState) {
	String phase1 = 
	    transformCodePhase1(originalCode, null, behaviourName, prototypeActive, netLogoModel, clientState);
	if (phase1.isEmpty()) {
	    return phase1;
	}
	// add-behaviours and add-behaviours-to were optimised away on 2 June 2010
	String phase2 = replaceObsoleteAddBehaviours(phase1);
	String proceduresRequiringQuotedMicroBehaviours[] = 
	 {"remove-behaviours", "remove-behaviours-from", "add-copies", "add-copy-of-another", "add-copies-of-another", "add-link-behaviours-after"};
	for (String procedure : proceduresRequiringQuotedMicroBehaviours) {
	    // need to ensure there is white space after each procedure
	    // otherwise "remove-behaviours" will be found in "remove-behaviours-from"
	    String[] pieces = phase2.split(procedure + "(\\s)+");
	    if (pieces.length > 1) {
		StringBuffer phase3 = new StringBuffer(pieces[0]);
		for (int i = 1; i < pieces.length; i++) {
		    String piece = pieces[i];
		    if (!piece.trim().isEmpty()) {
			phase3.append(procedure);
			phase3.append(' ');
			if (procedure.equals("remove-behaviours-from") ||
			    procedure.equals("remove-behaviours")) {
			    // this is special since really using name to remove matching behaviours
			    phase3.append(CommonUtils.quoteContentsOfNextBracket(piece));			    
			} else {
			    phase3.append(CommonUtils.taskifyContentsOfNextBracket(piece));
			}
		    }
		}
		phase2 = phase3.toString();
	    }
	}
	return workaroundAskPatchesLimitation(optimiseAwayAskSelf(phase2));
    }
    
    public static String replaceObsoleteAddBehaviours(String code) {
	return code.replaceAll("add-behaviours-to(\\s)+", "ask ")
	           .replaceAll("add-behaviours(\\s)+", "ask self ")
	           .replaceAll("add-behaviour(\\s)+", "");
    }
    
    protected String optimiseAwayAskSelf(String code) {
	// optimise ask self [...] to ...
	String[] split = askSelfPattern.split(code, 2);
	if (split.length == 2) {
	    String[] splitByOpenBracket = split[1].split("\\[", 2);
	    StringBuffer rest = new StringBuffer();
	    if (splitByOpenBracket.length == 2) {
		int closingBracket = CommonUtils.closeBracket(splitByOpenBracket[1], 0);
		if (closingBracket > 0) {
		    // don't include the final ] since optimising away the entire []
		    rest.append(splitByOpenBracket[1].substring(0, closingBracket-1));
		    rest.append("\n");
		    rest.append(splitByOpenBracket[1].substring(closingBracket+1)); 
		} else if (closingBracket == 0) {
		    // if ask self [] following will be empty code
		    rest.append("\n");
		    rest.append(splitByOpenBracket[1].substring(closingBracket+1)); 
		} else {
		    return code;
		}
	    } else {
		return code;
	    }
	    return optimiseAwayAskSelf(split[0] + rest.toString());
	}
	return code;
    }
    
    protected String workaroundAskPatchesLimitation(String code) {
	// NetLogo complains about ask patches [...] but not ask patches with [true] [...]
	String[] parts = askPatchesPattern.split(code);
	if (parts.length == 1) {
	    return code;
	}
	StringBuilder result = new StringBuilder(parts[0]);
	for (int i = 1; i < parts.length; i++) {
	    result.append("ask patches with [true] [ ");
	    result.append(parts[i]);
	}
	return result.toString();
    }

    protected String transformCodePhase1(
	    String originalCode,
	    String comment,
	    String behaviourName, 
	    boolean prototypeActive, 
	    NetLogoModel netLogoModel,
	    ClientState clientState) {
	// TODO: rewrite this to use the tokenizer
	String alreadyProcessed = "";
	String code = originalCode;
	while (true) {
	    // remove blank lines
//	    while (!code.isEmpty() && (code.charAt(0) == '\n' || code.charAt(0) == ' ')) {
//		code = code.substring(1);
//	    }
	    if (code.isEmpty()) {
		return alreadyProcessed + code;
	    }
	    if (code.startsWith("\"")) {
		int quotedStringEnd = CommonUtils.endQuoteIndex(code);
		if (quotedStringEnd < 0) {
		    netLogoModel.warn(CommonUtils.emphasiseError("The code of " + behaviourName + " has mismatching quotes. Please fix and try again."));
		    return "";
		}
		return alreadyProcessed + code.substring(0, quotedStringEnd+1) +
		       transformCode(code.substring(quotedStringEnd+1), 
			             behaviourName, 
			             prototypeActive, 
			             netLogoModel, 
			             netLogoModel.getClientState());
	    }
	    if (code.startsWith("constant-list")) {
		// this is a workaround for dealing with large lists that otherwise cause server to 
		// run out of memory or overflow the stack
		int openBracket = code.indexOf('[');
		int dataEnd = CommonUtils.closeBracket(code, openBracket+1);
		if (dataEnd >= 0) {
		    return alreadyProcessed + code.substring(openBracket, dataEnd+1) +
			    transformCode(code.substring(dataEnd+1), 
				          behaviourName, 
				          prototypeActive, 
				          netLogoModel, 
				          netLogoModel.getClientState());
		}
	    }
	    int operationIndex = indexOfFirstOperationExpectingABracketedExpression(code);
	    if (operationIndex >= 0) {
		// spit out the code before the operator and transform the rest
		String remainingCode = code.substring(operationIndex);
		String operation = CommonUtils.firstWord(remainingCode);
		String beforeActionAfter[];
		if (remainingCode.startsWith("create-agents-from-data")) {
		    beforeActionAfter = new String[3];
		    beforeActionAfter[0] = remainingCode; // special handler for create-agents-from-data will take care of this
		    beforeActionAfter[1] = "";
		    beforeActionAfter[2] = "";
		} else {
		    beforeActionAfter = splitOnSquareBrackets(remainingCode, operation, clientState);
		}
		if (beforeActionAfter != null) {
		    StringBuilder behaviourBody = new StringBuilder(code.substring(0, operationIndex));
		    transformBody(beforeActionAfter, operation, behaviourName, prototypeActive, netLogoModel, code, behaviourBody, clientState);
		    return alreadyProcessed + behaviourBody.toString();
		}
	    }
	    String[] operationAndBody = code.split("\n", 2); // new line
	    String operation = operationAndBody[0];
	    operationAndBody = code.split("(\\s)+", 2); // any white space
	    if (code.startsWith("\n")) {
		alreadyProcessed += "\n";
	    }
	    boolean newLineAfterOperation = operation.equals(operationAndBody[0]);
	    if (operationAndBody.length < 2) { 
		// too small to care about if it doesn't have a second word
		return alreadyProcessed + code;
	    }    
	    while (operationAndBody[0].isEmpty()) {
		// white space
		operationAndBody = CommonUtils.splitByFirstWhiteSpace(operationAndBody[1]);
		if (operationAndBody.length < 2) { 
		    // too small to care about if it doesn't have a second word
		    return alreadyProcessed + code;
		} 
	    }
	    operation = operationAndBody[0];
	    if (operation.charAt(0) == '[') {
		String beforeActionAfter[] = splitOnSquareBrackets(code, null, clientState);
		if (beforeActionAfter != null && !beforeActionAfter[1].trim().isEmpty()) {
		    // beforeActionAfter[0] should be the empty string or white space
		    return alreadyProcessed + 
		           "[ " +
		           transformCodePhase1(beforeActionAfter[1], comment, behaviourName, prototypeActive, netLogoModel, clientState) +
		           " ]" + 
		           transformCodePhase1(beforeActionAfter[2], comment, behaviourName, prototypeActive, netLogoModel, clientState);
		}
	    }
	    if (operation.charAt(0) == ';') {
		// is a comment
		int commentEnd = code.indexOf('\n');
		if (commentEnd < 0) {
		    return alreadyProcessed + code + "\n";
		}
		String localComment = code.substring(0, commentEnd+1);
		String remainingCode = code.substring(commentEnd+1);
		return alreadyProcessed + localComment + 
			transformCodePhase1(remainingCode,
				            localComment,
			                    behaviourName, 
                			    prototypeActive, 
                			    netLogoModel, 
                			    netLogoModel.getClientState());
	    }
	    String prefix = "";
	    if (operation.startsWith("[")) {
		prefix = "[";
		operation = operation.substring(1);
	    }
	    if (operation.startsWith("(")) {
		prefix = "(";
		operation = operation.substring(1);
	    }
	    String body = operationAndBody[1].trim();
	    if (body.isEmpty()) {
		return alreadyProcessed + code;
	    }
	    if (body.startsWith("]") && prefix.equals("[")) {
		// empty list
		alreadyProcessed += "[]";
		prefix = "";
		body = body.substring(1);
	    }
	    int indexOfOperation = code.indexOf(operation);
	    boolean operationOnNewLine = code.lastIndexOf('\n', indexOfOperation) >= 0;
	    int indexOfBody = code.indexOf(body);
	    boolean bodyOnNewLine = code.lastIndexOf('\n', indexOfBody) >= 0;
	    alreadyProcessed += prefix;
//	    if (operation.isEmpty()) {
//		alreadyProcessed += body;
//	    }
	    if (!prototypeActive) {
		continue;
	    }
	    if (operation.equalsIgnoreCase("define-parameter")) {
		// nothing follows a define-parameter call
		return alreadyProcessed + defineParameter(body, comment, netLogoModel, clientState);
	    } else if (operation.equalsIgnoreCase("create-plot") || operation.equalsIgnoreCase("create-histogram")) {
//		ArrayList<String> lines = CommonUtils.splitIntoNonEmptyLinesWithoutNetLogoComments(body);
//		int argumentCount = lines.size();
		boolean histogram = operation.equalsIgnoreCase("create-histogram");
//		String quoted[] = lines.get(0).split("\"");
//		boolean includesCorners = (quoted.length > 3);
		// create the NetLogo plot widget		    
		ArrayList<String> plotData = netLogoModel.createPlot(body, histogram);
		if (plotData == null) {
		    // ignore broken call to plot
		    return alreadyProcessed;
		}
		return alreadyProcessed + generateNetLogoPlottingCode(plotData, operation);
	    } else if (operation.equalsIgnoreCase("set-world-size")) {
		// can only be set via the API not from within NetLogo
		if (prototypeActive) {
		    String bodyParts[] = body.split("(\\s)+"); // split by white space
		    if (bodyParts.length == 4 || bodyParts.length == 6) {
			try {
			    netLogoModel.setMinPxcor(Integer.valueOf(bodyParts[0]));
			    netLogoModel.setMaxPxcor(Integer.valueOf(bodyParts[1]));
			    netLogoModel.setMinPycor(Integer.valueOf(bodyParts[2]));
			    netLogoModel.setMaxPycor(Integer.valueOf(bodyParts[3]));
			    if (bodyParts.length == 6) {
				netLogoModel.setMinPzcor(Integer.valueOf(bodyParts[4]));
				netLogoModel.setMaxPzcor(Integer.valueOf(bodyParts[5]));
				netLogoModel.setDimensions(3);
			    }
			} catch (Exception e) {
			    netLogoModel.warn("Error " + e.toString() + " interpreting: " + operation + " " + body);
			    e.printStackTrace();
			}
		    }
		}
		return alreadyProcessed;
	    } else if (operation.equalsIgnoreCase("set-world-location")) {
		// can only be set via the API not from within NetLogo
		if (prototypeActive) {
		    ArrayList<String> bodyParts = CommonUtils.removeEmptyLines(body.split("(\\s)+")); // split by white space
                    // TODO: rewrite this and the like to use CommonUtils.splitIntoNonEmptyLinesWithoutNetLogoComments(body);
		    // but need to be backwards compatible
		    // TODO: get rid of urx and ury
		    if (bodyParts.size() == 4) {
			try {
			    Integer llx = Integer.valueOf(bodyParts.get(0));
			    netLogoModel.setWorldLLX(llx);
			    Integer lly = Integer.valueOf(bodyParts.get(1));
			    netLogoModel.setWorldLLY(lly);
			    Integer urx = Integer.valueOf(bodyParts.get(2));
			    netLogoModel.setWorldURX(urx);
			    Integer ury = Integer.valueOf(bodyParts.get(3));
			    netLogoModel.setWorldURY(ury);
			} catch (Exception e) {
			    netLogoModel.warn("Error " + e.toString() + " interpreting: " + operation + " " + body);
			    e.printStackTrace();
			}
		    }
		}
		return alreadyProcessed;
	    } else if (operation.equalsIgnoreCase("set-patch-size")) {
		// can only be set via the API not from within NetLogo
		if (prototypeActive) {
		    String bodyParts[] = body.split("(\\s)+"); // split by white space
		    if (bodyParts.length == 1) {
			try {
			    netLogoModel.setPatchSize(Double.valueOf(bodyParts[0]));
			} catch (Exception e) {
			    netLogoModel.warn("Error " + e.toString() + " interpreting: " + operation + " " + body);
			    e.printStackTrace();
			}
		    }
		}
		return alreadyProcessed;
	    } else if (operation.equalsIgnoreCase("set-label-font-size")) {
		// can only be set via the API not from within NetLogo
		if (prototypeActive) {
		    String bodyParts[] = body.split("(\\s)+"); // split by white space
		    if (bodyParts.length == 1) {
			try {
			    netLogoModel.setLabelFontSize(Integer.valueOf(bodyParts[0]));
			} catch (Exception e) {
			    netLogoModel.warn("Error " + e.toString() + " interpreting: " + operation + " " + body);
			    e.printStackTrace();
			}
		    }
		}
		return alreadyProcessed;
	    } else if (operation.equalsIgnoreCase("set-world-geometry")) {
		if (prototypeActive) {
		    String bodyParts[] = body.split("(\\s)+"); // split by white space
		    if (bodyParts.length == 1) {
			try {
			    netLogoModel.setWorldGeometry(Integer.valueOf(bodyParts[0]));
			} catch (Exception e) {
			    netLogoModel.warn("Error " + e.toString() + " interpreting: " + operation + " " + body);
			    // ServerUtils.logException(e, getServletContext().getRealPath("/"));
			    e.printStackTrace();
			}
		    }
		}
		// NetLogo needs to know this too
		if (operationOnNewLine) {
		    operation = "\n" + operation;
		}
		if (bodyOnNewLine) {
		    alreadyProcessed += operation + "\n" + body; 
		} else {
		    alreadyProcessed += operation + " " + body; 
		}
		return alreadyProcessed;
	    } else if (operation.equalsIgnoreCase("set-tick-based-view-update-policy")) {
		// can only be set via the API not from within NetLogo
		if (prototypeActive) {
		    String bodyParts[] = body.split("(\\s)+"); // split by white space
		    if (bodyParts.length == 1) {
			try {
			    netLogoModel.setTickBasedUpdates(Boolean.valueOf(bodyParts[0]));
			} catch (Exception e) {
			    netLogoModel.warn("Error " + e.toString() + " interpreting: " + operation + " " + body);
			    e.printStackTrace();
			}
		    }
		}
		return alreadyProcessed;
	    } else if (operation.equalsIgnoreCase("set-frame-rate")) {
		// can only be set via the API not from within NetLogo
		if (prototypeActive) {
		    String bodyParts[] = body.split("(\\s)+"); // split by white space
		    if (bodyParts.length == 1) {
			try {
			    netLogoModel.setFrameRate(Double.valueOf(bodyParts[0]));
			} catch (Exception e) {
			    netLogoModel.warn("Error " + e.toString() + " interpreting: " + operation + " " + body);
			    e.printStackTrace();
			}
		    }
		}
		return alreadyProcessed;
	    } else if (operation.equalsIgnoreCase("set-tick-counter-label")) {
		// can only be set via the API not from within NetLogo
		if (prototypeActive) {
		    String bodyParts[] = body.split("(\\s)+"); // split by white space
		    if (bodyParts.length == 1) {
			try {
			    netLogoModel.setTickLabel(bodyParts[0]);
			} catch (Exception e) {
			    netLogoModel.warn("Error " + e.toString() + " interpreting: " + operation + " " + body);
			    e.printStackTrace();
			}
		    }
		}
		return alreadyProcessed;
	    } else if (operation.equalsIgnoreCase("set-tick-counter-visible")) {
		// can only be set via the API not from within NetLogo
		if (prototypeActive) {
		    String bodyParts[] = body.split("(\\s)+"); // split by white space
		    if (bodyParts.length == 1) {
			try {
			    netLogoModel.setShowTickCounter(Boolean.valueOf(bodyParts[0]));
			} catch (Exception e) {
			    netLogoModel.warn("Error " + e.toString() + " interpreting: " + operation + " " + body);
			    e.printStackTrace();
			}
		    }
		}
		return alreadyProcessed;
	    } else if (operation.equalsIgnoreCase("add-declaration")) {
		netLogoModel.addDeclaration(body);
		return alreadyProcessed;
	    } else {
		String extensionAndName[] = operation.split(":", 2);
		if (extensionAndName.length == 2) {
		    netLogoModel.extensionUsed(extensionAndName[0]);
		}
		if (!body.equals("")) {
		    if (operation.equalsIgnoreCase("create-slider")) {
			if (prototypeActive)
			    netLogoModel.createSlider(body); 
			// create the NetLogo slider widget
			return alreadyProcessed;
		    } else if (operation.equalsIgnoreCase("create-button")) {
			// create the NetLogo button widget
			// old style
			if (prototypeActive) {
			    netLogoModel.createButton(body, true, false);
			}
			return alreadyProcessed;
		    } else if (operation.equalsIgnoreCase("add-button")) {
			if (prototypeActive)
			    netLogoModel.createButton(body, false, false); 
			// create the NetLogo button widget -- using behaviours
			return alreadyProcessed;
		    } else if (operation.equalsIgnoreCase("add-netlogo-button")) {
			if (prototypeActive)
			    netLogoModel.createButton(body, false, true); 
			// create the NetLogo button widget - using NetLogo code
			return alreadyProcessed;
		    } else if (operation.equalsIgnoreCase("create-monitor")) {
			if (prototypeActive)
			    netLogoModel.createMonitor(body); 
			// create the NetLogo monitor widget
			return alreadyProcessed;
		    } else if (operation.equalsIgnoreCase("create-chooser")) {
			if (prototypeActive)
			    netLogoModel.createChooser(body); 
			// create the NetLogo chooser widget
			return alreadyProcessed;
		    } else if (operation.equalsIgnoreCase("create-switch")) {
			if (prototypeActive)
			    netLogoModel.createSwitch(body); 
			// create the NetLogo switch widget
			return alreadyProcessed;
		    } else if (operation.equalsIgnoreCase("create-text")) {
			if (prototypeActive)
			    netLogoModel.createText(body, false); 
			// create the NetLogo text widget
			continue;
		    } else if (operation.equalsIgnoreCase("create-color-text")) {
			if (prototypeActive)
			    netLogoModel.createText(body, true); 
			// create the NetLogo text widget
			return alreadyProcessed;
		    }
		}
		if (operation.equalsIgnoreCase("create-output-area")) {
		    if (prototypeActive)
			netLogoModel.createOutputArea(body); 
		    // create the NetLogo output widget
		    return alreadyProcessed;// no need to add this to the NetLogo code
		}
		String[] newOperationAndBody;
		try {
		    newOperationAndBody = expandSetMyNext(operation, operationOnNewLine, body, bodyOnNewLine, false);
		    alreadyProcessed += newOperationAndBody[0];
		    if (newLineAfterOperation) {
			alreadyProcessed += "\n";
		    }
		    code = newOperationAndBody[1];
		} catch (Exception e) {
		    netLogoModel.warn(e.getMessage() + " in " + getBehaviourDescription() + " (" + getBehaviourURL() + ")");
		    e.printStackTrace();
		    return alreadyProcessed + code;
		}
	    }
	}
    }

    private String generateNetLogoPlottingCode(ArrayList<String> plotData, String operation) {
	String name = getName().trim();
	String newCommandName1 = name.replace(' ', '-') + "-" + commandNumber++;
	generateReporter(plotData.get(3), newCommandName1, netLogoModel);
	String newCommandName2 = name.replace(' ', '-') + "-" + commandNumber++;
	generateReporter(plotData.get(4), newCommandName2, netLogoModel);
	return operation + " " + 
	       plotData.get(0) + "\n" + 
	       plotData.get(1) + "\n" + 
	       plotData.get(2) + "\n" + 
	       "task [" + newCommandName1 + "]\n" + 
	       "task [" + newCommandName2 + "]\n";
    }

    protected void transformBody(
	    String beforeActionAfter[], 
	    String operation, 
	    String behaviourName,
	    boolean prototypeActive, 
	    NetLogoModel netLogoModel, 
	    String originalCode, 
	    StringBuilder behaviourBody, 
	    ClientState clientState) {
	if (beforeActionAfter == null) {
	    netLogoModel.warn("Error: missing matched square brackets of " + behaviourURL + 
		              " (" + behaviourDescription + ") " + " in<br>" + originalCode);
	    return;
	}
	// removed call to addUpdateTurtlePositionIfNeeded since my-x/xcor etc handled differently now
	String transformedBracketedCode = 
	    transformCode(beforeActionAfter[1], behaviourName, prototypeActive, netLogoModel, clientState);
	boolean operationExpectingASecondBracketedExpression = isOperationExpectingASecondBracketedExpression(operation);
	if (operation.equals("do-now") || operation.equals("do-at-setup")) {
	    // no need to do anything at run-time
	    behaviourBody.append(transformedBracketedCode);
	} else if (operation.equals("do-with-probability")) {
	    behaviourBody.append("if ");
	    String[] parts = beforeActionAfter[0].split("(\\s)+", 2);	    
	    behaviourBody.append(" random-float 1.0 <= ");
	    behaviourBody.append(parts[1].trim());
	    behaviourBody.append("\n[");
	    behaviourBody.append(transformedBracketedCode.trim());
	    // need to go to a new line in case the above ended with a comment
	    behaviourBody.append("\n]");
	} else if (operation.equals("do-with-probabilities")) {
	    behaviourBody.append("let random-fraction random-float 1.0");
	    String oddsAndBehaviours = beforeActionAfter[1].trim();
	    int index = 0;
	    float odds = 0.0f;
	    float newOdds;
	    boolean first = true;
	    int closeBracketsNeeded = 0;
	    while (true) {
		int openBracketIndex = oddsAndBehaviours.indexOf("[", index);
		if (openBracketIndex < 0) {
		    behaviourBody.append("[]"); // last else branch
		    break;
		}
		String oddsString = oddsAndBehaviours.substring(index, openBracketIndex);
		try {
		    newOdds = Float.parseFloat(oddsString);
		} catch (NumberFormatException e) {
		    netLogoModel.warn("Expected the following to be a floating point number: " + oddsString);
		    netLogoModel.warn("In " + originalCode);
		    return;
		}
		index = openBracketIndex;
		int closeBracketIndex = oddsAndBehaviours.indexOf("]", index);
		if (closeBracketIndex < 0) {
		    netLogoModel.warn("Missing close bracket in " + originalCode);
		    return;
		}
		String behaviours = oddsAndBehaviours.substring(index, closeBracketIndex+1);
		index = closeBracketIndex+1;
		if (newOdds > 0.0f) { 
		    odds += newOdds;
		    if (odds < 1.0) {
			if (!first) {
			    behaviourBody.append("["); // for else branches
			    closeBracketsNeeded++;
			}
			behaviourBody.append("\nif-else random-fraction <= ");
			behaviourBody.append(odds);
		    }
		    behaviourBody.append(CommonUtils.removeQuotesAndAddNewLines(behaviours));
		    if (odds >= 1.0) {
			if (odds > 1.0) {
			    netLogoModel.warn("Probabilities add up to more than 1.0 in " + originalCode);
			}
			break;
		    }
		}
		first = false;
	    }
	    if (closeBracketsNeeded > 0) {
		behaviourBody.append('\n'); // in case the last line was a comment
	    }
	    while (closeBracketsNeeded > 0) {
		behaviourBody.append(']');
		closeBracketsNeeded--;
	    }
	} else if (operation.equals("do-if") || 
		   operation.equals("do-if-else")) {
	    // no need to do anything at run-time -- but remove the "do-" part
	    // except that do-if implicitly checked if predicate was equal to true
	    // otherwise uninitialised variables will trigger an error that 0 is not true/false
	    // first strip off the "do-" part
	    String conditional;
	    conditional = beforeActionAfter[0].substring(3);
	    // why this new line code?
	    int newLineIndex = conditional.indexOf('\n');
	    String firstPart;
	    if (newLineIndex > 0) {
		firstPart = conditional.substring(0, newLineIndex);
	    } else {
		firstPart = conditional;
	    }
	    behaviourBody.append(firstPart);
	    if (!beforeActionAfter[0].trim().equals("do-if") && 
		!containsNetLogoPredicate(beforeActionAfter[0])) {
		behaviourBody.append(" = true");
	    }
	    if (newLineIndex > 0) {
		behaviourBody.append(conditional.substring(newLineIndex));
	    }
	    behaviourBody.append('[');
	    behaviourBody.append(transformedBracketedCode);
	    // need to go to a new line in case the above ended with a comment
	    behaviourBody.append("\n]");
	    if (operationExpectingASecondBracketedExpression) {
		ArrayList<String> beforeContentsAndAfter = 
		    CommonUtils.beforeContentsAndAfter(beforeActionAfter[2]);
		if (beforeContentsAndAfter == null) {
		    return; // TODO: produce warning
		}
		String transformedElseCode = 
		    transformCode(addUpdateTurtlePositionIfNeeded(beforeContentsAndAfter.get(1)), behaviourName, prototypeActive, netLogoModel, clientState);
		// put brackets back
		behaviourBody.append(beforeContentsAndAfter.get(0));
		behaviourBody.append(transformedElseCode);
		behaviourBody.append(beforeContentsAndAfter.get(2));
		// taken care of second command
		return;
	    }
	} else if (operation.equals("create-agents") ||
		   // older name -- kept for backwards compatibility
		   operation.equals("create-objects")) {
	    // need to remove kind-name and 
	    // insert "initialise-instance-of-" kind-name into command list
	    // first one will be changed to 'let'
	    behaviourBody.append("; The following code was generated by a call to create-agent.\n");
	    behaviourBody.append("    set just-created-agents nobody\n");
	    // patch pxcor pycor works whether is a patch or turtle unlike patch-here
	    behaviourBody.append("ask patch pxcor pycor");
	    if (netLogoModel.getDimensions() == 3) {
		behaviourBody.append(" zcor");
	    }
	    behaviourBody.append("    [sprout-objects ");
	    String[] arguments = beforeActionAfter[0].split("(\\s)+", 3);
	    behaviourBody.append(arguments[1]); // number of agents
	    String kindNameExpression = arguments[2];
//	    String kindName = kindNameExpression.replace("\"", "").trim();
	    behaviourBody.append(" [\n set just-created-agents (turtle-set self just-created-agents)\n set kind " + kindNameExpression + "]]");
//	    String initialisationBehaviours = "";
//	    MacroBehaviour macroBehaviour = netLogoModel.getMacroBehaviourNamed(kindName);
//	    if (macroBehaviour != null) {
//		String behaviourNames = macroBehaviour.getBehaviourNames();
//		if (behaviourNames != null && !behaviourNames.isEmpty()) {
//		    initialisationBehaviours = behaviourNames; 
//		}
//	    } else {
//		clientState.warn("Unable to find a prototype named " + kindName + ".\nCode is: " + originalCode);
//	    }
	    // the following used to be just be called from within sprout-objects but then can't reference myself, etc.
	    // initialise my-x and my-y since sprouted where 'myself' is
	    // initialise them all before running any behaviours
	    behaviourBody.append("\nask just-created-agents [\n set my-x xcor\n set my-y ycor\n initialise-object\n initialise-previous-state\n]");
//	    if (!initialisationBehaviours.isEmpty()) {
		behaviourBody.append("\nask just-created-agents [\n " + "kind-initialisation " + kindNameExpression + "]\n");
//	    }
	    if (transformedBracketedCode != null) {
		behaviourBody.append("\nask just-created-agents [\n " + transformedBracketedCode + "\n ]");
	    }
	} else if (operation.equals("create-agents-from-data")) {
	    behaviourBody.append("; The following code was generated by a call to create-agents-from-data.\n");
	    // would like to split the code into 5 pieces but can only rely upon new line for the first ones since
	    // data can have new lines in it
	    String[] arguments = beforeActionAfter[0].split("\n", 4);
	    String kindNameExpression = arguments[1];
	    String attributeString = arguments[2].trim();
	    attributeString = attributeString.substring(1, attributeString.length()-1); // remove [ and ]
	    String[] attributes = attributeString.split("\\s");
	    int attributesCount = attributes.length;
	    int dataEndIndex = arguments[3].indexOf("]\n");
	    String dataString = arguments[3].substring(0, dataEndIndex+1);
	    String additionalBehaviours = arguments[3].substring(dataEndIndex+2).trim();
	    behaviourBody.append("let data " + dataString + "\n");
	    behaviourBody.append("let data-count length data\n");
	    behaviourBody.append("; Following is used to ignore any extra data at the end.\n");
	    behaviourBody.append("let last-index " + attributesCount + " * floor (data-count / " + attributesCount + ")\n");
	    behaviourBody.append("let index 0\n");
	    behaviourBody.append("while [index < last-index]\n");
	    // patch pxcor pycor works whether is a patch or turtle unlike patch-here
	    behaviourBody.append("[ask patch pxcor pycor");
	    if (netLogoModel.getDimensions() == 3) {
		behaviourBody.append(" zcor");
	    }
	    behaviourBody.append("\n[sprout-objects 1 \n");
	    behaviourBody.append("[\n");
	    behaviourBody.append("set kind " + kindNameExpression + "\n");
	    for (int i = 0; i < attributesCount; i++) {
		if (!attributes[i].equals("\"\"")) { // ignore ""
		    behaviourBody.append("set " + attributes[i] + " item index data\n");
		}
		behaviourBody.append("set index index + 1\n");
	    }
	    behaviourBody.append("initialise-object\n");
	    behaviourBody.append("kind-initialisation " + kindNameExpression + "\n");
	    
	    behaviourBody.append(additionalBehaviours.substring(1,additionalBehaviours.length()-1) + "\n ]"); // remove [ and ]
	    behaviourBody.append("]]\n");
	} else if (CommonUtils.isTrivialCode(transformedBracketedCode)) {
	    // use a task even though the body is trivial since
	    // relied upon by remove-behaviours
	    // and perhaps task [foo] is faster to run than "foo"
	    // when and whenever may be referring to free variables
	    behaviourBody.append(beforeActionAfter[0]);
	    behaviourBody.append(" task [");
	    behaviourBody.append(transformedBracketedCode);
	    behaviourBody.append("] ");
	} else if (NETLOGO_5) {
	    behaviourBody.append(beforeActionAfter[0]);
	    if (operationExpectingASecondBracketedExpression) {
		behaviourBody.append(" task [");
		behaviourBody.append(CommonUtils.addNewLineIfLastLineIsComment(beforeActionAfter[1]));
		behaviourBody.append("]");
		behaviourBody.append("\n task ");
		behaviourBody.append(transformCodePhase1(beforeActionAfter[2], null, behaviourName, prototypeActive, netLogoModel,clientState));
	    } else {
		behaviourBody.append(" task [");
		behaviourBody.append(CommonUtils.addNewLineIfLastLineIsComment(transformedBracketedCode));
		behaviourBody.append("]");
		String transformedAfter = 
			transformCode(addUpdateTurtlePositionIfNeeded(beforeActionAfter[2]), 
				      behaviourName, prototypeActive, netLogoModel, clientState);
		behaviourBody.append(transformedAfter); // might be some closing square brackets for example
	    }
	    return;
//	    netLogoModel.associateWithProcedureName(getBehaviourName(true), body);
	} else {
	    behaviourBody.append(beforeActionAfter[0]);
	    String newCommandNameBase = CommonUtils.replaceAllNonLetterOrDigit(getName().trim(), '-');
	    String newCommandName = newCommandNameBase + "-" + commandNumber++;
	    char firstCharacter = newCommandName.charAt(0);
	    if (!Character.isLetter(firstCharacter)) {
		// NetLogo can't handle procedures that begin with a non-letter
		newCommandName = "c" + newCommandName;
//		System.out.println(CommonUtils.decode(behaviourDescription));
	    }
	    StringBuilder newCommand = new StringBuilder();
	    newCommand.append("to");
	    if (operationExpectingASecondBracketedExpression) {
		// assumes the first one is a reporter -- e.g. when and whenever
		newCommand.append("-report");
	    }
	    newCommand.append(' ');
	    newCommand.append(newCommandName);
	    newCommand.append('\n');
	    if (operationExpectingASecondBracketedExpression) {
		newCommand.append("report ");
	    }
	    newCommand.append(transformedBracketedCode);
	    newCommand.append('\n');
	    newCommand.append("end");
	    netLogoModel.getGeneratedCommands().add(newCommand.toString());
//	    netLogoModel.associateWithProcedureName(getBehaviourName(true), CommonUtils.quote(newCommandNameBase));
	    behaviourBody.append(' ');
	    behaviourBody.append(CommonUtils.quote(newCommandName));
	    behaviourBody.append(' ');
	}
	if (operationExpectingASecondBracketedExpression) {
	    transformBody(splitOnSquareBrackets(beforeActionAfter[2], operation, clientState), 
		          "", behaviourName, prototypeActive, netLogoModel, originalCode, behaviourBody, clientState);
	} else {
	    String transformedAfter = 
		    transformCode(addUpdateTurtlePositionIfNeeded(beforeActionAfter[2]), 
			          behaviourName, prototypeActive, netLogoModel, clientState);
	    behaviourBody.append(transformedAfter);
	}
    }	
    
    private static boolean containsNetLogoPredicate(String code) {
	if (code.contains("and")) return true;
	if (code.contains("or")) return true;
	if (code.contains("=")) return true;
	if (code.contains("?")) return true; // predicates end in ?
	if (code.contains(">")) return true;
	if (code.contains("<")) return true;
	// might miss a few but worst case 
	// you end up with (predicate) = true rather than just (predicate)
	return false;
    }

    public static boolean isOperationExpectingASecondBracketedExpression(String operation) {
	for (String operationExpectingASecondBracketedExpression : operationsExpectingASecondBracketedExpression) {
	    if (operation.equals(operationExpectingASecondBracketedExpression)) {
		return true;
	    }
	}
	return false;
    }
    
    public static int indexOfFirstOperationExpectingABracketedExpression(String code) {
	int bestIndex = -1;
	for (String operationExpectingABracketedExpression : operationsExpectingABracketedExpression) {
	    int index = CommonUtils.indexOfNetLogoCodeOnly(operationExpectingABracketedExpression, code, 0);
	    if (index >= 0) {
		if (bestIndex < 0 || index < bestIndex) {
		    bestIndex = index;    
		}
	    }
	}
	return bestIndex;
    }
	
    protected String defineParameter(String body, String comment, NetLogoModel netLogoModel, ClientState clientState) {
	// supports (controlled by a slider), (controlled by an input box),
	// and no interface (just set and declare)
	String tokensNewVersion[] = 
	    {"name: ", 
	     "initial value: ", 
	     "upper left corner:  ",
	     "lower right corner: ",
	     "minimum value: ",
	     "maximum value: ",
	     "increment:     ",
	     "units:         ",
	     "horizontal:    ",
	     "Type check: ",
	     "Multi-line: ",
	    "\n"};
	String tokensOldVersion[] = 
	    {"name: ", 
	     "initial value: ", 
	     "lower left corner:  ",
	     "upper right corner: ",
	     "minimum value: ",
	     "maximum value: ",
	     "increment:     ",
	     "units:         ",
	     "horizontal:    ",
	     "Type check: ",
	     "Multi-line: ",
	    "\n"};
	String tokens[];
	if (body.contains(tokensOldVersion[2])) {
	    tokens = tokensOldVersion;
	} else {
	    tokens = tokensNewVersion;
	} 
	int startEnd[] = {0, 0};
	if (!ServerUtils.extractValue(tokens[0], tokens[1], body, startEnd, clientState)) {
	    return "";
	}
	String variableName = body.substring(startEnd[0], startEnd[1]).trim();
	if (!ServerUtils.extractValue(tokens[1], tokens[2], body, startEnd, clientState)) {
	    return "";
	}
	String interfaceType = null;
	String initialValue = body.substring(startEnd[0], startEnd[1]).trim();
	int sliderStart = variableName.indexOf(" (controlled by a slider)");
	int inputBoxStart = -1;
	int switchStart = -1;
	if (sliderStart > 0) {
	    variableName = variableName.substring(0, sliderStart);
	    interfaceType = "slider";
	    try {
		Double.parseDouble(initialValue);
	    } catch (NumberFormatException e) {
		clientState.warn("Initial value of a slider (define-parameter) is not a proper number: " + initialValue);
	    }
	} else {
	    inputBoxStart = variableName.indexOf(" (controlled by an input box)");
	    if (inputBoxStart > 0) {
		variableName = variableName.substring(0, inputBoxStart);
		interfaceType = "input box";
	    } else {
		switchStart = variableName.indexOf(" (controlled by a switch)");
		if (switchStart > 0) {
		    variableName = variableName.substring(0, switchStart);
		    interfaceType = "switch";
		    // no need to check if it is a correct boolean value since Boolean.parseBoolean
		    // just checks if the value is "true"
		}
	    }
	}
//	if (variableName.equalsIgnoreCase("clocked")) {
//	    netLogoModel.doNotAddClockedSwitch();
//	} else 
	if (variableName.equalsIgnoreCase("the-default-buttons-should-not-be-added")) {
	    netLogoModel.doNotAddDefaultButtons();
	    return "";
	}
	if (comment == null || !comment.equals("; Define a new parameter (optionally controlled by a slider or input box). \n")) {
	    netLogoModel.associateCommentWithGlobal(variableName, comment, interfaceType);
	}
	if (interfaceType != null) {
	    // if has interface then the new lines should be 'quoted' in the NetLogo file
	    initialValue = initialValue.replaceAll("\n", "\\\\n");
	}
	if (!netLogoModel.addExtraGlobalVariable(variableName, interfaceType == null, false)) {
	    // no need to warn since can have multiple interfaces for the same variable
	    // and if it has no interface OK to set it multiple times...
//	    clientState.warn("Warning. Parameter " + variableName + " is defined more than once.");
	}
	if (interfaceType != null) {
	    // both use same location parameters
	    // older versions used lower left
	    if (!ServerUtils.extractValue(tokens[2], tokens[3], body, startEnd, null)) {
		// don't warn
		return "";
	    }
	    String upperLeftCorner = body.substring(startEnd[0], startEnd[1]).trim();
	    // older versions used upper right
	    if (!ServerUtils.extractValue(tokens[3], tokens[4], body, startEnd, null)) {
		// don't warn{
		return "";
	    }
	    String lowerRightCorner = body.substring(startEnd[0], startEnd[1]).trim();
	    int llx, lly, urx, ury;
	    String[] point = upperLeftCorner.split(" ", 2);
	    if (point.length < 2) {
		clientState.warn("Upper left corner (in define-parameter) should be two numbers separated by a space. Not '" + 
			         upperLeftCorner + "' in " + behaviourDescription + " " + behaviourURL);
		return "";
	    }
	    llx = ServerUtils.parseInt(point[0], "defineParameter", clientState);
	    lly = ServerUtils.parseInt(point[1], "defineParameter", clientState);
	    point = lowerRightCorner.split(" ", 2);
	    if (point.length < 2) {
		clientState.warn("Lower right corner (in define-parameter) should be two numbers separated by a space. Not '" + 
			         lowerRightCorner + "'" + "' in " + behaviourDescription + " " + behaviourURL);
		return "";
	    }
	    urx = ServerUtils.parseInt(point[0], "defineParameter", clientState);
	    ury = ServerUtils.parseInt(point[1], "defineParameter", clientState);
	    if (sliderStart > 0) {
		if (!ServerUtils.extractValue(tokens[4], tokens[5], body, startEnd, clientState)) {
		    return "";
		}
		String minimumValue = body.substring(startEnd[0], startEnd[1]).trim();
		try {
		    Double.parseDouble(minimumValue);
		} catch (NumberFormatException e) {
		    clientState.warn("Minimum value of slider (in define-parameter) is not a number: " + minimumValue);
		}
		if (!ServerUtils.extractValue(tokens[5], tokens[6], body, startEnd, clientState)) {
		    return "";
		}
		String maximumValue = body.substring(startEnd[0], startEnd[1]).trim();
		try {
		    Double.parseDouble(maximumValue);
		} catch (NumberFormatException e) {
		    clientState.warn("Maximum value of slider (in define-parameter) is not a number: " + maximumValue);
		}
		if (!ServerUtils.extractValue(tokens[6], tokens[7], body, startEnd, clientState)) {
		    return "";
		}
		String increment = body.substring(startEnd[0], startEnd[1]).trim();
		try {
		    Double.parseDouble(increment);
		} catch (NumberFormatException e) {
		    clientState.warn("Increment value of slider (in define-parameter) is not a number: " + increment);
		}
		String units = "NIL"; // none by default
		// don't pass in clientState since no warnings since
		// older version of micro-behaviour didn't have this and next feature
		if (ServerUtils.extractValue(tokens[7], "Units displayed on the slider", body, startEnd, null)) {
		    units = body.substring(startEnd[0], startEnd[1]).trim();
		    if (units.isEmpty()) {
			units = "NIL";
		    }
		}
		boolean horizontal = true;
		if (ServerUtils.extractValue(tokens[8], "Displayed horizontally rather than vertically (true or false)", 
			body, startEnd, null)) {
		    horizontal = !body.substring(startEnd[0], startEnd[1]).trim().equalsIgnoreCase("false");
		}	
		netLogoModel.addCommandToAddSlider(variableName, llx, lly, urx, ury,
			                           minimumValue, maximumValue, increment, initialValue,
			                           units, horizontal);
		return "";
	    } else if (switchStart > 0) {
		netLogoModel.addCommandToAddSwitch(variableName, llx, lly, urx, ury, initialValue);
	    } else {
		if (!ServerUtils.extractValue(tokens[9], tokens[10], body, startEnd, clientState)) {
		    return "";
		}
		String typeString = body.substring(startEnd[0], startEnd[1]).trim();
		String[] typeAndComment = typeString.split("\n", 2);
		if (typeAndComment.length == 2) {
		    typeString = typeAndComment[0].trim();
		}
		// valid types: Number, String, Color, String (reporter), String (commands)
		if (!typeString.equals("Number") &&
		    !typeString.equals("String") &&
		    !typeString.equals("Color") &&
		    !typeString.equals("String (reporter)") &&
		    !typeString.equals("String (commands)")) {
		    clientState.warn("Input box type: '" + typeString + "' should be Number, String, Color, String (reporter), or String (commands) in input box in define-parameter.");
		    typeString = "Number"; // good default?
		}
		if (!ServerUtils.extractValue(tokens[10], tokens[11], body, startEnd, clientState)) {
		    return "";
		}
		String multiLineString = body.substring(startEnd[0], startEnd[1]).trim();
		boolean multiLine = multiLineString.startsWith("1");
		netLogoModel.addCommandToAddInputBox(variableName, llx, lly, urx, ury, 
			                             initialValue, multiLine, typeString);
	    }
	    return "";
	}
	String initialiseVariable = 
		"if not member? \"" + variableName + "\" globals-not-to-be-initialised\n" 
		+ "  [set " + variableName + " " + initialValue + "\n  ]"; // ] on new line in case initialValue ends with a comment
	netLogoModel.addGlobalInitialisation(initialiseVariable);
	return "";
    }
    
    protected void generateReporter(String body2, String newCommandName, NetLogoModel netLogoModel) {
	StringBuilder newReporter = new StringBuilder();
	newReporter.append("to-report ");
	newReporter.append(newCommandName);
	newReporter.append('\n');
	newReporter.append("report ");
	newReporter.append(body2);
	if (body2.charAt(body2.length()-1) != '\n') {
	    newReporter.append('\n');
	}
	newReporter.append("end");
	netLogoModel.getGeneratedCommands().add(newReporter.toString());
    }
    
    protected static String addUpdateTurtlePositionIfNeeded(String body) {
	if (body.indexOf("set my-x ") >= 0 || body.indexOf("set my-y ") >= 0) { 
	    return body + "\nupdate-turtle-position";
	} else {
	    return body;
	}
    }

    protected static String[] expandSetMyNext(String operation, boolean operationOnNewLine, 
	                                      String body, boolean bodyOnNewLine, 
	                                      boolean keepToOneLine) throws Exception {
	String[] result = new String[2]; // operation (with white space) and body
	boolean operationIsSet = operation.equalsIgnoreCase("set");
	result[0] = bodyOnNewLine & !keepToOneLine && !operationIsSet ? 
		               operation + "\n" : 
		               operation + " ";
	result[1] = body;
	if (operationOnNewLine && !keepToOneLine) {
	    result[0] = "\n" + result[0];
	}
	if (!operationIsSet) {
	    return result;
	}
	String setPrefix = operationOnNewLine && !keepToOneLine ? "\n" : "";
	String setPostfix = " ";
	// following just broke up set statements and looked bad
//	String setPostfix = bodyOnNewLine & !keepToOneLine ? "\n" : " ";
	final String variable = CommonUtils.firstWord(body);
	if (variable == null) {
	    throw new Exception("Error. The following code contains 'set' without a value expression: " + body);
	}
	int index = variable.indexOf("my-next-");
	if (index >= 0) {
	    if (variable.endsWith("-set")) {
		return result; // already processed
	    }
	    String setSet = setPrefix + "set" + setPostfix + variable + "-set true";
	    if (!keepToOneLine && !result[0].startsWith("\n")) {
		setSet = setSet + "\n";
	    }
	    result[0] = setSet + " " + result[0];
	    return result;
	}
	int setNextIndex = variable.indexOf("next-");
	if (setNextIndex >= 0) {
	    int ofPatchIndex = variable.indexOf("-of-patch");
	    if (ofPatchIndex >= 0) {
		if (variable.endsWith("-the")) {
		    return result; // already processed
		}
		String setSet = setPrefix + "set" + setPostfix + variable + "-set true";
		if (!keepToOneLine && !result[0].startsWith("\n")) {
		    setSet = setSet + "\n";
		}
		result[0] = setSet + " " + result[0];
		return result;
	    }
	}
	return result;
    }

    protected static String removeComment(String line) {
	int commentStart = line.indexOf(';');
	if (commentStart < 0) {
	    return line;
	}
	int quoteEnd = line.indexOf('\"', commentStart);
	if (quoteEnd < 0) {
	    return line.substring(0, commentStart);
	}
	int quoteStart = line.indexOf('\"');
	if (quoteStart > commentStart) {
	    return line.substring(0, commentStart);
	}
	// in principle could be foo " ... " ; " ... " which this misses
	return line;
    }

    protected void updateRequiredBehaviours(NetLogoModel netLogoModel) throws NetLogoException {
	if (referencedMicroBehaviours.size() > 0) {
	    for (MicroBehaviour referencedMicroBehaviour : referencedMicroBehaviours) {
		netLogoModel.addRequiredMicroBehaviour(referencedMicroBehaviour);
	    }
	    return; // already did all this or already know the answer
	}
	if (transformedBehaviourCode != null) {
	    return; // already transformed
	}
	String code = getBehaviourCode();
	boolean alreadyProcessed = ServerUtils.extraNameIndex(code) >= 0;
	if (alreadyProcessed) {
	    // processed code ended up in database -- rework this?
	    return;
	}
	StringBuilder newCode = new StringBuilder(code);
	for (int i = 0; i < NetLogoModel.proceduresWithOneBehaviourArgument.length; i++) {
	    int location = code.indexOf(NetLogoModel.proceduresWithOneBehaviourArgument[i], 0);
	    if (location >= 0) {
		int procedureNameLength = NetLogoModel.proceduresWithOneBehaviourArgument[i].length();
		char ch;
		if (code.length() <= location + procedureNameLength) {
		    ch = ' ';
		} else {
		    ch = code.charAt(location + procedureNameLength);
		}
		if (ch == ' ' || ch == '\n') { 
		    // otherwise is just part of the name
		    while (location >= 0) {
			int argumentStart = location + procedureNameLength;
			String behaviour[] = code.substring(argumentStart).split("\"", 3);
			if (behaviour.length == 3) {
			    String fullBehaviourName = behaviour[1];
			    int extraNameIndex = ServerUtils.extraNameIndex(fullBehaviourName);
			    if (extraNameIndex > 0) {
				fullBehaviourName = fullBehaviourName.substring(0, extraNameIndex);
			    }
			    processRequiredBehaviour(netLogoModel, newCode, fullBehaviourName);
			}
			location = code.indexOf(NetLogoModel.proceduresWithOneBehaviourArgument[i], argumentStart);
		    }
		}
	    }
	}
	// TODO: determine if this is still needed:
	for (int i = 0; i < NetLogoModel.proceduresWithBehaviourArguments.length; i++) {
	    int location = code.indexOf(NetLogoModel.proceduresWithBehaviourArguments[i], 0);
	    if (location >= 0) {
		int procedureNameLength = NetLogoModel.proceduresWithBehaviourArguments[i].length();
		char ch;
		if (code.length() <= location + procedureNameLength) {
		    ch = ' ';
		} else {
		    ch = code.charAt(location + procedureNameLength);
		}
		if (ch == ' ' || ch == '\n') { // otherwise is just part of the name
		    while (location >= 0) {
			String remainingCode = code.substring(location + procedureNameLength);
			int argumentStart = remainingCode.indexOf('[');
			if (argumentStart < 0) {
			    netLogoModel.warn("Expected to find a list with square brackets in " + code);
			    return;
			}
			remainingCode = remainingCode.substring(argumentStart);
			int argumentStop = remainingCode.indexOf(']');
			if (argumentStop < 0) {
			    netLogoModel.warn("Expected to find a list with square brackets in " + code);
			    return;
			}
			String behavioursString = remainingCode.substring(1, argumentStop);
			StringBuilder behaviourDescriptions = new StringBuilder();
			// old BehaviourComposer 1.0 quoted names
			String behaviour[] = behavioursString.split("\"");
			for (int j = 1; j < behaviour.length; j += 2) {
			    processRequiredBehaviour(netLogoModel, newCode, behaviour[j]);
			    behaviourDescriptions.append(behaviour[j]);
			    behaviourDescriptions.append(' ');
			}
//			int newArgumentStop = newCode.indexOf("]");
			// comments not needed now that micro-behaviour names do the same job better
//			// add a comment for clarity of the NetLogo code
//			newCode.insert(newArgumentStop, "\n; " + behaviourDescriptions.toString() + "\n");
			remainingCode = remainingCode.substring(argumentStop + 1);
			location = remainingCode.indexOf(NetLogoModel.proceduresWithBehaviourArguments[i], argumentStop + 1);
		    }
		}
	    }
	}
	// since don't want to transform twice store the result
	// Also make sure no non-breaking spaces remain (either as HTML or as UTF-8)
	transformedBehaviourCode = 
	    CommonUtils.replaceNonBreakingSpaces(newCode.toString().replaceAll("&nbsp;", " "));
    }

    private void processRequiredBehaviour(NetLogoModel netLogoModel, StringBuilder newCode, String requiredBehaviourName)
	    throws NetLogoException {
	MicroBehaviour referencedMicroBehaviour = 
	    netLogoModel.addRequiredBehaviourName(requiredBehaviourName, null, this); 
	if (referencedMicroBehaviour != null) {
	    referencedMicroBehaviours.add(referencedMicroBehaviour);
	    int nameStart = newCode.indexOf(requiredBehaviourName);
	    if (nameStart >= 0) {
		int nameEnd = nameStart+requiredBehaviourName.length();
		if (referencedMicroBehaviour.isRawNetLogoCode()) {
		    // no need to add this at run-time so remove it from the code
		    newCode.replace(nameStart, nameEnd, "");
		} else {
		    String referencedBehaviourName = referencedMicroBehaviour.getBehaviourName(false);
//		    if (addComment) {
//			// a comment for clarity
//			// replace the existing closing quote
//			// in order to add comment afterwards
//			referencedBehaviourName += "\"\n; " + requiredBehaviourName + "\n";
//			nameEnd++; // to add comment replace " as well
//		    }
		    newCode.replace(nameStart, nameEnd, referencedBehaviourName);
		}
	    }
	}
    }
    
    protected void updateVariables(NetLogoModel netLogoModel) throws NetLogoException {
	netLogoModel.findAllKindsOfVariables(getTransformedBehaviourCode());
    }

    protected void updateBreedVariables(NetLogoModel netLogoModel) throws NetLogoException {
	netLogoModel.findBreedVariables(getTransformedBehaviourCode());
    }

    protected void updateGlobalVariables(NetLogoModel netLogoModel) throws NetLogoException {
	netLogoModel.findGlobalVariables(getTransformedBehaviourCode());
    }
    
    protected void updatePatchVariables(NetLogoModel netLogoModel) throws NetLogoException {
	netLogoModel.findPatchVariables(getTransformedBehaviourCode());
    }
    
    protected void updateLinkVariables(NetLogoModel netLogoModel) throws NetLogoException {
	netLogoModel.findLinkVariables(getTransformedBehaviourCode());
    }
    
    protected String getBehaviourCodeUnprocessed() {
	return behaviourCode;
    }

    protected String getBehaviourCode() throws NetLogoException {
	// throws Exception since subclass does
	// some callers don't have a netLogoModel
	if (behaviourCode == null) {
	    Level level = getBehaviourURL() == null ? Level.WARNING : Level.SEVERE;
	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).log(
		    level,
		    "getBehaviourCode called but behaviourCode is null. Url is " + getBehaviourURL());
	    return null;
	}
	if (!enhancementsInstalled && enhancements != null && !enhancements.isEmpty()) {
	    int originalTextAreasCount = textAreaElements == null ? 0 : textAreaElements.size()/2;
	    installEnhancements(originalTextAreasCount);
	}
	String description = CommonUtils.getDescription(getBehaviourDescription());
	String result;
	if (!description.isEmpty()) {
	    String comment = CommonUtils.comment(description);
	    result = comment + "\n" + behaviourCode;
	} else {
	    result = behaviourCode;
	}
	Set<Entry<Integer, String>> textAreaValuesEntrySet = textAreaValues.entrySet();
	for (Entry<Integer, String> entry : textAreaValuesEntrySet) {
	    Integer index = entry.getKey();
	    if (index >= 0) { // ignore name changes here
		String replacement = entry.getValue();
		if (replacement != null) {
		    if (!CommonUtils.TOKEN_FOR_REMOVED_TEXT_AREA.equals(replacement)) {
			result = result.replace(ServerUtils.textAreaPlaceHolder(index), replacement.trim());
		    }
		} else {
		    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
			    "Replacement of a text area #" + 
			    index + " is null. " + result + 
			    " url=" + getBehaviourURL());
		}
	    }
	}
	if (macroBehaviours != null) {
	    for (MacroBehaviour macroBehaviour : macroBehaviours) {
		String name = macroBehaviour.getObjectName();
		String placeHolder = ServerUtils.macroBehaviourPlaceHolder(name);
		ArrayList<MicroBehaviour> microBehaviours = macroBehaviour.getMicroBehaviours();
		StringBuffer microBehaviourList = new StringBuffer("[");
		for (MicroBehaviour microBehaviour : microBehaviours) {
		    if (!microBehaviour.isRawNetLogoCode() && macroBehaviour.isActive(microBehaviour)) {
			microBehaviourList.append(microBehaviour.getBehaviourName(false));
			microBehaviourList.append("\n "); // the space is so the code isn't at the left edge
		    }
		}
		microBehaviourList.append(']');
		result = result.replace(placeHolder, microBehaviourList.toString());
	    }
	}
	// make sure there are no non-breaking spaces to interfere with NetLogo
	// not sure how the 160 (non-breaking space got there but this gets rid of it)
	transformedBehaviourCode = CommonUtils.replaceNonBreakingSpaces(result.replaceAll("&nbsp;", " ")).replace((char) 160, (char) 32);
	return transformedBehaviourCode;
    }
    
    private void installEnhancements(int nextTextAreaIndex) {
	for (MicroBehaviourEnhancement enhancement : enhancements) {
	    nextTextAreaIndex = enhanceCode(enhancement, nextTextAreaIndex);
	}
    }

    public void updateTextArea(String newContents, int index) {
	if (newContents == null) {
	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
		    "Updating text area#" + index + " with null. url=" + this.getBehaviourURL());
	}
	textAreaValues.put(index, newContents);
	transformedBehaviourCode = null; // no longer valid
	if (index == -1) {
	   setBehaviourDescriptionAndNameHTML(newContents);
	   setBehaviourDescription();
	}
    }
       
    public String getTransformedBehaviourCode() throws NetLogoException {
	if (transformedBehaviourCode != null) {
	    return transformedBehaviourCode;
	}
	return getBehaviourCode();
    }

    protected void setBehaviourCode(String newCode) {
	if (newCode != null) {
	    // replace UTF-8 encoding of non-breaking space and the like with ASCII space
	    behaviourCode = CommonUtils.replaceNonBreakingSpaces(newCode);
	    originalBehaviourCode = behaviourCode;
	} else {
	    behaviourCode = null;
	    System.err.println("behaviourCode set to null");
	}
    }
    
    protected static String[] splitOnSquareBrackets(String s, String operation, ClientState clientState) {
	// returns 3 strings:
	// before square bracketed code, contents of square brackets, after square bracketed code
	// or null if no square brackets
	return splitOnSquareBracketsInternal(s, 0, operation, clientState);
    }
	
    private static String[] splitOnSquareBracketsInternal(
	    String s, int earliestOpenSquareBracket, String operation, ClientState clientState) {
	// if it begins with parentheses then don't consider square brackets inside
	if (operation != null) {
	    int openParenthesis = CommonUtils.indexOfNetLogoCodeOnly('(', s, earliestOpenSquareBracket);
//	    List<String> conditionals = Arrays.asList("if", "ifelse", "if-else", "if-else-value", "while");
//	    if (openParenthesis < 0 && conditionals.contains(operation.toLowerCase())) {
		// TODO: act as if the conditional was in parentheses. 
//	    }
	    if (openParenthesis >= 0) { 
		String beforeParenthesis = s.substring(operation.length(), openParenthesis).trim();
		if (beforeParenthesis.isEmpty()) {
		    int closeParenthesis = CommonUtils.indexOfNetLogoCodeOnly(')', s, openParenthesis);
		    if (closeParenthesis >= 0) {
			earliestOpenSquareBracket = closeParenthesis+1;
		    }
		}
	    }
	}
	int openBracket = CommonUtils.indexOfNetLogoCodeOnly('[', s, earliestOpenSquareBracket);
	if (openBracket < 0) {
	    return null;
	}
	String beforeOpenBracket = s.substring(0, openBracket);
	String lastWord = CommonUtils.lastWord(beforeOpenBracket);
	if (operation != null && lastWord != null && !lastWord.equals(operation)) {
	    for (int i = 0; i < operationsExpectingBracketedExpressionAsFirstArgument.length; i++) {
		if (operationsExpectingBracketedExpressionAsFirstArgument[i].equals(operation)) {
		    clientState.warn("Ignored everything between " + operation + " and [ in " + s);
		    break;
		}
	    }
	}
	if (isNetLogoPrimitiveRequiringSquareBracketsAfter(lastWord)) {
	    return splitOnSquareBracketsInternal(s, openBracket+1, operation, clientState);
	}
	int closeBracket = CommonUtils.closeBracket(s, openBracket+1);
	if (closeBracket < 0) {
	    clientState.warn("Could not find ] to match [ in " + s);
	    return null; 
	}
	// following caused Issue 739
	// seemed to be support for the older NetLogo set [x] of y z
//	String firstWord = CommonUtils.firstWord(s.substring(closeBracket+1));
//	if (isNetLogoPrimitiveRequiringSquareBracketsBefore(firstWord)) {
//	    return splitOnSquareBracketsInternal(s, closeBracket+1, operation, clientState);
//	}
	String pieces[] = new String[3];
	pieces[0] = beforeOpenBracket;
	pieces[1] = s.substring(openBracket+1, closeBracket); // remove brackets
	pieces[2] = s.substring(closeBracket+1);
	return pieces;
    }
    
    protected static boolean isNetLogoPrimitiveRequiringSquareBracketsAfter(String word) {
	if (word == null) return false;
	return word.equalsIgnoreCase("with"); // what else?
    }
    
    protected static boolean isNetLogoPrimitiveRequiringSquareBracketsBefore(String word) {
	if (word == null) return false;
	return word.equalsIgnoreCase("of") || word.equalsIgnoreCase("set"); // what else?
    }
    
    protected static String[] splitOnLineContaining(String[] separators, String s) {
	int earliestSeparatorIndex = Integer.MAX_VALUE;
	String earliestSeparator = null;
	for (int i = 0; i < separators.length; i++) {
	    int index = s.indexOf(separators[i]);
	    if (index >= 0 && index < earliestSeparatorIndex) {
		earliestSeparatorIndex = index;
		earliestSeparator = separators[i];
	    }
	}
	if (earliestSeparator == null) {
	    return null;
	} else {
	    String pieces[] = new String[2];
	    pieces[0] = s.substring(0, earliestSeparatorIndex).trim();
	    pieces[1] = s.substring(earliestSeparatorIndex).trim();
	    return pieces;
	}
    }

    protected static String[] splitOnLineContainingOnly(String separator, String s) {
	String lines[] = s.split("\n", 0); // all of them
	for (int i = 0; i < lines.length; i++) {
	    if (lines[i].trim().startsWith(separator)) {
		// was equalsIgnoreCase(separator) but need do-after 5 kind of thing
		String answer[] = new String[3];
		answer[0] = "";
		answer[1] = lines[i];
		answer[2] = "";
		for (int j = 0; j < i; j++) {
		    answer[0] = answer[0] + " " + lines[j].trim() + "\n";
		}
		for (int j = i + 1; j < lines.length; j++) {
		    answer[2] = answer[2] + " " + lines[j].trim() + "\n";
		}
		return answer;
	    }
	}
	return null;
    }

    public String getBehaviourName(boolean quote) {
	return getBehaviourName(quote, null);
    }
	
    public String getBehaviourName(boolean quote, ArrayList<MicroBehaviour> visitedMicroBehaviours) {
	if (behaviourURL == null) {
	    return null;
	}
	String name = getNetLogoName();
	if (quote) {
	    return "\"" + name + "\"";
	} else {
	    return name;
	}
    }
    
    public int enhanceCode(MicroBehaviourEnhancement enhancement, int textAreaIndex) {
	switch (enhancement) {
	case DO_EVERY:
	    behaviourCode = "do-every (" + textAreaValues.get(textAreaIndex++) + ")\n [" + behaviourCode + "]";
	    break;
	case DO_AFTER:
	    behaviourCode = "do-after (" + textAreaValues.get(textAreaIndex++) + ")\n [" + behaviourCode + "]";
	    break;
	case DO_AT_TIME:
	    behaviourCode = "do-at-time (" + textAreaValues.get(textAreaIndex++) + ")\n [" + behaviourCode + "]";
	    break;
	case DO_WITH_PROBABILITY:
	    behaviourCode = "do-with-probability (" + textAreaValues.get(textAreaIndex++) + ")\n [" + behaviourCode + "]";
	    break;
	case DO_IF:
	    behaviourCode = "do-if (" + textAreaValues.get(textAreaIndex++) + ")\n [" + behaviourCode + "]";
	    break;
	case DO_WHEN:
	    behaviourCode = "when [" + textAreaValues.get(textAreaIndex++) + "]\n [" + behaviourCode + "]";
	    break;
	case DO_WHENEVER:
	    behaviourCode = "whenever [" + textAreaValues.get(textAreaIndex++) + "]\n [" + behaviourCode + "]";
	    break;
	case ADD_VARIABLE:
	    behaviourCode = "let " + addParenthesesToValue(textAreaValues.get(textAreaIndex++).trim()) + "\n " + behaviourCode;
	    break;
	case ADD_COMMENT:
	    behaviourCode = textAreaValues.get(textAreaIndex++) + "\n" + behaviourCode;
	    break;
	}
	enhancementsInstalled = true;
	transformedBehaviourCode = null; // no longer valid
	return textAreaIndex;
    }
    
    private String addParenthesesToValue(String nameAndValue) {
	// changes name value to name (value)
	// otherwise can't parse edits inside of BC2NetLogo
	String[] parts = nameAndValue.split("(\\s)+", 2);
	if (parts.length < 2) {
	    return nameAndValue;
	} else {
	    return parts[0] + " ( " + parts[1] + " ) ";
	}
    }

    protected MicroBehaviour getReferringMicroBehaviour() {
        return null;
    }
    
    public String getBehaviourURL() {
        return behaviourURL;
    }
    
    protected void setBehaviourURL(String behaviourURL) {
	if (behaviourURL == null) {
	    ServerUtils.logError("Warning. URL of a micro-behaviour is null.");
	}
	this.behaviourURL = behaviourURL;
	// the URL is the true identity so make sure the names are unique
	// computed on demand now -- not enough is always known at the time this is called
//        if (behaviourURL != null) {
//            behaviourName = ServerUtils.convertURLToNetLogoName(behaviourURL, behaviourDescription);
//        }
    }

    protected ArrayList<MicroBehaviour> getReferencedMicroBehaviours() {
        return referencedMicroBehaviours;
    }

    protected void setReferencedMicroBehaviours(ArrayList<MicroBehaviour> referencedMicroBehaviours) {
        this.referencedMicroBehaviours = referencedMicroBehaviours;
    }

    public String getBehaviourDescription() {
        return behaviourDescription;
    }

    public String getBehaviourDescriptionHTML() {
        return behaviourDescriptionAndNameHTML;
    }
    
    public String getName() {
	return CommonUtils.getName(behaviourDescriptionAndNameHTML);
    }

    public HashMap<Integer, String> getTextAreaValues() {
        return textAreaValues;
    }

    /**
     * @return a list alternating between names and text area HTML elements or NULL
     */
    public ArrayList<String> getTextAreaElements() {
        return textAreaElements;
    }
    
    protected void setTextAreaElements(ArrayList<String> textAreaElements) {
        this.textAreaElements = textAreaElements;
    }

    public ArrayList<MacroBehaviour> getMacroBehaviours() {
        return macroBehaviours;
    }

    public void addMacroBehaviour(MacroBehaviour macroBehaviour) {
	transformedBehaviourCodeInvalid();
	if (macroBehaviours == null) {
	    macroBehaviours = new ArrayList<MacroBehaviour>();
	}
	macroBehaviours.add(macroBehaviour);
    }
//    
//    public void removeMacroBehaviour(MacroBehaviour macroBehaviour) {
//	transformedBehaviourCodeInvalid();
//	macroBehaviours.remove(macroBehaviour);
//    }
    
    public void resetMacroBehaviours() {
	if (macroBehaviours != null) {
	    transformedBehaviourCodeInvalid();
	    macroBehaviours.clear();
	}
    }
    
    private void transformedBehaviourCodeInvalid() {
	transformedBehaviourCode = null;
    }

    protected void setMacroBehaviours(ArrayList<MacroBehaviour> macroBehaviours) {
        this.macroBehaviours = macroBehaviours;
    }

    public boolean isMacroBehaviourAsMicroBehaviour() {
	return false;
    }

    public NetLogoModel getNetLogoModel() {
        return netLogoModel;
    }

    public void setNetLogoModel(NetLogoModel netLogoModel) {
        this.netLogoModel = netLogoModel;
    }

    public MicroBehaviourData getMicroBehaviourData() {
	// should be up to date -- no need to do this
//	createOrUpdateMicroBehaviourData(false);
	return microBehaviourData;
    }

    public List<MicroBehaviourEnhancement> getEnhancements() {
        return enhancements;
    }
    
    public void resetEnhancements() {
	enhancements.clear();
	behaviourCode = originalBehaviourCode;
	transformedBehaviourCode = null; // no longer valid
    }

    public void addEnhancement(MicroBehaviourEnhancement enhancement) {
	enhancements.add(enhancement);	
    }

    public boolean isEnhancementsInstalled() {
        return enhancementsInstalled;
    }

    public static String getNextSerialNumber(String nameWithoutSerialNumber) {
	// return serial number as string with preceding zeros to have a fixed length
	// equivalent-micro-behaviour? in NLS auxiliary file expects 5 digits
	// in order to work backwards from procedure name to micro-behaviour URL
	// we need to be sure that the NetLogo names are globally unique
	// hence maintaining the serial number in the data store
	NetLogoNameSerialNumber lastSerialNumber = 
		DataStore.begin().find(NetLogoNameSerialNumber.class, nameWithoutSerialNumber);
	if (lastSerialNumber == null) {
	    lastSerialNumber = new NetLogoNameSerialNumber(nameWithoutSerialNumber, 1);
	} else {
	    lastSerialNumber.incrementSerialNumber();
	}
	DataStore.begin().put(lastSerialNumber);
	return Integer.toString(100000 + lastSerialNumber.getSerialNumber()).substring(1);
    }

    public String getNetLogoName() {
	if (netLogoName == null) {
	    netLogoName = ServerUtils.convertURLToNetLogoName(behaviourURL, CommonUtils.getName(behaviourDescriptionAndNameHTML));
	}
        return netLogoName;
    }

    public String getBehaviourDescriptionAndNameHTML() {
        return behaviourDescriptionAndNameHTML;
    }

    public void setBehaviourDescriptionAndNameHTML(String behaviourDescriptionAndNameHTML) {
	if (behaviourDescriptionAndNameHTML == null) {
	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
		    "MicroBehaviour's setBehaviourDescriptionAndNameHTML called with null name.");
	} else {
	    this.behaviourDescriptionAndNameHTML = 
		    behaviourDescriptionAndNameHTML.replaceAll("&nbsp;", " ").replaceAll("&NBSP;", " ");
	}
    }

}
