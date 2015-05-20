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

package uk.ac.lkl.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.lkl.server.persistent.DataStore;
import uk.ac.lkl.server.persistent.ModelDifferences;
import uk.ac.lkl.server.persistent.SessionExperiments;
import uk.ac.lkl.server.persistent.UserSession;
import uk.ac.lkl.shared.CommonUtils;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.Text;

public class NetLogoPostServlet extends HttpServlet {

    private static final long serialVersionUID = 48373567L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	    throws ServletException, IOException {
	ChannelService channelService = ChannelServiceFactory.getChannelService();
	String sessionGuid = request.getParameter("sessionGuid");
	String userGuid = request.getParameter("userGuid");
	String newSessionGuid = request.getParameter("newSessionGuid");
	String frozenModelGuid = request.getParameter("frozen");
	if (frozenModelGuid != null || newSessionGuid != null) {
	    String newUserGuid = request.getParameter("newUserGuid");
	    if (newSessionGuid == null) {
		newSessionGuid = ServerUtils.generateGUIDString();
	    }
	    channelService.sendMessage(new ChannelMessage(sessionGuid+userGuid, 
		    CommonUtils.UPDATE_URL + newUserGuid + " " + newSessionGuid + " " + frozenModelGuid));
	    PrintWriter writer = response.getWriter();
	    String newChannelToken = ServerUtils.channelToken(newSessionGuid);
	    writer.print(newChannelToken);
	} else {
	    String procedureDifferences = request.getParameter("procedureDifferences");
	    String declarationDifferences = request.getParameter("declarationDifferences");
	    String widgetDifferences = request.getParameter("widgetDifferences");
	    String infoTab = request.getParameter("infoTab");
	    String experiments = request.getParameter("experiments");
	    if (experiments != null) {
		SessionExperiments sessionExperiments = DataStore.begin().find(SessionExperiments.class, sessionGuid);
		if (sessionExperiments == null) {
		    sessionExperiments = new SessionExperiments(sessionGuid, experiments);
		} else {
		    sessionExperiments.setExperiments(experiments);
		}
		DataStore.begin().put(sessionExperiments);
	    }
	    if (infoTab != null) {
		PersistenceManager persistenceManager = JDO.getPersistenceManager();
		try {
		    UserSession session = persistenceManager.getObjectById(UserSession.class, sessionGuid);
		    session.setInfoTab(new Text(infoTab));
		    persistenceManager.makePersistent(session);
		} catch (JDOObjectNotFoundException e) {
		    // TODO:
		} finally {
		    persistenceManager.close();
		}
	    }
	    ModelDifferences modelDifferences = 
		    new ModelDifferences(sessionGuid, procedureDifferences, declarationDifferences, widgetDifferences, infoTab);
	    DataStore.begin().put(modelDifferences);
	    channelService.sendMessage(new ChannelMessage(sessionGuid+userGuid, CommonUtils.MICRO_BEHAVIOUR_UPDATES));
	}
	response.setStatus(HttpServletResponse.SC_OK);
    }

}
