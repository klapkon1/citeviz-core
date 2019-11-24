package cz.uhk.fim.citeviz.event.listeners;

import java.util.HashSet;
import java.util.List;

import javax.swing.JTable;

import cz.uhk.fim.citeviz.event.EventListener;
import cz.uhk.fim.citeviz.gui.RecordsNavigator;
import cz.uhk.fim.citeviz.gui.components.AdvancedTableCellRenderer;
import cz.uhk.fim.citeviz.gui.components.RecordsTableModel;
import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.DataType;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;
import cz.uhk.fim.citeviz.ws.connector.PagedResult;
import cz.uhk.fim.citeviz.ws.connector.SearchQuery;

public class SearchRecordsListener extends EventListener<SearchQuery> {

	public static final String EVENT_NAME = "searchRecords";
	
	private final DataInterface dataInterface;
	
	private final JTable tblSearchResult;
	
	private final RecordsNavigator pnlSearchResultNavigator;
	
	public SearchRecordsListener(DataInterface dataInterface, JTable tblSearchResult, RecordsNavigator pnlSearchResultNavigator) {
		this.dataInterface = dataInterface;
		this.tblSearchResult = tblSearchResult;
		this.pnlSearchResultNavigator = pnlSearchResultNavigator;
	}
	
	@Override
	public boolean isEventForMe(String eventName) {
		return EVENT_NAME.equals(eventName);
	}

	@Override
	public void processEvent(SearchQuery eventData) {
		PagedResult<IdRecord> pagedResult = null;
		RecordsTableModel<?> tableModel = null;
		
		if (DataType.PAPERS.equals(eventData.getQueryType())){
			pagedResult = dataInterface.searchPapers(eventData.getSearchValue(), eventData.getSearchkey(), eventData.getPage(), eventData.getPageSize());
			List<Paper> papers = dataInterface.getPaperDetails(new HashSet<IdRecord>(pagedResult.getResultData()));
			tableModel = new RecordsTableModel<Paper>(papers, Paper.class);
		} else if (DataType.AUTHORS.equals(eventData.getQueryType())){
			pagedResult = dataInterface.searchAuthors(eventData.getSearchValue(), eventData.getSearchkey(), eventData.getPage(), eventData.getPageSize());
			List<Author> authors = dataInterface.getAuthorDetails(new HashSet<IdRecord>(pagedResult.getResultData()));
			tableModel = new RecordsTableModel<Author>(authors, Author.class);
		} else {
			throw new IllegalArgumentException("Unexpected query type, expected only 'a' or 'p', but got: " + eventData.getQueryType());
		}
		
		tblSearchResult.setModel(tableModel);
		for (int i = 0; i < tblSearchResult.getColumnCount(); i++) {
			tblSearchResult.getColumnModel().getColumn(i).setCellRenderer(new AdvancedTableCellRenderer());
		}
		
		tblSearchResult.getParent().revalidate();
		tblSearchResult.getParent().repaint();
		
		pnlSearchResultNavigator.update(pagedResult, eventData);
	}

	@Override
	public boolean accept(Object eventData) {
		return eventData instanceof SearchQuery;
	}
}