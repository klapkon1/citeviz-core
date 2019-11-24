package cz.uhk.fim.citeviz.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import cz.uhk.fim.citeviz.model.HistoryItem;

public class HistoryPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private List<HistoryItem> historyItems = new ArrayList<>();
	
	private JTable tblHistory = new JTable();
	
	
	public HistoryPanel() {
		setLayout(new BorderLayout());
		add(new JScrollPane(tblHistory));
	}
	
	public void addToHistory(HistoryItem item){
		historyItems.add(item);
	}
}