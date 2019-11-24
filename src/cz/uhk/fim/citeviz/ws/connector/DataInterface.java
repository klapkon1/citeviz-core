package cz.uhk.fim.citeviz.ws.connector;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.uhk.fim.citeviz.model.Affiliation;
import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.AuthorCategorization;
import cz.uhk.fim.citeviz.model.DataType;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;
import cz.uhk.fim.citeviz.model.PaperCategorization;
import cz.uhk.fim.citeviz.model.PaperFullDetail;
/**
 * Rozhraní implementující metody pro pøístup k datùm
 * @author Ondøej Klapka
 *
 */
public interface DataInterface {
	public static final String SEARCH_KEY_AUTHOR_NAME = "name";
	public static final String SEARCH_KEY_YEAR = "date";
	public static final String SEARCH_KEY_TITLE = "title";
	public static final String SEARCH_KEY_ABSTRACT = "abstract";
	
	public PagedResult<IdRecord> searchPapers(String text, String key, int page, int pageSize);

	public PagedResult<IdRecord> searchAuthors(String text, String key, int page, int pageSize);
	
	public PagedResult<IdRecord> searchAffiliations(String text, String key, int page, int pageSize);

	public List<Paper> getPaperDetails(Set<IdRecord> ids);
	
	public List<Author> getAuthorDetails(Set<IdRecord> ids);
	
	public List<Affiliation> getAffiliationDetails(Set<IdRecord> ids);
	
	public Paper getPaperDetails(IdRecord id);
	
	public Author getAuthorDetails(IdRecord id);
	
	public PaperFullDetail getAllDataForPaper(IdRecord id);
	
	/**
	 * 0 means no limit
	 * @return
	 */
	public int getMaxIdsPerTransaction();
	
	/**
	 * operations for connection close
	 */
	public void close();
	
	public Map<AuthorCategorization, String> getAuthorCategorizationMapping();

	public Map<PaperCategorization, String> getPaperCategorizationMapping();
	
	public Set<DataType> getSupportedDataTypes();
}