package cz.uhk.fim.citeviz.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cz.uhk.fim.citeviz.event.EventProcessor;
import cz.uhk.fim.citeviz.event.listeners.SearchRecordsListener;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.model.DataType;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;
import cz.uhk.fim.citeviz.ws.connector.SearchQuery;


/**
 * Tøída implementuje panel, který obsahuje prvky pro nastavení parametrù
 * vyhledávání v databázi
 * @author Ondøej Klapka
 *
 */
public class SearchPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private JLabel lblExpression = new JLabel(Localizer.getString("searchPanel.expression"));
	private JTextField txtExpression = new JTextField(30);
	private JButton btnFind = new JButton(Localizer.getString("searchPanel.find"));
	private JComboBox<String> cboKey = new JComboBox<String>(Localizer.getStrings("searchPanel.title", "searchPanel.author", "searchPanel.year", "searchPanel.abstract"));
	private String selectedKey = DataInterface.SEARCH_KEY_TITLE;
	
	
	public SearchPanel(JComboBox<DataType> cboData) {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(layout.createSequentialGroup()
		    .addComponent(lblExpression)
		    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
		        .addComponent(txtExpression)
		        .addComponent(cboKey))
		    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
		        .addComponent(btnFind))
		);

		layout.setVerticalGroup(layout.createSequentialGroup()
		    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		        .addComponent(lblExpression)
		        .addComponent(txtExpression)
		        .addComponent(btnFind))
		    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
		        .addComponent(cboKey))
		);
		
		
		cboKey.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch (cboKey.getSelectedIndex()) {
					case  1: selectedKey = DataInterface.SEARCH_KEY_AUTHOR_NAME; break;
					case  2: selectedKey = DataInterface.SEARCH_KEY_YEAR;        break;
					case  3: selectedKey = DataInterface.SEARCH_KEY_ABSTRACT;    break;
				    default: selectedKey = DataInterface.SEARCH_KEY_TITLE;       break;
			};
			}
		});
		
		btnFind.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SearchQuery eventData = new SearchQuery(selectedKey, txtExpression.getText(), (DataType)cboData.getSelectedItem(), 0, RecordsNavigator.DEFAULT_PAGE_SIZE);
				EventProcessor.sendEvent(SearchRecordsListener.EVENT_NAME, eventData);
			}
		});
		
		txtExpression.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				btnFind.doClick();
			}
		});

	}
}