package cz.uhk.fim.citeviz.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.uhk.fim.citeviz.graph.alg.AttractNodesClusterEvaluatorAlg;
import cz.uhk.fim.citeviz.graph.alg.cluster.ClusterAlg;
import cz.uhk.fim.citeviz.graph.alg.cluster.ClusteringType;
import cz.uhk.fim.citeviz.graph.alg.cluster.GirvanNewmanAlg;
import cz.uhk.fim.citeviz.graph.alg.cluster.KeywordsAlg;
import cz.uhk.fim.citeviz.graph.primitives.Graph;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.gui.components.SpringUtilities;

public class ClusterSettings extends JDialog implements ChangeListener, ActionListener {

	private static final long serialVersionUID = 1L;

	private Graph graph;
	
	private JSlider sliClusteringThresold = new JSlider(1, 50, 3);
    
    private JLabel lblClusteringThresoldVal = new JLabel(String.valueOf(sliClusteringThresold.getValue()));
    
    private JComboBox<ClusteringType> cboClusteringType = new JComboBox<>(ClusteringType.values());
   
    private JButton btnProcess = new JButton(Localizer.getString("graph.clutsering.process"));
    
    private JButton btnClose = new JButton(Localizer.getString("global.close"));
    
    private ClusteringType lastSelectedCusteringType;
    
    private ClusterAlg clusterAlg;
    
	public ClusterSettings(Graph graph) {
		this.graph = graph;
		
		sliClusteringThresold.addChangeListener(this);
		btnProcess.addActionListener(this);
		btnClose.addActionListener(this);
		
		setLayout(new BorderLayout());
		JPanel pnlAll = new JPanel(new SpringLayout());
		add(pnlAll, BorderLayout.CENTER);
		
		pnlAll.add(new JLabel(Localizer.getString("graph.clustering.type")));
		pnlAll.add(cboClusteringType);
		
		pnlAll.add(new JLabel(Localizer.getString("graph.clustering.thresold")));
		JPanel pnlThresold = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlThresold.add(sliClusteringThresold);
		pnlThresold.add(lblClusteringThresoldVal);
		pnlAll.add(pnlThresold);
		
		
		
		JPanel pnlClose = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlClose.add(btnClose);
		pnlClose.add(btnProcess);
		
		pnlAll.add(new JPanel());
		pnlAll.add(pnlClose);
		
		SpringUtilities.makeCompactGrid(pnlAll, 3, 2, 5, 5, 5, 5);
		setTitle(Localizer.getString("graph.clustering.title"));
		pack();
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		lblClusteringThresoldVal.setText(String.valueOf(sliClusteringThresold.getValue()));
		
		//TODO: reagovat na zmenu prahu??
//		if (clusterAlg != null) {
//			//clusterAlg.resetGroups();
//			clusterAlg.computeGroups(sliClusteringThresold.getValue());
//			
//			graph.runLayoutThread();
//		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnProcess) {
			ClusteringType selectedClusteringType = (ClusteringType)cboClusteringType.getSelectedItem();
			
			if (lastSelectedCusteringType == null || lastSelectedCusteringType != selectedClusteringType) {
				switch (selectedClusteringType) {
					case STRUCTURAL: clusterAlg = new GirvanNewmanAlg(graph.getNodes()); break;
					case KEYWORDS: clusterAlg = new KeywordsAlg(graph.getNodes()); break;
				
					default: throw new IllegalArgumentException("The clustering type " + selectedClusteringType + " is not supported");
				}
				
				lastSelectedCusteringType = selectedClusteringType;
				clusterAlg.resetGroups();
			}
			
				
			clusterAlg.computeGroups(sliClusteringThresold.getValue());
			graph.setAttractNodesEvaluator(new AttractNodesClusterEvaluatorAlg());
		}
		
		if (e.getSource() == btnClose) {
			setVisible(false);
		}
	}	
}