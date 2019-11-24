package cz.uhk.fim.citeviz.graph.engine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import cz.uhk.fim.citeviz.async.TaskManager;
import cz.uhk.fim.citeviz.event.EventProcessor;
import cz.uhk.fim.citeviz.event.listeners.DisplayRecordsChangeListener;
import cz.uhk.fim.citeviz.event.listeners.RootRecordSelectListener;
import cz.uhk.fim.citeviz.event.listeners.ShowRecordDetailListener;
import cz.uhk.fim.citeviz.graph.builder.ViewBuilder;
import cz.uhk.fim.citeviz.graph.categorizer.Categorizer;
import cz.uhk.fim.citeviz.graph.primitives.Colors;
import cz.uhk.fim.citeviz.graph.primitives.PrimitivesLib;
import cz.uhk.fim.citeviz.graph.views.View;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.util.CiteVizUtils;

public class Renderer implements GLEventListener, MouseListener, MouseMotionListener, KeyListener, MouseWheelListener{

	private View view;
	
	private EyePosition eyePosition = new EyePosition();
	
	private ObjectPicker picker;
	
	private int mouseX, mouseY;
	
	private boolean objectDragging;
	
	private MouseEvent dragEvent;
	
	private GLU glu;
		
	private GLUT glut;
	
	private long lastTimeStamp, rendererSpeed;
	
	private GLJPanel container;
	
	private JToolBar tlbView;

	private Categorizer<IdRecord, ?> categorizer;
	
	public Renderer(GLJPanel container, JToolBar tlbView) {
		this.container = container;
		this.tlbView = tlbView;
	}
	
	public void setView(View view) {
		this.view = view;
		view.initScene(picker, eyePosition);
	}
	
	public void setView(View view, EyePosition eyePosition){
		this.eyePosition = eyePosition;
		setView(view);
	}
	
	public View getView() {
		return view;
	}
	
	public EyePosition getEyePosition() {
		return eyePosition;
	}
	
	 @Override
     public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
         this.eyePosition.setScreenSize(width, height);
     }
     
     @Override
     public void init(GLAutoDrawable glAutoDrawable) {
    	GL2 gl2 = glAutoDrawable.getGL().getGL2();
    	gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
 		gl2.glEnable(GL2.GL_LINE_SMOOTH);
 		gl2.glEnable(GL2.GL_BLEND);
 		gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
 		gl2.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
 		gl2.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
 		gl2.glShadeModel(GL2.GL_SMOOTH);
 	    gl2.glEnable(GL2.GL_DEPTH_TEST);
 	   
 	    
 	    glu = new GLU();
 	    glut = new GLUT();
 	    picker = new ObjectPicker(gl2){

			@Override
			protected void onObjectPick(IdRecord record) {
				if (record != null){
					container.setToolTipText(record.createHTMLTooltip());
					//force tooltip show
					MouseEvent phantom = new MouseEvent(container,
			                MouseEvent.MOUSE_MOVED, System.currentTimeMillis(),
			                0, container.getWidth() - 275, container.getHeight() - 160, 0, false);
			        ToolTipManager.sharedInstance().mouseMoved(phantom);
				} else {
					container.setToolTipText(null);
				}
				
				
			}
 	    };
 	    
 	    if (view != null){
 	    	view.initScene(picker, eyePosition);
 	    }
     }
     
     @Override
     public void dispose(GLAutoDrawable glAutoDrawable) {
     }
     
     @Override
     public void display(GLAutoDrawable glAutoDrawable) {
    	 
    	 //DETERMINE RENDERER SPEED
 		 rendererSpeed = System.currentTimeMillis() - lastTimeStamp;
 		 lastTimeStamp = System.currentTimeMillis();
 		
 		 
    	 GL2 gl2 = glAutoDrawable.getGL().getGL2();
    	 gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
 		 gl2.glClearColor(Colors.BACKGROUND[0], Colors.BACKGROUND[1], Colors.BACKGROUND[2], 1);
 
 		 //REDRAW TOOLBAR ON CHANGE
 		 if (view != null){
 			 if (view.viewChanged()){
 				 SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						 view.createToolBar(tlbView);
			 			 tlbView.revalidate();
			 			 tlbView.repaint();
			 			 if (categorizer != null){
			 				 categorizer.setObjects(view.getDisplayRecords());
			 				 categorizer.showLegend();
			 			 }
			 			 
			 			 EventProcessor.sendEvent(DisplayRecordsChangeListener.EVENT_NAME, getDisplayRecords());
					}
				});
 			 }
 			 view.setCategorizer(categorizer);
 		 }
 		 
 		 
 		 //DRAW VIEW
    	 setCamera(gl2, glu, false);
    	 if (view != null){
    		 view.drawScene(gl2, glu, glut, false);
    	 } else {
    		 ViewBuilder.testGraph().renderGraph(gl2, glu, glut, picker, eyePosition, false);
    	 }
    	 
    	//DRAGGING
         if (dragEvent != null){
        	 if (view != null){
     			view.mouseDragged(gl2, glu, dragEvent);
     		} else {
     			ViewBuilder.testGraph().dragged(gl2, glu, dragEvent.getX(), dragEvent.getY(), picker);
     		}
        	 
        	dragEvent = null;
         }
    	 
    	 
    	 //DRAW SUPPORT OBJECTS
    	 if (TaskManager.getInstance().isGroupRunnig(ViewBuilder.DATA_LOADING_THREAD)){
    		 gl2.glPushMatrix();
    		 gl2.glMatrixMode(GL2.GL_PROJECTION);
    		 gl2.glLoadIdentity();
    		 
    		 float[] position = EyePosition.recalculateToModel(gl2, glu, container.getWidth() - 40, 40);
    		 gl2.glTranslatef(position[0], position[1], 0);
    		 if ((System.currentTimeMillis() / 250) % 2 == 0) { //icon blink
    			 PrimitivesLib.icon(gl2, "dataLoadingRed.png", eyePosition.getScreenWidth(), eyePosition.getScreenHeight());
    		 } else {
    			 PrimitivesLib.icon(gl2, "dataLoadingOrange.png", eyePosition.getScreenWidth(), eyePosition.getScreenHeight());
    		 }
    		 gl2.glMatrixMode(GL2.GL_MODELVIEW);
    		 gl2.glPopMatrix();
    	 }
    	 
         //SELECT
    	 if (objectDragging || picker.isPickerLocked()){
    		 //in dragging mode is disabled process picking
    		 return;
    	 }
    	 
    	 picker.prepareForPicking(gl2, glu, mouseX, mouseY, rendererSpeed);
    	 setCamera(gl2, glu, true);
    	 if (view != null){
    		 view.drawScene(gl2, glu, glut, true);
    	 } else {
    		 ViewBuilder.testGraph().renderGraph(gl2, glu, glut, picker, eyePosition, true);
       	 }
    	
    	 picker.processPicking(gl2); 
     }
     
     
 	private void setCamera(GL2 gl2, GLU glu, boolean selectMode){
		if (!selectMode){
			 gl2.glMatrixMode(GL2.GL_PROJECTION);
	         gl2.glLoadIdentity();
		}

        if (view == null || !view.viewDependedCamera(gl2, glu)){   
         	gl2.glOrtho(-eyePosition.getScreenWidth() * (eyePosition.getEyeZ() / 190f), 
         			     eyePosition.getScreenWidth() * (eyePosition.getEyeZ() / 190f), 
         				-eyePosition.getScreenHeight() * (eyePosition.getEyeZ() / 190f), 
         				 eyePosition.getScreenHeight() * (eyePosition.getEyeZ() / 190f), 0.1f, 500.0f);
             
             
            glu.gluLookAt(eyePosition.getEyeX(), eyePosition.getEyeY(), eyePosition.getEyeZ(), 
             			  eyePosition.getCenterX(), eyePosition.getCenterY(), eyePosition.getCenterZ(), 
             			  0, 1, 0);
        }
        

   
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();
      
	}
 	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() != 0){
			eyePosition.zoom(e.getWheelRotation()<0);
		}
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		switch (e.getKeyCode()){
			case KeyEvent.VK_W : eyePosition.zoomIn(); break;
			case KeyEvent.VK_S : eyePosition.zoomOut(); break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!objectDragging){
			eyePosition.move(e.getX(), e.getY());
		} else {
			dragEvent = e;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if  (picker.isPickerLocked()){
			picker.unlockPickedObject();
		}
		
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {		
		if (e.getButton() == MouseEvent.BUTTON3 && picker.getPickedObject() != null){
			JPopupMenu popRecordMenu = new JPopupMenu(Localizer.getString("view.recordMenu"));
			
			JMenuItem itemDetail = new JMenuItem(Localizer.getString("view.recordMenu.detail"));
			itemDetail.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					EventProcessor.sendEvent(ShowRecordDetailListener.EVENT_NAME, picker.getPickedObject());
				}
			});
			popRecordMenu.add(itemDetail);
			
			if (view != null){
				popRecordMenu.addSeparator();
				view.preparePopupMenu(picker.getPickedObject(), popRecordMenu);
			}
			
			popRecordMenu.show(container, e.getX(), e.getY());
		}
	
		if (e.getButton() == MouseEvent.BUTTON1 && picker.getPickedObject() != null && e.getClickCount() == 2){
			EventProcessor.sendEvent(RootRecordSelectListener.EVENT_NAME, CiteVizUtils.asSet(picker.getPickedObject()));
		}
	}
	
	

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1){
			eyePosition.moveStart(e.getX(), e.getY());
			objectDragging = false;
		} else if (e.getButton() == MouseEvent.BUTTON3){
			if (view != null){
				view.mouseDraggedStart(e);
			} else {
				ViewBuilder.testGraph().draggedStart(picker);
			}
			objectDragging = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		objectDragging = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {}	

	@Override
	public void mouseExited(MouseEvent e) {}
	
	public void setCategorizer(Categorizer<IdRecord, ?> categorizer){
		this.categorizer = categorizer;	
	}
	
	public List<IdRecord> getDisplayRecords(){
		return view != null ? view.getDisplayRecords() : null;
	}
}
