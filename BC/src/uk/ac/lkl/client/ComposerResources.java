/**
 * 
 */
package uk.ac.lkl.client;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.TextResource;

/**
 * @author Ken Kahn
 *
 */
public interface ComposerResources extends Images {
    
    // Behaviour Composer images and other resources:
    
    @Source("uk/ac/lkl/client/images/m4a-favicon-large.png")
    ImageResource modelling4AllIcon();
    
    @Source("uk/ac/lkl/client/images/microBehaviourTemplate.html")
    TextResource microBehaviourTemplate();
    
    @Source("uk/ac/lkl/client/images/resourceTemplate.html")
    TextResource resourceTemplate();

}
