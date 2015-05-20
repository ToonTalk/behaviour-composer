/**
 * 
 */
package uk.ac.lkl.server.basicLTI;

import java.util.Date;

import javax.persistence.Id;
import javax.servlet.http.HttpServletRequest;

import uk.ac.lkl.server.persistent.DataStore;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Indexed;

/**
 * Associates Basic LTI user ids with Behaviour Composer userKeys
 * 
 * @author Ken Kahn
 *
 */

@Cached
public class BLTIUser {
    @Id private String bLTIUserId;
    @Indexed private String userGuid;
    private String sessionGuid;
    private String roles;
    private String currentContextId;
    private String givenName;
    private String fullName;
    private String familyName;
    private String email;
    private Date firstUse;
    private Date mostRecentUse;

    BLTIUser(String bLTIUserId, String userGuid,  String sessionGuid, HttpServletRequest request) {
	this.bLTIUserId = bLTIUserId;
	this.userGuid = userGuid;
	this.sessionGuid = sessionGuid;
	recordRequestParameters(request);
	this.firstUse = this.mostRecentUse;
    }
    
    BLTIUser() {}; // for Objectify

    /**
     * @param request
     * Updates the fields with the parameters in the BLTI request
     */
    public void recordRequestParameters(HttpServletRequest request) {
	this.roles = request.getParameter("roles");
	this.currentContextId = request.getParameter("context_id");
	this.givenName = request.getParameter("lis_person_name_given");
	this.fullName = request.getParameter("lis_person_name_full");
	this.familyName = request.getParameter("lis_person_name_family");
	this.email = request.getParameter("lis_person_contact_email_primary");
	this.mostRecentUse = new Date();
    }
    
    static public BLTIUser getBLTIUser(String userGuid) {
	return DataStore.begin().query(BLTIUser.class).filter("userGuid", userGuid).get();
    }

    public String getUserGuid() {
        return userGuid;
    }

    public String getbLTIUserId() {
        return bLTIUserId;
    }

    public String getRoles() {
        return roles;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getEmail() {
        return email;
    }

    public String getCurrentContextId() {
        return currentContextId;
    }

    public Date getFirstUse() {
        return firstUse;
    }

    public Date getMostRecentUse() {
        return mostRecentUse;
    }

    public String getSessionGuid() {
        return sessionGuid;
    }

    public void setSessionGuid(String sessionGuid) {
        this.sessionGuid = sessionGuid;
    }
    
}
