/**
 * 
 */
package uk.ac.lkl.server;

import java.util.ArrayList;

/**
 * Collects the attributes referenced by micro-behaviours
 * 
 * @author Ken Kahn
 *
 */
public class AttributesCollector implements VariableCollector {
    
    private ArrayList<FoundAttribute> foundAttributes = new ArrayList<FoundAttribute>();
    
    private MicroBehaviour microBehaviour;

    public AttributesCollector() {
	super();
    }

    @Override
    public boolean addBreedVariable(String fullVariableName, boolean writing) {
	return addAttribute(fullVariableName, writing);
    }

    @Override
    public boolean addExpectedGlobalVariable(String fullVariableName, boolean writing) {
	return addAttribute(fullVariableName, writing);
    }

    @Override
    public boolean addExtraGlobalVariable(String fullVariableName, boolean declareIt, boolean writing) {
	return addAttribute(fullVariableName, writing);
    }

    @Override
    public boolean addPatchOrLinkVariable(String variableName, boolean patch, boolean writing) {
	return addAttribute(variableName, writing);
    }

    private boolean addAttribute(String variableName, boolean writing) {
	variableName = canonicaliseAttributeName(variableName);
	FoundAttribute previouslyFoundAttribute = null;
	for (FoundAttribute foundAttribute : foundAttributes) {
	    if (foundAttribute.getAttributeName().equalsIgnoreCase(variableName)) {
		previouslyFoundAttribute = foundAttribute;
		continue;
	    }
	}
	boolean isNew = previouslyFoundAttribute == null;
	if (isNew) {
	    previouslyFoundAttribute = new FoundAttribute(variableName);
	    foundAttributes.add(previouslyFoundAttribute);
	}
	if (writing) {
	    previouslyFoundAttribute.addWritingMicroBehaviour(microBehaviour);
	} else {
	    previouslyFoundAttribute.addReadingMicroBehaviour(microBehaviour);
	}	
	return isNew;
    }

    private String canonicaliseAttributeName(String variableName) {
	// consider my-next-xxx and my-xxx the same attribute
	if (variableName.startsWith("my-next-") && variableName.endsWith("-set")) {
	    variableName = variableName.substring(0, variableName.length()-4); // remove -set
	}
	return variableName.replace("my-next-", "my-");
    }

    public MicroBehaviour getMicroBehaviour() {
        return microBehaviour;
    }

    public void setMicroBehaviour(MicroBehaviour microBehaviour) {
        this.microBehaviour = microBehaviour;
    }

    public ArrayList<FoundAttribute> getFoundAttributes() {
        return foundAttributes;
    }

}
