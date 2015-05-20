/**
 * 
 */
package uk.ac.lkl.client.composer;

import uk.ac.lkl.client.Modeller;

/**
 * The set of scheduling enhancements available for micro-behaviours
 * 
 * @author Ken Kahn
 *
 */

public enum MicroBehaviourEnhancement {
    DO_EVERY,
    DO_AFTER,
    DO_AT_TIME,
    DO_WITH_PROBABILITY,
    DO_IF,
    DO_WHEN,
    DO_WHENEVER,
    ADD_VARIABLE,
    ADD_COMMENT;

    public static MicroBehaviourEnhancement getEnhancement(Integer enhancementIndex) {
	if (enhancementIndex < values().length) {
	    return values()[enhancementIndex];
	}
	return null;
    }
    
    public static String getAdditionalDescription(MicroBehaviourEnhancement enhancement) {
	switch (enhancement) {
	case DO_EVERY:
	    return Modeller.constants.doEveryDescription();
	case DO_AFTER:
	    return Modeller.constants.doAfterDescription();
	case DO_AT_TIME:
	    return Modeller.constants.doAtTimeDescription();
	case DO_WITH_PROBABILITY:
	    return Modeller.constants.doWithProbabilityDescription();
	case DO_IF:
	    return Modeller.constants.doIfDescription();
	case DO_WHEN:
	    return Modeller.constants.doWhenDescription();
	case DO_WHENEVER:
	    return Modeller.constants.doWheneverDescription();
	case ADD_VARIABLE:
	    return Modeller.constants.addVariableDescription();
	case ADD_COMMENT:
	    return Modeller.constants.addCommentDescription();
	}
	return null;
    }
    
    public static String getRemovalDescription(MicroBehaviourEnhancement enhancement) {
	switch (enhancement) {
	case DO_EVERY:
	    return Modeller.constants.doEveryRemoved();
	case DO_AFTER:
	    return Modeller.constants.doAfterRemoved();
	case DO_AT_TIME:
	    return Modeller.constants.doAtTimeRemoved();
	case DO_WITH_PROBABILITY:
	    return Modeller.constants.doWithProbabilityRemoved();
	case DO_IF:
	    return Modeller.constants.doIfRemoved();
	case DO_WHEN:
	    return Modeller.constants.doWhenRemoved();
	case DO_WHENEVER:
	    return Modeller.constants.doWheneverRemoved();
	case ADD_VARIABLE:
	    return Modeller.constants.addVariableRemoved();
	case ADD_COMMENT:
	    return Modeller.constants.addCommentRemoved();
	}
	return null;
    }

}
