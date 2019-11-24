package cz.uhk.fim.citeviz.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import cz.uhk.fim.citeviz.event.EventProcessor;
import cz.uhk.fim.citeviz.event.listeners.DisplayRecordsChangeListener;
import cz.uhk.fim.citeviz.event.listeners.RootRecordSelectListener;
import cz.uhk.fim.citeviz.event.listeners.SearchRecordsListener;
import cz.uhk.fim.citeviz.graph.engine.ObjectPicker;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.model.DataType;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.util.CiteVizUtils;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;

public class RecordsPanel extends JTabbedPane{
	private static final long serialVersionUID = 1L;
	
	private JTable tblSearchResult;
	
	private JTable tblDisplayRecords;
	
	private JLabel lblDisplayRecordsCount;
	
	private DataInterface dataInterface;

	private JComboBox<DataType> cboData;

	private RecordsNavigator pnlSearchResultNavigator;
	
	private HistoryPanel historyPanel;

	public RecordsPanel(DataInterface dataInterface, JComboBox<DataType> cboData) {
		this.dataInterface = dataInterface;
		this.cboData = cboData;
		
		add(Localizer.getString("main.searchPanel.tabTitle"), initSearchPanel());
		add(Localizer.getString("main.displayRecordsPanel.tabTitle"), initDisplayRecordsPanel());
		add(Localizer.getString("main.historyPanel.tabTitle"), initHistoryPanel());
	}	
	
	private Component initHistoryPanel() {
		historyPanel = new HistoryPanel(); 
		return historyPanel;
	}
	
	public HistoryPanel getHistoryPanel() {
		return historyPanel;
	}

	private JComponent initDisplayRecordsPanel() {
		tblDisplayRecords = new JTable();
		lblDisplayRecordsCount = new JLabel(Localizer.getString("view.numOfRecords", "0"));
		EventProcessor.registerListener(new DisplayRecordsChangeListener(tblDisplayRecords, lblDisplayRecordsCount));
		
		JPanel pnlDisplayRecords = new JPanel(new BorderLayout());
		pnlDisplayRecords.add(new JScrollPane(tblDisplayRecords), BorderLayout.CENTER);
		pnlDisplayRecords.add(lblDisplayRecordsCount, BorderLayout.SOUTH);
		
		
		tblDisplayRecords.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (tblDisplayRecords.getSelectedRow() != -1){
					IdRecord id = (IdRecord) tblDisplayRecords.getModel().getValueAt(tblDisplayRecords.getSelectedRow(), -1);
					
					if (e.getClickCount() == 2){
						onRecordSelect(CiteVizUtils.asSet(id));
					} else if (e.getClickCount() == 1){
						EventProcessor.sendEvent(ObjectPicker.EVENT_LOCK_PICK, id);
					}
				}
			}
		});
		
		return pnlDisplayRecords;
	}
	
	
	
	private JComponent initSearchPanel() {	
		JPanel pnlSearch = new JPanel(new BorderLayout());
		pnlSearch.add(new SearchPanel(cboData), BorderLayout.NORTH);
		
		tblSearchResult = new JTable();
		tblSearchResult.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2){
					
					Set<IdRecord> ids = new HashSet<IdRecord>();
					for (int row : tblSearchResult.getSelectedRows()) {
						ids.add((IdRecord) tblSearchResult.getModel().getValueAt(row, -1));
					}
					
					if (ids.size() > 0){
						onRecordSelect(ids);
					}
				}
			}
		});
		pnlSearch.add(new JScrollPane(tblSearchResult), BorderLayout.CENTER);
		pnlSearchResultNavigator = new RecordsNavigator(tblSearchResult);
		pnlSearch.add(pnlSearchResultNavigator, BorderLayout.SOUTH);
		
		EventProcessor.registerListener(new SearchRecordsListener(dataInterface, tblSearchResult, pnlSearchResultNavigator));
		
		return pnlSearch;
	}
	
	
	
	protected void onRecordSelect(Set<IdRecord> records){
		EventProcessor.sendEvent(RootRecordSelectListener.EVENT_NAME, records);
	};
}