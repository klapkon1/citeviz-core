package cz.uhk.fim.citeviz.gui;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import cz.uhk.fim.citeviz.graph.alg.GraphComponentAlg;
import cz.uhk.fim.citeviz.graph.engine.ObjectPicker;
import cz.uhk.fim.citeviz.graph.primitives.Graph;
import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.gui.components.DataChart;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.gui.components.SpringUtilities;
import cz.uhk.fim.citeviz.model.IdRecord;

/**
 * Tøída reprezentující formuláø pro zobrazení statistik grafu,
 * výpoèet statistik také probíhá zde
 * @author Ondøej Klapka
 *
 */
public class GraphStats extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	private JButton btnClose = new JButton(Localizer.getString("global.close"));

	private ObjectPicker picker;
	
	public GraphStats(Graph graph, ObjectPicker picker){
		this.picker = picker;
		
		if (graph.getNodes().size() == 0){
			JOptionPane.showMessageDialog(null, Localizer.getString("graphStatsPanel.emptyGraph"), Localizer.getString("graphStatsPanel.emptyGraph.title"), JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource("stats.png")).getImage());
		setLayout(new BorderLayout());
		setTitle(Localizer.getString("graphStatsPanel.title"));
		
		
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.add(Localizer.getString("graphStatsPanel.tabBasic"), initBasicStatsPanel(graph));
		tabs.add(Localizer.getString("graphStatsPanel.tabMetrics"), initMetricStatsPanel(graph));
		add(tabs, BorderLayout.CENTER);
		
		JPanel pnlClose = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnClose.addActionListener(this);
		pnlClose.add(btnClose); 
		add(pnlClose, BorderLayout.SOUTH);
		
		setBounds(200, 200, 800, 600);
		setVisible(true);
	}

	private Component initMetricStatsPanel(Graph graph) {
		JPanel pnlStats = new JPanel(new SpringLayout());
			
		JPanel pnlHistograms = new JPanel(new GridLayout(1, 2, 10, 10));
		computeAndAddClosencessStats(graph, pnlStats, pnlHistograms);
		
		JPanel pnlHistogramHeaders = new JPanel(new GridLayout(1, 2, 10, 10));
		pnlHistogramHeaders.add(new JLabel(Localizer.getString("graphStatsPanel.closenessHistogram")));
		
		JPanel pnlHistogramContainer = new JPanel(new BorderLayout());
		pnlHistogramContainer.add(pnlHistogramHeaders, BorderLayout.NORTH);
		pnlHistogramContainer.add(pnlHistograms, BorderLayout.CENTER);
		
		SpringUtilities.makeCompactGrid(pnlStats, 3, 2, 5, 5, 5, 5);
		
		JPanel pnlTabContainer = new JPanel(new BorderLayout());
		pnlTabContainer.add(pnlStats, BorderLayout.NORTH);
		pnlTabContainer.add(pnlHistogramContainer, BorderLayout.CENTER);
		return pnlTabContainer;
	}

	private Component initBasicStatsPanel(Graph graph) {
		JPanel pnlStats = new JPanel(new SpringLayout());
		
		JPanel pnlHistograms = new JPanel(new GridLayout(1, 2, 10, 10));
		computeAndAddDegreeStats(graph, pnlStats, pnlHistograms);
		computeAndAddComponentStats(graph, pnlStats, pnlHistograms);
		
		
		JPanel pnlHistogramHeaders = new JPanel(new GridLayout(1, 2, 10, 10));
		pnlHistogramHeaders.add(new JLabel(Localizer.getString("graphStatsPanel.degreeHistogram")));
		pnlHistogramHeaders.add(new JLabel(Localizer.getString("graphStatsPanel.componentsHistogram")));
		
		JPanel pnlHistogramContainer = new JPanel(new BorderLayout());
		pnlHistogramContainer.add(pnlHistogramHeaders, BorderLayout.NORTH);
		pnlHistogramContainer.add(pnlHistograms, BorderLayout.CENTER);
		
		SpringUtilities.makeCompactGrid(pnlStats, 8, 2, 5, 5, 5, 5);
		
		JPanel pnlTabContainer = new JPanel(new BorderLayout());
		
		pnlTabContainer.add(pnlStats, BorderLayout.NORTH);
		pnlTabContainer.add(pnlHistogramContainer, BorderLayout.CENTER);
		return pnlTabContainer;
	}

	private void computeAndAddDegreeStats(Graph graph, JPanel pnlStats, JPanel pnlHistograms) {
		int maxDegree = Integer.MIN_VALUE;
		IdRecord maxDegreeData = null;
		int minDegree = Integer.MAX_VALUE;
		IdRecord minDegreeData = null;
		float avgDegree = 0;
		
		for (Node<?> node : graph.getNodes()) {
			if (maxDegree < node.getDegree()){
				maxDegree = node.getDegree();
				maxDegreeData = node.getData();
			}
			
			if (minDegree > node.getDegree()){
				minDegree = node.getDegree();
				minDegreeData = node.getData();
			}
			
			avgDegree += node.getDegree();
		}
		
		avgDegree = avgDegree / graph.getNodes().size();
		
		pnlStats.add(new JLabel(Localizer.getString("graphStatsPanel.nodeCount")));
		pnlStats.add(createValueField(String.valueOf(graph.getNodes().size())));
		
		pnlStats.add(new JLabel(Localizer.getString("graphStatsPanel.edgeCount")));
		pnlStats.add(createValueField(String.valueOf(graph.getEdges().size())));
		
		pnlStats.add(new JLabel(Localizer.getString("graphStatsPanel.degreeAvg")));
		pnlStats.add(createValueField(String.valueOf(Math.round(avgDegree * 1000)/1000f)));
		
		pnlStats.add(new JLabel(Localizer.getString("graphStatsPanel.degreeMax")));
		pnlStats.add(createValueField(maxDegree + " - " + maxDegreeData.getLongCaption()));
		
		pnlStats.add(new JLabel(Localizer.getString("graphStatsPanel.degreeMin")));
		pnlStats.add(createValueField(minDegree + " - " + minDegreeData.getLongCaption()));
		
		
		//HISTOGRAM
		List<Node<?>> nodesCopy = new ArrayList<>(graph.getNodes());
		
		Collections.sort(nodesCopy, new Comparator<Node<?>>() {

			@Override
			public int compare(Node<?> o1, Node<?> o2) {
				return o1.getDegree() - o2.getDegree();
			}
		});
		
		DataChart<Node<?>> histogramDegree = new DataChart<Node<?>>(nodesCopy, Localizer.getString("graphStatsPanel.degreeHistogram.axisX"), Localizer.getString("graphStatsPanel.degreeHistogram.axisY")){
			private static final long serialVersionUID = 1L;

			@Override
			protected void onObjectSelect(Node<?> selectedObject) {
				if (selectedObject != null){
					IdRecord data = selectedObject.getData();
					setToolTipText(data.createHTMLTooltip());
					picker.lockPickedObject(data);
				} else {
					setToolTipText(null);
					picker.unlockPickedObject();
				}
			}

			@Override
			protected float extractValueFromObject(Node<?> object) {
				return object.getNeighboursCached().size();
			}

			@Override
			protected IdRecord extractIdFromObject(Node<?> object) {
				return object.getData();
			}
			
		};
		
		pnlHistograms.add(histogramDegree);
	}
	
	private void computeAndAddComponentStats(Graph graph, JPanel pnlStats, JPanel pnlHistograms) {
		Set<Set<Node<?>>> components = GraphComponentAlg.computeComponents(graph);
	
		//HISTOGRAM
		List<Set<Node<?>>> componentsCopy = new ArrayList<>(components);
				
		Collections.sort(componentsCopy, new Comparator<Set<Node<?>>>() {

			@Override
			public int compare(Set<Node<?>> o1, Set<Node<?>> o2) {
				return o1.size() - o2.size();
			}
		});

		pnlStats.add(new JLabel(Localizer.getString("graphStatsPanel.componentsCount")));
		pnlStats.add(createValueField(String.valueOf(components.size())));
		
		pnlStats.add(new JLabel(Localizer.getString("graphStatsPanel.componentsMax")));
		pnlStats.add(createValueField(String.valueOf(componentsCopy.get(componentsCopy.size() - 1).size())));
		
		pnlStats.add(new JLabel(Localizer.getString("graphStatsPanel.componentsMin")));
		pnlStats.add(createValueField(String.valueOf(componentsCopy.get(0).size())));
		
		
		DataChart<Set<Node<?>>> histogramComponents = new DataChart<Set<Node<?>>>(componentsCopy, Localizer.getString("graphStatsPanel.componentsHistogram.axisX"), Localizer.getString("graphStatsPanel.componentsHistogram.axisY")){
			private static final long serialVersionUID = 1L;

			@Override
			protected void onObjectSelect(Set<Node<?>> selectedObject) {
				
			}

			@Override
			protected float extractValueFromObject(Set<Node<?>> object) {
				return object.size();
			}

			@Override
			protected IdRecord extractIdFromObject(Set<Node<?>> object) {
				return new IdRecord(Localizer.getString("graphStatsPanel.componentsHistogram.axisX") + " (" + object.size() + ")");
			}
			
		};
		
		pnlHistograms.add(histogramComponents);
		
	}
	
	private void computeAndAddClosencessStats(Graph graph, JPanel pnlStats, JPanel pnlHistograms) {
		float maxPathSum = -Float.MAX_VALUE;
		IdRecord maxPathSumData = null;
		float minPathSum = Float.MAX_VALUE;
		IdRecord minPathSumData = null;
		float avgPathSum = 0;
		
		List<CustomRankedNode<Float>> rankedNodes = new ArrayList<>(graph.getNodes().size());
		
		for (Node<?> node : graph.getNodes()) {
			CustomRankedNode<Float> rankedNode = new CustomRankedNode<Float>(node, (float)computePathSum(node));
			rankedNodes.add(rankedNode);
			
			if (maxPathSum < rankedNode.getRank()){
				maxPathSum = rankedNode.getRank();
				maxPathSumData = rankedNode.getNode().getData();
			}
		
			if (minPathSum > rankedNode.getRank()){
				minPathSum = rankedNode.getRank();
				minPathSumData = rankedNode.getNode().getData();
			}
		
			avgPathSum += rankedNode.getRank();
			
		}

		avgPathSum = avgPathSum / rankedNodes.size();
	
		pnlStats.add(new JLabel(Localizer.getString("graphStatsPanel.closenessAvg")));
		pnlStats.add(createValueField(String.valueOf(Math.round(avgPathSum * 1000)/1000f)));
		
		pnlStats.add(new JLabel(Localizer.getString("graphStatsPanel.closenessMax")));
		pnlStats.add(createValueField(maxPathSum + " - " + maxPathSumData.getLongCaption()));
		
		pnlStats.add(new JLabel(Localizer.getString("graphStatsPanel.closenessMin")));
		pnlStats.add(createValueField(minPathSum + " - " + minPathSumData.getLongCaption()));
		
		normalizeValues(rankedNodes, minPathSum, maxPathSum);
		
		//HISTOGRAM
		Collections.sort(rankedNodes, new Comparator<CustomRankedNode<Float>>() {

			@Override
			public int compare(CustomRankedNode<Float> o1, CustomRankedNode<Float> o2) {
				return Float.compare(o1.getRank(), o2.getRank());
			}
		});

		DataChart<CustomRankedNode<Float>> histogramDegree = new DataChart<CustomRankedNode<Float>>(rankedNodes, Localizer.getString("graphStatsPanel.closenessHistogram.axisX"), Localizer.getString("graphStatsPanel.closenessHistogram.axisY")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onObjectSelect(CustomRankedNode<Float> selectedObject) {
				if (selectedObject != null) {
					IdRecord data = selectedObject.getNode().getData();
					setToolTipText(data.createHTMLTooltip());
					picker.lockPickedObject(data);
				} else {
					setToolTipText(null);
					picker.unlockPickedObject();
				}
			}

			@Override
			protected float extractValueFromObject(CustomRankedNode<Float> object) {
				return object.getRank();
			}

			@Override
			protected IdRecord extractIdFromObject(CustomRankedNode<Float> object) {
				return object.getNode().getData();
			}

		};

		pnlHistograms.add(histogramDegree);
		
	}

	private void normalizeValues(List<CustomRankedNode<Float>> rankedNodes, float minPathSum, float maxPathSum) {
		for (CustomRankedNode<Float> customRankedNode : rankedNodes) {
			customRankedNode.setRank(minPathSum / customRankedNode.getRank());
		}
	}

	//FIXME: pocitat s vice komponentami v grafu
	private int computePathSum(Node<?> node) {
		int pathSum = 0;

		Set<Integer> visitedIds = new HashSet<>();

		// Create a queue for BFS
		LinkedList<CustomRankedNode<Integer>> queue = new LinkedList<>();

		// Mark the current node as visited and enqueue it
		visitedIds.add(node.hashCode());
		queue.add(new CustomRankedNode<Integer>(node, 0));
		while (queue.size() != 0) {
			// Dequeue a vertex from queue
			CustomRankedNode<Integer> currentNode = queue.poll();
			
			// Get all adjacent vertices of the dequeued vertex
			// If a adjacent has not been visited, then mark it visited and
			// enqueue it
			for (Node<?> neighbour : currentNode.getNode().getNeighboursCached()) {
				if (!visitedIds.contains(neighbour.hashCode())) {
					visitedIds.add(neighbour.hashCode());
					int steps = currentNode.getRank() + 1;
					queue.add(new CustomRankedNode<Integer>(neighbour, steps));
					pathSum += steps;
				}
			}
		}

		return pathSum;
	}
	
	private JTextField createValueField(String content){
		JTextField txtField = new JTextField(content);
		txtField.setEditable(false);
		txtField.setBorder(null);
		txtField.setCaretPosition(0);
		return txtField;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnClose){
			setVisible(false);
		}
	}
	
	private class CustomRankedNode<T> {
		private Node<?> node;
		private T rank;
		
		public CustomRankedNode(Node<?> node, T rank) {
			this.node = node;
			this.rank = rank;
		}
		public Node<?> getNode() {
			return node;
		}
		public T getRank() {
			return rank;
		}
		
		public void setRank(T rank) {
			this.rank = rank;
		}
	}
}