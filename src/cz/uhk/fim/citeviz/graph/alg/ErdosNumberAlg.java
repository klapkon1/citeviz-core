package cz.uhk.fim.citeviz.graph.alg;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;

import cz.uhk.fim.citeviz.graph.engine.ObjectPicker;
import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.gui.components.DataChart;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.NodeRankType;

public class ErdosNumberAlg {

	private int maxNumber;
	
	public JFrame computeErdosNumber(IdRecord start, List<Node<?>> nodes, ObjectPicker picker){		
		List<Node<?>> nodesCopy = new ArrayList<>(nodes);
		
		Node<?> startNode = null;
		maxNumber = 0;
		for (Node<?> node : nodesCopy) {
			node.addRank(NodeRankType.ERDOS, Integer.MAX_VALUE);
			if (node.getData().equals(start)){
				startNode = node;
				startNode.addRank(NodeRankType.ERDOS, 0);
			}
		}
		
		computeErdosNumber(startNode, 1);
		colorNodes(nodesCopy, startNode);
		
		
		Collections.sort(nodesCopy, new Comparator<Node<?>>() {

			@Override
			public int compare(Node<?> o1, Node<?> o2) {
				return o1.getRankByType(NodeRankType.ERDOS).compareTo(o2.getRankByType(NodeRankType.ERDOS));
			}
		});
		
		
		JFrame dialog = new JFrame();
		
		dialog.add(getChart(nodesCopy, picker));
		dialog.setSize(500, 400);
		dialog.setVisible(true);
		return dialog;
	}

	private void colorNodes(List<Node<?>> nodes, Node<?> startNode) {
		for (Node<?> node : nodes) {
			if (!node.equals(startNode)){
				int rank = node.getRankByType(NodeRankType.ERDOS);
				int b = 255 - (int)((rank / (float) maxNumber) * 185);
				int rg = 0;
				node.addRankColor(NodeRankType.ERDOS, new Color(rg, rg, b));
			} else {
				node.addRankColor(NodeRankType.ERDOS, Color.RED);
			}
		}
	}

	private DataChart<Node<?>> getChart(List<Node<?>> nodes, ObjectPicker picker) {
		return new DataChart<Node<?>>(nodes, Localizer.getString("view.colaborators.erdos.value"), Localizer.getString("view.colaborators.erdos.node")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected float extractValueFromObject(Node<?> object) {
				return object.getRankByType(NodeRankType.ERDOS);
			}
			
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
			protected IdRecord extractIdFromObject(Node<?> object) {
				return object.getData();
			}
			
			@Override
			protected Color getColorForColumn(Node<?> val) {
				return val.getRankColorByType(NodeRankType.ERDOS);
			}
		};
	}
	
	private void computeErdosNumber(Node<?> parent, int counter){
		for (Node<?> node : parent.getNeighboursCached()) {
			if (node.getRankByType(NodeRankType.ERDOS) > counter){
				node.addRank(NodeRankType.ERDOS, counter);
				if (counter > maxNumber){
					maxNumber = counter;
				}
				
				computeErdosNumber(node, counter + 1);
				
			}
		}
	}	
}