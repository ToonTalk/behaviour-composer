package uk.ac.lkl.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import uk.ac.lkl.client.Modeller;

import com.google.gwt.user.client.ui.HTML;

// utilities shared between client and server
// careful with any import of client code

public class CommonUtils {
        
    private static final String START_ALREADY_PROCESSED_LEAVE_ALONE = ";start-already-processed-leave-alone";
    private static final String END_ALREADY_PROCESSED_LEAVE_ALONE = ";end-already-processed-leave-alone";
    private static final String CHANGES_EQUAL = "changes=";
    final static String encodingTable = 
	"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"; 
    public static final String COMMON_BEHAVIOUR_COMPOSER_NLS = "bc_auxiliary_file_from_download_tab_version_21.nls";
    public static final String M4A_MODEL_URL_PARAMETER = "MforAllModel";
    public static final String OLD_M4A_MODEL_URL_PARAMETER = "M4AlModel";
    public static final String TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN = "top-level-macro-behaviour: ";
    // in some contexts the token is trimmed so easier to compare using the following
    public static final String TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN_TRIMMED = TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN.trim();
    public static final int TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN_LENGTH = TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN.length();
    // could try using Java resources to fetch this from a resource file instead.
    public static final String FIREFOX_RESIZING_APPLET_JAVASCRIPT = 
	    // not sure that this was only a bug in FireFox 2.0 but is OK when tested in FireFox 13 
	    // m4a-gae-hrd.appspot.com/m/?frozen=JLVsB0hY_zkVxB0-0pEX6c&MforAllModel=1
	    "<script type='text/javascript'>\n// FireFox and Safari don't display the applet unless it is made bigger\nif (navigator.userAgent.indexOf('Firefox/2.') >= 0 || navigator.userAgent.indexOf('Safari') >= 0) {\n   var appletElement = document.getElementsByTagName('applet')[0];\n   appletElement.width = (parseInt(appletElement.width)+90).toString();\n   appletElement.height = (parseInt(appletElement.height)+110).toString();\n}\n</script>";
    public static final String NOAPPLET = "<noapplet>Java applets must be enabled for this to run. See <a href='http://www.java.com'>www.java.com</a>.<br /></noapplet>";
    // following was an aborted attempt to fix the FireFox applet leak by using onunload to destroy the applet
    // but reading various posts and documentation not clear this will work
//  public static final String UNLOAD_APPLET_JAVASCRIPT = "<script type='text/javascript'>   </script>";
    private static final String BEGIN_DESCRIPTION = "Begin description:";
    private static final String END_DESCRIPTION = "End description";
    private static String hostBaseURL;
    private static String staticPagePath;
    private static String defaultInfoTabContents = null;
    public static final String MODELLER_ID_PREFIX = "_Modeller_ID_";
    private final static byte[] non_breaking_space_regular_expression_bytes = {'[', (byte) 194, (byte) 160, ']'};
    private static final String NON_BREAKING_SPACE_REGULAR_EXPRESSION = new String(non_breaking_space_regular_expression_bytes);
    
    public static final String BEFORE_CODE_ELEMENT = "before_code_element";
    public static final String AFTER_CODE_ELEMENT = "after_code_element";

    public static final String DEFAULT_APPLET_TEMPLATE = "en/model_applet.html";
    public static final String TOKEN_FOR_REMOVED_TEXT_AREA = "***text_area_removed***";

    final public static String SUBSTITUTE_TEXT_AREA_FOR = "substitute-text-area-for";
    
    public static final String EDITED_HTML = ".edited.html";
    
    public static final String MICRO_BEHAVIOUR_UPDATES = "microBehaviourUpdates";
    public static final String UPDATE_URL = "updateURL: ";
    
    public static List<String> NETLOGO_PRIMITIVES_3D = 
	    Arrays.asList(
		    "distancexyz", "distancexyz-nowrap", "dz", "patch-at-heading-pitch-and-distance", "tilt-down", "tilt-up", "roll-left", "roll-right", "setxyz", 
		    "towards-pitch", "towards-pitch-nowrap", "towards-pitch-xyz", "towards-pitch-xyz-nowrap", "neighbors6", "max-pzcor", "min-pzcor", "random-pzcor",
		    "random-zcor", "world-depth", "load-shapes-3d", "facexyz", "orbit-down", "orbit-left", "orbit-right", "orbit-up", "oxcor", "oycor", "ozcor",
		    "zcor", "zoom", "link-pitch" );
    
    public static List<String> NETLOGO_TURTLE_VARIABLES_3D = 
	    Arrays.asList( "my-z", "zcor", "pitch", "roll" ); 
    
    public static List<String> NETLOGO_PATCH_VARIABLES_3D = 
	    Arrays.asList( "pzcor" );
    
    public static final String NETLOGO_SECTION_SEPARATOR = "@#$#@#$#@";
    public static final String NETLOGO_SECTION_SEPARATOR_AND_NEW_LINE = "@#$#@#$#@\n";
    
    static public String getInnerText(String html) {
	// removes everything between < and > at any level
	// E.g. <pre>foo<br><p>bar</p></pre> returns
	// foo bar
	// really find stuff between > and <
	// note that removeHTMLMarkup calls this and also
	// translates &nbsp; and the like
	StringBuilder text = new StringBuilder();
	int firstStart = html.indexOf('<');
	if (firstStart < 0) {
	    return html; // there were no tags
	}
	int endTag = html.indexOf('>', firstStart);
	if (firstStart > 0) {
	    text.append(html.substring(0, firstStart));
	    String tagName = html.substring(firstStart+1, endTag);
	    if (!tagName.equalsIgnoreCase("sub") && !tagName.equalsIgnoreCase("sup")) {
		// super and sub script are ignored rather than generate a space
		text.append(' ');
	    }
	}
	int start = 0;
	do {
	    int startTag = html.indexOf('<', endTag);
	    if (startTag < 0) {
		text.append(html.substring(endTag + 1));
//		System.out.println(html + " doesn't have matching tags.");
		break;
	    }
	    text.append(html.substring(endTag + 1, startTag));
	    String tagName = html.substring(firstStart+1, endTag);
	    if (!tagName.equalsIgnoreCase("sub") && !tagName.equalsIgnoreCase("sup")) {
		text.append(' ');
	    }
	    start = startTag;
	} while ((endTag = html.indexOf('>', start)) >= 0);
	return text.toString();
    }  

    private static String removeHTMLMarkupIfAny(String code) {
	if (containsHTMLMarkup(code)) {
	    return removeHTMLMarkup(code);
	} else {
	    return code;
	}
    }
    
    private static boolean containsHTMLMarkup(String code) {
	// if starts with < followed by some letter and the count of < equals the count of > assume it is markup
	// may start with some closing elements eg. </span>
	return code != null && 
	       code.length() > 2 && 
	       code.charAt(0) == '<' && 
	       (Character.isLetter(code.charAt(1)) || code.charAt(1) == '/') &&
	       countCharacterOccurrences(code, '<') == countCharacterOccurrences(code, '>');
    }

    private static Object countCharacterOccurrences(String string, char c) {
	int result = 0;
	int length = string.length();
	for (int i = 0; i < length; i++) {
	    if (string.charAt(i) == c) {
		result++;
	    }
	}
	return result;
    }

    static public String removeHTMLMarkup(String code) {
	if (code == null) {
	    return code;
	} else {
	    String innerText = getInnerText(code);
	    // now replace quotes, >, and <
	    return removeHTMLTokens(innerText);
	}
    }

    public static String removeHTMLTokens(String code) {
	return CommonUtils.replaceNonBreakingSpaces(
		code.replaceAll("&nbsp;", " ")
	            .replaceAll("&quot;", "\"")
	            .replaceAll("&lt;", "<")
	            .replaceAll("&gt;", ">")
	            .replaceAll("\r","\r\n")
	            .replaceAll("&amp;", "&")
	            // replace non-breaking spaces with spaces
	            //	           .replace((char) 160, (char) 32) // happens on the Mac when images are part of the HTML
	            .replaceAll("\r\n\r\n","\r\n")) // remove blank lines
	            .trim();
    }

    public static String toHexString(int i) {
        String hex = Integer.toHexString(i);
        if (hex.length() == 1) {
            return "0" + hex;
        } else {
            return hex;
        }
    }

    static public String getBaseURL(String url) {
	if (url == null) return null;
        url = removeAttributes(url);
        url = removeBookmark(url);
        int colonSlashSlash = url.indexOf("://");
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash >= 0 && (colonSlashSlash < 0 || lastSlash > colonSlashSlash + "://".length())) {
            int dotAfterSlash = url.indexOf('.', lastSlash);
            if (dotAfterSlash < 0) {
        	return url;
            } else {
        	return url.substring(0, lastSlash);
        	// doesn't include last slash 
            }
        } else {
            return url;
        }
    }
    
    static public String getDomainURL(String url) {
	if (url == null) {
	    return null;
	}
        int colonSlashSlash = url.indexOf("://");
        if (colonSlashSlash < 0) {
            return "";
        } else {
            int nextSlash = url.indexOf('/', colonSlashSlash+3);
            if (nextSlash < 0) {
        	return "";
            } else {
        	return url.substring(0, nextSlash);
            }
        }
    }

    public static String removeBookmark(String url) {
	int sharp = url.indexOf('#');
        if (sharp >= 0) {
            return url.substring(0, sharp);
        }
	return url;
    }
    
    private static String removeAttributes(String url) {
	if (url == null) {
	    return null;
	}
	int questionMark = url.indexOf('?');
        if (questionMark >= 0) {
            return url.substring(0, questionMark);
        }
	return url;
    }
    
    static public boolean isAbsoluteURL(String url) {
	return url != null && url.indexOf("://") >= 0;
    }
    
    public static String cannonicaliseURL(String url) {
	// tries best to avoid have two different cannonicalised urls 
	// refer to the same page
	// puts the domain name in lower case
	// and removes bookmarks
        int colonSlashSlash = url.indexOf("://");
        if (colonSlashSlash < 0) {
            return url;
        }
        int slash = url.indexOf('/', colonSlashSlash+3);
        if (slash < 0) {
            return url;
        }
	StringBuffer newURL = new StringBuffer();
        newURL.append(url.substring(0, slash).toLowerCase()); 
        int hashIndex = url.indexOf("#", slash);
        if (hashIndex < 0 || url.startsWith(CHANGES_EQUAL, hashIndex+1)) {
            // don't remove bookmark if it indicates text area updates
            newURL.append(url.substring(slash));
        } else if  (url.startsWith(CHANGES_EQUAL, hashIndex+2)) {
            // due Issue 930 there may be URLs which have &# -- this fixes them
            newURL.append(url.substring(slash).replace("#&", "#"));
        } else {
            newURL.append(url.substring(slash, hashIndex));
        }
        int elementStartIndex = newURL.indexOf("<");
        if (elementStartIndex >= 0) {
            // if HTML remains in URL then remove < so that error messages make more sense
            newURL.replace(elementStartIndex, elementStartIndex+1, "&lt;");
        }
	return newURL.toString();
    }

    public static String getFileName(String url) {
	if (url == null) {
	    return null;
	}
	url = removeAttributes(url);
	url = removeBookmark(url);
        int lastSlash = url.lastIndexOf('/');
        int hasDot = url.indexOf('.', lastSlash);
        if (lastSlash >= 0) {
            if (lastSlash == url.length()) { 
        	// final slash
        	int previousSlash = url.lastIndexOf('/', lastSlash-1);
        	return url.substring(previousSlash+1, lastSlash);
            }
            if (hasDot < 0) {
        	// there is no dot after the last slash
        	return url.substring(lastSlash + 1).trim();
            } else {
        	int lastDot = url.lastIndexOf('.');
        	return url.substring(lastSlash + 1, lastDot).trim();
            }
        } else if (hasDot >= 0) {
            int lastDot = url.lastIndexOf('.');
            return url.substring(0,lastDot).trim();
        } else {
            return url.trim();
        }
    }
    
    static public String getFileExtension(String url) {
	// returns the file name extension
        url = removeAttributes(url);
        url = removeBookmark(url);
        int lastDot = url.lastIndexOf('.');
        if (lastDot >= 0) {
            return url.substring(lastDot+1);
        } else {
            return null;
        }
    }
    
    static public String getURLAttributes(String url) {
	// returns the attributes (after the ?) or null if there are none
        url = removeBookmark(url);
        int questionMark = url.indexOf('?');
        if (questionMark >= 0) {
            return url.substring(questionMark+1);
        } else {
            return null;
        }
    }

    static public String fullUrl(String url, String currentURLBase) {
        if (url.indexOf(':') < 0) {
            // url is relative so need both the base of the module and
            // contributions from the url
            return joinPaths(currentURLBase, url);
        } else {
            return url;
        }
    }

    /**
     * @param path1
     * @param path2
     * @return the combined path with one (and only one) forward slash between them
     * also simplifies some combinations involving ../
     */
    public static String joinPaths(String path1, String path2) {
        if (isCompleteURL(path2) || path1 == null || path1.equals("")) {
            return path2;
        }
        int length1 = path1.length();
        int length2 = path2.length();
        if (length2 == 0) {
            return path2;
        }
        int path2Start = path2.charAt(0);
	if (path2Start == '.' && length2 >= 3 && path2.charAt(1) == '.') {
            int lastSlash = path1.lastIndexOf('/', length1-1);
            if (lastSlash >= 0) {
        	// combining a/b with ../c to form a/c
        	return joinPaths(path1.substring(0,lastSlash), path2.substring(3));
            }
        }
	int path1End = path1.charAt(length1 - 1);
        if (path1End == '/' && path2Start == '/') {
            return path1 + path2.substring(1);
        } else if (path1End != '/' && path2Start != '/' && path2Start != '?') {
            return path1 + "/" + path2;
        } else {
            return path1 + path2;
        }
    }

    public static String extractServer(String urlString) {
        int doubleSlashLocation = urlString.indexOf("//");
        if (doubleSlashLocation >= 0) {
            int nextSlashLocation = urlString.indexOf("/", doubleSlashLocation + "//".length());
            if (nextSlashLocation >= 0) {
        	return urlString.substring(0, nextSlashLocation);
            }
        }
        return urlString;
    }

    public static ArrayList<String> removeEmptyLines(String lines[]) {
	ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].trim().isEmpty()) { 
        	result.add(lines[i]);
            }
        }	
        return result;
    }
    
    public static ArrayList<String> removeNetLogoCommentsAndEmptyLines(String lines[]) {
	ArrayList<String> result = new ArrayList<String>();
	for (int i = 0; i < lines.length; i++) {
	    String line;
	    int semicolon = lines[i].indexOf(';');
	    if (semicolon < 0) {
		line = lines[i];
	    } else {
		line = lines[i].substring(0, semicolon);
	    }
	    if (!line.trim().isEmpty()) { 
		result.add(line);
	    }
	}	
	return result;
    }
    
    public static ArrayList<String> splitIntoNonEmptyLines(String s) {
	return removeEmptyLines(s.split("\n"));
    }
    
    public static ArrayList<String> splitIntoNonEmptyLinesWithoutNetLogoComments(String s) {
	return removeNetLogoCommentsAndEmptyLines(s.split("\n"));
    }
    
    public static String replaceAllNonLetterOrDigit(String string, char replacement) {
	// removes all non alpha-numeric characters
	if (string == null) {
	    return null;
	}
	StringBuilder buffer = new StringBuilder();
	boolean replacedLastCharacter = false;
	for (int i = 0; i < string.length(); i++) {
	    char c = string.charAt(i);
	    if (Character.isLetterOrDigit(c)) {
		buffer.append(c);
		replacedLastCharacter = false;
	    } else if (!replacedLastCharacter) {
		buffer.append(replacement);
		replacedLastCharacter = true;
	    } // skip repeated replacements in a row
	}
	return buffer.toString();
    }
    
    static public String encode(String string) {
	StringBuilder encoding = new StringBuilder();
	int length = string.length();
	appendChunk(encoding, length);
	for (int i = 0; i < length-2; i += 3) {
	    int firstByte = string.charAt(i);
	    int secondByte = (string.charAt(i+1) << 8);
	    int thirdByte = (string.charAt(i+2) << 16);
	    appendChunk(encoding, firstByte | secondByte | thirdByte);
	}
	if (length%3 == 1) {
	    appendChunk(encoding, string.charAt(length-1));
	} else if (length%3 == 2) {
	    appendChunk(encoding, string.charAt(length-2) | (string.charAt(length-1) << 8));
	}
	return encoding.toString();
    }
    
    static public String createCDATASection(String string) {
	// TODO: substitute more generally than this
	// at least � causes the following when in XML
	// org.apache.xerces.impl.io.MalformedByteSequenceException: Invalid byte 1 of 1-byte UTF-8 sequence.
	return "<![CDATA[" + (string == null ? "no description" : string.replace("�","&not;")) + "]]>";
    }
    
    static public void appendChunk(StringBuilder encoding, int chunk) {
	int index = chunk & 0x3F;
	encoding.append(encodingTable.charAt(index));
	index = (chunk >> 6) & 0x3F;
	encoding.append(encodingTable.charAt(index));
	index = (chunk >> 12) & 0x3F;
	encoding.append(encodingTable.charAt(index));
	index = (chunk >> 18) & 0x3F;
	encoding.append(encodingTable.charAt(index));
    }
    
    static public String decode(String encoding) {
	if (encoding == null) {
	    Modeller.addToErrorLog("Decoding a null string");
	    return "";
	}
	StringBuilder decoding = new StringBuilder();
	int length = encoding.length();
	if (length%4 != 0) {
	    Modeller.addToErrorLog(encoding + " is not a multiple of 4 long. Truncated?");
	    return "";
	}
	int decodedLengthLeft = 0;
	for (int i = 0; i < length-3; i += 4) {
	    // every 4 characters encodes 3 original characters
	    int chunk = reverseEncodingTable(encoding.charAt(i)) |
	                (reverseEncodingTable(encoding.charAt(i+1)) << 6) |
	                (reverseEncodingTable(encoding.charAt(i+2)) << 12) |
	                (reverseEncodingTable(encoding.charAt(i+3)) << 18);
	    if (i == 0) {
		decodedLengthLeft = chunk;
	    } else {
		decoding.append((char) (chunk & 0xFF));
		decodedLengthLeft--;
		if (decodedLengthLeft == 0) break;
		char secondCharacter = (char) (chunk >> 8 & 0xFF);
		decoding.append(secondCharacter);
		decodedLengthLeft--;
		if (decodedLengthLeft == 0) break;
		char thirdCharacter = (char) (chunk >> 16 & 0xFF);
		decoding.append(thirdCharacter);
		decodedLengthLeft--;
	    }
	}
	return decoding.toString();
    }
    
    static public int reverseEncodingTable(char c) {
	if (c >= 'a' && c <= 'z') {
	    return c-'a';
	}
	if (c >= 'A' && c <= 'Z') {
	    return 26 + c-'A';
	}
	if (c >= '0' && c <= '9') {
	    return 52 + c-'0';
	}
	if (c == '_') {
	    return 62;
	}
	if (c == '-') {
	    return 63;
	}
	return 0; // shouldn't happen
    }

    static public String urlAttributeWithoutURLDecoding(String attributeValues, String attributeName) {
	if (attributeValues == null)
	    return null;
	attributeName = attributeName + "=";
	int attributePosition = attributeValues.indexOf("&" + attributeName);
	if (attributePosition < 0) {
	    attributePosition = attributeValues.indexOf("?" + attributeName);
	}
	if (attributePosition < 0) {
	    attributePosition = attributeValues.toLowerCase().indexOf("&" + attributeName.toLowerCase()); 
	}
	if (attributePosition < 0) {
	    attributePosition = attributeValues.toLowerCase().indexOf("?" + attributeName.toLowerCase()); 
	}
	if (attributePosition >= 0) {
	    String value = attributeValues.substring(attributePosition + attributeName.length() + 1);
	    int ampPosition = value.indexOf('&');
	    if (ampPosition >= 0) {
		value = value.substring(0, ampPosition);
	    }
	    int sharpPosition = value.indexOf('#');
	    if (sharpPosition > 0) {
		value = value.substring(0, sharpPosition);
	    }
	    return value;
	}
	return null;
    }
    
    static public String completeURL(String url) {
	if (isCompleteURL(url)) {
	    return url; 
	}
	return "http://" + url; // good default
    }

    public static boolean isCompleteURL(String url) {
	return url != null && url.indexOf(':') >= 0;
    }
    
    static public String removePTags(String html) {
	return removeTags("p", html, "", -1);
    }
    
    static public String removeOuterPTag(String html) {
	return removeTags("p", html, "", 0);
    }
    
    static public String removeTags(String tag, String html, String replacement, int repeatCount) {
	if (html == null) {
	    return null;
	}
	tag = tag.toLowerCase();
	String startTag = "<" + tag;
	return removeTags(startTag, tag, html, html.toLowerCase(), replacement, repeatCount);
    }
    
    private static String removeTags(String startTag, String tag, String html, String lowerCaseHTML, String replacement, int repeatCount) {
	// if repeatCount is negative it repeats until there are no more such tags
	int openingTagStart = lowerCaseHTML.indexOf(startTag);
	if (openingTagStart >= 0) {
	    int openingTagEnd = html.indexOf(">", openingTagStart);
	    if (openingTagEnd < 0) {
		return html;
	    } else {
		openingTagEnd++; // after the >
	    }
	    String closingTag = "</" + tag + ">";
	    // logically the following should be for the last closing tag
	    // but it doesn't matter operationally
	    int closingTagStart = lowerCaseHTML.indexOf(closingTag, openingTagEnd);
	    if (closingTagStart >= 0) {
		int closingTagEnd = closingTagStart+closingTag.length();
		// splice out the opening and closing tags
		html = html.substring(0, openingTagStart) + html.substring(openingTagEnd, closingTagStart) + replacement + html.substring(closingTagEnd);
		if (repeatCount != 0) {
		    return removeTags(startTag, tag, html, html.toLowerCase(), replacement, repeatCount-1);
		}
	    }
	}
	return html;
    }
    
    static public String quote(String s) {
	return "\"" + s + "\"";
    }
    
    static public String firstWord(String s) {
	String words[] = s.split("\\s|\\]|\\)"); // white space or ] or )
	if (words.length < 2) {
	    return null;
	}
	if (words[0].isEmpty()) {
	    return words[1];
	}
	return words[0];
    }
    
    static public String lastWord(String s) {
	String words[] = s.split("\\s|\\]|\\)"); // white space or ] or )
	if (words.length < 2) {
	    return null;
	}
	return words[words.length-1];
    }    
    
    public static boolean isInAComment(String code, int index) {
	// if a semi-colon is before the current index on the same line returns true
	for (int i = index-1; i >= 0; i--) {
	    char character = code.charAt(i);
	    if (character == ';') {
		// and the ; is not proceeded by a ' or a \
		if (i == 0 ||
		    i > 0 && code.charAt(i-1) != '\'' && code.charAt(i-1) != '\\' &&
		    !isInAString(code, i)) {
		    return true;
		}
	    } else if (character == '\r' || character == '\n') {
		return false;
	    }
	}
	return false;
    }
    
    public static boolean isInAString(String code, int index) {
	// true if the current index is in a quoted string
	// if there are an odd number of quotes on the line before or after the index then it is in a string
	boolean stringStarted = false;
	char kindOfQuote = 0;
	for (int i = 0; i < index; i++) {
	    char character = code.charAt(i);
	    if (stringStarted) {
		if (character == kindOfQuote &&
		    (kindOfQuote != '"' ||
		     i == 0 || 
		     // don't count \"
		     code.charAt(i-1) != '\\')) {
		    stringStarted = false;
		}
	    } else {
		if (character == ';') {
		    // NetLogo's comment so ignore this line
		    i = code.indexOf('\n', i);
		    if (i < 0) {
			// comment is last line -- no new line
			return stringStarted;
		    }
		} else if (character == '\'') {
		    kindOfQuote = '\'';
		    stringStarted = true;
		} else if (character == '"' && 
			   // don't count \"
			   (i == 0 || code.charAt(i-1) != '\\')) {
		    kindOfQuote = '"';
		    stringStarted = true;
		}
	    }
	}
	return stringStarted;
    }
    
//    public static int countUnslashedQuotes(String code, int stop) {
//	int count = 0;
//	for (int i = 0; i < stop; i++) {
//	    if (code.charAt(i) == '"' &&
//		(i == 0 || code.charAt(i-1) != '\\')) {
//		count++;
//	    }
//	}
//	return count;
//    }

    public static int indexOfNetLogoCodeOnly(char search, String s, int start) {
	// like indexOf(char, s, start) but ignores comments and strings
	int index = s.indexOf(search, start);
	while (index >= 0) {
	    if (isInAComment(s, index) || isInAString(s, index)) {
		index = s.indexOf(search, index+1);
	    } else {
		return index;
	    }
	}
	return index;
    }
    
    @SuppressWarnings("deprecation")
    public static int indexOfNetLogoCodeOnly(String search, String s, int start) {
	// like indexOf(string, s, start) but ignores comments and strings
	// and occurrences inside of literals
	int index = s.indexOf(search, start);
	while (index >= 0) {
	    int nextCharacterIndex = index+search.length();
	    if (nextCharacterIndex >= s.length()) {
		return index;
	    }
	    // GWT 2 doesn't handle isWhitespace
	    if (!Character.isSpace(s.charAt(nextCharacterIndex)) || isInAComment(s, index) || isInAString(s, index)) {
		index = s.indexOf(search, index+1);
	    } else {
		return index;
	    }
	}
	return -1;
    }
    
    static public String emphasise(String html) {
	// big, bold, and dark green
	return emphasise(html, "#00AF00");
    }
    
    static public String emphasise(String html, String color) {
	// big and bold and specified color
	return "<font color='" + color + "' size='4'><b>" + html + "</font></b>";
    }
    
    static public String changeColor(String html, String color) {
	String fontStart = "<font color='";
	int indexOfFontColor = html.indexOf(fontStart);
	if (indexOfFontColor < 0) {
	    // add specified color
	    return fontStart + color + "'>" + html + "</font>";
	} else {
	    // replace color
	    int indexOfColorEnd = html.indexOf("'", indexOfFontColor+fontStart.length());
	    return fontStart + color + html.substring(indexOfColorEnd);
	}
    }
    
    static public String emphasiseError(String html) {
	// big, bold, red
	return "<font color='ff0000' size='4'><b>" + html + "</font></b>";
    }
    
    public static String stronglyHighlight(String text) {
	return "<b><font size='4'><span style='background-color: #FFFF00'>" +  text + "</span></font></b>";
    }
    
    public static String fontSize(String text, int size) {
	return "<font size='" + size + "'>" +  text + "</font>";
    }

    public static String onlyValidNetLogoCharacters(String string, String replacement) {
	// remove all but letters, digits, and - and _
	return string.replaceAll("[^a-zA-Z0-9_-]", replacement);
    }

    public static String underline(String substring, String string) {
	String replacement = string.replaceFirst(substring, "<u>" + substring + "</u>");
	// following worked in development mode (JRE) but not in deployment (JavaScript)
	// regular expression differences probably to blame
	// since no longer using non-alphabetic short cuts not an issue
//	// don't want regular expression pattern so quoting it
//	String replacement = string.replaceFirst("\\Q" + substring + "\\E", "<u>" + substring + "</u>");
	if (replacement.equals(string)) {
	    // doesn't occur in string - so add it at the end
	    return string + " (<u>" + substring + "</u>)";
	} else {
	    return replacement;
	}
    }
    
    public static String validateGuid(String guid, String guidDescription) {
	// this could check that all characters are alphanumeric or - or _
	if (guid == null || guid.isEmpty()) {
	    return "Error. Missing " + guidDescription + ". Please correct your URL.";
	}
	if (guid.length() != 22) {
	    return "Error. " + guidDescription + " is " + guid.length() + " characters long but it should be exactly 22 characters long. Invalid key is '" + guid + "'. Please correct your URL.";
	}
	return null;
    }
    
    public static boolean errorIsDueToUser(String message) {
	// could be others
	// this is only used to determine how the error gets logged
	return message.endsWith("Please correct your URL.");
    }
    
    public static boolean probablyAURL(String string) {
	// intended to distinguish guids (which only contain alphanumeric or _ or -)
	return string.indexOf(':') >= 0 || 
	       string.indexOf('/') >= 0 || 
	       string.indexOf('.') >= 0 ||
	       // or it may be encoded
	       string.indexOf('%') >= 0;
    }

    public static String encodeColonAndSlash(String s) {
	// at least IE7 gets confused if a URL attribute is a URL with : or / in it
	return s.replace("/", "%2F").replace(":", "%3A").replace("#", "%23");
    }
    
    public static String addAttributeToURL(String url, String attributeName, String attributeValue) {
	// Adds either ? or & name=value to url
	// updates if attributeName already in the url
	StringBuffer newURL = new StringBuffer(url);
	int attributeIndex = newURL.indexOf("?");
	if (attributeIndex >= 0) {
	    int attributeNameIndex = newURL.indexOf(attributeName, attributeIndex);
	    if (attributeNameIndex >= 0) {
		int attributeValueEndIndex = newURL.indexOf("&", attributeNameIndex);
		if (attributeValueEndIndex < 0) {
		    attributeValueEndIndex = newURL.length();
		}
		newURL.replace(attributeNameIndex + attributeName.length() + 1, // add 1 for = sign
			       attributeValueEndIndex, 
			       attributeValue);
		return newURL.toString();
	    }
	}
	if (attributeIndex >= 0) {
	    newURL.append("&");
	} else {
	    newURL.append("?");
	}
	newURL.append(attributeName);
	newURL.append("=");
	newURL.append(attributeValue);
	return newURL.toString();
    }

    public static String addAttributeOrHashAttributeToURL(String url, String attributeName, String attributeValue) {
	// Adds either # or & name=value to url
	// updates if attributeName already in the url
	// makes more sense to add ? but that interferes with ordinary fetching of the page
	if (url == null) {
	    return null;
	}
	StringBuffer newURL = new StringBuffer(url);
	int attributeIndex = newURL.indexOf("#");
	if (attributeIndex >= 0) {
	    int attributeNameIndex = newURL.indexOf(attributeName, attributeIndex);
	    if (attributeNameIndex >= 0) {
		int attributeValueEndIndex = newURL.indexOf("&", attributeNameIndex);
		if (attributeValueEndIndex < 0) {
		    attributeValueEndIndex = newURL.length();
		}
		newURL.replace(attributeNameIndex + attributeName.length() + 1, // add 1 for = sign
			       attributeValueEndIndex, 
			       attributeValue);
		return newURL.toString();
	    }
	}
	if (attributeIndex < 0) {
	    newURL.append("#");
	} else if (!attributeName.equals("changes")) {
	    // is always #changes=...
	    newURL.append("&");
	}
	newURL.append(attributeName);
	newURL.append("=");
	newURL.append(attributeValue);
	return newURL.toString();
    }

    public static String upperCaseFirstLetter(String string) {
	if (string == null || string.isEmpty()) {
	    return string;
	}
	return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String changesGuid(String url) {
	if (url == null) {
	    return null;
	}
	// commented out until have a way to fetch the updated contents of a foreign URL copy (with changes)
//	if (!url.startsWith(getDomainURL())) {
//	    // is for another server
//	    return null;
//	}
	int hashIndex = url.indexOf('#');
	if (hashIndex < 0) {
	    return null;
	}
	if (hashIndex < 0) {
	    return null;
	}
	if (url.charAt(hashIndex+1) == '&') {
	    // fixes problem reported in Issue 930
	    hashIndex++;
	}
	if (!url.startsWith(CHANGES_EQUAL, hashIndex+1)) {
	    return null;
	}
	int semicolonIndex = url.indexOf(';');
	if (semicolonIndex < 0) {
	    return url.substring(hashIndex + CHANGES_EQUAL.length()+1);
	} else {
	    return url.substring(hashIndex + CHANGES_EQUAL.length()+1, semicolonIndex);
	}
    }
    
    public static boolean hasChangesGuid(String url) {
	// more efficient than changeGuid(url) != null
	if (url == null) {
	    return false;
	}
	// commented out until have a way to fetch the updated contents of a foreign URL copy (with changes)
//	if (!url.startsWith(getDomainURL())) {
//	    // is for another server
//	    return false;
//	}
	int hashIndex = url.indexOf('#');
	if (hashIndex < 0) {
	    return false;
	}
	if (url.length() <= hashIndex+1) {
	    return false;
	}
	if (url.charAt(hashIndex+1) == '&') {
	    // fixes problem reported in Issue 930
	    hashIndex++;
	}
	if (url.startsWith(CHANGES_EQUAL, hashIndex+1)) {
	    return true;
	} else {
	    return false;
	}
    }
    
    /**
     * Finds the values of a URL parameter list (e.g. ?foo=1&bar=yes)
     * @param attribute
     * @param url
     * @return the value of the attribute in the url or null if not there
     */
    public static String getURLParameter(String attribute, String url) {
	if (url == null) {
	    return null;
	}
	int entry = url.indexOf(attribute + "=");
	if (entry >= 0) {
	    int start = entry+attribute.length()+1;
	    int end = url.indexOf('&', start);
	    if (end < 0) {
		end = url.indexOf('#', start);
	    }
	    if (end < 0) {
		end = url.length();
	    }
	    return url.substring(start, end);
	}
	return null;
    }
    
    public static int firstNonSpace(String string, int start) {
	while (string.charAt(start) == ' ') {
	    start++;
	}
	return start;
    }

    /**
     * @param url
     * @return the value of the tab parameter decoded
     * or the url if there is no tab parameter
     */
    public static String extractTabAttribute(String url) {
	// TODO: make generic version of this
	int index = url.indexOf("tab=");
	if (index < 0) {
	    return null;
	} else {
	    index += "tab=".length();
	    int nextParameterIndex = url.indexOf("&", index);
	    if (nextParameterIndex < 0) {
		return url.substring(index);
	    } else {
		return url.substring(index, nextParameterIndex);
	    }
	}
    }
    
    public static void removeAttributeFromURL(String attributeName, StringBuffer newURL) {
	int copyStart = newURL.indexOf(attributeName + "=");
	if (copyStart > 0) {
	    // remove it if reloading
	    // remove proceeding ? or &
	    int copyEnd = newURL.indexOf("&", copyStart);
	    if (copyEnd < 0) {
		copyEnd = newURL.length();
	    }
	    newURL.replace(copyStart-1, copyEnd, "");
	    if (newURL.indexOf("?") < 0) {
		int firstAmpersand = newURL.indexOf("&");
		if (firstAmpersand >= 0) {
		    newURL.replace(firstAmpersand, firstAmpersand+1, "?");		    
		}
	    }
	}
    }

    public static ArrayList<String> removeEmptyStrings(String[] strings) {
	ArrayList<String> nonEmptyElements = new ArrayList<String>();
	for (String s : strings) {
	    if (!s.isEmpty()) {
		nonEmptyElements.add(s);
	    }
	}
	return nonEmptyElements;
    }

    public static String getHostBaseURL() {
	if (hostBaseURL == null) {
	    Logger.getLogger("CommonUtils").severe("hostBaseURL not set");
	}
        return hostBaseURL;
    }

    public static void setHostBaseURL(String hostBaseURL) {
        CommonUtils.hostBaseURL = hostBaseURL;
        if (hostBaseURL != null) {
            // not sure how it ever is null -- and it that will just cause more problems downstream
            staticPagePath = hostBaseURL.replace("/m/", "/p/");
        }
    }
    
    public static String getStaticPagePath() {
	if (staticPagePath == null) {
	    Logger.getLogger("CommonUtils").severe("hostBaseURL not set");
	}
        return staticPagePath;
    }
    
    public static String getDomainURL() {
        return CommonUtils.getDomainURL(hostBaseURL);
    }
    
    public static String replaceNonBreakingSpaces(String s) {
	return s.replaceAll(NON_BREAKING_SPACE_REGULAR_EXPRESSION, " ");
//	try {
//	    return new String(s.getBytes("US-ASCII"));
//	} catch (UnsupportedEncodingException e) {
//	    e.printStackTrace();
//	    return s;
//	}
//	return s.replace((char) 160, (char) 32);
    }
    
    public static String prototypeName(String nameOrUrl) {
	if (nameOrUrl.startsWith(CommonUtils.TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN)) {
	    String macroBehaviourNameEncoded = nameOrUrl.substring(CommonUtils.TOP_LEVEL_MACRO_BEHAVIOUR_TOKEN_LENGTH);
	    if (macroBehaviourNameEncoded.indexOf('>') >= 0) {
		// older stored versions didn't encode the HTML
		return macroBehaviourNameEncoded;
	    } else {
		// bookmark shouldn't be there is everything is working well
		// but Howard reported problem on 6 Feb 2013 email
		return CommonUtils.decode(CommonUtils.removeBookmark(macroBehaviourNameEncoded));
	    }
	} else {
	    return null;
	}
    }
    
    public static String quoteContentsOfNextBracket(String piece) {
	// e.g. ... [a b] ... -> ... ["a" "b"] ...
	if (piece.trim().isEmpty()) {
	    return "";
	}
	int openBracket = openBracket(piece, 0);
	if (openBracket < 0) {
	    return piece;
	}
	int closeBracket = closeBracket(piece, openBracket+1);
	if (closeBracket < 0) {
	    return piece;
	}
	return piece.substring(0, openBracket+1) + 
	       quoteEach(piece.substring(openBracket+1, closeBracket)) + 
	       piece.substring(closeBracket);
    }
    
    public static String quoteEach(String string) {
	String[] pieces = string.split("(\\s)+");
	StringBuffer result = new StringBuffer();
	for (String piece : pieces) {
	    if (piece.indexOf("\"") >= 0) {
		// already quoted
		return string;	    
	    }
	    if (!piece.isEmpty()) {
		result.append(" \"");
		result.append(piece);
		result.append("\"\n");
	    }
	}
	return result.toString();
    }
    
    public static String taskifyContentsOfNextBracket(String piece) {
	// e.g. ... [a b] ... -> ...(list task [a] task [b]) ...
	if (piece.trim().isEmpty()) {
	    return "";
	}
	int openBracket = openBracket(piece, 0);
	if (openBracket < 0) {
	    return piece;
	}
	int closeBracket = closeBracket(piece, openBracket+1);
	if (closeBracket < 0) {
	    return piece;
	}
	String taskList = (openBracket+1 == closeBracket) ? "[]" : 
	    "(list " + taskifyEach(piece.substring(openBracket+1, closeBracket)) + ")";
	String result = piece.substring(0, openBracket) + taskList;
	if (closeBracket+1 < piece.length() && result.indexOf(START_ALREADY_PROCESSED_LEAVE_ALONE) < 0) {
	    // adding ALREADY_PROCESSED_LEAVE_ALONE protects the rest for additional processing - e.g. taskifyList
	    result = "\n" + START_ALREADY_PROCESSED_LEAVE_ALONE + "\n" + result + "\n" + piece.substring(closeBracket+1) + END_ALREADY_PROCESSED_LEAVE_ALONE + "\n";
	}
	return result;
    }

    public static String taskifyEach(String string) {
	String[] pieces = string.split("(\\s)+");
	StringBuffer result = new StringBuffer();
	for (String piece : pieces) {
//	    if (piece.indexOf("\"") >= 0) {
//		// already quoted
//		return string;	    
//	    }
	    if (!piece.isEmpty()) {
		result.append(" task [");
		result.append(piece);
		result.append("]\n");
	    }
	}
	return result.toString();
    }

    public static String removeQuotesAndAddNewLines(String s) {
	// removes quotes and replaces every second one with a new line
	StringBuffer result = new StringBuffer();
	int length = s.length();
	boolean replaceNextQuoteWithNewLine = false;
	for (int i = 0; i < length; i++) {
	    char character = s.charAt(i);
	    if (character == '"') {
		if (replaceNextQuoteWithNewLine) {
		    result.append("\n  "); // new line and indent a space	    
		}
	    } else {
		result.append(character);
	    }
	}
	return result.toString();
    }

    public static ArrayList<String> beforeContentsAndAfter(String string) {
	ArrayList<String> result = new ArrayList<String>();
	int openBracket = string.indexOf('[');
	if (openBracket < 0) {
	    return null;
	}
	int closeBracket = string.lastIndexOf(']');
	if (closeBracket < 0) {
	    return null;
	}
	result.add(string.substring(0, openBracket+1));
	result.add(string.substring(openBracket+1, closeBracket));
	result.add(string.substring(closeBracket));
	return result;
    }

    public static int closeBracket(String s, int start) {
        // returns location within s of the matching closing bracket
        // this ignores bracketed sub-expressions
        // returns a negative number if there is no closing bracket
        int skipCount = 0;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ']' && !isInAComment(s, i)) {
        	if (skipCount == 0) {
        	    return i;
        	} else {
        	    skipCount--;
        	}
            } else if (c == '[' && !isInAComment(s, i)) {
        	skipCount++;
            }
        }
        return -1;
    }
    
    public static int openBracket(String s, int start) {
        // returns location within s of the the first open bracket
        // this ignores bracketed sub-expressions in parenthesis
	// and those bracketed by START_ALREADY_PROCESSED_LEAVE_ALONE and END_ALREADY_PROCESSED_LEAVE_ALONE
        // returns a negative number if there is no open bracket
	int skipStart = s.indexOf(START_ALREADY_PROCESSED_LEAVE_ALONE);
	int skipEnd = skipStart < 0 ? -1 : s.indexOf(END_ALREADY_PROCESSED_LEAVE_ALONE, skipStart);
        int skipCount = 0;
        for (int i = start; i < s.length(); i++) {
            if (i == skipStart) {
        	i = skipEnd + END_ALREADY_PROCESSED_LEAVE_ALONE.length();
            }
            char c = s.charAt(i);
            if (c == '[' && !isInAComment(s, i)) {
        	if (skipCount == 0) {
        	    return i;
        	}
            } else if (c == '(' && !isInAComment(s, i)) {
        	skipCount++;
            } else if (c == ')' && !isInAComment(s, i)) {
        	skipCount--;
            }
        }
        return -1;
    }

    public static boolean isTrivialCode(String code) {
	ArrayList<String> parts = removeEmptyLines(code.split("(\\s)+"));
	// too many parts and the overhead of NetLogo's implementation of run
	// makes it worth treating it as non-trivial
	// 1 for now since larger makes problems for quotes within quotes (easy to fix)
	// and checking for equality when removing micro-behaviours
	return parts.size() <= 1;
    }

    public static HashMap<Integer, String> transferTextAreaValues(HashMap<Integer, String> newTextAreaValues,
	                                                          HashMap<Integer, String> oldTextAreaValues) {
	if (newTextAreaValues == null) {
	    return oldTextAreaValues;
	}
	// transfers any old values of undefined new values
	Set<Entry<Integer, String>> oldEntrySet = oldTextAreaValues.entrySet();
	for (Entry<Integer, String> oldEntry : oldEntrySet) {
	    Integer key = oldEntry.getKey();
	    String oldValue = oldEntry.getValue();
	    if (!newTextAreaValues.containsKey(key)) {
		newTextAreaValues.put(key, oldValue);
	    }
	}
	Set<Entry<Integer, String>> newEntrySet = newTextAreaValues.entrySet();
	ArrayList<Integer> keysToRemove = new ArrayList<Integer>();
	for (Entry<Integer, String> newEntry : newEntrySet) {
	    if (TOKEN_FOR_REMOVED_TEXT_AREA.equals(newEntry.getValue())) {
		keysToRemove.add(newEntry.getKey());
	    }
	}
	for (Integer key : keysToRemove) {
	    newTextAreaValues.remove(key);
	}
	return newTextAreaValues;
    }
   
    /**
     * @param url
     * @return the string without the initial -
     */
    public static String withoutMinusSign(String url) {
	if (url != null && !url.isEmpty() && url.charAt(0) == '-') {
	    return url.substring(1);
	} else {
	    return url;
	}
    }

    public static String removeBRfromPREs(String html) {
	// works around a problem that the Rich Text Editor replaces new lines with <br>
	StringBuffer result = new StringBuffer(html);
	String lowerCaseHTML = html.toLowerCase();
	int preIndex;
	int preIndexEnd = 0;
	while ((preIndex = lowerCaseHTML.indexOf("<pre>", preIndexEnd)) >= 0) {
	    preIndexEnd = lowerCaseHTML.indexOf("</pre>", preIndex);
	    if (preIndexEnd > 0) {
		result.replace(preIndex, preIndexEnd,
			html.substring(preIndex, preIndexEnd).replace("<BR>", "\r").replace("<br>", "\r"));
	    } else {
		preIndexEnd = preIndex=1; // skip over this
	    }
	}
	return result.toString();
    }

    public static String staticPageFolder() {
	return "p";
    }

    /**
     * @param html
     * @return the string between <pre> and </pre>
     * or null if it doesn't exist
     */
    public static Integer[] getPreElement(String html) {
	String lowerCaseHTML = html.toLowerCase();
	int preIndex = lowerCaseHTML.indexOf("<pre>", 0);
	if (preIndex < 0) {
	    return null;
	}
	int preIndexEnd = lowerCaseHTML.indexOf("</pre>", preIndex);
	if (preIndexEnd < 0) {
	    return null;
	}
	// add 5 to skip <pre>
	return new Integer[] {preIndex+5, preIndexEnd};
    }

    public static int maximumIndex(HashMap<Integer, String> textAreaValues) {
	if (textAreaValues == null) {
	    return 0;
	}
        int maxIndex = -1;
	Set<Entry<Integer, String>> entrySet = textAreaValues.entrySet();
	for (Entry<Integer, String> entry : entrySet) {
	    if (!CommonUtils.TOKEN_FOR_REMOVED_TEXT_AREA.equals(entry.getValue())) {
		Integer key = entry.getKey();
		if (key > maxIndex) {
		    maxIndex = key;
		}
	    }
	}
        return maxIndex;
    }

    /**
     * @param text
     * @param maxCharacterWidth 
     * @return the number of lines and the length of the longest line
     */
    public static Integer[] lineCountAndLineMax(String text, int maxCharacterWidth) {
	if (text == null) {
	    return null;
	}
	Integer[] result = new Integer[2];
	String[] lines = text.split("\n");
	result[0] = lines.length;
	result[1] = 0;
	for (String line : lines) {
	    int length = line.length();
	    if (length > result[1]) {
		result[1] = length;
	    }
	    // if length == maxCharacterWidth don't add an extra line
	    result[0] += (length-1)/maxCharacterWidth;
	}
	if (!text.isEmpty() && text.charAt(text.length()-1) == '\n') {
	    // if enter is entered at the end then add a new line
	    result[0]++;
	}
	return result;
    }

    /**
     * @param line
     * @return line with any initial elements starting with </ removed
     */
    public static String stripAwayInitialClosingHTMLElements(String line) {
	if (line.trim().startsWith("</")) {
	    int closeOfElement = line.indexOf('>');
	    return stripAwayInitialClosingHTMLElements(line.substring(closeOfElement+1));
	}
	return line;
    }

    public static boolean isErrorResponse(String response) {
	return response.startsWith("Error");
    }

    public static String htmlStringToText(String html) {
	// <br> disappears completely and hence words before and after are joined
	// this replaces them with spaces
	// <div ...> causes the same problem
	String htmlWithSpaces = html.replace("<br>", "&nbsp;").replace("<div", "&nbsp;<div");
	return new HTML(htmlWithSpaces).getText();
    }

    public static String ignoreReleaseVersionNumber(String string) {
	// converts both m4a-gae.appspot.com and nnn.m4a-gae.appspot.com to m.modelling4all.org
        return string.replaceFirst("\\d+.m4a-gae.appspot.com/", "m.modelling4all.org/")
                     .replace("m4a-gae.appspot.com/", "m.modelling4all.org/");
    }

    /**
     * @param string
     * @return the integer if string is a quoted integer otherwise null
     */
    public static Integer integerIfQuoted(String string) {
	if (string == null) {
	    return null;
	}
	int length = string.length();
	if (length > 2 && string.charAt(0) == '"' && string.charAt(length-1) == '"') {
	    try {
		return Integer.parseInt(string.substring(1, length-1));
	    } catch (NumberFormatException e) {
		return null;
	    }
	} else {
	    return null;
	}
    }
    
    /**
     * @param string
     * @return the integer if string is a quoted integer otherwise null
     */
    public static Double doubleIfQuoted(String string) {
	if (string == null) {
	    return null;
	}
	int length = string.length();
	if (length > 2 && string.charAt(0) == '"' && string.charAt(length-1) == '"') {
	    try {
		return Double.parseDouble(string.substring(1, length-1));
	    } catch (NumberFormatException e) {
		return null;
	    }
	} else {
	    return null;
	}
    }

    /**
     * @param string
     * @return the string without the quotes if it is a quoted otherwise null
     */
    public static String stringIfQuoted(String string) {
	if (string == null) {
	    return null;
	}
	int length = string.length();
	if (length > 2 && string.charAt(0) == '"' && string.charAt(length-1) == '"') {
	    return string.substring(1, length-1);
	} else {
	    return null;
	}
    }

    private static String removeInitialClosingTags(String string) {
	// remove all instances of </ that occur before any < not followed by /
	boolean waitingForClosingBracket = false;
	for (int i = 0; i < string.length()-1; i++) {
	    char character = string.charAt(i);
	    if (character == '<') {
		character = string.charAt(i+1);
		if (character == '/') {
		    waitingForClosingBracket = true;   
		} else {
		    // doesn't start with a closing tag
		    return string;
		}
	    } else if (waitingForClosingBracket && character == '>') {
		// remove this closing tag but keep looking for the next one
		string = string.substring(i+1);
		i = -1;
	    }
	}
	return string;
    } 
    
    public static String removeFinalOpeningTags(String html) {
	// remove all opening tags (< not followed by /) at the end of the string
	if (html.isEmpty()) {
	    return html;
	}
	int newEndOfString = html.length()-1;
	// ignore a final < so start with newEndOfString-1
	for (int i = newEndOfString-1; i >= 0 ; i--) {
	    char character = html.charAt(i);
	    if (character == '<') {
		character = html.charAt(i+1);
		if (character == '/') {
		    return html.substring(0, newEndOfString);
		} else {
		    newEndOfString = i;
		}
	    }
	}
	return html.substring(0, newEndOfString);
    }
    
//    public static String removeOuterPElement(String html) {
//	// removes <p ...> xxx </p> resulting in xxx
//	for (int i = 0; i < html.length(); i++) {
//	    char character = html.charAt(i);
//	    if (character == '<') {
//		character = html.charAt(i+1);
//		if (character != 'p' && character != 'P') {
//		    return html;   
//		}
//		int startTagEnd = html.indexOf('>', i);
//		int endTag;
//		if (html.endsWith("</p>") || html.endsWith("</P>")) {
//		    endTag = html.length()-4;
//		} else {
//		    // is not enclosed in a P element
//		    return html;
//		}
////		// skip to end of P element
////		i = html.indexOf(">", i);
////		int end = html.lastIndexOf("</p");
////		if (end < 0) {
////		    end = html.lastIndexOf("</P");
////		}
////		if (end < 0) {
////		    end = html.length();
////		}
//		return html.substring(startTagEnd+1, endTag);
//	    }
//	}
//	return html;
//    }
    
    public static String removeAllPElements(String html) {
	return html.replace("<p>", "").replace("<P>", "").replace("</p>", " ").replace("</P>", " ").replace("</p>", " ").replace("</P>", " ").replace("<P/>", "").replace("<p/>", "");
    }

    public static String getNameHTML(String nameHTMLAndDescription) {
	int beginDescriptionIndex = nameHTMLAndDescription.indexOf(BEGIN_DESCRIPTION);
	if (beginDescriptionIndex >= 0) {
	    int endDescription = nameHTMLAndDescription.lastIndexOf(END_DESCRIPTION);
	    if (endDescription > 0) {
		return removeAllPElements(
			removeOuterPTag(
				removeInitialClosingTags(nameHTMLAndDescription.substring(endDescription+END_DESCRIPTION.length()))));
	    }
	}
        return removeAllPElements(removeOuterPTag(removeInitialClosingTags(nameHTMLAndDescription)));
    }
    
    public static String getName(String nameHTMLAndDescription) {
	if (nameHTMLAndDescription == null) {
	    return null;
	}
	return getInnerText(getNameHTML(nameHTMLAndDescription));
    }

    public static String getDescription(String nameHTMLAndDescription) {
	int beginDescriptionIndex = nameHTMLAndDescription.indexOf(BEGIN_DESCRIPTION);
	if (beginDescriptionIndex < 0) {
	    return "";
	}
	// following was using lastIndexOf but that meant that sometimes the comment included End description
	int endDescription = nameHTMLAndDescription.indexOf(END_DESCRIPTION);
	if (endDescription < 0) {
	    return "";
	}
	// add a trailing space to simplify adding this to title information
	return removeHTMLMarkupIfAny(
		nameHTMLAndDescription.substring(beginDescriptionIndex+BEGIN_DESCRIPTION.length(), endDescription).trim() + " ");
    }

    public static String combineNameHTMLAndDescription(String nameHTML, String description) {
	return BEGIN_DESCRIPTION + description + END_DESCRIPTION + nameHTML;
    }

    public static String addNewLineIfLastLineIsComment(String code) {
	String[] lines = code.split("\n");
	if (lines[lines.length-1].contains(";")) {
	    return code + "\n";
	} else {
	    return code;
	}
    }

    @SuppressWarnings("deprecation")
    public static String[] splitByFirstWhiteSpace(String string) {
        // like string.split("(\\s)+", 2) but doesn't consider white space inside quoted strings
	int length = string.length();
	for (int i = 0; i < length; i++) {
	    char character = string.charAt(i);
	 // GWT 2.0 doesn't handle isWhitespace
	    if (Character.isSpace(character)) {
		if (!isInAString(string, i)) {
		    String[] result = new String[2];
		    if (i == 0) {
			result[0] = "";
		    } else {
			result[0] = string.substring (0, i);
		    }
		    if (i+1 == length) {
			result[1] = "";
		    } else {
			result[1] = string.substring(i+1);
		    }
		    return result;
		}
	    }
	}
	String [] result = new String[1];
	result[0] = string;
        return result;
    }
    
    public static String truncateIfTooLong(String string, int maxLength) {
	if (string.length() > maxLength) {
	    return string.substring(0, maxLength) + "...";
	} else {
	    return string;
	}
    }
    
    public static String netLogoNameFromURL(String url) {
	String changesGuid = changesGuid(url);
	if (changesGuid == null) {
	    return null;
	} else {
	    return netLogoNameFromGuid(changesGuid, false);
	}
    }

    public static String netLogoNameFromGuid(String changesGuid, boolean ignoreGeneratedGuids) {
	if (changesGuid.isEmpty()) {
	    return null;
	}
	// convert to upper case since needed to remove-behaviours to match the NetLogo task format
	changesGuid = changesGuid.toUpperCase();
	changesGuid = changesGuid.replace('.', '-'); // looks nicer with hypens
//	int nameEnd = changesGuid.indexOf('.');
//	if (nameEnd >= 0) {
//	   changesGuid = changesGuid.substring(0, nameEnd);
//	}
	if (ignoreGeneratedGuids && changesGuid.length() == 22 && changesGuid.indexOf('-') < 0) {
	    // is an old-style generated guid -- by returning null should force a new name based upon
	    // the micro-behaviour description
	    return null;
	}
	// all generated names begin with - so they are listed together
	// and they can start with anything (e.g. a digit)
	char firstCharacter = changesGuid.charAt(0);
	if (firstCharacter == '-') {
	    int secondCharacter = changesGuid.charAt(1);
	    if (secondCharacter >= '0' && secondCharacter <= '9') {
		changesGuid = "-x" + changesGuid.substring(1);
	    }
	    return changesGuid;
	} else if (Character.isDigit(firstCharacter)) {
	    return "-MB-" + changesGuid;
	} else {   
	    return "-" + changesGuid;
	}
    }

    public static int firstDifference(String a, String b) {
	int stop = Math.min(a.length(), b.length());
	for (int i = 0; i < stop; i++) {
	    if (a.charAt(i) != b.charAt(i)) {
		return i;
	    }
	}
	return stop;
    }

    public static int firstWordDifference(String a, String b) {
	int index = firstDifference(a, b);
	if (index > 0) {
	    int lastSpaceBeforeDifference = a.lastIndexOf(' ', index);
	    if (lastSpaceBeforeDifference >= 0) {
		return lastSpaceBeforeDifference; 
	    } else {
		return index;
	    }
	}
	return 0;
    }

    public static int closingParenthesisIndex(String code) {
	// returns index of first closing parenthesis ignore embedded matching parentheses
	// start with open paren
	int index = code.indexOf('(');
	if (index < 0) {
	    return -1;
	}
	index++;
	int openParenCount = 1;
	while (true) {
	    int nextOpen = code.indexOf('(', index);
	    int nextClose = code.indexOf(')', index);
	    if (nextClose < 0) {
		return -1;
	    }
	    if (nextOpen < 0) {
		while (openParenCount > 1) {
		    nextClose = code.indexOf(')', nextClose+1);
		    openParenCount--;
		}
		return nextClose;
	    } else if (nextClose < nextOpen) {
		index = nextClose+1;
		openParenCount--;
	    } else {
		index = nextOpen+1;
		openParenCount++;
	    }
	}
    }

    /**
     * @param nouns
     * @return return an English list using commas to separate elements when there are more than 2 
     */
    public static String englishList(List<String> nouns) {
	int size = nouns.size();
	if (size == 0) {
	    return "";
	} else if (size == 1) {
	    return nouns.get(0);
	} else if (size == 2) {
	    return nouns.get(0) + " " + Modeller.constants.and() + " " + nouns.get(1);
	}
	String result = "";
	for (int i = 0; i < size-1; i++) {
	    result += nouns.get(i) + ", ";
	}
	result += Modeller.constants.and() + " " + nouns.get(size-1);
	return result;
    }

    public static String getDefaultInfoTabContents() {
	if (defaultInfoTabContents  == null) {
	    String contents = "## WHAT IS IT?\n\n";
	    contents += "(a general understanding of what the model is trying to show or explain)\n\n";
	    contents += "## HOW IT WORKS\n\n";
	    contents += "(what rules the agents use to create the overall behavior of the model)\n\n";
	    contents += "## HOW TO USE IT\n\n";
	    contents += "(how to use the model, including a description of each of the items in the Interface tab)\n\n";
	    contents += "## THINGS TO NOTICE\n\n";
	    contents += "(suggested things for the user to notice while running the model)\n\n";
	    contents += "## THINGS TO TRY\n\n";
	    contents += "(suggested things for the user to try to do (move sliders, switches, etc.) with the model)\n\n";
	    contents += "## EXTENDING THE MODEL\n\n";
	    contents += "(suggested things to add or change in the Code tab to make the model more complicated, detailed, accurate, etc.)\n\n";
	    contents += "## NETLOGO FEATURES\n\n";
	    contents += "(interesting or unusual features of NetLogo that the model uses, particularly in the Code tab; or where workarounds were needed for missing features)\n\n";
	    contents += "## RELATED MODELS\n\n";
	    contents += "(models in the NetLogo Models Library and elsewhere which are of related interest)\n\n";
	    contents += "## CREDITS AND REFERENCES\n\n";
	    contents += "(a reference to the model's URL on the web if it has one, as well as any other necessary credits, citations, and links)\n\n";
	    defaultInfoTabContents = contents;
	}
	return defaultInfoTabContents;
    }

    public static String listWithSeparator(List<String> list, String separator) {
	String result = "";
	for (String string : list) {
	    result += string + separator;
	}
	return result;
    }
    
    public static String comment(String text) {
	String result = "";
	String[] lines = text.split("\n");
	for (String line : lines) {
	    if (!line.trim().isEmpty()) {
		result += "\n ; " + line;
	    }
	}
	return result;
    }

    public static String firstURL(String allURLs) {
	int indexOfSemicolon = allURLs.indexOf(";");
	if (indexOfSemicolon < 0) {
	    return allURLs;
	} else {
	    return allURLs.substring(0, indexOfSemicolon);
	}
    }
    
    public static int endQuoteIndex(String string) {
	// returns the location of second double quote that is not proceeded by a \
	int index = string.indexOf('"');
	if (index < 0) {
	    return index;
	}
	index++;
	index = string.indexOf('"', index);
	if (index < 0) {
	    return index;
	}
	while (string.charAt(index-1) == '\\') {
	    index = string.indexOf('"', index+1);
	}
	return index;
    }

    public static String removeSerialNumber(String changesGuid) {
	int serialNumberStart = changesGuid.lastIndexOf(".");
	if (serialNumberStart < 0) {
	    return changesGuid;
	} else {
	    return changesGuid.substring(0, serialNumberStart);
	}
    }

    public static String removeAlreadyProcessedMarkers(String string) {
	return string.replaceAll(START_ALREADY_PROCESSED_LEAVE_ALONE, "").replaceAll(END_ALREADY_PROCESSED_LEAVE_ALONE, "");
    }

    
}
