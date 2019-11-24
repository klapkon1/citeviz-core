package cz.uhk.fim.citeviz.graph.alg.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import cz.uhk.fim.citeviz.graph.primitives.Colors;
import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.NodeRankType;

/**
 * Tøída reprezentující Girvan-Newmanovuv algoritmus
 * 
 * @author Ondøej Klapka
 *
 */
public class GirvanNewmanAlg extends ClusterAlg{

	private Map<IdRecord, Set<Node<?>>> actualNeigbours;

	private int lastThresold = 0;
	
	private int groupCount = 0;
	
	public GirvanNewmanAlg(List<Node<?>> nodes) {
		super(nodes);
		initActualNeigbours();
	}

	private void initActualNeigbours() {
		this.actualNeigbours = new HashMap<IdRecord, Set<Node<?>>>();
		
		for (Node<?> node : getNodes()) {
			actualNeigbours.put(node.getData(), new HashSet<Node<?>>());
			actualNeigbours.get(node.getData()).addAll(node.getNeighboursCached());
		}
	}

	@Override
	public void computeGroups(int thresold) {
		if (thresold < lastThresold){
			lastThresold = 0;
			initActualNeigbours();
		}
		
//		TaskManager.getInstance().startTask(new AsyncTask<Map<BetweennessKey, Float>>() {
//
//			@Override
//			protected Map<BetweennessKey, Float> runTask() throws Throwable {
//				for (int i = lastThresold; i < thresold; i++) {
//					Map<BetweennessKey, Float> result = computeBetweenness();
//					float maxValue = 0;			
//					for (Map.Entry<BetweennessKey, Float> entry : result.entrySet()) {
//						if (entry.getValue() > maxValue){
//							maxValue = entry.getValue();
//						}
//					}
//					synchronized (getLockObject()) {
//						for (Map.Entry<BetweennessKey, Float> entry : result.entrySet()) {
//							if (entry.getValue() == maxValue){
//								actualNeigbours.get(entry.getKey().getNode1().getData()).remove(entry.getKey().getNode2());
//								actualNeigbours.get(entry.getKey().getNode2().getData()).remove(entry.getKey().getNode1());
//							}
//						}
//					}
//				}
//				
//				
//				return null;
//			}
//
//			@Override
//			protected void onFinish(Map<BetweennessKey, Float> result) {
//				groupCount = markNodes();
//				lastThresold = thresold;
//			}
//		});
		
		
		for (int i = lastThresold; i < thresold; i++) {
			Map<BetweennessKey, Float> result = computeBetweenness();
			float maxValue = 0;			
			for (Map.Entry<BetweennessKey, Float> entry : result.entrySet()) {
				if (entry.getValue() > maxValue){
					maxValue = entry.getValue();
				}
			}
			
			for (Map.Entry<BetweennessKey, Float> entry : result.entrySet()) {
				if (entry.getValue() == maxValue){
					actualNeigbours.get(entry.getKey().getNode1().getData()).remove(entry.getKey().getNode2());
					actualNeigbours.get(entry.getKey().getNode2().getData()).remove(entry.getKey().getNode1());
				}
			}
		}
		
		groupCount = markNodes();
		lastThresold = thresold;
		
	}
	
	@Override
	public int getGroupCount() {
		return groupCount;
	}
	
	private int markNodes(){
		resetGroups();

		int currentGroup = 0;
		for (Node<?> node : getNodes()) {
			if (node.getRankByType(NodeRankType.CLUSTER) == null){
				recurrentMark(node, currentGroup);
				currentGroup++;
			}
		}
		for (Node<?> node : getNodes()) {
			node.addRankColor(NodeRankType.CLUSTER, Colors.generateColorFromPallette(currentGroup, node.getRankByType(NodeRankType.CLUSTER)));
		}
		return currentGroup - 1;
	}
	
	private void recurrentMark(Node<?> node, int group){
		if (node.getRankByType(NodeRankType.CLUSTER) != null){
			return;
		}
		
		node.addRank(NodeRankType.CLUSTER, group);
		if (actualNeigbours.containsKey(node.getData())){
			for (Node<?> neighbour : actualNeigbours.get(node.getData())) {
				recurrentMark(neighbour, group);
			}
		}
	}

	public Map<BetweennessKey, Float> computeBetweenness() {

		Map<BetweennessKey, Float> betweenessResult = new HashMap<BetweennessKey, Float>();

		for (Node<?> s : getNodes()) {
			Map<IdRecord, List<Node<?>>> pred = new HashMap<IdRecord, List<Node<?>>>();
			Map<IdRecord, Integer> dist = new HashMap<IdRecord, Integer>();
			Queue<Node<?>> queue = new LinkedBlockingDeque<Node<?>>();
			Stack<Node<?>> stack = new Stack<Node<?>>();
			Map<IdRecord, Integer> count = new HashMap<IdRecord, Integer>();
			Map<IdRecord, Float> dependency = new HashMap<IdRecord, Float>();

			dist.put(s.getData(), 0);
			count.put(s.getData(), 1);

			queue.add(s);

			while (!queue.isEmpty()) {
				Node<?> v = queue.poll();
				stack.push(v);
				for (Node<?> w : actualNeigbours.get(v.getData())) {
					if (dist.get(w.getData()) == null) {
						queue.add(w);
						dist.put(w.getData(), dist.get(v.getData()) + 1);
					}

					if (dist.get(w.getData()) == dist.get(v.getData()) + 1) {
						if (count.get(w.getData()) == null) {
							count.put(w.getData(), 0);
						}

						if (count.get(v.getData()) == null) {
							count.put(v.getData(), 0);
						}

						count.put(w.getData(),
								count.get(w.getData()) + count.get(v.getData()));

						if (pred.get(w.getData()) == null) {
							pred.put(w.getData(), new ArrayList<Node<?>>());
						}
						pred.get(w.getData()).add(v);
					}
				}
			}

			for (Node<?> v : getNodes()) {
				dependency.put(v.getData(), 0f);
			}

			while (!stack.isEmpty()) {
				Node<?> w = stack.pop();

				if (pred.get(w.getData()) == null) {
					continue;
				}

				for (Node<?> v : pred.get(w.getData())) {
					float c = count.get(v.getData()) / count.get(w.getData()) * (1 + dependency.get(w.getData()));
					dependency.put(v.getData(), dependency.get(v.getData()) + c);

					BetweennessKey key = new BetweennessKey(v, w);

					if (!betweenessResult.containsKey(key)) {
						betweenessResult.put(key, 0f);
					}
					betweenessResult.put(key, betweenessResult.get(key) + c);
				}
			}
		}

		return betweenessResult;
	}

	public static class BetweennessKey {
		private final Node<?> node1;

		private final Node<?> node2;

		public BetweennessKey(Node<?> node1, Node<?> node2) {
			this.node1 = node1;
			this.node2 = node2;
		}
		
		public Node<?> getNode1() {
			return node1;
		}
		
		public Node<?> getNode2() {
			return node2;
		}

		@Override
		public int hashCode() {
			return node1.hashCode() + node2.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof BetweennessKey)) {
				return false;
			} else {
				BetweennessKey other = (BetweennessKey) obj;
				if ((node1.equals(other.node1) && node2.equals(other.node2))
						|| (node2.equals(other.node1) && node1
								.equals(other.node2))) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			if (node1.getData().getId()
					.compareToIgnoreCase(node2.getData().getId()) < 0) {
				return node1.getData().getId() + "-" + node2.getData().getId();
			} else {
				return node2.getData().getId() + "-" + node1.getData().getId();
			}
		}
	}
}