package uk.ac.lkl.server.persistent;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import uk.ac.lkl.server.ServerUtils;

import com.google.appengine.api.datastore.Blob;


@PersistenceCapable
public class URLContents {
    
    @PrimaryKey
    private String url;
    @Persistent
    private Blob contents;
    
    public URLContents(String url, String contents) {
	this(url, new Blob(ensureNotTooBig(contents, url).getBytes()));
    }
    
    private static String ensureNotTooBig(String contents, String url) {
	if (contents.length() > 1000000) {
	    ServerUtils.logError("Contents of URL: " + url + " exceeds Google App Engine limit of 1,000,000 bytes. Truncating it.");
	    return contents.substring(0, 999999);
	} else {
	    return contents;
	}
    }

    public URLContents(String url, Blob contents) {
	this.url = url;
	this.contents = contents;
    }

    public String getUrl() {
        return url;
    }

    public String getContents() {
        return new String(contents.getBytes());
    }

}
