package cz.uhk.fim.citeviz.event.listeners;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;

import cz.uhk.fim.citeviz.event.EventListener;
import cz.uhk.fim.citeviz.gui.components.AdvancedTableCellRenderer;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.gui.components.RecordsTableModel;
import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;

public class DisplayRecordsChangeListener extends EventListener<List<IdRecord>> {

	public static final String EVENT_NAME = "displayRecordsChange";
	
	private JTable tblDisplayRecords;
	
	private JLabel lblDisplayRecordsCount;
	
	public DisplayRecordsChangeListener(JTable tblDisplayRecords, JLabel lblDisplayRecordsCount) {
		this.tblDisplayRecords = tblDisplayRecords;
		this.lblDisplayRecordsCount = lblDisplayRecordsCount;
	}

	@Override
	public boolean isEventForMe(String eventName) {
		return EVENT_NAME.equals(eventName);
	}

	@Override
	public void processEvent(List<IdRecord> eventData) {
		if (eventData == null){
			tblDisplayRecords.setModel(null);
			return;
		}
		
		List<Paper> papers = new ArrayList<Paper>(eventData.size());
		List<Author> authors = new ArrayList<Author>(eventData.size());
		for (IdRecord idRecord : eventData) {
			if (idRecord instanceof Paper){
				papers.add((Paper)idRecord);
			} else if (idRecord instanceof Author){
				authors.add((Author)idRecord);
			}
		}
		
		if (!papers.isEmpty()){
			tblDisplayRecords.setModel(new RecordsTableModel<Paper>(papers, Paper.class));
		} else {
			tblDisplayRecords.setModel(new RecordsTableModel<Author>(authors, Author.class));
		}
		
		for (int i = 0; i < tblDisplayRecords.getColumnCount(); i++) {
			tblDisplayRecords.getColumnModel().getColumn(i).setCellRenderer(new AdvancedTableCellRenderer());
		}
		
		lblDisplayRecordsCount.setText(Localizer.getString("view.numOfRecords", String.valueOf(tblDisplayRecords.getRowCount())));
	}

	@Override
	public boolean accept(Object eventData) {
		return eventData instanceof List;
	}
}