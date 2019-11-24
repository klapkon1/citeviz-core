package cz.uhk.fim.citeviz.graph.alg;

import javax.swing.JPanel;

import cz.uhk.fim.citeviz.graph.primitives.Node;

public class AttractNodesEvaluatorAlg {
	
	public float evaluate(Node<?> n1, Node<?> n2) {
		if (n1.getNeighboursCached().contains(n2)) {
			return 1;
		}
		
		return 0;
	}
	
	public JPanel getEvaluatorSettings() {
		return null;
	}
}