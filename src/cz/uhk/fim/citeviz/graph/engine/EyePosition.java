package cz.uhk.fim.citeviz.graph.engine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

public class EyePosition implements Cloneable{
	private static final float[] DEFAULT_POSITION = new float[]{0, 0, -25}; 
	
	private static final int ZOOM_STEP = 2;
	
	private double[] view = new double[3];
	
	private float[] position = new float[3];
	
	private double[] tangent = new double[3];
	
	private float k = 0.7f;
	
	private float azimut = 0;
	
	private float zenit = 0;
	
	private float[] tempPosition = new float[2];

	private int mouseX;

	private int mouseY;
	
	private float zoomRatio;

	private JButton zoomInButton;

	private JButton zoomOutButton;
	
	private int screenWidth;
	
	private int screenHeight;
	
	private float screenAspectRatio;
	
	private boolean cameraChanged;
	
	public EyePosition() {
		position[0] = DEFAULT_POSITION[0];
		position[1] = DEFAULT_POSITION[1];
		position[2] = DEFAULT_POSITION[2];
		zoomRatio = 1;
		
		view[0] = Math.cos(azimut)*Math.cos(zenit);
		view[1] = Math.sin(azimut)*Math.cos(zenit);
		view[2] = Math.sin(zenit);
		
		tangent[0] = Math.cos(azimut-Math.PI / 2);
		tangent[1] = Math.sin(azimut-Math.PI / 2);
		tangent[2] = 0;
	}
	
	
	public void moveUp(){
		position[0] -= view[0] * k;
		position[1] -= view[1] * k;
		position[2] -= view[2] * k;
		cameraChanged = true;
	}
	
	public void moveDown(){
		position[0] += view[0] * k;
		position[1] += view[1] * k;
		position[2] += view[2] * k;
		cameraChanged = true;
	}
	
	public void moveLeft(){
		position[0] += tangent[0] * k;
		position[1] += tangent[1] * k;
		position[2] += tangent[2] * k;
		cameraChanged = true;
	}
	
	public void moveRight(){
		position[0] -= tangent[0] * k;
		position[1] -= tangent[1] * k;
		position[2] -= tangent[2] * k;
		cameraChanged = true;
	}
	
	public void zoomIn(){
		if (position[2] > -5){
			return;
		}
				
		position[0] += view[2] * k * ZOOM_STEP;
		position[1] += view[1] * k * ZOOM_STEP;
		position[2] += view[0] * k * ZOOM_STEP;
		recalculateZoomRatio();
		cameraChanged = true;
	}
	
	public void zoomOut(){
		position[0] -= view[2] * k * ZOOM_STEP;
		position[1] -= view[1] * k * ZOOM_STEP;
		position[2] -= view[0] * k * ZOOM_STEP;
		recalculateZoomRatio();
		cameraChanged = true;
	}
	
	private void recalculateZoomRatio(){
		zoomRatio = DEFAULT_POSITION[2] / position[2];
	}
	
	public void moveStart(int mouseX, int mouseY){
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.tempPosition[0] = position[0];
		this.tempPosition[1] = position[1];
	}
	
	public void move(int moveX, int moveY){
		position[0] = tempPosition[0] + moveCamera(moveX - mouseX);
		position[1] = tempPosition[1] + moveCamera(moveY - mouseY);
		cameraChanged = true;
	}
	
	private float moveCamera(float screenPosition){
		return -((screenPosition  * position[2])  / (DEFAULT_POSITION[2] * 4));
	}
	
	public static float[] recalculateToModel(GL2 gl2, GLU glu, int winX, int winY){
		IntBuffer viewport = IntBuffer.allocate(4); 
		gl2.glGetIntegerv(GL2.GL_VIEWPORT, viewport); 
		FloatBuffer modelview = FloatBuffer.allocate(16); 
		gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelview);
		FloatBuffer projection = FloatBuffer.allocate(16); 
		gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projection);
		
		winY = (int)viewport.array()[3] - winY;
		
//		FloatBuffer winZ = FloatBuffer.allocate(1);
//		gl2.glReadPixels(winX, winY, 1, 1, GL2.GL_DEPTH_COMPONENT, GL2.GL_FLOAT, winZ);
		
		FloatBuffer result = FloatBuffer.allocate(3);
		
		glu.gluUnProject(winX, winY, 0, modelview, projection, viewport, result);
		
		
		return result.array();
	}
	
	public float[] getPosition() {
		return position;
	}
	
	public double getEyeX(){
		return position[0];
	}
	
	public double getEyeY(){
		return position[1];
	}
	
	public double getEyeZ(){
		return position[2];
	}
	
	public double getCenterX(){
		return view[0] + position[0] - DEFAULT_POSITION[0];		
	}
	
	public double getCenterY(){
		return view[1] + position[1] - DEFAULT_POSITION[1];
	}
	
	public double getCenterZ(){
		return view[2] + position[2] - DEFAULT_POSITION[2];
	}


	public void zoom(boolean zoomMode) {
		if (zoomMode){
			zoomIn();
		} else {
			zoomOut();
		}
	}
	
	public float getZoomRatio() {
		return zoomRatio;
	}
	
	public int getScreenWidth() {
		return screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public float getScreenAspectRatio() {
		return screenAspectRatio;
	}
	
	public void setScreenSize(int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.screenAspectRatio = screenWidth / (float) screenHeight;
	}

	public JButton getZoomInButton(){
		if (zoomOutButton == null){
			zoomOutButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("zoomIn.png")));
			zoomOutButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					zoomIn();
					
				}
			});
		}
		
		return zoomOutButton;
	}


	public JButton getZoomOutButton() {
		if (zoomInButton == null){
			zoomInButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("zoomOut.png")));
			zoomInButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					zoomOut();
				}
			});
		}
		return zoomInButton;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		EyePosition eyePosition = new EyePosition();
		eyePosition.view = view;
		eyePosition.position = position;
		eyePosition.tangent = tangent;
		eyePosition.azimut = azimut;
		eyePosition.zenit = zenit;
		eyePosition.zoomRatio = zoomRatio;
		return eyePosition;
	}
	
	public boolean isCameraChanged() {
		if (cameraChanged) {
			cameraChanged = false;
			return true;
		}
		
		return false;
	}
}