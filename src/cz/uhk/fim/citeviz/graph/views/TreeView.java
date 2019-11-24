package cz.uhk.fim.citeviz.graph.views;

import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import cz.uhk.fim.citeviz.graph.builder.ViewBuilder;
import cz.uhk.fim.citeviz.graph.categorizer.Categorizer;
import cz.uhk.fim.citeviz.graph.engine.EyePosition;
import cz.uhk.fim.citeviz.graph.engine.ObjectPicker;
import cz.uhk.fim.citeviz.graph.primitives.Colors;
import cz.uhk.fim.citeviz.graph.primitives.PrimitivesLib;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;
import cz.uhk.fim.citeviz.model.RefDirection;
import cz.uhk.fim.citeviz.model.ViewType;
import cz.uhk.fim.citeviz.util.CiteVizUtils;
import cz.uhk.fim.citeviz.util.CiteVizUtils.DoubleBox;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;

public class TreeView implements View, ActionListener, ChangeListener{
	
	private static final int LEVEL_HEIGHT = -25; 
	
	private static final int CITATION_INDEX_MAX_HEIGHT = 35;
	
	private static final int CITATION_INDEX_MIN_HEIGHT = 5;
	
	private static float[] LIGHT_POS = new float[]{0, 30, -35};
	
	private ObjectPicker picker;
	
	private EyePosition eyePosition;
	
	private DataInterface dataInterface;
	
	private Set<Paper> displayRecords;
	
	private Map<DoubleBox<IdRecord, Integer>, List<String>> recordsWrappedLabels;
	
	private IdRecord rootId;
	
	private JSlider sliDepth;
	
	private JComboBox<RefDirection> cboRefDirection; 
	
	private JLabel lblDepth;
	
	private JCheckBox chcTree3D;
	
	private boolean viewChanged = true;
	
	private Categorizer<IdRecord, ?> categorizer;

	private float[] leftCorner;

	private float[] rightCorner;

	private int minCitationIndex;

	private int maxCitationIndex;
	
	public TreeView(DataInterface dataInterface, Set<Paper> displayRecords, IdRecord rootId) {
		this.dataInterface = dataInterface;
		this.displayRecords = displayRecords;
		this.rootId = rootId;
		
		
		cboRefDirection = new JComboBox<RefDirection>(RefDirection.values());
		cboRefDirection.addActionListener(this);
		sliDepth = new JSlider(0, 5, 2);
		sliDepth.addChangeListener(this);
		lblDepth = new JLabel(String.valueOf(sliDepth.getValue()));
		chcTree3D = new JCheckBox(Localizer.getString("view.tree.3dswitch"));
		prepareAddititonalData();
	}
	
	@Override
	public void initScene(ObjectPicker picker, EyePosition eyePosition) {
		this.picker = picker;
		this.eyePosition = eyePosition;
	}
		
	@Override
	public void drawScene(GL2 gl, GLU glu, GLUT glut, boolean selectMode) {
		if (rootId == null) {
			return;	
		}
		
		if (chcTree3D.isSelected()){
			placeLight(gl, glu); 
		}		
		
		if (leftCorner == null && rightCorner == null){
			leftCorner = EyePosition.recalculateToModel(gl, glu, 20, eyePosition.getScreenHeight() - 150);
			rightCorner = EyePosition.recalculateToModel(gl, glu, eyePosition.getScreenWidth() - 20, eyePosition.getScreenHeight() - 150);
			
		}
		gl.glTranslatef(0, leftCorner[1], 0);
		
		drawTree(gl, glut, (Paper)rootId, 0, leftCorner[0], rightCorner[0], selectMode);
	}
	

	private void drawTree(GL2 gl, GLUT glut, Paper p, int depth, float pointLeft, float pointRight, boolean selectMode){
		drawPaper(gl, glut, p, depth, pointLeft, pointRight, selectMode);
		
		if (depth < sliDepth.getValue()){
		
			List<Paper> nextLevelPapers = getNextLevelRecords(p);
			float nextLevelWidth = (pointRight - pointLeft) / (float)nextLevelPapers.size();
			
			for (int i = 0; i < nextLevelPapers.size(); i++) {
				drawTree(gl, glut, nextLevelPapers.get(i), depth + 1, pointLeft + (nextLevelWidth * i), pointLeft + (nextLevelWidth * (i + 1)), selectMode);
			}
		}			
	}

	private void drawPaper(GL2 gl, GLUT glut, Paper p, int depth, float pointLeft, float pointRight, boolean selectMode) {
		gl.glPushMatrix();
		gl.glTranslatef(pointLeft, LEVEL_HEIGHT * depth, 0);
		
		if (selectMode){
			picker.pushObject(p);
		} else if (chcTree3D.isSelected()){
			turnOnLight(gl);
		}
		
		boolean colorSetted = false;
		if (picker.getPickedObject() != null){
			if (p.equals(picker.getPickedObject())) {
				if (chcTree3D.isSelected()) Colors.setFocusMaterialColor(gl); else Colors.setFocusColor(gl);
				colorSetted = true;
			} else if (p.getChilds().contains(new IdRecord(picker.getPickedObject().getId()))){
				if (chcTree3D.isSelected()) Colors.setReferenceMaterialColor(gl); else Colors.setReferenceColor(gl);
				colorSetted = true;
			} else if (p.getParents().contains(new IdRecord(picker.getPickedObject().getId()))){
				if (chcTree3D.isSelected()) Colors.setCitationMaterialColor(gl); else Colors.setCitationColor(gl);
				colorSetted = true;
			}
		}
		
		float width = pointRight - pointLeft;
		float alpha = picker.getPickedObject() != null ? picker.getAlphaAfterPicking() : 1;
		
		if (chcTree3D.isSelected()){
			PrimitivesLib.fillBlock(gl, width, LEVEL_HEIGHT, -CiteVizUtils.convertRange(minCitationIndex, maxCitationIndex, CITATION_INDEX_MIN_HEIGHT, CITATION_INDEX_MAX_HEIGHT, p.getCitationIndex()), colorSetted ? null : getColorForPaper3d(p, depth == 0, alpha));
		} else {
			PrimitivesLib.fillRect(gl, width, LEVEL_HEIGHT, colorSetted ? null : getColorForPaper(p, depth == 0, alpha));
			if (width > 1){
				gl.glTranslatef(0, 0, -0.1f);
				PrimitivesLib.rect(gl, width, LEVEL_HEIGHT, 1, new float[]{0.5f, 0.5f, 0.5f, colorSetted ? 0 : alpha});
			}
		}
		
		
		if (selectMode){
			picker.releaseLastObject();
		} else {
			if (chcTree3D.isSelected()){
				turnOffLight(gl);
			}
			
			gl.glPushMatrix();
			gl.glTranslatef(0.5f, LEVEL_HEIGHT + 3, -CiteVizUtils.convertRange(minCitationIndex, maxCitationIndex, CITATION_INDEX_MIN_HEIGHT, CITATION_INDEX_MAX_HEIGHT, p.getCitationIndex()) - 3);
			
			
			if (eyePosition.isCameraChanged()) {
				recordsWrappedLabels.clear();
			}
			
			List<String> captionLines = recordsWrappedLabels.get(new DoubleBox<IdRecord, Integer>(p, depth));
			
			if (captionLines == null) {
				captionLines = CiteVizUtils.getLabelWrapped(p.getTitle(), (int)(width / 2f), 7);
				recordsWrappedLabels.put(new DoubleBox<IdRecord, Integer>(p, depth), captionLines);
			}
			gl.glTranslatef(0, 0, -0.1f);
			PrimitivesLib.captionMultiLine(gl, glut, captionLines, eyePosition.getZoomRatio(), Colors.CAPTION);
			gl.glPopMatrix();
		}
		
		
		gl.glPopMatrix();
	}

	private float[][] getColorForPaper(Paper p, boolean root, float alpha) {
		if (categorizer != null){
			Color  color = categorizer.getColorForObject(p);
			if (color != null){
				float[] categoryColor = new float[]{
						color.getRed() / 255f,
						color.getGreen() / 255f,
						color.getBlue() / 255f,
						categorizer.isCategorySelected(p) ? alpha : Colors.ALPHA_INVISIBLE
				};
				return new float[][]{categoryColor, categoryColor, categoryColor, categoryColor};
			}
		}
		
		
		if (root){
			if (RefDirection.REFERENCE.equals(cboRefDirection.getSelectedItem())){
				return new float[][]{
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha}
				};
			} else {
				return new float[][]{
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha}
				};
			}
		} else {
			if (RefDirection.REFERENCE.equals(cboRefDirection.getSelectedItem())){
				return new float[][]{
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha}
				};
			} else {
				return new float[][]{
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha}
				};
			}
		}
	}

	private float[][] getColorForPaper3d(Paper p, boolean root, float alpha) {
		if (categorizer != null){
			Color  color = categorizer.getColorForObject(p);
			if (color != null){
				float[] categoryColor = new float[]{
						color.getRed() / 255f,
						color.getGreen() / 255f,
						color.getBlue() / 255f,
						categorizer.isCategorySelected(p) ? alpha : Colors.ALPHA_INVISIBLE
				};
				return new float[][]{categoryColor, categoryColor, categoryColor, categoryColor, categoryColor, categoryColor, categoryColor, categoryColor};
			}
		}
		
		
		if (root){
			if (RefDirection.REFERENCE.equals(cboRefDirection.getSelectedItem())){
				return new float[][]{
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha}
				};
			} else {
				return new float[][]{
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha}
				};
			}
		} else {
			if (RefDirection.REFERENCE.equals(cboRefDirection.getSelectedItem())){
				return new float[][]{
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha}
				};
			} else {
				return new float[][]{
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha},
						{Colors.CITATION[0], Colors.CITATION[1], Colors.CITATION[2], alpha},
						{Colors.REFERENCE[0], Colors.REFERENCE[1], Colors.REFERENCE[2], alpha}
				};
			}
		}
	}
	
	private List<Paper> getNextLevelRecords(Paper p) {
		Set<IdRecord> refIds;
		if (RefDirection.CITATION.equals(cboRefDirection.getSelectedItem())){
			refIds = p.getChilds();
		} else {
			refIds = p.getParents();
		}
		
		List<Paper> result = new ArrayList<Paper>();
		
		for (IdRecord ref : refIds) {
			for (Paper paper : displayRecords) {
				if (ref.getId().equals(paper.getId())){
					result.add(paper);
					break;
				}
			}
		}	
		return result;
	}
	
	@Override
	public void createToolBar(JToolBar toolBar) {
		toolBar.removeAll();
		toolBar.setVisible(true);
		toolBar.add(eyePosition.getZoomInButton());
		toolBar.add(eyePosition.getZoomOutButton());
		toolBar.addSeparator();
		toolBar.add(cboRefDirection);
		toolBar.addSeparator();
		toolBar.add(new Label(Localizer.getString("view.searchDepth")));
		toolBar.add(lblDepth);
		toolBar.add(sliDepth);
		toolBar.add(chcTree3D);
	}
	
	@Override
	public List<IdRecord> getDisplayRecords() {
		return new ArrayList<IdRecord>(displayRecords);
	}

	@Override
	public boolean viewDependedCamera(GL2 gl2, GLU glu) {
		if (chcTree3D.isSelected()){
		    glu.gluPerspective(45, eyePosition.getScreenWidth() / (float) eyePosition.getScreenHeight(), 0.1f, 500f);
		    glu.gluLookAt(eyePosition.getEyeX(), eyePosition.getEyeY() + 150, eyePosition.getEyeZ() - 150 /*zavisle na sirce okna*/, 
       			  eyePosition.getCenterX(), eyePosition.getCenterY(), eyePosition.getCenterZ(), 
       			  0, 0, -1);
			return true;
		}
		return false;
	}

	@Override
	public void preparePopupMenu(IdRecord record, JPopupMenu popRecordMenu) {
		
	}

	@Override
	public void mouseDragged(GL2 gl2, GLU glu, MouseEvent e) {
		//nothing
	}

	@Override
	public void mouseDraggedStart(MouseEvent e) {
		//nothing	
	}
	
	@Override
	public boolean isMultiRoot() {
		return false;
	}
	
	@Override
	public boolean viewChanged() {
		if (viewChanged){
			viewChanged = false;
			return true;
		}
		return false;
	}
	
	@Override
	public void setCategorizer(Categorizer<IdRecord, ?> categorizer){
		this.categorizer = categorizer;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cboRefDirection){
			displayRecords = ViewBuilder.treeView(dataInterface, (RefDirection) cboRefDirection.getSelectedItem(), rootId, sliDepth.getValue(), null);
			viewChanged = true;	
			prepareAddititonalData();
		}	
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == sliDepth){
			if (sliDepth.getValue() < Integer.valueOf(lblDepth.getText())){
				displayRecords.clear();
			}
			lblDepth.setText(String.valueOf(sliDepth.getValue()));
			displayRecords = ViewBuilder.treeView(dataInterface, (RefDirection) cboRefDirection.getSelectedItem(), rootId, sliDepth.getValue(), displayRecords);
			viewChanged = true;	
			prepareAddititonalData();
		}	
	}
	
	private void prepareAddititonalData() {
		minCitationIndex = Integer.MAX_VALUE;
		maxCitationIndex = 0;
		
		
		for (Paper paper : displayRecords) {
			if (minCitationIndex > paper.getCitationIndex()) {
				minCitationIndex = paper.getCitationIndex();
			}
			
			if (maxCitationIndex < paper.getCitationIndex()) {
				maxCitationIndex = paper.getCitationIndex();
			}
		}
		recordsWrappedLabels = new HashMap<>();
	}
	
	private void placeLight(GL2 gl, GLU glu){
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[] {0.4f, 0.4f, 0.4f, 1}, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[] {1, 1, 1, 1}, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, LIGHT_POS, 0);
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glPushMatrix();
		gl.glTranslatef(LIGHT_POS[0], LIGHT_POS[1], LIGHT_POS[2]);
		glu.gluSphere(glu.gluNewQuadric(), 5, 30, 10);
		gl.glPopMatrix();
	}
	
	public void turnOnLight(GL2 gl){
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
	}
	
	public void turnOffLight(GL2 gl){
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_LIGHT0);
	}
	
	@Override
	public ViewType getViewType() {
		return ViewType.PAPER_TREE_VIEW;
	}
}