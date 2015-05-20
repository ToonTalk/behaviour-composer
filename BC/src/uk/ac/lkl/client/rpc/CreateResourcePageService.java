package uk.ac.lkl.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class CreateResourcePageService {
    public static ResourcePageServiceAsync createResourcePageService() {
	// (1) Create the client proxy. Note that although you are creating the
	// service interface proper, you cast the result to the asynchronous
	// version of
	// the interface. The cast is always safe because the generated proxy
	// implements the asynchronous interface automatically.
	//
	ResourcePageServiceAsync resourcePageServiceService = 
	    (ResourcePageServiceAsync) GWT.create(ResourcePageService.class);
	// (2) Specify the URL at which our service implementation is running.
	// Note that the target URL must reside on the same domain and port from
	// which the host page was served.
	//
	ServiceDefTarget endpoint = (ServiceDefTarget) resourcePageServiceService;
	String moduleRelativeURL = GWT.getModuleBaseURL() + "ResourcePage";
//	String moduleRelativeURL = GWT.getHostPageBaseURL() + "Model/ResourcePage";
	endpoint.setServiceEntryPoint(moduleRelativeURL);
	return resourcePageServiceService;
    }
}
