package cz.uhk.fim.citeviz.graph.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import junit.framework.TestCase;
import cz.uhk.fim.citeviz.graph.alg.cluster.GirvanNewmanAlg;
import cz.uhk.fim.citeviz.graph.alg.cluster.GirvanNewmanAlg.BetweennessKey;
import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.NodeRankType;

public class GirvanNewmanAlgTestCase extends TestCase {

	
	@Test
	public void testComputeBetweenness(){
		List<Node<?>> nodes = new ArrayList<Node<?>>();
		nodes.add(createNode("A"));
		nodes.add(createNode("B"));
		nodes.add(createNode("C"));
		nodes.add(createNode("D"));
		nodes.add(createNode("E"));
		nodes.add(createNode("F"));
		nodes.add(createNode("G"));
		nodes.add(createNode("H"));
		nodes.add(createNode("I"));

		nodes.get(0).getNeighboursCached().add(nodes.get(1));
		nodes.get(0).getNeighboursCached().add(nodes.get(2));
		nodes.get(0).getNeighboursCached().add(nodes.get(3));
		nodes.get(0).getNeighboursCached().add(nodes.get(7));
		nodes.get(7).getNeighboursCached().add(nodes.get(0));
		nodes.get(8).getNeighboursCached().add(nodes.get(7));
		nodes.get(7).getNeighboursCached().add(nodes.get(8));
		

		nodes.get(1).getNeighboursCached().add(nodes.get(0));
		nodes.get(1).getNeighboursCached().add(nodes.get(2));
		nodes.get(1).getNeighboursCached().add(nodes.get(3));

		nodes.get(2).getNeighboursCached().add(nodes.get(0));
		nodes.get(2).getNeighboursCached().add(nodes.get(1));
		nodes.get(2).getNeighboursCached().add(nodes.get(3));

		nodes.get(3).getNeighboursCached().add(nodes.get(0));
		nodes.get(3).getNeighboursCached().add(nodes.get(1));
		nodes.get(3).getNeighboursCached().add(nodes.get(2));
		nodes.get(3).getNeighboursCached().add(nodes.get(4));

		nodes.get(4).getNeighboursCached().add(nodes.get(3));
		nodes.get(4).getNeighboursCached().add(nodes.get(5));
		nodes.get(4).getNeighboursCached().add(nodes.get(6));

		nodes.get(5).getNeighboursCached().add(nodes.get(4));
		nodes.get(5).getNeighboursCached().add(nodes.get(6));

		nodes.get(6).getNeighboursCached().add(nodes.get(4));
		nodes.get(6).getNeighboursCached().add(nodes.get(5));


		GirvanNewmanAlg alg = new GirvanNewmanAlg(nodes);
		
		Map<BetweennessKey, Float> result = alg.computeBetweenness();

		for (Map.Entry<BetweennessKey, Float> entry : result.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		
		alg.computeGroups(2);
		for (Node<?> node : nodes) {
			System.out.println(node.getData().getId() + ": "  + node.getRankByType(NodeRankType.CLUSTER));
		}

	}

	
	private static Node<IdRecord> createNode(String name) {
		Node<IdRecord> node = new Node<IdRecord>(null, null, 0);
		node.setData(new IdRecord(name));
		return node;
	}
}
