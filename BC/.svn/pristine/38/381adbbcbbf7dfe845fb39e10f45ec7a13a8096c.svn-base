/**
 * 
 */
package uk.ac.lkl.server;

import java.sql.Timestamp;

/**
 * A class for generating the sequence of events in a session
 * 
 * @author Ken Kahn
 *
 */
public class EventXML implements Comparable<EventXML> {
    private Long time;
    private String xml;
    
    public EventXML(Timestamp timestamp, String xml) {
	// remove when JDO version fully operational
	this.xml = xml;
	this.time = timestamp.getTime();
    }
    
    public EventXML(long time, String xml) {
	this.xml = xml;
	this.time = time;
    }

    public int compareTo(EventXML other) {
	return getTime().compareTo(other.getTime());
    }

    public Long getTime() {
        return time;
    }

    public String getXML() {
        return xml;
    }


}
