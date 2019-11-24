package cz.uhk.fim.citeviz.ws.scopus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.PaperFullDetail;
import cz.uhk.fim.citeviz.util.CiteVizUtils;

public class ScopusJsonToObjectConverter {

	private static final String PAPER_EID_PREFIX = "2-s2.0-";
	private static final String AUTHOR_EID_PREFIX = "9-s2.0-";

	public static final List<IdRecord> convertPaperIds(JsonObject jsonObject){
		List<IdRecord> result = new ArrayList<IdRecord>();
		
		if (jsonObject == null){
			return result;
		}
		
		JsonArray jsonArray = jsonObject.getAsJsonObject("search-results").getAsJsonArray("entry");
		
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonElement = jsonArray.get(i).getAsJsonObject();
			if (jsonElement.get("eid") != null){
				result.add(new IdRecord(jsonElement.get("eid").getAsString()));	
			}
		}
		
		return result;
	}
	
	
	public static List<IdRecord> convertAuthors(JsonObject jsonObject) {
		List<IdRecord> result = new ArrayList<IdRecord>();
		
		if (jsonObject == null){
			return result;
		}
		
		JsonArray jsonArray = jsonObject.getAsJsonObject("search-results").getAsJsonArray("entry");
		
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonElement = jsonArray.get(i).getAsJsonObject();
			if (jsonElement.get("eid") != null){
				
				StringBuilder authorName = new StringBuilder();
				
				JsonPrimitive jsonPrimitive = getElementsByPath(jsonElement, JsonPrimitive.class, new String[]{"preferred-name", "surname"});
				if (jsonPrimitive != null){
					authorName.append(jsonPrimitive.getAsString());
				}
				
				jsonPrimitive = getElementsByPath(jsonElement, JsonPrimitive.class, new String[]{"preferred-name", "given-name"});
				if (jsonPrimitive != null){
					authorName.append(" ");
					authorName.append(jsonPrimitive.getAsString());
				}
				
				
				
				
				ScopusAuthor author = new ScopusAuthor(jsonElement.get("eid").getAsString(), authorName.toString(), -1, new HashMap<String, Set<String>>());
				result.add(author);	
			}
		}
		
		return result;
	}


	private static Map<String, Set<String>> convertOtherPaperData(JsonObject paperJson) {
		JsonObject coreDataJson = paperJson.getAsJsonObject("coredata");
		
		Map<String, Set<String>> otherData = new HashMap<>();
		if (coreDataJson.get("dc:description") != null){
			otherData.put(PaperFullDetail.ABSTRACT_KEY, CiteVizUtils.asSet(coreDataJson.get("dc:description").getAsString()));
		}
		
		if (coreDataJson.get("prism:publicationName") != null){
			otherData.put(PaperFullDetail.CONTRIBUTOR_KEY, CiteVizUtils.asSet(coreDataJson.get("prism:publicationName").getAsString()));
		}
		
		if (coreDataJson.get("prism:url") != null){
			otherData.put(PaperFullDetail.URL_KEY, CiteVizUtils.asSet(coreDataJson.get("prism:url").getAsString()));
		}
		
		if (coreDataJson.get("prism:doi") != null){
			otherData.put(PaperFullDetail.DOI_KEY, CiteVizUtils.asSet(coreDataJson.get("prism:doi").getAsString()));
		}
		
		if (coreDataJson.get("prism:aggregationType") != null){
			otherData.put(PaperFullDetail.SOURCE_TYPE_KEY, CiteVizUtils.asSet(coreDataJson.get("prism:aggregationType").getAsString()));
		}
		
		if (paperJson.get("subject-areas") != null){
			JsonArray areas = getElementsByPath(paperJson, JsonArray.class, new String[]{"subject-areas", "subject-area"});
			
			if (areas != null){
				Set<String> areasSet = new HashSet<>();
				
				for (int j = 0; j < areas.size(); j++) {
					areasSet.add(areas.get(j).getAsJsonObject().get("$").getAsString());
				}
				
				otherData.put(PaperFullDetail.SUBJECT_KEY, areasSet);
			}
			
		}
		
		if (paperJson.get("affiliation") != null){
			if (paperJson.get("affiliation").isJsonArray()){
				Set<String> affiliationsSet = new HashSet<>();
				Set<String> countrySet = new HashSet<>();
					
				for (int j = 0; j < paperJson.get("affiliation").getAsJsonArray().size(); j++) {
					JsonObject affJsonObject = paperJson.get("affiliation").getAsJsonArray().get(j).getAsJsonObject();
					
					if (!affJsonObject.get("affilname").isJsonNull()){
						affiliationsSet.add(affJsonObject.get("affilname").getAsString());
					}
					if (!affJsonObject.get("affiliation-country").isJsonNull()){
						countrySet.add(affJsonObject.get("affiliation-country").getAsString());
					}
					
				}
					
				otherData.put(PaperFullDetail.AFFILIATION_KEY, affiliationsSet);
				otherData.put(PaperFullDetail.COUNTRY_KEY, countrySet);
			} else {
				JsonPrimitive affilname = getElementsByPath(paperJson, JsonPrimitive.class, new String[]{"affiliation", "affilname"});
				if (affilname != null){
					otherData.put(PaperFullDetail.AFFILIATION_KEY, CiteVizUtils.asSet(affilname.getAsString()));
				}
				
				JsonPrimitive affiliationCountry = getElementsByPath(paperJson, JsonPrimitive.class, new String[]{"affiliation", "affiliation-country"});
				if (affiliationCountry != null){
					otherData.put(PaperFullDetail.COUNTRY_KEY, CiteVizUtils.asSet(affiliationCountry.getAsString()));
				}
			}
		}
		
		
		if (paperJson.get("authkeywords") != null){
			JsonArray keywords = getElementsByPath(paperJson, JsonArray.class, new String[]{"authkeywords", "author-keyword"});
			
			if (keywords != null){
				Set<String> keywordsSet = new HashSet<>();
				
				for (int j = 0; j < keywords.size(); j++) {
					keywordsSet.add(keywords.get(j).getAsJsonObject().get("$").getAsString());
				}
				
				otherData.put(PaperFullDetail.KEYWORDS_KEY, keywordsSet);
			} else {
				JsonObject keyword = getElementsByPath(paperJson, JsonObject.class, new String[]{"authkeywords", "author-keyword"});
				
				
				otherData.put(PaperFullDetail.KEYWORDS_KEY, CiteVizUtils.asSet(keyword.toString()));
			}
		}
		
		
		return otherData;
	}
	
	
	public static final PaperFullDetail convertPaper(JsonObject paperJson, List<JsonObject> jsonChilds){
		if (paperJson == null || paperJson.getAsJsonObject("abstracts-retrieval-response") == null){
			return null;
		}
			
		//BASIC INFO WITH AUTHORS
		paperJson = paperJson.getAsJsonObject("abstracts-retrieval-response");
			
			
		Set<Author> authors = new HashSet<Author>();
		JsonArray jsonArrayAuthors = getElementsByPath(paperJson, JsonArray.class, new String[]{"authors", "author"});
				
		if (jsonArrayAuthors != null){
			for (int j = 0; j < jsonArrayAuthors.size(); j++) {
				JsonObject auObject = jsonArrayAuthors.get(j).getAsJsonObject();
				JsonPrimitive authorId = auObject.getAsJsonPrimitive("@auid");
				JsonPrimitive authorName = auObject.getAsJsonPrimitive("ce:indexed-name");
				if (authorId != null && authorName != null) {
					authors.add(new Author(AUTHOR_EID_PREFIX + authorId.getAsString(), authorName.getAsString()));
				}
				
			}
		}
			
		JsonPrimitive eid = getElementsByPath(paperJson, JsonPrimitive.class, new String[]{"coredata", "eid"});
		JsonPrimitive title = getElementsByPath(paperJson, JsonPrimitive.class, new String[]{"coredata", "dc:title"});
		JsonPrimitive coverDate = getElementsByPath(paperJson, JsonPrimitive.class, new String[]{"coredata", "prism:coverDate"});
		JsonPrimitive rank = getElementsByPath(paperJson, JsonPrimitive.class, new String[]{"coredata", "citedby-count"});
			
		//if we don't have EID or title, than paper cannot exist in CiteViz 
		if (eid == null || title == null){
			return null;
		}
			
		int year = Integer.valueOf(coverDate.getAsString().substring(0, 4));
			
		PaperFullDetail paper = new PaperFullDetail(eid.getAsString(),
								title.getAsString(),
						        authors,
						        year,
						        rank.getAsInt() / Math.max(1, Calendar.getInstance().get(Calendar.YEAR) - year + 1),
						        convertOtherPaperData(paperJson)
						        );
		
		
		JsonArray jsonArrayParents = getElementsByPath(paperJson, JsonArray.class, new String[]{"item", "bibrecord", "tail", "bibliography", "reference"});
		if (jsonArrayParents != null){
			for (int i = 0; i < jsonArrayParents.size(); i++) {
				JsonObject jsonElement = jsonArrayParents.get(i).getAsJsonObject();
				
				JsonObject jsonRefInfo = getElementsByPath(jsonElement, JsonObject.class, new String[]{"ref-info", "refd-itemidlist"});
				
				if (jsonRefInfo.get("itemid").isJsonArray()){
					JsonArray jsonArrayRefInfos = jsonRefInfo.get("itemid").getAsJsonArray();
					
					for (int j = 0; j < jsonArrayRefInfos.size(); j++) {
						if ("SGR".equals(jsonArrayRefInfos.get(j).getAsJsonObject().get("@idtype").getAsString())){
							paper.getParents().add(new IdRecord(PAPER_EID_PREFIX + jsonArrayRefInfos.get(j).getAsJsonObject().get("$").getAsString()));
						}
					}
				} else {
					if ("SGR".equals(jsonRefInfo.get("itemid").getAsJsonObject().get("@idtype").getAsString())){
						paper.getParents().add(new IdRecord(PAPER_EID_PREFIX + jsonRefInfo.get("itemid").getAsJsonObject().get("$").getAsString()));
					}
				}
			}
		}
		
			
		//CHILDS		
		for (JsonElement jsonChild : jsonChilds) {
			JsonArray jsonArrayChilds = getElementsByPath(jsonChild.getAsJsonObject(), JsonArray.class, new String[]{"search-results", "entry"});
			if (jsonArrayChilds != null){
				for (int i = 0; i < jsonArrayChilds.size(); i++) {
					JsonObject jsonElement = jsonArrayChilds.get(i).getAsJsonObject();
					if (jsonElement.get("eid") != null){
						paper.getChilds().add(new IdRecord(jsonElement.get("eid").getAsString()));
					} else {
						break;
					}
				}
			}
		}
		
		return paper;
	}
	
	
	public static final ScopusAuthor convertAuthor(JsonObject jsonAuthor, JsonObject jsonCollaborators, JsonObject jsonPapers, List<JsonObject> jsonParents, List<JsonObject> jsonChilds, ScopusAuthor partialResult){
		if (partialResult == null){
			jsonAuthor = jsonAuthor.getAsJsonObject("search-results").getAsJsonArray("entry").get(0).getAsJsonObject();
			
			if (jsonAuthor.get("eid") != null){
				
				StringBuilder authorName = new StringBuilder();
				
				authorName.append(getElementsByPath(jsonAuthor, JsonPrimitive.class, new String[]{"preferred-name", "surname"}).getAsString());
	
				JsonPrimitive authorGivenName = getElementsByPath(jsonAuthor, JsonPrimitive.class, new String[]{"preferred-name", "given-name"});
				if (authorGivenName != null) {
					authorName.append(" ");
					authorName.append(authorGivenName.getAsString());
				}
				
				
				partialResult = new ScopusAuthor(jsonAuthor.get("eid").getAsString(), authorName.toString(), -1, new HashMap<String, Set<String>>());
			} else {
				return null;
			}
		}
		
		JsonArray jsonArray = jsonCollaborators.getAsJsonObject("search-results").getAsJsonArray("entry");
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonElement = jsonArray.get(i).getAsJsonObject();
			if (jsonElement.get("eid") != null){
				partialResult.getCollaborators().add(new IdRecord(jsonElement.get("eid").getAsString()));
			}
		}
		
		
		
		jsonArray = jsonPapers.getAsJsonObject("search-results").getAsJsonArray("entry");
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonElement = jsonArray.get(i).getAsJsonObject();
			if (jsonElement.get("eid") != null){
				partialResult.getPapersId().add(new IdRecord(jsonElement.get("eid").getAsString()));
			}
		}
		
		for (JsonElement jsonChild : jsonChilds) {
			JsonArray jsonArrayChilds = getElementsByPath(jsonChild.getAsJsonObject(), JsonArray.class, new String[]{"search-results", "entry"});
			if (jsonArrayChilds != null){
				for (JsonElement jsonElement : jsonArrayChilds) {
					JsonArray jsonArrayChildAuthors = getElementsByPath(jsonElement.getAsJsonObject(), JsonArray.class, new String[]{"author"});
					if (jsonArrayChildAuthors != null) {
						for (JsonElement jsonChildAuthorElement : jsonArrayChildAuthors) {
							if (jsonChildAuthorElement.getAsJsonObject().get("authid") != null){
								partialResult.getChilds().add(new IdRecord(AUTHOR_EID_PREFIX + jsonChildAuthorElement.getAsJsonObject().get("authid").getAsString()));
							}
						}
					}
				}
			}
		}
		
		
		for (JsonObject jsonParent : jsonParents) {
			JsonArray jsonArrayParents = getElementsByPath(jsonParent, JsonArray.class, new String[]{"abstracts-retrieval-response", "references", "reference"});
			if (jsonArrayParents != null){
				for (JsonElement jsonElement : jsonArrayParents) {
					JsonArray jsonArrayParentAuthors = getElementsByPath(jsonElement.getAsJsonObject(), JsonArray.class, new String[]{"author-list", "author"});
					if (jsonArrayParentAuthors != null){
						for (JsonElement jsonParentAuthorElement : jsonArrayParentAuthors) {
							if (jsonParentAuthorElement.getAsJsonObject().get("@auid") != null){
								partialResult.getParents().add(new IdRecord(AUTHOR_EID_PREFIX + jsonParentAuthorElement.getAsJsonObject().get("@auid").getAsString()));
							}
						}
					}
				}
			}
		}
		
		
		partialResult.setCompleted(true);
		return partialResult;
	}
		
	@SuppressWarnings("unchecked")
	private static <C> C getElementsByPath(JsonObject input, Class<C> outputClass, String... members){
		if (input == null){
			return null;
		}
		
		//the last one is final output - don't iterate
		for (int i = 0; i < members.length - 1; i++) {
			if (input.get(members[i]) != null && input.get(members[i]) instanceof JsonObject) {
				input = input.getAsJsonObject(members[i]);
			} else {
				return null;
			}
		}
		
		Object result = input.get(members[members.length - 1]);
		if (result != null && result.getClass().isAssignableFrom(outputClass)){
			return (C) result;
		} else {
			return null;
		}
	}
}