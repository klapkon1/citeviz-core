package cz.uhk.fim.citeviz.ws.connector;

import java.util.List;

public class PagedResult<T> {
	
	private List<T> resultData;
	
	private int pagesCount;
	
	private int currentPage;
	
	private int totalCount;
	
	public PagedResult(List<T> resultData, int currentPage, int pageSize, int totalCount){
		this.resultData = resultData;
		this.currentPage = currentPage;
		this.totalCount = totalCount;
		this.pagesCount = (pageSize == 0) ? 0 : (totalCount / pageSize) + (totalCount % pageSize == 0 ? -1 : 0);
	}
	
	
	public List<T> getResultData() {
		return resultData;
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	public int getPagesCount() {
		return pagesCount;
	}
	
	public int getTotalCount() {
		return totalCount;
	}
}
