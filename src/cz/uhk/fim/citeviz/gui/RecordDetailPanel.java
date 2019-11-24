package cz.uhk.fim.citeviz.gui;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.gui.components.SpringUtilities;
import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;
import cz.uhk.fim.citeviz.model.PaperFullDetail;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;

public class RecordDetailPanel extends JFrame{
	
	
	private static final long serialVersionUID = 1L;
	private JPanel pnlClose = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	private JTextField txtPaperURL = new JTextField();
	
	
	public RecordDetailPanel(IdRecord record, DataInterface dataInterface) {
		if (record == null) {
			return;
		}
		
		JButton btnClose = new JButton(Localizer.getString("global.close"));
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		pnlClose.add(btnClose);
		
		if (record instanceof Paper){
			showPaperDetail((Paper) record, dataInterface);
		} else if (record instanceof Author){
			showAuthorDetail((Author)record, dataInterface);
		} else {
			throw new IllegalArgumentException("Unsupported type of record, expected Paper or Author, but got " + record.getClass().getName());
		}
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource("recordDetail.png")).getImage());
		setVisible(true);
		setResizable(false);
	}
	
	private void showAuthorDetail(Author author, DataInterface dataInterface) {
		setTitle(Localizer.getString("detailPanel.title.author", author.getId()));
		
		JPanel pnlAll = new JPanel(new SpringLayout());
		add(new JScrollPane(pnlAll));
		pnlAll.setSize(700, 350);
		
		pnlAll.add(new JLabel(Localizer.getString("detailPanel.name.author")));
		JTextField txtName = new JTextField(author.getName());
		txtName.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		txtName.setEditable(false);
		txtName.setBorder(null);
		txtName.setBackground(null);
		txtName.setSize(100, 20);
		pnlAll.add(txtName);
		
		pnlAll.add(new JLabel(Localizer.getString("detailPanel.id")));
		JTextField txtId = new JTextField(String.valueOf(author.getId()));
		txtId.setEditable(false);
		txtId.setBorder(null);
		txtId.setBackground(null);
		pnlAll.add(txtId);
		
//		pnlAll.add(new JLabel(Localizer.getString("detailPanel.rank")));
//		JTextField txtCitovanost = new JTextField(String.valueOf(author.get?()));
//		txtCitovanost.setEditable(false);
//		txtCitovanost.setBorder(null);
//		txtCitovanost.setBackground(null);
//		pnlAll.add(txtCitovanost);
		
		pnlAll.add(new JLabel(Localizer.getString("detailPanel.papersCount")));
		JTextField txtPapersCount = new JTextField(String.valueOf(author.getPapersId().size()));
		txtPapersCount.setEditable(false);
		txtPapersCount.setBorder(null);
		txtPapersCount.setBackground(null);
		pnlAll.add(txtPapersCount);
		
		
		
		StringBuilder papersStringBuilder = new StringBuilder();
		List<Paper> papers = dataInterface.getPaperDetails(author.getPapersId());
		for (Paper p : papers) {
			papersStringBuilder.append(p.getTitle());
			papersStringBuilder.append(", ");
			papersStringBuilder.append(p.getYear());
			papersStringBuilder.append("\n");
		}
		
		pnlAll.add(new JLabel(Localizer.getString("detailPanel.papers")));
		JTextArea txaPapers = new JTextArea();
		txaPapers.setEditable(false);
		txaPapers.setLineWrap(true);
		txaPapers.setWrapStyleWord(true);
		txaPapers.setRows(10);
		txaPapers.setText(papersStringBuilder.toString());
		txaPapers.setCaretPosition(0);
		pnlAll.add(new JScrollPane(txaPapers));
		 
		
		pnlAll.add(new JPanel()); 
		pnlAll.add(pnlClose);
		
		SpringUtilities.makeCompactGrid(pnlAll, 5, 2, 5, 5, 5, 5);
		setBounds(200, 200, 700, 350);
	}

	private void showPaperDetail(Paper paper, DataInterface dataInterface){
		PaperFullDetail data = dataInterface.getAllDataForPaper(paper);
		setTitle(Localizer.getString("detailPanel.title.paper", paper.getId()));
		
		
		JPanel pnlAll = new JPanel(new SpringLayout());
		add(new JScrollPane(pnlAll));
		pnlAll.setSize(700, 400);
		
		pnlAll.add(new JLabel(Localizer.getString("detailPanel.name.paper")));
		JTextField txtTitle = new JTextField(paper.getTitle());
		txtTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		txtTitle.setEditable(false);
		txtTitle.setBorder(null);
		txtTitle.setBackground(null);
		txtTitle.setSize(100, 20);
		pnlAll.add(txtTitle);
		
		pnlAll.add(new JLabel(Localizer.getString("detailPanel.id")));
		JTextField txtId = new JTextField(String.valueOf(paper.getId()));
		txtId.setEditable(false);
		txtId.setBorder(null);
		txtId.setBackground(null);
		pnlAll.add(txtId);
	 
		
		pnlAll.add(new JLabel(Localizer.getString("detailPanel.published")));
		JTextField txtPublished = new JTextField(String.valueOf(paper.getYear()) + ", " + data.getOtherDataSingleValue(PaperFullDetail.CONTRIBUTOR_KEY));
		txtPublished.setEditable(false);
		txtPublished.setBorder(null);
		txtPublished.setBackground(null);
		pnlAll.add(txtPublished);
		
		pnlAll.add(new JLabel(Localizer.getString("detailPanel.authors")));
		JTextArea txtAuthors = new JTextArea();
		txtAuthors.setEditable(false);
		txtAuthors.setLineWrap(true);
		txtAuthors.setWrapStyleWord(true);
		txtAuthors.setRows(3);
		txtAuthors.setText(paper.getAuthorsAsString());
		txtAuthors.setCaretPosition(0);
		pnlAll.add(new JScrollPane(txtAuthors));
		
		pnlAll.add(new JLabel(Localizer.getString("detailPanel.abstract")));
		JTextArea txtAbstract = new JTextArea();
		txtAbstract.setEditable(false);
		txtAbstract.setLineWrap(true);
		txtAbstract.setWrapStyleWord(true);
		txtAbstract.setRows(10);
		txtAbstract.setText(data.getOtherDataSingleValue(PaperFullDetail.ABSTRACT_KEY));
		txtAbstract.setCaretPosition(0);
		pnlAll.add(new JScrollPane(txtAbstract));
		
		
		pnlAll.add(new JLabel(Localizer.getString("detailPanel.paperURL")));
		txtPaperURL.setText(data.getOtherDataSingleValue(PaperFullDetail.URL_KEY));
		txtPaperURL.setEditable(false);
		txtPaperURL.setBorder(null);
		txtPaperURL.setBackground(null);
		pnlAll.add(txtPaperURL);
		
		MouseAdapter ma = new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getSource() == txtPaperURL){
					try {
						URL url = new URL(txtPaperURL.getText());
						//Main.appletCtx.showDocument(url,"_blank");
					} catch (MalformedURLException err) {
						new JOptionPane(Localizer.getString("detailPanel.paperURL.error"), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR);
					}
				}
			}
		};
		txtPaperURL.addMouseListener(ma);
		
		
		pnlAll.add(new JPanel()); 
		pnlAll.add(pnlClose);
		SpringUtilities.makeCompactGrid(pnlAll, 7, 2, 5, 5, 5, 5);
		setBounds(200, 200, 700, 400);
	}	
}