package uk.ac.lkl.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class CreateMathDiLSService {
    // static int fileCategories[] = {37}; // MoPiX files
    // static String languages[] ={"English"}; // for now
    // static String authors[] = {"KenKahn"};
    // emailAddress = email;
    public static MathDiLSServiceAsync createMathDiLSService() {
	// (1) Create the client proxy. Note that although you are creating the
	// service interface proper, you cast the result to the asynchronous
	// version of
	// the interface. The cast is always safe because the generated proxy
	// implements the asynchronous interface automatically.
	//
	MathDiLSServiceAsync MathDiLSService = (MathDiLSServiceAsync) GWT.create(MathDiLSService.class);
	// (2) Specify the URL at which our service implementation is running.
	// Note that the target URL must reside on the same domain and port from
	// which the host page was served.
	//
	ServiceDefTarget endpoint = (ServiceDefTarget) MathDiLSService;
	String moduleRelativeURL = GWT.getModuleBaseURL() + "MathDiLS";
	endpoint.setServiceEntryPoint(moduleRelativeURL);
	return MathDiLSService;
    }
}
