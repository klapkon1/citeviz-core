package cz.uhk.fim.citeviz.graph.views;

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import cz.uhk.fim.citeviz.graph.alg.ErdosNumberAlg;
import cz.uhk.fim.citeviz.graph.builder.ViewBuilder;
import cz.uhk.fim.citeviz.graph.engine.EyePosition;
import cz.uhk.fim.citeviz.graph.engine.ObjectPicker;
import cz.uhk.fim.citeviz.graph.primitives.Graph;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.ViewType;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;

public class ColaboratorsView extends GraphBasedView implements View{

	private Graph graph;
	
	private ObjectPicker picker;
	
	private EyePosition eyePosition;
	
	public ColaboratorsView(DataInterface dataInterface, Graph graph, Set<IdRecord> rootIds) {
		super(rootIds, dataInterface);
		this.graph = graph;
	}
	
	@Override
	public void drawScene(GL2 gl, GLU glu, GLUT glut, boolean selectMode) {
		graph.renderGraph(gl, glu, glut, picker, eyePosition, selectMode);
	}

	@Override
	public void initScene(ObjectPicker picker, EyePosition eyePosition) {
		this.picker = picker;
		this.eyePosition = eyePosition;
	}
	
	@Override
	public void createToolBar(JToolBar toolBar) {
		toolBar.removeAll();
		toolBar.setVisible(true);
		toolBar.add(eyePosition.getZoomInButton());
		toolBar.add(eyePosition.getZoomOutButton());
		toolBar.addSeparator();
		toolBar.add(new Label(Localizer.getString("view.searchDepth")));
		toolBar.add(getLblDepth());
		toolBar.add(getSliDepth());
		toolBar.addSeparator();
		for (JComponent component : graph.getLayoutSettings()) {
			toolBar.add(component);
		}
		toolBar.addSeparator();
		for (JComponent component : graph.getClusteringSettings()) {
			toolBar.add(component);
		}
		toolBar.addSeparator();
		toolBar.add(graph.getGraphStatsButton(picker));
		toolBar.add(graph.getGraphExportButton());
		toolBar.add(getChcShowLabels());
	}

	@Override
	public void preparePopupMenu(IdRecord record, JPopupMenu popRecordMenu) {
		super.preparePopupMenu(record, popRecordMenu);
		popRecordMenu.add(new JPopupMenu.Separator());
		
		JMenuItem item = new JMenuItem(Localizer.getString("view.colaborators.erdos"));
		item.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new ErdosNumberAlg().computeErdosNumber(record, graph.getNodes(), picker);
			}
		});
		
		popRecordMenu.add(item);
	}

	@Override
	public void mouseDragged(GL2 gl2, GLU glu, MouseEvent e) {
		graph.dragged(gl2, glu, e.getX(), e.getY(), picker);
	}
	
	@Override
	public void mouseDraggedStart(MouseEvent e) {
		graph.draggedStart(picker);
	}
	
	
	@Override
	protected void onDepthChanged(DataInterface dataInterface, boolean newGraphNeeded) {
		graph = ViewBuilder.colaboratorsGraph(dataInterface, getRootRecords(), getSelectedDepth(), newGraphNeeded ? null : graph);
	}
	
	@Override
	public Graph getGraph() {
		return graph;
	}
	
	@Override
	public ViewType getViewType() {
		return ViewType.AUTHOR_COLABORATORS;
	}
}