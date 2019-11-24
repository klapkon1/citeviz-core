package cz.uhk.fim.citeviz.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import cz.uhk.fim.citeviz.graph.categorizer.Categorizer;
import cz.uhk.fim.citeviz.graph.views.NonGraphicView;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.gui.components.RecordsTableModel;
import cz.uhk.fim.citeviz.gui.components.SpringUtilities;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;
import cz.uhk.fim.citeviz.model.ViewType;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;
/**
 * Tøída implementuje pohled detail publikace, který zobrazuje základní informace
 * o vybrané publikaci
 * @author Ondøej Klapka
 *
 */
public class PaperDetailView extends NonGraphicView{

	private static final long serialVersionUID = 1L;
	
	private JTable tblChilds = new JTable();
	private JTable tblParents = new JTable();
	
	
	private DataInterface dataInterface;
	
	public PaperDetailView(DataInterface dataInterface, IdRecord rootId) {
		this.dataInterface = dataInterface;
		initContent(dataInterface.getPaperDetails(rootId));
	}
	
	
	private void initContent(Paper paper){
		removeAll();
		
		
		JPanel pnlInfo = new JPanel(new SpringLayout());
		pnlInfo.add(new JLabel(Localizer.getString("view.paperDetail.title")));
		JTextField txtTitle = new JTextField(paper.getTitle());
		txtTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		txtTitle.setEditable(false);
		txtTitle.setBorder(null);
		txtTitle.setBackground(null);
		txtTitle.setSize(100, 20);
		pnlInfo.add(txtTitle);
		
		pnlInfo.add(new JLabel(Localizer.getString("view.paperDetail.id")));
		JTextField txtId = new JTextField(String.valueOf(paper.getId()));
		txtId.setEditable(false);
		txtId.setBorder(null);
		txtId.setBackground(null);
		pnlInfo.add(txtId);
		
		
		pnlInfo.add(new JLabel(Localizer.getString("view.paperDetail.year")));
		JTextField txtYear = new JTextField(String.valueOf(paper.getYear()));
		txtYear.setEditable(false);
		txtYear.setBorder(null);
		txtYear.setBackground(null);
		pnlInfo.add(txtYear);
		
		pnlInfo.add(new JLabel());
		JTextField txtAuthors = new JTextField(paper.getAuthorsAsString());
		txtAuthors.setEditable(false);
		txtAuthors.setBorder(null);
		txtAuthors.setBackground(null);
		pnlInfo.add(txtAuthors);
	
		SpringUtilities.makeCompactGrid(pnlInfo, 4, 2, 5, 5, 5, 5);
		
		JTextField txtParentsCount = new JTextField(20);
		txtParentsCount.setEditable(false);
		txtParentsCount.setForeground(Color.red);
		txtParentsCount.setFont(new Font("Tahoma",Font.BOLD,12));
		txtParentsCount.setBorder(null);
		txtParentsCount.setHorizontalAlignment(JTextField.CENTER);
		txtParentsCount.setText(Localizer.getString("global.references") + String.valueOf(paper.getParentsCount()));
		
		JTextField txtChildsCount = new JTextField(20);
		txtChildsCount.setEditable(false);
		txtChildsCount.setForeground(Color.blue);
		txtChildsCount.setFont(new Font("Tahoma",Font.BOLD,12));
		txtChildsCount.setBorder(null);
		txtChildsCount.setHorizontalAlignment(JTextField.CENTER);
		txtChildsCount.setText(Localizer.getString("global.citations") + String.valueOf(paper.getChildsCount()));
		
		JPanel pnlParents = new JPanel(new BorderLayout());
		pnlParents.add(txtParentsCount, BorderLayout.NORTH);
		pnlParents.add(new JScrollPane(tblParents), BorderLayout.CENTER);
		
		JPanel pnlChilds = new JPanel(new BorderLayout());
		pnlChilds.add(txtChildsCount, BorderLayout.NORTH);
		pnlChilds.add(new JScrollPane(tblChilds), BorderLayout.CENTER);
		
		
		JPanel pnlRefs = new JPanel(new GridLayout(1, 2));
		pnlRefs.add(pnlChilds);
		pnlRefs.add(pnlParents);
		add(new JScrollPane(pnlRefs),BorderLayout.CENTER);
		
		
		
		
		
		MouseListener ms = new MouseAdapter() {	
			@Override
			public void mouseClicked(MouseEvent e) {
				//procházení po citacích
				if (e.getSource() == tblChilds && e.getClickCount() == 2 && tblChilds.getSelectedRow() >= 0){
					initContent((Paper)tblChilds.getValueAt(tblChilds.getSelectedRow(), -1));
				};
			
				//procházení po zdrojích
				if (e.getSource() == tblParents && e.getClickCount() == 2 && tblParents.getSelectedRow() >= 0){
					initContent((Paper)tblParents.getValueAt(tblParents.getSelectedRow(), -1));
				}
			};
		};
		
		RecordsTableModel<Paper> modelChilds = new RecordsTableModel<Paper>(dataInterface.getPaperDetails(paper.getChilds()), Paper.class);
		tblChilds.setModel(modelChilds);
		tblChilds.addMouseListener(ms);
		
		RecordsTableModel<Paper> modelParents = new RecordsTableModel<Paper>(dataInterface.getPaperDetails(paper.getParents()), Paper.class);
		tblParents.setModel(modelParents);
		tblParents.addMouseListener(ms);
		
		validate();
		repaint();
	}
	
	
	@Override
	public List<IdRecord> getDisplayRecords() {
		List<IdRecord> records = new ArrayList<IdRecord>();
		//records.addAll(dataInterface.getPaperDetails())
		
		
		return records;
	}



	@Override
	public void setCategorizer(Categorizer<IdRecord, ?> categorizer) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public ViewType getViewType() {
		return ViewType.PAPER_DETAIL;
	}
}