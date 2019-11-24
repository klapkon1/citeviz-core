package cz.uhk.fim.citeviz.graph.alg;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.gui.components.WrapLayout;
import cz.uhk.fim.citeviz.model.NodeRankType;

public class AttractNodesCategoryEvaluatorAlg extends AttractNodesEvaluatorAlg implements ChangeListener{

	private float categoryForce = 0.5f;
	
	private JSlider sliCategory = new JSlider(1, 30, (int)(categoryForce * 10));
	
	private float edgeForce = 0.3f;
	
	private JSlider sliEdge = new JSlider(1, 30, (int)(edgeForce * 10));
	
	public AttractNodesCategoryEvaluatorAlg() {
		sliCategory.addChangeListener(this);
		sliCategory.setPaintTrack(true);
		sliEdge.addChangeListener(this);
		sliEdge.setPaintTrack(true);
	}
	
	@Override
	public float evaluate(Node<?> n1, Node<?> n2) {
		if (n1.getRankColorByType(NodeRankType.CATEGORY) == n2.getRankColorByType(NodeRankType.CATEGORY)){
			return categoryForce;
		}
		
		return super.evaluate(n1, n2) * edgeForce;
	}
	
	@Override
	public JPanel getEvaluatorSettings() {
		JPanel pnlSettings = new JPanel();
		
		pnlSettings.setLayout(new WrapLayout());
		pnlSettings.add(new JLabel(Localizer.getString("graph.layout.settings.category")));
		pnlSettings.add(sliCategory);
		pnlSettings.add(new JLabel(Localizer.getString("graph.layout.settings.edge")));
		pnlSettings.add(sliEdge);
		
		return pnlSettings;
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == sliCategory) {
			categoryForce = sliCategory.getValue() / 10f;
		}
		
		if (e.getSource() == sliEdge) {
			edgeForce = sliEdge.getValue() / 10f;
		}
	}
}
