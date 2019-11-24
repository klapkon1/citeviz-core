package cz.uhk.fim.citeviz.graph.alg.cluster;

import java.util.List;

import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.model.NodeRankType;

public abstract class ClusterAlg {

	private List<Node<?>> nodes;
	
	public ClusterAlg(List<Node<?>> nodes) {
		this.nodes = nodes;
	}
	
	protected List<Node<?>> getNodes() {
		return nodes;
	}
	
	public abstract void computeGroups(int thresold);
	
	public abstract int getGroupCount();
	
	public void resetGroups() {
		for (Node<?> node : nodes) {
			node.removeRank(NodeRankType.CLUSTER);
		}
	}
}
