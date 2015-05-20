/**
 * 
 */
package uk.ac.lkl.server.persistent;

import uk.ac.lkl.server.BC2NetLogoChannels;
import uk.ac.lkl.server.GAEPassword;
import uk.ac.lkl.server.OpenSessionChannels;
import uk.ac.lkl.server.basicLTI.BLTIUser;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyOpts;

/** Based upon ObjectifyService
 * 
 * Consolidates class registration
 * 
 * @author Ken Kahn
 *
 */
public class DataStore {
    
    private static boolean classesRegistered = false;
    
    /** Singleton instance */
    protected static ObjectifyFactory factory = new ObjectifyFactory();

    /** Call this to get the instance */
    public static ObjectifyFactory factory() { 
	ensureClassesRegisteredWithObjectify();
	return factory;
    }

    public static void ensureClassesRegisteredWithObjectify() {
	if (!classesRegistered) {
	    registerClasses();
	}
    }

    //
    // All static methods simply pass-through to the singleton factory
    //

    /** @see ObjectifyFactory#begin() */
    public static Objectify begin() { return factory().begin(); }

    /** @see ObjectifyFactory#beginTransaction() */
    public static Objectify beginTransaction() { return factory().beginTransaction(); }

    /** @see ObjectifyFactory#begin(ObjectifyOpts) */
    public static Objectify begin(ObjectifyOpts opts) { return factory().begin(opts); }

    /** @see ObjectifyFactory#register(Class) */
    public static void register(Class<?> clazz) { factory().register(clazz); }
    
    private static void registerClasses() {
	classesRegistered = true;
	register(BLTIUser.class);
	register(GAEPassword.class);
	register(ModelXMLStringHash.class);
	register(ModelGuidCopy.class);
	register(MicroBehaviourDataHash.class);
	register(MicroBehaviourURLCopy.class);
	register(OpenSessionChannels.class);
	register(BC2NetLogoChannels.class);
	register(MicroBehaviourNetLogoName.class);
	register(NetLogoNameSerialNumber.class);
	register(ModelDifferences.class);
	register(SessionExperiments.class);
	register(BehaviourCode.class);
    }

}
