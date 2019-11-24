package cz.uhk.fim.citeviz.ws.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.AuthorFullDetail;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;
import cz.uhk.fim.citeviz.model.PaperFullDetail;
import cz.uhk.fim.citeviz.util.CiteVizUtils;

public class SqliteDatabaseDriver implements DatabaseDriver {
	
	private static final String MAP_SEPARATOR = "->";
	private static final String ITEM_SEPARATOR = "|";
	private static final String ITEM_PART_SEPARATOR = "#";
	
	@Override
	public String objectToSql(IdRecord element, String connector) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		
		if (element instanceof Paper){
			Paper paper = (Paper) element;
			
			StringBuilder builder = new StringBuilder();
			builder.append("INSERT INTO papers VALUES ('");
			builder.append(paper.getId());
			builder.append("', '");
			builder.append(connector);
			builder.append("', '");
			builder.append(escapeString(paper.getTitle()));
			builder.append("', '");
			builder.append(paper.getCitationIndex());
			builder.append("', '");
			builder.append(serializeAuthors(paper.getAuthors()));
			builder.append("', '");
			builder.append(serializeIds(paper.getParents()));
			builder.append("', '");
			builder.append(serializeIds(paper.getChilds()));
			builder.append("', '");
			builder.append(paper.getYear());
			builder.append("',");
			
			if (element instanceof PaperFullDetail){
				PaperFullDetail paperFullDetail = (PaperFullDetail) element;
				
				builder.append("'");
				builder.append(serializeOtherData(paperFullDetail.getOtherData()));
				builder.append("',");
				
				String paperAbstract = paperFullDetail.getOtherDataSingleValue(PaperFullDetail.ABSTRACT_KEY);
				if (paperAbstract != null){
					builder.append("'");
					builder.append(escapeString(paperAbstract));
					builder.append("'");
				} else {
					builder.append("null");
				}
			} else {
				builder.append("null, null");
			}
			
			builder.append(", '");
			builder.append(dateFormat.format(new Date()));
			builder.append("');");
			return builder.toString();
		} else if (element instanceof Author){
			Author author = (Author) element;
			
			StringBuilder builder = new StringBuilder();
			builder.append("INSERT INTO authors VALUES ('");
			builder.append(author.getId());
			builder.append("', '");
			builder.append(connector);
			builder.append("', '");
			builder.append(escapeString(author.getName()));
			builder.append("', '");
			builder.append(author.getCitationIndex());
			builder.append("', '");
			builder.append(serializeIds(author.getParents()));
			builder.append("', '");
			builder.append(serializeIds(author.getChilds()));
			builder.append("', '");
			builder.append(serializeIds(author.getPapersId()));
			builder.append("', '");
			builder.append(serializeIds(author.getCollaborators()));
			builder.append("',");
			
			if (element instanceof AuthorFullDetail){
				builder.append("'");
				builder.append(serializeOtherData(((AuthorFullDetail) element).getOtherData()));
				builder.append("'");
			} else {
				builder.append("null");
			}
			
			builder.append(", '");
			builder.append(dateFormat.format(new Date()));
			builder.append("');");
			return builder.toString();
		} else {
			throw new IllegalArgumentException("Unsupported type " + (element != null ? element.getClass().getSimpleName() : "null"));
		}
	}
	
	private String serializeIds(Set<IdRecord> ids){
		StringBuilder builder = new StringBuilder();
		
		for (IdRecord id : ids) {
			if (builder.length() > 0){
				builder.append(ITEM_SEPARATOR);
			}
			builder.append(id.getId());
		}
		
		return builder.toString();
	}
	
	private String serializeAuthors(Set<Author> authors) {
		StringBuilder builder = new StringBuilder();
		for (Author author : authors) {
			if (builder.length() > 0){
				builder.append(ITEM_SEPARATOR);
			}
			
			builder.append(author.getId());
			builder.append(MAP_SEPARATOR);
			builder.append(escapeString(author.getName()));
		}
		return builder.toString();
	}
	
	private String serializeOtherData(Map<String, Set<String>> data) {
		StringBuilder builder = new StringBuilder();
		for (Entry<String, Set<String>> item : data.entrySet()) {
			if (PaperFullDetail.ABSTRACT_KEY.equals(item.getKey())){
				continue;
			}
			
			if (builder.length() > 0){
				builder.append(ITEM_SEPARATOR);
			}
		
			builder.append(item.getKey());
			builder.append(MAP_SEPARATOR);
			
			boolean firstItemPart = true;
			for (String itemPart : item.getValue()) {
				if (!firstItemPart){
					builder.append(ITEM_PART_SEPARATOR);
				} else {
					firstItemPart = false;
				}
				
				builder.append(escapeString(itemPart));
			}
			
		}
		return builder.toString();
	}
	
	private static String escapeString(String string){
		if (string == null){
			return null;
		} 
		
		return string.replace("'", "''");
	}

	@Override
	public IdRecord databaseToObject(ResultSet result, Class<?> type) throws SQLException {
		if (Paper.class.isAssignableFrom(type)){
			
			Paper paper = null;
			
			if (result.getString("other_data") != null){
				
				Map<String, Set<String>> otherData = deserializeOtherData(result.getString("other_data"));
				
				if (result.getString("abstract") != null){
					otherData.put(PaperFullDetail.ABSTRACT_KEY, CiteVizUtils.asSet(result.getString("abstract")));
				}
				
				paper = new PaperFullDetail(
						result.getString("id"), 
						result.getString("title"), 
						deserializeAuthors(result.getString("authors")), 
						result.getInt("year"), 
						result.getInt("citation_index"),
						otherData
						);
			} else {
				 paper = new Paper(
						result.getString("id"), 
						result.getString("title"), 
						deserializeAuthors(result.getString("authors")), 
						result.getInt("year"), 
						result.getInt("citation_index"));
			}
			
			
			paper.getParents().addAll(deserializeIds(result.getString("parents")));
			paper.getChilds().addAll(deserializeIds(result.getString("childs")));
			
			
			return paper;
		} else if (Author.class.isAssignableFrom(type)){
			Author author = null;
			
			if (result.getString("other_data") != null){
				author = new AuthorFullDetail(
						result.getString("id"), 
						result.getString("name"), 
						result.getInt("citation_index"),
						deserializeOtherData(result.getString("other_data")));
			} else {
				author = new Author(
						result.getString("id"), 
						result.getString("name"), 
						result.getInt("citation_index"));
			}
			
			
			author.getParents().addAll(deserializeIds(result.getString("parents")));
			author.getChilds().addAll(deserializeIds(result.getString("childs")));
			author.getCollaborators().addAll(deserializeIds(result.getString("collaborators")));
			author.getPapersId().addAll(deserializeIds(result.getString("papers_id")));
			
			
			return author;
		} else {
			throw new IllegalArgumentException("Unsupported conversion from SQL to object for given type: " + type.getSimpleName());
		}
	}

	private Set<IdRecord> deserializeIds(String ids){
		Set<IdRecord> result = new HashSet<>();
		
		if (ids != null && !ids.isEmpty()){
			StringTokenizer tokenizer = new StringTokenizer(ids, ITEM_SEPARATOR);
			
			while (tokenizer.hasMoreTokens()) {
				String id = tokenizer.nextToken();
				result.add(new IdRecord(id));
			}
		}
		
		return result;
	}

	private Set<Author> deserializeAuthors(String authors) {
		Set<Author> result = new HashSet<>();
		
		if (authors != null && !authors.isEmpty()){
			StringTokenizer tokenizer = new StringTokenizer(authors, ITEM_SEPARATOR);
			
			while (tokenizer.hasMoreTokens()) {
				String authorVals = tokenizer.nextToken();
				String[] splitedAuthorVals = authorVals.split(MAP_SEPARATOR);
				
				if (splitedAuthorVals.length != 2){
					throw new IllegalArgumentException("Unexpected count of items in author map: " + authorVals + ", expected 2 items, but got: " + splitedAuthorVals.length);
				}
				
				result.add(new Author(splitedAuthorVals[0], splitedAuthorVals[1]));
			}
		}
		
		return result;
	}
	
	private Map<String, Set<String>> deserializeOtherData(String data) {
		Map<String, Set<String>> result = new HashMap<>();
		
		if (data != null && !data.isEmpty()){
			StringTokenizer tokenizer = new StringTokenizer(data, ITEM_SEPARATOR);
			
			while (tokenizer.hasMoreTokens()) {
				String dataVals = tokenizer.nextToken();
				String[] splitedDataVals = dataVals.split(MAP_SEPARATOR);
				
				if (splitedDataVals.length != 2){
					throw new IllegalArgumentException("Unexpected count of items in data map: " + dataVals + ", expected 2 items, but got: " + splitedDataVals.length);
				}
				
			
				StringTokenizer dataPartTokenizer = new StringTokenizer(splitedDataVals[1], ITEM_PART_SEPARATOR);
				Set<String> dataSet = new HashSet<String>();
				while (dataPartTokenizer.hasMoreElements()){
					dataSet.add(dataPartTokenizer.nextToken());
				}
				
				result.put(splitedDataVals[0], dataSet);
			}
		}
		
		return result;
	}

	@Override
	public <R extends IdRecord> String resolveTableName(Class<R> type) {
		if (Paper.class.isAssignableFrom(type)){
			return "papers";
		} else if (Author.class.isAssignableFrom(type)){
			return "authors";
		} else {
			throw new IllegalArgumentException("Unsupported type " + type.getSimpleName());
		}
	}
	
	public Statement initializeDatabazeIfNotExist() {
		try {
			Connection connection = DriverManager.getConnection("jdbc:sqlite:citeviz.db");
			Statement statement = connection.createStatement();
			StringBuilder builder = new StringBuilder();

			// PAPERS
			builder.append("CREATE TABLE IF NOT EXISTS ");
			builder.append(" papers");
			builder.append(" (id TEXT,");
			builder.append(" connector_name TEXT,");
			builder.append(" title TEXT,");
			builder.append(" citation_index INTEGER,");
			builder.append(" authors TEXT,");
			builder.append(" parents TEXT,");
			builder.append(" childs TEXT,");
			builder.append(" year INTEGER,");
			builder.append(" other_data TEXT,");
			builder.append(" abstract TEXT,");
			builder.append(" saved DATE,");
			builder.append(" PRIMARY KEY (id, connector_name))");
			statement.executeUpdate(builder.toString());

			// AUTHORS
			builder = new StringBuilder();
			builder.append("CREATE TABLE IF NOT EXISTS ");
			builder.append(" authors");
			builder.append(" (id TEXT,");
			builder.append(" connector_name TEXT,");
			builder.append(" name TEXT,");
			builder.append(" citation_index INTEGER,");
			builder.append(" parents TEXT,");
			builder.append(" childs TEXT,");
			builder.append(" papers_id TEXT,");
			builder.append(" collaborators TEXT,");
			builder.append(" other_data TEXT,");
			builder.append(" saved DATE,");
			builder.append(" PRIMARY KEY (id, connector_name))");
			statement.executeUpdate(builder.toString());
			return statement;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}