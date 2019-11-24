package cz.uhk.fim.citeviz.gui.components;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;

public class RecordsTableModel<T extends IdRecord> extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	protected List<T> records;
	protected Class<T> recordClass;
	
	public RecordsTableModel(List<T> records, Class<T> recordClass){
		this.records = records;
		this.recordClass = recordClass;
	}
	
	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return records.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (records == null || records.isEmpty()) return "";
		
		if (columnIndex == -1){
			return (IdRecord) records.get(rowIndex);
		}
		
		if (recordClass.isAssignableFrom(Paper.class)){
			Paper p = (Paper) records.get(rowIndex);
			switch (columnIndex) {
				case 0: return p.getTitle() + " (" + p.getYear() + ")"; 
				case 1: return p.getAuthorsAsString(); 
				case 2: return Localizer.getString("global.citations") + p.getChildsCount() + ", " + Localizer.getString("global.references") + p.getParentsCount();
				default : return p;
			}
		} else if (recordClass.isAssignableFrom(Author.class)){
			Author a = (Author) records.get(rowIndex);
			switch (columnIndex) {
				case 0: return a.getName(); 
				case 1: return a.getParentsCount(); 
				case 2: return a.getChildsCount();
				default : return a;
			}
		} else {
			throw new IllegalArgumentException("Unsupported data type: " + recordClass.getSimpleName());
		}
	}
	
	@Override
	public String getColumnName(int column) {
		
		if (recordClass.isAssignableFrom(Paper.class)){
			switch (column) {
				case 0: return Localizer.getString("searchResultTable.title"); 
				case 1: return Localizer.getString("searchResultTable.authors"); 
				case 2: return Localizer.getString("searchResultTable.refs");
				default : return null;
			}
		} else if (recordClass.isAssignableFrom(Author.class)){
			switch (column) {
				case 0: return Localizer.getString("searchResultTable.name"); 
				case 1: return Localizer.getString("searchResultTable.refsCount"); 
				case 2: return Localizer.getString("searchResultTable.citeCount");
				default : return null;
			}
		} else {
			throw new IllegalArgumentException("Unsupported data type: " + recordClass.getSimpleName());
		}
	}
}
