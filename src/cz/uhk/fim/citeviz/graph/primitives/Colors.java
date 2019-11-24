package cz.uhk.fim.citeviz.graph.primitives;

import java.awt.Color;

import com.jogamp.opengl.GL2;

import cz.uhk.fim.citeviz.util.CiteVizUtils;

public class Colors {
	public static final float[] BACKGROUND = new float[]{0.9f, 0.9f, 0.9f};
	public static final float[] FOCUS = new float[]{0.9f, 0.8f, 0f, 1f};
	public static final float[] EDGE_BIDIRECTIONAL = new float[]{0.3f, 0.3f, 0.3f};
	
	
	public static final float[] REFERENCE = new float[]{1.0f, 0.2f, 0f, 1f};
	public static final float[] CITATION = new float[]{0.5f, 0.7f, 1f, 1f};
	public static final float[] COLABORATOR = new float[]{0.5f, 0.7f, 1f, 1f};
	
	public static final float[] CAPTION = new float[]{0f, 0f, 0f, 1f};
	
	public static final float ALPHA_INVISIBLE = 0.08f;
	
	public static Color generateColorFromPallette(int palletteSize, int colorIndex) {
		
		int lightness, saturation;
		
		
		//lightness compute - use interval 10 - 90 
		//saturation compute - use interval 20 - 100
		
		if (colorIndex % 3 == 0){ 
			if (colorIndex % 2 == 0){ //cca 15 % + cca 15 %
				lightness = CiteVizUtils.convertRange(0, palletteSize, 60, 84, colorIndex);
			} else { 
				lightness = CiteVizUtils.convertRange(0, palletteSize, 16, 40, colorIndex);
			}
			
			//cca 30 %
			saturation = CiteVizUtils.convertRange(0, palletteSize, 33, 59, colorIndex);
		} else if (colorIndex % 5 == 0){
			if (colorIndex % 2 == 0){ //cca 10 % + cca 10 %
				lightness = CiteVizUtils.convertRange(0, palletteSize, 85, 90, colorIndex);
			} else {
				lightness = CiteVizUtils.convertRange(0, palletteSize, 10, 15, colorIndex);
			}
			
			//cca 20 %
			saturation = CiteVizUtils.convertRange(0, palletteSize, 20, 32, colorIndex);
		} else {//cca 50 %
			lightness = CiteVizUtils.convertRange(0, palletteSize, 41, 59, colorIndex);
			saturation = CiteVizUtils.convertRange(0, palletteSize, 60, 100, colorIndex);
		}
		
		return new HSLColor((360/(float)palletteSize)*colorIndex, saturation, lightness).getRGB();
	}
	
	public static boolean isColorDark(Color color){
	    double darkness = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255d;
	    if(darkness < 0.35){
	        return false; // It's a light color
	    }else{
	        return true; // It's a dark color
	    }
	}

	public static void setGlColorFromRGB(GL2 gl, Color color){
		setGlColorFromRGB(gl, color, 1);
	}
	
	public static void setGlColorFromRGB(GL2 gl, Color color, float alpha){
		gl.glColor4ub((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue(), (byte)(alpha * 255));
	}

	public static void setFocusColor(GL2 gl) {
		gl.glColor3f(FOCUS[0], FOCUS[1], FOCUS[2]);
	}
	
	public static void setFocusMaterialColor(GL2 gl){
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, FOCUS, 0); 
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, FOCUS, 0); 
	}
	
	public static void setReferenceColor(GL2 gl){
		gl.glColor3f(REFERENCE[0], REFERENCE[1], REFERENCE[2]);
	}
	
	public static void setReferenceMaterialColor(GL2 gl){
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, REFERENCE, 0); 
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, REFERENCE, 0); 
	}
	
	public static Color getReferenceColor(){
		return new Color(REFERENCE[0], REFERENCE[1], REFERENCE[2]);
	}
	
	public static void setCitationColor(GL2 gl){
		gl.glColor3f(CITATION[0], CITATION[1], CITATION[2]);
	}
	
	public static void setCitationMaterialColor(GL2 gl){
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, CITATION, 0); 
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, CITATION, 0); 
	}
	
	public static Color getCitationColor(){
		return new Color(CITATION[0], CITATION[1], CITATION[2]);
	}
	
	public static Color getColaboratorColor(){
		return new Color(COLABORATOR[0], COLABORATOR[1], COLABORATOR[2]);
	}
	
	public static Color getEdgeColor(){
		return new Color(EDGE_BIDIRECTIONAL[0], EDGE_BIDIRECTIONAL[1], EDGE_BIDIRECTIONAL[2]);
	}
}
