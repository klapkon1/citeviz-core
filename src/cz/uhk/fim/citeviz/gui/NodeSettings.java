package cz.uhk.fim.citeviz.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JSlider;

import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.graph.primitives.NodeShape;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.gui.components.WrapLayout;
import cz.uhk.fim.citeviz.util.CiteVizUtils;

public class NodeSettings extends JDialog implements ActionListener{

	private static final long serialVersionUID = 1L;

	private List<Node<?>> nodes;
	
	private JColorChooser cchNodeColor;
	
	private JComboBox<NodeShape> cboNodeShape;
	
	private JSlider sliNodeSize;
	
	private JButton btnOk;
	
	private JButton btnClose;
	
	public NodeSettings(Node<?> node) {
		this(CiteVizUtils.asList(node));
	}
	
	public NodeSettings(List<Node<?>> nodes) {
		this.nodes = nodes;
		
		setTitle(Localizer.getString("graph.node.settings"));
		setModal(true);
		setResizable(false);
		
		setLayout(new WrapLayout());
		
		cboNodeShape = new JComboBox<>(NodeShape.values());
		cboNodeShape.setSelectedItem(nodes.get(0).getShape());
		add(cboNodeShape);
		
		
		sliNodeSize = new JSlider(1, 20, nodes.get(0).getSize());
		sliNodeSize.setPaintTicks(true);
		sliNodeSize.setPaintLabels(true);
		sliNodeSize.setPaintTrack(true);
		sliNodeSize.setMajorTickSpacing(10);
		add(sliNodeSize);
		
		
		cchNodeColor = new JColorChooser(nodes.get(0).getColor());
		add(cchNodeColor);
		
		
		btnOk = new JButton(Localizer.getString("global.ok"));
		btnOk.addActionListener(this);
		add(btnOk);
		
		btnClose = new JButton(Localizer.getString("global.close"));
		btnClose.addActionListener(this);
		add(btnClose);
		
		
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOk) {
			
			for (Node<?> node : nodes) {
				node.setShape((NodeShape) cboNodeShape.getSelectedItem());
				node.setColor(cchNodeColor.getColor());
				node.setSize(sliNodeSize.getValue());
			}
			
			
			setVisible(false);
		}
		
		if (e.getSource() == btnClose) {
			setVisible(false);
		}
	}
	
	public Color getLastSelectedColor() {
		return cchNodeColor.getColor();
	}
	
	public int getLastSelectedSize() {
		return sliNodeSize.getValue();
	}
	
	public NodeShape getLastSelectedShape() {
		return (NodeShape) cboNodeShape.getSelectedItem();
	}
}
