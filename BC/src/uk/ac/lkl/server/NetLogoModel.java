package uk.ac.lkl.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import uk.ac.lkl.server.persistent.DataStore;
import uk.ac.lkl.server.persistent.ModelNetLogo;
import uk.ac.lkl.server.persistent.SessionExperiments;
import uk.ac.lkl.shared.CommonUtils;
import uk.ac.lkl.shared.NetLogoTokenizer;

public class NetLogoModel implements VariableCollector {

    private static final String SPACE_TOKEN = "|***space***|";
    protected ArrayList<MacroBehaviour> macroBehaviours = new ArrayList<MacroBehaviour>();
    protected ArrayList<MicroBehaviour> totalRequired = new ArrayList<MicroBehaviour>();
    protected ArrayList<String> widgetsToCreate = new ArrayList<String>();
    protected String commonBreedVariables[] = { "my-x", "my-next-x", "my-y", "my-next-y", "my-heading", "my-next-heading", "my-visibility", null} ;
    protected ArrayList<String> breedVariables = new ArrayList<String>();
    protected StringBuffer globalInitialisations = new StringBuffer();
    // my-location maybe should be removed but then micro-behaviours need updating
    // for the others is there a way to warn?
    protected String breedVariablesExceptions[] = { "my-location", "my-out-links", "my-in-links", "my-links"};
    protected String extraBreedVariables = " scheduled-behaviours behaviours-at-tick-start current-behaviours  current-behaviour behaviour-removals rules log-attributes\n kind dead previous-xcor previous-ycor previous-heading dirty";
    protected ArrayList<String> expectedGlobalVariables = new ArrayList<String>();
    protected ArrayList<String> encounteredGlobalVariables = new ArrayList<String>();
    protected ArrayList<String> globalVariablesToDeclare = new ArrayList<String>();
    protected String patchVariables[] = {};
    protected String linkVariables[] = {};
    protected ArrayList<String> generatedCommands = new ArrayList<String>();

    // alternates between old URL and new
    protected ArrayList<String> microBehaviourRenamings = new ArrayList<String>();

    static final int currentModelFileFormatVersionNumber = 2;
    static final int currentNetLogoEngineVersionNumber = 5;
    protected static int NetLogoEngineVersionNumber = currentNetLogoEngineVersionNumber;
    protected static int ModelFileFormatVersionNumber = currentModelFileFormatVersionNumber;
    protected static String proceduresWithBehaviourArguments[] = {
            "add-behaviours", "add-behaviours-to", 
            "add-copies", "add-copy-of-another", "add-copies-of-another", "create-agents", "create-objects", "create-agents-from-data",
    "remove-behaviours"};
    protected static String proceduresWithOneBehaviourArgument[] = {
            "add-behaviour", "add-behaviour-to", 
            "add-copy", "remove-behaviour"};
    protected int minPxcor;
    protected int maxPxcor;
    protected int minPycor;
    protected int maxPycor;
    protected int minPzcor;
    protected int maxPzcor;
    protected double patchSize;
    protected int worldLLX;
    protected int worldLLY;
    protected int worldURX;
    protected int worldURY;
    protected int labelFontSize;
    protected int horizontallyWrapping;
    protected int verticallyWrapping;
    protected static final int preferredWidth = 1280;
    protected static final int initialgraphXCoordinate = -104;
    protected int graphXCoordinate = initialgraphXCoordinate;
    protected int dimensions = 2;
    protected boolean tickBasedUpdates = true;
    protected double frameRate = 30.0;
    protected boolean ShowTickCounter = true;
    protected String tickLabel = "time";
    // the following was ConcurrentHashMap but that caused Google App Engine errors when getting the set of keys
    protected HashMap<String, Boolean> shapesReferenced = new HashMap<String, Boolean>();
    protected ConcurrentHashMap<String, String> urlToShapeDefinitionMap = new ConcurrentHashMap<String, String>();
    // following used to generate NetLogo lists such as behaviour-procedure-numbers
    //    protected ArrayList<MicroBehaviour> behaviours = new ArrayList<MicroBehaviour>();
    protected StringBuilder behaviourNames = new StringBuilder();
    protected ClientState clientState;
    final static private String modelWidgetsText = "BUTTON\n5\n5\n66\n40\nSETUP\nsetup\nNIL\n1\nT\nOBSERVER\nNIL\nS\nNIL\nNIL\n1\n\nBUTTON\n5\n45\n66\n80\nGO\ngo\nT\n1\nT\nOBSERVER\nNIL\nG\nNIL\nNIL\n1\n\nBUTTON\n5\n85\n68\n118\nPAUSE\nset stop-running true\nNIL\n1\nT\nOBSERVER\nNIL\n.\nNIL\nNIL\n1\n\n";
    //    final static private String modelDocumentationText = "WHAT IS IT?\n-----------\nThis section could give a general understanding of what the model is trying to show or explain.\n\n\nHOW IT WORKS\n------------\nThis section could explain what rules the agents use to create the overall behavior of the model.\n\n\nHOW TO USE IT\n-------------\nThis section could explain how to use the model, including a description of each of the items in the interface tab.\n\n\nTHINGS TO NOTICE\n----------------\nThis section could give some ideas of things for the user to notice while running the model.\n\n\nTHINGS TO TRY\n-------------\nThis section could give some ideas of things for the user to try to do (move sliders, switches, etc.) with the model.\n\n\nEXTENDING THE MODEL\n-------------------\nThis section could give some ideas of things to add or change in the procedures tab to make the model more complicated, detailed, accurate, etc.\n\n\nNETLOGO FEATURES\n----------------\nThis section could point out any especially interesting or unusual features of NetLogo that the model makes use of, particularly in the Procedures tab.  It might also point out places where workarounds were needed because of missing features.\n\n\nRELATED MODELS\n--------------\nThis section could give the names of models in the NetLogo Models Library or elsewhere which are of related interest.\n\n\nCREDITS AND REFERENCES\n----------------------\nThis section could contain a reference to the model's URL on the web if it has one, as well as any other necessary credits or references.\n\n";
    final static private String linkShapesText = "default\n0.0\n-0.2 0 0.0 1.0\n0.0 1 1.0 0.0\n0.2 0 0.0 1.0\nlink direction\ntrue\n0\nLine -7500403 true 150 150 90 180\nLine -7500403 true 150 150 210 180\n";
    final static private String netLogoEngineDeclarations = "breed [objects object]\n\n";
    private static final String BEGIN_NET_LOGO_SHAPE_TOKEN = "BehaviourComposer begin NetLogo shape:";
    private static final String END_NET_LOGO_SHAPE_TOKEN = "BehaviourComposer end NetLogo shape";
    protected ResourcePageServiceImpl resourcePageServiceImpl = null;
    private boolean outputAreaSpecified = false;
    //    private boolean addClockedSwitch = true;
    private boolean addDefaultButtons = true;
    //    private String procedureNamesToActions = "";
    private ArrayList<String> unneededCommands = new ArrayList<String>();
    private boolean outputAreaNeeded;
    private boolean onlyForFetchingCode = false;
    public final ArrayList<String> commands = new ArrayList<String>(Arrays.asList(
            // generated ones:
            "initialise-object", "initialise-previous-state", "initialise-globals", 
            // Behaviour Composer commands:
            "create-objects", "do-every", "do-at-setup", "do-after-setup", "when", "whenever", "do-after",
            "do-at-time", "do-now", "do-for-n",
            "do-repeatedly", "do-with-probability", "do-with-probabilities", 
            "add-behaviour", "add-behaviours", "add-behaviours-to", "add-behaviour-to", 
            "add-link-behaviour", "add-link-behaviours", "add-link-behaviour-after", "add-link-behaviours-after",
            "remove-behaviours-from", "remove-all-behaviours", "remove-all-behaviours-from",
            "ask-every-patch", "go-forward", "turn-right", "turn-left",
            "move-horizontally-or-vertically-towards-another", "log-log-histogram",
            "add-copies", "remove-behaviours",
            "run-in-observer-context", "add-to-plot", "set-world-geometry",
            "go", "setup",
            // NetLogo:
            "ask", "ask-concurrent", "auto-plot-off", "auto-plot-on", "back", "bk", "beep",
            "breed", "carefully", "clear-all", "ca",
            "clear-all-plots", "clear-drawing", "cd", "clear-links", "clear-output", "clear-patches", 
            "cp", "clear-plot", "clear-turtles", "ct", "create-ordered-turtles",
            "create-ordered-objects", "cro", "create-histogram", "create-link-to", "create-links-to",
            "create-link-from", "create-links-from", "create-link-with", "create-links-with", "create-plot",
            "create-turtles",
            "crt", "create-objects", "create-temporary-plot-pen", "die", "diffuse", "diffuse4", 
            "directed-link-breed", "display", "downhill", "downhill4", 
            "end", "error-message", "every", "export-view", "export-interface",
            "export-output", "export-plot", "export-all-plots", "export-world", "extensions", 
            "face", "facexy", "file-close", "file-close-all", "file-delete",
            "file-flush", "file-open", "file-print", "file-show", "file-type", "file-write", 
            "follow", "follow-me", "foreach", "forward", "fd", "globals", "hatch", "hatch-objects", 
            "hide-link", "hide-turtle", "ht", "histogram", "home", "hubnet-broadcast",
            "hubnet-broadcast-clear-output", "hubnet-broadcast-message", "hubnet-broadcast-view",
            "hubnet-clear-override", "hubnet-clear-overrides", "hubnet-enter-message?", "hubnet-exit-message?", 
            "hubnet-fetch-message", "hubnet-message", "hubnet-message-source", "hubnet-message-tag",
            "hubnet-message-waiting?", "hubnet-reset", "hubnet-reset-perspective", "hubnet-send", 
            "hubnet-send-clear-output", "hubnet-send-follow", "hubnet-send-message", "hubnet-send-override", 
            "hubnet-send-watch", "hubnet-set-client-interface", "if", "ifelse", "if-else", 
            "import-drawing", "import-pcolors", "import-pcolors-rgb", "import-world", 
            "includes", "inspect", "jump", 
            "layout-circle", "layout-magspring", "layout-radial", "layout-spring", 
            "layout-tutte", "left", "lt", "let", "link", "links-own", "link-breeds-own", 
            "loop", "move-to", "movie-cancel", "movie-close", "movie-grab-view",
            "movie-grab-interface", "movie-set-frame-rate", "movie-start", "movie-status", "new-seed",
            "no-display", "output-print", "output-show",
            "output-type", "output-write", "patches-own", "pen-down", "pd", "pen-erase", "pe", "pen-up", "pu",
            "plot", "plot-pen-down", "plot-pen-up", "plot-pen-reset", "plotxy", 
            "print", "repeat", "report", "reset-perspective", "rp", "reset-ticks", "reset-timer", "resize-world",
            "ride", "ride-me", "right", "rt", "run", "set", "set-current-directory", "set-current-plot",
            "set-current-plot-pen", "set-default-shape", "set-histogram-num-bars", "set-line-thickness",
            "set-patch-size", "set-plot-pen-color", "set-plot-pen-interval", "set-plot-pen-mode",
            "set-plot-x-range", "set-plot-y-range", "setxy", "show", 
            "show-turtle", "st", "show-link", "sprout", 
            "sprout-objects", "stamp", "stamperase", "stop", 
            "startup",
            "tick", "tick-advance", "tie", "turtles-own", "breeds-own", "type", 
            "untie", "uphill", "uphill4", "user-file", "user-new-file", "user-input",
            "user-message", "user-one-of", "wait", "watch", 
            "watch-me", "while", "without-interruption", "write",
            // and some 3D ones:
            "tilt-down", "tilt-up", "roll-left", "roll-right", "setxyz", "facexyz",
            "oxcor", "oycor", "ozcor", "zoom", "link-pitch"
            ));
    private ArrayList<String> generatedCommandNames;
    public final ArrayList<String> mathUtilities = new ArrayList<String>(Arrays.asList(
            "random-integer-between",
            "random-number-between",
            "random-item",
            "unit-vector",
            "add",
            "subtract",
            "multiply",
            "divide",
            "add",
            "is-zero?",
            "reciprocal",
            "within-range",
            "power-law-random",
            "power-law-list",
            "power-law-list-no-zeros",
            "hfunction",
            "hinv",
            "log-log-histogram"));
    private boolean mathUtilitiesNeeded = false;
    public final ArrayList<String> locationUtilities = new ArrayList<String>(Arrays.asList(
            "canonical-vector",
            "make-location",
            "my-location",
            "location",
            "direction-to-heading",
            "direction-vector",
            "turn-by",
            "angle-from-me",
            "heading-to-direction",
            "heading-towards",
            "heading-towards-another"));
    private boolean locationUtilitiesNeeded = false;
    public final ArrayList<String> patchUtilities = new ArrayList<String>(Arrays.asList(
            "random-unoccupied-location",
            "random-selection-of-unoccupied-locations",
            "random-location-found-to-be-unoccupied"));
    private boolean patchUtilitiesNeeded = false;
    public final ArrayList<String> patchUtilitiesDeprecated = new ArrayList<String>(Arrays.asList(
            "unoccupied-locations",
            "patches-between"));
    private boolean patchUtilitiesDeprecatedNeeded = false;
    public final ArrayList<String> moveHorizontallyOrVerticallyCommands = new ArrayList<String>(Arrays.asList(
            "move-horizontally-or-vertically-towards-another",
            "move-horizontally-or-vertically-towards-patch"));
    private boolean moveHorizontallyOrVerticallyCommandsNeeded = false;
    public final ArrayList<String> distanceUtilities = new ArrayList<String>(Arrays.asList(
            "distance-to-me",
            "distance-within",
            "distance-between"));
    private boolean distanceUtilitiesNeeded = false; 
    private boolean canonicalDistanceNeeded = false;
    private String declarations = "";
    private String extensions = "";
    private final ArrayList<String> topLevelProcedures = new ArrayList<String>(Arrays.asList(
            "the-model", "update-attributes", "update-turtle-state", "update-all-turtle-states",
            "initialise-attributes", "initialise-previous-state", "initialise-patch-attributes", "initialise-globals", "initialise-object",
            "setup", "go", "startup"));
    private boolean useAuxiliaryFile;
    // allow very long lines but there needs to be a limit -- see for example Issue 962
    private int maxLineLength = 160;
    private String commentsAboutglobalsWithInterface = "";
    private HashMap<String, String> globalVariableComments = new HashMap<String, String>();

    public NetLogoModel(ResourcePageServiceImpl resourcePageServiceImpl, boolean useAuxiliaryFile, ClientState clientState) {
        super();
        this.resourcePageServiceImpl = resourcePageServiceImpl;
        this.useAuxiliaryFile = useAuxiliaryFile;
        this.clientState = clientState;
        resourcePageServiceImpl.setNetLogoModel(this);
    }
    /**
     * Generates and writes the contents of the NLOGO file
     * 
     * @param clientState   hold client state including browser client description
     * @param tabInfo 
     * @return            String[6] containing 
     * the unique ID, -- the GUID or a string beginning with "Error"
     * the applet width, 
     * the applet height, 
     * list of URL renamings
     * number of dimensions for the model (2 or 3)
     * any warnings to communicate back
     */
    protected String[] generateNLogoFile(String modelGuid, String tabInfo, boolean forWebVersion, ClientState clientState) {
        String answer[] = new String[6];
        //	procedureNamesToActions = "";
        graphXCoordinate = initialgraphXCoordinate; 
        // reset location of graphs without explicit coordinates
        String netLogoFileContents = "";
        try {
            initialiseWorldParameters();
            String code = netLogoCode(modelGuid, forWebVersion);
            answer[1] = Integer.toString(getAppletWidth());
            answer[2] = Integer.toString(getAppletHeight());
            String separator = CommonUtils.NETLOGO_SECTION_SEPARATOR_AND_NEW_LINE;
            int halfWidth;
            if (maxPxcor == -minPxcor) {
                halfWidth = maxPxcor;
            } else {
                halfWidth = -1; // this is what NLOGO files seem to do
            }
            int halfHeight;
            if (maxPycor == -minPycor) {
                halfHeight = maxPycor;
            } else {
                halfHeight = -1; // this is what NLOGO files seem to do
            }
            String extraWidgets = "";
            for (String widget : widgetsToCreate) {
                extraWidgets += widget + "\n";
            }
            widgetsToCreate.clear(); // reset for next time
            updateWorldURXY(); // if never set use default
            if (!outputAreaSpecified && outputAreaNeeded) {
                // no output area explicitly provided and one is needed
                int outputAreaBottom = 53;
                extraWidgets += "\nOUTPUT\n" + 
                        "0\n" +
                        (worldURY + 5) + "\n" +
                        worldURX + "\n" +
                        (worldURY + outputAreaBottom) + "\n" +
                        "12\n"; // 12 point font
                worldURY += outputAreaBottom; // need more room for the applet if output area included
            }
            // see https://github.com/NetLogo/NetLogo/wiki/Widget-Format
            String world = "GRAPHICS-WINDOW\n" + worldLLX + "\n"
                    + worldLLY + "\n" + worldURX + "\n" + worldURY + "\n" 
                    + halfWidth + "\n"
                    + halfHeight + "\n"
                    + patchSize  + "\n"
                    + 1 + "\n"
                    + labelFontSize + "\n" + 1 + "\n" + 1 + "\n" + 1
                    + "\n" + 0 + "\n" + horizontallyWrapping + "\n"
                    + verticallyWrapping + "\n"
                    + 1 + "\n"
                    + minPxcor + "\n" + maxPxcor + "\n"
                    + minPycor + "\n" + maxPycor + "\n";
            if (dimensions == 3) {
                world += minPzcor + "\n" + maxPzcor + "\n";
            }
            // new on 260907 extra stuff in NetLogo 4.0 to
            // deal with ticks and continuous vs tick-mode
            // on 9/10/12
            //	    world += "0\n0\n1\ntime\n30.0\n";
            if (tickBasedUpdates) {
                world += "1\n0\n";
            } else {
                world += "0\n0\n";
            }
            world += (ShowTickCounter ? "1" : "0") + "\n" + tickLabel + "\n" + frameRate + "\n";
            if (!extensions.isEmpty() && !declarations.contains("extensions")) {
                declarations += "extensions [ " + extensions + "]\n\n";
            }
            String modelDocumentationText;
            if (tabInfo == null) {
                modelDocumentationText = CommonUtils.getDefaultInfoTabContents();
            } else {
                modelDocumentationText = tabInfo;
            }
            code = replaceTasks(code);
            netLogoFileContents = declarations
                    + code
                    + separator
                    + world
                    + "\n"
                    + (addDefaultButtons ? modelWidgetsText : "")
                    + extraWidgets
                    + "\n"
                    + separator
                    // other widgets too
                    + modelDocumentationText
                    + "\n"
                    + separator
                    + shapesUsed()
                    + separator + "NetLogo" + (dimensions == 3 ? " 3D" : "") + " 6.0.1\n" + separator 
                    + linkShapesText + 
                    separator + separator + separator + separator + 
                    "default\n" +
                    "0.0\n" +
                    "-0.2 0 0.0 1.0\n" +
                    "0.0 1 1.0 0.0\n" +
                    "0.2 0 0.0 1.0\n" +
                    "link direction\n" +
                    "true\n" +
                    "0\n" +
                    "Line -7500403 true 150 150 90 180\n" +
                    "Line -7500403 true 150 150 210 180\n" +
                    "\n" +
                    separator +
                    "0\n" +
                    separator;
            StringBuffer microBehaviourRenamingsFlattened = new StringBuffer();
            for (String url : microBehaviourRenamings) {
                microBehaviourRenamingsFlattened.append(url);
                microBehaviourRenamingsFlattened.append('<'); // can't be in a URL
            }
            answer[3] = microBehaviourRenamingsFlattened.toString();
            answer[4] = Integer.toString(dimensions);
            answer[5] = clientState.getAndRecordWarningsToSendBackToClient();
            SessionExperiments sessionExperiments = DataStore.begin().find(SessionExperiments.class, clientState.getSessionGuid());
            if (sessionExperiments != null) {
                netLogoFileContents = ServerUtils.insertExperiments(sessionExperiments.getExperiments(), netLogoFileContents);
            }
            if (dimensions == 2 && contains3DCode(netLogoFileContents)) {
                netLogoFileContents = addDummy3DPrimitives(netLogoFileContents);
            }
            netLogoFileContents = removeDeadCode(netLogoFileContents);
            // remove extra blank lines
            netLogoFileContents = netLogoFileContents.replaceAll("(\r?\n){3,}", "\n\n");
            ServerUtils.persist(new ModelNetLogo(modelGuid, netLogoFileContents));
            answer[0] = modelGuid;
            // TODO: determine if the following catch is redundant
        } catch (NetLogoException e) {
            ServerUtils.logException(e, "Error " + e.getMessage() + " in generating the model. Contents are: \n"
                    + netLogoFileContents);
            answer[0] = "Error generating the NetLogo file: " + e.getMessage();
        } catch (Exception e) {
            ServerUtils.logException(e, "Error " + e.toString() + " in generating or running the model. Contents are: \n"
                    + netLogoFileContents);
            answer[0] = "Error while generating the NetLogo file " + e.toString();
        }
        return answer;
    }

    private String replaceTasks(String code) {
        return code.replace("task [", "[ [] -> ");
    }
    
    private String removeDeadCode(String netLogoFileContents) {
        return removeDeadCode("", netLogoFileContents);
    }

    private String removeDeadCode(String processedAlready, String remaining) {
        // recursive version ran out of memory
        int procedureStart = remaining.indexOf("\nto");
        int sectionEnd = remaining.indexOf(CommonUtils.NETLOGO_SECTION_SEPARATOR);
        while (procedureStart >= 0) {
            if (sectionEnd < procedureStart) {
                // done -- \nto is something else -- e.g. a widget label
                return processedAlready + remaining;
            }
            int nameStart = remaining.indexOf(" ", procedureStart)+1;
            int nextSpace = remaining.indexOf(" ", nameStart);
            int nextNewLine = remaining.indexOf("\n", nameStart);
            int nextReturn = remaining.indexOf("\r", nameStart);
            int nameEnd = minimumIgnoringNegativeValues(nextSpace, minimumIgnoringNegativeValues(nextReturn, nextNewLine));
            int procedureEnd = remaining.indexOf("\nend", nameEnd)+5;
            String before = remaining.substring(0, procedureStart);
            String procedure = remaining.substring(procedureStart, procedureEnd);
            String after = remaining.substring(procedureEnd);
            // shouldn't consider any part of the file after the widget section
            // since 'Info' tab can contain strings
            int nextSeparator = after.indexOf(CommonUtils.NETLOGO_SECTION_SEPARATOR);
            nextSeparator = after.indexOf(CommonUtils.NETLOGO_SECTION_SEPARATOR, nextSeparator+CommonUtils.NETLOGO_SECTION_SEPARATOR.length());
            String afterContainsCode = after.substring(0, nextSeparator);
            String procedureName = remaining.substring(nameStart, nameEnd);
            if (containsProcedureCall(afterContainsCode, procedureName) || 
                    containsProcedureCall(processedAlready, procedureName) || 
                    topLevelProcedures.contains(procedureName) ||
                    // keep explicit commands --- good for typing to command center
                    procedureName.endsWith("-command")) {
                processedAlready = processedAlready + before + procedure;
            } else {
                processedAlready = processedAlready + before;
            }
            remaining = after;
            procedureStart = remaining.indexOf("\nto");
            sectionEnd = remaining.indexOf(CommonUtils.NETLOGO_SECTION_SEPARATOR);
        }
        return processedAlready + remaining;	
    }

    public static boolean containsProcedureCall(String code, String procedureName) {
        //	return Pattern.matches("(?s).*[(\\s)(\\[)(\\()]" + procedureName.replace("?", "\\?") + "[(\\s)(\\])(\\))](?s).*", code);
        // rewrote the above since includes occurrences in comments
        if (procedureName.isEmpty()) {
            return false;
        }
        int index = code.indexOf(procedureName);
        if (index == 0) {
            // special case where procedure if first thing in the code
            code = " " + code;
            index = 1;
        }
        while (index >= 0) {
            char previousCharacter = code.charAt(index-1);
            int nextIndex = index + procedureName.length();
            if (previousCharacter == ' ' ||
                    previousCharacter == '\n' ||
                    previousCharacter == '[' ||
                    previousCharacter == '(' ||
                    previousCharacter == ']' ||
                    previousCharacter == ')' ||
                    previousCharacter == '\t') {
                if (!CommonUtils.isInAComment(code, index)) {
                    if (nextIndex <= code.length()) {   
                        char followingCharacter = code.charAt(nextIndex);
                        if (followingCharacter == ' ' ||
                                followingCharacter == ']' ||
                                followingCharacter == ')' ||
                                followingCharacter == '\n' ||
                                followingCharacter == '\r' ||
                                followingCharacter == '\t') {
                            return true;
                        }
                    }
                }
            }
            index = code.indexOf(procedureName, nextIndex);
        }
        return false;
    }

    private int minimumIgnoringNegativeValues(int a, int b) {
        if (a < 0) {
            return b;
        } else if (b < 0) {
            return a;
        } else {
            return Math.min(a, b);
        }
    }

    private String addDummy3DPrimitives(String netLogoFileContents) {
        int turtlesOwnIndexStart = netLogoFileContents.indexOf("turtles-own [");
        if (turtlesOwnIndexStart < 0) {
            // should warn
            return netLogoFileContents;
        }
        int turtlesOwnIndexEnd = CommonUtils.indexOfNetLogoCodeOnly(']', netLogoFileContents, turtlesOwnIndexStart);
        netLogoFileContents = netLogoFileContents.substring(0, turtlesOwnIndexEnd) 
                + (contains3DTurtleVariables(netLogoFileContents) ? CommonUtils.listWithSeparator(CommonUtils.NETLOGO_TURTLE_VARIABLES_3D, " ") : "") + "\n" 
                + netLogoFileContents.substring(turtlesOwnIndexEnd);
        int patchesOwnIndexStart = netLogoFileContents.indexOf("patches-own [");
        if (patchesOwnIndexStart >= 0) {
            // following finds next ] ignoring comments  
            int patchesOwnIndexEnd = CommonUtils.indexOfNetLogoCodeOnly(']', netLogoFileContents, patchesOwnIndexStart);
            netLogoFileContents = netLogoFileContents.substring(0, patchesOwnIndexEnd) + "\n  " 
                    + (contains3DPatchVariables(netLogoFileContents) ? CommonUtils.listWithSeparator(CommonUtils.NETLOGO_PATCH_VARIABLES_3D, " ") : "") + "\n" 
                    + netLogoFileContents.substring(patchesOwnIndexEnd);
        } else {
            netLogoFileContents = netLogoFileContents.substring(0, turtlesOwnIndexStart) + "\npatches-own [" 
                    + (contains3DPatchVariables(netLogoFileContents) ? CommonUtils.listWithSeparator(CommonUtils.NETLOGO_PATCH_VARIABLES_3D, " ") : "") + "]\n\n" 
                    + netLogoFileContents.substring(turtlesOwnIndexStart);
        }
        int codeEndIndex = netLogoFileContents.indexOf(CommonUtils.NETLOGO_SECTION_SEPARATOR);
        if (codeEndIndex < 0) {
            // should warn
            return netLogoFileContents;
        }
        String dummy3DPrimitives = getNetLogoCode("dummy-netlogo-3d-primitives");
        netLogoFileContents = netLogoFileContents.substring(0, codeEndIndex) + "\n" + dummy3DPrimitives + "\n" + netLogoFileContents.substring(codeEndIndex);
        return netLogoFileContents;
    }

    protected void updateWorldURXY() {
        // need to add a bit for borders and bar
        worldURX = 10+(int) Math.round(worldLLX + patchSize * (1+maxPxcor-minPxcor));
        worldURY = 31+(int) Math.round(worldLLY + patchSize * (1+maxPycor-minPycor));
    }

    // I think the following was needed for Java applets but it confused the applet size with the world view size
    //    protected void updateWorldURXY() {
    //	// need to add a bit for borders and bar
    //	int defaultWorldURX = 10+(int) Math.round(worldLLX + patchSize * (1+maxPxcor-minPxcor));
    //	int defaultWorldURY = 31+(int) Math.round(worldLLY + patchSize * (1+maxPycor-minPycor));
    //	if (defaultWorldURX > worldURX) {
    //	    worldURX = defaultWorldURX;
    //	}
    //	if (defaultWorldURY > worldURY) {
    //	    worldURY = defaultWorldURY;
    //	}
    //    }

    //    private String codeOKWithNetLogoCompiler(String NetLogoFileContents, String folderName) {
    //	HeadlessWorkspace workspace = new HeadlessWorkspace();
    //	String result = null;
    //        try {
    //            StringBuffer absolutePathForIncludes = new StringBuffer(NetLogoFileContents);
    //            int includeFileNameIndex = NetLogoFileContents.indexOf(CommonUtils.COMMON_BEHAVIOUR_COMPOSER_NLS);
    //            if (includeFileNameIndex >= 0) {
    //        	absolutePathForIncludes.insert(includeFileNameIndex, folderName.replace('\\', '/'));
    //            }
    //	    workspace.openFromSource(absolutePathForIncludes.toString());
    //        }
    //        catch(Exception e) {
    //            e.printStackTrace();
    //            // startPos and endPos in e might be useful..
    //            result = e.getLocalizedMessage();
    //        }
    //	return result;
    //    }

    protected String shapesUsed() {
        String defaultShape = NetLogoShapes.getShape("default");
        if (defaultShape == null) {
            defaultShape = "";
        }
        StringBuilder shapes = new StringBuilder(defaultShape);
        for (String shapeName : shapesReferenced.keySet()) {
            String shape = NetLogoShapes.getShape(shapeName);
            if (shape == null) {
                // TODO: make this localisable -- not easy since this is running on the server
                warn("The system does not support a shape named " + shapeName
                        + ". NetLogo requires that the shape name be typed exactly as listed on the Shape micro-behaviour page, including upper and lower case and spaces.");
            } else {
                shapes.append(shape);
            }
        }
        Collection<String> shapeDefinitions = urlToShapeDefinitionMap.values();
        for (String shapeDefinition : shapeDefinitions) {
            shapes.append(shapeDefinition);
            shapes.append("\n\n");
        }
        return shapes.toString();
    }

    protected String findShapesUsed(String code) {
        String pieces[] = code.split("set(\\s)+shape(\\s)+\""); // any white space OK
        boolean codeChanged = false;
        for (int i = 1; i < pieces.length; i++) {
            int endQuote = pieces[i].indexOf('"');
            if (endQuote < 0) {
                warn("Troubles finding matching quotes in set shape in " + pieces[i-1] + " and " + pieces[i]);
            } else {
                String shapeName = pieces[i].substring(0, endQuote);
                String shapeNameTrimmed = shapeName.trim();
                if (CommonUtils.isAbsoluteURL(shapeName)) {
                    String url = shapeName;
                    String oldShapeDefinition = urlToShapeDefinitionMap.get(url); 
                    String shapeDefinition;
                    int definitionStart = -1;
                    String errorMessage = null;
                    if (oldShapeDefinition == null) {
                        shapeDefinition = ServerUtils.urlToString(shapeName, clientState, true);
                        if (shapeDefinition != null) {
                            shapeDefinition = CommonUtils.getInnerText(shapeDefinition);
                            definitionStart = shapeDefinition.indexOf(BEGIN_NET_LOGO_SHAPE_TOKEN);
                            if (definitionStart >= 0) {
                                definitionStart += BEGIN_NET_LOGO_SHAPE_TOKEN.length();
                                int definitionEnd = shapeDefinition.indexOf(END_NET_LOGO_SHAPE_TOKEN, definitionStart);
                                if (definitionEnd < 0) {
                                    definitionEnd = shapeDefinition.length();
                                }
                                shapeDefinition = shapeDefinition.substring(definitionStart, definitionEnd).trim();
                            }
                        }
                    } else {
                        shapeDefinition = oldShapeDefinition;
                    }
                    if (shapeDefinition != null && definitionStart >= 0) {
                        String[] parts = shapeDefinition.split("(\\s)+", 2);
                        if (parts.length > 1) {
                            codeChanged = true;
                            shapeName = parts[0].toLowerCase();
                            shapeDefinition = shapeName + "\n" + parts[1];
                            pieces[i] = "\"" + shapeName + pieces[i].substring(endQuote);
                            if (oldShapeDefinition == null) {
                                urlToShapeDefinitionMap.put(url, shapeDefinition);
                            }
                        } else {
                            errorMessage = "The contents of the shape URL: " + url + "\nis not correct. Perhaps the URL is incorrect or else something is wrong with this shape defintion: " + shapeDefinition;
                        }
                    } else {
                        errorMessage = "The contents of the shape URL: " + url + "\nis not correct. Perhaps the URL is incorrect.";
                    } 
                    if (errorMessage != null) {
                        clientState.warn(errorMessage);
                    }
                } else {
                    if (!shapeNameTrimmed.equals(shapeName)) {
                        codeChanged = true;
                        shapeName = shapeNameTrimmed;
                    }
                    pieces[i] = "\"" + shapeNameTrimmed + pieces[i].substring(endQuote);
                    Boolean referencedAlready = shapesReferenced.get(shapeName);
                    if (referencedAlready == null) {
                        shapesReferenced.put(shapeName, true);
                    }
                }
                // need to check if it is in a list -- see Issue 547
                // update endQuote index in case pieces[i] has changed
                // commented out since this can't be supported in general
                // best workaround is to have a BC that does set color "a" set color "b" ...
                // even if it accomplishes nothing else than signalling which shapes are needed
                //		endQuote = pieces[i].indexOf('"');
                //		String rest = pieces[i].substring(endQuote+1);
                //		if (rest.trim().startsWith("\"")) {
                //		    // next bit of code is a quoted string so consider it a name
                //		    startQuote = pieces[i].indexOf('"', endQuote+1);
                //		    endQuote = pieces[i].indexOf('"', startQuote+1);
                //		} else {
                //		    break;
                //		}
            }
        }
        if (!codeChanged) {
            return code; // nothing changed
        }
        StringBuffer newCode = new StringBuffer(pieces[0]);
        for (int i = 1; i < pieces.length; i++) {
            newCode.append("set shape ");
            newCode.append(pieces[i]);
        }
        return newCode.toString();
    }

    private String netLogoCode(String uniqueID, boolean forWebVersion) throws NetLogoException {
        breedVariables.clear();
        for (int i = 0; i < commonBreedVariables.length; i++) {
            breedVariables.add(commonBreedVariables[i]);
        }
        String code = generateNetLogoCode();
        code = findShapesUsed(code);
        String fileContents = 
                ";; This file was generated by the Behaviour Composer at modelling4all.org on " + new Date().toString() + "\n" +
                        ";; The model can be found at " + CommonUtils.joinPaths(CommonUtils.getHostBaseURL(), "?frozen=" + uniqueID) + "\n";
        if (dimensions == 3) {
            // if the following text is updated then also update the string in the BC2NetLogo class
            fileContents += ";; This is a 3D model that needs to be run by the 3D version of NetLogo.\n";
        }
        fileContents += "\n";
        if (useAuxiliaryFile) {
            fileContents += "__includes [\"" + CommonUtils.COMMON_BEHAVIOUR_COMPOSER_NLS + "\"]\n\n" + netLogoEngineDeclarations + code;
        } else {
            String auxiliaryCode = getNetLogoCodeWithFileName(CommonUtils.COMMON_BEHAVIOUR_COMPOSER_NLS);
            int indexOfGlobalsAuxiliary = auxiliaryCode.indexOf("\nglobals [");
            int indexOfGlobalsEndAuxiliary = CommonUtils.indexOfNetLogoCodeOnly(']', auxiliaryCode, indexOfGlobalsAuxiliary); 
            String auxiliaryGlobals = auxiliaryCode.substring(indexOfGlobalsAuxiliary+"\nglobals [".length(), indexOfGlobalsEndAuxiliary);
            auxiliaryCode = auxiliaryCode.substring(0, indexOfGlobalsAuxiliary) + auxiliaryCode.substring(indexOfGlobalsEndAuxiliary+1);
            int indexOfBreedAuxiliary = auxiliaryCode.indexOf("\nbreed [");
            int indexOfBreedEndAuxiliary = CommonUtils.indexOfNetLogoCodeOnly(']', auxiliaryCode, indexOfBreedAuxiliary); 
            String auxiliaryBreed = auxiliaryCode.substring(indexOfBreedAuxiliary, indexOfBreedEndAuxiliary+1);
            auxiliaryCode = auxiliaryCode.substring(0, indexOfBreedAuxiliary) + auxiliaryCode.substring(indexOfBreedEndAuxiliary+1);
            int indexOfGlobalsCode = code.indexOf("\nglobals [");
            if (indexOfGlobalsCode > 0) {
                // following finds next ] ignoring comments and strings 
                int indexOfGlobalsEndCode = CommonUtils.indexOfNetLogoCodeOnly(']', code, indexOfGlobalsCode); 
                //		System.out.println(code.substring(indexOfGlobalsCode-100, indexOfGlobalsCode));
                //		System.out.println("***********************************");
                //		System.out.println(code.substring(indexOfGlobalsCode));
                //		System.out.println("***********************************");
                //		System.out.println(code.substring(indexOfGlobalsCode, indexOfGlobalsEndCode));
                code = code.substring(0, indexOfGlobalsCode) + auxiliaryBreed + "\n" +
                        code.substring(indexOfGlobalsCode, indexOfGlobalsEndCode) +
                        "\n; The following are needed by the Behaviour Composer" + auxiliaryGlobals + code.substring(indexOfGlobalsEndCode);
            } else {
                int indexOfTurtlesOwn = code.indexOf("turtles-own [");
                int indexOfTurtlesOwnEndCode = CommonUtils.indexOfNetLogoCodeOnly(']', code, indexOfTurtlesOwn)+1; 
                code = code.substring(0, indexOfTurtlesOwnEndCode) + "\n" + auxiliaryBreed +
                        "\n\n; The following are needed by the Behaviour Composer\nglobals [" + auxiliaryGlobals + "]\n" + code.substring(indexOfTurtlesOwnEndCode);
            }
            code += "\n\n" + auxiliaryCode;
            // nested display is obsolete since NetLogo update on ticks does it better
            code = code.replace("nested-no-display", "")
                    .replace("nested-display", "")
                    .replace("no-display", "")
                    .replace(" display ", " ");
            fileContents += netLogoEngineDeclarations + code; 
        }
        fileContents += "\n";
        return fileContents;
    }

    protected String generateNetLogoCode() throws NetLogoException {
        return generateNetLogoCodePhase2(prettyPrintCode(generateNetLogoCodePhase1()));
    }

    private String prettyPrintCode(String code) {
        final ArrayList<String> commandsWithTwoBlocks = 
                new ArrayList<String>(Arrays.asList("when", "whenever", "while", "ifelse", "if-else"));
        final ArrayList<String> commandsWithOneBlock = 
                new ArrayList<String>(Arrays.asList(
                        "if", "ask", "create-objects", "do-every", "do-after", "do-at-time", "do-after-setup",
                        "foreach", "repeat", "add-copies", "add-copy-of-another", "add-copies-of-another"));
        final int indentation = 2;
        int commandEnd = 0;
        boolean definingCommand = false;
        boolean alreadyIndented = false;
        int lineLength = 2; // minimum indentation
        String currentCommand = null;
        boolean expectingFirstOfTwoBlocks = false;
        Stack<Integer> secondBlockIndentation = new Stack<Integer>(); // for if-else
        // only use the above if at the correct level of nesting of blocks
        Stack<Integer> secondBlockIndentationDepth = new Stack<Integer>(); 
        int identNextBlock = 0;
        Stack<Integer> openBrackets = new Stack<Integer>();
        StringBuffer result = new StringBuffer();
        NetLogoTokenizer tokenizer = new NetLogoTokenizer(code);
        String token = tokenizer.nextToken(true);
        boolean newLineNeeded = false;
        boolean newLineNeededAfterNextToken = false;
        while (token != null) {
            String lowerCaseToken = token.toLowerCase();
            if (lineLength >= maxLineLength) {
                newLineNeededAfterNextToken = true;
            }
            boolean isComment = token.charAt(0) == ';';
            if (!isComment) {
                // comments handled below
                checkIfTokenRequiresAdditionalCode(lowerCaseToken);
            }
            if (token.equals("end")) {
                result.append(" \nend\n\n");
                newLineNeeded = false;
                currentCommand = null;
                lineLength = 0;
                identNextBlock = 0;
                commandEnd = 0;
                alreadyIndented = false;
                expectingFirstOfTwoBlocks = false;
                definingCommand = false;
                secondBlockIndentation.clear();
                secondBlockIndentationDepth.clear();
                openBrackets.clear();
            } else if (!definingCommand && isCommand(lowerCaseToken, code)) {
                currentCommand = lowerCaseToken;	
                if (alreadyIndented) {
                    alreadyIndented = false;
                    lineLength += token.length()+1;
                } else {
                    int spaces = indent(indentation, openBrackets, result);
                    newLineNeeded = false;
                    lineLength = spaces+token.length()+1;
                }
                if (newLineNeeded) {
                    result.append("\n");
                    newLineNeeded = false;
                }
                result.append(" " + token);
                if (commandsWithTwoBlocks.contains(lowerCaseToken)) {
                    expectingFirstOfTwoBlocks = true;
                    identNextBlock = lineLength;
                } else if (commandsWithOneBlock.contains(lowerCaseToken)) {
                    identNextBlock = lineLength;
                } else if (token.equals("set") || token.equals("let")) {
                    // next token must be a name
                    newLineNeededAfterNextToken = true;
                }
                commandEnd = lineLength;
            } else if (token.equals("[") || token.equals("(") || token.equals("task")) {
                // tasks indent like [
                boolean isTask = token.equals("task");
                if (alreadyIndented) {
                    alreadyIndented = false;
                } else {
                    boolean openBlockOrTask = token.equals("[") || isTask;
                    boolean nextTokenIsCommand = nextTokenIsCommand(tokenizer, code);
                    boolean expectsTwoBlocks = commandsWithTwoBlocks.contains(currentCommand);
                    if (newLineNeeded) {
                        result.append("\n");
                        newLineNeeded = false;
                    }
                    if (openBlockOrTask && 
                            !secondBlockIndentation.isEmpty() && 
                            !expectingFirstOfTwoBlocks &&
                            nextTokenIsCommand &&
                            secondBlockIndentationDepth.peek() == openBrackets.size()) {
                        // starting second block
                        Integer indent = secondBlockIndentation.pop();
                        secondBlockIndentationDepth.pop();
                        result.append(" \n" + nCopies(SPACE_TOKEN, indent));
                        lineLength = indent+token.length()+1;
                        alreadyIndented = true;
                    } else if (openBlockOrTask && identNextBlock > 0 && nextTokenIsCommand) {
                        result.append(" \n" + nCopies(SPACE_TOKEN, identNextBlock));
                        lineLength = identNextBlock+token.length()+1;
                        alreadyIndented = true;
                        if (expectsTwoBlocks) {
                            secondBlockIndentation.push(identNextBlock);
                            secondBlockIndentationDepth.push(openBrackets.size());
                            expectingFirstOfTwoBlocks = false;
                        } else {
                            identNextBlock = 0;
                        }
                    } else {
                        boolean firstBlock = 
                                expectingFirstOfTwoBlocks && 
                                ((token.equals("[") && ("while".equals(currentCommand) || nextTokenIsCommand)) || isTask);
                        // while is [reporter] [command] so special cased here
                        if (firstBlock && currentCommand.startsWith("if")) {
                            if (expectsTwoBlocks) {
                                secondBlockIndentation.push(commandEnd);
                                secondBlockIndentationDepth.push(openBrackets.size());
                            }
                            result.append(" \n" + nCopies(SPACE_TOKEN, commandEnd));
                            lineLength = commandEnd+token.length()+1;
                        } else {
                            lineLength += token.length()+1;
                        }
                        if (firstBlock && expectsTwoBlocks) {
                            expectingFirstOfTwoBlocks = false;
                            if (!isTask) { // task is handled specially below
                                alreadyIndented = true;
                            }
                        }
                    }
                }
                newLineNeeded = false;
                result.append(" " + token);
                int currentLineLength = lineLength;
                if (isTask)  {
                    token = tokenizer.nextToken(false);
                    if (token == null) {
                        continue;
                    }
                    if (token.equals("[")) {
                        result.append(" [");
                        lineLength += 2;
                        currentLineLength += 2;
                        alreadyIndented = true; // following command should not be on a new line
                    } else {
                        System.err.println("Expected an open square bracket after task.");
                    }
                }
                openBrackets.push(currentLineLength);
            } else if (token.equals("]") || token.equals(")")) {
                if (newLineNeeded) {
                    result.append("\n");
                    newLineNeeded = false;
                }
                result.append(" " + token);
                lineLength += 2;
                if (!openBrackets.isEmpty()) {
                    // might be empty only if code isn't bracketed correctly
                    openBrackets.pop();
                }
                alreadyIndented = false;
            } else if (token.equals("to") || token.equals("to-report")) {
                if (newLineNeeded) {
                    result.append("\n");
                    newLineNeeded = false;
                }
                result.append(token);
                lineLength += token.length();
                definingCommand = true;
            } else if (definingCommand) {
                if (newLineNeeded) {
                    result.append("\n");
                    newLineNeeded = false;
                }
                result.append(" " + token);
                lineLength += token.length()+1;
                definingCommand = false;
                alreadyIndented = false;
                commandEnd = lineLength;
            } else if (isComment) {
                result.append("\n " + nCopies(SPACE_TOKEN, commandEnd) + token);
                newLineNeeded = (token.charAt(token.length()-1) != '\n');
            } else {
                lineLength += token.length()+1;
                //		if (lineLength > maxLength) {
                //		    return prettyPrintCode(code, maxLength*2);
                //		    if (commandEnd == 0 || (!openBrackets.isEmpty() && commandEnd < openBrackets.peek())) {
                //			int spaces = indent(16, openBrackets, result);
                //			newLineNeeded = false;
                //			lineLength = spaces+token.length()+1;
                //		    } else {
                //			result.append(" \n" + nCopies(SPACE_TOKEN, commandEnd));
                //			lineLength = commandEnd+token.length()+1;
                //		    }
                //		    newLineNeeded = false;
                //		} else 
                if (newLineNeeded) {
                    result.append("\n");
                    newLineNeeded = false;
                }
                result.append(" " + token);
                alreadyIndented = false;
                if (newLineNeededAfterNextToken) {
                    newLineNeededAfterNextToken = false;
                    result.append(" \n" + nCopies(SPACE_TOKEN, commandEnd));
                    lineLength = commandEnd;
                    //		    newLineNeeded = true;
                }
            }
            token = tokenizer.nextToken(true);
        }
        String finalResult = result.toString().replace(SPACE_TOKEN,  " ");
        return finalResult;
    }
    public void checkIfTokenRequiresAdditionalCode(String lowerCaseToken) {
        if (!mathUtilitiesNeeded && mathUtilities.contains(lowerCaseToken)) {
            mathUtilitiesNeeded = true;
            canonicalDistanceNeeded = true; // used by math-utilities
        } else if (!patchUtilitiesNeeded && patchUtilities.contains(lowerCaseToken)) {
            patchUtilitiesNeeded = true;
        } else if (!patchUtilitiesDeprecatedNeeded && patchUtilitiesDeprecated.contains(lowerCaseToken)) {
            patchUtilitiesDeprecatedNeeded = true;
        } else if (!distanceUtilitiesNeeded && distanceUtilities.contains(lowerCaseToken)) {
            distanceUtilitiesNeeded = true;
        } else if (!moveHorizontallyOrVerticallyCommandsNeeded && moveHorizontallyOrVerticallyCommands.contains(lowerCaseToken)) {
            moveHorizontallyOrVerticallyCommandsNeeded = true;
        } else if (!locationUtilitiesNeeded && locationUtilities.contains(lowerCaseToken)) {
            locationUtilitiesNeeded = true;
        } else if (!canonicalDistanceNeeded && lowerCaseToken.equals("canonical-distance")) {
            canonicalDistanceNeeded = true;
        }
    }

    private boolean nextTokenIsCommand(NetLogoTokenizer tokenizer, String code) {
        String peekToken = tokenizer.peekToken();
        if (peekToken == null) {
            return false;
        }
        if (peekToken.equals("[")) {
            peekToken = tokenizer.peekToken(2);
        }
        return isCommand(peekToken.toLowerCase(), code);
    }

    public boolean isCommand(String token, String code) {
        if (commands.contains(token)) {
            return true;
        } else {
            return getGeneratedCommandNames(code).contains(token);
        }
    }

    public ArrayList<String> getGeneratedCommandNames(String code) {
        if (generatedCommandNames == null) {
            generatedCommandNames = new ArrayList<String>();
            int index = code.indexOf("\nto ");
            while (index >= 0) {
                int nameStart = index+3;
                int nameEnd = Math.min(code.indexOf(" ", nameStart+1), code.indexOf("\n", nameStart+1));
                if (nameEnd < 0) {
                    nameEnd = code.length()-1;
                }
                String name = code.substring(nameStart, nameEnd).trim();
                generatedCommandNames.add(name.toLowerCase());
                index = code.indexOf("\nto ", nameEnd);
            }
        }
        return generatedCommandNames;
    }

    public int indent(int defaultIndentation, Stack<Integer> openBrackets, StringBuffer result) {
        int spaces = openBrackets.isEmpty() ? defaultIndentation : openBrackets.peek();
        result.append(" "); // spaces on both sides of all tokens
        result.append("\n" + nCopies(SPACE_TOKEN, spaces));
        return spaces;
    }

    private String nCopies(String string, int i) {
        if (i < 1) {
            return "";
        } else if (i == 1) {
            return string;
        } else {
            return string + nCopies(string, i-1);
        }
    }
    protected String generateNetLogoCodePhase1() throws NetLogoException {
        totalRequired = new ArrayList<MicroBehaviour>(); // to recompute it below
        ArrayList<MicroBehaviour> allMicroBehaviousContainingRawNetLogoCode = 
                new ArrayList<MicroBehaviour>();
        widgetsToCreate.clear(); // to recompute it below
        //	int objectNumber = 0;
        // initialise kind info first in case needed
        // in the procedural initialisation 
        //	String objectInitialiseKind = "";
        String objectInitialisation = "";
        String objectSetup = "";
        String answer = "";
        for (MacroBehaviour macroBehaviour : macroBehaviours) {
            String quotedBehaviourNames = "";
            StringBuilder behaviourDescriptions = new StringBuilder();
            quotedBehaviourNames = 
                    generateNetLogoCodeForMacroBehaviour(macroBehaviour, 
                            allMicroBehaviousContainingRawNetLogoCode,
                            quotedBehaviourNames, 
                            behaviourDescriptions);
            if (macroBehaviour.isActive() && macroBehaviour.isAddToModel()) {
                objectInitialisation += macroBehaviour.getInitialisationCode() + "\n";
                if (!quotedBehaviourNames.isEmpty()) {
                    objectSetup += macroBehaviour.getSetupCode() + "\n";
                }
            }
        }
        answer += "to the-model  [globals-not-to-be-initialised]" + "\n" + 
                " initialise-globals globals-not-to-be-initialised\n" +
                objectInitialisation + objectSetup + 
                "\nend\n";
        answer += "to kind-initialisation [kind-name]\n";
        boolean first = true;
        for (MacroBehaviour macroBehaviour : macroBehaviours) {
            String callBehaviours = macroBehaviour.getBehaviourNames();
            if (!first) {
                answer += "[";
            }
            answer += " if-else (kind-name = \"" + macroBehaviour.getObjectName() + "\")[ " + (callBehaviours == null ? "" : callBehaviours) + "]\n";
            first = false;
        }
        answer += "[output-print (word \"create-agent called with an unknown kind: \" kind-name)]\n";
        for (int i = 1; i < macroBehaviours.size(); i++) {
            answer += "]";
        }
        answer += "end\n";
        for (MicroBehaviour microBehaviour : allMicroBehaviousContainingRawNetLogoCode) {
            answer += microBehaviour.getBehaviourCode() + "\n";
        }
        answer += "\n";
        answer += processRequiredMicroBehaviours();
        for (String newCommand : getGeneratedCommands()) {
            answer += newCommand + "\n\n";
        }
        for (String unneededCommand : unneededCommands) {
            // if it is replaced with "" then the end can be on the same line as the 'to'
            // this assumes the generated code puts spaces on both sides of a list of command
            answer = answer.replaceAll("(\\s)+" + unneededCommand + "(\\s)+", "\n");
            answer = answer.replace("[" + unneededCommand, "[");
            answer = answer.replace(unneededCommand + "]", "]");
            //	    if (answer.contains(unneededCommand)) {
            //		// is needed -- e.g. used by remove-behaviours-from
            //		answer += "to " + unneededCommand + "\nend\n\n";		
            //	    }
        }
        return answer;
    }

    protected String generateNetLogoCodeForMacroBehaviour(MacroBehaviour macroBehaviour,
            ArrayList<MicroBehaviour> allMicroBehavioursContainingRawNetLogoCode,
            String quotedBehaviourNames, 
            StringBuilder behaviourDescriptions) {
        if (macroBehaviour.isGeneratingNetLogoCode()) {
            return "";
        }
        if (!macroBehaviour.isActive()) { //  || !macroBehaviour.isAddToModel()) {
            // either inactive or a Behaviour List so don't generate the code here
            // need to generate them so others can refer to them
            // Arthur's predator prey guide needs some work about this
            return "";
        }
        macroBehaviour.setGeneratingNetLogoCode(true);
        List<MicroBehaviour> microBehaviours = macroBehaviour.getMicroBehaviours();
        for (MicroBehaviour microBehaviour : microBehaviours) {
            if (macroBehaviour.isActive(microBehaviour)) {
                if (microBehaviour.isRawNetLogoCode()) {
                    if (!microBehaviourIsMember(microBehaviour, allMicroBehavioursContainingRawNetLogoCode) &&
                            !microBehaviourIsMember(microBehaviour, totalRequired)) {
                        allMicroBehavioursContainingRawNetLogoCode.add(microBehaviour);
                        try {
                            String code = microBehaviour.getBehaviourCode();
                            if (code != null) {
                                findAllKindsOfVariables(code);
                            } else {
                                clientState.warn("No code found for micro behaviour with URL: " + microBehaviour.getBehaviourURL());
                            }
                        } catch (NetLogoException e) {
                            clientState.logException(
                                    e, "while getting the NetLogo code of a micro-behaviour");
                        }	
                    }
                } else {
                    ArrayList<MacroBehaviour> macroBehavioursInMicroBehaviour = 
                            microBehaviour.getMacroBehaviours();
                    if (macroBehavioursInMicroBehaviour != null) {
                        for (MacroBehaviour macroBehaviourInMicroBehaviour : macroBehavioursInMicroBehaviour) {
                            generateNetLogoCodeForMacroBehaviour(
                                    macroBehaviourInMicroBehaviour,
                                    allMicroBehavioursContainingRawNetLogoCode,
                                    quotedBehaviourNames, 
                                    behaviourDescriptions);
                        }
                    }
                    quotedBehaviourNames += microBehaviour.getBehaviourName(true) + "\n ";
                    behaviourDescriptions.append(microBehaviour.getBehaviourDescription());
                    behaviourDescriptions.append(' ');
                    addRequiredMicroBehaviour(microBehaviour);
                }
            }
        }
        macroBehaviour.setGeneratingNetLogoCode(false);
        macroBehaviour.setQuotedBehaviourNames(quotedBehaviourNames);
        return quotedBehaviourNames;
    }

    public static boolean microBehaviourIsMember(MicroBehaviour microBehaviour, 
            ArrayList<MicroBehaviour> microBehaviours) {
        String name = microBehaviour.getNetLogoName();
        if (name == null) {
            return false;
        }
        for (MicroBehaviour otherMicroBehaviour : microBehaviours) {
            String otherName = otherMicroBehaviour.getNetLogoName();
            if (name.equals(otherName)) {
                return true;
            }
        }
        return false;
    }

    protected String generateNetLogoCodePhase2(String code) {
        if (!expectedGlobalVariables.isEmpty()) {
            String message = null; 
            for (String expectedGlobalVariable : expectedGlobalVariables) {
                if (!encounteredGlobalVariables.contains(expectedGlobalVariable)) {
                    if (message == null) {
                        message = "Warning. The following parameters were not set or defined and were given the value of 0:";
                    }
                    message += " " + expectedGlobalVariable + ",";
                    globalVariablesToDeclare.add(expectedGlobalVariable);
                }
            }
            if (message != null) {
                message += " <b>Use DEFINE-PARAMETER or other micro-behaviours to remedy this. </b>";
            }
            expectedGlobalVariables.clear();
            encounteredGlobalVariables.clear();
            if (message != null) {
                clientState.warn(message);
            }
        }
        if (!commentsAboutglobalsWithInterface.isEmpty()) {
            code = "\n;; Other global variables:\n\n" + commentsAboutglobalsWithInterface + "\n" + code;
        }
        if (!globalVariablesToDeclare.isEmpty()) {
            String globalsDeclaration = "globals [";
            for (String variable : globalVariablesToDeclare) {
                //		System.out.println("declared " + variable);
                globalsDeclaration += " \n " + variable;
                String comment = globalVariableComments.get(variable);
                if (comment != null) {
                    globalsDeclaration += " ;" + comment.trim();
                }
            }
            globalsDeclaration += "\n]\n\n";
            code = globalsDeclaration + code;  
        }
        String patchDeclarations = "";
        boolean logPatchAttributesUsed = code.contains("log-patch-attributes");
        if (patchVariables.length > 0 || logPatchAttributesUsed) {
            patchDeclarations += "patches-own [\n ";
            for (int i = 0; i < patchVariables.length; i++) {
                if (patchVariables[i] != null) {
                    patchDeclarations += " " + patchVariables[i] + " ";
                    if (i % 2 == 1) { // is a my-next-... variable
                        patchDeclarations += " " + patchVariables[i] + "-set ";
                    }
                    patchDeclarations += "\n ";
                }
            }
            patchDeclarations += " log-patch-attributes]\n\n";
        }
        String linkDeclarations = "";
        if (linkVariables.length > 0) {
            linkDeclarations += "links-own [";
            for (int i = 0; i < linkVariables.length; i++) {
                if (linkVariables[i] != null) {
                    linkDeclarations += linkVariables[i] + " ";
                    if (i % 2 == 1) { // is a my-next-... variable
                        linkDeclarations +=  " " + linkVariables[i] + "-set ";
                    }
                }
            }
            linkDeclarations += "]\n\n";
        }
        if (dimensions == 3) {
            addBreedVariable("my-z", false);
            addBreedVariable("my-next-z", false);
            addBreedVariable("previous-zcor", false);
            //	    addBreedVariable("my-pitch", true);
            //	    addBreedVariable("my-next-pitch", false);
            //	    addBreedVariable("previous-pitch", false);
        }
        if (moveHorizontallyOrVerticallyCommandsNeeded) {
            code += getNetLogoCode("move-horizontally-or-vertically-towards-another");
        }
        if (distanceUtilitiesNeeded) {
            code += getNetLogoCode("distance-utilities");
        }
        if (mathUtilitiesNeeded) {
            code += getNetLogoCode("math-utilities");
        }
        if (locationUtilitiesNeeded) {
            if (dimensions == 3) {
                code += getNetLogoCode("location-3d");
            } else {
                code += getNetLogoCode("location");
            } 
        }
        if (canonicalDistanceNeeded) {
            if (dimensions == 3) {
                code += getNetLogoCode("canonical-distance-3d");
            } else {
                code += getNetLogoCode("canonical-distance");
            } 
        }
        if (patchUtilitiesNeeded) {
            if (dimensions == 3) {
                code += getNetLogoCode("patch-utilities-3d");
            } else {
                code += getNetLogoCode("patch-utilities");
            } 
        }
        if (patchUtilitiesDeprecatedNeeded) {
            if (dimensions == 3) {
                code += getNetLogoCode("patch-utilities-deprecated-3d");
            } else {
                code += getNetLogoCode("patch-utilities-deprecated");
            } 
        }
        // logically the following should be objects-own
        // but benchmark tests showed that turtles-own cut running time by 30 to 50%.
        String breedDeclaration = "turtles-own [\n" + extraBreedVariables + "\n";
        if (breedVariables != null) {
            for (int i = 0; i < breedVariables.size(); i++) {
                if (breedVariables.get(i) != null) {
                    breedDeclaration += " " + breedVariables.get(i) + " ";
                    if (i % 2 == 1) { // is a my-next-... variable
                        breedDeclaration += " " + breedVariables.get(i) + "-set ";
                    }
                    breedDeclaration += " \n ";
                }
            }
            code += "\nto-report update-attributes\n";
            for (int i = 0; i < breedVariables.size(); i += 2) {
                String breedVariable = breedVariables.get(i + 1);
                if (breedVariable != null) {
                    if (isBreedVariableUsed(breedVariable, code)) {
                        // the next version has been used
                        // or it is one of my-next-x, my-next-y, or my-next-heading
                        code += "  ifElse " + breedVariable
                                + "-set\n" + "    [set " + breedVariables.get(i) + " "
                                + breedVariable + "\n" + "     set "
                                + breedVariable + "-set false]\n"
                                + "    [set " + breedVariable + " "
                                + breedVariables.get(i) + "]\n";
                        // e.g. ifElse my-next-x-set [set my-x my-next-x set my-next-x-set false] [set my-next-x my-x]
                    } else {
                        // no need to declare this variable
                        String variableSet = breedVariable + "-set";
                        breedDeclaration = breedDeclaration.replace(variableSet, "");
                        breedDeclaration = breedDeclaration.replace(breedVariable, "");
                        int index = breedVariables.indexOf(breedVariable);
                        if (index >= 0) {
                            breedVariables.set(index, null);
                        }
                    }
                }
            }
            if (breedDeclaration.contains("my-next-visibility")) {
                code += "  set hidden? not my-visibility\n";
            }
            if (code.contains("log-attributes")) {
                code  += "  if log-attributes != []\n" +
                        "     [if log-attributes = true\n" +
                        "       [set log-attributes \n           [";
                for (int i = 0; i < breedVariables.size(); i += 2) {
                    code += "\"" + breedVariables.get(i) + "\" ";
                }
                // following doesn't include those that have my-... equivalents like my-heading and my-x
                code +=   "\n            \"color\" \"label\" \"label-color\" \"pen-mode\" \"pen-size\" \"shape\" \"size\"";
                code += "]]\n";
                code += "      log-changed-attributes]\n";
            } else {
                breedDeclaration = breedDeclaration.replace("log-attributes", "");
            }
            code += "  report false\nend\n\n";
            if (breedDeclaration.contains("log-attributes")) {
                code += getNetLogoCode("log-attributes-enabled");
            }
            //	    answer += "to update-attributes-and-log\n";
            //	    for (int j = 0; j < breedVariables.size(); j += 2) {
            //		if (breedVariables.get(j + 1) != null) {
            //		    answer += "ifElse " + breedVariables.get(j + 1)
            //			      + "-set\n"
            //			      + "  [if log-attributes = true or member? \""
            //			      + breedVariables.get(j) + "\" log-attributes\n"
            //			      + "      [output-print (word \""
            //			      + breedVariables.get(j) + " of \" kind \" \" who \" set to \" "
            //			      + breedVariables.get(j + 1) + " time-description)]\n"
            //			      + "       set " + breedVariables.get(j) + " "
            //			      + breedVariables.get(j + 1) + "\n" + "       set "
            //			      + breedVariables.get(j + 1) + "-set false]\n"
            //			      + "  [set " + breedVariables.get(j + 1) + " "
            //			      + breedVariables.get(j) + "]\n";
            //		} else {
            //		    answer += "if log-attributes = true or member? \""
            //			      + breedVariables.get(j) + "\" log-attributes\n"
            //			      + "   [output-print (word \"" + 
            //			      breedVariables.get(j)
            //			      + " of \" kind \" \" who \" set to \" " + breedVariables.get(j)
            //			      + " time-description)]\n";
            //		}
            //	    }
            //	    answer += "end\n\n";
            code += "to initialise-patch-attributes\n";
            String patchSettings = "";
            for (int k = 0; k < patchVariables.length; k += 2) {
                if (patchVariables[k + 1] != null) {
                    patchSettings = patchSettings + " set "
                            + patchVariables[k + 1] + "-set false\n ";
                }
            }
            if (patchSettings != "") {
                code += " ask-every-patch task [ " + patchSettings + " ]\n";
            }
            if (patchVariables.length > 0) {
                code += " set log-patch-attributes []\n";
            }
            code += "end\n\n";
            code += "to initialise-globals [globals-not-to-be-initialised]\n" + globalInitialisations.toString();
            code += " set update-patch-attributes-needed " + logPatchAttributesUsed + "\n";
            code += "end\n\n";
            code += "to update-patch-attributes\n";
            if (logPatchAttributesUsed) {
                code += " if log-patch-attributes != []\n";
                code += "    [if log-patch-attributes = true\n";
                code += "       [set log-patch-attributes\n";
                code += "            [";
                for (int i = 0; i < patchVariables.length; i += 2) {
                    code += "\"" + patchVariables[i] + "\" ";
                }
                code +=   "\n             \"pcolor\" \"plabel\" \"plabel-color\"";
                code += "]]\n";
                //	    if (patchVariables.length > 0) {
                //		answer += "ifElse log-patch-attributes = [] [";
                //		for (int i = 0; i < patchVariables.length; i += 2) {
                //		    if (patchVariables[i + 1] != null) {
                //			answer += "ifElse " 
                //			       + patchVariables[i + 1]
                //			       + "-set\n" + "  [set " + patchVariables[i] + " "
                //			       + patchVariables[i + 1] + "\n" + "   set "
                //			       + patchVariables[i + 1] + "-set false]\n"
                //			       + "  [set " + patchVariables[i + 1] + " "
                //			       + patchVariables[i] + "]\n";
                //		    }
                //		}
                //		answer += "]\n" + "[";
                //		for (int j = 0; j < patchVariables.length; j += 2) {
                //		    if (patchVariables[j + 1] != null) {
                //			answer += "ifElse " 
                //			       + patchVariables[j + 1]
                //			       + "-set\n"
                //			       + "  [if member? \""
                //			       + patchVariables[j] + "\" log-patch-attributes\n"
                //			       + "       [set " + patchVariables[j] + " "
                //			       + patchVariables[j + 1] + "\n" + "       set "
                //			       + patchVariables[j + 1] + "-set false]\n"
                //			       + "  [set " + patchVariables[j + 1] + " "
                //			       + patchVariables[j] + "]\n";
                //		    }
                //		}
                //	    }
                code += "     log-changed-patch-attributes]\n";
            }
            code += "end\n\n";
            if (logPatchAttributesUsed) {
                code += getNetLogoCode("log-changed-patch-attributes");
            }
            if (!breedDeclaration.contains(" my-next-x ")) { 
                code = code.replace(" my-x ", " xcor ");
                // check that any remain -- code may have been my-y my-y and only one gets changed since they share a space
                if (code.contains(" my-x ")) {
                    code = code.replace(" my-x ", " xcor ");
                }
                breedDeclaration = breedDeclaration.replace(" my-x ", " ");
                breedDeclaration = breedDeclaration.replace(" previous-xcor ", " ");
                code = code.replace("set xcor xcor", "");
            }
            if (!breedDeclaration.contains(" my-next-y ")) { 
                code = code.replace(" my-y ", " ycor ");
                if (code.contains(" my-y ")) {
                    code = code.replace(" my-y ", " ycor ");
                }
                breedDeclaration = breedDeclaration.replace(" my-y ", " ");
                breedDeclaration = breedDeclaration.replace(" previous-ycor ", " ");
                code = code.replace("set ycor ycor", "");
            } 
            if (!breedDeclaration.contains(" my-next-z ")) { 
                code = code.replace(" my-z ", " zcor ");
                if (code.contains(" my-z ")) {
                    code = code.replace(" my-z ", " zcor ");
                }
                breedDeclaration = breedDeclaration.replace(" my-z ", " ");
                breedDeclaration = breedDeclaration.replace(" previous-zcor ", " ");
                code = code.replace("set zcor zcor", "");
            }  
            if (!breedDeclaration.contains(" my-next-heading ")) {
                code = code.replace(" my-heading ", " heading ");
                if (code.contains(" my-heading ")) {
                    code = code.replace(" my-heading ", " heading ");   
                }
                breedDeclaration = breedDeclaration.replace(" my-heading ", " ");
                breedDeclaration = breedDeclaration.replace(" previous-heading ", " ");
            }
            //	    if (dimensions == 3 && !breedDeclaration.contains(" my-next-pitch ")) {
            //		code = code.replace(" my-pitch ", " pitch ");
            //		if (code.contains(" my-pitch ")) {
            //		    code = code.replace(" my-pitch ", " pitch ");   
            //		}
            //		breedDeclaration = breedDeclaration.replace(" my-pitch ", " ");
            //		breedDeclaration = breedDeclaration.replace(" previous-pitch ", " ");
            //	    }
            if (!breedDeclaration.contains(" my-next-visibility ")) { 
                if (code.contains(" my-visibility ")) {
                    code = code.replaceAll("set(\\s)+my-visibility(\\s)+", "set hidden? not ");
                    code = code.replace(" my-visibility ", " not hidden? ");
                    code = code.replace(" not true ", " false ");
                    code = code.replace(" not false ", " true ");
                }
                breedDeclaration = breedDeclaration.replace(" my-visibility ", " ");
            }
            String updateCodeSource = null;
            if (breedDeclaration.contains(" my-next-x ") || 
                    breedDeclaration.contains(" my-next-y ") ||
                    breedDeclaration.contains(" my-next-z ")) {
                if (breedDeclaration.contains(" my-next-heading ") || 
                        breedDeclaration.contains(" my-next-pitch ")) {
                    updateCodeSource = "update-turtle-state-x-y-heading";
                } else {
                    updateCodeSource = "update-turtle-state-x-y";
                }
            } else if (breedDeclaration.contains(" my-next-heading ")) {
                updateCodeSource = "update-turtle-state-heading";
            } else {
                updateCodeSource = "update-turtle-state-dummy";
            }
            if (updateCodeSource != null) {
                if (dimensions == 3) {
                    updateCodeSource += "-3d";
                }
                code += getNetLogoCode(updateCodeSource);
            }
            code += "\nto initialise-attributes\n";
            for (int k = 0; k < breedVariables.size(); k += 2) {
                if (breedVariables.get(k + 1) != null) {
                    code += " set " + breedVariables.get(k + 1)
                    + "-set false\n";
                }	 
            }
            //	    if (answer.contains("log-behaviours")) {
            //		answer += " set log-behaviours []\n";
            //	    } else {
            //		breedDeclaration = breedDeclaration.replace("log-behaviours", "");
            //	    }
            if (code.contains(" log-attributes ")) {
                code += " set log-attributes []\n";
            } else {
                breedDeclaration = breedDeclaration.replace(" log-attributes ", " ");
            }
            if (code.contains(" my-visibility ")) {
                code += " set my-visibility true\n";
            }
            code += "end\n\n";
        }
        //	if (breedDeclaration.contains("log-behaviours")) {
        //	    answer += getNetLogoCode("log-behaviours-enabled");
        //	} else {
        //	    answer += getNetLogoCode("log-behaviours-disabled");
        //	}
        // remove blank lines from declaration
        breedDeclaration = breedDeclaration.replaceAll("(?m)^[ \t]*\r?\n", "");
        // and answer
        // TODO: determine if still needed now that pretty printer runs
        //	code = code.replaceAll("(?m)^[ \t]*\r?\n", "");
        // put back blank lines between commands/reporters
        //	code = code.replaceAll("(?m)^to", "\nto");
        // remove extra spaces
        breedDeclaration = breedDeclaration.replaceAll("[ \t]{2,}", " ");
        breedDeclaration += "]\n\n";
        return breedDeclaration + patchDeclarations + linkDeclarations + code;
    }

    private boolean isBreedVariableUsed(String breedVariable, String afterDeclarations) {
        if (afterDeclarations.contains(breedVariable)) {
            return true;
        }
        if (breedVariable.equals("my-next-heading")) {
            return afterDeclarations.contains("turn-right") ||
                    afterDeclarations.contains("turn-left") ||
                    afterDeclarations.contains("go-forward"); // read only
        }
        if (breedVariable.equals("my-next-x") || breedVariable.equals("my-next-y") || breedVariable.equals("my-next-z")) {
            return afterDeclarations.contains("go-forward") ||
                    afterDeclarations.contains("move-horizontally-or-vertically-towards-another"); 
        }
        return true;
    }

    private String processRequiredMicroBehaviours() throws NetLogoException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < totalRequired.size(); i++) {
            result.append(totalRequired.get(i).generateNetLogo(this, true));
        }
        return result.toString();
        // the above works even if new elements are added at the end while the following
        // throws ConcurrentModificationException 
        //	Iterator<ReferencedMicroBehaviour> referencedMicroBehaviours = totalRequired.iterator();
        //	while (referencedMicroBehaviours.hasNext()) {
        //	    ReferencedMicroBehaviour referencedMicroBehaviour = referencedMicroBehaviours.next();
        //	    referencedMicroBehaviour.generateNetLogo(true, this, runningLocalHost);
        //	    // what about duplicates and will this work OK when the above adds new items to totalRequired?
        //	}
    }

    //	int labelLocation = 0;
    //	if (includesCorners) {
    //	    labelLocation = 2; // since skipping over the corners
    //	}
    //	if (argumentCount == 5 + labelLocation || 
    //        argumentCount == 6 + labelLocation || 
    //        argumentCount >= 9 + labelLocation) {
    //	    String newCommandName1 = behaviourDescription.replace(' ', '-') + "-" + commandNumber++;
    //	    generateReporter(lines.get(labelLocation + 3), newCommandName1, netLogoModel);
    //	    String newCommandName2 = behaviourDescription.replace(' ', '-') + "-" + commandNumber++;
    //	    generateReporter(lines.get(labelLocation + 4), newCommandName2, netLogoModel);
    //	    answer = 
    //		operation + " " + 
    //		lines.get(labelLocation) + "\n" + 
    //		lines.get(labelLocation + 1) + "\n" + 
    //		lines.get(labelLocation + 2) + "\n" + 
    //		"\"" + newCommandName1 + "\"\n" + 
    //		"\"" + newCommandName2 + "\"\n";
    //	} else {
    //	    answer = ""; // not updated by this code
    //	}

    public ArrayList<String> createPlot(String arguments, boolean histogram) {
        // a bit of a hack but this is to work around NetLogo limitations
        // (that this can't be run within NetLogo)
        if (arguments == null) {
            clientState.warn("No arguments passed to create-plot.\n" + arguments);
            return null;
        }
        ArrayList<String> plotData = new ArrayList<String>();
        NetLogoTokenizer tokenizer = new NetLogoTokenizer(arguments);
        Integer llx = null;
        Integer lly = null;
        Integer urx = null;
        Integer ury = null;
        String token = tokenizer.nextToken(false);
        Integer number = CommonUtils.integerIfQuoted(token);
        boolean includesCorners = number != null;
        if (includesCorners) {
            llx = number;
            token = tokenizer.nextToken(false);
            number = CommonUtils.integerIfQuoted(token);
            if (number != null) {
                lly = number;
            } else {
                clientState.warn("Expected all first four arguments to create-plot or create-histogram to be integers.\n" + arguments);
                return null;
            }
            token = tokenizer.nextToken(false);
            number = CommonUtils.integerIfQuoted(token);
            if (number != null) {
                urx = number;
            } else {
                clientState.warn("Expected all first four arguments to create-plot or create-histogram to be integer.\n" + arguments);
                return null;
            }
            token = tokenizer.nextToken(false);
            number = CommonUtils.integerIfQuoted(token);
            if (number != null) {
                ury = number;
            } else {
                clientState.warn("Expected all first four arguments to create-plot or create-histogram to be integer.\n" + arguments);
                return null;
            }
        }
        token = tokenizer.nextToken(false);
        String plotName = CommonUtils.stringIfQuoted(token);
        if (getWidgetWithName("PLOT", plotName) != null) {
            // already created one with this name 
            return null;
        }
        plotData.add(token);
        String commandToAddAPlot = null;
        if (includesCorners) {
            // NetLogo 4.1 minimum dimensions for a plot is 160x120
            commandToAddAPlot = createCommandToAdd("PLOT", plotName, llx, lly, urx, ury, 160, 120);
        } else {
            graphXCoordinate += 204; // 4 pixels between each graph
            final int margin = 300; 
            if (graphXCoordinate > preferredWidth - margin) { // near right edge
                graphXCoordinate = preferredWidth - margin + (int) (Math.random() * margin);
            }
            commandToAddAPlot = 
                    "PLOT\n" + 
                            graphXCoordinate + "\n" + 557 + "\n" +
                            (graphXCoordinate + 200) + "\n" + 707 + "\n";
            updateWorldBoundingBox(graphXCoordinate, graphXCoordinate + 200, 557, 707);
        }
        token = tokenizer.nextToken(false);
        String xLabel = CommonUtils.stringIfQuoted(token);
        plotData.add(token);
        token = tokenizer.nextToken(false);
        String yLabel = CommonUtils.stringIfQuoted(token);
        plotData.add(token);
        commandToAddAPlot += plotName + "\n" +
                xLabel + "\n" +
                yLabel + "\n";
        // latest version adds the parentheses for these code fragments
        token = tokenizer.nextToken(false);
        number = CommonUtils.integerIfQuoted(token);
        boolean includesCode = number == null && 
                token != null && 
                !token.equalsIgnoreCase("true") && 
                !token.equalsIgnoreCase("false") &&
                // if it is quoted then it is pen and color names -- not code 
                CommonUtils.stringIfQuoted(token) == null;
        if (includesCode) {
            if (token.equals("(")) {
                token = collectCodeInParens(tokenizer);
            }
            plotData.add(token);
            token = tokenizer.nextToken(false);
            if (token.equals("(")) {
                token = collectCodeInParens(tokenizer);
            }
            plotData.add(token);
            token = tokenizer.nextToken(false);
            number = CommonUtils.integerIfQuoted(token);
        }
        boolean rangeSpecified = number != null;
        if (rangeSpecified) {
            for (int i = 0; i < 4; i++) {
                Double range = CommonUtils.doubleIfQuoted(token);
                if (number != null) {
                    commandToAddAPlot += range + "\n";
                } else {
                    clientState.warn("Expected all four arguments specifying the range of values to create-plot or create-histogram to be numbers.\n" + arguments);
                    return null;
                }
                if (i < 3) {
                    token = tokenizer.nextToken(false);
                }
            }
            token = tokenizer.nextToken(false); // for the legends (if any)
            // no auto-plot if dimensions specified explicitly
            commandToAddAPlot += "false\n";
        } else {
            commandToAddAPlot += 0.0 + "\n" + 1.0 + "\n" + 0.0 + "\n" + 1.0 + "\n" + "true" + "\n";
        }
        //	if (token == null) {
        //	    commandToAddAPlot += "false\n";
        //	} else {
        //	    if (!rangeSpecified && !includesCode && !histogram) {
        //		token = tokenizer.nextToken(false);
        //	    } // otherwise token is already correctly bound
        if (token == null || token.equalsIgnoreCase("false")) {
            // no legend
            commandToAddAPlot += "false\n\"\" \"\"\nPENS\n\"default\" 1.0 0 -16777216 true \"\" \"\"\n";
        } else if (token.equalsIgnoreCase("true")) {
            // legend when pens added
            // need to add a pen but it doesn't need to be in the legend
            commandToAddAPlot += "true\n\"\" \"\"\nPENS\n";
        } else {
            // create a legend
            commandToAddAPlot += "true\n\"\" \"\"\nPENS\n";
            // all that remains is alternating label and color names
            while (token != null) {
                String label = token;
                String labelIfQuoted = CommonUtils.stringIfQuoted(label);
                if (labelIfQuoted != null) {
                    label = labelIfQuoted;
                }
                String colorName = tokenizer.nextToken(false);
                if (colorName == null) {
                    clientState.warn("create-plot and create-histogram need to have alternating pen names and colors.\n" + arguments);
                    return null;
                }
                String colorNameIfQuoted = CommonUtils.stringIfQuoted(colorName);
                if (colorNameIfQuoted != null) {
                    colorName = colorNameIfQuoted;
                }
                String colorNumber = colorNumber(colorName);
                if (colorNumber != null) {
                    commandToAddAPlot += "\"" + label + "\"" + " 1.0 0 " + colorNumber + " true \"\" \"\"\n";
                } else {
                    clientState.warn(colorName + " is not a recognised pen color (in create-plot or create-histogram)");
                }
                token = tokenizer.nextToken(false);
            }
        }
        //	}
        commandToAddAPlot += "\n";
        widgetsToCreate.add(commandToAddAPlot);
        if (includesCode) {
            return plotData;
        } else {
            // just instructed NetLogo to make a graph -- but not what to draw on it (yet)
            return null;
        }
    }

    protected String collectCodeInParens(NetLogoTokenizer tokenizer) {
        String token;
        String contents = "";
        int openParenCount = 1;
        while ((token = tokenizer.nextToken(false)) != null) {
            if (token.equals(")")) {
                openParenCount--;
                if (openParenCount == 0) {
                    break;
                }
            } else if (token.equals("(")) {
                openParenCount++;
            }
            checkIfTokenRequiresAdditionalCode(token.toLowerCase());
            contents += token + " ";
        }
        return contents;
    }

    protected void createPlot(ArrayList<String> lines, String code, boolean includesCorners, boolean histogram) {
        // a bit of a hack but this is to work around NetLogo limitations
        // (that this can't be run within NetLogo)
        if (code == null) {
            clientState.warn("No code passed to create-plot.\n" + code);
            return;
        }
        NetLogoTokenizer tokenizer = new NetLogoTokenizer(code);
        String token;
        while ((token = tokenizer.nextToken(false)) != null) {
            System.out.println(token);
        }
        if (lines.size() < 3) {
            warn("CreatePlot has too few arguments in " + code + "\nPlease revise it.");
            return;
        }
        int labelLocation = 0; 
        if (includesCorners) {
            labelLocation = 2;
        }
        String plotName = firstQuoted(lines.get(labelLocation).trim());
        if (getWidgetWithName("PLOT", plotName) != null) {
            // already created one with this name 
            return;
        }
        String commandToAddAPlot = null;
        if (labelLocation == 0) {
            graphXCoordinate += 204; // 4 pixels between each graph
            final int margin = 300; 
            if (graphXCoordinate > preferredWidth - margin) { // near right edge
                graphXCoordinate = preferredWidth - margin
                        + (int) (Math.random() * margin);
            }
            commandToAddAPlot = 
                    "PLOT\n" + 
                            graphXCoordinate + "\n" + 557 + "\n" +
                            (graphXCoordinate + 200) + "\n" + 707 + "\n";
            updateWorldBoundingBox(graphXCoordinate, graphXCoordinate + 200, 557, 707);
        } else {
            String quoted[] = splitCodeByQuotes(code, 9);
            int llx = ServerUtils.parseInt(quoted[1], "createPlot", clientState);
            int lly = ServerUtils.parseInt(quoted[3], "createPlot", clientState);
            int urx = ServerUtils.parseInt(quoted[5], "createPlot", clientState);
            int ury = ServerUtils.parseInt(quoted[7], "createPlot", clientState);
            // NetLogo 4.1 minimum dimensions for a plot is 160x120
            commandToAddAPlot = createCommandToAdd("PLOT", plotName, llx, lly, urx, ury, 160, 120);
        }
        String xLabel = firstQuoted(lines.get(1 + labelLocation).trim());
        String yLabel = firstQuoted(lines.get(2 + labelLocation).trim());
        commandToAddAPlot += plotName + "\n" +
                xLabel + "\n" +
                yLabel + "\n";
        boolean rangeSpecified = false; // unless found otherwise below
        String quoted[];
        if (lines.size() > 3 + labelLocation) {
            if (lines.size() > 4 + labelLocation) {
                String range = lines.get(labelLocation+2).trim();
                int lastPartEndIndex = code.indexOf(range) + range.length();
                String lastPart = code.substring(lastPartEndIndex);
                quoted = splitCodeByQuotes(lastPart);
                rangeSpecified = (quoted.length > 8 && ServerUtils.isInteger(quoted[5]));
                if (rangeSpecified) {
                    commandToAddAPlot += 
                            ServerUtils.parseInt(quoted[1], "createPlot", clientState) + "\n" + 
                                    ServerUtils.parseInt(quoted[3], "createPlot", clientState) + "\n" +
                                    ServerUtils.parseInt(quoted[5], "createPlot", clientState) + "\n" + 
                                    ServerUtils.parseInt(quoted[7], "createPlot", clientState) + "\n" + 
                                    // no auto-plot if dimensions specified explicitly
                                    "false\n";
                }
            }
            if (!rangeSpecified) {
                commandToAddAPlot += 
                        0.0 + "\n" + 1.0 + "\n" + 0.0 + "\n" + 1.0 + "\ntrue\n";
            }

            int legendIndex = rangeSpecified ? 11 : 9;
            if (lines.size() <= legendIndex) {
                legendIndex = lines.size()-1;
                //		if (lines.size() == 8) {
                //		    legendIndex = 7;
                //		} else if (lines.size() == 6) {
                //		    legendIndex = 5;
                //		}
            }	    
            if (lines.size() <= legendIndex || lines.get(legendIndex).isEmpty()) {
                commandToAddAPlot += "false\n";
            } else {
                String legendString = lines.get(legendIndex).trim();
                if (legendString.equalsIgnoreCase("false")) {
                    // no legend
                    commandToAddAPlot += "false\n";
                } else if (legendString.equalsIgnoreCase("true")) {
                    // legend when pens added
                    // need to add a pen but it doesn't need to be in the legend
                    commandToAddAPlot += "true\n";
                } else {
                    // create a legend
                    commandToAddAPlot += "true\nPENS\n";
                    String[] legends = splitCodeByQuotes(legendString);
                    // legends should alternate between labels and color names
                    int legendCount = legends.length/4;
                    for (int i = 0; i < legendCount; i++) {
                        // skip every other because of empty strings
                        String colorNumber = colorNumber(legends[i*4+3]);
                        if (colorNumber != null) {
                            String label = legends[i*4+1];
                            commandToAddAPlot += "\"" + label + "\"" +
                                    " 1.0 0 " + colorNumber + " true\n";
                        } else {
                            Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
                                    legends[i*4+1] + " is not a recognised pen color");
                        }
                    }
                }
            }
        }
        commandToAddAPlot += " \"\" \"\"\n";
        widgetsToCreate.add(commandToAddAPlot);
    }

    private String getWidgetWithName(String widgetType, String widgetName) {
        for (String widget : widgetsToCreate) {
            if (widget.startsWith(widgetType)) {
                if (widget.indexOf("\n" + widgetName + "\n") > 0) {
                    return widget;
                }
            }
        }
        return null;
    }

    private Object getWidgetWithCoordinates(String widgetType, String coordinates) {
        for (String widget : widgetsToCreate) {
            if (widget.startsWith(widgetType)) {
                if (widget.indexOf(coordinates) > 0) {
                    return widget;
                }
            }
        }
        return null;
    }

    private String colorNumber(String name) {
        // a hack to use NetLogo's pen color encodings as a table
        if (name.equalsIgnoreCase("black")) {
            return "-16777216";
        } else if (name.equalsIgnoreCase("cyan")) {
            return "-11221820";
        } else if (name.equalsIgnoreCase("white")) {
            return "-1";
        } else if (name.equalsIgnoreCase("red")) {
            return "-2674135";
        } else if (name.equalsIgnoreCase("green")) {
            return "-10899396";
        } else if (name.equalsIgnoreCase("blue")) {
            return "-13345367";
        } else if (name.equalsIgnoreCase("gray")) {
            return "-7500403";
        } else if (name.equalsIgnoreCase("orange")) {
            return "-955883"; 
        } else if (name.equalsIgnoreCase("brown")) {
            return "-6459832"; 
        } else if (name.equalsIgnoreCase("yellow")) {
            return "-1184463";
        } else if (name.equalsIgnoreCase("lime")) {
            return "-13840069";
        } else if (name.equalsIgnoreCase("turquoise")) {
            return "-14835848";
        } else if (name.equalsIgnoreCase("sky")) {
            return "-13791810"; 
        } else if (name.equalsIgnoreCase("violet")) {
            return "-8630108";
        } else if (name.equalsIgnoreCase("magenta")) {
            return "-5825686";
        } else if (name.equalsIgnoreCase("pink")) {
            return "-2064490";
        } else {
            try {
                Integer.parseInt(name);
                return name;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    protected void updateWorldBoundingBox(int llx, int lly, int urx, int ury) {
        if (urx > worldURX) {
            setWorldURX(urx);
        }
        if (ury > worldURY) {
            setWorldURY(ury);
        }
    }

    protected static String firstQuoted(String line) {
        String quoted[] = splitCodeByQuotes(line);
        if (quoted.length < 2) {
            return line;
        } else {
            return quoted[1];
        }
    }

    protected String createCommandToAdd(
            String widgetType, 
            String widgetName, 
            int llx, int lly, int urx, int ury) {
        return createCommandToAdd(widgetType, widgetName, llx, lly, urx, ury, 0, 0);
    }

    protected String createCommandToAdd(
            String widgetType, 
            String widgetName, 
            int llx, int lly, int urx, int ury,
            int minimumWidth, int minimumHeight) {
        // this moves the widget down if there already is something with these coordinates
        // widgetName is sometimes a button label or the first line of text in a text area
        // used to prevent this from avoiding placing a widget on top of itself
        // should test whether two text areas with the same coordinates and same first line may end up on top of each other
        String coordinates = llx + "\n" + lly + "\n" + urx + "\n" + ury + "\n";
        int width = urx - llx;
        int height = ury - lly;
        if (width < minimumWidth) {
            int delta = minimumWidth - width;
            urx += delta; // grow to the right
            width = minimumWidth;
        }
        if (height < minimumHeight) {
            int delta = minimumHeight - height;
            ury += delta; // grow downward
            height = minimumHeight;
        }
        while (getWidgetWithCoordinates(widgetType, coordinates) != null) {
            if (width < height) {    
                int shiftRight = width + 3; 
                // 3 pixels space between them
                llx += shiftRight; // move right and try again
                urx += shiftRight;
            } else {
                int shiftDown = height + 3;
                lly += shiftDown;
                ury += shiftDown;
            }
            coordinates = llx + "\n" + lly + "\n" + urx + "\n" + ury + "\n";
        }
        updateWorldBoundingBox(llx, lly, urx, ury);
        return widgetType + "\n" + coordinates;
    }

    protected void createSlider(String code) {
        // first remove spurious returns within quoted elements
        // can be caused by extracting value from textarea
        code = code.replace("\"\n", "\"").replace("\n\"", "\"");
        String quoted[] = splitCodeByQuotes(code);
        // [name-of-variable llx lly urx ury min-value max-value increment current-value]
        if (quoted.length < 18) {
            Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).warning(
                    "Too few quoted arguments to create-slider in the following\n" + code);
            return;
        }
        // a bit of a hack but this is to work around NetLogo limitations
        // (that this can't be run within NetLogo)
        int llx = ServerUtils.parseInt(quoted[3], "createSlider", clientState);
        int lly = ServerUtils.parseInt(quoted[5], "createSlider", clientState);
        int urx = ServerUtils.parseInt(quoted[7], "createSlider", clientState);
        int ury = ServerUtils.parseInt(quoted[9], "createSlider", clientState);
        final String variableName = quoted[1].trim();
        String minimumValue = quoted[11];
        String maximumValue = quoted[13];
        String increment = quoted[15];
        String initialValue = quoted[17];
        String units = "NIL";
        if (quoted.length > 19 && !quoted[19].isEmpty()) {
            units = quoted[19];
        }
        boolean horizontal = true;
        if (quoted.length > 21 && !quoted[21].isEmpty()) {
            horizontal = !quoted[21].equalsIgnoreCase("false");
        }
        addCommandToAddSlider(variableName, llx, lly, urx, ury, minimumValue,
                maximumValue, increment, initialValue, units, horizontal);
        addExtraGlobalVariable(variableName, false, false);
    }

    public void addCommandToAddSlider(String variableName, 
            int llx, int lly, int urx, int ury, 
            String minimumValue,
            String maximumValue, 
            String increment, 
            String initialValue,
            String units,
            boolean horizontal) {
        String commandToAddASlider = createCommandToAdd("SLIDER", variableName, llx, lly, urx, ury);
        commandToAddASlider = commandToAddASlider + 
                variableName + "\n" +
                variableName + "\n" +
                // it is there twice (label and variable name?)
                minimumValue + "\n" + maximumValue + "\n" + initialValue + "\n" +
                increment + "\n" +
                "1\n" + // not clear what this does
                units + "\n" +
                (horizontal ? "HORIZONTAL\n" : "VERTICAL\n");
        for (int i = 0; i < widgetsToCreate.size(); i++) {
            String widget = widgetsToCreate.get(i);
            if (widget.startsWith("SLIDER") && widget.indexOf(variableName) > 0) {
                // already defined a slider for this variable
                // replace the old one with this one
                widgetsToCreate.set(i, commandToAddASlider);
                return;
            }
        }
        widgetsToCreate.add(commandToAddASlider);
    }

    public void addCommandToAddInputBox(String variableName, 
            int llx, int lly, int urx, int ury,
            String initialValue,
            boolean multiLine, 
            String typeString) {
        String commandToAddAnInputBox = createCommandToAdd("INPUTBOX", variableName, llx, lly, urx, ury);
        // the following was needed for older versions of NetLogo
        //	String initialised = initialValue.isEmpty() ? "0" : "1";
        if (initialValue.isEmpty()) {
            initialValue = "NIL";
        }
        commandToAddAnInputBox = commandToAddAnInputBox + variableName + "\n" +
                initialValue + "\n" + (multiLine ? "1" : "0") + "\n0\n" +
                typeString + "\n";
        if (widgetsToCreate.indexOf(commandToAddAnInputBox) < 0) {
            // haven't already planned to create this one
            widgetsToCreate.add(commandToAddAnInputBox);
        }
    }

    public void addCommandToAddSwitch(String variableName, 
            int llx, int lly, int urx, int ury,
            String initialValue) {
        String commandToAddASwitch = createCommandToAdd("SWITCH", variableName, llx, lly, urx, ury);
        commandToAddASwitch += 
                variableName + "\n" + variableName + "\n" + 
                        (initialValue.equalsIgnoreCase("true") ? "0\n" : "1\n");
        commandToAddASwitch += "1\n-1000\n";
        if (widgetsToCreate.indexOf(commandToAddASwitch) < 0) {
            // haven't already planned to create this one
            widgetsToCreate.add(commandToAddASwitch);
        }
    }

    protected void createMonitor(String arguments) {
        // a bit of a hack but this is to work around NetLogo limitations
        // (that this can't be run within NetLogo)
        if (arguments == null) {
            clientState.warn("No arguments passed to create-monitor.\n" + arguments);
            return;
        }
        NetLogoTokenizer tokenizer = new NetLogoTokenizer(arguments);
        String token = tokenizer.nextToken(false);
        String label = CommonUtils.stringIfQuoted(token);
        if (label == null) {
            clientState.warn("Expected first argument to create-monitor to be a label.\n" + arguments);
            return;
        }
        Integer llx = null;
        Integer lly = null;
        Integer urx = null;
        Integer ury = null;
        token = tokenizer.nextToken(false);
        Integer number = CommonUtils.integerIfQuoted(token);
        if (number != null) {
            llx = number;
            token = tokenizer.nextToken(false);
            number = CommonUtils.integerIfQuoted(token);
            if (number != null) {
                lly = number;
            } else {
                clientState.warn("Expected second to fifth arguments to create-monitor to be numbers.\n" + arguments);
                return;
            }
            token = tokenizer.nextToken(false);
            number = CommonUtils.integerIfQuoted(token);
            if (number != null) {
                urx = number;
            } else {
                clientState.warn("Expected second to fifth arguments to create-monitor to be numbers.\n" + arguments);
                return;
            }
            token = tokenizer.nextToken(false);
            number = CommonUtils.integerIfQuoted(token);
            if (number != null) {
                ury = number;
            } else {
                clientState.warn("Expected second to fifth arguments to create-monitor to be numbers.\n" + arguments);
                return;
            }
        } else {
            clientState.warn("Expected second to fifth arguments to create-monitor to be numbers.\n" + arguments);
            return;
        }
        String commandToAddAMonitor = createCommandToAdd("MONITOR", label, llx, lly, urx, ury);
        String expression = tokenizer.nextToken(false);
        if (expression.equals("(")) {
            expression = collectCodeInParens(tokenizer);	    
        }
        token = tokenizer.nextToken(false);
        Integer numberOfDecimals = CommonUtils.integerIfQuoted(token);
        if (numberOfDecimals == null) {
            numberOfDecimals = 0;
        }
        token = tokenizer.nextToken(false);
        Integer fontSize = CommonUtils.integerIfQuoted(token);
        if (fontSize == null) {
            fontSize = 11;
        }
        commandToAddAMonitor += 
                // not clear what final 1 does 
                label + "\n" + expression + "\n" + numberOfDecimals + "\n" + "1\n" + fontSize + "\n\n";
        if (widgetsToCreate.indexOf(commandToAddAMonitor) < 0) {
            // haven't already planned to create this one
            widgetsToCreate.add(commandToAddAMonitor);
        }
    }

    protected void createChooser(String arguments) {
        // [variable-name llx lly urx ury default-choice choice-1 ... choice-n]
        if (arguments == null) {
            clientState.warn("No arguments passed to create-chooser.\n" + arguments);
            return;
        }
        NetLogoTokenizer tokenizer = new NetLogoTokenizer(arguments);
        String token = tokenizer.nextToken(false);
        String variableName = CommonUtils.stringIfQuoted(token);
        if (variableName == null) {
            clientState.warn("Expected first argument to create-chooser to be a variable name.\n" + arguments);
            return;
        }
        Integer llx = null;
        Integer lly = null;
        Integer urx = null;
        Integer ury = null;
        token = tokenizer.nextToken(false);
        Integer number = CommonUtils.integerIfQuoted(token);
        if (number != null) {
            llx = number;
            token = tokenizer.nextToken(false);
            number = CommonUtils.integerIfQuoted(token);
            if (number != null) {
                lly = number;
            } else {
                clientState.warn("Expected second to fifth arguments to create-chooser to be numbers.\n" + arguments);
                return;
            }
            token = tokenizer.nextToken(false);
            number = CommonUtils.integerIfQuoted(token);
            if (number != null) {
                urx = number;
            } else {
                clientState.warn("Expected second to fifth arguments to create-chooser to be numbers.\n" + arguments);
                return;
            }
            token = tokenizer.nextToken(false);
            number = CommonUtils.integerIfQuoted(token);
            if (number != null) {
                ury = number;
            } else {
                clientState.warn("Expected second to fifth arguments to create-chooser to be numbers.\n" + arguments);
                return;
            }
        } else {
            clientState.warn("Expected second to fifth arguments to create-chooser to be numbers.\n" + arguments);
            return;
        }
        String commandToAddAChooser = createCommandToAdd("CHOOSER", variableName, llx, lly, urx, ury);
        // variableName is there twice for some reason
        commandToAddAChooser += variableName + "\n" + variableName + "\n";
        token = tokenizer.nextToken(false);
        Integer defaultChoice = CommonUtils.integerIfQuoted(token);
        if (defaultChoice == null) {
            clientState.warn("Expected sixth argument to create-chooser to be a number indicating the default choice.\n" + arguments);
            return;
        }
        int choiceCount = 0;
        while ((token = tokenizer.nextToken(false)) != null) {
            commandToAddAChooser += token + " ";
            choiceCount++;
        }
        if (defaultChoice >= choiceCount || defaultChoice < 0) {
            clientState.warn("There are only " + choiceCount + " choices for " + variableName + " so the default choice of " + defaultChoice + " is changed to " + (choiceCount-1));
            defaultChoice = (choiceCount-1);
        }
        commandToAddAChooser += "\n" + defaultChoice + "\n\n"; 
        if (widgetsToCreate.indexOf(commandToAddAChooser) < 0) {
            // haven't already planned to create this one
            widgetsToCreate.add(commandToAddAChooser);
        }
        addExtraGlobalVariable(variableName, false, false);
    }

    protected void createText(String code, boolean includeFontAndColor) {
        String quoted[] = splitCodeByQuotes(code);
        // if includeFontAndColor
        // [llx lly urx ury font-size colour-number transparent line-1 ... line-n]
        // otherwise
        // [llx lly urx ury line-1 ... line-n]
        int minimumNumber = includeFontAndColor ? 15 : 9;
        if (quoted.length <= minimumNumber) {
            if (quoted.length == minimumNumber) {
                warn("No quoted text is provided in create-color-text in the following\n" + code);   
            } else {
                warn("Too few quoted arguments to create-color-text in the following\n" + code);   
            }
            return; 
        }
        // a bit of a hack but this is to work around NetLogo limitations
        // (that this can't be run within NetLogo)
        int llx = ServerUtils.parseInt(quoted[1], "createText", clientState);
        int lly = ServerUtils.parseInt(quoted[3], "createText", clientState);
        int urx = ServerUtils.parseInt(quoted[5], "createText", clientState);
        int ury = ServerUtils.parseInt(quoted[7], "createText", clientState);
        String commandToAddATextBox = createCommandToAdd("TEXTBOX", quoted[minimumNumber], llx, lly, urx, ury);
        for (int i = minimumNumber; i < quoted.length; i += 2) {
            commandToAddATextBox = commandToAddATextBox + quoted[i];
            if (i < quoted.length - 2) { 
                // not last one so add the code for a new line (but the new line now)
                commandToAddATextBox += "\\n";
            }
        }
        if (includeFontAndColor) {
            commandToAddATextBox += 
                    "\n" + quoted[9] + "\n" + quoted[11] + ".0\n" + 
                            (quoted[13].equalsIgnoreCase("false") ? "0\n\n" : "1\n\n");
        } else {
            // following not tested since updated but only very old micro-behaviours will have no includeFontAndColor
            commandToAddATextBox += "12\n0\n\0\n\n";
        }
        if (widgetsToCreate.indexOf(commandToAddATextBox) < 0) {
            // haven't already planned to create this one
            widgetsToCreate.add(commandToAddATextBox);
        }
    }

    protected void createButton(String createButtonExpression, boolean addSetButtonAction, boolean netLogoCode) {
        // TODO: update to use NetlogoTokenizer
        // addSetButtonAction is needed for older micro-behaviours (prior to MB.4)
        String quoted[] = splitCodeByQuotes(createButtonExpression);
        // [llx lly urx ury button-label command keyboard-shortcut]
        // the last argument can be ""
        if (quoted.length < 14) {
            warn("Too few quoted arguments to create-button in the following\n" + createButtonExpression);
            return;
        }
        // a bit of a hack but this is to work around NetLogo limitations
        // (that this can't be run within NetLogo)
        int llx = ServerUtils.parseInt(quoted[1], "createButton", clientState);
        int lly = ServerUtils.parseInt(quoted[3], "createButton", clientState);
        int urx = ServerUtils.parseInt(quoted[5], "createButton", clientState);
        int ury = ServerUtils.parseInt(quoted[7], "createButton", clientState);
        String shortcut;
        if (quoted[13].length() == 0) {
            shortcut = "NIL";
        } else {
            shortcut = quoted[13].substring(0, 1); // just first character
        }
        String code = quoted[11].trim();
        if (code.charAt(0) == '[') {
            // a list of behaviours so just run them
            int lastIndexOf = code.lastIndexOf(']');
            code = code.substring(1, lastIndexOf);
        }
        findBreedVariables(code); 
        // in case my-next- is used only here
        String buttonLabel = quoted[9].trim();
        String commandToAddAButton = createCommandToAdd("BUTTON", buttonLabel, llx, lly, urx, ury);
        commandToAddAButton += buttonLabel + "\n";
        if (addSetButtonAction) {
            commandToAddAButton += "set button-command \"";
        }
        String buttonCode;
        if (netLogoCode) {
            buttonCode = code;
        } else {
            final String[] operationAndBody = code.split("(\\s)+", 2); // white space
            // following should really transform the code completely but
            // following is enough for existing micro-behaviours
            String[] newOperationAndBody;
            try {		
                String body = operationAndBody.length > 1 ? operationAndBody[1] : "";
                newOperationAndBody = MicroBehaviour.expandSetMyNext(operationAndBody[0], false, body, false, true);
                buttonCode = newOperationAndBody[0] + newOperationAndBody[1];	
                buttonCode = MicroBehaviour.replaceObsoleteAddBehaviours(buttonCode);
                // if uninitialised then call setup first
                if (!buttonCode.trim().equals("(setup)")) {
                    buttonCode = "setup-only-if-needed " + buttonCode;
                }
            } catch (Exception e) {
                e.printStackTrace();
                warn(e.getMessage());
                return;
            }  
        }
        // new lines break NetLogo
        commandToAddAButton += buttonCode.replace("\n", " ");
        if (addSetButtonAction) {
            commandToAddAButton += "\"";
        }
        if (quoted.length > 15 && quoted[15].equalsIgnoreCase("true")) {
            // forever switch
            commandToAddAButton += "\nT\n";
        } else {
            commandToAddAButton += "\nNIL\n";
        }
        commandToAddAButton += "1\nT\nOBSERVER\nNIL\n";
        // not sure if these parameters are worth providing an interface
        commandToAddAButton += shortcut + "\nNIL\nNIL\n1\n\n";
        if (widgetsToCreate.indexOf(commandToAddAButton) < 0) {
            // don't already plan to create this one
            widgetsToCreate.add(commandToAddAButton);
        }
    }

    public static String[] splitCodeByQuotes(String code) {
        return splitCodeByQuotes(code, 0);
    }

    public static String[] splitCodeByQuotes(String code, int count) {
        code = code.trim();
        if (code.charAt(0) == '\'') {
            return code.split("'", count);
        } else {
            return code.split("\"", count);
        }
    }

    protected void createOutputArea(String code) {
        String quoted[] = splitCodeByQuotes(code);
        // e.g. create-output-area 624 10 1246 546 font-size; lower left corner followed
        // by upper right
        if (quoted.length < 8) {
            warn("Too few quoted arguments to create-output-area in the following\n" + code);
            return;
        }
        outputAreaSpecified = true;
        try {
            int llx = ServerUtils.parseInt(quoted[1], "createOutputArea", clientState);
            int lly = ServerUtils.parseInt(quoted[3], "createOutputArea", clientState);
            int urx = ServerUtils.parseInt(quoted[5], "createOutputArea", clientState);
            int ury = ServerUtils.parseInt(quoted[7], "createOutputArea", clientState);
            if (urx-llx <= 0 || ury-lly <= 0) {
                return; // no output wanted
            }
            // so applets are wide enough to include this area
            updateWorldBoundingBox(llx, lly, urx, ury);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).warning("Could not parse dimensions of the output area");
            return;
        }
        String commandToAddAButton = 
                "OUTPUT\n" + quoted[1] + "\n" + quoted[3] +
                "\n" + quoted[5] + "\n" + quoted[7] + "\n";
        String fontSize = "12";
        if (quoted.length > 9) {
            fontSize = quoted[9];
        }
        commandToAddAButton += fontSize + "\n\n";
        if (widgetsToCreate.indexOf(commandToAddAButton) < 0) {
            // don't already plan to create this one
            widgetsToCreate.add(commandToAddAButton);
        }
    }

    protected void createSwitch(String code) {
        String quoted[] = splitCodeByQuotes(code);
        // e.g. create-switch 624 10 1246 546 name on-or-off
        // lower left corner followed
        // by upper right
        if (quoted.length < 12) {
            warn("Too few quoted arguments to create-switch in the following\n" + code);
            return;
        }
        String variableName = quoted[1].trim();
        boolean on = !quoted[11].equalsIgnoreCase("false");
        try {
            int llx = ServerUtils.parseInt(quoted[3], "createSwitch", clientState);
            int lly = ServerUtils.parseInt(quoted[5], "createSwitch", clientState);
            int urx = ServerUtils.parseInt(quoted[7], "createSwitch", clientState);
            int ury = ServerUtils.parseInt(quoted[9], "createSwitch", clientState);
            // so applets are wide enough to include this area
            updateWorldBoundingBox(llx, lly, urx, ury);
            String commandToAddASwitch = createCommandToAdd("SWITCH", variableName, llx, lly, urx, ury);
            // variableName is there twice for some reason
            commandToAddASwitch += 
                    variableName + "\n" + variableName + "\n" + (on ? "0\n" : "1\n");
            if (widgetsToCreate.indexOf(commandToAddASwitch) < 0) {
                // don't already plan to create this one
                widgetsToCreate.add(commandToAddASwitch);
            }
            addExtraGlobalVariable(variableName, false, false);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).warning("Could not parse dimensions of the switch");
            return;
        }
    }

    // move following to somewhere more logical
    public static boolean contains(String[] strings, String string) {
        if (string == null) {
            return false; // warn??
        }
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] != null && string.equals(strings[i]))
                return true;
        }
        return false;
    }

    public MicroBehaviour addRequiredBehaviourName(String description, String url, MicroBehaviour referringMicroBehaviour) {
        MicroBehaviour newReferencedMicroBehaviour = 
                new ReferencedMicroBehaviour(description, url, referringMicroBehaviour, resourcePageServiceImpl, this);
        return addRequiredMicroBehaviour(newReferencedMicroBehaviour);
    }

    public MicroBehaviour addRequiredMicroBehaviour(MicroBehaviour newReferencedMicroBehaviour) {
        //	String name = newReferencedMicroBehaviour.getBehaviourName();
        //	for (MicroBehaviour referencedMicroBehaviour : totalRequired) {
        //	    if (name.equals(referencedMicroBehaviour.getBehaviourName())) {
        //		return newReferencedMicroBehaviour; // duplicate so do nothing
        //	    }
        //	}
        if (newReferencedMicroBehaviour.isMacroBehaviourAsMicroBehaviour()) {
            // assumes that the macro behaviour is processed as a top-level "prototype"
            return newReferencedMicroBehaviour;
        }
        if (microBehaviourIsMember(newReferencedMicroBehaviour, totalRequired)) {
            return newReferencedMicroBehaviour; // duplicate so do nothing
        }
        totalRequired.add(newReferencedMicroBehaviour);
        return newReferencedMicroBehaviour;
    }

    public void addMacroBehaviour(MacroBehaviour newMacroBehaviour) {
        macroBehaviours.add(newMacroBehaviour);
    }

    public void resetMacroBehaviours() {
        macroBehaviours = new ArrayList<MacroBehaviour>(); 
    }

    public static String removeComments(String code) {
        String parts[] = code.split(";");
        if (parts.length == 1) {
            return code; // no comments
        } else {
            String answer = "";
            for (int i = 0; i < parts.length; i += 2) { // every other
                answer += parts[i] + " ";
            }
            return answer;
        }
    }

    public boolean addBreedVariable(String variable, boolean writing) {
        // returns true unless it is an exception
        if (variable.endsWith("-set")) {
            // happens in NetLogo primitive micro-behaviours (maybe they should be lightly transformed?)
            return false;
        }
        if (variable.endsWith("-of")) {
            variable = variable.substring(0, variable.length() - 3); 
            // remove -of part
        }
        if (variable.length() > 8 && variable.substring(0, 8).equalsIgnoreCase("my-next-")) {
            String myForm = "my-" + variable.substring(8);
            if (addBreedVariable(myForm, writing)) { 
                // perhaps only the "next" form is referred to
                for (int i = 0; i < breedVariables.size(); i += 2) {
                    if (breedVariables.get(i).equals(myForm)) {
                        breedVariables.set(i + 1, variable);
                        return true;
                    }
                }
            }
            return false;
        }
        for (int i = 0; i < breedVariablesExceptions.length; i++) {
            if (breedVariablesExceptions[i].equalsIgnoreCase(variable)) {
                // this should be ignored -- e.g. my-location which
                // is defined by a procedure
                return false; 

            }
        }
        if (is3DVariable(variable)) {
            dimensions = 3;
        }
        if (!breedVariables.contains(variable)) {
            breedVariables.add(variable);
            breedVariables.add(null);
        }
        return true;
    }

    public boolean addExpectedGlobalVariable(String variable, boolean writing) {
        variable = variable.trim().toLowerCase(); // since NetLogo is case-insensitive (do this elsewhere too?)
        if (variable.equals("the-other")) {
            // could rename the-other instead but too many micro-behaviours use it (13 as of 22 Oct 2008)
            return false;
        }
        if (!expectedGlobalVariables.contains(variable)) {
            expectedGlobalVariables.add(variable);
            return true;
        }
        return false;
    }

    public boolean addPatchOrLinkVariable(String variable, boolean patch, boolean writing) {
        // returns true unless it is an exception
        if (variable.endsWith("-of")) {
            variable = variable.substring(0, variable.length() - 3); 
            // remove -of part
        }
        String variables[] = patch ? patchVariables : linkVariables;
        if (variable.toLowerCase().startsWith("next-")) {
            String patch_form = variable.substring(5);
            if (addPatchOrLinkVariable(patch_form, patch, writing)) { 
                // perhaps only the "next" form is referred to
                for (int i = 0; i < variables.length; i += 2) {
                    if (variables[i].equals(patch_form)) {
                        variables[i + 1] = variable;
                        return true;
                    }
                }
            }
            return false;
        }
        String newVariables[];
        int oldVariableCount;
        if (variables == null) {
            oldVariableCount = 0;
            newVariables = new String[2];
        } else {
            oldVariableCount = variables.length;
            for (int i = 0; i < oldVariableCount; i += 2) {
                if (variables[i].equalsIgnoreCase(variable)) {
                    return true; // already know about it
                }
            }
            newVariables = new String[oldVariableCount + 2];
            for (int i = 0; i < oldVariableCount; i++) {
                newVariables[i] = variables[i];
            }
        }
        newVariables[oldVariableCount] = variable;
        newVariables[oldVariableCount + 1] = null;
        if (patch) {
            patchVariables = newVariables;
        } else {
            linkVariables = newVariables;
        }
        return true;
    }

    protected static boolean contains3DCode(String code) {
        return contains3DPrimitives(code) || contains3DVariables(code);
    }

    private static boolean contains3DVariables(String code) {
        return contains3DTurtleVariables(code) || contains3DPatchVariables(code);
    }

    protected static boolean contains3DTurtleVariables(String code) {
        for (String variable : CommonUtils.NETLOGO_TURTLE_VARIABLES_3D) {
            if (containsProcedureCall(code, variable)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean contains3DPatchVariables(String code) {
        for (String variable : CommonUtils.NETLOGO_PATCH_VARIABLES_3D) {
            if (containsProcedureCall(code, variable)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean contains3DPrimitives(String code) {
        for (String primitive : CommonUtils.NETLOGO_PRIMITIVES_3D) {
            if (containsProcedureCall(code, primitive)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean is3DVariable(String variable) {
        for (String variable3D : CommonUtils.NETLOGO_TURTLE_VARIABLES_3D) {
            if (variable.equals(variable3D)) {
                return true;
            }
        }
        return false;
    }

    protected void setWorldGeometry(int code) {
        switch (code) {
        case 1: // torus
            horizontallyWrapping = 1;
            verticallyWrapping = 1;
            break;
        case 2: // vertical cylinder
            horizontallyWrapping = 1;
            verticallyWrapping = 0;
            break;
        case 3: // horizontal cylinder
            horizontallyWrapping = 0;
            verticallyWrapping = 1;
            break;
        case 4: // flat
        case 5: // flat with camera following centroid
            horizontallyWrapping = 0;
            verticallyWrapping = 0;
            break;
        default:
            warn("Unrecognised world geometry code: " + code);
        }
    }

    protected void initialiseWorldParameters() {
        // NetLogo's default is 35 x 35 grid but we use 21x21 
        minPxcor = -10;
        maxPxcor = 10;
        minPycor = -10;
        maxPycor = 10;
        minPzcor = -10;
        maxPzcor = 10;
        patchSize = 24.0;
        worldLLX = 104;
        worldLLY = 10;
        worldURX = 0;
        worldURY = 0;
        labelFontSize = 10;
        horizontallyWrapping = 1;
        verticallyWrapping = 1;
    }

    protected long getWorldWidth() {
        return Math.round(getPatchSize() * (1+getMaxPxcor()-getMinPxcor()));
    }

    protected long getWorldHeight() {
        return Math.round(getPatchSize() * (1+getMaxPycor()-getMinPycor()));
    }

    protected static String quoteSingleQuotes(String s) {
        String answer = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\'') {
                answer += "\"";
            } else {
                answer += s.charAt(i);
            }
        }
        return answer;
    }

    //    protected String generateInitialiseBehaviourNames() {
    //	// list of names and numbers
    //	StringBuilder answer = new StringBuilder("to initialise-behaviour-names\n"
    //		                               + " set behaviour-procedure-numbers [");
    //	for (MicroBehaviour behaviour : behaviours) {
    //	    answer.append(" [\"" + behaviour.getBehaviourName() + "\" " + behaviour.getNetLogoNumber()+ "]\n");
    //	}
    //	answer.append("]\n");
    //	answer.append(" set behaviour-names [");
    //	answer.append(behaviourNames);
    //	answer.append("]\nend\n");
    //	behaviours.clear(); // reset to be ready for next time
    //	behaviourNames = new StringBuilder(); // reset to be ready for next time
    //	return answer.toString();
    //    }

    protected void findAllKindsOfVariables(String code) {
        ServerUtils.findAllKindsOfVariables(code, this);
    }

    protected void findBreedVariables(String code) {
        ServerUtils.findBreedVariables(code, this);
    }

    protected void findGlobalVariables(String code) {
        ServerUtils.findGlobalVariables(code, this);
    }

    protected void findPatchVariables(String code) {
        ServerUtils.findPatchVariables(code, this);
    }

    protected void findLinkVariables(String code) {
        ServerUtils.findLinkVariables(code, this);
    }

    public int getMinPxcor() {
        return minPxcor;
    }

    public void setMinPxcor(int minPxcor) {
        this.minPxcor = minPxcor;
    }

    public int getMaxPxcor() {
        return maxPxcor;
    }

    public void setMaxPxcor(int maxPxcor) {
        this.maxPxcor = maxPxcor;
    }

    public int getMinPycor() {
        return minPycor;
    }

    public void setMinPycor(int minPycor) {
        this.minPycor = minPycor;
    }

    public int getMaxPycor() {
        return maxPycor;
    }

    public void setMaxPycor(int maxPycor) {
        this.maxPycor = maxPycor;
    }

    public double getPatchSize() {
        return patchSize;
    }

    public void setPatchSize(double patchSize) {
        this.patchSize = patchSize;
    }

    public int getWorldLLX() {
        return worldLLX;
    }

    public void setWorldLLX(int worldLLX) {
        this.worldLLX = worldLLX;
    }

    public int getWorldLLY() {
        return worldLLY;
    }

    public void setWorldLLY(int worldLLY) {
        this.worldLLY = worldLLY;
    }

    public int getWorldURX() {
        return worldURX;
    }

    public void setWorldURX(int worldURX) {
        if (worldURX > this.worldURX) {
            this.worldURX = worldURX;
        }
    }

    public int getWorldURY() {
        return worldURY;
    }

    public void setWorldURY(int worldURY) {
        if (worldURY > this.worldURY) {
            this.worldURY = worldURY;
        }
    }

    public int getAppletWidth() {
        // if SET-WORLD-SIZE or SET-PATCH-SIZE has been used then getWorldWidth() together with where the 'world' is located may determine the width
        // 20 pixels seems to be a sufficient margin for a default sized World
        // 36 seems to work when World size dominates
        return (int) (Math.max(getWorldLLX()+getWorldWidth()+36, worldURX+20)); 
    }

    public int getAppletHeight() {
        // if SET-WORLD-SIZE or SET-PATCH-SIZE has been used then getWorldHeight() may determine the height
        // 41 seems to work when World size dominates
        return (int) Math.max(getWorldLLY()+getWorldHeight()+41, worldURY); // extra margin not needed anymore
    }

    public int getLabelFontSize() {
        return labelFontSize;
    }

    public void setLabelFontSize(int labelFontSize) {
        this.labelFontSize = labelFontSize;
    }

    //    public ArrayList<MicroBehaviour> getBehaviours() {
    //        return behaviours;
    //    }

    public StringBuilder getBehaviourNames() {
        return behaviourNames;
    }

    public void warn(String message) {
        clientState.warn(message);
    }

    public void logException(Exception e, String message) {
        clientState.logException(e, message);
    }

    public ClientState getClientState() {
        return clientState;
    }

    public boolean addExtraGlobalVariable(String variableName, boolean declareIt, boolean writing) {
        variableName = variableName.trim().toLowerCase(); // since NetLogo is case-insensitive
        if (variableName.equals("the-default-buttons-should-not-be-added")) {
            return false; // just a way to specify no default buttons
        }
        if (!encounteredGlobalVariables.contains(variableName)) {
            if (declareIt) {
                globalVariablesToDeclare.add(variableName);
            }
            encounteredGlobalVariables.add(variableName);
            return true;
        } else {
            if (!declareIt && globalVariablesToDeclare.contains(variableName)) {
                // shouldn't be declared -- e.g. is a slider or input box
                globalVariablesToDeclare.remove(variableName);
            }
            return false;
        }
    }

    public ArrayList<String> getGeneratedCommands() {
        return generatedCommands;
    }

    public void addGlobalInitialisation(String initialisation) {
        globalInitialisations.append(' ');
        globalInitialisations.append(initialisation);
        globalInitialisations.append('\n');
    }

    //    public void doNotAddClockedSwitch() {
    //	addClockedSwitch = false;	
    //    }

    public void doNotAddDefaultButtons() {
        addDefaultButtons = false;	
    }

    public MacroBehaviour getMacroBehaviourNamed(String kindName) {
        for (MacroBehaviour macroBehaviour : macroBehaviours) {
            if (macroBehaviour.getObjectName().equals(kindName)) {
                return macroBehaviour;
            }
        }
        return getMacroBehaviourIgnoringHTML(kindName);
    }

    public MacroBehaviour getMacroBehaviourIgnoringHTML(String nameHTML) {
        // compare without HTML or spaces
        // whitespace removed on 7/7/14 because IE, FireFox, and Chrome differ on how to encode new line in prototype name
        nameHTML = CommonUtils.removeHTMLMarkup(nameHTML).replaceAll("\\s","");
        for (MacroBehaviour macroBehaviour : macroBehaviours) {
            String otherNameHTML = CommonUtils.removeHTMLMarkup(macroBehaviour.getObjectName().replace(" ", "")).replaceAll("\\s","");
            if (nameHTML.equalsIgnoreCase(otherNameHTML)) {
                return macroBehaviour;
            }
        }
        return null;
    }

    public void microBehaviourRenamed(String oldURL, String newURL) {
        microBehaviourRenamings.add(oldURL);
        microBehaviourRenamings.add(newURL);
    }

    public ArrayList<String> getMicroBehaviourRenamings() {
        return microBehaviourRenamings;
    }

    public void addUnneededCommands(String behaviourName) {
        unneededCommands.add(behaviourName);
    }

    public void setOutputAreaNeeded() {
        outputAreaNeeded = true;	
    }

    public ResourcePageServiceImpl getResourcePageServiceImpl() {
        return resourcePageServiceImpl;
    }

    public String getNetLogoCode(String name) {
        String fileName = "netlogocode/" + name + ".txt";
        return getNetLogoCodeWithFileName(fileName);
    }

    protected String getNetLogoCodeWithFileName(String fileName) {
        InputStream inputStream = 
                ServerUtils.getInputStreamFromResourceJar(
                        fileName, 
                        resourcePageServiceImpl.getClass(), 
                        resourcePageServiceImpl.getResourceArchiveFileName());
        if (inputStream == null) {
            System.err.println("Expected to find the following in the static resource archive: " + fileName);
            return null;
        }
        try {
            return new String(ServerUtils.streamToBytes(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isOnlyForFetchingCode() {
        return onlyForFetchingCode;
    }

    public void setOnlyForFetchingCode(boolean onlyForFetchingCode) {
        this.onlyForFetchingCode = onlyForFetchingCode;
    }
    public ArrayList<MacroBehaviour> getMacroBehaviours() {
        return macroBehaviours;
    }
    public int getMinPzcor() {
        return minPzcor;
    }
    public void setMinPzcor(int minPzcor) {
        this.minPzcor = minPzcor;
    }
    public int getMaxPzcor() {
        return maxPzcor;
    }
    public void setMaxPzcor(int maxPzcor) {
        this.maxPzcor = maxPzcor;
    }
    public int getDimensions() {
        return dimensions;
    }
    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }
    public void addDeclaration(String declaration) {
        declarations += declaration + "\n";
    }
    public void extensionUsed(String extensionName) {
        if (!extensions.contains(extensionName)) {
            extensions += extensionName + " ";
        }
    }
    public boolean isTickBasedUpdates() {
        return tickBasedUpdates;
    }
    public void setTickBasedUpdates(boolean tickBasedUpdates) {
        this.tickBasedUpdates = tickBasedUpdates;
    }
    public double getFrameRate() {
        return frameRate;
    }
    public void setFrameRate(double frameRate) {
        this.frameRate = frameRate;
    }
    public boolean isShowTickCounter() {
        return ShowTickCounter;
    }
    public void setShowTickCounter(boolean ShowTickCounter) {
        this.ShowTickCounter = ShowTickCounter;
    }
    public String getTickLabel() {
        return tickLabel;
    }
    public void setTickLabel(String tickLabel) {
        this.tickLabel = tickLabel;
    }
    public void associateCommentWithGlobal(String variableName, String comment, String interfaceType) {
        if (interfaceType == null) {
            if (comment != null && !comment.isEmpty()) {
                globalVariableComments.put(variableName, comment);
            }
        } else {
            commentsAboutglobalsWithInterface += ";; " + variableName + " is a global variable control by a " + interfaceType +"\n";
            if (comment != null && !comment.isEmpty()) {
                commentsAboutglobalsWithInterface += ";" + comment + "\n";
            }
        }	
    }

}
