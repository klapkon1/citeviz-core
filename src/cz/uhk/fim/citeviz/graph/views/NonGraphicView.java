package cz.uhk.fim.citeviz.graph.views;

import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import cz.uhk.fim.citeviz.graph.engine.EyePosition;
import cz.uhk.fim.citeviz.graph.engine.ObjectPicker;
import cz.uhk.fim.citeviz.model.IdRecord;

public abstract class NonGraphicView extends JPanel implements View{

	private static final long serialVersionUID = 1L;

	@Override
	public void initScene(ObjectPicker picker, EyePosition eyePosition) {
		throw new UnsupportedOperationException("NOT IMPLEMENTED FOR " + getClass().getName());
	}

	@Override
	public void drawScene(GL2 gl, GLU glu, GLUT glut, boolean selectMode) {
		throw new UnsupportedOperationException("NOT IMPLEMENTED FOR " + getClass().getName());
	}

	@Override
	public void createToolBar(JToolBar toolBar) {
		throw new UnsupportedOperationException("NOT IMPLEMENTED FOR " + getClass().getName());
	}

	@Override
	public boolean viewDependedCamera(GL2 gl2, GLU glu) {
		throw new UnsupportedOperationException("NOT IMPLEMENTED FOR " + getClass().getName());
	}

	@Override
	public void preparePopupMenu(IdRecord record, JPopupMenu popRecordMenu) {
		throw new UnsupportedOperationException("NOT IMPLEMENTED FOR " + getClass().getName());
	}

	@Override
	public void mouseDragged(GL2 gl2, GLU glu, MouseEvent e) {
		throw new UnsupportedOperationException("NOT IMPLEMENTED FOR " + getClass().getName());
	}

	@Override
	public void mouseDraggedStart(MouseEvent e) {
		throw new UnsupportedOperationException("NOT IMPLEMENTED FOR " + getClass().getName());
	}
	
	@Override
	public boolean viewChanged() {
		throw new UnsupportedOperationException("NOT IMPLEMENTED FOR " + getClass().getName());
	}

	@Override
	public boolean isMultiRoot() {
		return false;
	}
}