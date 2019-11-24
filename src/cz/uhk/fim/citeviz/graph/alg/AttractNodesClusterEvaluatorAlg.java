package cz.uhk.fim.citeviz.graph.alg;

import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.model.NodeRankType;

public class AttractNodesClusterEvaluatorAlg extends AttractNodesEvaluatorAlg {

	
	@Override
	public float evaluate(Node<?> n1, Node<?> n2) {
		
		float result = super.evaluate(n1, n2);
		
		if (result > 0 && n1.getRankByType(NodeRankType.CLUSTER) == n2.getRankByType(NodeRankType.CLUSTER)){
			return result * 15;
		}
		
		return result;
	}
}
