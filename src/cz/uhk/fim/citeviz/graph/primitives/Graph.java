package cz.uhk.fim.citeviz.graph.primitives;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import cz.uhk.fim.citeviz.async.AsyncTask;
import cz.uhk.fim.citeviz.async.TaskManager;
import cz.uhk.fim.citeviz.graph.alg.AttractNodesCategoryEvaluatorAlg;
import cz.uhk.fim.citeviz.graph.alg.AttractNodesClusterEvaluatorAlg;
import cz.uhk.fim.citeviz.graph.alg.AttractNodesEvaluatorAlg;
import cz.uhk.fim.citeviz.graph.alg.FruchtermanReingoldAlg;
import cz.uhk.fim.citeviz.graph.categorizer.Categorizer;
import cz.uhk.fim.citeviz.graph.engine.EyePosition;
import cz.uhk.fim.citeviz.graph.engine.ObjectPicker;
import cz.uhk.fim.citeviz.graph.util.GraphExport;
import cz.uhk.fim.citeviz.gui.ClusterSettings;
import cz.uhk.fim.citeviz.gui.GraphStats;
import cz.uhk.fim.citeviz.gui.LayoutSettings;
import cz.uhk.fim.citeviz.gui.NodeSettings;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.NodeRankType;

public class Graph implements ActionListener{

	public static final String GRAPH_LAYOUT_THREAD = "graphLayout";
	
	private static final int EDGE_LENGTH_RATIO = 3;
	
	private List<Node<?>> nodes = new ArrayList<Node<?>>();
	
	private List<Edge<?, ?>> edges = new ArrayList<Edge<?,?>>();
	
	private FruchtermanReingoldAlg layoutAlg = new FruchtermanReingoldAlg();
	
	private Random randomGenerator = new Random();

    private JCheckBox chcLayout = new JCheckBox(Localizer.getString("graph.layout"));
    
    private JButton btnLayoutSettings = new JButton(new ImageIcon(getClass().getClassLoader().getResource("settings.png")));
    
    private JCheckBox chcClustering = new JCheckBox(Localizer.getString("graph.clustering"));
    
    private JButton btnClusteringSettings = new JButton(new ImageIcon(getClass().getClassLoader().getResource("settings.png")));
    
    private JMenuItem mniFixPos = new JMenuItem(Localizer.getString("graph.node.fix"));
    
    private JMenuItem mniReleasePos = new JMenuItem(Localizer.getString("graph.node.release"));
    
    private Node<?> draggedNode;
    
    private boolean graphChanged;
    
    private boolean showLabels = true;
    
    private Categorizer<IdRecord, ?> categorizer;

	private ClusterSettings dlgClusterSettings;
    
    public Graph() {
		chcLayout.addActionListener(this);
		btnLayoutSettings.setToolTipText(Localizer.getString("graph.layout.settings"));
		btnLayoutSettings.addActionListener(this);
		chcClustering.addActionListener(this);
		btnClusteringSettings.addActionListener(this);
		
		mniFixPos.addActionListener(this);
		mniReleasePos.addActionListener(this);
		
		
		
		onGraphChanged();
	}
	
	public synchronized void renderGraph(GL2 gl, GLU glu, GLUT glut, ObjectPicker picker, EyePosition eye, boolean selectMode){
		
		if (!selectMode && chcLayout.isSelected()) {
			layoutAlg.synchronizePositions(getNodes());
		}
		
		
		gl.glLoadIdentity();
		
		float edgeLength = EDGE_LENGTH_RATIO /** eye.getZoomRatio()*/;
		
		if (edges != null && !selectMode){
			for (Edge<?, ?> edge : edges) {
				float alpha = 1;
				
				if (categorizer != null){
					if (!categorizer.isCategorySelected(edge.getFrom().getData()) || !categorizer.isCategorySelected(edge.getTo().getData())) {
						alpha = Colors.ALPHA_INVISIBLE;
					}
				}
				
				if (picker.getPickedObject() != null){
					if (!picker.getPickedObject().equals(edge.getFrom().getData()) && !picker.getPickedObject().equals(edge.getTo().getData()) && alpha != Colors.ALPHA_INVISIBLE){
						alpha = picker.getAlphaAfterPicking();
					}
					
					if (picker.getPickedObject().equals(edge.getFrom().getData()) || picker.getPickedObject().equals(edge.getTo().getData())) {
						alpha = 1;
					}
				}
				
				Colors.setGlColorFromRGB(gl, edge.getColor(), alpha);
				
				if (edge.isBidirectional()){
					PrimitivesLib.line(gl, 
							edge.getFrom().getX() * edgeLength, 
							edge.getFrom().getY() * edgeLength, 
							edge.getTo().getX() * edgeLength, 
							edge.getTo().getY() * edgeLength, 
							edge.getWidth(),
							null);
				} else {
					PrimitivesLib.lineArrow(gl, 
							edge.getFrom().getX() * edgeLength, 
							edge.getFrom().getY() * edgeLength, 
							edge.getTo().getX() * edgeLength, 
							edge.getTo().getY() * edgeLength, 
							edge.getWidth(),
							1,
							edge.getTo().getSize() / 2f,
							null);
				}
				
				
			}
		}
		
		if (getNodes() != null){
			for (Node<?> node : nodes) {
				if (selectMode && node.getData() != null){
					picker.pushObject(node.getData()); 
				}
				gl.glPushMatrix();
				gl.glTranslatef(node.getX() * edgeLength, node.getY() * edgeLength, -0.03f);
				
				
				float alpha = 0;
				if (!selectMode){
					alpha = setColorForNode(gl, picker, node);
				}
				
				switch (node.getShape()){
					case RECTANGLE : 
						PrimitivesLib.fillRectCentered(gl, node.getSize(), node.getSize(), null); 
						if (node.isFixed()) {
							PrimitivesLib.rectCentered(gl, node.getSize(), node.getSize(), 1, new float[]{0, 0, 0});
						}
						break;
					case CIRCLE : PrimitivesLib.disk(gl, node.getSize(), null); 
						if (node.isFixed()) {
							PrimitivesLib.circle(gl, node.getSize(), new float[]{0, 0, 0});
						}
						break;
						
					case TRIANGLE : PrimitivesLib.fillTriangle(gl, node.getSize(), null); 
						if (node.isFixed()) {
							PrimitivesLib.triangle(gl, node.getSize(), 1, new float[]{0, 0, 0});
						}
						break;
						
					case DIAMOND : PrimitivesLib.fillDiamond(gl, node.getSize(), null); 
						if (node.isFixed()) {
							PrimitivesLib.diamond(gl, node.getSize(), 1, new float[]{0, 0, 0});
						}
						break;
				}
				
				if (!selectMode && showLabels){
					gl.glTranslatef(edgeLength, node.getSize()/2f, 0);
					PrimitivesLib.caption(gl, glut, node.getData().getCaption(), new float[]{0, 0, 0, alpha});
				}
				
				gl.glPopMatrix();
				if (selectMode && node.getData() != null){
					picker.releaseLastObject();	
				}
			}
		}
	}

	private float setColorForNode(GL2 gl, ObjectPicker picker, Node<?> node) {
		if (picker.getPickedObject() != null && picker.getPickedObject().equals(node.getData())){
			Colors.setFocusColor(gl);
			return 1;
		} 
		
		float alpha = 1;
			
		if (picker.getPickedObject() != null){
			alpha = picker.getAlphaAfterPicking();
			for (Edge<?, ?> edge : edges) {
				if (picker.getPickedObject().equals(edge.getFrom().getData()) && edge.getTo().equals(node)){
					if (edge.isBidirectional()){
						Colors.setGlColorFromRGB(gl, node.getColorForDraw());
					} else {
						Colors.setReferenceColor(gl);
					}
					return 1;
				}
				if (picker.getPickedObject().equals(edge.getTo().getData()) && edge.getFrom().equals(node)){
					if (edge.isBidirectional()){
						Colors.setGlColorFromRGB(gl, node.getColorForDraw());
					} else {
						Colors.setCitationColor(gl);
					}
					return 1;
				}
			}
		}
		
		if (categorizer != null){
			IdRecord nodeData = (IdRecord)node.getData();
			node.addRankColor(NodeRankType.CATEGORY, categorizer.getColorForObject(nodeData));
			
		
			if (!categorizer.isCategorySelected(node.getData())){
				alpha = Colors.ALPHA_INVISIBLE;
			}
		} else {
			node.removeRank(NodeRankType.CATEGORY);
		}
			
		Colors.setGlColorFromRGB(gl, node.getColorForDraw(), alpha);
		return alpha;
	}
	
	public synchronized List<Edge<?, ?>> getEdges() {
		return edges;
	}

	public void setEdges(List<Edge<?, ?>> edges) {
		this.edges = edges;
		onGraphChanged();
	}
	
	public void addEdge(Edge<?, ?> edge){
		getEdges().add(edge);
		onGraphChanged();
	}

	public synchronized List<Node<?>> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node<?>> nodes) {
		this.nodes = nodes;
		onGraphChanged();
	}
	
	public void addNode(Node<?> node){	
		//set random node X and Y
		node.setX(randomGenerator.nextFloat() * 20 - 10);
		node.setY(randomGenerator.nextFloat() * 20 - 10);
		
		getNodes().add(node);
		onGraphChanged();
	}
	

	public void onGraphChanged() {
		runLayoutThread();
		chcClustering.setSelected(false);
		dlgClusterSettings = null;
		graphChanged = true;
	}

	public void runLayoutThread() {
		if (TaskManager.getInstance().isGroupRunnig(GRAPH_LAYOUT_THREAD)){
			return;
		}
		chcLayout.setSelected(true);
		layoutAlg.reset();
		TaskManager.getInstance().startTask(new AsyncTask<Void>() {

			@Override
			protected Object getLockObject() {
				return Graph.this;
			}

			@Override
			protected Void runTask() throws Throwable {
				while (isRunning() && chcLayout.isSelected() && !layoutAlg.doIteration(getNodes())) {
					sleep(60);
				}
				return null;
			}

			@Override
			protected void onFinish(Void result) {
				chcLayout.setSelected(false);
			}

			@Override
			protected void onError(Throwable e) {
				chcLayout.setSelected(false);
				e.printStackTrace();
			}

			@Override
			public String getTaskId() {
				return GRAPH_LAYOUT_THREAD;
			}
		}, GRAPH_LAYOUT_THREAD);
	}
	
	public boolean graphChanged() {
		if (graphChanged){
			graphChanged = false;
			return true;
		}
		return false;
	}
	
	public List<JComponent> getLayoutSettings() {
		List<JComponent> layoutSettings = new ArrayList<>();
		layoutSettings.add(chcLayout);
		layoutSettings.add(btnLayoutSettings);
		return layoutSettings;
	}
	
	public List<IdRecord> getDataElements(){
		List<IdRecord> result = new ArrayList<IdRecord>(getNodes().size());
		for (Node<?> node : getNodes()) {
			result.add(node.getData());
		}
		
		return result;
	}
	
	public void preparePopupMenu(IdRecord record, JPopupMenu popRecordMenu) {
		for (Node<?> node : getNodes()) {
			if (node.getData().equals(record)){
				JMenuItem nodeFix = new JMenuItem(Localizer.getString("graph.node.fix"));
				nodeFix.setEnabled(!node.isFixed());
				nodeFix.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						node.setFixed(true);
						
					}
				});
				popRecordMenu.add(nodeFix);
				
				JMenuItem nodeRelease = new JMenuItem(Localizer.getString("graph.node.release"));
				nodeRelease.setEnabled(node.isFixed());
				nodeRelease.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						node.setFixed(false);
					}
				});
				popRecordMenu.add(nodeRelease);
				
				JMenuItem nodeSettings = new JMenuItem(Localizer.getString("graph.node.settings"));
				nodeSettings.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						NodeSettings dlgNodeSettings = new NodeSettings(node);
						dlgNodeSettings.setVisible(true);
					}
				});
				
				popRecordMenu.add(nodeSettings);
			}
		}
	}
	
	public void draggedStart(ObjectPicker picker){
		if (picker.getPickedObject() != null){
			for (Node<?> node : getNodes()) {
				if (picker.getPickedObject().equals(node.getData())){
					draggedNode = node;
					return;
				}
			}
		}
	}

	public void dragged(GL2 gl2, GLU glu, int moveX, int moveY, ObjectPicker picker) {
		if (draggedNode != null){
			draggedNode.setFixed(true);
			runLayoutThread();
			
			float[] modelPos = EyePosition.recalculateToModel(gl2, glu, moveX, moveY);
			
			draggedNode.setX(modelPos[0] / EDGE_LENGTH_RATIO);
			draggedNode.setY(modelPos[1] / EDGE_LENGTH_RATIO);
			
			//TODO dragging
			//draggedNode.setX(draggedX + eyePosition.recalculateToModel(mouseX - moveX));
			//draggedNode.setY(draggedY + eyePosition.recalculateToModel(mouseY - moveY));
			return;
		}
	}

	public List<JComponent> getClusteringSettings() {
		return Arrays.asList(new JComponent[]{chcClustering, btnClusteringSettings});
	}
	
	public JButton getGraphStatsButton(ObjectPicker picker){
		JButton	btnGraphStats = new JButton(new ImageIcon(getClass().getClassLoader().getResource("stats.png")));
		btnGraphStats.addActionListener(new ActionListener() {
				
			@Override
			public void actionPerformed(ActionEvent e) {
				new GraphStats(Graph.this, picker);
			}
		});
		
		btnGraphStats.setToolTipText(Localizer.getString("graphStatsPanel.title"));
		
		return btnGraphStats;
	}
	
	public JButton getGraphExportButton(){
		JButton btnGraphExport = new JButton(new ImageIcon(getClass().getClassLoader().getResource("save.png")));
		btnGraphExport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GraphExport.exportGraph(Graph.this);
			}
		});
		
		btnGraphExport.setToolTipText(Localizer.getString("graphExport.title"));
		
		return btnGraphExport;
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == chcLayout){
			if (chcLayout.isSelected()) {
				runLayoutThread();
			} else { 
				TaskManager.getInstance().stopTask(GRAPH_LAYOUT_THREAD);
			}
		}
		
		if (e.getSource() == btnLayoutSettings) {
			JDialog dlgLayoutSettings = new LayoutSettings(layoutAlg);
			dlgLayoutSettings.setVisible(true);
		}
		
		
		if (e.getSource() == chcClustering){
			runLayoutThread();
			if (chcClustering.isSelected()){
				layoutAlg.setAttractNodesEvaluatorAlg(new AttractNodesClusterEvaluatorAlg());
				layoutAlg.reset();
				if (dlgClusterSettings == null) {
					btnClusteringSettings.doClick();
				}
			} else {
				layoutAlg.setAttractNodesEvaluatorAlg(new AttractNodesEvaluatorAlg());
				layoutAlg.reset();
			}
		}
		
		if (e.getSource() == btnClusteringSettings) {
			if (dlgClusterSettings == null) {
				dlgClusterSettings = new ClusterSettings(this);
			}
			dlgClusterSettings.setVisible(true);
		}
	}
	
	public void setCategorizer(Categorizer<IdRecord, ?> categorizer) {
		this.categorizer = categorizer;
		
		if (categorizer != null) {
			layoutAlg.setAttractNodesEvaluatorAlg(new AttractNodesCategoryEvaluatorAlg());
		}
	}
	
	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}
	
	public void setAttractNodesEvaluator(AttractNodesEvaluatorAlg alg) {
		layoutAlg.setAttractNodesEvaluatorAlg(alg);
	}
}