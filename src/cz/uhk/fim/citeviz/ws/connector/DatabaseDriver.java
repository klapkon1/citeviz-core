package cz.uhk.fim.citeviz.ws.connector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cz.uhk.fim.citeviz.model.IdRecord;

public interface DatabaseDriver {
	
	
	public String objectToSql(IdRecord element, String connector);
	
	public IdRecord databaseToObject(ResultSet result, Class<?> type) throws SQLException;

	public <R extends IdRecord> String resolveTableName(Class<R> type);
	
	public Statement initializeDatabazeIfNotExist();
}
