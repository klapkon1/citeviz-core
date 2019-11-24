package cz.uhk.fim.citeviz.gui;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.uhk.fim.citeviz.graph.alg.FruchtermanReingoldAlg;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.gui.components.WrapLayout;

public class LayoutSettings extends JDialog implements ChangeListener{
	
	private static final long serialVersionUID = 1L;
	
	private FruchtermanReingoldAlg layoutAlg;
	
	private JSlider sliGravity = new JSlider(0, 30);
	
	private JSlider sliCanvas = new JSlider(10, 60000);
	
	public LayoutSettings(FruchtermanReingoldAlg layoutAlg) {
		this.layoutAlg = layoutAlg;
		
		setTitle(Localizer.getString("graph.layout.settings"));
		setResizable(false);
		setModal(true);
		setLayout(new WrapLayout());
		
		add(new JLabel(Localizer.getString("graph.layout.settings.gravity")));
		add(sliGravity);
		sliGravity.addChangeListener(this);
		sliGravity.setPaintTicks(true);
		sliGravity.setPaintLabels(true);
		sliGravity.setPaintTrack(true);
		sliGravity.setValue(layoutAlg.getGravity());
		sliGravity.setMajorTickSpacing(10);
		

		add(new JLabel(Localizer.getString("graph.layout.settings.canvas")));
		add(sliCanvas);
		sliCanvas.addChangeListener(this);
		sliCanvas.setPaintTrack(true);
		sliCanvas.setValue(layoutAlg.getCanvas());
		sliCanvas.setMajorTickSpacing(20000);
		sliCanvas.setPaintLabels(true);
		sliGravity.setPaintTicks(true);
		
		
		if (layoutAlg.getAttractNodesEvaluatorAlg() != null && layoutAlg.getAttractNodesEvaluatorAlg().getEvaluatorSettings() != null) {
			add(layoutAlg.getAttractNodesEvaluatorAlg().getEvaluatorSettings());
		}
	
		pack();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == sliGravity) {
			layoutAlg.setGravity(sliGravity.getValue());
		}
		
		if (e.getSource() == sliCanvas) {
			layoutAlg.setCanvas(sliCanvas.getValue());
		}
		
	}
}
