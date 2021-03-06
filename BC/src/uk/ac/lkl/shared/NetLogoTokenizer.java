/**
 * 
 */
package uk.ac.lkl.shared;

import java.util.ArrayList;

/**
 * Produces a sequence of identifiers, strings, brackets, HTML elements
 * skips over NetLogo comments
 * 
 * @author Ken Kahn
 *
 */
public class NetLogoTokenizer {
    
    private String code;
    
    private int start = -1;
    private int end = -1;

    // acts like a stack where the first element is the current things being searched for
    // e.g. <foo a="...">
    private ArrayList<Character> searchingForStack = new ArrayList<Character>();

    private ArrayList<String> elementStack = new ArrayList<String>();

    private boolean incrementEndNextCharacter = true;

    private static String validIdentifierCharacters = ".?=*!<>:#+/%$_^'&-";

    public NetLogoTokenizer(String code) {
	// PRE elements do cause line breaks, hence the use of "\n" below
	// &lt; and the like get confused for comments without calling removeHTMLTokens
	this.code = CommonUtils.removeHTMLTokens(CommonUtils.removeTags("pre", code, "\n", -1));
    }
    
    public String peekToken() {
	return peekToken(1);
    }

    public String peekToken(int n) {
	int saveStart = start;
	int saveEnd = end;
	ArrayList<Character> savedSearchingForStack = new ArrayList<Character>(searchingForStack);
	ArrayList<String> savedEementStack = new ArrayList<String>(elementStack);
	boolean savedIncrementEndNextCharacter = incrementEndNextCharacter;
	String nextToken = nextToken();
	n--;
	while (n > 0) {
	    nextToken = nextToken();
	    n--;
	}
	start = saveStart;
	end = saveEnd;
	searchingForStack = savedSearchingForStack;
	elementStack = savedEementStack;
	incrementEndNextCharacter = savedIncrementEndNextCharacter;
	return nextToken;
    }
    
    public String nextToken() {
	return nextToken(false);
    }
    
    public String nextToken(boolean includeComments) {
	searchingForStack.clear();
	if (incrementEndNextCharacter) {
	    end++;
	} else {
	    incrementEndNextCharacter = true;
	}
	start = end;
	Character previousCharacter = 0;
	while (end < code.length()) {
	    Character character = code.charAt(end);
	    char searchingFor = getSearchingFor();
	    if (searchingFor == '>' && character != '>' && peekSearchingFor() != '>') {
		// if inside an HTML element ignore everything except closes
		end++;
	    } else if (searchingFor == '\n') {
		if (character == '\n' || character == '\r') {
		    // end of a comment 
		    popSearchingFor();
		    if (includeComments) {
			if (code.charAt(end) == '\r') {
			    end++;
			}
//			end++;
//			incrementEndNextCharacter = false;
			return code.substring(start, end);
		    } else {
			end++;
			start = end;
		    }
		} else {
		    end++;
		}
	    } else if (elementStack.isEmpty() && character == '"' && previousCharacter != '\\') {
		if (searchingFor == character) {
		    popSearchingFor(); // reset it
		    searchingFor = getSearchingFor();
		    if (searchingFor == 0) {
			return code.substring(start, end+1); // include the closing quote
		    } else {
			end++; // continue searching after popping the search for quotes
		    }
		} else {
		    end++;
		    pushSearchingFor('"');
		}
	    } else if (searchingFor == '"') {
		if (character == '\n') {
		    // Strings (words) can't span multiple lines so terminate the string
		    // unless inside an HTML element
		    searchingFor = 0;
		    return code.substring(start, end);
		} else {
		    end++;
		}
	    } else if (character == '<' && 
		       (start == end || !elementStack.isEmpty()) && 
		       searchingFor == 0 &&
		       alphaNextCharacter(start)) {
		// HTML element -- not inside an identifier
//		if (start != end) {
//		    // return the token inside the HTML element
//		    // next time around it'll deal with closing tag.
//		    end--;
//		    return code.substring(start, end+1);
//		}
		pushSearchingFor('>');
		end++;
	    } else if (character == '>' && searchingFor == '>' && possiblePenultimateCloseTagCharacter(previousCharacter)) {
		// if previous character is space then not closing an HTML element
		popSearchingFor();
		searchingFor = getSearchingFor();
		if (searchingFor == 0) {
		    String element = code.substring(start, end+1);
		    if (!elementStack.isEmpty() && pairedElements(element)) {
			elementStack.remove(0);
			return element; // include the starting tag and the the closing tag (including >)
		    } else {
			elementStack.add(element);
			end++;
		    }
		} else {
		    end++; // continue searching after popping the search for >
		}
	    } else if (elementStack.isEmpty() && code.substring(end, end+1).matches("\\s")) {
//	    else if (Character.isWhitespace(character)) {
		// Character.isWhitespace not available in GWT due to UNICODE dependence
		if (searchingFor != 0) {
		    end++;
		} else if (start == end) {
		    start++;
		    end++;
		} else {
		    return code.substring(start, end);
		}
	    } else if (Character.isLetterOrDigit(character) || validIdentifierCharacters.indexOf(character) >= 0) {
		end++;
	    } else if (elementStack.isEmpty() && (character == '[' || character == ']' || character == '(' || character == ')')) {
		if (start == end) {
		    // return the bracket
		    return code.substring(start, end+1);
		} else {
		    incrementEndNextCharacter  = false;
		    return code.substring(start, end);
		}
	    } else if (character == ';') { // && elementStack.isEmpty()) && searchingFor == 0) {
		if (start == end) {
		    end++;
		    pushSearchingFor('\n');
		} else {
		    // return before the comment and set up to revisit the ; next time
		    end--;
		    return code.substring(start, end+1);
		}
	    } else {
		end++;
	    }
	    previousCharacter = character;  
	}
	if (start != end) {
	    if (getSearchingFor() == '\n' && !includeComments) {
		return null;
	    } else {
		return code.substring(start, end);
	    }
	} else {
	    return null;
	}
    }
    
    private boolean pairedElements(String element) {
	// e.g. <textarea ...> ... </textarea>
	// TODO: determine if it is worth checking this and if its false what to do
	return true;
    }

    private boolean possiblePenultimateCloseTagCharacter(Character previousCharacter) {
	// true if can be before > in a closing tag
	return previousCharacter == '\\' || previousCharacter == '\'' || previousCharacter == '\"' || Character.isLetter(previousCharacter);
    }

    private boolean alphaNextCharacter(int index) {
	if (index+1 >= code.length()) {
	    return false;
	} else {
	    return Character.isLetter(code.charAt(index+1));
	}
    }

    private Character popSearchingFor() {
	return searchingForStack.remove(0);
    }

    private char getSearchingFor() {
	if (searchingForStack.isEmpty()) {
	    return 0;
	} else {
	    return searchingForStack.get(0);
	}
    }
    
    private char peekSearchingFor() {
	if (searchingForStack.size() < 2) {
	    return 0;
	} else {
	    return searchingForStack.get(1);
	}
    }
    
    private void pushSearchingFor(char character) {
	searchingForStack.add(0, character);
    }

    public void replaceCurrentToken(String replacement) {
	String before = code.substring(0, start);
	String after = code.substring(end);
	code = before + replacement + after;
	end = start+replacement.length(); //-1;
	start = end;
    }

    public String getCode() {
        return code;
    }

}
