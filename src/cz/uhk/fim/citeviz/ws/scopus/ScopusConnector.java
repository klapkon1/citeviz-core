package cz.uhk.fim.citeviz.ws.scopus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cz.uhk.fim.citeviz.async.AsyncTask;
import cz.uhk.fim.citeviz.async.TaskManager;
import cz.uhk.fim.citeviz.model.Affiliation;
import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.AuthorCategorization;
import cz.uhk.fim.citeviz.model.AuthorFullDetail;
import cz.uhk.fim.citeviz.model.DataType;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;
import cz.uhk.fim.citeviz.model.PaperCategorization;
import cz.uhk.fim.citeviz.model.PaperFullDetail;
import cz.uhk.fim.citeviz.ws.connector.CachedDataInterface;
import cz.uhk.fim.citeviz.ws.connector.DataInterfaceErrorHandler;
import cz.uhk.fim.citeviz.ws.connector.PagedResult;

public class ScopusConnector extends CachedDataInterface {

	private static final int MAX_PARALLEL_QUERIES = 15;

	private static final String INTERFACE_URL = "https://api.elsevier.com/content/";

	private static final String API_KEY = "34265cf899847b73503158449a7cd0e3";

	private static final Map<String, String> keyMapping = new HashMap<String, String>();

	static {
		keyMapping.put(SEARCH_KEY_TITLE, "title");
	}

	@Override
	public PagedResult<IdRecord> searchPapers(String text, String key, int page, int pageSize) {
		Map<String, String> urlParams = new HashMap<String, String>();
		//urlParams.put("query", "AFFIL(university of hradec kralove)");
		urlParams.put("query", text);
		urlParams.put("field", "eid");
		urlParams.put("count", String.valueOf(pageSize));
		urlParams.put("start", String.valueOf(pageSize * page));
		
		urlParams.put("sort", "citedby-count");
		
		JsonObject jsonObject = null;
		try {
			jsonObject = getJsonObject("search/scopus", urlParams);
			
		} catch (Exception e){
			DataInterfaceErrorHandler.handleError(e);
		}
		
		return makePagedResult(ScopusJsonToObjectConverter.convertPaperIds(jsonObject), jsonObject, page);
	}

	private PagedResult<IdRecord> makePagedResult(List<IdRecord> parsedResult, JsonObject replyObject, int page) {
		return new PagedResult<>(parsedResult, page, 
				replyObject.getAsJsonObject("search-results").get("opensearch:itemsPerPage").getAsInt(), 
				replyObject.getAsJsonObject("search-results").get("opensearch:totalResults").getAsInt());
	}

	@Override
	public PagedResult<IdRecord> searchAuthors(String text, String key, int page, int pageSize) {
		Map<String, String> urlParams = new HashMap<String, String>();
		urlParams.put("query", "AUTHLASTNAME(" + text + ")");
		
		//urlParams.put("query", "AFFIL(university hradec kralove)");
		
		 
		urlParams.put("field", "eid,preferred-name,affiliation-current,subject-area");
		urlParams.put("count", String.valueOf(pageSize));
		urlParams.put("start", String.valueOf(pageSize * page));
		

		JsonObject jsonObject = null;
		try {
			jsonObject = getJsonObject("search/author", urlParams);
		} catch (Exception e){
			DataInterfaceErrorHandler.handleError(e);
		}
		
		List<IdRecord> authors = ScopusJsonToObjectConverter.convertAuthors(jsonObject);
//		for (IdRecord author : authors) {
//			if (author instanceof ScopusAuthor){
//				storeToCache(Author.class, (ScopusAuthor)author, false);
//			}
//		}
		return makePagedResult(authors, jsonObject, page);
	}

	@Override
	public List<Paper> getPaperDetails(Set<IdRecord> ids) {
		List<Paper> result = new ArrayList<Paper>(ids.size());
		Set<IdRecord> idsForDlownload = new HashSet<IdRecord>();

		Iterator<IdRecord> idsIterator = ids.iterator();
		while (idsIterator.hasNext()) {
			IdRecord id = idsIterator.next();
			Paper paper = loadFromCache(Paper.class, id);

			if (paper != null) {
				result.add(paper);
			} else {
				idsForDlownload.add(id);
			}
		}

		String groupId = TaskManager.getInstance().generateGroupId();

		
		
		while (!idsForDlownload.isEmpty() || TaskManager.getInstance().isGroupRunnig(groupId)) {
			if (TaskManager.getInstance().getRunningTasksInGroup(groupId).size() < MAX_PARALLEL_QUERIES && !idsForDlownload.isEmpty()) {
				idsIterator = idsForDlownload.iterator();
				IdRecord id = idsIterator.next();
				idsIterator.remove();
				TaskManager.getInstance().startTask(
						new AsyncTask<PaperFullDetail>() {
						
							@Override
							protected PaperFullDetail runTask() throws Throwable{
								Map<String, String> urlParams = new HashMap<String, String>();
	
								//ALL PAPER DATA WITHOUT CITATIONS
								urlParams.put("field", "title,eid,author,affiliation,url,coverDate,doi,publicationName,description,citedby-count,tail,aggregationType,subject-area,authkeywords");
								urlParams.put("httpAccept", "application/json");
								JsonObject jsonPaper = getJsonObject("abstract/eid/" + id.getId(), urlParams);
	
								//CITATIONS FOR PAPER
								List<JsonObject> jsonChilds = new ArrayList<JsonObject>();
								int itemsDownloaded = 0;
								int total = 0;
								boolean firstIteration = true;
								while (itemsDownloaded < total || firstIteration){
									firstIteration = false;
									urlParams.clear();
									urlParams.put("query", "ref(" + id.getId() + ")");
									urlParams.put("field", "eid");
									urlParams.put("count", "200");
									urlParams.put("start", String.valueOf(itemsDownloaded));
									
									try {
										JsonObject replyItem = getJsonObject("search/scopus/", urlParams);
										
										itemsDownloaded += replyItem.getAsJsonObject("search-results").get("opensearch:itemsPerPage").getAsInt(); 
										total = replyItem.getAsJsonObject("search-results").get("opensearch:totalResults").getAsInt();
										
										jsonChilds.add(replyItem);
									} catch (Throwable e) {
										//Scopus sometimes returns absurd number of total records, but these records not exist in Scopus - why, who know :-(
										itemsDownloaded = total;
										DataInterfaceErrorHandler.storeError(e);
									}
									
									
								}
								
	
								return ScopusJsonToObjectConverter.convertPaper(jsonPaper, jsonChilds);
							}

							@Override
							protected void onFinish(PaperFullDetail paper) {
								if (paper != null) {
									result.add(paper);
									storeToCache(Paper.class, paper);
								}
							}
							
							@Override
							protected void onError(Throwable e) {
								DataInterfaceErrorHandler.storeError(e);
							}
							
							@Override
							protected Object getLockObject() {
								return this;
							}
						}, groupId);
			}
		}
		return result;
	}

	@Override
	public List<Author> getAuthorDetails(Set<IdRecord> ids) {
		List<Author> result = new ArrayList<Author>(ids.size());
		Set<IdRecord> idsForDlownload = new HashSet<IdRecord>();

		Iterator<IdRecord> idsIterator = ids.iterator();
		while (idsIterator.hasNext()) {
			IdRecord id = idsIterator.next();
			Author author = loadFromCache(Author.class, id);

			if (author != null && (!(author instanceof ScopusAuthor) || ((author instanceof ScopusAuthor) && ((ScopusAuthor) author).isCompleted()))) {
				result.add(author);
			} else {
				idsForDlownload.add(id);
			}
		}
		
		String groupId = TaskManager.getInstance().generateGroupId();

		while (!idsForDlownload.isEmpty() || TaskManager.getInstance().isGroupRunnig(groupId)) {
			if (TaskManager.getInstance().getRunningTasksInGroup(groupId).size() < MAX_PARALLEL_QUERIES && !idsForDlownload.isEmpty()) {
				idsIterator = idsForDlownload.iterator();
				IdRecord id = idsIterator.next();
				idsIterator.remove();
				TaskManager.getInstance().startTask(
						new AsyncTask<ScopusAuthor>() {

							@Override
							protected ScopusAuthor runTask() throws Throwable{
								ScopusAuthor loadedAuthor = (ScopusAuthor)loadFromCache(Author.class, id);
								JsonObject jsonAuthor = null;
								Map<String, String> urlParams = new HashMap<String, String>();
									
								String authId = id.getId().substring(id.getId().lastIndexOf("-") + 1);
									
								//BASIC DATA OF AUTHOR
								if (loadedAuthor == null){
									urlParams.put("query", "AU-ID(" + authId + ")");
									urlParams.put("field", "eid,preferred-name,affiliation-current,subject-area");
									jsonAuthor = getJsonObject("search/author/", urlParams);
								}
									
								//COLLABORATORS
								urlParams.clear();
								urlParams.put("co-author", authId);
								urlParams.put("field", "eid");
								JsonObject jsonCollaborators = getJsonObject("search/author/", urlParams);
									
								//PUBLISHED PAPERS
								urlParams.clear();
								urlParams.put("query",  "AU-ID(" + authId + ")");
								urlParams.put("field", "eid");
								JsonObject jsonPapers = getJsonObject("search/scopus", urlParams);
								
								//PARENTS & CHILDS
								List<JsonObject> jsonParents = new ArrayList<JsonObject>();
								List<JsonObject> jsonChilds = new ArrayList<JsonObject>();
								for (IdRecord idRecord : ScopusJsonToObjectConverter.convertPaperIds(jsonPapers)) {
									urlParams.clear();
									urlParams.put("view", "REF");
									urlParams.put("httpAccept", "application/json");
									jsonParents.add(getJsonObject("abstract/eid/" + idRecord.getId(), urlParams));
									
									int itemsDownloaded = 0;
									int total = 0;
									boolean firstIteration = true;
									while (itemsDownloaded < total || firstIteration){
										firstIteration = false;
										urlParams.clear();
										urlParams.put("query", "ref(" + idRecord.getId() + ")");
										urlParams.put("field", "eid,author");
										urlParams.put("count", "200");
										urlParams.put("start", String.valueOf(itemsDownloaded));
										try {
											JsonObject replyItem = getJsonObject("search/scopus/", urlParams);
											
											itemsDownloaded += replyItem.getAsJsonObject("search-results").get("opensearch:itemsPerPage").getAsInt(); 
											total = replyItem.getAsJsonObject("search-results").get("opensearch:totalResults").getAsInt();
											
											jsonChilds.add(replyItem);
										} catch (Throwable e) {
											DataInterfaceErrorHandler.storeError(e);
										}
									}
								}
								
								
								
								
									
								return ScopusJsonToObjectConverter.convertAuthor(jsonAuthor, jsonCollaborators, jsonPapers, jsonParents, jsonChilds, loadedAuthor);
							}

							@Override
							protected void onFinish(ScopusAuthor author) {
								if (author != null) {
									result.add(author);
									storeToCache(Author.class, author);
								}
							}
							
							@Override
							protected void onError(Throwable e) {
								DataInterfaceErrorHandler.storeError(e);
							}
							
							@Override
							protected Object getLockObject() {
								return this;
							}
						}, groupId);
			}
		}
		
		DataInterfaceErrorHandler.handleLastErrors();
		
		return result;
	}

	@Override
	public PaperFullDetail getAllDataForPaper(IdRecord id) {
		Paper paper = loadFromCache(Paper.class, id);

		if (paper != null && paper instanceof PaperFullDetail) {
			return (PaperFullDetail) paper;
		} else {
			return (PaperFullDetail) getPaperDetails(id);
		}

	}

	private JsonObject getJsonObject(String action, Map<String, String> queryParams) throws IOException{

			StringBuilder urlParams = new StringBuilder();
			for (Entry<String, String> param : queryParams.entrySet()) {
				if (urlParams.length() > 0) {
					urlParams.append("&");
				}

				urlParams.append(param.getKey());
				urlParams.append("=");
				urlParams.append(URLEncoder.encode(param.getValue(), "UTF-8"));
			}

			URL url = new URL(INTERFACE_URL + action + "?" + urlParams.toString());
			System.out.println("CALL URL: " + url.toString());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.addRequestProperty("X-ELS-APIKey", API_KEY);
			connection.setRequestMethod("GET");
			
			InputStreamReader is = new InputStreamReader(connection.getInputStream(), "UTF-8");

			JsonObject jsonObject = new JsonParser().parse(is).getAsJsonObject();

			return jsonObject;
	}

	@Override
	public int getMaxIdsPerTransaction() {
		return MAX_PARALLEL_QUERIES * 2;
	}

	@Override
	public Map<AuthorCategorization, String> getAuthorCategorizationMapping() {
		Map<AuthorCategorization, String> map = new LinkedHashMap<AuthorCategorization, String>();
		map.put(AuthorCategorization.PROVIDER_CATEGORY_0, AuthorFullDetail.AFFILIATION_KEY);
		map.put(AuthorCategorization.PROVIDER_CATEGORY_1, AuthorFullDetail.COUNTRY_KEY);
		map.put(AuthorCategorization.PROVIDER_CATEGORY_2, AuthorFullDetail.SUBJECT_KEY);
		return map;
	}

	@Override
	public Map<PaperCategorization, String> getPaperCategorizationMapping() {
		Map<PaperCategorization, String> map = new LinkedHashMap<PaperCategorization, String>();
		map.put(PaperCategorization.PROVIDER_CATEGORY_0, PaperFullDetail.CONTRIBUTOR_KEY);
		map.put(PaperCategorization.PROVIDER_CATEGORY_1, PaperFullDetail.SOURCE_TYPE_KEY);
		map.put(PaperCategorization.PROVIDER_CATEGORY_2, PaperFullDetail.SUBJECT_KEY);
		map.put(PaperCategorization.PROVIDER_CATEGORY_3, PaperFullDetail.AFFILIATION_KEY);
		map.put(PaperCategorization.PROVIDER_CATEGORY_4, PaperFullDetail.COUNTRY_KEY);
		map.put(PaperCategorization.PROVIDER_CATEGORY_5, PaperFullDetail.KEYWORDS_KEY);
		return map;
	}

	@Override
	public Set<DataType> getSupportedDataTypes() {
		Set<DataType> result = new LinkedHashSet<DataType>();
		result.add(DataType.PAPERS);
		result.add(DataType.AUTHORS);
		result.add(DataType.AFFILIATIONS);
		return result;
	}

	@Override
	public PagedResult<IdRecord> searchAffiliations(String text, String key, int page, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Affiliation> getAffiliationDetails(Set<IdRecord> ids) {
		// TODO Auto-generated method stub
		return null;
	}
}