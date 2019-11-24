package cz.uhk.fim.citeviz.graph.alg;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cz.uhk.fim.citeviz.graph.primitives.Graph;
import cz.uhk.fim.citeviz.graph.primitives.Node;

public class GraphComponentAlg {
	
	public static Set<Set<Node<?>>> computeComponents(Graph g){
		Set<Set<Node<?>>> components = new HashSet<>();
		
		List<Node<?>> notProcessed = new LinkedList<>(g.getNodes());
		
		while (!notProcessed.isEmpty()){
			Set<Node<?>> currentComponent = new HashSet<>();
			Node<?> node = notProcessed.remove(0);
			currentComponent.add(node);
			computeComponent(node, notProcessed, currentComponent);
			components.add(currentComponent);
			
		}
		
		return components;
	}

	private static void computeComponent(Node<?> rootNode, List<Node<?>> notProcessed, Set<Node<?>> currentComponent) {
		for (Node<?> node : rootNode.getNeighboursCached()) {
			if (notProcessed.remove(node)){
				currentComponent.add(node);
				computeComponent(node, notProcessed, currentComponent);
			}
		}
	}
}