package cz.uhk.fim.citeviz.ws.connector;

import cz.uhk.fim.citeviz.model.DataType;

public class SearchQuery {
	
	private DataType queryType;
	
	private String searchkey;
	
	private String searchValue;

	private int page;
	
	private int pageSize;
	
	public SearchQuery(String searchkey, String searchValue, DataType queryType, int page, int pageSize) {
		super();
		this.searchkey = searchkey;
		this.searchValue = searchValue;
		this.queryType = queryType;
		this.page = page;
		this.pageSize = pageSize;
	}

	public String getSearchkey() {
		return searchkey;
	}

	public String getSearchValue() {
		return searchValue;
	}
	
	public int getPage() {
		return page;
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	public DataType getQueryType() {
		return queryType;
	}
}