package cz.uhk.fim.citeviz.graph.views;

import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import cz.uhk.fim.citeviz.graph.categorizer.Categorizer;
import cz.uhk.fim.citeviz.graph.engine.EyePosition;
import cz.uhk.fim.citeviz.graph.engine.ObjectPicker;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.ViewType;

public interface View {
	public void initScene(ObjectPicker picker, EyePosition eyePosition);
	
	public void drawScene(GL2 gl, GLU glu, GLUT glut, boolean selectMode);
	
	public void createToolBar(JToolBar toolBar);
	
	public List<IdRecord> getDisplayRecords();
	
	public boolean viewDependedCamera(GL2 gl2, GLU glu);
	
	public void preparePopupMenu(IdRecord record, JPopupMenu popRecordMenu);

	public void mouseDragged(GL2 gl2, GLU glu, MouseEvent e);
	
	public void mouseDraggedStart(MouseEvent e);
	
	public boolean isMultiRoot();
	
	public boolean viewChanged();

	public void setCategorizer(Categorizer<IdRecord, ?> categorizer);
	
	public ViewType getViewType();
}
