package uk.ac.lkl.server;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public class JDO {
    
    private static final PersistenceManagerFactory pmfInstance =
        JDOHelper.getPersistenceManagerFactory("transactions-optional");
    
    private JDO() {}

    public static PersistenceManagerFactory getPersistenceManagerFactory() {
        return pmfInstance;
    }
    
    public static PersistenceManager getPersistenceManager() {
	return pmfInstance.getPersistenceManager();
    }
}
