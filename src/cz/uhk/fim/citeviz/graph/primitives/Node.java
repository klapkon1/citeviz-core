package cz.uhk.fim.citeviz.graph.primitives;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.NodeRankType;

public class Node<D extends IdRecord> {
	private float x;
	
	private float y;
	
	private NodeShape shape;
	
	private Color color;
	
	private int size;
	
	private D data;
	
	private float tmpX;
	
	private float tmpY;
	
	private float moveX;
	
	private float moveY;
	
	private Set<Node<?>> neighboursCached = new HashSet<Node<?>>();
	
	private boolean fixed;
	
	private Map<NodeRankType, RankBox> nodeRanks = new HashMap<>();
	
	public Node(float x, float y, NodeShape shape, Color color, int size) {
		this(shape, color, size);
		this.x = x;
		this.tmpX = x;
		this.y = y;
		this.tmpY = y;
	}
	
	public Node(NodeShape shape, Color color, int size) {
		this.shape = shape;
		this.color = color;
		this.size = size;
	}
	
	public D getData() {
		return data;
	}

	public void setData(D data) {
		this.data = data;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
		this.tmpX = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
		this.tmpY = y;
	}
	
	public void synchronizePosition() {
		x = tmpX;
		y = tmpY;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColorForDraw(){
		if (!nodeRanks.isEmpty()){
			Iterator<Entry<NodeRankType, Node<D>.RankBox>> iterator = nodeRanks.entrySet().iterator();
			while (iterator.hasNext()){
				Color color = iterator.next().getValue().color;
				if (color != null){
					return color;
				}
			}
		}
		return color;
	}
	
	public NodeShape getShape() {
		return shape;
	}

	public void setShape(NodeShape shape) {
		this.shape = shape;
	}

	public Set<Node<?>> getNeighboursCached() {
		return neighboursCached;
	}
	
	public int getDegree(){
		return neighboursCached.size();
	}

	/**
	 * for internal use only, THIS IS NOT PART OF PUBLIC API
	 * @return
	 */
	public float getTmpX() {
		return tmpX;
	}
	/**
	 * for internal use only, THIS IS NOT PART OF PUBLIC API
	 */
	public void setTmpX(float tmpX) {
		this.tmpX = tmpX;
	}
	/**
	 * for internal use only, THIS IS NOT PART OF PUBLIC API
	 * @return
	 */
	public float getTmpY() {
		return tmpY;
	}
	/**
	 * for internal use only, THIS IS NOT PART OF PUBLIC API
	 */
	public void setTmpY(float tmpY) {
		this.tmpY = tmpY;
	}
	/**
	 * for internal use only, THIS IS NOT PART OF PUBLIC API
	 * @return
	 */
	public float getMoveX() {
		return moveX;
	}
	/**
	 * for internal use only, THIS IS NOT PART OF PUBLIC API
	 */
	public void setMoveX(float moveX) {
		this.moveX = moveX;
	}
	/**
	 * for internal use only, THIS IS NOT PART OF PUBLIC API
	 * @return
	 */
	public float getMoveY() {
		return moveY;
	}
	/**
	 * for internal use only, THIS IS NOT PART OF PUBLIC API
	 */
	public void setMoveY(float moveY) {
		this.moveY = moveY;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public boolean isFixed() {
		return fixed;
	}
	
	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}
	
	public Integer getRankByType(NodeRankType type) {
		return nodeRanks.containsKey(type) ? nodeRanks.get(type).rank : null;
	}
	
	public Color getRankColorByType(NodeRankType type) {
		return nodeRanks.containsKey(type) ? nodeRanks.get(type).color : null;
	}
	
	public void addRank(NodeRankType rankType, int rank, Color color) {
		RankBox box = new RankBox();
		box.rank = rank;
		box.color = color;
		nodeRanks.put(rankType, box);
	}
	
	public void addRank(NodeRankType rankType, int rank) {
		addRank(rankType, rank, null);
	}
	
	public void addRankColor(NodeRankType rankType, Color color){
		RankBox box;
		if (nodeRanks.containsKey(rankType)){
			box = nodeRanks.get(rankType);
		} else {
			box = new RankBox();
			nodeRanks.put(rankType, box);
		}
		box.color = color;
		
	}
	
	public void removeRank(NodeRankType rankType){
		nodeRanks.remove(rankType);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node<?> other = (Node<?>) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}
	
	private class RankBox{
		int rank;
		Color color; 
	}
}