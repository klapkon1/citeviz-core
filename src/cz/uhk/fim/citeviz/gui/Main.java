package cz.uhk.fim.citeviz.gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.Animator;

import cz.uhk.fim.citeviz.async.TaskManager;
import cz.uhk.fim.citeviz.event.EventProcessor;
import cz.uhk.fim.citeviz.event.listeners.RootRecordSelectListener;
import cz.uhk.fim.citeviz.event.listeners.ShowRecordDetailListener;
import cz.uhk.fim.citeviz.graph.categorizer.Categorizer;
import cz.uhk.fim.citeviz.graph.categorizer.CategorizerBuilder;
import cz.uhk.fim.citeviz.graph.engine.Renderer;
import cz.uhk.fim.citeviz.graph.views.NonGraphicView;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.model.AuthorCategorization;
import cz.uhk.fim.citeviz.model.DataType;
import cz.uhk.fim.citeviz.model.HistoryItem;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.PaperCategorization;
import cz.uhk.fim.citeviz.model.ViewType;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;
import cz.uhk.fim.citeviz.ws.custom.CustomRestConnector;
import cz.uhk.fim.citeviz.ws.scopus.ScopusConnector;


public class Main extends JFrame{
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_INTERFACE_URL = "http://apache/citeseer_api/apiOptimized.php";
	
	private Animator animator;
	
	private DataInterface dataInterface;

	private JComboBox<DataType> cboData = new JComboBox<>(DataType.values());
	
	private JComboBox<ViewType> cboViewPapers;
	
	private JComboBox<ViewType> cboViewAuthors;
	
	private JComboBox<PaperCategorization> cboPaperCategorization;
	
	private JComboBox<AuthorCategorization> cboAuthorCategorization;
	
	private Renderer renderer;

	private JToolBar bottomToolBar;

	private JToolBar pnlStatLegend;
	
	private JComponent glPanel;
	
	private JDialog dlgTaskManager;
	
	private JPanel graphicContainer = new JPanel(new BorderLayout());
	
	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				new Main();
			}
		});
	}
	
	public Main(){
		initAppletParameters();
		setLayout(new BorderLayout());
		
		RecordsPanel pnlRecords = new RecordsPanel(dataInterface, cboData);
		
	    add(pnlRecords, BorderLayout.WEST);
	    
	    
	    add(initTopToolBar(), BorderLayout.NORTH);
	    add(initBottomPanel(), BorderLayout.SOUTH);
	    
	    glPanel = initGLPanel();
	    graphicContainer.add(glPanel);
	    add(graphicContainer, BorderLayout.CENTER);
	    switchView(null);
	    
	    EventProcessor.registerListener(new RootRecordSelectListener(
									    		this, 
									    		renderer, 
									    		dataInterface, 
									    		cboData, 
									    		cboViewPapers, 
									    		cboViewAuthors){

					@Override
					protected void addItemToHistory(HistoryItem item) {
						pnlRecords.getHistoryPanel().addToHistory(item);
					}
	    	
	    });
	    
	    setVisible(true);
	    setTitle("CiteViz");
	    setMinimumSize(new Dimension(800, 600));
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}
	
	private void initAppletParameters(){
		dataInterface = new ScopusConnector();
		//dataInterface = new CustomRestConnector(DEFAULT_INTERFACE_URL);
	}

	private JComponent initGLPanel(){
		GLProfile glProfile = GLProfile.getDefault();
	    GLCapabilities glCapabilities = new GLCapabilities(glProfile);
	    glCapabilities.setRedBits(8);
	    glCapabilities.setBlueBits(8);
	    glCapabilities.setGreenBits(8);
	    glCapabilities.setAlphaBits(8);
	    glCapabilities.setDepthBits(24);
	    ToolTipManager.sharedInstance().setInitialDelay(0);
	    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
	    GLJPanel glJPanel = new GLJPanel(glCapabilities); 
	    renderer = new Renderer(glJPanel, bottomToolBar);
	    glJPanel.addMouseMotionListener(renderer);
	    glJPanel.addKeyListener(renderer);
	    glJPanel.addMouseWheelListener(renderer);
	    glJPanel.addMouseListener(renderer);	
	    glJPanel.addGLEventListener(renderer);
	    animator = new Animator(glJPanel);
	    animator.start();
	    EventProcessor.registerListener(new ShowRecordDetailListener(dataInterface));
	    
	    return glJPanel;
	}
	
	private JComponent initTopToolBar(){
		JToolBar topToolbar = new JToolBar();
		topToolbar.add(new JLabel(Localizer.getString("main.data") + " "));
		topToolbar.add(cboData);
		topToolbar.addSeparator();
		topToolbar.add(new JLabel(Localizer.getString("main.view") + " "));
		cboViewPapers = new JComboBox<>(new ViewType[]{ViewType.PAPER_DETAIL, ViewType.PAPER_TREE_VIEW, ViewType.PAPER_CITATION_NETWORK});
		topToolbar.add(cboViewPapers);
		cboViewAuthors = new JComboBox<>(new ViewType[]{ViewType.AUTHOR_DETAIL, ViewType.AUTHOR_CITATION_NETWORK, ViewType.AUTHOR_COLABORATORS});
		cboViewAuthors.setVisible(false);
		topToolbar.add(cboViewAuthors);
		topToolbar.addSeparator();
		
		
		
		topToolbar.add(new JLabel(Localizer.getString("main.view.categorize") + " "));
		cboPaperCategorization = new JComboBox<>(CategorizerBuilder.paperCategories(dataInterface));
		topToolbar.add(cboPaperCategorization);
		cboPaperCategorization.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!PaperCategorization.NONE.equals(cboPaperCategorization.getSelectedItem())){
					Categorizer<IdRecord, ?> categorizer = CategorizerBuilder.buildCategorizer(renderer, pnlStatLegend, cboPaperCategorization, dataInterface);
					renderer.setCategorizer(categorizer);
					categorizer.showLegend();
				} else {
					renderer.setCategorizer(null);
					pnlStatLegend.removeAll();
					pnlStatLegend.revalidate();
					pnlStatLegend.repaint();
				}
			}
		});
		cboAuthorCategorization = new JComboBox<>(CategorizerBuilder.authorCategories(dataInterface));
		cboAuthorCategorization.setVisible(false);
		cboAuthorCategorization.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!AuthorCategorization.NONE.equals(cboAuthorCategorization.getSelectedItem())){
					Categorizer<IdRecord, ?> categorizer = CategorizerBuilder.buildCategorizer(renderer, pnlStatLegend, cboAuthorCategorization, dataInterface);
					renderer.setCategorizer(categorizer);
					categorizer.showLegend();
				} else {
					renderer.setCategorizer(null);
					pnlStatLegend.removeAll();
					pnlStatLegend.revalidate();
					pnlStatLegend.repaint();
				}
			}
		});
		
		topToolbar.add(cboAuthorCategorization);
		
		cboData.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cboViewPapers.setVisible(cboData.getSelectedIndex() == 0);
				cboViewAuthors.setVisible(cboData.getSelectedIndex() == 1);
				cboPaperCategorization.setVisible(cboData.getSelectedIndex() == 0);
				cboAuthorCategorization.setVisible(cboData.getSelectedIndex() == 1);
			}
		});
		
		JButton btnTaskManager = new JButton(new ImageIcon(getClass().getClassLoader().getResource("taskManager.png")));
		btnTaskManager.setToolTipText(Localizer.getString("taskManager.title"));
		btnTaskManager.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (dlgTaskManager != null && dlgTaskManager.isVisible()){
					return;
				}
				
				dlgTaskManager = new JDialog();
				dlgTaskManager.add(TaskManager.getInstance().getTasksView());
				dlgTaskManager.pack();
				dlgTaskManager.setTitle(Localizer.getString("taskManager.title"));
				dlgTaskManager.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("taskManager.png")).getImage());
				dlgTaskManager.setVisible(true);
				dlgTaskManager.setAlwaysOnTop(true);
			}
		});
		topToolbar.addSeparator();
		topToolbar.add(btnTaskManager);
		
		return topToolbar;
	}
	
	private Component initBottomPanel() {
		JPanel pnlBottom = new JPanel(new BorderLayout());
		bottomToolBar = new JToolBar();
		bottomToolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		
		final JScrollPane scrollPane = new JScrollPane();
		
		pnlStatLegend = new JToolBar(){
			private static final long serialVersionUID = 1L;
			
			@Override
			public void setVisible(boolean aFlag) {
				super.setVisible(aFlag);
				scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth(), Math.min((int)pnlStatLegend.getPreferredSize().getHeight() + 5, 250)));
				pnlBottom.revalidate();
				pnlBottom.repaint();
			}
			
			@Override
			public void removeAll() {
				super.removeAll();
				scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth(), 2));
				pnlBottom.revalidate();
				pnlBottom.repaint();
			}
		};
		scrollPane.setViewportView(pnlStatLegend);
		pnlBottom.add(bottomToolBar, BorderLayout.SOUTH);
		pnlBottom.add(scrollPane, BorderLayout.NORTH);
		return pnlBottom;
	}
	
	public void switchView(NonGraphicView view){
		if (view == null){
			for (Component comp : graphicContainer.getComponents()) {
				if (comp == glPanel){
					return;
				}
			}
		} 
		graphicContainer.removeAll();
		
		if (view == null) {
			graphicContainer.add(glPanel, BorderLayout.CENTER);
			graphicContainer.updateUI();
		} else {
			graphicContainer.add(view, BorderLayout.CENTER);
			graphicContainer.updateUI();
		}

		graphicContainer.revalidate();
		graphicContainer.repaint();
		
	}
	

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			 dataInterface.close();
		}
		
		super.processWindowEvent(e);
	}
}