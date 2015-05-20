package uk.ac.lkl.server;

import java.util.logging.Logger;


public class ReferencedMicroBehaviour extends MicroBehaviour {
    protected MicroBehaviour referringMicroBehaviour;
    
    public ReferencedMicroBehaviour(
	    String behaviourDescription, 
	    String url,
	    MicroBehaviour referringMicroBehaviour, 
	    ResourcePageServiceImpl resourcePageServiceImpl,
	    NetLogoModel netLogoModel) { 
	super(behaviourDescription, null, null, resourcePageServiceImpl, netLogoModel, null, null, null, null, true);
	if (behaviourDescription.startsWith("MB")) {
	    Logger.getLogger(ResourcePageServiceImpl.RESOURCE_SERVICE_LOGGER_NAME).severe(
		    "Probably a link converted to NetLogo and then treated as a link again: " + behaviourDescription);
	}
	this.referringMicroBehaviour = getUltimateReferringMicroBehaviour(referringMicroBehaviour);
	// no longer care about this kind of compatibility
//	if (url == null) {
//	    String referringURL = referringMicroBehaviour.getBehaviourURL();
	    // the following to maintain compatibility with BehaviourComposer 1.0
	    // where references were quoted and mapped exactly to the URL file name
//	    String referencedMicroBehaviourURL = referringURL.replace(referringMicroBehaviour.getBehaviourDescription(), 
//		                                                      this.getBehaviourDescription());
//	    if (referencedMicroBehaviourURL != referringURL) {
//		setBehaviourURL(referencedMicroBehaviourURL);
//	    }
//	    if (!CommonUtils.useJDO) {
//		try {
//		    Connection database = DatabaseConnection.connectToBehaviourComposerDataBase();
//		    databaseID = getMicroBehaviourID(database);
//		    database.close();
//		} catch (Exception e) {    
//		    ServerUtils.logException(e, "In new ReferencedMicroBehaviour ");
//		}
//	    }
//	} else {
	    setBehaviourURL(url);
//	}
    }
       
    protected MicroBehaviour getUltimateReferringMicroBehaviour(MicroBehaviour referringMicroBehaviour) {
	MicroBehaviour referringReferringMicroBehaviour = referringMicroBehaviour.getReferringMicroBehaviour();
	if (referringReferringMicroBehaviour == null) {
	    return referringMicroBehaviour;
	} else {
	    return getUltimateReferringMicroBehaviour(referringReferringMicroBehaviour);
	}
    }
    
    @Override
    public String getBehaviourCode() throws NetLogoException {
	if (behaviourCode == null) {
	    String url = getBehaviourURL();
	    int idCounters[] = {0, 0, 0};
	    ClientState clientState = netLogoModel.getClientState();
	    MicroBehaviour microBehaviour = 
		resourcePageServiceImpl.getMicroBehaviour(url, "", clientState, idCounters, null, true);
//	    long deltaTime = new Date().getTime() - startTime;
	    if (microBehaviour != null) {
		behaviourCode = microBehaviour.getBehaviourCode();
		textAreaValues = microBehaviour.getTextAreaValues();
		return behaviourCode;
	    } else {
		throw new NetLogoException("Error fetching " + url);
	    }
//	    if (page != null) {
//		if (page.startsWith("Error ")) {
//		    throw new Exception(page);
//		} else {
//		    int preStart = page.indexOf("<pre>");
//		    if (preStart < 0) {
//			preStart = page.indexOf("<PRE>");
//		    }
//		    if (preStart < 0) return ""; // warn?
//		    int preEnd = page.indexOf("</pre>", preStart);
//		    if (preEnd < 0) {
//			preEnd = page.indexOf("</PRE>", preStart);
//		    }
//		    if (preEnd < 0) return ""; // warn?
//		    setBehaviourCode(CommonUtils.removeHTMLMarkup(page.substring(preStart + "<pre>".length(), preEnd)));
//		    String finalBehaviourCode = super.getBehaviourCode();
//		    cachedURLToBehaviourCodeTable.put(getBehaviourURL(), finalBehaviourCode);
//		    int bytesJustCached = finalBehaviourCode.length();
//		    bytesCached += bytesJustCached;
//		    System.out.println("To getBehaviourCode of " + getBehaviourURL() + " took " + deltaTime +
//			               "ms. Cached increased by " + bytesJustCached + " bytes. Total is now " + bytesCached);
//		    return finalBehaviourCode;
//		}
//	    } else {
//		Modeller.addToErrorLog("Couldn't fetch the code for " + getBehaviourURL());
//	    }
	}
	return super.getBehaviourCode();
    }

    protected MicroBehaviour getReferringMicroBehaviour() {
        return referringMicroBehaviour;
    }

}
