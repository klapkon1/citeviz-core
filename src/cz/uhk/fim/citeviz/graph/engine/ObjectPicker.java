package cz.uhk.fim.citeviz.graph.engine;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

import cz.uhk.fim.citeviz.event.EventListener;
import cz.uhk.fim.citeviz.event.EventProcessor;
import cz.uhk.fim.citeviz.graph.primitives.Colors;
import cz.uhk.fim.citeviz.model.IdRecord;

public abstract class ObjectPicker {
	
	public static final String EVENT_ON_PICK = "onObjectPick";
	
	public static final String EVENT_LOCK_PICK = "objectLockPick";
	
	private Map<Integer, IdRecord> namedObjects = new HashMap<>(); 
	
	private IdRecord pickedObject;
	
	private IdRecord lastPickedObject;

	private GL2 gl2;
	
	private IntBuffer selectBuffer;
	
	private float alpha = Colors.ALPHA_INVISIBLE, waiting;
	
	private boolean pickerLocked;
	
	public ObjectPicker(GL2 gl2) {
		this.gl2 = gl2;
		EventProcessor.registerListener(new EventListener<IdRecord>() {

			@Override
			public boolean isEventForMe(String eventName) {
				return EVENT_LOCK_PICK.equals(eventName);
			}

			@Override
			public void processEvent(IdRecord eventData) {
				if (eventData == null){
					unlockPickedObject();
				} else {
					lockPickedObject(eventData);
				}
			}

			@Override
			public boolean accept(Object eventData) {
				return eventData instanceof IdRecord;
			}
		});
	}

	public void pushObject(IdRecord record){
		gl2.glPushName(record.getNumericId());
		namedObjects.put(record.getNumericId(), record);
	}
	
	public void releaseLastObject(){
		gl2.glPopName();
	}
	
	
	public void prepareForPicking(GL2 gl2, GLU glu, int mouseX, int mouseY, long rendererSpeed){		
		namedObjects = new HashMap<>();
		pickedObject = null;
		int buffsize = 256;
		selectBuffer = Buffers.newDirectIntBuffer(buffsize);
		int[] viewPort = new int[4]; 
		gl2.glGetIntegerv(GL2.GL_VIEWPORT, viewPort, 0); 
		gl2.glSelectBuffer(buffsize, selectBuffer); 
		gl2.glRenderMode(GL2.GL_SELECT); 
		gl2.glInitNames(); 
		gl2.glMatrixMode(GL2.GL_PROJECTION); 
		gl2.glPushMatrix(); 
		gl2.glLoadIdentity(); 
		glu.gluPickMatrix((double) mouseX, (double) (viewPort[3] - mouseY), 1, 1, viewPort, 0); 
		
		waiting +=(rendererSpeed/1000f); 
 		
 		
 		if (alpha > Colors.ALPHA_INVISIBLE && waiting > 0.5f){
 			alpha -= rendererSpeed*(alpha/300.0f);
 			
 			//mùže se stát že znevyrazneni klesne po výpoètu pod 0.08
 			//proto zarážka
 			if (alpha < Colors.ALPHA_INVISIBLE) alpha = Colors.ALPHA_INVISIBLE;
 		}
    	 
	}
	
	public float getAlphaAfterPicking() {
		return alpha;
	}
	
	
	public void processPicking(GL2 gl2){		
		gl2.glMatrixMode(GL2.GL_PROJECTION); 
        gl2.glPopMatrix(); 
        gl2.glFlush(); 
        processHits(gl2.glRenderMode(GL2.GL_RENDER), selectBuffer);
	}
	 
	private void processHits(int hits, IntBuffer buffer){ 
	     int offset = 0; 
	     int names; 
	     int z = Integer.MAX_VALUE;
	     boolean override = false;
	     for (int i = 0; i < hits; i++){ 
	         names = buffer.get(offset);
	         offset++; // minimum 
	         offset++; // maximum  
	         
	         override = z > buffer.get(offset);
	         if (override){
	        	 z = buffer.get(offset);
	         }
	       
	         offset++; 
	        
	         for (int j = 0; j < names; j++){ 
	        	 if (override && namedObjects.containsKey(buffer.get(offset))){
	        		pickedObject = namedObjects.get(buffer.get(offset));
	        		
	        		if (lastPickedObject != pickedObject){
	        			lastPickedObject = pickedObject;
	        			waiting = 0;
		        		alpha = 1;
		        		onObjectPick(pickedObject);
		        		EventProcessor.sendEvent(EVENT_ON_PICK, pickedObject);
	        		}
	        	}
	            offset++; 
	         } 
	     } 
	     
	     if (pickedObject == null && lastPickedObject != null){
	    	 lastPickedObject = null;
	    	 onObjectPick(null);
	    	 EventProcessor.sendEvent(EVENT_ON_PICK, null);
	     }
	 } 
	 
	 public IdRecord getPickedObject() {
		return pickedObject;
	 }
	 
	 protected abstract void onObjectPick(IdRecord record);
	 
	 public void lockPickedObject(IdRecord pickedObject){
		 this.pickedObject = pickedObject;
		 this.pickerLocked = true;
	 }
	 
	 public void unlockPickedObject(){
		 this.pickedObject = null;
		 this.pickerLocked = false;
	 }
	 
	 public boolean isPickerLocked() {
		return pickerLocked;
	}
}