package uk.ac.lkl.client;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.shared.CommonUtils;
import uk.ac.lkl.shared.NetLogoTokenizer;

public class Utils {
    
    public static final String MODELLER_LOGGER = "Modeller Logger";
    
    public static String encode(String s) {
	// could do uuencode
	StringBuilder buffer = new StringBuilder();
	for (int i = 0; i < s.length(); i++) {
	    int c = s.charAt(i);
	    int topHalf = c >> 4;
	    int bottomHalf = c & 0xF;
	    buffer.append((char) ('a' + topHalf));
	    buffer.append((char) ('a' + bottomHalf));
	}
	return buffer.toString();
    }

    public static String decode(String s) {
	// could do uudecode
	if (s == null) return null;
	StringBuilder buffer = new StringBuilder();
	for (int i = 0; i < s.length(); i += 2) {
	    int topHalf = s.charAt(i) - 'a';
	    int bottomHalf = s.charAt(i + 1) - 'a';
	    char c = (char) (topHalf << 4 | bottomHalf);
	    buffer.append(c);
	}
	return buffer.toString();
    }
    
    public static void positionPopupMenu(int mouseX, int mouseY, PopupPanel popupMenu) {
	int menuWidth = popupMenu.getOffsetWidth();
	int menuHeight = popupMenu.getOffsetHeight();
	int scrollLeft = Window.getScrollLeft();
	int scrollTop = Window.getScrollTop();
	int windowWidth = Window.getClientWidth();
	int windowHeight = Window.getClientHeight();
	// center the menu horizontally around the mouse
	int menuX = mouseX-menuWidth/2;
	if (menuX < 0) {
	    // would go off the left edge
	    menuX = 0;
	} else if (menuX+menuWidth > windowWidth) {
	    // would go off the right edge
	    menuX = windowWidth-menuX;
	}
	int menuY = mouseY;
	if (menuY + menuHeight > windowHeight) {
	    // would go off the bottom
	    menuY = windowHeight-menuHeight;
	}
	popupMenu.setPopupPosition(menuX+scrollLeft, menuY+scrollTop);
    }

    public static int firstUnusedTemplateName(String mathML, int start) {
	// searches for OTHER1, then OTHER2, etc. until it finds out that isn't
	// in mathML
	for (int i = start; i < Integer.MAX_VALUE; i++) {
	    String name = "OTHER" + i;
	    if (mathML.indexOf(name) < 0)
		return i;
	}
	return Integer.MAX_VALUE;
    }

    public static String niceNameForBinaryOperator(String operator) {
	if (operator.equals("divide")) {
	    return Modeller.constants.dividedBy();
	} else if (operator.equals("times")) {
	    return Modeller.constants.multipliedBy();
	} else if (operator.equals("rem")) {
	    return Modeller.constants.remainder();
	} else if (operator.equals("plus")) {
	    return Modeller.constants.plus();
	} else if (operator.equals("minus")) {
	    return Modeller.constants.minus();
	} else if (operator.equals("eq")) {
	    return Modeller.constants.equals();
	} else if (operator.equals("power")) {
	    return Modeller.constants.raisedTo();
	} else if (operator.equals("lt")) {
	    return Modeller.constants.isLessThan();
	} else if (operator.equals("leq")) {
	    return Modeller.constants.isLessThanOrEqualTo();
	} else if (operator.equals("gt")) {
	    return Modeller.constants.isGreaterThan();
	} else if (operator.equals("geq")) {
	    return Modeller.constants.isGreaterThanOrEqualTo();
	} else if (operator.equals("eq")) {
	    return Modeller.constants.isEqualTo();
	} else if (operator.equals("neq")) {
	    return Modeller.constants.isNotEqualTo();
	} else if (operator.equals("and")) {
	    return Modeller.constants.and();
	} else if (operator.equals("or")) {
	    return Modeller.constants.or();
	} else {
	    return null;
	}
    }
    
    public static String ordinal(String digit) {
	if (digit.equals("1")) {
	    return Modeller.constants.first();
	} else if (digit.equals("2")) {
	    return Modeller.constants.second();
	} else	if (digit.equals("3")) {
	    return Modeller.constants.third();
	} else {
	    return digit + Modeller.constants.th();
	}
    }
    
    public static String niceNameForUnaryOperator(String operator) {
	if (operator.equals("sin")) {
	    return Modeller.constants.theSineOf();
	} else if (operator.equals("cos")) {
	    return Modeller.constants.theCosineOf();
	} else if (operator.equals("tan")) {
	    return Modeller.constants.theTangentOf();
	} else if (operator.equals("asin")) {
	    return Modeller.constants.theArcSineOf();
	} else if (operator.equals("acos")) {
	    return Modeller.constants.theArcCosineOf();
	} else if (operator.equals("atan")) {
	    return Modeller.constants.theArcTangentOf();
	} else if (operator.equals("abs")) {
	    return Modeller.constants.theAbsoluteValueOf();
	} else if (operator.equals("not")) {
	    return Modeller.constants.not();
	} else {
	    return null;
	}
    }

    public static String algebraicNameForMathMLOperator(String operator) {
	if (operator.equals("divide")) {
	    return "&divide";
	} else if (operator.equals("times")) {
	    return "&times";
	} else if (operator.equals("plus")) {
	    return "+";
	} else if (operator.equals("minus")) {
	    return "-";
	} else if (operator.equals("eq")) {
	    return "=";
	} else {
	    return null;
	}
    }

    public static String niceNameForAttribute(String attribute) {
	// add pen stuff later
	if (attribute.charAt(0) == 'x') {
	    if (attribute.equals("x")) {
		return Modeller.constants.horizontalPosition();
	    } else {
		String angle = attribute.substring(1);
		try {
		    Integer.parseInt(angle);
		    return Modeller.constants.horizontalPositionOfThePointOnThePerimeterReachedByARayAt()
			    + angle + " " + Modeller.constants.degreesFromTheCentre();
		} catch (NumberFormatException e) {
		    return null; // no nice name
		}
	    }
	} else if (attribute.charAt(0) == 'y') {
	    if (attribute.equals("y")) {
		return Modeller.constants.verticalPosition();
	    } else {
		String angle = attribute.substring(1);
		try {
		    Integer.parseInt(angle);
		    return Modeller.constants.verticalPositionOfThePointOnThePerimeterReachedByARayAt()
			    + angle + " " + Modeller.constants.degreesFromTheCentre();
		} catch (NumberFormatException e) {
		    return null; // no nice name
		}
	    }
	} else if (attribute.equals("redColour")) {
	    return "<font COLOR='#FF0000'>" + Modeller.constants.percentOfRed() + "</font>";
	} else if (attribute.equals("greenColour")) {
	    return "<font COLOR='#00BB00'>" + Modeller.constants.percentOfGreen() + "</font>";
	    // darker green since it looks better
	} else if (attribute.equals("blueColour")) {
	    return "<font COLOR='#0000FF'>" + Modeller.constants.percentOfBlue() + "</font>";
	} else if (attribute.equals("rotation")) {
	    return Modeller.constants.angleOfRotation();
	} else if (attribute.equals("width")) {
	    return Modeller.constants.width();
	} else if (attribute.equals("height")) {
	    return Modeller.constants.height();
	} else if (attribute.equals("transparency")) {
	    return Modeller.constants.transparency();
	} else if (attribute.equals("appearance")) {
	    return Modeller.constants.costume();
	} else if (attribute.equals("penDown")) {
	    return Modeller.constants.leavingATrail();
	} else if (attribute.equals("thicknessPen")) {
	    return Modeller.constants.widthOfMyTrail();
	} else if (attribute.equals("redColourPen")) {
	    return Modeller.constants.percentageOfRedInMyTrail();
	} else if (attribute.equals("greenColourPen")) {
	    return Modeller.constants.percentageOfGreenInMyTrail();
	} else if (attribute.equals("blueColourPen")) {
	    return Modeller.constants.percentageOfBlueInMyTrail();
	} else if (attribute.equals("transparencyPen")) {
	    return Modeller.constants.thePercentageOfTransparencyOfMyTrail();
	} else if (attribute.equals("keyDown")) {
	    return Modeller.constants.keyDown();
        // following are really "user" attributes so should be user table driven
	} else if (attribute.equals("Vx")) {
	    return Modeller.constants.horizontalVelocity();
	} else if (attribute.equals("Vy")) {
	    return Modeller.constants.verticalVelocity();
	} else if (attribute.equals("Ax")) {
	    return Modeller.constants.horizontalAcceleration();
	} else if (attribute.equals("Ay")) {
	    return Modeller.constants.verticalAcceleration();
	} else if (attribute.equals("amIHittingASide")) {
	    return Modeller.constants.hittingASide();	
	} else if (attribute.equals("amIHittingGround")) {
	    return Modeller.constants.hittingTheGround();
	} else if (attribute.equals("horizontalDistanceWithOther")) {
	    return Modeller.constants.horizontalDistanceToOther();
	} else if (attribute.equals("verticalDistanceWithOther")) {
	    return Modeller.constants.verticalDistanceToOther();
	} else if (attribute.equals("isOverlappingHorizontallyWithOther")) {
	    return Modeller.constants.horizontallyOverlappingWithOther();
	} else if (attribute.equals("isOverlappingVerticallyWithOther")) {
	    return Modeller.constants.verticallyOverlappingWithOther();
	} else if (attribute.equals("delta47")) {
	    return Modeller.constants.delta47();
	} else if (attribute.equals("delta53")) {
	    return Modeller.constants.delta53();
	} else if (attribute.equals("delta59")) {
	    return Modeller.constants.delta59();
	} else {
	    return null;
	}
    }
   
    public static String textFontToMatchIcons(String html) {
	return Modeller.instance().textFontToMatchIcons(html);
    }
   
    public static String keepHTMLOnSameLine(String html) {
	return "<div style='float:left'>"
    	       + html.replaceFirst("<P>","").replaceFirst("</P>","") + 
    	       "</div>";
    }

    public static String replaceHTMLBody(String newBody, String html) {
	if (html != null) {
	    int bodyStart = html.indexOf("<body");
	    if (bodyStart < 0) {
		bodyStart = html.indexOf("<BODY");
	    }
	    if (bodyStart >= 0) {
		int bodyTagEnd = html.indexOf(">", bodyStart);
		if (bodyTagEnd >= 0) {
		    int endBodyTagStart = html.indexOf("</body", bodyTagEnd);
		    if (endBodyTagStart < 0) {
			endBodyTagStart = html.indexOf("</BODY");
		    }
		    if (endBodyTagStart >= 0) {
			return html.substring(0, bodyTagEnd + 1) + newBody + html.substring(endBodyTagStart);
		    }
		}
	    }
	}
	return "<html><body>" + newBody + "</body></html>";
    }
    
    public static String getTimeDifference(Date time1, Date time2) {
	long milliseconds = time1.getTime() - time2.getTime();
	long seconds = milliseconds/1000;
	if (seconds == 1) {
	    return seconds + " " + Modeller.constants.secondTimeUnit();
	}
	if (seconds < 60) {
	    return seconds + " " + Modeller.constants.seconds();
	}
	long minutes = seconds/60;
	if (minutes == 1) {
	    return minutes + " " + Modeller.constants.minute();
	}
	if (minutes < 60) {
	    return minutes + " " + Modeller.constants.minutes();
	}
	long hours = minutes/60;
	if (hours == 1) {
	    return hours + " " + Modeller.constants.hour();
	}
	if (hours < 60) {
	    return hours + " " + Modeller.constants.hours();
	}
	long days = hours/24;
	if (days == 1) {
	    return days + " " + Modeller.constants.day();
	}
	return days + " " + Modeller.constants.days();
    }
        
    public static HTML goodTabWidget(String name) {
//	int length = name.length();
//	int maximumTabLabelLength = ClosableTab.getMaximumTabLabelLength();
//	if (length > maximumTabLabelLength) {
//	    String newName = name.substring(0, maximumTabLabelLength/2) + "..." + name.substring(length-maximumTabLabelLength/2);
//	    HTML html = new HTML(newName);
//	    html.setTitle(name);
//	    return html;	    
//	} else {
	// used to call URL.decodeQueryString but if name contained a % or the like caused 
	// JavaScript failures
	    return new HTML(name);
//	}
    }
    
    public static HTML goodTabWidgetFromURL(String url) {
	return new HTML(goodTabNameFromURL(url));
    }

    public static String goodTabNameFromURL(String url) {
	return URL.decodeQueryString(CommonUtils.getFileName(url));
    }
    
    public static void replaceElementWithWidget(String id, Element element, Widget widget, HTMLPanel htmlPanel) {
	if (htmlPanel != null) {
	    try {
		htmlPanel.addAndReplaceElement(widget, id);
		Element newElement = widget.getElement();
		newElement.setAttribute("id", id);
		// triggers an exception
		// not needed for DEFINE-PARAMETER.html anymore
//		String onchangeAttribute = element.getAttribute("onchange");
//		if (!onchangeAttribute.isEmpty()) {
//		    widget.getElement().setAttribute("onchange", onchangeAttribute);
//		}
	    } catch (Exception e) {
		e.printStackTrace();
		Modeller.addToErrorLog(e.toString() + "; " + Modeller.constants.errorAddingTheFollowingToAWebPage() + widget.toString());
	    }
	} else {
	    Modeller.addToErrorLog("browsePanel has no PanelHTML");
	}
    }
    
    public static VerticalPanel wrapForGoodSize(Widget widget) {
	// if widget is a button then this causes it to appear a good size
	// otherwise it can appear very wide
	VerticalPanel wrapper = new VerticalPanel();
	wrapper.setStylePrimaryName("modeller-wrapper");
 	wrapper.add(widget);
 	return wrapper;
    }
    
    public static String fullURL(String shortURL) {
	return CommonUtils.joinPaths(GWT.getHostPageBaseURL(), shortURL);
    }
    
    public static String getNameFromElement(com.google.gwt.xml.client.Element element) {
	// shouldn't the following use getFirstNodeWithTagName
	String name = getElementString("name", element);
	if (name == null) {
	    // could be older convention
	    String encodedName = element.getAttribute("name");
	    if (encodedName != null) {
		name = CommonUtils.decode(encodedName);
	    } else {
		Modeller.addToErrorLog("Expected macro-behaviour to have a name attribute: " + element.toString());
	    }
	}
	return name;
    }

    public static String getElementString(String tagName, com.google.gwt.xml.client.Element element) {
	// note this is using the GWT XML Element not the DOM one
	com.google.gwt.xml.client.NodeList nameElements = element.getElementsByTagName(tagName);
	if (nameElements.getLength() > 0) {
	    com.google.gwt.xml.client.Node nameElement = nameElements.item(0);
	    return nameElement.getFirstChild().getNodeValue();		
	}
	// sometimes called just to check if there is such an element so no need to warn here
	return null;
    }
    
    public static Node getFirstNodeWithTagName(Node node, String tag) {
	// doesn't explore the entire tree as getElementsByTagName does
	// returns the first one found or null
	if (node == null) {
	    return null;
	}
	com.google.gwt.xml.client.NodeList childNodes = node.getChildNodes();
	int length = childNodes.getLength();
	for (int i = 0; i < length; i++) {
	    com.google.gwt.xml.client.Node childNode = childNodes.item(i);
	    if (tag.equalsIgnoreCase(childNode.getNodeName())) {
		return childNode;
	    }
	}
	return null;
    }
    
    public static Integer getIntegerAttribute(com.google.gwt.xml.client.Element element, String attribute) {
	String stringValue = element.getAttribute(attribute);
	if (stringValue == null || stringValue.isEmpty()) {
	    return null;
	}
	try {
	    return Integer.parseInt(stringValue);
	} catch (Exception e) {
	    e.printStackTrace();
	    Modeller.addToErrorLog("Error while parsing integer valued attribute: " + stringValue +
	                           " " + e.toString());
	    return null;
	}
    }
    
    public static Integer getIntegerAttribute(Element element, String attribute) {
	// HTML as well as XML version
	String stringValue = element.getAttribute(attribute);
	if (stringValue == null || stringValue.isEmpty()) {
	    return null;
	}
	try {
	    return Integer.parseInt(stringValue);
	} catch (Exception e) {
	    e.printStackTrace();
	    Modeller.addToErrorLog("Error while parsing integer valued attribute: " + stringValue +
	                           " " + e.toString());
	    return null;
	}
    }
    
    public static int getIntAttribute(com.google.gwt.xml.client.Element eventElement, String attribute, int defaultValue) {
	String versionString = eventElement.getAttribute(attribute);
	if (versionString != null) {
	    try {
		return Integer.parseInt(versionString);
	    } catch (NumberFormatException e) {
		return defaultValue; // warn?
	    }
	}
	return defaultValue; 
    }
    
    public static String getLocationParameter(String key) {
	String parameter = Window.Location.getParameter(key);
	if (parameter == null) { 
	    return null;
	}
	int sharpIndex = parameter.indexOf('#');
	if (sharpIndex < 0) {
	    return parameter;
	}
	return parameter.substring(0, sharpIndex);
    }
    
    static public boolean urlAttributeNotZero(String attributeName, boolean currentValue) {
	String option = Window.Location.getParameter(attributeName);
	if (option != null) {
	    return !option.equals("0");
	} else {
	    return currentValue;
	}
    }   

//    public int getAvailableWidth() {
//	if (Modeller.isSplitHorizontally()) {
//	    return Window.getClientWidth()/2-Modeller.windowWidthMargins;
//	} else {
//	    return Window.getClientWidth()-Modeller.windowWidthMargins;
//	}
//    }
//    
//    public int getAvailableHeight() {
//	if (Modeller.isSplitVertically()) {
//	    return Window.getClientHeight()-Modeller.windowHeightMargins;
//	} else {
//	    return Window.getClientHeight()-Modeller.windowHeightMargins;
//	}
//    }
    
    public static int getFullWidth() {
	return Window.getClientWidth()-Modeller.windowWidthMargins;
    }
    
    public static int getFullHeight() {
	return Window.getClientHeight()-Modeller.windowHeightMargins;
    }
    
    /**
     * @param url
     * @return the value of the tab parameter decoded
     * or the url if there is no tab parameter
     */
    public static String urlCheckingTabAttribute(String url) {
	String tabAttribute = CommonUtils.extractTabAttribute(url);
	if (tabAttribute == null) {
	    return url;
	} else {
	    return URL.decodeQueryString(tabAttribute);
	}
    }

    public static void logServerMessage(final Level level, final String message) {
	if (message == null || message.isEmpty()) {
	    return;
	}
	final AsyncCallbackNetworkFailureCapable<String> callback = new AsyncCallbackNetworkFailureCapable<String>();
	callback.setAndRunTimer(new TimerInSequence() {

	    @Override
	    public void run() {
		logServerMessage(level, message, callback);
	    }
	    
	});
    }
    
    public static void logServerMessage(final Level level, final String message, final AsyncCallback<String> logMessageCallback) {
	// workaround for a GWT bug in Level.getName()
	// see http://code.google.com/p/google-web-toolkit/issues/detail?id=6545
	String levelName;
	if (level == Level.INFO) {
	    levelName = "INFO";
	} else if (level == Level.WARNING) {
	    levelName = "WARNING";
	} else {
	    levelName = "SEVERE";
	}
        Modeller.getResourcePageService().logMessage(levelName, message + "; Session id: " + Modeller.sessionGuid, logMessageCallback);
	// and report it for development
	Logger.getLogger(MODELLER_LOGGER).log(level, message);
    }

    public static HTML indentHTML(int depth) {
	StringBuffer stringBuffer = new StringBuffer();
	for (int i = 0; i < depth; i++) {
	    stringBuffer.append("&nbsp;&nbsp;");
	}
	return new HTML(stringBuffer.toString());
    }
    
    public static CssColor createColor(int red, int green, int blue, double alpha) {
	return CssColor.make("rgba(" + red + ", " + green + "," + blue + ", " + alpha + ")");
    }

    public static String addLinksToDocumentation(String code) {
        // finds NetLogo commands and reporters and links to documentation
	if (code == null) {
	    return null;
	}
        NetLogoTokenizer tokenizer = new NetLogoTokenizer(code);
        String token;
        String previousToken = null;
        // Can't put links into a text area so skip them
//        boolean previousTokenIsTextAreaElement = false;
        while ((token = tokenizer.nextToken(true)) != null) {
            if (token.equals("add-button") || token.equals("add-netlogo-button")) {
        	// add-button isn't a real NetLogo primitive and uses single quotes to identify arguments
        	// so the tokenizer doesn't process it sensibly
        	return code;
            }
            String replacement = null;
//            if (previousTokenIsTextAreaElement) {
//        	// skip it until </textarea>
//            } else 
            if (netLogoPrimitives.contains(token)) {
        	String anchorName = token;
        	if (token.endsWith("?")) {
        	    // anchor doesn't include question mark
        	    anchorName = token.substring(0, token.length()-1);
        	} else if (token.contains("objects")) {
        	    anchorName = token.replace("objects", "breeds");
        	} else if (token.contains("object")) {
        	    anchorName = token.replace("object", "breeds");
        	}
        	replacement =
        	    "<a href='http://ccl.northwestern.edu/netlogo/docs/dictionary.html#" + anchorName + "' target='_blank' class='modeller-code-primitive'>" + 
        	    token + "</a>";
            } else if (netLogoArithmeticOperators.contains(token)) {
        	replacement =
            	    "<a href='http://ccl.northwestern.edu/netlogo/docs/dictionary.html#Symbols' target='_blank' class='modeller-code-primitive'>" + 
            	    token + "</a>";
            } else if (behaviourComposerPrimitives.contains(token)) {
        	replacement = 
        	    "<a href='http://resources.modelling4all.org/guides/dictionary#TOC-" + token +"' target='_blank' class='modeller-code-primitive'>" +
        	    token + "</a>";
            } else if (CommonUtils.NETLOGO_PRIMITIVES_3D.contains(token)) {
        	replacement =
        	    "<a href='http://ccl.northwestern.edu/netlogo/docs/3d.html#" + token + "' target='_blank' class='modeller-code-primitive'>" + 
        	    token + "</a>";
            } else if (token.startsWith(";") || token.startsWith("<I>;") || token.startsWith("<i>;")) {
        	// TODO: make this more general
        	if (previousToken == null || !(previousToken.startsWith("<textarea") && !previousToken.endsWith("</textarea>"))) {
        	    // don't add this CSS if inside a text area
        	    replacement = "<span class='modeller-netlogo-comment'>" + token + "</span>";
        	}
            }
            if (replacement != null) {
        	tokenizer.replaceCurrentToken(replacement);
            }
            previousToken = token;
//            String lowerCaseToken = token.toLowerCase();
//            if (lowerCaseToken.startsWith("<textarea")) {
//        	previousTokenIsTextAreaElement = true;
//            } else if (lowerCaseToken.startsWith("</textarea")) {
//        	previousTokenIsTextAreaElement = false;
//            }
        }
        return "<pre>" + tokenizer.getCode().trim() + "</pre>";
    }

    static public List<String> behaviourComposerPrimitives = 
	    Arrays.asList(
    	"do-every", "do-at-setup", "do-after-setup", "when", "whenever", "do-after", "do-at-time", "do-now", 
    	"do-for-n", "select-n", 
    	"do-repeatedly", "do-with-probability", "do-with-probabilities", 
    	"add-behaviour", "add-behaviours", "add-behaviours-to", "add-behaviour-to", 
    	"add-link-behaviour", "add-link-behaviours", "add-link-behaviour-after", "add-link-behaviours-after",
    	"remove-behaviours-from", "remove-all-behaviours", "remove-all-behaviours-from",
    	"can-pick-one", "any", "anyone-who-is", "all-who-are", "the-other", "ask-every-patch", 
    	"all-individuals", "all-of-kind", "all-others", "any-of-kind",
        "random-integer-between", "random-number-between",
    	"go-forward", "turn-right", "turn-left",
    	"within-range", "heading-towards-another",
    	"move-horizontally-or-vertically-towards-another",
    	"second", "third", "fourth",
    	"random-unoccupied-location",
    	"canonical-heading", "canonical-distance",
    	"log-log-histogram",
    	"power-law-random", "power-law-list", "power-law-list-with-mean");
    
    static public List<String> netLogoPrimitives = 
	    Arrays.asList(
    	"num-e", "pi", "boolconstants", "false", "true", "color-constants", "colorblack", "colorgray", 
    	"colorwhite", "colorred", "colororange", "colorbrown", "coloryellow", "colorgreen", "colorlime",
    	"colorturquoise", "colorcyan", "colorsky", "colorblue", "colorviolet", "colormagenta", "colorpink",
    	"acos", "all?", "and", "any?", "approximate-hsb", "approximate-rgb", "Symbols", "symboltimes",
    	"symbolminus", "symboldiv", "symbolexp", "symbollt", "symbolgt", "symbolequal", "symbolnotequal",
    	"symbolltequal", "symbolgtequal", "asin", "ask", "ask-concurrent", "at-points", "atan", 
    	"autoplot?", "auto-plot-off", "auto-plot-on", "back", "bk", "base-colors", "beep",
    	"behaviorspace-run-number", "both-ends", "breed", "but-first", "butfirst", "bf",
    	"but-last", "butlast", "bl", "can-move?", "carefully", "ceiling", "clear-all", "ca",
    	"clear-all-plots", "clear-drawing", "cd", "clear-links", "clear-output", "clear-patches", 
    	"cp", "clear-plot", "clear-turtles", "ct", "color", "cos", "count", "create-ordered-turtles",
    	"create-ordered-objects", "cro", "create-link-to", "create-links-to",
    	"create-link-from", "create-links-from", "create-link-with", "create-links-with", "create-turtles",
    	"crt", "create-objects", "create-temporary-plot-pen", "date-and-time", "die", "diffuse", "diffuse4", 
    	"directed-link-breed", "display", "distance", "distancexy", "downhill", "downhill4", "dx", "dy",
    	"empty?", "end", "end1", "end2", "error-message", "every", "exp", "export-view", "export-interface",
    	"export-output", "export-plot", "export-all-plots", "export-world", "extensions", "extract-hsb",
    	"extract-rgb", "face", "facexy", "file-at-end?", "file-close", "file-close-all", "file-delete",
    	"file-exists?", "file-flush", "file-open", "file-print", "file-read", "file-read-characters",
    	"file-read-line", "file-show", "file-type", "file-write", "filter", "first", "floor", "follow",
    	"follow-me", "foreach", "forward", "fd", "fput", "globals", "hatch", "hatch-objects", "heading",
    	"hidden?", "hide-link", "hide-turtle", "ht", "histogram", "home", "hsb", "hubnet-broadcast",
    	"hubnet-broadcast-clear-output", "hubnet-broadcast-message", "hubnet-broadcast-view",
    	"hubnet-clear-override", "hubnet-clear-overrides", "hubnet-enter-message?", "hubnet-exit-message?", 
    	"hubnet-fetch-message", "hubnet-message", "hubnet-message-source", "hubnet-message-tag",
    	"hubnet-message-waiting?", "hubnet-reset", "hubnet-reset-perspective", "hubnet-send", 
    	"hubnet-send-clear-output", "hubnet-send-follow", "hubnet-send-message", "hubnet-send-override", 
    	"hubnet-send-watch", "hubnet-set-client-interface", "if", "ifelse", "ifelse-value", "import-drawing",
    	"import-pcolors", "import-pcolors-rgb", "import-world", "in-cone", 
    	"in-link-neighbor?", "in-link-neighbors", "in-link-from",
    	"includes", "in-radius", "inspect", "int", "is-agent?", "is-agentset?", "is-boolean?", 
    	"is-object?", "is-objects?", "is-directed-link?", "is-link?", "is-link-set?", "is-list?", "is-number?", 
    	"is-patch?", "is-patch-set?", "is-object?", "is-string?", "is-turtle?", "is-turtle-set?",
    	"is-undirected-link?", "item", "jump", "label", "label-color", "last",
    	"layout-circle", "layout-magspring", "layout-radial", "layout-spring", 
    	"layout-tutte", "left", "lt", "length", "let", "link", "link-heading", 
    	"link-length", "link-set", "link-shapes", "links", "links-own", "link-breeds-own", 
    	"list", "ln", "log", "loop", "lput", "map", "max", "max-n-of", "max-one-of",
    	"max-pxcor", "max-pycor", "mean", "median", "member?", "min", "min-n-of", "min-one-of", 
    	"min-pxcor", "min-pycor", "mod", "modes", "mouse-down?", "mouse-inside?", "mouse-xcor", 
    	"mouse-ycor", "move-to", "movie-cancel", "movie-close", "movie-grab-view",
    	"movie-grab-interface", "movie-set-frame-rate", "movie-start", "movie-status", 
    	"my-links", "my-in-links", "my-out-links", 
    	"myself", "n-of", "n-values", "neighbors", "neighbors4", "link-neighbors", 
    	"link-neighbor?", "netlogo-applet?", "netlogo-version", "new-seed", 
    	"no-display", "nobody", "no-links", "no-patches", "not", "no-turtles", "of", "one-of",
    	"or", "other", "other-end", "out-link-neighbor?", 
    	"out-link-neighbors", "out-link-to", "output-print", "output-show",
    	"output-type", "output-write", "patch", "patch-ahead", "patch-at", "patch-at-heading-and-distance",
    	"patch-here", "patch-left-and-ahead", "patch-right-and-ahead", "patch-set", "patch-size",
    	"patches", "patches-own", "pcolor", "pen-down", "pd", "pen-erase", "pe", "pen-up", "pu",
    	"pen-mode", "pen-size", "plabel", "plabel-color", "plot", "plot-name", "plot-pen-exists?",
    	"plot-pen-down", "plot-pen-up", "plot-pen-reset", "plotxy", "plot-x-min", "plot-x-max",
    	"plot-y-min", "plot-y-max", "position", "precision", "print", "pxcor", "pycor", "random",
    	"random-float", "random-exponential", "random-gamma", "random-normal", "random-poisson",
    	"random-pxcor", "random-pycor", "random-seed", "random-xcor", "random-ycor", "read-from-string", 
    	"reduce", "remainder", "remove", "remove-duplicates", "remove-item", "repeat", "replace-item", 
    	"report", "reset-perspective", "rp", "reset-ticks", "reset-timer", "resize-world", "reverse",
    	"rgb", "ride", "ride-me", "right", "rt", "round", "run", "runresult", "scale-color", "self",
    	"sentence", "se", "set", "set-current-directory", "set-current-plot",
    	"set-current-plot-pen", "set-default-shape", "set-histogram-num-bars", "set-line-thickness",
    	"set-patch-size", "set-plot-pen-color", "set-plot-pen-interval", "set-plot-pen-mode",
    	"set-plot-x-range", "set-plot-y-range", "setxy", "shade-of?", "shape", "shapes", "show", 
    	"show-turtle", "st", "show-link", "shuffle", "sin", "size", "sort", "sort-by", "sprout", 
    	"sprout-objects", "sqrt", "stamp", "stamperase", "standard-deviation", "startup", "stop",
    	"subject", "sublist", "substring", "subtract-headings", "sum", "tan", "task", "thickness", "tick", 
    	"tick-advance", "ticks", "tie", "tie-mode", "timer", "to", "to-report", "towards", 
    	"towardsxy", "turtle", "turtle-set", "turtles", "turtles-at", "objects-at", "turtles-here",
    	"objects-here", "turtles-on", "objects-on", "turtles-own", "breeds-own", "type", 
    	"untie", "uphill", "uphill4", "user-directory", "user-file", "user-new-file", "user-input",
    	"user-message", "user-one-of", "user-yes-or-no?", "variance", "wait", "watch", 
    	"watch-me", "while", "who", "with", "link-with", "with-max", "with-min",
    	"with-local-randomness", "without-interruption", "word", "world-width", "world-height", 
    	"wrap-color", "write", "xcor", "xor", "ycor");
    
    static public List<String> netLogoArithmeticOperators = 
	    Arrays.asList("+", "*", "-", "/", "^", "<", ">", "=", "!=", "<=", ">=");
    /**
     * @param url
     * @return with parameters added for context (if known)
     */
    public static String addContextToLaunchURL(String url) {
        String contextId = Modeller.instance().getContextId();
        if (contextId != null) {
            url += "&contextId=" + contextId;
        }
        String givenName = Modeller.instance().getGivenName();
        if (givenName != null) {
            url += "&givenName=" + givenName;
        }
	String bc2NetLogoChannelToken = Modeller.instance().getNetLogo2BCChannelToken();
	if (bc2NetLogoChannelToken != null) {
	    // TODO: implement 'new' by contacting the server and getting the token and reloading
	    url += "&bc2NetLogoChannelToken=" + bc2NetLogoChannelToken +
		   "&netLogo2BCChannelToken=new" +
		   "&bc2NetLogoOriginalSessionGuid=" + Modeller.instance().getBc2NetLogoOriginalSessionGuid();
	}
	String gwtParameter = Window.Location.getParameter("gwt.codesvr");
	if (gwtParameter != null) {
	    url += "&gwt.codesvr=" + gwtParameter;
	}
	if (Modeller.forWebVersion) {
	    url += "&forWebVersion=1";
	}
        return url;
    }
    
    public static void setWidthAndSoleContents(Widget widget, String width) {
	widget.setWidth(width);
	if (widget instanceof HasOneWidget) {
	    setWidthAndSoleContents(((HasOneWidget) widget).getWidget(), width);
	}
    }
    
    public static void setHeightAndSoleContents(Widget widget, String height) {
	widget.setHeight(height);
	if (widget instanceof HasOneWidget) {
	    setHeightAndSoleContents(((HasOneWidget) widget).getWidget(), height);
	}
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> T getAncestorWidget(Widget widget, Class<T> c) {
	Widget ancestor = widget.getParent();
	while (ancestor != null) {
	    if (ancestor.getClass()== c) {
		return (T) ancestor;
	    }
	    ancestor = ancestor.getParent();
	}
	return null;
    }
    
    public static DecoratedPopupPanel popupMessage(String textOrHTML) {
	return popupMessage(textOrHTML, true);
    }
    
    /**
     * Displays textOrHTML in a centered pop up with auto hide.
     * 
     * @param textOrHTML
     * @return 
     */
    public static DecoratedPopupPanel popupMessage(String textOrHTML, boolean modal) {
        DecoratedPopupPanel popup = new DecoratedPopupPanel(true, modal);
        popup.setWidget(new HTML(textOrHTML));
        popup.show();
        popup.center();
        return popup;
    }
    
    public static Node parseXML(String xml) {
	try {
	    Document document = XMLParser.parse(xml);
	    NodeList nodes = document.getChildNodes();
	    if (nodes.getLength() == 0) {
		Modeller.addToErrorLog("Problem parsing this XML: '" + xml + "'");
		return null;
	    }
	    // document has a single child in XML
	    Node contents = nodes.item(0);
	    String tag = contents.getNodeName();
	    if (tag.equalsIgnoreCase("xml")) {
		contents = nodes.item(1);
	    }
	    return contents;
	} catch (Exception e) {
	    Modeller.reportException(e, "Problem parsing XML: '" + xml + "'");
	    return null;
	}
    }
}
