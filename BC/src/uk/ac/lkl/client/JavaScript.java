package uk.ac.lkl.client;

import com.google.gwt.dom.client.Element;

public class JavaScript {
    
    //  switchToLibrary needs more work (and should be more general -- i.e. tab and bookmark)
    
//    public static native void exposeAPItoJavaScript() /*-{
//    	 $wnd.__browseLibrary = function(s) {
//    	   @uk.ac.lkl.client.MoPiX2::switchToLibrary(Ljava/lang/String;)(s);
//    	 };
//    }-*/;
    
//    public static native String getLocationHREF() /*-{
//    	return $wnd.locationHRefAtLoadTime;
//    }-*/;
    
    // see https://code.google.com/p/googleappengine/issues/detail?id=4940
    public static native void unloadGAEChannelIFrame() /*-{
	$('#wcs-iframe').remove();
    }-*/;

    // turn on or off whether to ask for confirmation before leaving web page
    public static native void confirmUnload(boolean flag) /*-{
          $wnd.confirmBeforeExit = flag;
    }-*/;
    
    public static native void setConfirmBeforeExitMessage(String message) /*-{
         $wnd.confirmBeforeExitMessage = message;
    }-*/;   
    
    // from http://code.google.com/p/google-web-toolkit/issues/detail?id=2152
    public static native void cleanAnchors()/*-{
	if(navigator.userAgent != null && navigator.userAgent.indexOf( "MSIE" ) == -1) return;
	var links = $doc.links;
	for(var i = 0;i < links.length;i++) {
		if(links[i].href.indexOf("#") > -1) {
			$doc.links[i].realhref = links[i].href.split("#")[1].split("?")[0];
			$doc.links[i].onclick = function() {
				@com.google.gwt.user.client.History::newItem(Ljava/lang/String;)(this.realhref);
				return true;
			};
		}
	}
     }-*/; 
    
    // from http://code.google.com/p/google-web-toolkit/issues/detail?id=1821
    public static native void killFrame(Element pElement) /*-{
	var element = pElement;
	element.onreadystatechange=function() {
		if (element.readyState=='complete') {
		    // following commented out since caused a null exception
//		    element.onreadystatechange=null;
		    element.outerHTML='';
		    element = null;
//		    alert("Frame killed");
		}
	}
	element.src='javascript:\'\'';
     }-*/;
    
    public static native void click(Element element) /*-{
        element.click();
    }-*/;
    
//    public static native JavaScriptObject getMoPiXElements(JavaScriptObject parent) /*-{
//        var mopixElements = new Array();
//	for (var i = 0; i < parent.childNodes.length; i++) {
//	$wnd.alert("i = " + i + " " + parent.childNodes[0].innerHTML);
//	$wnd.alert("flag: " + parent.childNodes[i].mopix);
//	if (parent.childNodes[i].mopix == undefined) {
//	$wnd.alert("not mopix");
//	} else {
//	$wnd.alert("is mopix");
//	};
//	if (parent.childNodes[i].mopix != undefined) {
//	    $wnd.alert("mopixElements is ");
//	    $wnd.alert(mopixElements);
//	    $wnd.alert("mopixElements is " + mopixElements.toString());
//	        mopixElements.push(parent.childNodes[i]);
//	    } else {
//	        $wnd.alert(getMoPiXElements(parent.childNodes[i]).length);
//	        mopixElements = getMoPiXElements(parent.childNodes[i]);
//	    };
//        };
//        return mopixElements;    
//    }-*/;
    
//    public static native JavaScriptObject getMoPiXElements(JavaScriptObject parent) /*-{
//	var elements = parent.getElementsByTagName("p");
//	var mopixElements = new Array();
//	for (var i = 0; i < elements.length; i++) {
//	    if (elements[i].mopix == '1') {
//	        mopixElements.push(elements[i]);
//	    }
//    }
//    return mopixElements;    
//}-*/;

//    public static native int getMoPiXElementCount(JavaScriptObject elements) /*-{
//	return elements.length;
//    }-*/;
//
//    public static native Element getMoPiXElement(int i, JavaScriptObject elements) /*-{
//        return elements[i];
//    }-*/;
       
    // following didn't help
    // see http://groups.google.com/group/Google-Web-Toolkit-Contributors/browse_thread/thread/97a74385c586a940/202d926efba1dcf1?lnk=gst&q=history#202d926efba1dcf1
    // following should stop the reload problem in IE
//    public static native void workaroundIEReloadProblem()  /*-{
//       $wnd.__gwt_historyToken = false;
//    }-*/;
}
