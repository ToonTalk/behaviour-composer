/**
 * 
 */
package uk.ac.lkl.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 
 * This contains (and creates on demand) the PreparedStatement
 * Code should synchronize on these instances not the statements themselves
 * 
 * @author Ken Kahn
 *
 */


public class SharedPreparedStatement {
     
    private PreparedStatement statement = null;
    
    public SharedPreparedStatement() {
    }
    
    public PreparedStatement getStatement(String sql, Connection database) throws SQLException {
	if (statement == null || statement.getConnection() != database || statement.isClosed()) {
	    statement = database.prepareStatement(sql);
	}
	return statement;
    }
    
    public PreparedStatement getStatement(String sql, Connection database, int resultSetType, int resultSetConcurrency) 
       throws SQLException {
	if (statement == null || statement.getConnection() != database || statement.isClosed()) {
	    statement = database.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}
	return statement;
    }

}
