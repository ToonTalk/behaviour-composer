/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 * Copyright (c) 2008 IMS GLobal Learning Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. 
 *
 **********************************************************************************/

/*  Note:  This code was initially developed by 
    Chuck Wight at utah.edu and practicezone.org 
    This servlet file is adapted from an open-source Java servlet 
    LTIProviderServlet written by Charles Severance at imsglobal.org
@author Chuck Wight
*/

// obtained from http://code.google.com/p/ims-dev/source/browse/#svn%2Ftrunk%2Fbasiclti%2Fjava-appengine
// on 6 January 2012 and edited by Ken Kahn

package uk.ac.lkl.server.basicLTI;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import uk.ac.lkl.server.ServerUtils;
import uk.ac.lkl.server.oauth.OAuthAccessor;
import uk.ac.lkl.server.oauth.OAuthConsumer;
import uk.ac.lkl.server.oauth.OAuthMessage;
import uk.ac.lkl.server.oauth.OAuthValidator;
import uk.ac.lkl.server.oauth.SimpleOAuthValidator;
import uk.ac.lkl.server.oauth.server.OAuthServlet;
import uk.ac.lkl.server.oauth.signature.OAuthSignatureMethod;
import uk.ac.lkl.server.persistent.DataStore;

public class BLTIToolProviderServlet extends HttpServlet {

private static final String BASIC_LTI_SERVICE = "Basic LTI Service";
private static final long serialVersionUID = 48373566L;

static {
    DataStore.register(BLTIConsumer.class);
    // might have already been registered by another servlet
    try {
	DataStore.factory().getMetadata(BLTIUser.class);
    } catch (IllegalArgumentException e) {
	DataStore.register(BLTIUser.class);
    }
}

@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
throws ServletException, IOException 
{
	doPost(request, response);
}

@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) 
throws ServletException, IOException 
{
	String ipAddress = request.getRemoteAddr();
	System.out.println("Basic LTI Provider request from IP="+ipAddress);
	// required 
	String oauth_consumer_key = request.getParameter("oauth_consumer_key");
	if (oauth_consumer_key == null || oauth_consumer_key.isEmpty()) {
	    doError(request, response, "oauth_consumer_key required parameter is missing from the Basic LTI launch post. Launch the Behaviour Composer directly in the browser from modelling4all.org", null);
	    return;
	}
	// Lookup the secret that corresponds to the oauth_consumer_key in the AppEngine datastore
	// moved here to generate a secret if there is none
	String oauth_secret = BLTIConsumer.getSecret(oauth_consumer_key);
	if (oauth_secret == null) {
	    Logger.getLogger(BASIC_LTI_SERVICE).warning("No secret created yet with " + oauth_consumer_key);
	    BLTIConsumer.create(oauth_consumer_key);
	    oauth_secret = BLTIConsumer.getSecret(oauth_consumer_key);
	    Logger.getLogger(BASIC_LTI_SERVICE).warning("New secret is " + oauth_secret);
	}
	String resource_link_id = request.getParameter("resource_link_id");
	if ( ! "basic-lti-launch-request".equals(request.getParameter("lti_message_type")) ||
			! "LTI-1p0".equals(request.getParameter("lti_version")) ||
			oauth_consumer_key == null || resource_link_id == null ) {
		doError(request, response, "Missing required parameter from Basic LIT. Launch the Behaviour Composer directly in the browser from modelling4all.org", null);
		return;
	}
	OAuthMessage oam = OAuthServlet.getMessage(request, null);
	OAuthValidator oav = new SimpleOAuthValidator();
	OAuthConsumer cons = new OAuthConsumer("about:blank#OAuth+CallBack+NotUsed",oauth_consumer_key, oauth_secret, null);
	OAuthAccessor acc = new OAuthAccessor(cons);

	String base_string = null;
	try {
		base_string = OAuthSignatureMethod.getBaseString(oam);
	} catch (Exception e) {
		base_string = null;
	}
	
	try {
		oav.validateMessage(oam,acc);
	} catch(Exception e) {
		System.out.println("Provider failed to validate message");
		System.out.println(e.getMessage());
		if ( base_string != null ) System.out.println(base_string);
		doError(request, response,"Basic LTI launch data does not validate. Launch the Behaviour Composer directly in the browser from modelling4all.org", null);
		return;
	}
	// BLTI Launch message was validated successfully. 
	
	// Store the request parameters name/value pairs in the user's web session
	// In a real application these values would be used to authenticate the user and provision an account
	
	HttpSession session = request.getSession();
	Enumeration<?> names = request.getParameterNames();
	while (names.hasMoreElements()) {
		String n = (String) names.nextElement();
//		System.err.println("Parameter name: " + n + "; value: " + request.getParameter(n));
		session.setAttribute(n, request.getParameter(n));
	}
	String bltiUserId = request.getParameter("user_id");
	String givenName = request.getParameter("lis_person_name_given");
	// other possible parameters: lis_person_contact_email_primary,  lis_person_name_full,  lis_person_name_family
	BLTIUser user = DataStore.begin().find(BLTIUser.class, bltiUserId);
	boolean newUser = user == null;
	if (newUser) {
	    user = new BLTIUser(bltiUserId, ServerUtils.generateGUIDString(), ServerUtils.generateGUIDString(), request);
	    DataStore.begin().put(user);
	} else {
	    // update all the fields with the request parameters
	    // at least context may have changed (but so might name or roles)
	    user.recordRequestParameters(request);
	    if (user.getSessionGuid() == null) {
		// in case an old record before sessionGuid was added
		user.setSessionGuid(ServerUtils.generateGUIDString());
	    }
	    DataStore.begin().put(user);
	}
	String userGuid = user.getUserGuid();
	String newURL = "http://m.modelling4all.org/m/?user=" + userGuid + 
		                                     "&contextId=" + request.getParameter("context_id") +
			                             "&givenName=" + givenName;
	String newSession = request.getParameter("custom_newsession");
	if (newSession != null && newSession.equals("1")) {
	    newURL += "&share=new";
	} else {
	    newURL += "&warnIfUnknownSessionId=0";
	    newURL += "&share=" + user.getSessionGuid();
	}
	String resources = request.getParameter("custom_resources");
	if (resources != null) {
	    newURL += "&resources=" + resources;
	}
	String tab = request.getParameter("custom_tab");
	if (tab != null) {
	    // will start on tab
	    newURL += "&tab=" + tab;
	} else if (newUser) {
	    newURL += "&start=help";
	} else {
	    newURL += "&start=models";
	}
	Logger.getLogger(BASIC_LTI_SERVICE).warning("Basic LTI redirecting to URL: " + newURL);
	response.sendRedirect(newURL);
}

public void doError(HttpServletRequest request, HttpServletResponse response, 
		 String message, Exception e)
throws java.io.IOException
{
	String return_url = request.getParameter("launch_presentation_return_url");
	if ( return_url != null && return_url.length() > 1 ) {
		if ( return_url.indexOf('?') > 1 ) {
			return_url += "&lti_msg=" + message;
		} else {
			return_url += "?lti_msg=" + message;
		}
		response.sendRedirect(return_url);
		return;
	}
	response.getWriter().println(message);
}

@Override
public void destroy() {

}

}
