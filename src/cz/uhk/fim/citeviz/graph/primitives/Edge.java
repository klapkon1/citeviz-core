package cz.uhk.fim.citeviz.graph.primitives;

import java.awt.Color;

import cz.uhk.fim.citeviz.model.IdRecord;

/**
 * 
 * @author Ondøej Klapka
 *
 * @param <F>
 * @param <T>
 */
public class Edge<F extends IdRecord, T extends IdRecord> {
	private final Node<F> from;
	
	private final Node<T> to;
	
	private Color color;
	
	private float width = 1.35f;
	
	private boolean bidirectional;

	public Edge(Node<F> from, Node<T> to, Color color) {
		super();
		this.from = from;
		this.to = to;
		this.color = color;

		from.getNeighboursCached().add(to);
		to.getNeighboursCached().add(from);
	}
	
	public Edge(Node<F> from, Node<T> to, Color color, boolean bidirectional) {
		this(from, to, color);
		this.bidirectional = bidirectional;
	}
	
	public Edge(Node<F> from, Node<T> to, Color color, boolean bidirectional, float width) {
		this(from, to, color, bidirectional);
		this.width = width;
	}

	public Node<F> getFrom() {
		return from;
	}

	public Node<T> getTo() {
		return to;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public boolean isBidirectional() {
		return bidirectional;
	}
	
	public void setBidirectional(boolean bidirectional) {
		this.bidirectional = bidirectional;
	}
}
