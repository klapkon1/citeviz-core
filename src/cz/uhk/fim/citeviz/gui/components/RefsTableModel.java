package cz.uhk.fim.citeviz.gui.components;

import java.util.List;

import cz.uhk.fim.citeviz.model.IdRecord;

public class RefsTableModel<T extends IdRecord> extends RecordsTableModel<T>{

	private static final long serialVersionUID = 1L;

	public RefsTableModel(List<T> records, Class<T> recordClass) {
		super(records, recordClass);
	}

	@Override
	public int getColumnCount() {
		return 1;
	}
}
