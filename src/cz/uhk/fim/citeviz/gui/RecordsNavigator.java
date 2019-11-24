package cz.uhk.fim.citeviz.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import cz.uhk.fim.citeviz.event.EventProcessor;
import cz.uhk.fim.citeviz.event.listeners.RootRecordSelectListener;
import cz.uhk.fim.citeviz.event.listeners.SearchRecordsListener;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.ws.connector.PagedResult;
import cz.uhk.fim.citeviz.ws.connector.SearchQuery;

public class RecordsNavigator extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	public static final int DEFAULT_PAGE_SIZE = 50;
	
	private int currentPage;

	private int pagesCount;
	
	private JTable tblSearchResult;

	private JButton btnAddAll;
	
	private JButton btnAddSelected;
	
	private JButton btnFirst;
	
	private JButton btnPrevious;
	
	private JButton btnNext;
	
	private JButton btnLast;
	
	private JTextField txtPage;
	
	private SearchQuery lastQuery;
	
	private JLabel lblTotalResults = new JLabel();

	public RecordsNavigator(JTable tblSearchResult) {
		this.tblSearchResult = tblSearchResult;
		setLayout(new FlowLayout(FlowLayout.CENTER));
		
		//add(lblTotalResults);
		
		btnFirst = new JButton("<<");
		btnFirst.addActionListener(this);
		add(btnFirst);
		btnPrevious = new JButton("<");
		btnPrevious.addActionListener(this);
		add(btnPrevious);
		
		txtPage = new JTextField(4);
		txtPage.setHorizontalAlignment(JTextField.CENTER);
		txtPage.setText(String.valueOf(currentPage + 1));
		txtPage.addActionListener(this);
		add(txtPage);
		
		btnNext = new JButton(">");
		btnNext.addActionListener(this);
		add(btnNext);
		
		btnLast = new JButton(">>");
		btnLast.addActionListener(this);
		add(btnLast);	
		
		add(new JPanel());
		
		btnAddAll = new JButton(Localizer.getString("searchPanel.addAllResults"));
		btnAddAll.addActionListener(this);
		add(btnAddAll);
		
		btnAddSelected = new JButton(Localizer.getString("searchPanel.addSelectedResults"));
		btnAddSelected.addActionListener(this);
		add(btnAddSelected);
		
		setVisible(false);
	}
	
	public void update(PagedResult<IdRecord> pagedResult, SearchQuery lastQuery) {
		this.lastQuery = lastQuery;
		
		if (pagedResult == null){
			setVisible(false);
			return;
		} else {
			this.pagesCount = pagedResult.getPagesCount();
			this.currentPage = pagedResult.getCurrentPage();
			txtPage.setText(String.valueOf(currentPage + 1));
			
			btnPrevious.setEnabled(currentPage > 0);
			btnFirst.setEnabled(currentPage > 0);
			btnLast.setEnabled(currentPage < pagesCount);
			btnNext.setEnabled(currentPage < pagesCount);
			
			lblTotalResults.setText(String.valueOf(pagedResult.getTotalCount()));
			
			setVisible(true);
		}
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnAddAll){
			Set<IdRecord> ids = new HashSet<IdRecord>();
			for (int i = 0; i < tblSearchResult.getModel().getRowCount(); i++){
				ids.add((IdRecord) tblSearchResult.getModel().getValueAt(i, -1));
			}
			
			EventProcessor.sendEvent(RootRecordSelectListener.EVENT_NAME, ids);
		} if (e.getSource() == btnAddSelected){
			Set<IdRecord> ids = new HashSet<IdRecord>();
			for (int i = 0; i < tblSearchResult.getSelectedRowCount(); i++){
				ids.add((IdRecord) tblSearchResult.getModel().getValueAt(tblSearchResult.getSelectedRows()[i], -1));
			}
			
			EventProcessor.sendEvent(RootRecordSelectListener.EVENT_NAME, ids);
		} else {
			
			if (e.getSource() == btnFirst){
				currentPage = 0;
			}
			
			if (e.getSource() == btnPrevious){
				if (currentPage > 0){
					currentPage--;
				}
			}
			
			if (e.getSource() == btnNext){
				if (currentPage < pagesCount){
					currentPage++;
				}
			}
			
			if (e.getSource() == btnLast){
				currentPage = pagesCount;
			}
			
			if (e.getSource() == txtPage){
				try {
					int selectedPage = Integer.parseInt(txtPage.getText()) - 1;
					
					if (selectedPage > 0 && selectedPage < pagesCount){
						currentPage = selectedPage;
					}
				} catch (NumberFormatException exception){
					JOptionPane.showMessageDialog(null, "Zadaná hodnota není èíslo", "Nelze pøejít na požadovnou stránku", JOptionPane.ERROR_MESSAGE);
				}
			}
			
			
			txtPage.setText(String.valueOf(currentPage + 1));
			lastQuery.setPage(currentPage);
			
			EventProcessor.sendEvent(SearchRecordsListener.EVENT_NAME, lastQuery);
		}
	}
}