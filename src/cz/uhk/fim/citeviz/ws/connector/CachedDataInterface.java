package cz.uhk.fim.citeviz.ws.connector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;
/**
 * 
 * @author ondra
 *
 */
public abstract class CachedDataInterface implements DataInterface {

	public int cacheSize = 3000;
	
	private boolean useCache = true;
	
	private List<Paper> papersCache;
	
	private List<Author> authorsCache;
	
	private Statement sqlStatement;
	
	public CachedDataInterface() {
		if (useCache){
			sqlStatement = getDatabaseConverter().initializeDatabazeIfNotExist();
			
			papersCache = new LinkedList<Paper>();
			authorsCache = new LinkedList<Author>();
		}
	}
	
	public boolean isCacheUsed() {
		return useCache;
	}
	
	@SuppressWarnings("unchecked")
	protected <C  extends IdRecord> List<C> getCache(Class<C> type) {
		if (Paper.class.isAssignableFrom(type)){
			return (List<C>) papersCache;
		} else if (Author.class.isAssignableFrom(type)) {
			return (List<C>) authorsCache;
		} else {
			throw new IllegalArgumentException("Unsupported cache for given type " + type.getSimpleName());
		}
	}
	
	protected DatabaseDriver getDatabaseConverter() {
		return new SqliteDatabaseDriver();
	}
	
	private void compactCache(List<? extends IdRecord> cache) {
		while (cache.size() > cacheSize){
			cache.remove(cache.size() - 1);
		}
	}
	
	
	protected <R extends IdRecord> R loadFromCache(Class<R> type, IdRecord id){
		if (!useCache){
			return null;
		}
		List<R> cache = getCache(type);
		
		Iterator<R> iter = cache.iterator();
		while (iter.hasNext()){
			R record = iter.next();
			if (record.getId().equals(id.getId())){
				iter.remove();
				cache.add(0, record);
				return record; 
			}
		}	
		
		R databaseItem = loadFromDatabaseCache(type, id);
		if (databaseItem != null){
			storeToCache(type, databaseItem, false);
		}
		return databaseItem;
	}
	
	@SuppressWarnings("unchecked")
	private <R extends IdRecord> R loadFromDatabaseCache(Class<R> type, IdRecord id) {
		String sql = "SELECT * FROM " + getDatabaseConverter().resolveTableName(type) + " WHERE id = '" + id.getId() + "' AND connector_name = '" + getConnectorName() + "'";
	
		try {
			sqlStatement.execute(sql);
			ResultSet result = sqlStatement.getResultSet();
			if (!result.isClosed() && result.getString("id") != null){
				R record = (R) getDatabaseConverter().databaseToObject(result, type);
				
				return record;
			}
		} catch (SQLException e) {
			System.err.println(sql);
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Error in read record from database cache!");
			e.printStackTrace();
		}
		
		return null;
	}

	protected synchronized <R extends IdRecord> void storeToCache(Class<R> type, R record){
		storeToCache(type, record, true);
	}
	
	private <R extends IdRecord> void storeToCache(Class<R> type, R record, boolean persist){
		List<R> cache = getCache(type);
		cache.add(0, record);
		compactCache(cache);
		if (persist){
			storeToDatabaseCache(record);
		}
	}
	
	private <R extends IdRecord> void storeToDatabaseCache(R record) {
		String sql = getDatabaseConverter().objectToSql(record, getConnectorName());
		try {
			sqlStatement.executeUpdate(sql);
		} catch (SQLException e){
			System.err.println(sql);
			e.printStackTrace();
		}
	}

	private String getConnectorName() {
		return getClass().getSimpleName();
	}
	
	@Override
	public Paper getPaperDetails(IdRecord id) {
		Set<IdRecord> ids = new HashSet<IdRecord>(1);
		ids.add(id);
		List<Paper> papers = getPaperDetails(ids);
		if (papers.size()>0)
			return papers.get(0);
		
		return null;
	}
	
	@Override
	public Author getAuthorDetails(IdRecord id) {
		Set<IdRecord> ids = new HashSet<IdRecord>(1);
		ids.add(id);
		List<Author> authors = getAuthorDetails(ids);
		if (authors.size()>0)
			return authors.get(0);
		
		return null;
	}

	@Override
	public void close() {
		try {
			sqlStatement.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	}	
}