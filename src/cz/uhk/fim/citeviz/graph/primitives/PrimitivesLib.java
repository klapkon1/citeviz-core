package cz.uhk.fim.citeviz.graph.primitives;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class PrimitivesLib {
	
	private static final HashMap<String, Texture> textureMap = new HashMap<String, Texture>();

	public static void fillBlock(GL2 gl, float width, float height, float depth, float[][] color){
		gl.glBegin(GL2.GL_TRIANGLE_STRIP);
		
		//spodni strana
		if (color != null) setColorMaterialForDraw(gl, color[0]);
		gl.glNormal3f(0.0f, -1.0f, 0.0f);
		gl.glVertex3f(0, 0, 0);
		
		if (color != null) setColorMaterialForDraw(gl, color[1]);
		gl.glNormal3f(0.0f, -1.0f, 0.0f);
		gl.glVertex3f(0, height, 0);
		
		if (color != null) setColorMaterialForDraw(gl, color[2]);
		gl.glNormal3f(0.0f, -1.0f, 0.0f);
		gl.glVertex3f(width, 0, 0);
		
		if (color != null) setColorMaterialForDraw(gl, color[3]);
		gl.glNormal3f(0.0f, -1.0f, 0.0f);
		gl.glVertex3f(width, height, 0);
		
		//leva strana
		if (color != null) setColorMaterialForDraw(gl, color[4]); 
		gl.glNormal3f(-1.0f, 0.0f, 0.0f);
		gl.glVertex3f(width, 0, depth);
		
		if (color != null) setColorMaterialForDraw(gl, color[5]);
		gl.glNormal3f(-1.0f, 0.0f, 0.0f);
		gl.glVertex3f(width, height, depth);
		
		//horni strana
		if (color != null) setColorMaterialForDraw(gl, color[6]); 
		gl.glNormal3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(0, 0, depth);
		
		if (color != null) setColorMaterialForDraw(gl, color[7]);
		gl.glNormal3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(0, height, depth);
		
		gl.glEnd();
		gl.glBegin(GL2.GL_TRIANGLE_STRIP);
		
		//predni strana
		if (color != null) setColorMaterialForDraw(gl, color[3]);
		gl.glNormal3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(width, height, 0);
		
		if (color != null) setColorMaterialForDraw(gl, color[5]);
		gl.glNormal3f(0.0f, 1.0f, 0.0f);
	    gl.glVertex3f(width, height, depth);
	    
	    if (color != null) setColorMaterialForDraw(gl, color[1]);
		gl.glVertex3f(0, height, 0);
		gl.glNormal3f(0.0f, 1.0f, 0.0f);
		
		if (color != null) setColorMaterialForDraw(gl, color[7]);
		gl.glNormal3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(0, height, depth);
		
		//prava strana
		if (color != null) setColorMaterialForDraw(gl, color[0]);
		gl.glNormal3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(0, 0, 0);
		
		if (color != null) setColorMaterialForDraw(gl, color[6]);
		gl.glNormal3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(0, 0, depth);
		
		
		//zadni strana
		if (color != null) setColorMaterialForDraw(gl, color[2]);
		gl.glNormal3f(0.0f, -1.0f, 0.0f);
		gl.glVertex3f(width, 0, 0);
		
		if (color != null) setColorMaterialForDraw(gl, color[4]);
		gl.glNormal3f(0.0f, -1.0f, 0.0f);	
		gl.glVertex3f(width, 0, depth);
		gl.glEnd();
	}
	
	public static void fillRectCentered(GL2 gl, float width, float height, float[][] color) {
		gl.glPushMatrix();
		gl.glTranslatef(-width/2f, -height/2f, 0);
		fillRect(gl, width, height, color);
		gl.glPopMatrix();
	}
	
	public static void fillRect(GL2 gl, float width, float height, float[][] color){
		gl.glBegin(GL2.GL_TRIANGLE_FAN);
		if (color != null) setColorForDraw(gl, color[0]);
		gl.glVertex3f(0, 0, 0f);
		if (color != null) setColorForDraw(gl, color[1]);
		gl.glVertex3f(0, height, 0f);
		if (color != null) setColorForDraw(gl, color[2]);
		gl.glVertex3f(width, height, 0f);
		if (color != null) setColorForDraw(gl, color[3]);
		gl.glVertex3f(width, 0, 0f);
		gl.glEnd();
	
	}
	
	public static void rectCentered(GL2 gl, float width, float height, float lineWidth, float[] color){
		setColorForDraw(gl, color);
		gl.glLineWidth(lineWidth);
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glVertex3f(- width / 2f, - height / 2f, 0);
		gl.glVertex3f(+ width / 2f, - height / 2f, 0);
		gl.glVertex3f(+ width / 2f, + height / 2f, 0);
		gl.glVertex3f(- width / 2f, + height / 2f, 0);
		gl.glEnd();
	}
	
	public static void rect(GL2 gl, float width, float height, float lineWidth, float[] color){
		setColorForDraw(gl, color);
		gl.glLineWidth(lineWidth);
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(+ width, 0, 0);
		gl.glVertex3f(+ width, + height, 0);
		gl.glVertex3f(0, + height, 0);
		gl.glVertex3f(0, 0, 0);
		gl.glEnd();
	}
	
	public static void line(GL2 gl, float x1, float y1, float x2, float y2, float width, float[][] color) {
		gl.glLineWidth(width);
		gl.glBegin(GL2.GL_LINES);
		if (color != null) setColorForDraw(gl, color[0]);
		gl.glVertex3f(x1, y1, 0f);
		if (color != null) setColorForDraw(gl, color[1]);
		gl.glVertex3f(x2, y2, 0f);
		gl.glEnd();
	}
	
	public static void lineArrow(GL2 gl, float x1, float y1, float x2, float y2, float width, float arrowSize, float arrowTranslate, float[][] color) {
		gl.glLineWidth(width);
		
		gl.glBegin(GL2.GL_LINES);
		if (color != null) setColorForDraw(gl, color[0]);
		gl.glVertex3f(x1, y1, 0f);
		if (color != null) setColorForDraw(gl, color[1]);
		gl.glVertex3f(x2, y2, 0f);
		gl.glEnd();
		
		
		//šipka
		gl.glPushMatrix();
		float rotate = (float)Math.atan2((y2 - y1), (x2 - x1));
		rotate = (rotate * 180 / (float)Math.PI);
		gl.glTranslatef(x2, y2, 0);
		gl.glRotatef(rotate, 0, 0, 1);
		gl.glTranslatef(-arrowTranslate, 0, 0);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3f(0, 0, 0f);
		gl.glVertex3f(-arrowSize, arrowSize, 0f);
		gl.glVertex3f(0, 0, 0f);
		gl.glVertex3f(-arrowSize, -arrowSize, 0f);
		gl.glEnd();
		gl.glPopMatrix();
	}
	
	
	public static void disk(GL2 gl, float diameter, float[] color) {
		setColorForDraw(gl, color);
		float radius = diameter / 2f;
		gl.glBegin(GL2.GL_TRIANGLE_FAN);
 	    gl.glVertex2f(0, 0);
 	    float step = 0.20943951023931f; // = 2 * Math.PI/30;
 	    for (float i = 0; i < 2 * Math.PI; i += step) {
 	    	gl.glVertex2d(Math.cos(i) * radius, Math.sin(i) * radius);
 	    }
 	    gl.glEnd();
	}
	

	public static void circle(GL2 gl, int diameter, float[] color) {
		setColorForDraw(gl, color);
		
		float radius = diameter / 2f;
		gl.glBegin(GL2.GL_LINE_LOOP);
 	    float step = 0.20943951023931f; // = 2 * Math.PI/30;
 	    for (float i = 0; i < 2 * Math.PI; i += step) {
             gl.glVertex2d(Math.cos(i) * radius, Math.sin(i) * radius);
 	    }
 	    gl.glEnd();
		
	}
	
	public static void fillTriangle(GL2 gl, int size, float[][] color) {
		gl.glBegin(GL2.GL_TRIANGLE_FAN);
		if (color != null) setColorForDraw(gl, color[0]);
		gl.glVertex3f(0, -size / 2f, 0f);
		if (color != null) setColorForDraw(gl, color[1]);
		gl.glVertex3f(-size / 2f, size / 2f, 0f);
		if (color != null) setColorForDraw(gl, color[2]);
		gl.glVertex3f(size / 2f, size / 2f, 0f);
		gl.glEnd();
	}
	
	public static void triangle(GL2 gl, int size, int lineWidth, float[] color) {
		setColorForDraw(gl, color);
		gl.glLineWidth(lineWidth);
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glVertex3f(0, -size / 2f, 0f);
		gl.glVertex3f(-size / 2f, size / 2f, 0f);
		gl.glVertex3f(size / 2f, size / 2f, 0f);
		gl.glEnd();
	}
	
	public static void fillDiamond(GL2 gl, int size, float[][] color) {
		gl.glBegin(GL2.GL_TRIANGLE_FAN);
		if (color != null) setColorForDraw(gl, color[0]);
		gl.glVertex3f(0, -size / 2f, 0f);
		if (color != null) setColorForDraw(gl, color[1]);
		gl.glVertex3f(-size / 2f, 0f, 0f);
		if (color != null) setColorForDraw(gl, color[2]);
		gl.glVertex3f(0, size / 2f, 0f);
		if (color != null) setColorForDraw(gl, color[2]);
		gl.glVertex3f(size / 2f, 0f, 0f);
		gl.glEnd();
	}
	
	public static void diamond(GL2 gl, int size, int lineWidth, float[] color) {
		setColorForDraw(gl, color);
		gl.glLineWidth(lineWidth);
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glVertex3f(0, -size / 2f, 0f);
		gl.glVertex3f(-size / 2f, 0f, 0f);
		gl.glVertex3f(0, size / 2f, 0f);
		gl.glVertex3f(size / 2f, 0f, 0f);
		gl.glEnd();
	}
	
	public static void caption(GL2 gl, GLUT glut, String text, float[] color){
		setColorForDraw(gl, color);
		gl.glPushMatrix();
		gl.glRasterPos2f(0, 0);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, text);
		gl.glPopMatrix(); 
	}
	
	public static void captionMultiLine(GL2 gl, GLUT glut, List<String> lines, float lineHeightScale, float[] color){
		setColorForDraw(gl, color);
		gl.glPushMatrix();
		
		int offset = 0;
		for (String line : lines) {
			gl.glRasterPos2f(0, offset / lineHeightScale);
			offset += 3;
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, line);
		}
		gl.glPopMatrix(); 
	}
	
	public static void icon(GL2 gl, String iconName, float screenWidth, int screenHeight) {
		try {
			Texture texture = null;
			gl.glEnable(GL2.GL_TEXTURE);
			gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
	        if (!textureMap.containsKey(iconName)) {
	            texture = TextureIO.newTexture(new File(PrimitivesLib.class.getClassLoader().getResource(iconName).toURI()), false); 
	            textureMap.put(iconName, texture);
	        } else {
	        	texture = textureMap.get(iconName);
	        }
	        texture.enable(gl);
	        texture.bind(gl);
	        
	       
	        float width = texture.getWidth() / (float) screenWidth;
    		float height = texture.getHeight() / (float) screenHeight;
	
	        gl.glBegin(GL2.GL_QUADS);
	        gl.glTexCoord2f(0f, 0f);
	        gl.glVertex3f(0f, 0f, -0.05f);
	        gl.glTexCoord2f(1f, 0f);
	        gl.glVertex3f(width, 0f, -0.05f);
	        gl.glTexCoord2f(1f, 1f);
	        gl.glVertex3f(width, height, -0.05f);
	        gl.glTexCoord2f(0f, 1f);
	        gl.glVertex3f(0f, height, -0.05f);
	        gl.glEnd();
	        gl.glDisable(GL2.GL_TEXTURE_2D);
		} catch (Exception e) {
			throw new IllegalAccessError("Could not load texture for icon " + iconName);
		}
	}
	
	private static void setColorForDraw(GL2 gl, float[] color){
		if (color == null){
			return;
		}
		
		if (color.length == 3){
			gl.glColor3f(color[0], color[1], color[2]);
		} else if (color.length == 4){
			gl.glColor4f(color[0], color[1], color[2], color[3]);
		} else {
			throw new IllegalArgumentException("Unexpected color array length, expected 3 or 4, but got: " + color.length);
		}
	}
	
	private static void setColorMaterialForDraw(GL2 gl, float[] color){
		if (color == null){
			return;
		}
		
		if (color.length == 3 || color.length == 4){
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, color, 0); 
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, color, 0);
		} else {
			throw new IllegalArgumentException("Unexpected color array length, expected 3 or 4, but got: " + color.length);
		}
	}
}