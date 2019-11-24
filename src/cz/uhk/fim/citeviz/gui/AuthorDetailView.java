package cz.uhk.fim.citeviz.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import cz.uhk.fim.citeviz.gui.components.RefsTableModel;
import cz.uhk.fim.citeviz.gui.components.SpringUtilities;
import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.ViewType;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;
/**
 * Tøída implemetuje pohled detail autora, který zobrazuje základní informace
 * o vybraném autorovi
 * @author Ondøej Klapka
 *
 */
public class AuthorDetailView extends NonGraphicView{
	
	private static final long serialVersionUID = 1L;
	
	private JTable tblChilds = new JTable();
	private JTable tblParents = new JTable();
	private JTable tblColaborators = new JTable();
	
	private DataInterface dataInterface;
	
	public AuthorDetailView(DataInterface dataInterface, IdRecord rootId) {
		this.dataInterface = dataInterface;
		initContent(dataInterface.getAuthorDetails(rootId));
	}
	
	
	public void initContent(Author author){
		removeAll();
		
		JPanel pnlInfo = new JPanel(new SpringLayout());
		pnlInfo.add(new JLabel(Localizer.getString("view.authorDetail.name")));
		JTextField txtName = new JTextField(author.getName());
		txtName.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		txtName.setEditable(false);
		txtName.setBorder(null);
		txtName.setBackground(null);
		txtName.setSize(100, 20);
		pnlInfo.add(txtName);
		
		pnlInfo.add(new JLabel(Localizer.getString("view.authorDetail.id")));
		JTextField txtId = new JTextField(String.valueOf(author.getId()));
		txtId.setEditable(false);
		txtId.setBorder(null);
		txtId.setBackground(null);
		pnlInfo.add(txtId);
		
		SpringUtilities.makeCompactGrid(pnlInfo, 2, 2, 5, 5, 5, 5);
		
		add(new JScrollPane(pnlInfo),BorderLayout.NORTH);
		

		
		
		MouseListener ms = new MouseAdapter() {	
			@Override
			public void mouseClicked(MouseEvent e) {
				//procházení po citujících
				if (e.getSource() == tblChilds && e.getClickCount() == 2 && tblChilds.getSelectedRow() >= 0){
					initContent((Author)tblChilds.getValueAt(tblChilds.getSelectedRow(), -1));
				};
			
				//procházení po citovaných
				if (e.getSource() == tblParents && e.getClickCount() == 2 && tblParents.getSelectedRow() >= 0){
					initContent((Author)tblParents.getValueAt(tblParents.getSelectedRow(), -1));
				};
				
				//procházení po spolupracovnících
				if (e.getSource() == tblColaborators && e.getClickCount() == 2 && tblColaborators.getSelectedRow() >= 0){
					initContent((Author)tblColaborators.getValueAt(tblColaborators.getSelectedRow(), -1));
				};
					
					
				
			};
		};
		
//-------------------citovaní autoøi výpis	
		JTextField txtParents = new JTextField(10);
		txtParents.setEditable(false);
		txtParents.setForeground(Color.red);
		txtParents.setFont(new Font("Tahoma",Font.BOLD,12));
		txtParents.setBorder(null);
		txtParents.setHorizontalAlignment(JTextField.CENTER);
		txtParents.setText("Citovaní: "+String.valueOf(author.getParentsCount()));
		JPanel pnlCitovani = new JPanel(new BorderLayout());
		pnlCitovani.add(txtParents,BorderLayout.NORTH);
				
				
		
		RefsTableModel<Author> modelParents = new RefsTableModel<Author>(dataInterface.getAuthorDetails(author.getParents()), Author.class);
		tblParents.setModel(modelParents);
		tblParents.addMouseListener(ms);
		
		pnlCitovani.add(new JScrollPane(tblParents),BorderLayout.CENTER);
//------------------citující autoøi výpis		
		JTextField txtChilds = new JTextField(10);
		txtChilds.setEditable(false);
		txtChilds.setForeground(Color.blue);
		txtChilds.setFont(new Font("Tahoma",Font.BOLD,12));
		txtChilds.setBorder(null);
		txtChilds.setHorizontalAlignment(JTextField.CENTER);
		txtChilds.setText("Citující: "+String.valueOf(author.getChildsCount()));
		JPanel pnlCitujici = new JPanel(new BorderLayout());
		pnlCitujici.add(txtChilds,BorderLayout.NORTH);
		
		RefsTableModel<Author> modelCitujici = new RefsTableModel<Author>(dataInterface.getAuthorDetails(author.getChilds()), Author.class);
		tblChilds.setModel(modelCitujici);
		tblChilds.addMouseListener(ms);
		
		pnlCitujici.add(new JScrollPane(tblChilds),BorderLayout.CENTER);
//----------------- spoluautoøi výpis
		JTextField txtColaborators = new JTextField(10);
		txtColaborators.setEditable(false);
		txtColaborators.setFont(new Font("Tahoma",Font.BOLD,12));
		txtColaborators.setBorder(null);
		txtColaborators.setHorizontalAlignment(JTextField.CENTER);
		txtColaborators.setText("Spolupracovníci: "+String.valueOf(author.getCollaboratorsCount()));
		JPanel pnlSpolupracovnici = new JPanel(new BorderLayout());
		pnlSpolupracovnici.add(txtColaborators,BorderLayout.NORTH);
		
		RefsTableModel<Author> modelSpolupracovnici = new RefsTableModel<Author>(dataInterface.getAuthorDetails(author.getCollaborators()), Author.class);
		tblColaborators.setModel(modelSpolupracovnici);
		tblColaborators.addMouseListener(ms);
		
		pnlSpolupracovnici.add(new JScrollPane(tblColaborators),BorderLayout.CENTER);
//----------------- vložení tabulek do pohledu		
		JPanel pnlRefs = new JPanel(new GridLayout(1, 3));
		pnlRefs.add(pnlCitovani);
		pnlRefs.add(pnlCitujici);
		pnlRefs.add(pnlSpolupracovnici);
		add(new JScrollPane(pnlRefs),BorderLayout.CENTER);
		
		
		
		
		validate();
		repaint();
	}


	@Override
	public List<IdRecord> getDisplayRecords() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setCategorizer(Categorizer<IdRecord, ?> categorizer) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public ViewType getViewType() {
		return ViewType.AUTHOR_DETAIL;
	}
}
